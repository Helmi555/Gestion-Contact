package helmi.benabdelghani.gestioncontactjc.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import helmi.benabdelghani.gestioncontactjc.data.database.AppDatabase
import helmi.benabdelghani.gestioncontactjc.data.model.Contact
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SuspiciousIndentation")
@Composable
fun AddContactScreen(
    userId: Int = 0,
    navController: NavController? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nom by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current



        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header houni
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                IconButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "New Contact",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Text(
                text = "*All fields are required",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Yellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PremiumTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = "Full Name",
                        placeholder = "Enter the full name",
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) }
                    )

                    PremiumTextField(
                        value = pseudo,
                        onValueChange = { pseudo = it },
                        label = "Nickname",
                        placeholder = "Enter a nickname"
                    )

                    PremiumTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Phone Number",
                        placeholder = "+216 XX XXX XXXX",
                        keyboardType = KeyboardType.Phone,
                        icon = { Icon(Icons.Default.Call, contentDescription = null, tint = Color.White) }

                    )

                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD80A0A),
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center,
                        )
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            errorMessage = ""
                            when {
                                nom.isBlank() -> {
                                    errorMessage = "Please enter a name"
                                }
                                pseudo.isBlank() -> {
                                    errorMessage = "Please enter a nickname"
                                }
                                (phoneNumber.isBlank() || !phoneNumber.matches("\\d+".toRegex())) -> {
                                    errorMessage = "Please enter a valid phone number"
                                }
                                else -> {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val db = AppDatabase.getDatabase(context)
                                            val contact = Contact(
                                                userId = userId,
                                                nom = nom,
                                                pseudo = pseudo,
                                                phoneNumber = phoneNumber
                                            )
                                            db.contactDao().insert(contact)
                                            nom = ""
                                            pseudo = ""
                                            phoneNumber = ""
                                            withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                navController?.popBackStack()
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = if (e.message?.contains("UNIQUE constraint failed") == true) {
                                                "This phone number already exists."
                                            } else {
                                                "Error adding contact"
                                            }
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF42A5F5),
                            disabledContainerColor = Color(0xFF42A5F5).copy(alpha = 0.6f)
                        ),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Adding..." else "Add Contact",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
}



@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    icon: @Composable (() -> Unit)? = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White) },
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.5f)) },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = Color.White,
            fontSize = 16.sp
        ),
        leadingIcon = icon
    )
}