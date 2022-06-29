package com.zimmy.splitmoney.expense

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import com.zimmy.splitmoney.R
import com.zimmy.splitmoney.constants.Konstants
import com.zimmy.splitmoney.models.Friend

class PaidByActivity : AppCompatActivity() {

    private lateinit var friendDetailList: ArrayList<Friend>
    private lateinit var paidByPhone: String
    private lateinit var linearLayout: LinearLayout
    private lateinit var save: Button
    private lateinit var phoneMap: HashMap<String, String>
    private lateinit var resultMap: HashMap<String, Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paid_by)

        friendDetailList = this.intent.getSerializableExtra(Konstants.DATA) as ArrayList<Friend>
        resultMap =
            this.intent.getSerializableExtra(Konstants.RESULT_MAP) as HashMap<String, Boolean>
        if (resultMap == null) {
            resultMap = HashMap()
        }
        phoneMap = HashMap()
        if (resultMap == null) {
            for (ele in friendDetailList) {
                if (ele.phone == getSharedPreferences(
                        Konstants.PERSONAL,
                        Context.MODE_PRIVATE
                    ).getString(Konstants.PHONE, "9537830943").toString()
                ) {
                    ele.name = "You"
                }
                phoneMap[ele.name] = ele.phone!!
                resultMap[ele.phone!!] = ele.phone == getSharedPreferences(
                    Konstants.PERSONAL,
                    Context.MODE_PRIVATE
                ).getString(Konstants.PHONE, "9537830943").toString()
            }
        }

        linearLayout = findViewById(R.id.linear)
        save = findViewById(R.id.save)

        for (ele in friendDetailList) {

            val view = layoutInflater.inflate(R.layout.equal_expense_item, null, false)
            val check = view.findViewById<CheckBox>(R.id.checkbox)
            check.text = ele.name
            check.isChecked = ele.phone == getSharedPreferences(
                Konstants.PERSONAL,
                Context.MODE_PRIVATE
            ).getString(Konstants.PHONE, "9537830943").toString()

            check.setOnCheckedChangeListener { button, isChecked ->
                resultMap[phoneMap[check.text]!!] = isChecked
            }
            linearLayout.addView(view)
        }


        save.setOnClickListener {
            val intent = Intent()
            intent.putExtra(Konstants.RESULT, resultMap)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}