package com.example.wordnotes.ui.auth

import com.example.wordnotes.MainCoroutineRule
import com.example.wordnotes.R
import com.example.wordnotes.mocks.FakeUserRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SignUpViewModelTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var signUpViewModel: SignUpViewModel

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        userRepository = FakeUserRepository(testDispatcher = mainCoroutineRule.testDispatcher)
        signUpViewModel = SignUpViewModel(userRepository)
    }

    @Test
    fun `check initial state`() = runTest {
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.isLoading).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isNull()
    }

    @Test
    fun `enter correct information then signing up successfully`() {
        signUpViewModel.signUp(username = "user4", email = "user4@gmail.com", password = "444444", confirmedPassword = "444444")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isTrue()
    }

    @Test
    fun `sign up with an existing user then signing up failed`() {
        signUpViewModel.signUp(username = "user1", email = "user1@gmail.com", password = "111111", confirmedPassword = "111111")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
    }

    @Test
    fun `enter incomplete information then signing up failed and showing error message`() = runTest {
        signUpViewModel.signUp(username = "", email = "user4@gmail.com", password = "444444", confirmedPassword = "444444")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)

        signUpViewModel.signUp(username = "user4", email = "", password = "444444", confirmedPassword = "444444")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)

        signUpViewModel.signUp(username = "user4", email = "user4@gmail.com", password = "", confirmedPassword = "444444")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)

        signUpViewModel.signUp(username = "user4", email = "user4@gmail.com", password = "444444", confirmedPassword = "")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.please_complete_all_information)
    }

    @Test
    fun `enter wrong email format then signing up failed and showing error message`() = runTest {
        signUpViewModel.signUp(username = "user4", email = "user4@", password = "444444", confirmedPassword = "444444")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.the_email_address_isnt_in_the_correct_format)
    }

    @Test
    fun `enter password with length less than 6 then signing up failed and showing error message`() {
        signUpViewModel.signUp(username = "user4", email = "user4@gmail.com", password = "44444", confirmedPassword = "44444")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.password_must_be_at_least_6_characters)
    }

    @Test
    fun `enter password and confirm password don't matches then signing up failed and showing error message`() {
        signUpViewModel.signUp(username = "user4", email = "user4@gmail.com", password = "444444", confirmedPassword = "4444445")
        assertThat(signUpViewModel.uiState.value.isSignUpSuccess).isFalse()
        assertThat(signUpViewModel.uiState.value.message).isEqualTo(R.string.confirm_password_didnt_match)
    }
}