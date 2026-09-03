package com.hdtchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hdtchat.data.User
import com.hdtchat.data.UserViewModel
import com.hdtchat.data.Userdatabase
import com.hdtchat.databinding.ActivitySignupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.security.MessageDigest




class Signup : AppCompatActivity() {

    private lateinit var binding : ActivitySignupBinding
    private lateinit var appDb : Userdatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDb = Userdatabase.getDatabase(this)

        binding.btnSignUp.setOnClickListener {

            writeData()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

    }

    private fun writeData() {

        val username = binding.edtUsername.text.toString()
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()
        val validEmail = email.trim()

        val viewModel: UserViewModel by viewModels()

        if (username.isNotBlank() && email.isNotEmpty() && password.isNotEmpty()) {
            //Sjekker om en gyldig email addresse som har blitt skrevet.
            if (!Patterns.EMAIL_ADDRESS.matcher(validEmail).matches()) {
                Toast.makeText(this@Signup, "Type in a valid email address", Toast.LENGTH_SHORT).show()

            }
            else if (Patterns.EMAIL_ADDRESS.matcher(validEmail).matches()) {
                val shaPassword = hashPassword(password)
                val user = User(null, username, email, shaPassword)

                viewModel.addUser(user)

                binding.edtUsername.text.clear()
                binding.edtEmail.text.clear()
                binding.edtPassword.text.clear()

                Toast.makeText(this@Signup, "Velykket Registrering", Toast.LENGTH_SHORT).show()
                //Hvis alt info er fullt ut riktig, så går den videre.
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }
        else {
            Toast.makeText(this@Signup, "Vennligst fyll ut alle kolonner", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedWord = md.digest(password.toByteArray(StandardCharsets.UTF_8))
        return hashedWord.joinToString("") { "%02x".format(it) }
    }
}


