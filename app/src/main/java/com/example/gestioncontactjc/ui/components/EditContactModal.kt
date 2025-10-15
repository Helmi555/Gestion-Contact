package com.example.gestioncontactjc.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestioncontactjc.data.model.Contact

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditContactModal(
    contact: Contact,
    onSave: (Contact) -> Unit,
    onDismiss: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val modalHeight = screenHeight * 0.60f

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(260)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(320)
                ) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(320)),
        exit = fadeOut(animationSpec = tween(200)) +
                slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = tween(220)
                ) +
                scaleOut(targetScale = 0.98f, animationSpec = tween(220))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            // Dark translucent overlay behind the modal (click to dismiss)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        onClick = onDismiss,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )

            // Modal (no blur)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(modalHeight)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF1976D2))
                        )
                    )
                    .padding(20.dp)
            ) {
                var nom by remember { mutableStateOf(TextFieldValue(contact.nom)) }
                var pseudo by remember { mutableStateOf(TextFieldValue(contact.pseudo)) }
                var phone by remember { mutableStateOf(TextFieldValue(contact.phoneNumber)) }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Edit Contact",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    @Composable
                    fun field(label: String, value: TextFieldValue, onChange: (TextFieldValue) -> Unit) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = onChange,
                            label = { Text(label, color = Color.White) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.06f),
                                    RoundedCornerShape(12.dp)

                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                                cursorColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    field("Name", nom) { nom = it }
                    field("Pseudo", pseudo) { pseudo = it }
                    field("Phone Number", phone) { phone = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.18f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancel") }

                        Button(
                            onClick = {
                                onSave(contact.copy(nom = nom.text, pseudo = pseudo.text, phoneNumber = phone.text))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4AB3EB),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}
