package com.example.peertutoringapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.peertutoringapplication.data.datasource.DatabaseProvider
import com.example.peertutoringapplication.data.repository.UserRepository
import com.example.peertutoringapplication.databinding.ActivityRegisterBinding
import com.example.peertutoringapplication.viewmodel.UserViewModel
import com.example.peertutoringapplication.viewmodel.UserViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = UserRepository(database.userDao())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(repository)
        )[UserViewModel::class.java]

        setupPasswordVisibility()
        setupRegistrationButton()
        observeRegistrationMessage()

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupPasswordVisibility() {
        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->

            val cursorPosition = binding.etPassword.selectionStart

            binding.etPassword.transformationMethod =
                if (isChecked) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }

            binding.etPassword.setSelection(
                cursorPosition.coerceIn(0, binding.etPassword.text.length)
            )
        }
    }

    private fun setupRegistrationButton() {

        binding.btnRegister.setOnClickListener {

            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            val selectedRoleId = binding.rgRole.checkedRadioButtonId

            if (selectedRoleId == -1) {
                Toast.makeText(
                    this,
                    "Please select your role.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val role = when (selectedRoleId) {
                binding.rbTutor.id -> "Tutor"
                binding.rbTutee.id -> "Tutee"
                else -> ""
            }

            userViewModel.registerUser(
                firstName,
                lastName,
                email,
                password,
                role
            )
        }
    }

    private fun observeRegistrationMessage() {

        lifecycleScope.launch {
            userViewModel.registrationMessage.collect { message ->

                if (message != null) {
                    Toast.makeText(
                        this@RegisterActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
