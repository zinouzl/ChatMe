package com.example.sony.chatme.util


import android.content.Context
import android.util.Log
import com.example.sony.chatme.model.*
import com.example.sony.chatme.recyclerview.item.ChatChannelItem
import com.example.sony.chatme.recyclerview.item.ImageMessageItem
import com.example.sony.chatme.recyclerview.item.PersonItem
import com.example.sony.chatme.recyclerview.item.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.xwray.groupie.kotlinandroidextensions.Item
import java.util.*

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
                    null,
                    mutableListOf()
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

    fun addUsersListener(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firebaseStoreInstance.collection("users")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("Firestore", "Users Listener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()

                querySnapshot?.documents?.forEach {
                    if (it.id != FirebaseAuth.getInstance().currentUser?.uid) {
                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))

                    }
                }



                onListen(items)
            }


    }


    fun addChatChannelsListener(
        context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        return currentUserDocRef.collection("engagedChatChannels")
            .addSnapshotListener(MetadataChanges.INCLUDE) { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("Firestore", "Users Listener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }


                val chatChannelItems = mutableListOf<ChatChannelItem>()
                val documentSize = querySnapshot?.documents?.size
                var counter = 0

                querySnapshot?.documents?.forEach { document ->

                    var person: User
                    val userId = document.id
                    var chatChannel: ChatChannel


                    firebaseStoreInstance.collection("users").document(document.id)
                        .get().addOnSuccessListener {

                            person = it.toObject(User::class.java)!!

                            chatChannelCollectionRef.document(document["channelId"] as String)
                                .get().addOnSuccessListener {

                                    chatChannel = it.toObject(ChatChannel::class.java)!!
                                    chatChannelItems.add(
                                        ChatChannelItem(
                                            person,
                                            userId,
                                            chatChannel,
                                            context
                                        )
                                    )
                                    counter++
                                    if (counter >= documentSize!!)
                                        chatChannelItems.sortByDescending { it.chatChannel.activeTime }
                                    onListen(chatChannelItems)

                                }
                        }


                }


            }
    }


    fun removeListener(registration: ListenerRegistration) = registration.remove()


    fun getOrCreateChatChannel(
        otherUserId: String,
        onComplet: (channelId: String) -> Unit
    ) {
        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if (it.exists()) {
                    onComplet(it["channelId"] as String)
                    return@addOnSuccessListener
                }
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChatChannel = chatChannelCollectionRef.document()
                newChatChannel.set(
                    ChatChannel(
                        mutableListOf(currentUserId, otherUserId),
                        "",
                        Calendar.getInstance().time
                    )
                )

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

    private fun updateChatchannel(channelId: String, lastMessage: Message, date: Date) {
        val mutableMap = mutableMapOf<String, Any>()
        val message =
            if (lastMessage.dataType == "TEXT") (lastMessage as TextMessage).text else "An image message"
        mutableMap["lastMessage"] = message
        mutableMap["activeTime"] = date

        chatChannelCollectionRef.document(channelId).update(mutableMap)


    }

    fun deleteChatChannel(channelId: String, otherUserId: String) {


        currentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId)
            .delete()

        firebaseStoreInstance.collection("users").document(otherUserId)
            .collection("engagedChatChannels")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .delete()

        chatChannelCollectionRef.document(channelId).delete()

    }


    fun addChatMessagesListener(
        channelId: String,
        context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        return chatChannelCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FireStore", "ChatMessageListener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    if (it["dataType"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java)!!, context))
                    else
                        items.add(
                            ImageMessageItem(
                                it.toObject(ImageMessage::class.java)!!,
                                context
                            )
                        )


                }
                onListen(items)
            }
    }


    fun sendMessage(message: Message, otherUserId: String, chanelid: String) {
        chatChannelCollectionRef.document(chanelid)
            .collection("messages")
            .add(message).addOnSuccessListener {
                updateChatchannel(chanelid, message, Calendar.getInstance().time)
                currentUserDocRef.collection("engagedChatChannels")
                    .document(otherUserId)
                    .update(mapOf("time" to Calendar.getInstance().time))

                firebaseStoreInstance.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .update(mapOf("time" to Calendar.getInstance().time))
            }

    }


    //FCM
    fun getFCMRegistrationTokens(onComplet: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            onComplet(user.registrationtokens)
        }


    }

    fun setFCMRegistrationTokens(tokens: MutableList<String>) {
        currentUserDocRef.update(mapOf("registrationtokens" to tokens))
    }


    //endFCM


}
