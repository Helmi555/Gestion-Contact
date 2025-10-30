package helmi.benabdelghani.gestioncontactjc.ui.screens

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
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignUpScreen(
    onSignUpSuccess: (Int) -> Unit,
    onLoginClick:()->Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var signUpError by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
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
            Text(
                text = "Create Account",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Join Contact Manager today",
                fontSize = 16.sp,
                color = Color(0xB3FFFFFF),
                modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name", color = Color(0xB3FFFFFF)) },
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

            if (signUpError.isNotBlank()) {
                Text(
                    text = signUpError,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Button(
                onClick = {
                    keyboardController?.hide()
                    if (username.isBlank() || name.isBlank() || password.isBlank()) {
                        signUpError = "Please fill all the fields"
                        return@Button
                    }
                    scope.launch {
                        val db = AppDatabase.getDatabase(context)
                        val usernameExists = db.userDao().userExists(username)
                        if (!usernameExists) {
                            val newUser = User(username = username, password = password, name = name)
                            db.userDao().insert(newUser)
                            val insertedUser = db.userDao().getUserByUsername(username) // get full user with ID
                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "✅ Account created!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            if (insertedUser != null) {
                                onSignUpSuccess(insertedUser.id)
                            } else {
                                signUpError = "Failed to create user"
                            }

                        } else {
                            signUpError = "Username already exists"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp, vertical = 20.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sign Up",
                        color = Color(0xFF1E88E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Already have an account? ", color = Color(0xB3FFFFFF), fontSize = 15.sp)
                Text(
                    "Login",
                    color = Color.White,
                    modifier = Modifier.clickable { onLoginClick() },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}