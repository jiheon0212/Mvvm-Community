package com.example.mvvmexample.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.databinding.ChatRecyclerReceiverBinding
import com.example.mvvmexample.databinding.ChatRecyclerSenderBinding

// 다수의 viewholder 사용 시 adapter의 참조타입을 특정 viewholder가 아닌 recyclerview.viewholder로 해준다.
class ChatAdapter(private val messageList: MutableList<MessageData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // 타입 상수 선언
    private val VIEW_TYPE_SEND = 1
    private val VIEW_TYPE_RECEIVED = 2

    inner class ChatViewSenderHolder(val binding: ChatRecyclerSenderBinding): RecyclerView.ViewHolder(binding.root)
    inner class ChatViewReceiverHolder(val binding: ChatRecyclerReceiverBinding): RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messageData = messageList[position]
        // 구조 분해를 통해 messageData 객체를 받아온다.
        val (time, message, sender) = messageData

        // 분기된 view_type을 통해서 holder를 설정해준다.
        if (holder.itemViewType == VIEW_TYPE_SEND) {
            (holder as ChatViewSenderHolder).binding.apply {
                tvMessage.text = message
                tvCurrentTime.text = time
                tvUserName.text = sender
            }
        } else {
            (holder as ChatViewReceiverHolder).binding.apply {
                tvMessage.text = message
                tvCurrentTime.text = time
                tvUserName.text = sender
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
        return if (messageList[position].sender == "sender uid") VIEW_TYPE_SEND else VIEW_TYPE_RECEIVED
    }
}