package com.alaaaya.iptv.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alaaaya.iptv.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    fun login(username: String, password: String, serverUrl: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            // Validate inputs
            if (username.isBlank() || password.isBlank() || serverUrl.isBlank()) {
                _loginState.value = LoginState.Error("Please fill in all fields")
                return@launch
            }
            
            // Attempt login
            val result = userRepository.login(username, password, serverUrl)
            
            if (result.isSuccess) {
                _loginState.value = LoginState.Success
            } else {
                _loginState.value = LoginState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }
    
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
