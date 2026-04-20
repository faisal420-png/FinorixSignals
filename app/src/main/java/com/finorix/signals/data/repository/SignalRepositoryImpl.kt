package com.finorix.signals.data.repository

import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.model.Signal
import com.finorix.signals.domain.repository.SignalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : SignalRepository {

    private val userId: String? get() = auth.currentUser?.uid
    private val signalsCollection get() = userId?.let { db.collection("users").document(it).collection("signals") }

    override fun saveSignal(signal: Signal): Flow<Result<String>> = flow {
        val collection = signalsCollection
        if (collection == null) {
            emit(Result.Error("User not logged in"))
            return@flow
        }
        
        emit(Result.Loading)
        try {
            val docRef = collection.add(signal).await()
            emit(Result.Success(docRef.id))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Failed to save signal"))
        }
    }

    override fun getUserSignals(limit: Int): Flow<List<Signal>> = callbackFlow {
        val collection = signalsCollection
        if (collection == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val listener = collection.orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, _ ->
                val signals = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Signal::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(signals)
            }
        awaitClose { listener.remove() }
    }

    override fun updateSignalOutcome(signalId: String, outcome: String): Flow<Result<Unit>> = flow {
        val collection = signalsCollection
        if (collection == null) {
            emit(Result.Error("User not logged in"))
            return@flow
        }
        
        emit(Result.Loading)
        try {
            collection.document(signalId).update("outcome", outcome).await()
            updateUserStats()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Failed to update outcome"))
        }
    }

    private suspend fun updateUserStats() {
        val uid = userId ?: return
        val collection = signalsCollection ?: return
        
        try {
            val snapshot = collection.get().await()
            val docs = snapshot.documents
            val total = docs.size
            val wins = docs.count { it.getString("outcome") == "WIN" }
            val rate = if (total > 0) (wins.toFloat() / total) * 100 else 0f
            
            db.collection("users").document(uid).update(
                mapOf(
                    "winRate" to rate,
                    "totalSignals" to total
                )
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getWinRate(daysBack: Int): Flow<Float> = callbackFlow {
        val collection = signalsCollection
        if (collection == null) {
            trySend(0f)
            awaitClose { }
            return@callbackFlow
        }
        
        val startTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        val listener = collection.whereGreaterThan("timestamp", startTime)
            .addSnapshotListener { snapshot, _ ->
                val docs = snapshot?.documents ?: emptyList()
                val wins = docs.count { it.getString("outcome") == "WIN" }
                val losses = docs.count { it.getString("outcome") == "LOSS" }
                val total = wins + losses
                val rate = if (total > 0) wins.toFloat() / total else 0f
                trySend(rate)
            }
        awaitClose { listener.remove() }
    }

    override fun observeLatestSignals(): Flow<List<Signal>> = callbackFlow {
        val collection = signalsCollection
        if (collection == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val listener = collection.orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, _ ->
                val signals = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Signal::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(signals)
            }
        awaitClose { listener.remove() }
    }
}
