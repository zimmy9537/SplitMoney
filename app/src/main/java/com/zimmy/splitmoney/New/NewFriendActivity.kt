package com.zimmy.splitmoney.New

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
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
                    baseContext,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    baseContext as Activity,
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
                Toast.makeText(baseContext, "Please Grant permission", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getContactList() {
        val uri = ContactsContract.Contacts.CONTENT_URI

        val sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "ASC"
        val cursor = contentResolver.query(uri, null, null, null, sort)
        if (cursor != null) {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val index = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (index >= 0) {
                        val id = cursor.getString(index)
                        val indexDisplayName =
                            cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

                        val indexImage = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                        var imageUri: String? = null
                        if (indexImage >= 0) {
                            imageUri = cursor.getString(indexImage)
                        }

                        if (indexDisplayName >= 0) {
                            val name = cursor.getString(indexDisplayName)

                            val uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"
                            val phoneCursor = contentResolver.query(
                                uriPhone, null, selection,
                                arrayOf(String(), (id)), null
                            )
                            if (phoneCursor != null) {
                                if (phoneCursor.moveToNext()) {
                                    val phoneIndex =
                                        phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (phoneIndex >= 0) {
                                        val number = phoneCursor.getString(phoneIndex)
                                        val user = ContactModel(imageUri, name, number)
                                        phoneList.add(user)
                                        Log.v(TAG, "image $imageUri\n name $name, phone$number")
                                    }
                                }
                                phoneCursor.close()
                            }
                        }
                    }
                }
                cursor.close()
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
                adapter = PhoneAdapter(phoneList, this@NewFriendActivity)
                phoneRecyclerView.adapter = adapter
                phoneRecyclerView.layoutManager = linearLayoutManager
            }
        }
    }
}