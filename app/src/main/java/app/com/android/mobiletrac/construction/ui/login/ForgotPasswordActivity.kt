package app.com.android.mobiletrac.construction.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import app.com.android.mobiletrac.construction.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var loading: ProgressBar? = null
    private var btnSendEmail: Button? = null

    private var lytResetInfo: LinearLayout? = null
    private var lytFormBody: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etForgotEmail: EditText = findViewById(R.id.etForgotEmail)
        val btnGoBack: TextView = findViewById(R.id.btnForgotBack)
        val btnBackToLogin: TextView = findViewById(R.id.tvForgotBackLogin)
        loading = findViewById(R.id.resetLoading)
        btnSendEmail = findViewById(R.id.btnConfirmEmail)

        lytResetInfo = findViewById(R.id.lytForgotPasswordInfo)
        lytFormBody = findViewById(R.id.fpFormLayout)

        btnSendEmail!!.setOnClickListener {
            loading!!.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            it?.isEnabled = false

            if(etForgotEmail.text.toString() != "") {
                confirmEmail(etForgotEmail.text.toString())
            }
            else {
                Snackbar.make(it, "All fields are required. Pls try again", Snackbar.LENGTH_INDEFINITE)
                    .setAction("DISMISS") {
                    }.show()
            }
        }

        btnBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnGoBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun confirmEmail(email: String) {

        auth = Firebase.auth
        db = Firebase.firestore

        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener {

                if(it.size() > 0) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener {
                            if(it.isSuccessful) {
                                loading!!.visibility = View.GONE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                btnSendEmail?.isEnabled = true

                                lytFormBody!!.visibility = View.GONE
                                lytResetInfo!!.visibility = View.VISIBLE
                            }
                            else {
                                Snackbar.make(btnSendEmail!!, "Reset Password Error: " + it.exception?.localizedMessage, Snackbar.LENGTH_LONG)
                                    .setAction("Close", null).show()
                            }
                        }
                }
                else {
                    Snackbar.make(btnSendEmail!!, "Could not find user with that email. ", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show()
                }


            }
            .addOnFailureListener {
                loading!!.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                btnSendEmail?.isEnabled = true
                Snackbar.make(btnSendEmail!!, "Confirm Email Error: " + it.localizedMessage, Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
            }
    }
}