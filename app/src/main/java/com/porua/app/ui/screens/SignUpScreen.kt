package com.porua.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Person
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
import com.porua.app.ui.components.PoruaFilterChip
import com.porua.app.ui.components.PoruaPrimaryButton
import com.porua.app.ui.theme.LocalPoruaColors
import com.porua.app.ui.theme.ShapeMd
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignUpScreen(
    authRepository: AuthRepository,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalPoruaColors.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf("Science") }
    val selectedClass = "HSC"

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailOrPhoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var groupError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val groups = listOf("Science", "Arts", "Commerce")

    val errNameEmpty = stringResource(R.string.err_name_empty)
    val errContactEmpty = stringResource(R.string.err_contact_empty)
    val errEmailInvalid = stringResource(R.string.err_email_invalid)
    val errPhoneInvalid = stringResource(R.string.err_phone_invalid)
    val errPasswordEmpty = stringResource(R.string.err_password_empty)
    val errPasswordShort = stringResource(R.string.err_password_short)
    val errGroupEmpty = stringResource(R.string.err_group_empty)
    val errClassEmpty = stringResource(R.string.err_class_empty)
    val errSignupFailed = stringResource(R.string.err_signup_failed)

    fun validate(): Boolean {
        var valid = true

        if (name.trim().isBlank()) {
            nameError = errNameEmpty
            valid = false
        } else {
            nameError = null
        }

        val contact = emailOrPhone.trim()
        if (contact.isBlank()) {
            emailOrPhoneError = errContactEmpty
            valid = false
        } else if (contact.contains("@")) {
            val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (!emailPattern.matches(contact)) {
                emailOrPhoneError = errEmailInvalid
                valid = false
            } else {
                emailOrPhoneError = null
            }
        } else {
            val cleanPhone = contact.replace(" ", "").replace("-", "")
            val phonePattern = Regex("^(\\+?8801|01)[3-9]\\d{8}$")
            if (!phonePattern.matches(cleanPhone) && cleanPhone.length < 11) {
                emailOrPhoneError = errPhoneInvalid
                valid = false
            } else {
                emailOrPhoneError = null
            }
        }

        if (password.isBlank()) {
            passwordError = errPasswordEmpty
            valid = false
        } else if (password.length < 6) {
            passwordError = errPasswordShort
            valid = false
        } else {
            passwordError = null
        }

        if (selectedGroup.isBlank()) {
            groupError = errGroupEmpty
            valid = false
        } else {
            groupError = null
        }

        if (selectedClass.isBlank()) {
            classError = errClassEmpty
            valid = false
        } else {
            classError = null
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

        Spacer(modifier = Modifier.height(20.dp))

        // Screen Heading
        Text(
            text = stringResource(R.string.signup_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.signup_subtitle),
            fontSize = 14.sp,
            color = colors.textMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

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
            Spacer(modifier = Modifier.height(18.dp))
        }

        // 1. Name Input
        Text(
            text = stringResource(R.string.label_full_name),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = null
            },
            placeholder = { Text(stringResource(R.string.placeholder_full_name), color = colors.textMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (nameError != null) colors.danger else colors.textMuted
                )
            },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it, color = colors.danger, fontSize = 12.sp) } },
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

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Email or Phone Input
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
            placeholder = { Text(stringResource(R.string.placeholder_email_or_phone), color = colors.textMuted) },
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

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Password Input
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
            placeholder = { Text(stringResource(R.string.placeholder_password), color = colors.textMuted) },
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

        Spacer(modifier = Modifier.height(20.dp))

        // 4. HSC Group Selection
        Text(
            text = stringResource(R.string.label_select_group),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groups.forEach { group ->
                PoruaFilterChip(
                    text = group,
                    isSelected = selectedGroup == group,
                    onClick = {
                        selectedGroup = group
                        if (groupError != null) groupError = null
                    }
                )
            }
        }
        if (groupError != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = groupError ?: "", color = colors.danger, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Target Exam Level: Fixed to HSC
        Text(
            text = "Target Exam",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.text
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PoruaFilterChip(
                text = "HSC",
                isSelected = true,
                onClick = { /* Fixed to HSC */ }
            )
        }

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
                text = stringResource(R.string.btn_create_account),
                onClick = {
                    if (validate()) {
                        isLoading = true
                        generalError = null
                        coroutineScope.launch {
                            val result = authRepository.signUp(
                                name = name,
                                emailOrPhone = emailOrPhone,
                                password = password,
                                group = selectedGroup,
                                hscClass = selectedClass
                            )
                            isLoading = false
                            result.onSuccess {
                                onSignUpSuccess()
                            }.onFailure { err ->
                                generalError = err.message ?: errSignupFailed
                            }
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom link to Login
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.already_have_account),
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Text(
                text = stringResource(R.string.log_in_action),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
