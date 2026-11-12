package com.example.propertymanagementsystem.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.databinding.ActivityLoginBinding
import com.example.propertymanagementsystem.ui.main.MainActivity
import com.example.propertymanagementsystem.util.SessionManager
import com.example.propertymanagementsystem.util.Validation

/**
 * The first screen of the app.
 *
 * The user signs in here. If the details are correct the app moves on to the
 * main screen, otherwise a message is shown under the field that is wrong.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // If somebody is already signed in there is no reason to show this
        // screen again, so the app goes straight to the main screen.
        if (session.isSignedIn()) {
            goToMainScreen()
            return
        }

        binding.loginButton.setOnClickListener { attemptLogin() }
        binding.goToRegisterButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // Checks what was typed and either opens the app or shows the problem.
    private fun attemptLogin() {
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

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

        if (!session.hasAccount()) {
            Toast.makeText(this, R.string.no_account_yet, Toast.LENGTH_LONG).show()
            return
        }

        if (!session.login(email, password)) {
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_LONG).show()
            return
        }

        session.setSignedIn(email.trim())
        goToMainScreen()
    }

    private fun goToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
