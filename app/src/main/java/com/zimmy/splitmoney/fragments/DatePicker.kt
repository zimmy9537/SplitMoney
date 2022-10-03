package com.zimmy.splitmoney.fragments

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*


class DatePicker : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mCalendar: Calendar = Calendar.getInstance()
        val year: Int = mCalendar.get(Calendar.YEAR)
        val month: Int = mCalendar.get(Calendar.MONTH)
        val dayOfMonth: Int = mCalendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(
            requireActivity(),
            activity as OnDateSetListener?,
            year,
            month,
            dayOfMonth
        )
    }
}