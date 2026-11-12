package com.example.propertymanagementsystem.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.databinding.ActivityRegisterBinding
import com.example.propertymanagementsystem.ui.main.MainActivity
import com.example.propertymanagementsystem.util.SessionManager
import com.example.propertymanagementsystem.util.Validation

/**
 * Creates the account that is used to sign in.
 *
 * The details are checked one by one and a message is shown under the field
 * that has a problem. When everything is fine the account is saved and the
 * app opens the main screen straight away.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.registerButton.setOnClickListener { attemptRegister() }
        binding.goToLoginButton.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val name = binding.nameInput.text.toString()
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()
        val confirmPassword = binding.confirmPasswordInput.text.toString()

        if (Validation.isEmpty(name)) {
            binding.nameLayout.error = getString(R.string.name_empty)
            return
        }
        binding.nameLayout.error = null

        if (Validation.isEmpty(email)) {
            binding.emailLayout.error = getString(R.string.email_empty)
            return
        }
        binding.emailLayout.error = null

        if (!Validation.isValidEmail(email)) {
            binding.emailLayout.error = getString(R.string.email_invalid)
            return
        }

        if (Validation.isEmpty(password)) {
            binding.passwordLayout.error = getString(R.string.password_empty)
            return
        }
        binding.passwordLayout.error = null

        if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.password_short)
            return
        }

        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.password_mismatch)
            return
        }
        binding.confirmPasswordLayout.error = null

        session.register(email, password)
        session.setSignedIn(email.trim())

        Toast.makeText(this, R.string.account_created, Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
