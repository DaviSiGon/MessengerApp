package com.example.messengerapp

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messengerapp.adapters.MessageAdapter
import com.example.messengerapp.databinding.ActivityChatBinding
import com.example.messengerapp.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
import java.util.UUID

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatId: String
    private lateinit var chatName: String
    private var isGroup: Boolean = false
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        chatId = intent.getStringExtra("chatId") ?: ""
        chatName = intent.getStringExtra("chatName") ?: ""
        isGroup = intent.getBooleanExtra("isGroup", false)

        binding.chatNameTextView.text = chatName

        setupRecyclerView()
        loadMessages()
        setupListeners()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, auth.currentUser?.uid ?: "")
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun loadMessages() {
        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                messages.clear()
                snapshot?.documents?.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    message?.let { messages.add(it) }
                }
                messageAdapter.notifyDataSetChanged()
                binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
    }

    private fun setupListeners() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, null)
                binding.messageEditText.text.clear()
            }
        }

        binding.attachButton.setOnClickListener {
            // Implementar anexo de mídia
        }
    }

    private fun sendMessage(text: String, mediaUrl: String?) {
        val currentUserId = auth.currentUser?.uid ?: return
        val messageId = UUID.randomUUID().toString()

        val message = Message(
            messageId = messageId,
            senderId = currentUserId,
            text = text,
            mediaUrl = mediaUrl,
            timestamp = Date(),
            status = "sent"
        )

        db.collection("chats").document(chatId)
            .collection("messages").document(messageId)
            .set(message)
            .addOnSuccessListener {
                updateLastMessage(text)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao enviar mensagem", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateLastMessage(lastMessage: String) {
        db.collection("chats").document(chatId)
            .update(
                "lastMessage", lastMessage,
                "lastMessageTimestamp", FieldValue.serverTimestamp()
            )
    }

    fun onBackClicked(view: View) {
        finish()
    }
}