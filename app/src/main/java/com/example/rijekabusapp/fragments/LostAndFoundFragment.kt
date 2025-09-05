package com.example.rijekabusapp.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.FragmentLostAndFoundBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class LostAndFoundFragment : Fragment() {
    private var _binding: FragmentLostAndFoundBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLostAndFoundBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupClickListeners()
        setupFAQSection()
        setupOfficeHours()
    }
    
    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupClickListeners() {
        // Phone number click
        binding.layoutPhone.setOnClickListener {
            val phoneNumber = getString(R.string.autotrolej_phone)
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No phone app found", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Email click
        binding.layoutEmail.setOnClickListener {
            val email = getString(R.string.autotrolej_email)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.no_mail_app, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Website click
        binding.layoutWebsite.setOnClickListener {
            val url = "https://www.autotrolej.hr"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No browser app found", Toast.LENGTH_SHORT).show()
            }
        }
        
        // View on map button click
        binding.buttonViewOnMap.setOnClickListener {
            val address = getString(R.string.autotrolej_address)
            val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                // If Google Maps is not installed, open in browser
                val mapUrl = "https://maps.google.com/?q=$address"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                try {
                    startActivity(browserIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "No map application found", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        // Report lost item button click
        binding.buttonReportLost.setOnClickListener {
            // Show a form or dialog to report a lost item
            Snackbar.make(binding.root, "Report form will be available soon", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun setupFAQSection() {
        // Setup FAQ recycler view
        val faqItems = listOf(
            FAQItem(
                getString(R.string.faq_question_1),
                getString(R.string.faq_answer_1)
            ),
            FAQItem(
                getString(R.string.faq_question_2),
                getString(R.string.faq_answer_2)
            ),
            FAQItem(
                getString(R.string.faq_question_3),
                getString(R.string.faq_answer_3)
            ),
            FAQItem(
                getString(R.string.faq_question_4),
                getString(R.string.faq_answer_4)
            ),
            FAQItem(
                getString(R.string.faq_question_5),
                getString(R.string.faq_answer_5)
            )
        )
        binding.recyclerFaq.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerFaq.adapter = FAQAdapter(faqItems)
    }
    
    private fun setupOfficeHours() {
        // Get current day of week to highlight
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Set current day text style to bold
        when (dayOfWeek) {
            Calendar.MONDAY -> binding.textMonday.setTextColor(resources.getColor(R.color.color_primary, null))
            Calendar.TUESDAY -> binding.textTuesday.setTextColor(resources.getColor(R.color.color_primary, null))
            Calendar.WEDNESDAY -> binding.textWednesday.setTextColor(resources.getColor(R.color.color_primary, null))
            Calendar.THURSDAY -> binding.textThursday.setTextColor(resources.getColor(R.color.color_primary, null))
            Calendar.FRIDAY -> binding.textFriday.setTextColor(resources.getColor(R.color.color_primary, null))
            Calendar.SATURDAY -> binding.textSaturday.setTextColor(resources.getColor(R.color.color_primary, null))
            Calendar.SUNDAY -> binding.textSunday.setTextColor(resources.getColor(R.color.color_primary, null))
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Inner classes for FAQ adapter
    data class FAQItem(val question: String, val answer: String)
    
    inner class FAQAdapter(private val items: List<FAQItem>) : 
            androidx.recyclerview.widget.RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {
        
        private val expandedItems = mutableSetOf<Int>()
        
        inner class FAQViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val questionText = view.findViewById<android.widget.TextView>(R.id.text_faq_question)
            val answerText = view.findViewById<android.widget.TextView>(R.id.text_faq_answer)
            val expandIcon = view.findViewById<android.widget.ImageView>(R.id.image_expand)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_faq, parent, false)
            return FAQViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
            val item = items[position]
            holder.questionText.text = item.question
            holder.answerText.text = item.answer
            
            // Set visibility based on expanded state
            val isExpanded = expandedItems.contains(position)
            holder.answerText.visibility = if (isExpanded) View.VISIBLE else View.GONE
            holder.expandIcon.rotation = if (isExpanded) 180f else 0f
            
            // Set click listener to toggle expansion
            holder.itemView.setOnClickListener {
                if (isExpanded) {
                    expandedItems.remove(position)
                } else {
                    expandedItems.add(position)
                }
                notifyItemChanged(position)
            }
        }
        
        override fun getItemCount() = items.size
    }
} 