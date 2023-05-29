package com.zimmy.splitmoney.expense

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Friend
import kotlin.math.log

class PaidByActivity : AppCompatActivity() {

    private lateinit var friendDetailList: ArrayList<Friend>
    private lateinit var paidByPhone: String
    private lateinit var linearLayout: LinearLayout
    private lateinit var save: Button
    private lateinit var resultMap: HashMap<String, Boolean>
    private lateinit var checkMap: HashMap<CheckBox, String>
    private lateinit var myPhone: String
    private val TAG = PaidByActivity::class.java.simpleName
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paid_by)

        myPhone = getSharedPreferences(
            Konstants.PERSONAL,
            Context.MODE_PRIVATE
        ).getString(Konstants.PHONE, "9537830943").toString()

        friendDetailList = this.intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>
        resultMap =
            this.intent.getSerializableExtra(Konstants.RESULT_MAP) as HashMap<String, Boolean>

        linearLayout = findViewById(R.id.linearPercent)
        save = findViewById(R.id.save)
        checkMap = HashMap()

        for (ele in friendDetailList) {

            val view = layoutInflater.inflate(R.layout.equal_expense_item, null, false)
            val check = view.findViewById<CheckBox>(R.id.checkbox)
            if (ele.phone == myPhone) {
                check.text = "Me"
            } else {
                check.text = ele.name
            }
            check.isChecked = resultMap[ele.phone!!]!!
            checkMap[check] = ele.phone!!
            if (check.isChecked) {
                counter++
            }

            check.setOnCheckedChangeListener { _, isChecked ->
                resultMap[checkMap[check!!]!!] = isChecked
                if (!isChecked) {
                    counter--
                    if (counter == 0) {
                        check.isChecked = true
                        resultMap[checkMap[check]!!] = true
                        Toast.makeText(
                            this,
                            "Atleast someone must pay the bill",
                            Toast.LENGTH_SHORT
                        ).show()
                        counter = 1
                    }
                } else {
                    counter++
                }
            }
            linearLayout.addView(view)
        }


        save.setOnClickListener {
            val intent = Intent()
            intent.putExtra(Konstants.RESULT, resultMap)
            for (ele in resultMap) {
                Log.v(TAG, "${ele.key} pays ${ele.value}")
            }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}