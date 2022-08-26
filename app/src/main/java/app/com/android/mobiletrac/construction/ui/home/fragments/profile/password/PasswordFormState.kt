package app.com.android.mobiletrac.construction.ui.home.fragments.profile.password

data class PasswordFormState (val currentPasswordError: Int? = null,
                              val newPasswordError: Int? = null,
                              val confirmPasswordError: Int? = null,
                              val isDataValid: Boolean = false)