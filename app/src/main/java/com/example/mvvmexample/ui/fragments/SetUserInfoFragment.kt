package com.example.mvvmexample.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.mvvmexample.R
import com.example.mvvmexample.data.ModifyCallback
import com.example.mvvmexample.databinding.DialogImageBinding
import com.example.mvvmexample.databinding.DialogNicknameBinding
import com.example.mvvmexample.databinding.DialogSexBinding
import com.example.mvvmexample.databinding.FragmentSetUserInfoBinding
import com.example.mvvmexample.ui.viewmodel.FirebaseAuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SetUserInfoFragment : Fragment(), ModifyCallback {
    private lateinit var binding: FragmentSetUserInfoBinding
    private val viewModel: FirebaseAuthViewModel by viewModels()
    private lateinit var reqGallery: ActivityResultLauncher<Intent>
    private lateinit var reqCamera: ActivityResultLauncher<Intent>
    private lateinit var filePath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetUserInfoBinding.inflate(layoutInflater, container, false)
        cameraLauncher(this)
        galleryLauncher(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getUserInformation()

        viewModel.userInformation.observe(viewLifecycleOwner) {
            val (userProfileImage, userNickname, userSex, userBirthDate) = it
            binding.apply {
                tvNickname.text = userNickname
                tvUserSex.text = userSex
                tvUserBirthDate.text = userBirthDate
                Glide.with(requireContext()).load(userProfileImage).into(userImage)
            }
        }
        binding.tvBtnModify.setOnClickListener {
            nicknameDialog(this)
        }
        binding.tvBtnModifyBirthDate.setOnClickListener {
            selectDateDialog(this)
        }
        binding.tvBtnModifySex.setOnClickListener {
            selectSexDialog(this)
        }
        binding.userImage.setOnClickListener {
            imageDialog()
        }
    }

    // userbirthdate 변경 관련 method
    override fun birthDateModify(date: String) {
        viewModel.updateUserInformation(date, 4)
    }

    private fun selectDateDialog(callback: ModifyCallback) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
                callback.birthDateModify(selectedDate)
            }, year, month, day
        )

        datePickerDialog.show()
    }

    // usersex 변경 관련 method
    override fun sexModify(sex: String) {
        viewModel.updateUserInformation(sex, 3)
    }

    private fun selectSexDialog(callback: ModifyCallback) {
        val builder = AlertDialog.Builder(context)
        val dialogInflater = DialogSexBinding.inflate(layoutInflater)
        val dialogView = dialogInflater.root
        val radioGroup = dialogInflater.radioGroup

        builder.setTitle("성별 변경")
        builder.setMessage("변경할 성별을 선택해주세요.")
        builder.setView(dialogView)

        builder.setPositiveButton("확인") { dialog, _ ->
            when (radioGroup.checkedRadioButtonId) {
                R.id.radio_male -> callback.sexModify("남성")
                R.id.radio_female -> callback.sexModify("여성")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // usernickname 변경 관련 method
    override fun nicknameModify(nickname: String) {
        viewModel.updateUserInformation(nickname, 2)
    }

    // nickname 수정에 사용하는 dialog
    private fun nicknameDialog(callback: ModifyCallback) {
        val builder = AlertDialog.Builder(context)
        val dialogInflater = DialogNicknameBinding.inflate(layoutInflater)
        val dialogView = dialogInflater.root
        val editText = dialogInflater.dialogNicknameEdit

        builder.setTitle("닉네임 변경")
        builder.setMessage("변경할 닉네임을 입력해주세요.")
        builder.setView(dialogView)

        builder.setPositiveButton("확인") { dialog, _ ->
            val inputText = editText.text.toString()
            callback.nicknameModify(inputText)
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // viewmodel에 bitmap에 대해 변경 요청을 내리고 storage에 업로드
    override fun imageModify(bitmap: Bitmap) {
        viewModel.updateUserInformation(bitmap, 1)
    }

    // image 교체 dialog 생성 method
    private fun imageDialog() {
        val builder = AlertDialog.Builder(context)
        val dialogInflater = DialogImageBinding.inflate(layoutInflater)
        val dialogView = dialogInflater.root
        val group = dialogInflater.group

        builder.setTitle("이미지 변경")
        builder.setMessage("사진을 가져올 방법을 선택해주세요.")
        builder.setView(dialogView)

        builder.setPositiveButton("확인") { dialog, _ ->
            when (group.checkedRadioButtonId) {
                R.id.radio_camera -> {
                    getUserImageFromCamera()
                }
                R.id.radio_gallery -> {
                    getUserImageFromGallery()
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // gallery를 통해 bitmap을 가져오는 method
    private fun galleryLauncher(callback: ModifyCallback) {
        reqGallery = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            try {
                val inputStream = context?.contentResolver?.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                callback.imageModify(bitmap)
                inputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("IntentReset")
    private fun getUserImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        reqGallery.launch(intent)
    }

    // camera를 통해 bitmap을 가져오는 method
    private fun cameraLauncher(callback: ModifyCallback) {
        reqCamera = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            try {
                val bitmap = BitmapFactory.decodeFile(filePath)
                callback.imageModify(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getUserImageFromCamera() {
        val timeStamp: String = SimpleDateFormat("yyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "JPEG_${timeStamp}",
            ".jpg",
            storageDir
        )
        filePath = file.absolutePath
        val photoUri: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.mvvmexample.fileprovider", file
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        reqCamera.launch(intent)
    }
}