package com.example.messengerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messengerapp.adapters.ContactsAdapter
import com.example.messengerapp.databinding.FragmentContactsBinding
import com.example.messengerapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var contactsAdapter: ContactsAdapter
    private val contacts = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            layoutManager = LinearLayoutManager(requireContext())
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
                val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatName", contact.name)
                    putExtra("isGroup", false)
                }
                startActivity(intent)
            }
    }
}