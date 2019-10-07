package com.example.sony.chatme.util


import android.content.Context
import android.util.Log
import com.example.sony.chatme.model.*
import com.example.sony.chatme.recyclerview.item.ImageMessageItem
import com.example.sony.chatme.recyclerview.item.PersonItem
import com.example.sony.chatme.recyclerview.item.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlin.math.log

object FirebaseUtil {


    private val firebaseStoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val currentUserDocRef: DocumentReference
        get() = firebaseStoreInstance.document(
            "users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null")}"
        )
    private val chatChannelCollectionRef = firebaseStoreInstance.collection("chatChannels")

    fun initCurrentUserIfFirstTiime(onComplet: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                    FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                    "",
                    null
                )
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplet()
                }
            } else
                onComplet()
        }
    }

    fun updateCurrentUser(name: String, bio: String, picturePath: String? = null) {
        val userFeildMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFeildMap["userName"] = name
        if (bio.isNotBlank()) userFeildMap["bio"] = bio
        if (picturePath != null) userFeildMap["profilPicturPath"] = picturePath
        currentUserDocRef.update(userFeildMap)
    }

    fun getCurrentUser(onComplet: (User) -> Unit) {
        currentUserDocRef.get()
            .addOnSuccessListener {
                it.toObject(User::class.java)?.let { it1 -> onComplet(it1) }
            }
    }

    fun addUsersListener(context: Context,onListen: (List<Item>) -> Unit): ListenerRegistration{
        return firebaseStoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException !=null) {
                    Log.e("Firestore", "Users Listener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach{
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(PersonItem(it.toObject(User::class.java)!!,it.id,context))

                }
                onListen(items)
            }


    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()


    fun getOrCreateChatChannel(otherUserId:String,
                               onComplet: (channelId:String) -> Unit){
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if(it.exists()){
                    onComplet(it["channelId"] as String)
                    return@addOnSuccessListener
                }
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChatChannel = chatChannelCollectionRef.document()
                newChatChannel.set(ChatChannel(mutableListOf(currentUserId,otherUserId)))

                currentUserDocRef.collection("engagedChatChannels")
                    .document(otherUserId)
                    .set(mapOf("channelId" to newChatChannel.id))

                firebaseStoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChatChannel.id))

                onComplet(newChatChannel.id)

            }

    }


    fun addChatMessagesListener(channelId: String,context: Context,onListen: (List<Item>) -> Unit):ListenerRegistration{
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null){
                    Log.e("FireStore","ChatMessageListener error",firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach{
                    if (it["dataType"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!,context))
                    else
                        items.add(ImageMessageItem(it.toObject(ImageMessage::class.java)!!, context))


                }
                onListen(items)
            }
    }


    fun sendMessage(message:Message,chanelid:String){
        chatChannelCollectionRef.document(chanelid)
            .collection("messages")
            .add(message)
    }
}
