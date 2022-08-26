package app.com.android.mobiletrac.construction.ui.home.fragments.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.adapters.HistoryAdapter
import app.com.android.mobiletrac.construction.ui.home.databinding.CheckInScannerActivity
import app.com.android.mobiletrac.construction.models.HistoryModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

   // private lateinit var database: DatabaseReference
   private lateinit var homeViewModel: HomeViewModel
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private lateinit var locationRequest: LocationRequest
    private val PERMISSION_REQUEST_CODE = 100
    private val REQUEST_CHECK_SETTINGS = 200
    private var topLayout: LinearLayout? = null
    private var TAG: String = "HomeFragment"
    private var recyclerViewLogs: RecyclerView? = null
    private var adapter: HistoryAdapter? = null
    private var tvNotFound: TextView? = null
    private var pbRecentLogs: ProgressBar? = null
    private var query: Query? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val tvOnlineStatus: TextView = root.findViewById(R.id.tvOnlineStatus)
        val btnCheckIn: Button = root.findViewById(R.id.btnCheckIn)
        val btnCheckOut: Button = root.findViewById(R.id.btnCheckOut)
        tvNotFound = root.findViewById(R.id.tvNotFound)
        pbRecentLogs = root.findViewById(R.id.pbRecentLogs)

        topLayout = root.findViewById(R.id.home_top_area)

        recyclerViewLogs = root.findViewById(R.id.rvLogs)
        recyclerViewLogs?.layoutManager = LinearLayoutManager(requireContext())

      /*  homeViewModel.status.observe(viewLifecycleOwner, Observer {
            tvOnlineStatus.text = it
        }) */

     //   database = Firebase.database.reference
        db = Firebase.firestore
        auth = Firebase.auth

        val curUser = auth.currentUser

        if(curUser != null) {

            val docRef = db.collection("users").document(curUser.uid)
            val logRef = FirebaseFirestore.getInstance()
            query = logRef.collection("logs").whereEqualTo("userid", curUser.uid)
                    .orderBy("dateTime", Query.Direction.DESCENDING).limit(5)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val locationList = locationResult?.locations
                    if (locationList!!.isNotEmpty()){
                        val latestLocation = locationList.last()
                        docRef.update("coordinates", latestLocation)
                        // database.child("users").child(curUser.uid).child("coordinates").setValue(latestLocation)
                        // Update UI with location data
                        // ...
                    }
                }
            }

            docRef.addSnapshotListener{snapshot, e ->
                if (e != null) {
                    Snackbar.make(topLayout!!, "Connection Error: " + e.message, Snackbar.LENGTH_LONG)
                            .setAction("Close", null).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                   // Log.d(TAG, "Current data: ${snapshot.data}")
                    val status = snapshot.data?.get("onlineStatus") as Boolean
                    if(status) {

                        tvOnlineStatus.text = activity?.resources?.getString(R.string.str_online)
                        checkIfFragmentAttached {
                            tvOnlineStatus.setTextColor(
                                    ContextCompat.getColor(
                                            requireContext(),
                                            R.color.green_online
                                    )
                            )
                        }

                        tvOnlineStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.record_green, 0, 0, 0)

                    }

                    else {
                        tvOnlineStatus.text = activity?.resources?.getString(R.string.str_offline)
                        checkIfFragmentAttached {
                            tvOnlineStatus.setTextColor(
                                    ContextCompat.getColor(
                                            requireContext(),
                                            android.R.color.holo_red_light
                                    )
                            )
                        }

                        tvOnlineStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.record_red, 0, 0, 0)
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }


            val options = FirestoreRecyclerOptions.Builder<HistoryModel>()
                .setQuery(query!!, HistoryModel::class.java)
                .setLifecycleOwner(this)
                .build()

            val divider = DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.recent_logs_divider)!!)
            recyclerViewLogs?.addItemDecoration(divider)
            adapter = HistoryAdapter(options)
            recyclerViewLogs?.adapter = adapter

            query?.get()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    if(it.result.size() > 0) {
                        tvNotFound?.visibility = View.GONE
                        pbRecentLogs?.visibility = View.GONE
                    //    recyclerViewLogs?.visibility = View.VISIBLE
                   //     System.out.println("DATA SIZE STATUS: " + it.result.size().toString())
                    } else {
                        tvNotFound?.visibility = View.VISIBLE
                        pbRecentLogs?.visibility = View.GONE
                    //    recyclerViewLogs?.visibility = View.GONE
                    //    System.out.println("DATA SIZE STATUS: EMPTY")
                    }
                }
            }

        }

        btnCheckIn.setOnClickListener {
            createLocationRequest()

            val intent = Intent(activity, CheckInScannerActivity::class.java)
            intent.putExtra("check_status", 1)
            activity?.startActivity(intent)
            activity?.finish()
        }

        btnCheckOut.setOnClickListener {
            fusedLocationClient.removeLocationUpdates(locationCallback)

            val intent = Intent(activity, CheckInScannerActivity::class.java)
            intent.putExtra("check_status", 0)
            activity?.startActivity(intent)
            activity?.finish()

        }



        return root
    }

    private fun getCurrentLocation() {

        if(ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
        else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if(location != null) {
                    currentLocation = location

                }
            }
        }

    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getCurrentLocation()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(
                    requireContext(),
                    "Permission required to access current location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createLocationRequest() {

        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }!!

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        }
        else {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper())
        }

    }

    fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    override fun onPause() {
        super.onPause()
        //fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        //fusedLocationClient.asGoogleApiClient().isConnected
       // startLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}

