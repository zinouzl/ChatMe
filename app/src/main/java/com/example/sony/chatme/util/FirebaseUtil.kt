package com.example.sony.chatme.util


import com.example.sony.chatme.model.User
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

object FirebaseUtil {

    private val firebaseStoreInstance : FirebaseFirestore by lazy {FirebaseFirestore.getInstance()}

    private val currentUserDocRef : DocumentReference
        get()= firebaseStoreInstance.document("users/${FirebaseAuth.getInstance().uid
            ?: throw NullPointerException("UID is null")}")


    fun initCurrentUserIfFirstTiime(onComplet: ()->Unit) {
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
    fun updateCurrentUser(name:String,bio:String,picturePath:String?=null){
        val userFeildMap = mutableMapOf<String,Any>()
        if(name.isNotBlank()) userFeildMap["name"]=name
        if(bio.isNotBlank()) userFeildMap["bio"]=bio
        if(picturePath != null) userFeildMap["pic"]=picturePath
        currentUserDocRef.update(userFeildMap)
    }

    fun getCurrentUser(onComplet: (User) -> Unit){
        currentUserDocRef.get()
            .addOnSuccessListener {
                it.toObject(User::class.java)?.let { it1 -> onComplet(it1) }
        }
    }
}
