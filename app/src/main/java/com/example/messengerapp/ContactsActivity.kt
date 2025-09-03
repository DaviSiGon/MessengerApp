package com.example.messengerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messengerapp.adapters.ContactsAdapter
import com.example.messengerapp.databinding.ActivityContactsBinding
import com.example.messengerapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var contactsAdapter: ContactsAdapter
    private val contacts = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadContacts()
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(contacts) { contact ->
            createChat(contact)
        }

        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ContactsActivity)
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

    private fun createChat(contact: User) {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatId = "$currentUserId-${contact.userId}"

        val chat = hashMapOf(
            "chatId" to chatId,
            "chatName" to contact.name,
            "participants" to listOf(currentUserId, contact.userId),
            "isGroup" to false,
            "lastMessage" to "",
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        db.collection("chats").document(chatId)
            .set(chat)
            .addOnSuccessListener {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatName", contact.name)
                    putExtra("isGroup", false)
                }
                startActivity(intent)
                finish()
            }
    }
}