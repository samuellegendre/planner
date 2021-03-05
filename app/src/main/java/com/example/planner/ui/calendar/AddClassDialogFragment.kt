package com.example.planner.ui.calendar

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.planner.R

class AddClassDialogFragment : DialogFragment() {

    /*private lateinit var listener: AddClassDialogListener

    interface AddClassDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as AddClassDialogListener
            val listener: AddClassDialogListener = context
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + "must implement NoticeDialogListener"))
        }
    }*/

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_class, null)

            val dateButton: Button = view.findViewById(R.id.dateButton)
            val timeButton: Button = view.findViewById(R.id.timeButton)
            val spinner: Spinner = view.findViewById(R.id.classMethod)

            dateButton.setOnClickListener {
                DatePickerFragment().show(childFragmentManager, "datePicker")
            }

            timeButton.setOnClickListener {
                TimePickerFragment().show(childFragmentManager, "timePicker")
            }

            ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.teaching_methods,
                    android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            builder.setView(view)
                    .setTitle(R.string.addClass)
                    .setPositiveButton(R.string.add,
                            DialogInterface.OnClickListener { dialog, id ->
                                //listener.onDialogPositiveClick(this)
                            })
                    .setNegativeButton(R.string.cancel,
                            DialogInterface.OnClickListener { dialog, id ->
                                //listener.onDialogNegativeClick(this)
                            })
            builder.create()
        } ?: throw IllegalStateException()

    }
}