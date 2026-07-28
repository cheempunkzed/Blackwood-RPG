package com.example.blackwoodrpg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.model.CombatLog
import com.example.blackwoodrpg.model.LogType

/**
 * Real-time action & activity feed panel.
 */
@Composable
fun CombatLogPanel(
    logs: List<CombatLog>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF14100D))
            .border(1.dp, Color(0xFF2E241C), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "📜 Activity & Combat Log",
            color = Color(0xFFE8C180),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(logs, key = { it.id }) { log ->
                    val color = when (log.logType) {
                        LogType.COMBAT -> Color(0xFFFF8888)
                        LogType.GATHERING -> Color(0xFF88DDFF)
                        LogType.CRAFTING -> Color(0xFFFFDD88)
                        LogType.LEVEL_UP -> Color(0xFFFFD700)
                        LogType.EQUIPMENT -> Color(0xFFDDAAFF)
                        LogType.SYSTEM -> Color.LightGray
                    }

                    Text(
                        text = "• ${log.message}",
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (log.logType == LogType.LEVEL_UP) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
