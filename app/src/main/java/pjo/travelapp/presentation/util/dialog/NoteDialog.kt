package pjo.travelapp.presentation.util.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import pjo.travelapp.R
import pjo.travelapp.databinding.DialogNoteBinding

class NoteDialog(private val context: Context) {

    private var dialog: AlertDialog? = null
    private val binding: DialogNoteBinding = DialogNoteBinding.inflate(LayoutInflater.from(context))

    fun show(initialNote: String?, onSave: (String) -> Unit) {
        dialog = AlertDialog.Builder(context)
            .setTitle("메모장")
            .setView(binding.root)
            .setIcon(R.drawable.ic_note)
            .setPositiveButton("저장", null)
            .setNegativeButton("취소", null)
            .create()

        dialog?.show()

        // 초기 노트 설정
        binding.etNote.setText(initialNote)

        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val note = binding.etNote.text.toString()
            onSave(note)
            dialog?.dismiss()
        }

        dialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.setOnClickListener {
            dialog?.dismiss()
        }
    }
}
