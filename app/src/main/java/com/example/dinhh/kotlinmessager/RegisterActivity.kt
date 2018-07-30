package com.example.dinhh.kotlinmessager

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Show login activity")

            //Lauch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            Log.d("RegisterActivity", "Select photo")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_SHORT).show()
            return
        }

        //Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        return@addOnCompleteListener
                    } else {
                        Log.d("RegisterActivity", "Successfully created useer with uid: ${it.result.user.uid}")

                        uploadImageToFirebaseStorage()
                    }
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "File location: $it")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    //do some logging here
                }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, username_edittext_register.text.toString(),profileImageUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Error save user to database: ${it.toString()}")
                }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            selected_photo_imageview_register.setImageBitmap(bitmap)

            selectphoto_button_register.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }
}

class User(val uid: String, val username: String, val profileImageUrl: String) {
    constructor() : this("", "", "")
}