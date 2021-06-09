package com.motuber.motuber


import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import com.rilixtech.widget.countrycodepicker.CountryCodePicker
import java.util.concurrent.TimeUnit

public class Global : Application() {
    companion object {
        @JvmField
        var verifiId: String = "no"
    }
}
class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    lateinit  var callbackManager : CallbackManager
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks




    private lateinit var googleSignInClient: GoogleSignInClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()



        var buttEmailLogin = findViewById<Button>(R.id.login_email_access_butt)
        var buttPhoneLogin = findViewById<Button>(R.id.butt_reg_phone)
        var buttforgotLogin = findViewById<Button>(R.id.login_forgot_butt)

        var logn_to_reg = findViewById<Button>(R.id.login_reg)

        logn_to_reg.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))

        }




       callbackManager = CallbackManager.Factory.create()





        buttPhoneLogin.setOnClickListener {


            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+"+findViewById<CountryCodePicker>(R.id.ccp).selectedCountryCode + (findViewById<EditText>( R.id.numero_tel).text).toString())       // Phone number to verify
                .setTimeout(30L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }


        buttforgotLogin.setOnClickListener {


/*

            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.login_dialog, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Recordar contraseña")
            //show dialog
            val  mAlertDialog = mBuilder.show()
            //login button click of custom layout
            mDialogView.dialogLoginBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                val email = mDialogView.dialogEmailEt.text.toString()
                //set the input text in TextView
                //mainInfoTv.setText("Name:"+ name +"\nEmail: "+ email +"\nPassword: "+ password)

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "revisa tu bandeja de correo", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            //cancel button click of custom layout
            mDialogView.dialogCancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }


            /*
            auth.sendPasswordResetEmail(auth.currentUser?.email.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,
                            "revisa tu bandeja de correo", Toast.LENGTH_LONG).show()
                    }
                }*/

            */
        }


        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                //signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                Global.verifiId = verificationId
                toVerifyCode()

                // Save verification ID and resending token so we can use them later
                //storedVerificationId = verificationId
                //resendToken = token
            }
        }

        buttEmailLogin.setOnClickListener {
            login(findViewById<EditText>(R.id.login_username).text.toString(),findViewById<EditText>(
                R.id.login_pass).text.toString() )
        }



        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if(auth.currentUser != null){
           startActivity(Intent(this, HomeActivity::class.java))

            Toast.makeText(this, "logueado", Toast.LENGTH_LONG).show()
        }

        var buttonGoogle = findViewById<SignInButton>(R.id.sign_in_google_button)


        buttonGoogle?.setOnClickListener()
        {
            Toast.makeText(this,
                "mensaje", Toast.LENGTH_LONG).show()
            signIn()
        }

        callbackManager = CallbackManager.Factory.create()





/*
        buttonFacebookLogin.setReadPermissions("email", "public_profile")
        buttonFacebookLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
                to_Home()

            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })// ...

*/

    }

    fun toVerifyCode(){
        startActivity(Intent(this, VerifyCodeActivity::class.java))

    }


    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(auth.currentUser != null){
            startActivity(Intent(this, HomeActivity::class.java))

            Toast.makeText(this,
                "logueado", Toast.LENGTH_LONG).show()
        }
    }


    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
                startActivity(Intent(this, HomeActivity::class.java))

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(null, "Google sign in failed", e)
                // ...
            }
        }
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(null, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(null, "signInWithCredential:success")
                    Toast.makeText(this,
                        "logueado", Toast.LENGTH_LONG).show()
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this,
                        "no login", Toast.LENGTH_LONG).show()
                    Log.w(null, "signInWithCredential:failure", task.exception)
                }

                // ...
            }
    }


/*
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Authentication succesfull.",
                        Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

                // [START_EXCLUDE]
                // [END_EXCLUDE]
            }
    }
*/



    fun login(email: String, pass: String){
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(baseContext, "Authentication email success",
                        Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    startActivity(Intent(this, HomeActivity::class.java))

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

                // ...
            }
    }




    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }

    fun to_Home(){
        startActivity(Intent(this, HomeActivity::class.java))

    }




}