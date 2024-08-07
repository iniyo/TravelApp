package pjo.travelapp.presentation.util

import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAnimation : DefaultItemAnimator() {
    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.let {
            val fadeIn = AlphaAnimation(0.0f, 1.0f)
            fadeIn.duration = 300
            it.startAnimation(fadeIn)
        }
        return super.animateAdd(holder)
    }
}