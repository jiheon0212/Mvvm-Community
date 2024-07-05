package com.example.mvvmexample.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.databinding.ChatRecyclerReceiverBinding
import com.example.mvvmexample.databinding.ChatRecyclerSenderBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// 다수의 viewholder 사용 시 adapter의 참조타입을 특정 viewholder가 아닌 recyclerview.viewholder로 해준다.
@SuppressLint("NotifyDataSetChanged")
class ChatAdapter(private var messageList: MutableList<MessageData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 타입 상수 선언
    private val VIEW_TYPE_SEND = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val auth = Firebase.auth

    inner class ChatViewSenderHolder(val binding: ChatRecyclerSenderBinding): RecyclerView.ViewHolder(binding.root)
    inner class ChatViewReceiverHolder(val binding: ChatRecyclerReceiverBinding): RecyclerView.ViewHolder(binding.root)

    fun newMessage(newMessageList: MutableList<MessageData>) {
        messageList = newMessageList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageData = messageList[position]
        // 구조 분해를 통해 messageData 객체를 받아온다.
        val (time, message, _, sender, image) = messageData

        // 분기된 view_type을 통해서 holder를 설정해준다.
        if (holder.itemViewType == VIEW_TYPE_SEND) {
            (holder as ChatViewSenderHolder).binding.apply {
                tvMessage.text = message
                tvCurrentTime.text = time

                // todo - user가 정보를 수정하면 메세지 객체들도 전부 수정되는 방식으로 구현 예정
                tvUserName.text = sender
                Glide.with(imgUserProfile).load(image).into(imgUserProfile)
            }
        } else {
            (holder as ChatViewReceiverHolder).binding.apply {
                tvMessage.text = message
                tvCurrentTime.text = time

                tvUserName.text = sender
                Glide.with(imgUserProfile).load(image).into(imgUserProfile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val senderView = ChatRecyclerSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val receiverView = ChatRecyclerReceiverBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 구분한 타입을 통해 binding해줄 viewholder를 설정해준다.
        return if (viewType == VIEW_TYPE_SEND) {
            ChatViewSenderHolder(senderView)
        } else {
            ChatViewReceiverHolder(receiverView)
        }
    }
    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        // messageData 객체에 담긴 uid를 통해 sender인지 recevier인지 구분한다.
        return if (messageList[position].uid == auth.uid) VIEW_TYPE_SEND else VIEW_TYPE_RECEIVED
    }
}