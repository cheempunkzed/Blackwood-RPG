package com.example.blackwoodrpg.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.model.EnemyEntity
import com.example.blackwoodrpg.model.FloatingText
import com.example.blackwoodrpg.model.MiningNode
import com.example.blackwoodrpg.model.Vector2i
import kotlin.math.abs

/**
 * Converts 2D grid coordinates (x, y) into 2D isometric projection screen offsets.
 */
fun gridToIso(gridX: Float, gridY: Float, tileWidth: Float, tileHeight: Float, originX: Float, originY: Float): Offset {
    val isoX = originX + (gridX - gridY) * (tileWidth / 2f)
    val isoY = originY + (gridX + gridY) * (tileHeight / 2f)
    return Offset(isoX, isoY)
}

/**
 * 2D Isometric Canvas world view rendering grid tiles, entities, health bars, and interactive actions.
 */
@Composable
fun IsometricWorldView(
    playerPos: Vector2i,
    enemies: List<EnemyEntity>,
    miningNodes: List<MiningNode>,
    floatingTexts: List<FloatingText>,
    onEnemyClicked: (EnemyEntity) -> Unit,
    onNodeClicked: (MiningNode) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapWidth = 10
    val mapHeight = 10

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF141210))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enemies, miningNodes) {
                    detectTapGestures { tapOffset ->
                        val tileW = 76f
                        val tileH = 38f
                        val origX = size.width / 2f
                        val origY = 110f

                        // Find closest enemy tapped
                        val tappedEnemy = enemies.firstOrNull { enemy ->
                            if (enemy.isDead) return@firstOrNull false
                            val pos = gridToIso(enemy.position.x.toFloat(), enemy.position.y.toFloat(), tileW, tileH, origX, origY)
                            val dist = (tapOffset - pos).getDistance()
                            dist < 45f
                        }

                        if (tappedEnemy != null) {
                            onEnemyClicked(tappedEnemy)
                            return@detectTapGestures
                        }

                        // Find closest mining node tapped
                        val tappedNode = miningNodes.firstOrNull { node ->
                            if (node.isDepleted) return@firstOrNull false
                            val pos = gridToIso(node.position.x.toFloat(), node.position.y.toFloat(), tileW, tileH, origX, origY)
                            val dist = (tapOffset - pos).getDistance()
                            dist < 45f
                        }

                        if (tappedNode != null) {
                            onNodeClicked(tappedNode)
                        }
                    }
                }
        ) {
            val tileW = 76f
            val tileH = 38f
            val origX = size.width / 2f
            val origY = 110f

            // 1. Render Isometric Ground Grid
            for (x in 0 until mapWidth) {
                for (y in 0 until mapHeight) {
                    val centerIso = gridToIso(x.toFloat(), y.toFloat(), tileW, tileH, origX, origY)

                    val path = Path().apply {
                        moveTo(centerIso.x, centerIso.y - tileH / 2f)
                        lineTo(centerIso.x + tileW / 2f, centerIso.y)
                        lineTo(centerIso.x, centerIso.y + tileH / 2f)
                        lineTo(centerIso.x - tileW / 2f, centerIso.y)
                        close()
                    }

                    val tileColor = if ((x + y) % 2 == 0) Color(0xFF232D1B) else Color(0xFF1D2616)
                    drawPath(path, color = tileColor)
                    drawPath(path, color = Color(0xFF323F28), style = Stroke(width = 1f))
                }
            }

            // 2. Render Mining Nodes
            miningNodes.forEach { node ->
                if (!node.isDepleted) {
                    val iso = gridToIso(node.position.x.toFloat(), node.position.y.toFloat(), tileW, tileH, origX, origY)
                    
                    // Draw Node Shadow
                    drawCircle(Color(0x66000000), radius = 18f, center = Offset(iso.x, iso.y + 6f))

                    // Draw Node Icon
                    drawContext.canvas.nativeCanvas.drawText(
                        node.iconSymbol,
                        iso.x - 14f,
                        iso.y + 10f,
                        android.graphics.Paint().apply {
                            textSize = 34f
                        }
                    )

                    // Draw Ore count indicator
                    drawContext.canvas.nativeCanvas.drawText(
                        "${node.currentOre}/${node.maxOre}",
                        iso.x - 12f,
                        iso.y - 20f,
                        android.graphics.Paint().apply {
                            textSize = 20f
                            color = android.graphics.Color.YELLOW
                            isFakeBoldText = true
                        }
                    )
                }
            }

            // 3. Render Enemies
            enemies.forEach { enemy ->
                if (!enemy.isDead) {
                    val iso = gridToIso(enemy.position.x.toFloat(), enemy.position.y.toFloat(), tileW, tileH, origX, origY)

                    // Draw Shadow
                    drawCircle(Color(0x88000000), radius = 22f, center = Offset(iso.x, iso.y + 8f))

                    // Draw Enemy Symbol
                    drawContext.canvas.nativeCanvas.drawText(
                        enemy.iconSymbol,
                        iso.x - 16f,
                        iso.y + 12f,
                        android.graphics.Paint().apply {
                            textSize = 40f
                        }
                    )

                    // Draw Health Bar
                    val hpRatio = enemy.currentHp.toFloat() / enemy.maxHp.toFloat()
                    val barWidth = 48f
                    val barHeight = 6f
                    val barX = iso.x - barWidth / 2f
                    val barY = iso.y - 32f

                    drawRect(Color.DarkGray, topLeft = Offset(barX, barY), size = androidx.compose.ui.geometry.Size(barWidth, barHeight))
                    drawRect(Color.Red, topLeft = Offset(barX, barY), size = androidx.compose.ui.geometry.Size(barWidth * hpRatio, barHeight))
                }
            }

            // 4. Render Player Character
            val playerIso = gridToIso(playerPos.x.toFloat(), playerPos.y.toFloat(), tileW, tileH, origX, origY)
            drawCircle(Color(0xA32D5A), radius = 26f, center = Offset(playerIso.x, playerIso.y + 10f))

            // Player Avatar Symbol
            drawContext.canvas.nativeCanvas.drawText(
                "⚔️",
                playerIso.x - 18f,
                playerIso.y + 14f,
                android.graphics.Paint().apply {
                    textSize = 46f
                }
            )

            // Player Title Tag
            drawContext.canvas.nativeCanvas.drawText(
                "YOU",
                playerIso.x - 16f,
                playerIso.y - 32f,
                android.graphics.Paint().apply {
                    textSize = 22f
                    color = android.graphics.Color.GREEN
                    isFakeBoldText = true
                }
            )

            // 5. Render Floating Combat Damage / XP Numbers
            floatingTexts.forEach { ft ->
                val ftIso = gridToIso(ft.isoX, ft.isoY, tileW, tileH, origX, origY)
                val alphaInt = (ft.alpha.coerceIn(0f, 1f) * 255).toInt()
                val paintColor = (alphaInt shl 24) or (ft.colorHex.toInt() and 0x00FFFFFF)

                drawContext.canvas.nativeCanvas.drawText(
                    ft.text,
                    ftIso.x - 20f,
                    ftIso.y + ft.offsetY - 25f,
                    android.graphics.Paint().apply {
                        textSize = 28f
                        color = paintColor
                        isFakeBoldText = true
                    }
                )
            }
        }

        // World Banner Legend
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xDD1B1713))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "🗺️ Blackwood Wilderness (Tap 👹 Enemy to Attack | 🪨 Ore to Mine)",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
