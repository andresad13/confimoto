package com.motuber.motuber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.chaos.view.PinView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class VerifyCodeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var pinCode: PinView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        pinCode = findViewById(R.id.code)
        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.validCodeButt).setOnClickListener {
            verifyPhoneNumberWithCode(
                Global.verifiId, (pinCode.text).toString()
            )
        }

    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code!!)

        signInWithPhoneAuthCredential(credential)

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(null, "signInWithCredential:success")
                    Toast.makeText(this,
                        "login correcto", Toast.LENGTH_LONG).show()
                   startActivity(Intent(this, HomeActivity::class.java))


                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(null, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,
                        "codigo errado", Toast.LENGTH_LONG).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }


}