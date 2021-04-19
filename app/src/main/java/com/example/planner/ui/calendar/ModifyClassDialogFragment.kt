package com.example.planner.ui.calendar

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.planner.R
import com.example.planner.ui.dialogs.DatePickerFragment
import com.example.planner.ui.dialogs.TimePickerFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*

class ModifyClassDialogFragment(private val event: Event) : DialogFragment(),
    DatePickerFragment.DatePickerListener,
    TimePickerFragment.TimePickerListener {

    private lateinit var listener: ModifyClassDialogListener
    private lateinit var dialog: AlertDialog
    private lateinit var startDateButton: Button
    private lateinit var startTimeButton: Button
    private lateinit var endDateButton: Button
    private lateinit var endTimeButton: Button
    private var startCalendar = event.startTime
    private var endCalendar = event.endTime
    private var dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var validated = true
    lateinit var defaultButtonColors: ColorStateList

    interface ModifyClassDialogListener {
        fun onModifyClassDialogPositiveClick(dialog: DialogFragment, event: Event)
        fun onModifyClassDialogNegativeClick(dialog: DialogFragment, event: Event)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as ModifyClassDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(("$context must implement NoticeDialogListener"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_class, null)
            val builder = AlertDialog.Builder(it)

            val className: EditText = view.findViewById(R.id.className)
            val allDaySwitch: SwitchMaterial = view.findViewById(R.id.allDaySwitch)
            startDateButton = view.findViewById(R.id.startDateButton)
            startTimeButton = view.findViewById(R.id.startTimeButton)
            endDateButton = view.findViewById(R.id.endDateButton)
            endTimeButton = view.findViewById(R.id.endTimeButton)
            val classLocation: EditText = view.findViewById(R.id.classLocation)
            val spinner: Spinner = view.findViewById(R.id.classMethod)

            className.setText(event.title)

            allDaySwitch.isChecked = event.isAllDay
            disableButtons(allDaySwitch.isChecked)
            allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
                disableButtons(isChecked)
            }

            startDateButton.text = dateFormat.format(startCalendar.time)
            startTimeButton.text = timeFormat.format(startCalendar.time)
            endDateButton.text = dateFormat.format(endCalendar.time)
            endTimeButton.text = timeFormat.format(endCalendar.time)

            classLocation.setText(event.subtitle)

            spinner.adapter = TeachingMethodArrayAdapter(requireContext(), TeachingMethods.list!!)
            spinner.setSelection(event.spinnerIndex)

            startDateButton.setOnClickListener {
                val datePickerFragment = DatePickerFragment(startDateButton, startCalendar)
                datePickerFragment.show(childFragmentManager, "startDatePicker")
            }

            startTimeButton.setOnClickListener {
                val timePickerFragment = TimePickerFragment(startTimeButton, startCalendar)
                timePickerFragment.show(childFragmentManager, "startTimePicker")
            }

            endDateButton.setOnClickListener {
                val datePickerFragment = DatePickerFragment(endDateButton, endCalendar)
                datePickerFragment.show(childFragmentManager, "endDatePicker")
            }

            endTimeButton.setOnClickListener {
                val timePickerFragment = TimePickerFragment(endTimeButton, endCalendar)
                timePickerFragment.show(childFragmentManager, "endTimePicker")
            }

            builder.setView(view)
                .setTitle(R.string.addClass)
                .setPositiveButton(
                    R.string.save
                ) { _, _ ->
                    if (allDaySwitch.isChecked && startCalendar.time == endCalendar.time) {
                        endCalendar.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE) + 1)
                    }
                    listener.onModifyClassDialogPositiveClick(
                        this, Event(
                            0,
                            if (className.text.toString()
                                    .isBlank()
                            ) "Sans titre" else className.text.toString(),
                            classLocation.text.toString(),
                            startCalendar,
                            endCalendar,
                            (spinner.selectedItem as TeachingMethod).color,
                            spinner.selectedItemPosition,
                            allDaySwitch.isChecked
                        )
                    )
                }
                .setNeutralButton(R.string.cancel) { _, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton(
                    R.string.delete
                ) { _, _ ->
                    listener.onModifyClassDialogNegativeClick(this, event)
                }
            dialog = builder.create()
            dialog
        } ?: throw IllegalStateException()
    }

    override fun onDateSet(
        view: DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int,
        button: Button,
        calendar: Calendar
    ) {
        calendar.set(year, month, dayOfMonth)
        button.text = dateFormat.format(calendar.time)

        if (button == startDateButton && endCalendar < startCalendar) {
            endCalendar.set(year, month, dayOfMonth)
            endDateButton.text = dateFormat.format(calendar.time)
        }

        calendarValidation()
    }

    override fun onTimeSet(
        view: TimePicker?,
        hourOfDay: Int,
        minute: Int,
        button: Button,
        calendar: Calendar
    ) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        button.text = timeFormat.format(calendar.time)

        calendarValidation()
    }

    private fun calendarValidation() {
        validated = if (endCalendar < startCalendar) {
            defaultButtonColors = startDateButton.textColors
            startDateButton.setTextColor(Color.RED)
            startTimeButton.setTextColor(Color.RED)
            false
        } else {
            startDateButton.setTextColor(defaultButtonColors)
            startTimeButton.setTextColor(defaultButtonColors)
            true
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = validated
    }

    private fun disableButtons(isChecked: Boolean) {
        if (isChecked) {
            startTimeButton.isEnabled = false
            endTimeButton.isEnabled = false
        } else {
            startTimeButton.isEnabled = true
            endTimeButton.isEnabled = true
        }
    }
}