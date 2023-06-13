package com.matuyuhi.shiftapp.component

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*


class DatePickerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstantState: Bundle?): Dialog {

        //デフォルトのタイムゾーンおよびロケールを使用してカレンダを取得
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireActivity(), activity as OnDateSetListener?, year, month, day)
    }
}
