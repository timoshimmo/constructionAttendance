package app.com.android.mobiletrac.construction.ui.start

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.ui.home.HomeActivity
import app.com.android.mobiletrac.construction.ui.login.LoginActivity
import app.com.android.mobiletrac.construction.ui.register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class StartActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnOpenSignUp: Button = findViewById(R.id.btn_go_to_signup)
        val btnOpenLogin: TextView = findViewById(R.id.tv_go_to_login)

        auth = Firebase.auth
        val curUser = auth.currentUser

        if(curUser != null) {

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        btnOpenSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnOpenLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}