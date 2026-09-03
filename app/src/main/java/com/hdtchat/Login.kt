package com.hdtchat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.hdtchat.data.Dao
import com.hdtchat.data.UserViewModel
import com.hdtchat.data.Userdatabase
import com.hdtchat.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var appDb: Userdatabase
    private lateinit var userDao: Dao
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDb = Userdatabase.getDatabase(this)
        userDao = appDb.dao() //Henter ut Dao objektet
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            readData()
        }

        //Sjekker om brukeren allerede er innlogget.
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {
            val userName = sharedPreferences.getString("username","")
            val password = sharedPreferences.getString("password", "")
            if (!userName.isNullOrEmpty() && !password.isNullOrEmpty()) {

                val viewModel: UserViewModel by viewModels()
                viewModel.verifyUser(userName, password).observe(this) { result ->
                    if (result) {
                        Toast.makeText(this@Login, "Login Success", Toast.LENGTH_SHORT).show()
                        val intent1 = Intent(this@Login, Home::class.java)
                        finish()
                        startActivity(intent1)
                    }
                }
            }
        }
    }

    private fun readData() {

        val password = binding.edtPassword.text.toString()
        val username = binding.edtUsername.text.toString()
  
        if (username.isNotBlank() && password.isNotBlank()) {

            val viewModel: UserViewModel by viewModels()
            viewModel.verifyUser(username, password).observe(this) { result ->
                if (result) {
                    Toast.makeText(this@Login, "Login Success", Toast.LENGTH_SHORT).show()
                    //Lagrer påloggingsinformasjon til SharedPreferences
                    val editor = sharedPreferences?.edit()
                    editor?.putBoolean("isLoggedIn", true)

                    editor?.putString("password", password)
                    editor?.putString("username",username)
                    editor?.apply()

                    val intent1 = Intent(this@Login, Home::class.java)
                    finish()
                    startActivity(intent1)
                    binding.edtPassword.text.clear()
                    binding.edtUsername.text.clear()
                } else {
                    Toast.makeText(this@Login, "Login Failed", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

}
