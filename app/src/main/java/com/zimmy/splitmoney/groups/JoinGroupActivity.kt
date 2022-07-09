package com.zimmy.splitmoney.groups

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.budiyev.android.codescanner.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Friend
import com.zimmy.splitmoney.models.Group


class   JoinGroupActivity : AppCompatActivity() {

    lateinit var gCodeEt: EditText
    lateinit var joinButton: Button
    lateinit var codeScannerView: CodeScannerView
    lateinit var codeScanner: CodeScanner


    lateinit var mAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var groupReference: DatabaseReference
    lateinit var userReference: DatabaseReference

    lateinit var personalPreference: SharedPreferences

    lateinit var gcode: String
    lateinit var phone: String
    lateinit var name: String
    var isFemale: Boolean = true
    private var isPermissionGranted = false
    private val RequestCameraPermissionId = 50
    val TAG = JoinGroupActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_group)

        //TODO check for the rejoining of same group
        //TODO code scanner not working
        gCodeEt = findViewById(R.id.gCodeEt)
        joinButton = findViewById(R.id.gCodeJoin)
        codeScannerView = findViewById(R.id.scanner_view)

        codeScanner = CodeScanner(this@JoinGroupActivity, codeScannerView)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@JoinGroupActivity,
                arrayOf(Manifest.permission.CAMERA),
                RequestCameraPermissionId
            )
            return
        }

        try {
            isPermissionGranted = true
            startScanner()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        joinButton.setOnClickListener {
            gcode = gCodeEt.text.toString()
            joinGroup(gCodeEt.text.toString())
        }

        mAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        groupReference = firebaseDatabase.reference.child(Konstants.GROUPS)
        userReference = firebaseDatabase.reference.child(Konstants.USERS)

        personalPreference = getSharedPreferences(Konstants.PERSONAL, Context.MODE_PRIVATE)
        phone = personalPreference.getString(Konstants.PHONE, "9537830943").toString()
        name = personalPreference.getString(Konstants.NAME, "Zimmy").toString()
        isFemale = personalPreference.getBoolean(Konstants.FEMALE, true)
        Toast.makeText(this, "phone $phone", Toast.LENGTH_SHORT).show()

        startScanner()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RequestCameraPermissionId -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                try {
                    isPermissionGranted = true
                    startScanner()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun joinGroup(gcode: String) {
        val friend = Friend(name, isFemale,null,null)



        groupReference.child(gcode).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    ifGCodeExists(gcode, friend)
                } else {
                    Toast.makeText(
                        this@JoinGroupActivity,
                        "No such group Code exist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.v(TAG, "database error " + error.message)
            }
        })
    }

    private fun ifGCodeExists(gcode: String, friend: Friend) {
        Toast.makeText(this, "Welcome to the group", Toast.LENGTH_SHORT).show()
        groupReference.child(gcode).child(Konstants.MEMBERS).child(phone).setValue(friend)
        groupReference.child(gcode).child(Konstants.GROUPINFO).child(Konstants.TOTALMEMBERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var members: Int? = snapshot.getValue(Int::class.java)
                    members = members?.plus(1)
                    groupReference.child(gcode).child(Konstants.GROUPINFO)
                        .child(Konstants.TOTALMEMBERS).setValue(members)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error " + error.message)
                }
            })

        groupReference.child(gcode).child(Konstants.EXPENSE_GLOBAL).child(phone)
            .setValue(0.00)

        groupReference.child(gcode).child(Konstants.GROUPINFO).child(Konstants.GROUPNAME)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groupName: String? = snapshot.getValue(String::class.java)
                    val group= groupName?.let { Group("null",gcode, it,"you owe no one",0.00) }
                    userReference.child(phone).child(Konstants.GROUPS).child(gcode).setValue(group)
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v(TAG, "database error " + error.message)
                }

            })
    }

    fun startScanner() {
        codeScanner.startPreview()
        codeScanner.setCamera(CodeScanner.CAMERA_BACK) // or CAMERA_FRONT or specific camera id
        codeScanner.setFormats(CodeScanner.ALL_FORMATS) // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.setAutoFocusMode(AutoFocusMode.SAFE) // or CONTINUOUS
        codeScanner.setScanMode(ScanMode.SINGLE) // or CONTINUOUS or PREVIEW
        codeScanner.setAutoFocusEnabled(true) // Whether to enable auto focus or not
        codeScanner.setFlashEnabled(false) // Whether to enable flash or not
        codeScanner.decodeCallback = DecodeCallback { result ->
            val vibrator = applicationContext.getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(500)
            gcode = result.text
            Log.v(TAG, "gcode $gcode")
            joinGroup(gcode)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPermissionGranted)
            codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}