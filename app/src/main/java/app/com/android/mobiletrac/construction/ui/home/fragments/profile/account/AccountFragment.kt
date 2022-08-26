package app.com.android.mobiletrac.construction.ui.home.fragments.profile.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.ui.home.fragments.profile.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileViewModel: ProfileViewModel

    private var progressBar: ProgressBar? = null
    private var btnUpdateAccount: Button? = null

   /* override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    } */

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_account, container, false)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val inputFullName: EditText = root.findViewById(R.id.etAcctFullName)
        val inputMobileNo: EditText = root.findViewById(R.id.etAcctMobileNo)
        val btnBack: TextView = root.findViewById(R.id.btnAccountBack)

        btnUpdateAccount = root.findViewById(R.id.btnUpdateAccount)
        progressBar = root.findViewById(R.id.acctLoading)

        db = Firebase.firestore
        auth = Firebase.auth


        val curUser = auth.currentUser

        if(curUser != null) {

            db.collection("users").document(curUser.uid).get()
                .addOnSuccessListener { result ->
                    if(result.exists()) {
                        profileViewModel.setName(result.get("name").toString())
                        profileViewModel.setMobileNo(result.get("mobileNo").toString())
                    }
                }
                .addOnFailureListener {
                    Snackbar.make(btnUpdateAccount!!, "Error getting user profile data", Snackbar.LENGTH_INDEFINITE)
                        .setAction("DISMISS") {
                        }.show()
                }
        }

        profileViewModel.name.observe(viewLifecycleOwner, {

            inputFullName.setText(it)
        })

        profileViewModel.mobileNo.observe(viewLifecycleOwner, {
            inputMobileNo.setText(it)
        })

        btnUpdateAccount?.setOnClickListener {

            progressBar!!.visibility = View.VISIBLE
            it?.isEnabled = false


            if(inputFullName.text.toString() != "" && inputMobileNo.text.toString() != "") {

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(inputFullName.text.toString())
                    .build()

                curUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateAccount(
                            inputFullName.text.toString(),
                            inputMobileNo.text.toString()
                        )
                    }
                    else {
                        Snackbar.make(it, "Error updating profile. Pls try again", Snackbar.LENGTH_INDEFINITE)
                            .setAction("DISMISS") {
                            }.show()
                    }
                }
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

    private fun updateAccount(fullName: String, mobileNo: String) {

        val curUser = auth.currentUser

        // val profileUpdates =


        db.collection("users").document(curUser!!.uid)
            .update(
                mapOf(
                    "name" to fullName,
                    "mobileNo" to mobileNo
                )
            )
            .addOnSuccessListener {
                progressBar!!.visibility = View.GONE
                btnUpdateAccount?.isEnabled = true
                Snackbar.make(
                    btnUpdateAccount!!,
                    "Updated Profile Successfully ",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Close", null).show()
            }
            .addOnFailureListener {
                progressBar!!.visibility = View.GONE
                btnUpdateAccount?.isEnabled = true
                Snackbar.make(
                    btnUpdateAccount!!,
                    "Update Error: Could not update user credentials. Pls try again",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Close", null).show()
            }

    }

    private fun goBack() {
        findNavController().navigate(R.id.action_navigation_account_to_navigation_profile)
    }

}