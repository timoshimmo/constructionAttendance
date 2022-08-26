package app.com.android.mobiletrac.construction.ui.home.fragments.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name

    private val _email = MutableLiveData<String>("")
    val email: LiveData<String> = _email

    private val _mobileNo = MutableLiveData<String>("")
    val mobileNo: LiveData<String> = _mobileNo


    fun setName(name: String?) {
        _name.apply {
            value = name
        }
    }

    fun setEmail(email: String?) {
        _email.apply {
            value = email
        }
    }

    fun setMobileNo(mobileNo: String?) {
        _mobileNo.apply {
            value = mobileNo
        }
    }
    /*.apply {
                value = "This is profile Fragment" */

}