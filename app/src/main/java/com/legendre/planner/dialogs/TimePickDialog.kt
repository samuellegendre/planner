package com.legendre.planner.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickDialog(private val button: TextView, private val calendar: Calendar) :
    DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    private lateinit var listener: TimePickerListener

    interface TimePickerListener {
        fun onTimeSet(
            view: TimePicker?,
            hourOfDay: Int,
            minute: Int,
            button: TextView,
            calendar: Calendar
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = parentFragment as TimePickerListener
        } catch (e: ClassCastException) {
            throw ClassCastException()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            activity,
            this,
            hour,
            minute,
            DateFormat.is24HourFormat(activity)
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        listener.onTimeSet(view, hourOfDay, minute, button, calendar)
    }

    override fun onCancel(dialog: DialogInterface) {
        dialog.dismiss()
    }
}