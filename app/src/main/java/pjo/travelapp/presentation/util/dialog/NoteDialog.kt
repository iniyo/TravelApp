package pjo.travelapp.presentation.util.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import pjo.travelapp.databinding.DialogNoteBinding

class NoteDialog(private val context: Context) {

    private var dialog: AlertDialog? = null
    private val binding: DialogNoteBinding = DialogNoteBinding.inflate(LayoutInflater.from(context))

    fun show(onSave: (String) -> Unit) {
        dialog = AlertDialog.Builder(context)
            .setTitle("New Note")
            .setView(binding.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog?.show()

        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val note = binding.etNote.text.toString()
            if (note.isNotEmpty()) {
                onSave(note)
                dialog?.dismiss()
            } else {
                Toast.makeText(context, "Note is empty!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
            dialog?.dismiss()
        }
    }
}