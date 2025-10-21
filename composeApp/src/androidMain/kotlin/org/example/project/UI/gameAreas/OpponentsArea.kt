

import android.content.ClipDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.models.Player
import kotlin.random.Random

@Composable
fun OpponentsArea(playerId: Int, players: List<Player>, onCardDropped: (cardId: String, opponent: Player) -> Unit) {
    println("XXX OpponentsArea")
    val colors = remember {
        (0..players.size + 1).map { Color(Random.nextLong()).copy(alpha = 1f) }
    }

    Text("Gracze:", style = MaterialTheme.typography.titleLarge)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        players.filter { it.id != playerId }.forEachIndexed { index, opponent ->

            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .background(colors[index])
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { evet ->
                            evet
                                .mimeTypes()
                                .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        },
                        target = remember {
                            object : DragAndDropTarget {
                                override fun onDrop(event: DragAndDropEvent): Boolean {
                                   val cardId = event.toAndroidDragEvent().clipData.getItemAt(0)?.text
                                    println("Dropped card $cardId on opponent ${opponent.name}")
                                    cardId?.let {
                                        onCardDropped(it.toString(), opponent)
                                    }
                                    return true
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Gracz $opponent", style = MaterialTheme.typography.bodySmall)
                    Text("Zagrane karty:")

                }
            }
        }
    }
}



