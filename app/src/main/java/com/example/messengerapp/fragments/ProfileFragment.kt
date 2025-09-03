package com.example.messengerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.messengerapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserProfile()
        setupListeners()
    }

    private fun loadUserProfile() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val user = snapshot?.toObject(User::class.java)
                user?.let {
                    binding.nameEditText.setText(it.name)
                    binding.emailEditText.setText(it.email)
                    binding.statusEditText.setText(it.status)

                    if (it.profileImage.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(it.profileImage)
                            .into(binding.profileImageView)
                    }
                }
            }
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            updateProfile()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.profileImageView.setOnClickListener {
            // Implementar seleção de imagem
        }
    }

    private fun updateProfile() {
        val currentUserId = auth.currentUser?.uid ?: return
        val name = binding.nameEditText.text.toString().trim()
        val status = binding.statusEditText.text.toString().trim()

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "status" to status
        )

        db.collection("users").document(currentUserId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Perfil atualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Erro ao atualizar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }
}