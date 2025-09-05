package com.example.rijekabusapp.fragments

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.RijekaBusApplication
import com.example.rijekabusapp.adapters.BusRatingAdapter
import com.example.rijekabusapp.adapters.ChatMessageAdapter
import com.example.rijekabusapp.adapters.CommentAdapter
import com.example.rijekabusapp.database.models.ObjectType
import com.example.rijekabusapp.databinding.FragmentLineStationsBinding
import com.example.rijekabusapp.databinding.LayoutBusRatingBinding
import com.example.rijekabusapp.databinding.LayoutChatBinding
import com.example.rijekabusapp.databinding.LayoutCommentsBinding
import com.example.rijekabusapp.viewmodels.ChatViewModel
import com.example.rijekabusapp.viewmodels.CommentViewModel
import com.example.rijekabusapp.viewmodels.RatingViewModel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import androidx.core.view.isVisible

/**
 * Helper class to add social features to LineStationsFragment
 */
class LineStationsSocialExtension(
    private val fragment: Fragment,
    private val binding: FragmentLineStationsBinding,
    private val ratingBinding: LayoutBusRatingBinding,
    private val commentsBinding: LayoutCommentsBinding,
    private val chatBinding: LayoutChatBinding
) {
    private lateinit var ratingViewModel: RatingViewModel
    private lateinit var commentViewModel: CommentViewModel
    private lateinit var chatViewModel: ChatViewModel
    
    private val fragmentRef = WeakReference(fragment)
    private var currentBusId: String? = null
    private var lineNumber: String? = null
    
    private val app by lazy { fragment.requireActivity().application as RijekaBusApplication }
    private val firebaseAuthHelper by lazy { app.firebaseAuthHelper }
    
    // Setup method to initialize all social features
    fun setup(busId: String, lineNum: String) {
        currentBusId = busId
        lineNumber = lineNum
        
        // Initialize ViewModels
        initializeViewModels()
        
        // Setup each social feature section
        setupRatingSection()
        setupCommentsSection()
        setupChatSection()
    }
    
    private fun initializeViewModels() {
        ratingViewModel = ViewModelProvider(fragment.requireActivity())[RatingViewModel::class.java]
        commentViewModel = ViewModelProvider(fragment.requireActivity())[CommentViewModel::class.java]
        chatViewModel = ViewModelProvider(fragment.requireActivity())[ChatViewModel::class.java]
        
        // Clear any error messages
        ratingViewModel.clearErrorMessage()
        commentViewModel.clearErrorMessage()
        chatViewModel.clearErrorMessage()
    }
    
    // Rating section setup
    private fun setupRatingSection() {
        if (currentBusId == null) {
            Log.e("LineStationsSocialExtension", "currentBusId is null")
            return
        }
        
        // Initialize rating section
        ratingBinding.buttonExpandRatings.setOnClickListener {
            toggleRatingSection()
        }
        
        // Setup RatingBar
        ratingBinding.ratingBarUser.setOnRatingBarChangeListener { _, rating, _ ->
            // Enable/disable submit button based on rating
            ratingBinding.buttonSubmitRating.isEnabled = rating > 0
        }
        
        // Setup submit button
        ratingBinding.buttonSubmitRating.setOnClickListener {
            submitRating()
        }
        
        // Set initial visibility - collapsed by default
        ratingBinding.divider.visibility = View.GONE
        ratingBinding.textYourRating.visibility = View.GONE
        ratingBinding.ratingBarUser.visibility = View.GONE
        ratingBinding.buttonSubmitRating.visibility = View.GONE
        ratingBinding.textAlreadyRated.visibility = View.GONE
        ratingBinding.recyclerRatings.visibility = View.GONE
        
        // Observe average rating
        ratingViewModel.averageRating.observe(fragment.viewLifecycleOwner) { average ->
            ratingBinding.ratingBarAverage.rating = average
            ratingBinding.textRatingValue.text = String.format("%.1f", average)
        }
        
        // Observe if user can rate, but don't change visibility yet
        ratingViewModel.userCanRate.observe(fragment.viewLifecycleOwner) { _ ->
            // We'll update the visibility only when expanded
        }
        
        // Observe loading state
        ratingViewModel.isLoading.observe(fragment.viewLifecycleOwner) { isLoading ->
            ratingBinding.progressRating.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe error messages
        ratingViewModel.errorMessage.observe(fragment.viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(fragment.context, it, Toast.LENGTH_SHORT).show()
                ratingViewModel.clearErrorMessage()
            }
        }
        
        // Load initial data
        loadRatingData()
    }
    
    private fun loadRatingData() {
        currentBusId?.let { busId ->
            // Load average rating
            ratingViewModel.loadBusRating(busId)
            
            // Check if user can rate
            val userId = firebaseAuthHelper.getUserId()
            ratingViewModel.checkIfUserCanRate(busId, userId)
        }
    }
    
    private fun submitRating() {
        val rating = ratingBinding.ratingBarUser.rating
        if (rating <= 0) {
            Toast.makeText(fragment.context, "Please select a rating", Toast.LENGTH_SHORT).show()
            return
        }
        
        currentBusId?.let { busId ->
            val userId = firebaseAuthHelper.getUserId()
            val username = firebaseAuthHelper.getUsername()
            
            ratingViewModel.rateBus(busId, userId, username, rating)
        }
    }
    
    private fun toggleRatingSection() {
        val isExpanded = ratingBinding.divider.visibility == View.VISIBLE
        
        // Rotate the arrow
        val rotation = if (isExpanded) 0f else 180f
        ObjectAnimator.ofFloat(ratingBinding.buttonExpandRatings, "rotation", 
            ratingBinding.buttonExpandRatings.rotation, rotation)
            .apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        
        // Toggle visibility based on expansion state
        if (isExpanded) {
            // Collapse
            ratingBinding.divider.visibility = View.GONE
            ratingBinding.textYourRating.visibility = View.GONE
            ratingBinding.ratingBarUser.visibility = View.GONE
            ratingBinding.buttonSubmitRating.visibility = View.GONE
            ratingBinding.textAlreadyRated.visibility = View.GONE
            ratingBinding.recyclerRatings.visibility = View.GONE
        } else {
            // Expand
            ratingBinding.divider.visibility = View.VISIBLE
            
            // Show appropriate UI based on whether user can rate
            if (ratingViewModel.userCanRate.value == true) {
                ratingBinding.textYourRating.visibility = View.VISIBLE
                ratingBinding.ratingBarUser.visibility = View.VISIBLE
                ratingBinding.buttonSubmitRating.visibility = View.VISIBLE
                ratingBinding.textAlreadyRated.visibility = View.GONE
            } else {
                ratingBinding.textYourRating.visibility = View.GONE
                ratingBinding.ratingBarUser.visibility = View.GONE
                ratingBinding.buttonSubmitRating.visibility = View.GONE
                ratingBinding.textAlreadyRated.visibility = View.VISIBLE
            }
            
            // Load ratings list
            loadRatingsRecyclerView()
        }
    }
    
    private fun loadRatingsRecyclerView() {
        currentBusId?.let { busId ->
            val adapter = BusRatingAdapter()
            ratingBinding.recyclerRatings.layoutManager = LinearLayoutManager(fragment.context)
            ratingBinding.recyclerRatings.adapter = adapter
            
            // Load ratings from Firebase using the async method with callback
            Log.d("LineStationsSocialExtension", "Getting bus ratings...")
            app.firebaseRepository.getBusRatingsForTodayAsync(busId) { ratingsList ->
                if (ratingsList.isNotEmpty()) {
                    Log.d("LineStationsSocialExtension", "Ratings list size: ${ratingsList.size}")
                    adapter.submitList(ratingsList)
                    ratingBinding.recyclerRatings.visibility = View.VISIBLE
                } else {
                    Log.d("LineStationsSocialExtension", "No ratings found")
                    ratingBinding.recyclerRatings.visibility = View.GONE
                }
            }
        }
    }
    
    // Comments section setup
    private fun setupCommentsSection() {
        if (currentBusId == null) return
        
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
        
        // Set initial visibility - collapsed by default
        commentsBinding.recyclerComments.visibility = View.GONE
        commentsBinding.inputLayoutComment.visibility = View.GONE
        commentsBinding.buttonAddComment.visibility = View.GONE
        commentsBinding.textNoComments.visibility = View.GONE
        
        // Setup add comment button
        commentsBinding.buttonAddComment.setOnClickListener {
            addComment()
        }
        
        // Observe comments list
        commentViewModel.comments.observe(fragment.viewLifecycleOwner) { comments ->
            adapter.submitList(comments)
            
            // Only update visibility if already expanded
            if (commentsBinding.recyclerComments.visibility == View.VISIBLE) {
                commentsBinding.textNoComments.visibility = if (comments.isEmpty()) View.VISIBLE else View.GONE
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
        
        if (isExpanded) {
            // Collapse
            commentsBinding.recyclerComments.visibility = View.GONE
            commentsBinding.inputLayoutComment.visibility = View.GONE
            commentsBinding.buttonAddComment.visibility = View.GONE
            commentsBinding.textNoComments.visibility = View.GONE
        } else {
            // Expand
            commentsBinding.recyclerComments.visibility = View.VISIBLE
            commentsBinding.inputLayoutComment.visibility = View.VISIBLE
            commentsBinding.buttonAddComment.visibility = View.VISIBLE
            
            // Show empty state if needed
            val comments = commentViewModel.comments.value ?: emptyList()
            commentsBinding.textNoComments.visibility = if (comments.isEmpty()) View.VISIBLE else View.GONE
            
            // Load comments
            loadComments()
        }
    }
    
    private fun loadComments() {
        currentBusId?.let { busId ->
            commentViewModel.loadComments(busId, ObjectType.BUS)
        }
    }
    
    private fun addComment() {
        val commentText = commentsBinding.editTextComment.text?.toString()?.trim() ?: ""
        
        if (commentText.isEmpty()) {
            Toast.makeText(fragment.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        currentBusId?.let { busId ->
            val userId = firebaseAuthHelper.getUserId()
            val username = firebaseAuthHelper.getUsername()
            
            commentViewModel.addComment(busId, ObjectType.BUS, userId, username, commentText)
            
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
    
    // Chat section setup
    private fun setupChatSection() {
        if (lineNumber == null) return
        
        // Initialize chat section
        chatBinding.buttonExpandChat.setOnClickListener {
            toggleChatSection()
        }
        
        // Setup RecyclerView
        chatBinding.recyclerChat.layoutManager = LinearLayoutManager(fragment.context)
        
        // Setup adapter
        val userId = firebaseAuthHelper.getUserId()
        val adapter = ChatMessageAdapter(userId)
        chatBinding.recyclerChat.adapter = adapter
        
        // Setup send message button
        chatBinding.buttonSendMessage.setOnClickListener {
            sendChatMessage()
        }
        
        // Observe chat messages
        chatViewModel.messages.observe(fragment.viewLifecycleOwner) { messages ->
            adapter.submitList(messages)
            
            // Scroll to bottom when new messages arrive
            if (messages.isNotEmpty()) {
                chatBinding.recyclerChat.post {
                    chatBinding.recyclerChat.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
        
        // Observe loading state
        chatViewModel.isLoading.observe(fragment.viewLifecycleOwner) { isLoading ->
            chatBinding.progressChat.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe error messages
        chatViewModel.errorMessage.observe(fragment.viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(fragment.context, it, Toast.LENGTH_SHORT).show()
                chatViewModel.clearErrorMessage()
            }
        }
    }
    
    private fun toggleChatSection() {
        val isExpanded = chatBinding.recyclerChat.visibility == View.VISIBLE
        
        // Rotate the arrow
        val rotation = if (isExpanded) 0f else 180f
        ObjectAnimator.ofFloat(chatBinding.buttonExpandChat, "rotation", 
            chatBinding.buttonExpandChat.rotation, rotation)
            .apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        
        // Toggle the visibility
        val newVisibility = if (isExpanded) View.GONE else View.VISIBLE
        chatBinding.recyclerChat.visibility = newVisibility
        chatBinding.inputLayoutMessage.visibility = newVisibility
        chatBinding.buttonSendMessage.visibility = newVisibility
        
        // Load chat messages if expanding
        if (!isExpanded) {
            loadChatMessages()
        }
    }
    
    private fun loadChatMessages() {
        lineNumber?.let { lineNum ->
            chatViewModel.loadChatMessages(lineNum)
        }
    }
    
    private fun sendChatMessage() {
        val messageText = chatBinding.editTextMessage.text?.toString()?.trim() ?: ""
        
        if (messageText.isEmpty()) {
            Toast.makeText(fragment.context, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        lineNumber?.let { lineNum ->
            val userId = firebaseAuthHelper.getUserId()
            val username = firebaseAuthHelper.getUsername()
            
            chatViewModel.sendMessage(lineNum, userId, username, messageText)
            
            // Clear input field
            chatBinding.editTextMessage.text?.clear()
        }
    }
    
    // Cleanup method to remove listeners
    fun cleanup() {
        // No specific cleanup needed yet
    }
} 