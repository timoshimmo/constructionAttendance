package app.com.android.mobiletrac.construction.ui.register

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.com.android.mobiletrac.construction.R

class RegisterViewModel() : ViewModel() {

    private val _registerForm = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerForm

    // A placeholder fullname validation check
    private fun isNameValid(name: String): Boolean {
        return name.length > 4
    }

    // A placeholder mobile validation check
    private fun isMobileValid(mobile: String): Boolean {
        return Patterns.PHONE.matcher(mobile).matches()
    }

    // A placeholder email validation check
    private fun isEmailValid(email: String): Boolean {
        return if (email.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    fun registerDataChanged(name: String, email: String, mobile: String, password: String) {
        if (!isNameValid(name)) {
            _registerForm.value = RegisterFormState(nameError = R.string.invalid_name)
        } else if (!isEmailValid(email)) {
            _registerForm.value = RegisterFormState(emailError = R.string.invalid_username)
        } else if (!isMobileValid(mobile)) {
            _registerForm.value = RegisterFormState(mobileError = R.string.invalid_mobile)
        }
        else if (!isPasswordValid(password)) {
            _registerForm.value = RegisterFormState(passwordError = R.string.invalid_password)
        } else {
            _registerForm.value = RegisterFormState(isDataValid = true)
        }
    }
}