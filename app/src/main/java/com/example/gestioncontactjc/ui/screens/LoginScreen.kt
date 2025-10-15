package com.example.gestioncontactjc.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestioncontactjc.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onLoginSuccess: (Int,Boolean) -> Unit,
    onSignUpClick:()->Unit,
    modifier: Modifier = Modifier
)
{
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)) // deep blue gradient
                )
            )
            .clickable { focusManager.clearFocus() }
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = -100.dp, y = -100.dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 280.dp, y = 500.dp)
                .background(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = CircleShape
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title – white, bold, large
            Text(
                text = "Contact Manager",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Subtitle
            Text(
                text = "Secure. Simple. Organized.",
                fontSize = 16.sp,
                color = Color(0xB3FFFFFF), // 70% white
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color(0xB3FFFFFF)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier.padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0x80FFFFFF),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color(0xB3FFFFFF)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0x80FFFFFF),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color(0xB3FFFFFF)
                )
            )

            if (loginError.isNotEmpty()) {
                Text(
                    text = loginError,
                    color = Color(0xFFF55E5E), // soft red
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Remember Me
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, start = 40.dp)
                    .clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    modifier = Modifier.size(20.dp).padding(end = 18.dp),

                )
                Text("Remember me", color = Color.White, fontSize = 15.sp)
            }

            // Login Button
            Button(
                onClick = {
                    keyboardController?.hide()
                    if(username.isBlank() || password.isBlank()){
                        loginError="Please enter username and password"
                        return@Button
                    }
                    scope.launch {
                        val db = AppDatabase.getDatabase(context)
                        val user = db.userDao().login(username, password)
                        if (user != null) {

                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "✅ Login successful", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(user.id,rememberMe)}
                        } else {
                            val userNameExists = db.userDao().userExists(username)
                            loginError = if (userNameExists) {
                                "Invalid password"
                            } else {
                                "User not found"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 20.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    "Login",
                    color = Color(0xFF1E88E5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            // Sign Up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("New here? ", color = Color(0xB3FFFFFF), fontSize = 15.sp)
                Text(
                    "Create Account",
                    color = Color.White,
                    modifier = Modifier.clickable { onSignUpClick() },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exit
            Text(
                text = "Exit App",
                color = Color(0xB3030908),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable {
                        (context as? androidx.activity.ComponentActivity)?.finishAffinity()
                    }
            )
        }
    }
}