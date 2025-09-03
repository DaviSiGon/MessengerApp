package com.example.messengerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.messengerapp.R
import com.example.messengerapp.models.Chat
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private val chats: List<Chat>,
    private val onChatClick: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
        holder.itemView.setOnClickListener { onChatClick(chat) }
    }

    override fun getItemCount(): Int = chats.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chatName: TextView = itemView.findViewById(R.id.chat_name)
        private val lastMessage: TextView = itemView.findViewById(R.id.last_message)
        private val messageTime: TextView = itemView.findViewById(R.id.message_time)

        fun bind(chat: Chat) {
            chatName.text = chat.chatName
            lastMessage.text = chat.lastMessage
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            messageTime.text = timeFormat.format(chat.lastMessageTimestamp)
        }
    }
}