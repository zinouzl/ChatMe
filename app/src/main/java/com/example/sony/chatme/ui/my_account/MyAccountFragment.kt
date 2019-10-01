package com.example.sony.chatme.ui.my_account

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.sony.chatme.R
import com.example.sony.chatme.glide.GlideApp
import com.example.sony.chatme.ui.SignInActivity
import com.example.sony.chatme.util.FirebaseUtil
import com.example.sony.chatme.util.StorageUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_my_account.*
import kotlinx.android.synthetic.main.fragment_my_account.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import java.io.ByteArrayOutputStream


class MyAccountFragment : Fragment() {

    private lateinit var myAccountViewModel: MyAccountViewModel
    private val RC_SELECT_IMAGE = 2
    private lateinit var selectedImageBytes: ByteArray
    private var pictureJustChanged = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myAccountViewModel =
            ViewModelProviders.of(this).get(MyAccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_account, container, false)

        root.apply {
            image_picture.setOnClickListener {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))

                }
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    RC_SELECT_IMAGE
                )
            }
            button_save.setOnClickListener {
                if (::selectedImageBytes.isInitialized) {
                    StorageUtil.uploadUserPicture(selectedImageBytes) {
                        FirebaseUtil.updateCurrentUser(
                            edit_name.text.toString(),
                            edit_bio.text.toString(),
                            it
                        )
                    }

                } else {

                    FirebaseUtil.updateCurrentUser(
                        edit_name.text.toString(),
                        edit_bio.text.toString(),
                        null
                    )

                }
                Snackbar.make(root,"Saving Data to Cloud",Snackbar.LENGTH_SHORT).show()


            }
            button_sing_out.setOnClickListener {
                AuthUI.getInstance()
                    .signOut(this@MyAccountFragment.context!!)
                    .addOnCompleteListener {
                        startActivity(intentFor<SignInActivity>().newTask().clearTask())
                    }
            }
        }
        return root
    }

    override fun onStart() {
        super.onStart()
        FirebaseUtil.getCurrentUser {
            if (this@MyAccountFragment.isVisible) {
                edit_name.setText(it.userName)
                edit_bio.setText(it.bio)
                if (!pictureJustChanged && it.profilPicturPath != null) {
                    GlideApp.with(this)
                        .load(StorageUtil.pathToReference(it.profilPicturPath))
                        .placeholder(R.drawable.ic_account_circle_black_24dp)
                        .into(image_picture)
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SELECT_IMAGE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {

            val selectedImagePath = data.data
            val selectedImageBmp = MediaStore.Images.Media
                .getBitmap(activity?.contentResolver, selectedImagePath)

            val outputStream = ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            //TODO Load Image
            GlideApp.with(this)
                .load(selectedImageBytes)
                .into(image_picture)

            pictureJustChanged = true


        }
    }
}