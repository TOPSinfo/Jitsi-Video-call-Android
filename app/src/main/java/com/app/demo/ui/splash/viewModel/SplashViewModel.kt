package com.app.demo.ui.splash.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.demo.data.repository.UserRepository
import com.app.demo.network.NetworkHelper
import com.app.demo.network.Resource
import com.app.demo.utils.Constant
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkHelper: NetworkHelper
) : ViewModel() {


    private val _userDataResponse: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val userDataResponse: LiveData<Resource<Boolean>> get() = _userDataResponse

    /**
     * Used to check user is register in application or not
     */
    fun checkUserRegisterOrNot(userId: String) = viewModelScope.launch {
        if (networkHelper.isNetworkConnected()) {
            _userDataResponse.value = Resource.loading(null)
            val docIdRef: DocumentReference = userRepository.getUserProfileRepository(userId)

            docIdRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot? = task.result
                    if (document?.exists() == true) {
                        _userDataResponse.postValue(
                            Resource.success(
                                true,
                            )
                        )

                    } else {
                        _userDataResponse.postValue(
                            Resource.success(
                                false,
                            )
                        )
                    }
                } else {
                    _userDataResponse.postValue(
                        Resource.error(
                            Constant.validation_error,
                            null
                        )
                    )
                }
            }
        } else _userDataResponse.postValue(
            Resource.error(
                Constant.MSG_NO_INTERNET_CONNECTION,
                null
            )
        )
    }
}