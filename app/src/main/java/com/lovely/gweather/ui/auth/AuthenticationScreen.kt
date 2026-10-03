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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.lovely.gweather.MainActivity
import com.lovely.gweather.data.auth.CredentialHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AuthenticationScreen(
    onNavigateToRegistration: () -> Unit,
    onSignIn: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showPrompt by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = Color(0xFF247A78), shape = RoundedCornerShape(8.dp)) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
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
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Welcome back", color = Color(0xFF18333A), style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Sign in to check your local weather.", color = Color(0xFF61767A), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF247A78)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF247A78), focusedLabelColor = Color(0xFF247A78), cursorColor = Color(0xFF247A78))
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF247A78)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF247A78), focusedLabelColor = Color(0xFF247A78), cursorColor = Color(0xFF247A78))
                    )

                    Spacer(Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (isSigningIn) return@Button
                            scope.launch {
                                isSigningIn = true
                                try {
                                    val normalizedEmail = email.trim().lowercase()
                                    val user = MainActivity.Database.getInstance(context)
                                        .userDao()
                                        .getUserByEmail(normalizedEmail)
                                    val passwordMatches = user?.passwordHash?.let { hash ->
                                        withContext(Dispatchers.Default) {
                                            CredentialHasher.verify(password, hash, user.passwordSalt)
                                        }
                                    } ?: false

                                    if (passwordMatches) {
                                        onSignIn(normalizedEmail)
                                    } else {
                                        showPrompt = true
                                    }
                                } catch (exception: Exception) {
                                    Toast.makeText(context, "Sign in failed. Please try again.", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSigningIn = false
                                }
                            }
                        },
                        enabled = !isSigningIn,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF247A78))
                    ) {
                        Text(if (isSigningIn) "Signing in..." else "Sign in")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("New to GWeather?", color = Color(0xFF61767A), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onNavigateToRegistration) {
                            Text("Create account", color = Color(0xFF247A78))
                        }
                    }
                }
            }
        }
    }

    if (showPrompt) {
        AlertDialog(
            onDismissRequest = { showPrompt = false },
            confirmButton = {
                TextButton(onClick = { showPrompt = false }) { Text("OK") }
            },
            title = { Text("Sign-in failed") },
            text = { Text("Check your email and password, or create a new account.") }
        )
    }
}