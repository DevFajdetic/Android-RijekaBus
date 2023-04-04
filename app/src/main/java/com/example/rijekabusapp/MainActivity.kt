package com.example.rijekabusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.example.rijekabusapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setupUserProfile()

        setContentView(binding.root)
    }

    private fun setupUserProfile() {
        binding.userProfilePic.load(SavedPreference.getPictureUrl(this)) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        binding.userProfileName.text = SavedPreference.getGivenName(this)
        binding.userProfileSurname.text = SavedPreference.getFamilyName(this)
    }
}
