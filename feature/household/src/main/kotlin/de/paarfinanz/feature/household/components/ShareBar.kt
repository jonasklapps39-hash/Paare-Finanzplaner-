package de.paarfinanz.feature.household.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.paarfinanz.core.domain.model.ShareBps
import de.paarfinanz.core.ui.theme.PaarfinanzTheme
import kotlin.math.roundToInt

/** Zweifarbiger Balken mit Prozentangaben, z. B. "Anna 58 % · Ben 42 %". */
@Composable
internal fun ShareBar(
    firstName: String,
    firstShare: ShareBps,
    secondName: String,
    modifier: Modifier = Modifier,
) {
    val colors = PaarfinanzTheme.extendedColors
    val firstWeight = firstShare.value.toFloat()
    val secondWeight = firstShare.complement.value.toFloat()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
        ) {
            if (firstWeight > 0f) {
                Box(Modifier.weight(firstWeight).height(10.dp).background(colors.partnerA))
            }
            if (firstWeight > 0f && secondWeight > 0f) Spacer(Modifier.width(2.dp))
            if (secondWeight > 0f) {
                Box(Modifier.weight(secondWeight).height(10.dp).background(colors.partnerB))
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "$firstName ${firstShare.percent.roundToInt()} %",
                style = MaterialTheme.typography.labelLarge,
                color = colors.partnerA,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "$secondName ${firstShare.complement.percent.roundToInt()} %",
                style = MaterialTheme.typography.labelLarge,
                color = colors.partnerB,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
