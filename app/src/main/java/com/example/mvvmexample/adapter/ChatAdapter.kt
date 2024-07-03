package com.example.mvvmexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.databinding.ChatRecyclerViewBinding

class ChatAdapter(private val messageList: MutableList<MessageData>): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    inner class ChatViewHolder(val binding: ChatRecyclerViewBinding): RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val messageData = messageList[position]
        val (time, message, sender) = messageData

        holder.binding.apply {
            tvMessage.text = message
            tvCurrentTime.text = time
            tvUserName.text = sender
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatRecyclerViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }
    override fun getItemCount(): Int = messageList.size
}