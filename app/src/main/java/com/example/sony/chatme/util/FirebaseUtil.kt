package com.example.sony.chatme.util


import android.content.Context
import android.util.Log
import com.example.sony.chatme.model.User
import com.example.sony.chatme.recyclerview.item.PersonItem
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
}
