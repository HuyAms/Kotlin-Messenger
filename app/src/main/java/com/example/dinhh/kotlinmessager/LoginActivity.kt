package com.example.dinhh.kotlinmessager

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {

            }.addOnFailureListener {

            }
        }

        back_to_register_text_view.setOnClickListener {
            finish()
        }
    }
}
