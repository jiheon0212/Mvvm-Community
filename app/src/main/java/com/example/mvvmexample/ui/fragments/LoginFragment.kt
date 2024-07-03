package com.example.mvvmexample.ui.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.mvvmexample.R
import com.example.mvvmexample.databinding.FragmentLoginBinding
import com.example.mvvmexample.ui.viewmodel.FirebaseAuthViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: FirebaseAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.googleLoginBtn.setOnClickListener {
            startLoginWithGoogle()
        }
        viewModel.userData.observe(viewLifecycleOwner) {
            if (it != null) {
                navigate()
            } else {
                Toast.makeText(context, "Login failed please do it again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // google login에 필요한 manager, option, request를 담는 method
    private fun startLoginWithGoogle() {
        val credentialManager = CredentialManager.create(requireActivity().applicationContext)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setServerClientId(getString(R.string.firebase_auth_client_id))
            .build()

        val request: GetCredentialRequest =
            GetCredentialRequest.Builder().addCredentialOption(
                googleIdOption
            ).build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = requireContext(),
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "coroutine scope has an error" , e)
            }
        }
    }

    // google credential login handler method <공식문서 참조>
    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            // GoogleIdToken credential
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        // viewModel을 호출해 firebase user에 등록시킨 뒤 observe로 해당 유저가 null이 아닐 시 main 진입
                        viewModel.getFirebaseUser(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized custom credential type here.
                    Log.e(TAG, "Unexpected custom type of credential")
                }
            }
            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    // navcontroller, fcv들의 view를 관리해주는 method
    private fun navigate() {
        activity?.findViewById<FragmentContainerView>(R.id.main_container)?.visibility = View.VISIBLE
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.VISIBLE
        activity?.findViewById<FragmentContainerView>(R.id.login_container)?.visibility = View.GONE

        val navController = (activity?.supportFragmentManager?.findFragmentById(R.id.main_container) as NavHostFragment).navController
        navController.navigate(R.id.freeChatFragment)
    }
}