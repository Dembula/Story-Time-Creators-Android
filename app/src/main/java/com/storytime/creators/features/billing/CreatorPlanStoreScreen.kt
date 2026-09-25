package com.storytime.creators.features.billing

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.billing.BillingError
import com.storytime.creators.core.billing.CreatorFreePlanOption
import com.storytime.creators.core.billing.CreatorStoreProduct
import com.storytime.creators.core.billing.PurchaseKind
import com.storytime.creators.core.network.AppConfig
import com.storytime.creators.core.theme.STColor

@Composable
fun CreatorPlanStoreDialog(
    title: String = "Choose your creator plan",
    onDismiss: () -> Unit,
    onCompleted: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        CreatorPlanStoreScreen(
            title = title,
            onClose = onDismiss,
            onCompleted = {
                onCompleted()
                onDismiss()
            },
        )
    }
}

@Composable
fun CreatorPlanStoreScreen(
    title: String = "Choose your creator plan",
    onClose: () -> Unit,
    onCompleted: () -> Unit = {},
) {
    val auth = Session.auth
    val billing = Session.billing
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    var busyKey by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var succeeded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { billing.loadProducts() }

    Column(
        Modifier
            .fillMaxSize()
            .background(STColor.background)
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = STColor.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Close", color = STColor.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onClose() })
        }
        Spacer(Modifier.height(12.dp))

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Subscriptions and unlocks are purchased with Google Play Billing. Your Google account is charged — not a web payment page.",
                color = STColor.textSecondary,
                fontSize = 13.sp,
            )

            PlanCard(
                title = CreatorFreePlanOption.title,
                detail = CreatorFreePlanOption.detail,
                priceLabel = CreatorFreePlanOption.priceLabel,
                badge = CreatorFreePlanOption.badge,
                busy = busyKey == "perfilm",
                enabled = busyKey == null,
            ) {
                scope.launch {
                    busyKey = "perfilm"
                    message = null
                    succeeded = false
                    try {
                        billing.activateFreePerFilmPlan()
                        auth.refreshPackageGate()
                        succeeded = true
                        message = "Pay-per-film plan activated. You’ll pay per title when submitting for review."
                        onCompleted()
                    } catch (e: Exception) {
                        message = e.message ?: "Could not activate plan."
                    } finally {
                        busyKey = null
                    }
                }
            }

            CreatorStoreProduct.planProducts.forEach { kind ->
                PlanCard(
                    title = kind.title,
                    detail = kind.detail,
                    priceLabel = billing.displayPrice(kind),
                    badge = if (billing.productDetails[kind.productId] == null) "Store" else null,
                    busy = busyKey == kind.productId,
                    enabled = busyKey == null,
                ) {
                    if (activity == null) {
                        message = "Unable to open Google Play from this screen."
                        return@PlanCard
                    }
                    scope.launch {
                        busyKey = kind.productId
                        message = null
                        succeeded = false
                        try {
                            val purchase = billing.purchase(activity, kind)
                            billing.reportPurchaseToServer(purchase, PurchaseKind.creatorLicense, kind)
                            auth.refreshPackageGate()
                            succeeded = true
                            message = "Plan unlocked."
                            onCompleted()
                        } catch (e: BillingError.UserCancelled) {
                            message = e.message
                        } catch (e: Exception) {
                            message = e.message ?: "Purchase failed."
                        } finally {
                            busyKey = null
                        }
                    }
                }
            }

            if (billing.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = STColor.primary, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.size(10.dp))
                    Text("Loading Play Store products…", color = STColor.textMuted, fontSize = 13.sp)
                }
            }

            message?.let {
                Text(it, color = if (succeeded) STColor.success else STColor.danger, fontSize = 13.sp)
            }

            Text(
                "Restore purchases",
                color = STColor.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = busyKey == null) {
                        scope.launch {
                            busyKey = "restore"
                            try {
                                val unlocked = billing.restoreAndReport()
                                auth.refreshPackageGate()
                                if (!auth.needsPlanSetup || unlocked) {
                                    succeeded = true
                                    message = "Purchases restored."
                                    onCompleted()
                                } else {
                                    succeeded = false
                                    message = billing.lastError ?: "No active subscription found for this Google account."
                                }
                            } catch (e: Exception) {
                                succeeded = false
                                message = e.message
                            } finally {
                                busyKey = null
                            }
                        }
                    }
                    .padding(vertical = 12.dp),
            )

            if (!auth.needsPlanSetup) {
                Text(
                    "Manage existing subscription",
                    color = STColor.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.MANAGE_SUBS_URL)))
                        }
                        .padding(vertical = 12.dp),
                )
            }

            Text(
                "Payment is charged to your Google account. Auto-renewing subscriptions renew unless cancelled at least 24 hours before the end of the period. Manage in Play Store → Payments & subscriptions.",
                color = STColor.textMuted,
                fontSize = 11.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Terms of Use",
                    color = STColor.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.TERMS_URL)))
                    },
                )
                Text(
                    "Privacy Policy",
                    color = STColor.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PRIVACY_URL)))
                    },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun UploadFeeStoreSheet(
    contentId: String,
    displayFee: String?,
    onDismiss: () -> Unit,
    onPaid: () -> Unit,
) {
    val billing = Session.billing
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val product = CreatorStoreProduct.perFilmUpload
    val price = displayFee?.takeIf { it.isNotBlank() } ?: billing.displayPrice(product)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(STColor.surface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Pay upload fee", color = STColor.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Your draft is saved. Pay $price with Google Play to submit this title for admin review.",
                color = STColor.textSecondary,
                fontSize = 13.sp,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(STColor.primary)
                    .clickable(enabled = !busy && activity != null) {
                        if (activity == null) {
                            message = "Unable to open Google Play."
                            return@clickable
                        }
                        scope.launch {
                            busy = true
                            message = null
                            try {
                                val purchase = billing.purchase(activity, product)
                                billing.reportPurchaseToServer(
                                    purchase,
                                    PurchaseKind.contentUpload,
                                    product,
                                    contentId = contentId,
                                )
                                onPaid()
                                onDismiss()
                            } catch (e: BillingError.UserCancelled) {
                                message = e.message
                            } catch (e: Exception) {
                                message = e.message ?: "Payment failed."
                            } finally {
                                busy = false
                            }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (busy) CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                else Text("Pay $price", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            message?.let { Text(it, color = STColor.danger, fontSize = 12.sp) }
            Text(
                "Cancel",
                color = STColor.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onDismiss() }.padding(8.dp),
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    detail: String,
    priceLabel: String,
    badge: String?,
    busy: Boolean,
    enabled: Boolean,
    onContinue: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(STColor.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(title, color = STColor.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(priceLabel, color = STColor.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        badge?.let { Text(it, color = STColor.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
        Text(detail, color = STColor.textSecondary, fontSize = 13.sp)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) STColor.primary else STColor.textMuted)
                .clickable(enabled = enabled) { onContinue() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (busy) CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                Text(if (busy) "Working…" else "Continue", color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}
