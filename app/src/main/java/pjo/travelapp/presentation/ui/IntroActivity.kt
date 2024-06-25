package pjo.travelapp.presentation.ui

import android.content.Intent
import android.os.Bundle
import pjo.travelapp.databinding.ActivityIntroBinding
import pjo.travelapp.presentation.BaseActivity

class IntroActivity : BaseActivity() {

    private lateinit var binding: ActivityIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIntro.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}