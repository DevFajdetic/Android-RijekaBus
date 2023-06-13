package com.example.rijekabusapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rijekabusapp.databinding.ActivityAboutBinding


class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivSofa.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.riteh.uniri.hr/")
            )
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else { Toast.makeText(
                    applicationContext,
                    "No web browser app found",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
