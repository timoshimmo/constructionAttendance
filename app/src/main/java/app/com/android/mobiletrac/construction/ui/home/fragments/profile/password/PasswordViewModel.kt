package app.com.android.mobiletrac.construction.ui.home.fragments.profile.password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.com.android.mobiletrac.construction.R

class PasswordViewModel : ViewModel() {

    private val _passwordForm = MutableLiveData<PasswordFormState>()
    val passwordFormState: LiveData<PasswordFormState> = _passwordForm

    // A placeholder password validation check
    private fun isCurrentPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    // A placeholder password validation check
    private fun isNewPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    // A placeholder password validation check
    private fun isConfirmPasswordValid(newpass: String, confirm: String): Boolean {
       // var isConirmed = false

      //  if(newpass === confirm) isConirmed = true

        return newpass == confirm
    }

    fun registerDataChanged(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (!isCurrentPasswordValid(currentPassword)) {
            _passwordForm.value = PasswordFormState(currentPasswordError = R.string.invalid_password)
        } else if (!isNewPasswordValid(newPassword)) {
            _passwordForm.value = PasswordFormState(newPasswordError = R.string.invalid_password)
        } else if (!isConfirmPasswordValid(newPassword, confirmPassword)) {
            _passwordForm.value = PasswordFormState(confirmPasswordError = R.string.invalid_confirm_password)
        }
        else {
            _passwordForm.value = PasswordFormState(isDataValid = true)
        }
    }
}