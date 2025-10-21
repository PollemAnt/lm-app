

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.models.Player
import kotlin.random.Random

@Composable
fun OpponentsArea(currentPlayer: Player, players: List<Player>) {
    println("XXX OpponentsArea")
    val colors = remember {
        (0..players.size + 1).map {
            Color(Random.nextLong()).copy(alpha = 1f)
        }
    }
    var dragBoxIndex by remember {
        mutableIntStateOf(0)
    }

    Text("Gracze:", style = MaterialTheme.typography.titleLarge)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        repeat(players.size) { index ->

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
                                    val text = event.toAndroidDragEvent().clipData
                                        ?.getItemAt(0)?.text
                                    println("Drag data was $text")

                                    dragBoxIndex = index
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
                    AnimatedVisibility(
                        visible = index == dragBoxIndex,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Text(
                            text = "Drag me!",
                            fontSize = 40.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .dragAndDropSource(
                                    transferData = { offset ->

                                        DragAndDropTransferData(
                                            clipData = ClipData.newPlainText("text/plain", "To jest przeciągany tekst!")
                                        )
                                    }
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { offset ->
                                        }
                                    )
                                }
                        )
                    }

                    Text("Gracz ${players[index]}", style = MaterialTheme.typography.bodySmall)
                    Text("Zagrane karty:")

                }
            }

        }
    }
    Row {
        players.filter { it.id != currentPlayer.id }.forEach { opponent ->
            HiddenCardView(opponent.name, Modifier.weight(1f))
        }
    }
}



