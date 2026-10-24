package de.paarfinanz.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import de.paarfinanz.shared.resources.Res
import de.paarfinanz.shared.resources.auth_action_google
import de.paarfinanz.shared.resources.auth_action_sign_in
import de.paarfinanz.shared.resources.auth_action_sign_up
import de.paarfinanz.shared.resources.auth_email
import de.paarfinanz.shared.resources.auth_forgot_password
import de.paarfinanz.shared.resources.auth_hide_password
import de.paarfinanz.shared.resources.auth_name
import de.paarfinanz.shared.resources.auth_or
import de.paarfinanz.shared.resources.auth_password
import de.paarfinanz.shared.resources.auth_password_hint
import de.paarfinanz.shared.resources.auth_show_password
import de.paarfinanz.shared.resources.auth_switch_to_sign_in
import de.paarfinanz.shared.resources.auth_switch_to_sign_up
import de.paarfinanz.shared.resources.auth_tagline
import de.paarfinanz.shared.resources.auth_title_sign_in
import de.paarfinanz.shared.resources.auth_title_sign_up
import de.paarfinanz.core.ui.platform.rememberGoogleSignIn
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthRoute(viewModel: AuthViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val googleSignIn = rememberGoogleSignIn(onError = viewModel::onGoogleError)

    AuthScreen(
        state = state,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::submit,
        onToggleMode = viewModel::toggleMode,
        onForgotPassword = viewModel::sendPasswordReset,
        onGoogleSignIn = googleSignIn,
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleSignIn: (() -> Unit)?,
) {
    val focusManager = LocalFocusManager.current
    val isSignUp = state.mode == AuthMode.SIGN_UP
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BrandHeader()

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(if (isSignUp) Res.string.auth_title_sign_up else Res.string.auth_title_sign_in),
                    style = MaterialTheme.typography.headlineSmall,
                )

                AnimatedVisibility(visible = isSignUp) {
                    OutlinedTextField(
                        value = state.displayName,
                        onValueChange = onDisplayNameChange,
                        label = { Text(stringResource(Res.string.auth_name)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(Res.string.auth_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(Res.string.auth_password)) },
                    supportingText = if (isSignUp) {
                        { Text(stringResource(Res.string.auth_password_hint)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = stringResource(
                                    if (passwordVisible) Res.string.auth_hide_password else Res.string.auth_show_password,
                                ),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onSubmit()
                    }),
                    modifier = Modifier.fillMaxWidth(),
                )

                state.errorRes?.let { MessageText(stringResource(it), isError = true) }
                state.infoRes?.let { MessageText(stringResource(it), isError = false) }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSubmit()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(stringResource(if (isSignUp) Res.string.auth_action_sign_up else Res.string.auth_action_sign_in))
                    }
                }

                if (!isSignUp) {
                    TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                        Text(stringResource(Res.string.auth_forgot_password))
                    }
                }

                if (onGoogleSignIn != null) {
                    OrDivider()
                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(stringResource(Res.string.auth_action_google))
                    }
                }

                TextButton(onClick = onToggleMode, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(if (isSignUp) Res.string.auth_switch_to_sign_in else Res.string.auth_switch_to_sign_up))
                }
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    val colors = PaarfinanzTheme.extendedColors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Zwei überlappende Kreise in den Partnerfarben
        Box(modifier = Modifier.size(width = 76.dp, height = 44.dp)) {
            Surface(shape = CircleShape, color = colors.partnerA, modifier = Modifier.size(44.dp)) {}
            Surface(
                shape = CircleShape,
                color = colors.partnerB,
                modifier = Modifier
                    .offset(x = 32.dp)
                    .size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        Text(text = "Paarfinanz", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = stringResource(Res.string.auth_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.auth_or),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MessageText(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth(),
    )
}
