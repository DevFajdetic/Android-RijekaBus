package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rijekabusapp.R

class ExploreFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //setupUserProfile()
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    /*
private fun setupUserProfile() {
    binding.userProfilePic.load(SavedPreference.getPictureUrl(this)) {
        crossfade(true)
        placeholder(R.drawable.ic_person)
        transformations(CircleCropTransformation())
    }

    binding.userProfileName.text = SavedPreference.getGivenName(this)
    binding.userProfileSurname.text = SavedPreference.getFamilyName(this)
}*/
}
