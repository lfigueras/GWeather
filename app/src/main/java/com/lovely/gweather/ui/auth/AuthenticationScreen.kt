package com.yourapp.weather.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lovely.gweather.MainActivity
import com.lovely.gweather.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AuthenticationScreen(
    onNavigateToRegistration: () -> Unit,
    onSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showPrompt by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF22D3EE), // cyan-400
                        Color(0xFF3B82F6), // blue-500
                        Color(0xFF4F46E5)  // indigo-600
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Weather Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Text(
                text = "Sign in to continue to GWeather",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, "Email")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, "Password")
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            GlobalScope.launch() {
                            val userEntity: UserEntity = MainActivity.Database.getInstance(context).userDao().findByName(email, password)

                            if (userEntity == null) {
                                //mali
                                showPrompt = true
                                Log.d("dave", "mali")
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Incorrect credentials", Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Log.d("dave", "tama")
                                //tama
                                scope.launch { onSignIn() }
                            }
                        }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF06B6D4)
                        )
                    ) {
                        Text("Sign In")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, "Next")
                    }
                    if (showPrompt) {
                        AlertDialog(
                            onDismissRequest = { showPrompt = false },
                            confirmButton = {
                                TextButton(onClick = { showPrompt = false }) {
                                    Text("OK")
                                }
                            },
                            title = {
                                Text("Account Not Found")
                            },
                            text = {
                                Text("We couldn’t find an account matching the details you provided. Please check your information or sign up for a new account.")
                            },
                            icon = {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )

                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ")
                        TextButton(onClick = onNavigateToRegistration) {
                            Text("Create Account")
                        }
                    }
                }
            }
        }
    }
}