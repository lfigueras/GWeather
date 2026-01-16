package com.lovely.gweather.ui.auth

import androidx.compose.foundation.Image
import com.lovely.gweather.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lovely.gweather.data.model.UserCredentials


@Preview(showBackground = true)
@Composable
fun AuthenticationScreen() {
    val viewModel: AuthViewModel = viewModel()

    val gradientColors = listOf(Color(0xFF87CEEB), Color(0xFFEEEEEE), Color(0xFF40E0D0))

    // Create a vertical linear gradient brush
    val brush = Brush.verticalGradient(colors = gradientColors)

    Box(modifier = Modifier.background(brush = brush)){

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                // Use R.drawable to point to your PNG file
                painter = painterResource(id = R.drawable.meteorology),
                contentDescription = "Weather App Logo",
                // Correct syntax: modifier = Modifier.size()
                modifier = Modifier.size(120.dp)
                    .padding(bottom = 10.dp)
            )
            Text(text = "Sign In", fontSize = 30.sp, fontWeight = FontWeight.Bold);
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            OutlinedTextField(
                value = email,
                onValueChange = {email = it},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },

                )
            OutlinedTextField(
                value = password,
                onValueChange = {password = it},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
            )
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(onClick = { },
                    modifier = Modifier.padding(top = 16.dp)

                    , shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBDDDE4),      // Background color
                        contentColor = Color(color = 0xFF154D71),       // Text/Icon color
                        disabledContainerColor = Color.Gray, // Background when disabled
                        disabledContentColor = Color.LightGray // Text color when disabled
                    )
                ) {
                    Text("Register Now")
                }
                Spacer(modifier = Modifier.width(8.dp)) // Add your desired small space here
                FilledTonalButton(onClick = {
                    // 1. Create the credentials object from the state variables
                    val credentials = UserCredentials(email = email, password = password)

                    // 2. Call the login function
                    val isSuccess = viewModel.login(credentials)

                    // 3. Handle the result
                    if (isSuccess) {
                        // Navigate to home screen (we can set up navigation next)
                        println("Login Successful!")
                    } else {
                        // Show an error (e.g., a Toast or a red error message)
                        println("Login Failed: Invalid credentials")
                    }
                },
                    modifier = Modifier.padding(top = 16.dp)

                    , shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF154D71),      // Background color
                        contentColor = Color.White,       // Text/Icon color
                        disabledContainerColor = Color.Gray, // Background when disabled
                        disabledContentColor = Color.LightGray // Text color when disabled
                    )
                ) {
                    Text("Sign In")
                }
            }


        }
    }




}

