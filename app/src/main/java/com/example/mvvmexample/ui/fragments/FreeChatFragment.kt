package com.example.mvvmexample.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mvvmexample.adapter.ChatAdapter
import com.example.mvvmexample.data.MessageData
import com.example.mvvmexample.databinding.FragmentFreeChatBinding
import com.example.mvvmexample.ui.viewmodel.FirebaseDBViewModel

class FreeChatFragment : Fragment() {
    private val viewModel: FirebaseDBViewModel by viewModels()
    private lateinit var binding: FragmentFreeChatBinding
    private lateinit var messageList: MutableList<MessageData>
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        messageList = mutableListOf()
        chatAdapter = ChatAdapter(messageList)
        binding = FragmentFreeChatBinding.inflate(layoutInflater, container, false)

        binding.chatRecyclerView.run {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // chatting adapter를 구현해 uid로 사용자와 대상을 구분하며 채팅을 무한 스크롤로 이어갈 수 있게 만든다.
        // view model에 msg객체를 가져오라고 항상 요청한다.
        viewModel.getMessage()

        // view model에서 message 변경 시, textview에 추가해준다.
        viewModel.message.observe(viewLifecycleOwner) { list ->
            // 초기화된 textview에 messagedata를 전부 append 해준다. -> textview에서 recyclerview로 수정 - 완료
            // list를 초기화 시켜주고 다시 담기
            messageList.clear()
            list.forEach {
                messageList.add(it)
            }
            chatAdapter.notifyDataSetChanged()
            binding.chatRecyclerView.scrollToPosition(messageList.size - 1)
        }

        // view model에 사용자의 입력 사항을 보낸다.
        binding.textInputLayout.setEndIconOnClickListener {
            val chatValue = binding.editText.text.toString()
            // 메세지가 비어있는지 유효성 검사
            if (chatValue.isNotEmpty()) {
                viewModel.sendMessage(chatValue)
                binding.editText.setText("")
                hideKeyboard(it)
            } else {
                binding.textInputLayout.error = "보낼 메세지가 없습니다."
            }
        }

        // textinputlayout의 error 상태를 사용자가 신규 text 입력 시 초기화 해준다.
        binding.editText.addTextChangedListener {
            binding.textInputLayout.error = null
        }
    }

    // 키보드를 내려주는 method
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}