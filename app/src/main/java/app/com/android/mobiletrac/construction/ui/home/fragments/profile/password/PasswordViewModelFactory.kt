package app.com.android.mobiletrac.construction.ui.home.fragments.profile.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class PasswordViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            return PasswordViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}