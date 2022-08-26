package app.com.android.mobiletrac.construction.ui.home.fragments.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _status = MutableLiveData<String>("-")
    val status: LiveData<String> = _status

    /****
     *
     * Setter for  pickup details model parameters
     *
     */
    fun setStatus(status: String) {
        _status.value = status

    }


}