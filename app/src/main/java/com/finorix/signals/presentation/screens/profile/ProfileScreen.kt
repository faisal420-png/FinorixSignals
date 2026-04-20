package com.finorix.signals.presentation.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.finorix.signals.domain.model.Result
import com.finorix.signals.presentation.screens.auth.AuthViewModel
import com.finorix.signals.presentation.theme.NeonGreen
import com.finorix.signals.presentation.theme.PureBlack
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()
    val context = LocalContext.current
    
    // Launcher for cropping
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                viewModel.uploadProfilePicture(resultUri)
            }
        }
    }

    // Launcher for picking image
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val destinationUri = Uri.fromFile(File(context.cacheDir, "avatar_crop.jpg"))
            val options = UCrop.Options().apply {
                setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                setCompressionQuality(80)
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
            }
            val intent = UCrop.of(uri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(512, 512)
                .withOptions(options)
                .getIntent(context)
            cropLauncher.launch(intent)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(PureBlack).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable { 
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                contentAlignment = Alignment.Center
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user?.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = Color.Gray)
                }
                
                if (uploadState is Result.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize().padding(2.dp), 
                        color = NeonGreen,
                        strokeWidth = 4.dp
                    )
                }
            }
            
            Surface(
                color = NeonGreen,
                shape = CircleShape,
                modifier = Modifier.size(36.dp).offset(x = 4.dp, y = 4.dp)
            ) {
                Icon(
                    Icons.Default.CameraAlt, 
                    null, 
                    modifier = Modifier.padding(8.dp), 
                    tint = PureBlack
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(user?.displayName ?: "Finorix User", fontSize = 22.sp, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text(user?.email ?: "", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.DarkGray.copy(alpha = 0.3f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Plan", color = Color.Gray, fontSize = 12.sp)
                    Text("FREE", color = NeonGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Signals", color = Color.Gray, fontSize = 12.sp)
                    Text("128", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Accuracy", color = Color.Gray, fontSize = 12.sp)
                    Text("74%", color = NeonGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}
