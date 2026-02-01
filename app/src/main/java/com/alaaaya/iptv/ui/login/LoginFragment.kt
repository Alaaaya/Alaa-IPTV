package com.alaaaya.iptv.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.alaaaya.iptv.MainActivity
import com.alaaaya.iptv.R
import com.alaaaya.iptv.data.local.AppDatabase
import com.alaaaya.iptv.data.repository.UserRepository
import com.alaaaya.iptv.ui.main.MainFragment

class LoginFragment : Fragment() {
    
    private lateinit var viewModel: LoginViewModel
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var serverUrlEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val database = AppDatabase.getInstance(requireContext())
        val userRepository = UserRepository(database.userDao())
        viewModel = LoginViewModel(userRepository)
        
        // Initialize views
        usernameEditText = view.findViewById(R.id.username_input)
        passwordEditText = view.findViewById(R.id.password_input)
        serverUrlEditText = view.findViewById(R.id.server_url_input)
        loginButton = view.findViewById(R.id.login_button)
        progressBar = view.findViewById(R.id.progress_bar)
        errorTextView = view.findViewById(R.id.error_text)
        
        // Set up click listener
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val serverUrl = serverUrlEditText.text.toString()
            
            viewModel.login(username, password, serverUrl)
        }
        
        // Observe login state
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    loginButton.isEnabled = false
                    errorTextView.visibility = View.GONE
                }
                is LoginViewModel.LoginState.Success -> {
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                    Toast.makeText(requireContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    
                    // Navigate to main screen
                    (activity as? MainActivity)?.navigateToFragment(MainFragment())
                }
                is LoginViewModel.LoginState.Error -> {
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                    errorTextView.text = state.message
                    errorTextView.visibility = View.VISIBLE
                }
                else -> {
                    progressBar.visibility = View.GONE
                    loginButton.isEnabled = true
                    errorTextView.visibility = View.GONE
                }
            }
        }
    }
}
