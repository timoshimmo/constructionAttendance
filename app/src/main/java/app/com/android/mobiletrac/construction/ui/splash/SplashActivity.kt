package app.com.android.mobiletrac.construction.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.ui.home.HomeActivity
import app.com.android.mobiletrac.construction.ui.start.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = layoutInflater.inflate(R.layout.activity_splash, null, false)

        auth = Firebase.auth


        setContentView(view)

        val curUser = auth.currentUser

        view.postDelayed({
            if(curUser != null) {
                openHome()
            }
            else {
                openStart()
            }

        }, 2000)



    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun openStart() {
        startActivity(Intent(this, StartActivity::class.java))
        finish()
    }

}