package pjo.travelapp.presentation.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import pjo.travelapp.databinding.DialogMapsSearchDirectionBinding
import pjo.travelapp.presentation.util.extension.layoutInflater

class MapsSearchDirectionDialog(private val context: Context) {

    private lateinit var binding : DialogMapsSearchDirectionBinding
    private val dlg = Dialog(context)

    fun show(content: String) {
        binding = DialogMapsSearchDirectionBinding.inflate(context.layoutInflater)

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)// title bar delete
        dlg.setContentView(binding.root)
        dlg.setCancelable(false)


    }
}