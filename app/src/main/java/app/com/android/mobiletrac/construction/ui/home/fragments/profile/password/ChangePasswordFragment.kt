package app.com.android.mobiletrac.construction.ui.home.fragments.profile.password

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.com.android.mobiletrac.construction.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ChangePasswordFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var passwordViewModel: PasswordViewModel

    private var progressBar: ProgressBar? = null
    private var btnSave: Button? = null

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_change_password, container, false)

        val currentPassword: EditText = root.findViewById(R.id.etCurrentPassword)
        val newPassword: EditText = root.findViewById(R.id.etNewPassword)
        val confirmPassword: EditText = root.findViewById(R.id.etConfirmPassword)
        val btnBack: TextView = root.findViewById(R.id.btnChangePasswordBack)

        progressBar = root.findViewById(R.id.cpLoading)
        btnSave = root.findViewById(R.id.btnChangePassword)

        passwordViewModel = ViewModelProvider(this, PasswordViewModelFactory())
            .get(PasswordViewModel::class.java)

        passwordViewModel.passwordFormState.observe(requireActivity(), Observer {

            val passwordState = it ?: return@Observer

            btnSave?.isEnabled = passwordState.isDataValid

            if (passwordState.currentPasswordError != null) {
                currentPassword.error = getString(passwordState.currentPasswordError)
            }

            if (passwordState.newPasswordError != null) {
                newPassword.error = getString(passwordState.newPasswordError)
            }

            if (passwordState.confirmPasswordError != null) {
                confirmPassword.error = getString(passwordState.confirmPasswordError)
            }

        })

        currentPassword.afterTextChanged {
            passwordViewModel.registerDataChanged(currentPassword.text.toString(),
                newPassword.text.toString(), confirmPassword.text.toString())
        }

        newPassword.afterTextChanged {
            passwordViewModel.registerDataChanged(currentPassword.text.toString(),
                newPassword.text.toString(), confirmPassword.text.toString())
        }

        confirmPassword.afterTextChanged {
            passwordViewModel.registerDataChanged(currentPassword.text.toString(),
                newPassword.text.toString(), confirmPassword.text.toString())
        }

        btnSave?.setOnClickListener {
            progressBar!!.visibility = View.VISIBLE
            it?.isEnabled = false

            if(currentPassword.text.toString() != "" && newPassword.text.toString() != "" &&
                confirmPassword.text.toString() != "") {
                updatePassword(newPassword.text.toString())
            }
            else {
                Snackbar.make(it, "All fields are required. Pls try again", Snackbar.LENGTH_INDEFINITE)
                    .setAction("DISMISS") {
                    }.show()
                //Toast.makeText(this, "All fields are required. Pls try again", Toast.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener {
            goBack()
        }

        return root
    }

    private fun updatePassword(newPassword: String) {

        auth = Firebase.auth
        db = Firebase.firestore

        val currentUser = auth.currentUser

        currentUser?.updatePassword(newPassword)?.addOnSuccessListener {
            progressBar!!.visibility = View.GONE
            btnSave?.isEnabled = true
            Snackbar.make(btnSave!!, "Updated Password Successfully", Snackbar.LENGTH_LONG)
                .setAction("Close", null).show()
        }?.addOnFailureListener {
            progressBar!!.visibility = View.GONE
            btnSave?.isEnabled = true
            Snackbar.make(btnSave!!, it.localizedMessage!!.toString(), Snackbar.LENGTH_LONG)
                .setAction("Close", null).show()
        }
    }

    private fun goBack() {
        findNavController().navigate(R.id.action_navigation_change_password_to_navigation_profile)
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