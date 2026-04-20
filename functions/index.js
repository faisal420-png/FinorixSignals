const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Triggers when a new document is created in the /broadcasts/ collection.
 * Sends a push notification to all users subscribed to the 'all_users' topic.
 */
exports.onBroadcastCreated = functions.firestore
    .document('broadcasts/{broadcastId}')
    .onCreate(async (snapshot, context) => {
        const broadcast = snapshot.data();
        
        const payload = {
            notification: {
                title: '📢 Finorix Global Signal',
                body: `${broadcast.pair} — ${broadcast.direction} signal with ${broadcast.confidence}% confidence`,
            },
            data: {
                type: 'broadcast',
                pair: broadcast.pair,
                direction: broadcast.direction,
                confidence: broadcast.confidence.toString()
            },
            topic: 'all_users'
        };

        try {
            const response = await admin.messaging().send(payload);
            console.log('Successfully sent broadcast notification:', response);
            return response;
        } catch (error) {
            console.error('Error sending broadcast notification:', error);
            throw error;
        }
    });

/* 
DEPLOAYMENT INSTRUCTIONS:
1. Ensure you have the Firebase CLI installed: npm install -g firebase-tools
2. Login: firebase login
3. Initialize (if not done): firebase init functions
4. Deploy: firebase deploy --only functions

Note: Cloud Functions requires the Firebase Blaze (pay-as-you-go) plan.
*/
