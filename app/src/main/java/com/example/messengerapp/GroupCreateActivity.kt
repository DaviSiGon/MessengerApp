package com.example.messengerapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messengerapp.adapters.ContactsAdapter
import com.example.messengerapp.databinding.ActivityGroupCreateBinding
import com.example.messengerapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class GroupCreateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupCreateBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var contactsAdapter: ContactsAdapter
    private val contacts = mutableListOf<User>()
    private val selectedContacts = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadContacts()
        setupListeners()
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(contacts) { contact ->
            if (selectedContacts.contains(contact.userId)) {
                selectedContacts.remove(contact.userId)
            } else {
                selectedContacts.add(contact.userId)
            }
            contactsAdapter.notifyDataSetChanged()
        }

        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GroupCreateActivity)
            adapter = contactsAdapter
        }
    }

    private fun loadContacts() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                contacts.clear()
                snapshot?.documents?.forEach { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null && user.userId != currentUserId) {
                        contacts.add(user)
                    }
                }
                contactsAdapter.notifyDataSetChanged()
            }
    }

    private fun setupListeners() {
        binding.createGroupButton.setOnClickListener {
            val groupName = binding.groupNameEditText.text.toString().trim()
            if (groupName.isNotEmpty() && selectedContacts.isNotEmpty()) {
                createGroup(groupName)
            } else {
                Toast.makeText(this, "Preencha o nome e selecione participantes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createGroup(groupName: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = UUID.randomUUID().toString()
        val participants = selectedContacts.toMutableList().apply {
            add(currentUserId)
        }

        val chat = hashMapOf(
            "chatId" to chatId,
            "chatName" to groupName,
            "participants" to participants,
            "isGroup" to true,
            "lastMessage" to "",
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        db.collection("chats").document(chatId)
            .set(chat)
            .addOnSuccessListener {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatName", groupName)
                    putExtra("isGroup", true)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao criar grupo", Toast.LENGTH_SHORT).show()
            }
    }
}