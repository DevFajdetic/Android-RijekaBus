package com.example.rijekabusapp.fragments.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rijekabusapp.databinding.BottomsheetAddPhotoBinding
import com.example.rijekabusapp.helpers.customValidate
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.network.models.StationImage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AddPhotoBottomSheet(
    private val selectedStation: Station,
    private val insertCallback: ((RequestBody) -> Unit)?,
    private val updateImageSlider: ((StationImage) -> Unit)?,
    private val hideEmptyStateCallback: (() -> Unit)
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetAddPhotoBinding
    private var validationFields = ArrayList<TextInputEditText>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetAddPhotoBinding.inflate(inflater, container, false)

        setupValidationFields()

        binding.btnAdd.setOnClickListener {
            if (formValid()) {
                val stationImage = StationImage(
                    selectedStation.id, binding.etUrl.text.toString(),
                    binding.etTitle.text.toString(), null
                )

                val jsonObject = JSONObject()
                jsonObject.put("stationId", stationImage.stationId)
                jsonObject.put("imageUrl", stationImage.imageUrl)
                jsonObject.put("imageCaption", stationImage.imageCaption)

                val jsonObjectString = jsonObject.toString()
                val requestBody = jsonObjectString
                    .toRequestBody("application/json".toMediaTypeOrNull())

                insertCallback?.invoke(requestBody)
                updateImageSlider?.invoke(stationImage)
                hideEmptyStateCallback.invoke()

                clearForm()
            }
        }

        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }

        return binding.root
    }

    private fun formValid(): Boolean {
        validationFields.forEach {
            if (!it.customValidate(requireContext())) {
                return false
            }
        }
        return true
    }

    private fun clearForm() {
        validationFields.forEach {
            it.text!!.clear()
        }
    }

    private fun setupValidationFields() {
        validationFields.add(binding.etUrl)
        validationFields.add(binding.etTitle)
    }
}
