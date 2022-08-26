package app.com.android.mobiletrac.construction.ui.login

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.ui.home.HomeActivity
import app.com.android.mobiletrac.construction.ui.start.StartActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth
    private var loading: ProgressBar? = null
    private var btnLogin: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val inputEmail: EditText = findViewById(R.id.etLgEmail)
        val inputPassword: EditText = findViewById(R.id.lgPassword)
        btnLogin = findViewById(R.id.btnLogin)
        loading = findViewById(R.id.lgLoading)

        val btnBack: TextView = findViewById(R.id.btnLgBack)
        val btnForgotPassword: TextView = findViewById(R.id.btnGoToForgot)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
                .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            btnLogin!!.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                inputEmail.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                inputPassword.error = getString(loginState.passwordError)
            }
        })

     /*   loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading!!.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        //Put in inputPassword on change action
          setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                                inputEmail.text.toString(),
                                inputPassword.text.toString()
                        )
                }
                false
            }*/

        inputEmail.afterTextChanged {
            loginViewModel.loginDataChanged(
                    inputEmail.text.toString(),
                    inputPassword.text.toString()
            )
        }

        inputPassword.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                        inputEmail.text.toString(),
                        inputPassword.text.toString()
                )
            }

        }

        btnLogin!!.setOnClickListener {
            loading!!.visibility = View.VISIBLE
            btnLogin!!.isEnabled = false
            login(inputEmail.text.toString(), inputPassword.text.toString())
            // loginViewModel.login(inputEmail.text.toString(), inputPassword.text.toString())
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

  /*  private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
                applicationContext,
                "$welcome $displayName",
                Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }*/

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun login(email: String, password: String) {

        auth = Firebase.auth
        //val sharedPreferences = this.getSharedPreferences("USER_DATA", MODE_PRIVATE)

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information

                        val user = auth.currentUser
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                        if(user!!.isEmailVerified) {
                            //val editor: SharedPreferences.Editor = sharedPreferences.edit()
                            //editor.putString("USER_ID", user.uid)
                            // editor.apply()

                            loading!!.visibility = View.GONE

                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        else {
                            loading!!.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                            btnLogin?.isEnabled = true
                            /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                ViewCompat.setBackgroundTintList(btnLogin!!, ColorStateList.valueOf(resources.getColor(R.color.secondary, theme)))
                            }
                            else {
                                ViewCompat.setBackgroundTintList(btnLogin!!, ColorStateList.valueOf(resources.getColor(R.color.secondary)))
                            }*/
                            user.sendEmailVerification()

                            Snackbar.make(btnLogin!!, "Your account is not validated. Pls click the link in your email to validate account", Snackbar.LENGTH_LONG)
                                    .setAction("Close", null).show()

                            // Toast.makeText(baseContext, "Your account is not validated. Pls click the link in your email to validate account",
                            //     Toast.LENGTH_SHORT).show()
                        }


                    } else {
                        loading!!.visibility = View.GONE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                        btnLogin?.isEnabled = true
                      /*  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            ViewCompat.setBackgroundTintList(btnLogin!!, ColorStateList.valueOf(resources.getColor(R.color.secondary, theme)))
                        }
                        else {
                            ViewCompat.setBackgroundTintList(btnLogin!!, ColorStateList.valueOf(resources.getColor(R.color.secondary)))
                        }*/
                        // If sign in fails, display a message to the user.
                        Snackbar.make(btnLogin!!, "Invalid email/password. Pls try again", Snackbar.LENGTH_INDEFINITE)
                                .setAction("DSIMISS") {
                                }.show()
                        // Toast.makeText(baseContext, "Invalid email/password. Pls try again",
                        //    Toast.LENGTH_SHORT).show()
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