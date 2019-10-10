const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

const admin = require('firebase-admin');

admin.initializeApp();

exports.notifyNewMessage = functions.firestore
    .document('chatChannels/{channel}/messages/{message}')
    .onCreate((docSnapshot,context)=> {
        const message = docSnapshot.data();
        const recipientId = message['recipientId'];
        const senderName = message['senderName'];

        return admin.firestore().doc('users/'+recipientId).get().then(userDoc =>{
            const registrationtokens = userDoc.get('registrationtokens')

            const notificationBody = (message['dataType']=== "TEXT") ? message['text']:'you recevied an image message'

            const payload  ={
                notification: {
                    title: senderName + "sent you a message",
                    body: notificationBody,
                    clickAction:"ChatActivity"
                },
                data:{
                    USER_NAME : senderName,
                    USER_ID: message['senderId']
                }
            }
            return admin.messaging().sendToDevice(registrationtokens,payload).then(response =>
                {
                    const stillRegisteredTokens = registrationtokens;

                    response.results.forEach((result,index)=>{
                        const error = result.error
                        if(error){
                            const failedRegistrationToken = registrationtokens[index];
                            console.error('blah',failedRegistrationToken,error);
                            if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registred'){
                                const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken);
                                if(failedIndex > -1)    
                                stillRegisteredtokens.splice(failedIndex,1);



                            }
                        }
                    });
                    return admin.firestore().doc("users/"+recipientId).update({
                        registrationtokens:stillRegisteredTokens
                    });
                    
                });
        });
    });