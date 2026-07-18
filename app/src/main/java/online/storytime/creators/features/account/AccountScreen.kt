package online.storytime.creators.features.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.AccountPatchBody
import online.storytime.creators.core.model.CreatorUser
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.patch
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.ui.GradientButton
import online.storytime.creators.ui.Loadable
import online.storytime.creators.ui.STTextField
import online.storytime.creators.ui.SectionHeader

@Composable
fun AccountScreen() {
    val client = Session.api
    val auth = Session.auth
    val loader = online.storytime.creators.ui.rememberLoadable()
    val scope = rememberCoroutineScope()

    Loadable(
        controller = loader,
        loader = { client.get<CreatorUser>("/api/me").also { auth.applyProfile(it) } },
    ) { me ->
        var name by remember { mutableStateOf(me.name ?: "") }
        var headline by remember { mutableStateOf(me.headline ?: "") }
        var handle by remember { mutableStateOf(me.networkHandle ?: "") }
        var location by remember { mutableStateOf(me.location ?: "") }
        var website by remember { mutableStateOf(me.website ?: "") }
        var bio by remember { mutableStateOf(me.bio ?: "") }
        var email by remember { mutableStateOf(me.email ?: "") }
        var phone by remember { mutableStateOf(me.phoneNumber ?: "") }
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var saving by remember { mutableStateOf(false) }
        var message by remember { mutableStateOf<String?>(null) }
        var success by remember { mutableStateOf(false) }

        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.surface).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(72.dp).clip(CircleShape).background(STColor.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text((me.displayName.firstOrNull() ?: 'C').uppercase(), color = STColor.primary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(me.displayName, color = STColor.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    me.email?.let { Text(it, color = STColor.textSecondary, fontSize = 13.sp) }
                    me.reputationScore?.let { Text("Reputation ${it.toInt()}", color = STColor.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                }
            }

            Section("Public profile") {
                STTextField(name, { name = it }, "Display name")
                STTextField(headline, { headline = it }, "Headline")
                STTextField(handle, { handle = it }, "Network handle")
                STTextField(location, { location = it }, "Location")
                STTextField(website, { website = it }, "Website")
                STTextField(bio, { bio = it }, "Bio", singleLine = false)
            }

            Section("Contact") {
                STTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
                STTextField(phone, { phone = it }, "Phone", keyboardType = KeyboardType.Phone)
            }

            Section("Security") {
                STTextField(currentPassword, { currentPassword = it }, "Current password", isPassword = true)
                STTextField(newPassword, { newPassword = it }, "New password", isPassword = true)
            }

            message?.let { Text(it, color = if (success) STColor.success else STColor.danger, fontSize = 13.sp) }

            GradientButton("Save changes", Modifier.fillMaxWidth(), busy = saving) {
                scope.launch {
                    saving = true
                    message = null
                    fun String.orNull() = trim().ifEmpty { null }
                    val body = AccountPatchBody(
                        name = name.orNull(), email = email.orNull(), phoneNumber = phone.orNull(),
                        bio = bio.orNull(), headline = headline.orNull(), location = location.orNull(),
                        website = website.orNull(), networkHandle = handle.orNull(),
                        currentPassword = currentPassword.orNull(), newPassword = newPassword.orNull(),
                    )
                    val result = runCatching { client.patch<CreatorUser, AccountPatchBody>("/api/me", body) }
                    saving = false
                    if (result.isSuccess) {
                        result.getOrNull()?.let { auth.applyProfile(it) }
                        currentPassword = ""; newPassword = ""
                        success = true; message = "Profile saved."
                    } else {
                        success = false; message = result.exceptionOrNull()?.message ?: "Save failed."
                    }
                }
            }

            GradientButton("Sign out", Modifier.fillMaxWidth()) { scope.launch { auth.signOut() } }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeader(title)
        content()
    }
}
