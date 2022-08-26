package app.com.android.mobiletrac.construction.ui.home.fragments.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val tvProfileName: TextView = root.findViewById(R.id.tvProfileCurrentUserName)
        val tvProfileEmail: TextView = root.findViewById(R.id.tvProfileEmail)

        val lytBtnUserAccount: LinearLayout = root.findViewById(R.id.lytBtnUserAccount)
        val lytBtnChangePassword: LinearLayout = root.findViewById(R.id.lytBtnChangePassword)
        val btnSignOut: TextView = root.findViewById(R.id.btnSignOut)

        db = Firebase.firestore
        auth = Firebase.auth

        val curUser = auth.currentUser

        if(curUser != null) {

            val displayName = curUser.displayName
            val userEmail = curUser.email
           // tvProfileName.text = displayName
            profileViewModel.setName(displayName)
            profileViewModel.setEmail(userEmail)


        }

        profileViewModel.name.observe(viewLifecycleOwner, {
            tvProfileName.text = it
        })

        profileViewModel.email.observe(viewLifecycleOwner, {
            tvProfileEmail.text = it
        })

        lytBtnUserAccount.setOnClickListener {
            openUserAccount()
        }

        lytBtnChangePassword.setOnClickListener {
            openChangePassword()
        }

        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            activity?.startActivity(intent)
            activity?.finish()
        }

        return root
    }

    private fun openUserAccount() {
        findNavController().navigate(R.id.action_navigation_profile_to_navigation_account)
    }

    private fun openChangePassword() {
        findNavController().navigate(R.id.action_navigation_profile_to_navigation_change_password)
    }

}