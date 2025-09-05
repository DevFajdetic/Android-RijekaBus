package com.example.rijekabusapp.fragments

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.RijekaBusApplication
import com.example.rijekabusapp.adapters.CommentAdapter
import com.example.rijekabusapp.database.models.ObjectType
import com.example.rijekabusapp.databinding.FragmentStationLinesBinding
import com.example.rijekabusapp.databinding.LayoutCommentsBinding
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.CommentViewModel
import java.lang.ref.WeakReference

/**
 * Helper class to add social features to StationLinesFragment
 */
class StationSocialExtension(
    private val fragment: Fragment,
    private val binding: FragmentStationLinesBinding,
    private val commentsBinding: LayoutCommentsBinding
) {
    private lateinit var commentViewModel: CommentViewModel
    
    private val fragmentRef = WeakReference(fragment)
    private var stationId: String? = null
    
    private val app by lazy { fragment.requireActivity().application as RijekaBusApplication }
    private val firebaseAuthHelper by lazy { app.firebaseAuthHelper }
    
    // Setup method to initialize all social features
    fun setup(station: Station) {
        stationId = station.id.toString()
        
        // Initialize ViewModels
        initializeViewModels()
        
        // Setup comments section
        setupCommentsSection()
    }
    
    private fun initializeViewModels() {
        commentViewModel = ViewModelProvider(fragment.requireActivity())[CommentViewModel::class.java]
        
        // Clear any error messages
        commentViewModel.clearErrorMessage()
    }
    
    // Comments section setup
    private fun setupCommentsSection() {
        if (stationId == null) return
        
        // Initialize comments section
        commentsBinding.buttonExpandComments.setOnClickListener {
            toggleCommentsSection()
        }
        
        // Setup RecyclerView
        commentsBinding.recyclerComments.layoutManager = LinearLayoutManager(fragment.context)
        
        // Setup adapter
        val userId = firebaseAuthHelper.getUserId()
        val adapter = CommentAdapter(
            userId,
            { commentMap -> likeComment(commentMap) },
            { commentMap -> deleteComment(commentMap) }
        )
        commentsBinding.recyclerComments.adapter = adapter
        
        // Setup add comment button
        commentsBinding.buttonAddComment.setOnClickListener {
            addComment()
        }
        
        // Observe comments list
        commentViewModel.comments.observe(fragment.viewLifecycleOwner) { comments ->
            adapter.submitList(comments)
            
            // Show empty state if needed
            if (comments.isEmpty()) {
                commentsBinding.textNoComments.visibility = View.VISIBLE
            } else {
                commentsBinding.textNoComments.visibility = View.GONE
            }
        }
        
        // Observe loading state
        commentViewModel.isLoading.observe(fragment.viewLifecycleOwner) { isLoading ->
            commentsBinding.progressComments.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe error messages
        commentViewModel.errorMessage.observe(fragment.viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(fragment.context, it, Toast.LENGTH_SHORT).show()
                commentViewModel.clearErrorMessage()
            }
        }
    }
    
    private fun toggleCommentsSection() {
        val isExpanded = commentsBinding.recyclerComments.visibility == View.VISIBLE
        
        // Rotate the arrow
        val rotation = if (isExpanded) 0f else 180f
        ObjectAnimator.ofFloat(commentsBinding.buttonExpandComments, "rotation", 
            commentsBinding.buttonExpandComments.rotation, rotation)
            .apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        
        // Toggle the visibility
        val newVisibility = if (isExpanded) View.GONE else View.VISIBLE
        commentsBinding.recyclerComments.visibility = newVisibility
        commentsBinding.inputLayoutComment.visibility = newVisibility
        commentsBinding.buttonAddComment.visibility = newVisibility
        
        // Load comments if expanding
        if (!isExpanded) {
            loadComments()
        }
    }
    
    private fun loadComments() {
        stationId?.let { id ->
            commentViewModel.loadComments(id, ObjectType.STATION)
        }
    }
    
    private fun addComment() {
        val commentText = commentsBinding.editTextComment.text?.toString()?.trim() ?: ""
        
        if (commentText.isEmpty()) {
            Toast.makeText(fragment.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        stationId?.let { id ->
            val userId = firebaseAuthHelper.getUserId()
            val username = firebaseAuthHelper.getUsername()
            
            commentViewModel.addComment(id, ObjectType.STATION, userId, username, commentText)
            
            // Clear input field
            commentsBinding.editTextComment.text?.clear()
        }
    }
    
    private fun likeComment(commentMap: Map<String, Any>) {
        commentViewModel.likeComment(commentMap)
    }
    
    private fun deleteComment(commentMap: Map<String, Any>) {
        commentViewModel.deleteComment(commentMap)
    }
    
    // Cleanup method to remove listeners
    fun cleanup() {
        // No specific cleanup needed yet
    }
} 