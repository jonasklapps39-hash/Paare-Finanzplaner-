package de.paarfinanz.feature.household.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.paarfinanz.core.data.household.HouseholdState
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.domain.model.SplitMode
import de.paarfinanz.core.domain.split.PartnerShares
import de.paarfinanz.core.ui.components.PartnerAvatar
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import de.paarfinanz.feature.household.R
import de.paarfinanz.feature.household.components.ShareBar
import java.time.LocalDate

/** Haushaltskarte für die Übersicht: wer gehört dazu, wie wird geteilt, was fehlt noch. */
@Composable
fun HouseholdOverviewCard(
    onInvite: () -> Unit,
    onEditIncome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HouseholdOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.householdState.collectAsStateWithLifecycle()
    val ready = state as? HouseholdState.Ready ?: return
    val household = ready.household
    val today = LocalDate.now()
    val meLabel = stringResource(R.string.me)
    val partnerFallback = stringResource(R.string.partner_fallback)

    fun nameOf(index: Int): String {
        val member = household.members.getOrNull(index) ?: return partnerFallback
        return if (member.userId == ready.currentUserId) meLabel else member.displayName.ifBlank { partnerFallback }
    }

    val needsInvite = !household.isComplete
    val needsIncome = household.defaultSplitMode == SplitMode.INCOME &&
        household.currentIncome(ready.currentUserId, today) == null

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarPair(
                    firstName = household.members.getOrNull(0)?.displayName.orEmpty(),
                    secondName = household.members.getOrNull(1)?.displayName,
                )
                Column {
                    Text(household.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = if (household.isComplete) "${nameOf(0)} & ${nameOf(1)}" else stringResource(R.string.overview_alone),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (household.isComplete) {
                SplitSummary(
                    mode = household.defaultSplitMode,
                    firstName = nameOf(0),
                    secondName = nameOf(1),
                    incomeShare = household.incomeShares(today),
                    fixedShare = household.fixedShares()?.firstShare,
                )
            }

            if (needsInvite || needsIncome) {
                HorizontalDivider()
                Text(stringResource(R.string.overview_setup_title), style = MaterialTheme.typography.titleSmall)
                if (needsInvite) SetupItem(Icons.Outlined.PersonAdd, stringResource(R.string.overview_setup_invite), onInvite)
                if (needsIncome) SetupItem(Icons.Outlined.Payments, stringResource(R.string.overview_setup_income), onEditIncome)
            }
        }
    }
}

@Composable
private fun SplitSummary(
    mode: SplitMode,
    firstName: String,
    secondName: String,
    incomeShare: PartnerShares?,
    fixedShare: ShareBps?,
) {
    val (label, share) = when (mode) {
        SplitMode.INCOME -> stringResource(R.string.overview_split_income) to (incomeShare?.firstShare ?: ShareBps.HALF)
        SplitMode.FIXED -> stringResource(R.string.overview_split_fixed) to (fixedShare ?: ShareBps.HALF)
        else -> stringResource(R.string.overview_split_equal) to ShareBps.HALF
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ShareBar(firstName = firstName, firstShare = share, secondName = secondName)
        if (mode == SplitMode.INCOME && incomeShare?.isFallback != false) {
            Text(
                text = stringResource(R.string.overview_split_fallback),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AvatarPair(firstName: String, secondName: String?) {
    val colors = PaarfinanzTheme.extendedColors
    Box(modifier = Modifier.size(width = 64.dp, height = 40.dp)) {
        PartnerAvatar(name = firstName, color = colors.partnerA, size = 40.dp)
        Box(modifier = Modifier.offset(x = 24.dp)) {
            if (secondName != null) {
                PartnerAvatar(name = secondName, color = colors.partnerB, size = 40.dp)
            } else {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("?", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.medium, color = Color.Transparent) {
        ListItem(
            leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            headlineContent = { Text(text) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
