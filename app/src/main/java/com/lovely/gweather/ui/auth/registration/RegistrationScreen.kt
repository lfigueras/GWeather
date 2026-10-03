package com.yourapp.weather.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lovely.gweather.MainActivity
import com.lovely.gweather.data.auth.CredentialHasher
import com.lovely.gweather.data.local.entity.UserEntity
import com.lovely.gweather.ui.auth.AuthValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onNavigateToSignIn: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isRegistering by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F2F0), Color(0xFFF8F6F0))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = Color(0xFF247A78), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.WbSunny, null, tint = Color.White, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("GWeather", color = Color(0xFF18333A), style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Text("LOCAL WEATHER", color = Color(0xFF61767A), style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(28.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text("Create account", color = Color(0xFF18333A), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Save your weather history on this device.", color = Color(0xFF61767A), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(18.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; nameError = null },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, "Name", tint = Color(0xFF247A78))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        isError = nameError != null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF247A78), focusedLabelColor = Color(0xFF247A78), cursorColor = Color(0xFF247A78))
                    )
                    nameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, "Email", tint = Color(0xFF247A78))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF247A78), focusedLabelColor = Color(0xFF247A78), cursorColor = Color(0xFF247A78))
                    )
                    emailError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; passwordError = null },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "Password", tint = Color(0xFF247A78))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        isError = passwordError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF247A78), focusedLabelColor = Color(0xFF247A78), cursorColor = Color(0xFF247A78))
                    )
                    passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmPasswordError = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "Confirm", tint = Color(0xFF247A78))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        isError = confirmPasswordError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF247A78), focusedLabelColor = Color(0xFF247A78), cursorColor = Color(0xFF247A78))
                    )
                    confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    Spacer(modifier = Modifier.height(18.dp))

                    // Register Button
                    Button(
                        onClick = {

                            nameError = AuthValidator.validateName(name)
                            emailError = AuthValidator.validateEmail(email)
                            passwordError = AuthValidator.validatePassword(password)
                            confirmPasswordError = AuthValidator.validateConfirmPassword(password, confirmPassword)


                            val isValid = nameError == null && emailError == null && passwordError == null && confirmPasswordError == null

                            if (isValid) {
                                if (isRegistering) return@Button
                                scope.launch {
                                    isRegistering = true
                                    try {
                                        val normalizedEmail = email.trim().lowercase()
                                        val userDao = MainActivity.Database.getInstance(context).userDao()
                                        if (userDao.getUserByEmail(normalizedEmail) != null) {
                                            emailError = "An account with this email already exists"
                                            return@launch
                                        }
                                        val credential = withContext(Dispatchers.Default) {
                                            CredentialHasher.hash(password)
                                        }
                                        userDao.insertAll(
                                            UserEntity(
                                                fullName = name.trim(),
                                                emailAddress = normalizedEmail,
                                                passwordHash = credential.hash,
                                                passwordSalt = credential.salt
                                            )
                                        )
                                        onNavigateToSignIn()
                                    } catch (exception: Exception) {
                                        Toast.makeText(context, "Account creation failed. Please try again.", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isRegistering = false
                                    }
                                }
                            }
                        },
                        enabled = !isRegistering,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF247A78)
                        )
                    ) {
                        Text(if (isRegistering) "Creating Account" else "Create Account")
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already registered?", color = Color(0xFF61767A), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onNavigateToSignIn) {
                            Text("Sign in", color = Color(0xFF247A78))
                        }
                    }
                }
            }
        }
    }
}