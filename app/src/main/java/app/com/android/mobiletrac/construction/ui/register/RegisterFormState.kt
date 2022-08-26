package app.com.android.mobiletrac.construction.ui.register

data class RegisterFormState (val nameError: Int? = null,
                              val emailError: Int? = null,
                              val mobileError: Int? = null,
                              val passwordError: Int? = null,
                              val isDataValid: Boolean = false)