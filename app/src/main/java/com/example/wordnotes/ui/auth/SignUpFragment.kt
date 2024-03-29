package com.example.wordnotes.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.wordnotes.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val signUpViewModel: SignUpViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        observeUiState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setListeners() {
        binding.buttonSignUp.setOnClickListener {
            signUpViewModel.signUp(
                username = binding.inputUsername.text.toString(),
                email = binding.inputEmail.text.toString(),
                password = binding.inputPassword.text.toString(),
                confirmedPassword = binding.inputConfirmedPassword.text.toString()
            )
        }

        binding.textSignIn.setOnClickListener {
            popBackToSignInFragment()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.uiState.collect { uiState ->
                    if (uiState.isSignUpSuccess) {
                        popBackToSignInFragment()
                    }
                    uiState.message?.let {
                        showSnackBar(it)
                    }
                    toggleCircularLoading(uiState.isLoading)
                }
            }
        }
    }

    /**
     * Pop back to SignInFragment and clear EditTexts
     */
    private fun popBackToSignInFragment() {
        findNavController().apply {
            previousBackStackEntry?.savedStateHandle?.set(SignInFragment.IS_CLEAR, true)
            popBackStack()
        }
    }

    private fun toggleCircularLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSignUp.visibility = View.INVISIBLE
        } else {
            binding.buttonSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    private fun showSnackBar(@StringRes messageResId: Int) {
        Snackbar.make(requireView(), messageResId, Snackbar.LENGTH_SHORT).show()
        signUpViewModel.snakeBarShown()
    }
}