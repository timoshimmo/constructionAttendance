package app.com.android.mobiletrac.construction.ui.home.databinding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.databinding.ActivityScannerBinding
import app.com.android.mobiletrac.construction.ui.home.HomeActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.io.IOException
import java.util.*

class CheckInScannerActivity : AppCompatActivity() {

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var binding: ActivityScannerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScannerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
       // setContentView(R.layout.activity_scanner)

        if (ContextCompat.checkSelfPermission(
                this@CheckInScannerActivity, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }

        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this@CheckInScannerActivity, R.anim.scanner_animation)
        binding.barCodeLine.startAnimation(aniSlide)
    }


    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.cameraSurfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {

            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    if (ActivityCompat.checkSelfPermission(this@CheckInScannerActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        askForCameraPermission()
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        //return
                    }
                    else {
                        cameraSource.start(holder)
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    if (ActivityCompat.checkSelfPermission(this@CheckInScannerActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        askForCameraPermission()
                    }
                    else {
                        cameraSource.start(holder)
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcode = detections.detectedItems
                if (barcode.size() == 1) {

                    scannedValue = barcode.valueAt(0).rawValue

                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    runOnUiThread {
                        cameraSource.stop()
                        binding.barCodeLine.clearAnimation()

                        val msg: String
                        val status = intent.getIntExtra("check_status", -1)

                        msg = when (status) {
                            1 -> {
                                "Successfully Checked In"

                            }

                            0 -> {
                                "Successfully Checked Out"

                            }
                            else -> {
                                "Error in creating checking data"
                            }
                        }

                        val dialogBuilder = AlertDialog.Builder(this@CheckInScannerActivity)
                        dialogBuilder.setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("Ok") { _, _ ->
                                auth = Firebase.auth
                                val curUser = auth.currentUser
                                db = Firebase.firestore

                                val gson = Gson()
                                val jsonString = scannedValue
                                val logs = gson.fromJson(jsonString, LogJson::class.java)

                                //val millis = Date().time

                                val currentDateTime = Calendar.getInstance()


                                /*    val formatted =
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                            DateTimeFormatter
                                                .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                                                .withZone(ZoneOffset.UTC)
                                                .format(Instant.now())
                                        } else {
                                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())
                                        }
                                */

                                if (curUser != null) {

                                    val docRef = db.collection("users").document(curUser.uid)


                                    val data = hashMapOf(
                                            "userid" to curUser.uid,
                                            "serverName" to logs.server,
                                            "entrance" to logs.entrance,
                                            "logStatus" to status,
                                            "dateTime" to currentDateTime.timeInMillis
                                    )

                                    when (status) {
                                        1 -> {

                                            db.collection("logs").add(data)
                                                    .addOnSuccessListener {
                                                        docRef.update("onlineStatus", true)
                                                    }
                                        }
                                        0 -> {

                                            db.collection("logs").add(data)
                                                    .addOnSuccessListener {
                                                        docRef.update("onlineStatus", false)
                                                    }
                                        }

                                    }

                                }
                                val intent = Intent(this@CheckInScannerActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        val alert = dialogBuilder.create()
                        // set title for alert dialog box
                        alert.setTitle("Log Status")
                        // show alert dialog
                        alert.show()

                        // val alertDialog: AlertDialog? =
                        /*   Toast.makeText(
                            this@CheckInScannerActivity,
                                msg,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish() */

                    }
                } else {
                    Toast.makeText(
                        this@CheckInScannerActivity,
                        "Barcode error. Pls try again",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@CheckInScannerActivity,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }


}

