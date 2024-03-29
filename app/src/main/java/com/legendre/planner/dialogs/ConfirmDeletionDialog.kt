package com.legendre.planner.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.legendre.planner.R

class ConfirmDeletionDialog : DialogFragment() {
    private lateinit var listener: ConfirmDeletionDialogListener

    interface ConfirmDeletionDialogListener {
        fun confirmDeletionDialogPositiveClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = parentFragment as ConfirmDeletionDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.confirm_deletion)
                .setPositiveButton(
                    R.string.delete
                ) { _, _ ->
                    listener.confirmDeletionDialogPositiveClick(this)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { _, _ ->
                    this.dismiss()
                }
            builder.create()
        } ?: throw IllegalStateException()
    }
}