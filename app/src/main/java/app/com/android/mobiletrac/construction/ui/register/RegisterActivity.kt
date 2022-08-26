package app.com.android.mobiletrac.construction.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.ui.login.LoginActivity
import app.com.android.mobiletrac.construction.ui.start.StartActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
  //  private lateinit var database: DatabaseReference
    private lateinit var db: FirebaseFirestore
    private lateinit var registerViewModel: RegisterViewModel

    private var progressBar: ProgressBar? = null
    private var btnSignUp: Button? = null

    private var lytValidateInfo: LinearLayout? = null
    private var lytRegistrationBody: ConstraintLayout? = null
    private var TAG: String = "RegisterActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnBack: TextView = findViewById(R.id.btnSpBack)

        lytValidateInfo = findViewById(R.id.lytSignUpInfo)
        lytRegistrationBody = findViewById(R.id.registrationBody)

        if(lytRegistrationBody!!.visibility == View.GONE) {
            lytRegistrationBody!!.visibility = View.VISIBLE
        }

        val inputFullName: EditText = findViewById(R.id.etSpFullName)
        val inputEmail: EditText = findViewById(R.id.etSpEmail)
        val inputPhoneNo: EditText = findViewById(R.id.etSpMobileNo)
        val inputPassword: EditText = findViewById(R.id.etSpPassword)

        val tvGValidateToLogin: TextView = findViewById(R.id.tvValidateLogin)
        btnSignUp = findViewById(R.id.btnSignUp)
        progressBar = findViewById(R.id.sploading)

        registerViewModel = ViewModelProvider(this, RegisterViewModelFactory())
            .get(RegisterViewModel::class.java)

        btnSignUp?.setOnClickListener {
            progressBar!!.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            it?.isEnabled = false
          /*  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(resources.getColor(R.color.light_grey, theme)))
            }
            else {
                ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(resources.getColor(R.color.light_grey)))
            }*/
            if(inputEmail.text.toString() != "" && inputPassword.text.toString() != "" &&
                inputFullName.text.toString() != "" && inputPhoneNo.text.toString() != "") {
                signup(inputFullName.text.toString(), inputEmail.text.toString(), inputPhoneNo.text.toString(), inputPassword.text.toString())
            }
            else {
                Snackbar.make(it, "All fields are required. Pls try again", Snackbar.LENGTH_INDEFINITE)
                    .setAction("DISMISS") {
                    }.show()
                //Toast.makeText(this, "All fields are required. Pls try again", Toast.LENGTH_LONG).show()
            }
        }

        registerViewModel.registerFormState.observe(this@RegisterActivity, Observer {

            val registerState = it ?: return@Observer

            btnSignUp!!.isEnabled = registerState.isDataValid

            if (registerState.nameError != null) {
                inputFullName.error = getString(registerState.nameError)
            }

            if (registerState.emailError != null) {
                inputEmail.error = getString(registerState.emailError)
            }

            if (registerState.mobileError != null) {
                inputPhoneNo.error = getString(registerState.mobileError)
            }

            if (registerState.passwordError != null) {
                inputPassword.error = getString(registerState.passwordError)
            }

        })

        inputFullName.afterTextChanged {
            registerViewModel.registerDataChanged(
                    inputFullName.text.toString(),
                    inputEmail.text.toString(),
                    inputPhoneNo.text.toString(),
                    inputPassword.text.toString()
            )
        }

        inputEmail.afterTextChanged {
            registerViewModel.registerDataChanged(
                    inputFullName.text.toString(),
                    inputEmail.text.toString(),
                    inputPhoneNo.text.toString(),
                    inputPassword.text.toString()
            )
        }

        inputPhoneNo.afterTextChanged {
            registerViewModel.registerDataChanged(
                    inputFullName.text.toString(),
                    inputEmail.text.toString(),
                    inputPhoneNo.text.toString(),
                    inputPassword.text.toString()
            )
        }

        inputPassword.afterTextChanged {
            registerViewModel.registerDataChanged(
                    inputFullName.text.toString(),
                    inputEmail.text.toString(),
                    inputPhoneNo.text.toString(),
                    inputPassword.text.toString()
            )

        }

        tvGValidateToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun signup(name: String, email: String, mobileNo: String, password: String) {
        // [START send_email_verification_with_continue_url]
        auth = Firebase.auth
        db = Firebase.firestore
       // database = Firebase.database.getReference("users")
        // val sharedPreferences = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val curUser = auth.currentUser

                        if(curUser != null) {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            curUser.sendEmailVerification()

                          //  val user = Users(name, email, mobileNo, false)

                            val data = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "mobileNo" to mobileNo,
                                    "onlineStatus" to false
                            )

                         /*   database.setValue(user, DatabaseReference.CompletionListener {
                                error, ref ->

                                if (error != null) {
                                    Log.d(TAG, error.message)
                                     Toast.makeText(
                                         baseContext, error.message,
                                         Toast.LENGTH_SHORT
                                      ).show()
                                }
                            }) */

                            db.collection("users").document(curUser.uid)
                                    .set(data)
                                    .addOnSuccessListener {
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build()

                                        curUser.updateProfile(profileUpdates)
                                    }
                                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                        }


                          progressBar!!.visibility = View.GONE
                           window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                           btnSignUp?.isEnabled = true
                         /*  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                               ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.secondary, theme)))
                           }
                           else {
                               ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.secondary)))
                           } */
                        lytRegistrationBody!!.visibility = View.GONE
                        lytValidateInfo!!.visibility = View.VISIBLE

                    } else {
                        progressBar!!.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        btnSignUp?.isEnabled = true
                       /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.secondary, theme)))
                        }
                        else {
                            ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.secondary)))
                        }*/
                        Snackbar.make(btnSignUp!!, "Authentication Error: " + task.exception?.localizedMessage, Snackbar.LENGTH_LONG)
                                .setAction("Close", null).show()
                        // Toast.makeText(
                        //     baseContext, "Failed to create user account",
                        //     Toast.LENGTH_SHORT
                        //  ).show()
                    }
                }

    }


}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}