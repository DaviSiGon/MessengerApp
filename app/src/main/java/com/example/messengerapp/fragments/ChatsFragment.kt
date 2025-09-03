package com.example.messengerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messengerapp.adapters.ChatAdapter
import com.example.messengerapp.databinding.FragmentChatsBinding
import com.example.messengerapp.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatListener: ListenerRegistration
    private val chats = mutableListOf<Chat>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadChats()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(chats) { chat ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("chatId", chat.chatId)
                putExtra("chatName", chat.chatName)
                putExtra("isGroup", chat.isGroup)
            }
            startActivity(intent)
        }

        binding.chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun loadChats() {
        val currentUserId = auth.currentUser?.uid ?: return

        chatListener = db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                chats.clear()
                snapshot?.documents?.forEach { document ->
                    val chat = document.toObject(Chat::class.java)
                    chat?.let { chats.add(it) }
                }
                chats.sortByDescending { it.lastMessageTimestamp }
                chatAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatListener?.remove()
    }
}