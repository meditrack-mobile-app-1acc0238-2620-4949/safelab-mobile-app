package pe.edu.upc.safelab.boundedcontext.identityaccess.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pe.edu.upc.safelab.boundedcontext.identityaccess.domain.model.AuthenticatedUser

@Composable
fun IdentityAccessEntryScreen(
    onAuthenticated: (AuthenticatedUser) -> Unit,
    modifier: Modifier = Modifier
) {
    LoginScreen(
        onAuthenticated = onAuthenticated,
        modifier = modifier
    )
}
