package com.zimmy.splitmoney.new

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.adapters.PhoneAdapter
import com.zimmy.splitmoney.models.ContactModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class NewFriendActivity : AppCompatActivity() {

    val PERMISSION_REQUEST_CODE = 999
    var PERMITTED = false
    lateinit var phoneList: ArrayList<ContactModel>
    val TAG = NewFriendActivity::class.java.simpleName
    lateinit var phoneRecyclerView: RecyclerView
    lateinit var phoneSView: SearchView
    lateinit var progress: ProgressBar
    lateinit var adapter: PhoneAdapter
    lateinit var back: Button
    var group:Boolean=false
    var groupName="TestGroup"
    var groupCode="ABCD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_friend)
        phoneRecyclerView = findViewById(R.id.phoneRecyclerview)
        phoneSView = findViewById(R.id.phoneSv)
        progress = findViewById(R.id.progress)
        progress.visibility = View.VISIBLE
        phoneList = ArrayList()
        requestPermissions()
        back = findViewById(R.id.back)
        back.setOnClickListener {
            finish()
        }

        group=intent.getBooleanExtra("group",false)
        if(group){
            groupName=intent.getStringExtra("name").toString()
            groupCode=intent.getStringExtra("code").toString()
        }

        phoneSView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.getFilter().filter(newText)
                return false
            }
        })

    }

    private fun requestPermissions() {
        CoroutineScope(Dispatchers.IO).launch {
            if (ContextCompat.checkSelfPermission(
                    this@NewFriendActivity,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@NewFriendActivity,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSION_REQUEST_CODE
                )
                val permission: String = Manifest.permission.READ_CONTACTS
                val granted = applicationContext.checkCallingOrSelfPermission(permission)
                PERMITTED = granted == PackageManager.PERMISSION_GRANTED
            } else {
                PERMITTED = true
                getContactListPro()
            }
            if (!PERMITTED) {
                Looper.prepare()
                Toast.makeText(this@NewFriendActivity, "Please Grant permission", Toast.LENGTH_SHORT).show()
                finish()
                Looper.loop()
            }
        }
    }

    private fun getContactListPro() {
        val cr = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val index = cur.getColumnIndex(ContactsContract.Contacts._ID)
                val id: String = cur.getString(
                    index
                )
                val picsIndex = cur.getColumnIndex(
                    ContactsContract.Contacts.PHOTO_URI
                )
                val picsUri = cur.getString(picsIndex)
                val nameIndex = cur.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME
                )
                val name: String = cur.getString(
                    nameIndex
                )
                val numberIndex = cur.getColumnIndex(
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
                )
                if (cur.getInt(
                        numberIndex
                    ) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            val numberIndex = pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                            val phoneNo: String = pCur.getString(
                                numberIndex
                            )
                            Log.v(TAG, "Image $picsUri")
                            Log.v(TAG, "Name: $name Phone Number: $phoneNo")
                            phoneList.add(ContactModel(picsUri, name, phoneNo))
                        }
                    }
                    pCur?.close()
                }
            }
        }
        cur?.close()
        runOnUiThread {
            progress.visibility = View.GONE
            if (!phoneList.isEmpty()) {
                val linearLayoutManager = LinearLayoutManager(this@NewFriendActivity)
                adapter = PhoneAdapter(phoneList, this@NewFriendActivity,group,groupName,groupCode)
                phoneRecyclerView.adapter = adapter
                phoneRecyclerView.layoutManager = linearLayoutManager
            }
        }
    }
}