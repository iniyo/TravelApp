package pjo.travelapp.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 현재 액티비티의 윈도우 객체 참조 - 액티비티의 UI를 나타냄.
        window.apply {
            // FLAG_LAYOUT_NO_LIMITS - 창의 레이아웃의 화면의 경계를 무시할 수 있도록.
            setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }
}