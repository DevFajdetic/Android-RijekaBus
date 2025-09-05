package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.RijekaBusApplication
import com.example.rijekabusapp.SavedPreference
import com.example.rijekabusapp.adapters.ChatMessageAdapter
import com.example.rijekabusapp.databinding.FragmentChatBinding
import com.example.rijekabusapp.firebase.FirebaseAuthHelper
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.viewmodels.ChatViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.DividerItemDecoration
import coil.load
import coil.transform.CircleCropTransformation

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatMessageAdapter
    
    private val firebaseAuthHelper: FirebaseAuthHelper by lazy {
        (requireActivity().application as RijekaBusApplication).firebaseAuthHelper
    }
    
    private var currentChatMode = CHAT_MODE_PUBLIC
    private var selectedUserId: String? = null
    private var selectedUsername: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupMessageSending()
        setupTabs()
        
        if (!requireContext().isOnline()) {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }
    
    private fun setupToolbar() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbarTitle.text = getString(R.string.public_chat)
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        
        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        
        // Observe chat messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            binding.progressBar.visibility = View.GONE
            
            if (messages.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.emptyState.setupEmptyStateView(getString(R.string.no_messages_yet))
            } else {
                binding.emptyState.visibility = View.GONE
                adapter.submitList(messages)
                
                // Scroll to the bottom to show the latest message
                if (messages.isNotEmpty()) {
                    binding.recyclerChat.post {
                        binding.recyclerChat.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
        
        // Observe chat users for direct messaging
        viewModel.chatUsers.observe(viewLifecycleOwner) { users ->
            binding.progressBar.visibility = View.GONE
            
            if (users.isNotEmpty() && currentChatMode == CHAT_MODE_DIRECT && selectedUserId == null) {
                showUserSelectionDialog(users)
            }
        }
        
        // Load public chat messages by default
        loadPublicChat()
    }
    
    private fun setupRecyclerView() {
        // Get current user ID
        val currentUserId = if (firebaseAuthHelper.isUserSignedIn()) {
            firebaseAuthHelper.getUserId()
        } else {
            "anonymous"
        }
        
        adapter = ChatMessageAdapter(currentUserId)
        binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChat.adapter = adapter
        binding.recyclerChat.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }
    
    private fun setupMessageSending() {
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editMessage.text.toString().trim()
            
            if (messageText.isEmpty()) {
                return@setOnClickListener
            }
            
            if (SavedPreference.getEmail(requireContext())?.isEmpty() == true) {
                showCustomDialog(getString(R.string.login_to_chat), requireContext())
                return@setOnClickListener
            }
            
            if (!requireContext().isOnline()) {
                showCustomDialog(getString(R.string.no_internet_connection), requireContext())
                return@setOnClickListener
            }
            
            // Get user info
            val userId = firebaseAuthHelper.getUserId()
            val username = if (SavedPreference.getUsername(requireContext())?.isNotEmpty() == true) {
                SavedPreference.getUsername(requireContext())
            } else if (SavedPreference.getGivenName(requireContext())?.isNotEmpty() == true) {
                "${SavedPreference.getGivenName(requireContext())} ${SavedPreference.getFamilyName(requireContext())}"
            } else {
                firebaseAuthHelper.getUsername()
            }
            
            // Send the message based on current chat mode
            if (username != null) {
                when (currentChatMode) {
                    CHAT_MODE_PUBLIC -> {
                        viewModel.sendMessage("public", userId, username, messageText)
                    }
                    CHAT_MODE_DIRECT -> {
                        selectedUserId?.let { receiverId ->
                            viewModel.sendDirectMessage(userId, username, receiverId, messageText)
                        }
                    }
                }
            }
            
            // Clear the input field
            binding.editMessage.text.clear()
        }
    }
    
    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.public_chat))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.direct_message))
        
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentChatMode = CHAT_MODE_PUBLIC
                        binding.toolbarTitle.text = getString(R.string.public_chat)
                        binding.directMessageInfo.visibility = View.GONE
                        loadPublicChat()
                    }
                    1 -> {
                        currentChatMode = CHAT_MODE_DIRECT
                        binding.toolbarTitle.text = getString(R.string.direct_message)
                        
                        // Reset selected user
                        selectedUserId = null
                        selectedUsername = null
                        
                        // Load users to select for direct messaging
                        viewModel.loadChatUsers()
                    }
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    // Allow selecting a different user when direct message tab is reselected
                    selectedUserId = null
                    selectedUsername = null
                    viewModel.loadChatUsers()
                }
            }
        })
    }
    
    private fun loadPublicChat() {
        binding.directMessageInfo.visibility = View.GONE
        viewModel.loadChatMessages("public")
    }
    
    private fun loadDirectChat(userId: String, otherUserId: String, otherUsername: String) {
        binding.directMessageInfo.visibility = View.VISIBLE
        binding.directMessageUsername.text = otherUsername
        
        // Load user profile image if available
        val photoUrl = SavedPreference.getPictureUrl(requireContext())
        binding.directMessageUserImage.load(photoUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }
        
        viewModel.loadDirectMessages(userId, otherUserId)
    }
    
    private fun showUserSelectionDialog(users: List<Map<String, Any>>) {
        val currentUserId = firebaseAuthHelper.getUserId()
        
        // Filter out current user
        val otherUsers = users.filter { it["id"] != currentUserId }
        
        if (otherUsers.isEmpty()) {
            Toast.makeText(requireContext(), "No other users available for chat", Toast.LENGTH_SHORT).show()
            return
        }
        
        val usernames = otherUsers.map { it["username"] as String }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.select_user))
            .setItems(usernames) { _, which ->
                val selectedUser = otherUsers[which]
                selectedUserId = selectedUser["id"] as String
                selectedUsername = selectedUser["username"] as String
                
                binding.toolbarTitle.text = selectedUsername
                
                loadDirectChat(currentUserId, selectedUserId!!, selectedUsername!!)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                // Switch back to public chat if user cancels
                binding.tabLayout.getTabAt(0)?.select()
            }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        private const val CHAT_MODE_PUBLIC = 0
        private const val CHAT_MODE_DIRECT = 1
    }
} 