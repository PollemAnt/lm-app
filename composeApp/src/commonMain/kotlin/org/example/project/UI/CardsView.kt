package org.example.project.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import org.example.Card
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

class CardsView {

    @Composable
    fun CardView(card: Card, modifier: Modifier) {
        Box(
            modifier = modifier
                .padding(8.dp)
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nr ${card.number}", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Text(card.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(card.description)
            }
        }
    }

    @Composable
    fun HiddenCardView(playerName: String, modifier: Modifier=Modifier) {
        Box(
            modifier = modifier
                .padding(8.dp)
                .width(120.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text("?", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(8.dp))
                Text(playerName, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}