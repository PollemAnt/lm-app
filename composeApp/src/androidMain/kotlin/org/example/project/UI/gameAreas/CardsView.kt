
import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import org.example.models.Card
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CardView(card: Card) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(card.name, style = MaterialTheme.typography.bodySmall)
            Text("Nr ${card.number}", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun HiddenCardView(playerName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .width(80.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text(playerName, style = MaterialTheme.typography.bodySmall)
            Text("Zagrane karty:")
        }
    }
}

@Composable
fun DraggableCard(
    card: Card
) {
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .width(80.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            .dragAndDropSource(
                transferData = {
                    DragAndDropTransferData(
                        clipData = ClipData.newPlainText("card",card.id.toString()),
                        println("XXX dragAndDropSource card ${card.id} ${card.name}")
                    )
                }
            ),
        contentAlignment = Alignment.TopCenter
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(card.name, style = MaterialTheme.typography.bodyLarge)
            Text("${card.number}")
        }
    }
}

@Composable
fun ClickableCard(
    card: Card,
    isSelected: Boolean,
    onCardClicked: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                2.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                RoundedCornerShape(16.dp)
            )
            .clickable { onCardClicked() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(card.name, style = MaterialTheme.typography.titleMedium)
            Text("Nr ${card.number}")
        }
    }
}