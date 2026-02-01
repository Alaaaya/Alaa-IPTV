package com.alaaaya.iptv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.alaaaya.iptv.data.local.AppDatabase
import com.alaaaya.iptv.ui.login.LoginFragment

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize database
        AppDatabase.getInstance(applicationContext)
        
        if (savedInstanceState == null) {
            // Start with login fragment
            navigateToFragment(LoginFragment())
        }
    }
    
    fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
