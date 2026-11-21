package com.porua.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porua.app.R
import com.porua.app.data.repository.AuthRepository
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeMd
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailOrPhoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val errContactEmpty = stringResource(R.string.err_contact_empty)
    val errPasswordEmpty = stringResource(R.string.err_password_empty)
    val errLoginFailed = stringResource(R.string.err_login_failed)

    fun validate(): Boolean {
        var valid = true

        val contact = emailOrPhone.trim()
        if (contact.isBlank()) {
            emailOrPhoneError = errContactEmpty
            valid = false
        } else {
            emailOrPhoneError = null
        }

        if (password.isBlank()) {
            passwordError = errPasswordEmpty
            valid = false
        } else {
            passwordError = null
        }

        return valid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back button
        Row(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surface2)
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.text,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Screen Heading
        Text(
            text = stringResource(R.string.login_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.login_subtitle),
            fontSize = 14.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        // General Error Banner
        if (generalError != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ShapeMd)
                    .background(colors.dangerSoft)
                    .border(1.dp, colors.danger.copy(alpha = 0.5f), ShapeMd)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = colors.danger,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = generalError ?: "",
                    fontSize = 13.sp,
                    color = colors.danger,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 1. Email or Phone Input
        Text(
            text = stringResource(R.string.label_email_or_phone),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = {
                emailOrPhone = it
                if (emailOrPhoneError != null) emailOrPhoneError = null
            },
            placeholder = { Text(stringResource(R.string.placeholder_login_contact), color = colors.textMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = if (emailOrPhoneError != null) colors.danger else colors.textMuted
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = emailOrPhoneError != null,
            supportingText = emailOrPhoneError?.let { { Text(it, color = colors.danger, fontSize = 12.sp) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeMd,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                errorBorderColor = colors.danger
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Password Input
        Text(
            text = stringResource(R.string.label_password),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) passwordError = null
            },
            placeholder = { Text(stringResource(R.string.placeholder_login_password), color = colors.textMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (passwordError != null) colors.danger else colors.textMuted
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = colors.textMuted
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it, color = colors.danger, fontSize = 12.sp) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = ShapeMd,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedTextColor = colors.text,
                unfocusedTextColor = colors.text,
                errorBorderColor = colors.danger
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Button
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colors.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            PoruaPrimaryButton(
                text = stringResource(R.string.btn_login),
                onClick = {
                    if (validate()) {
                        isLoading = true
                        generalError = null
                        coroutineScope.launch {
                            val result = authRepository.login(
                                emailOrPhone = emailOrPhone,
                                password = password
                            )
                            isLoading = false
                            result.onSuccess {
                                onLoginSuccess()
                            }.onFailure { err ->
                                generalError = err.message ?: errLoginFailed
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom link to Sign Up
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.no_account_prompt),
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Text(
                text = stringResource(R.string.sign_up_action),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
