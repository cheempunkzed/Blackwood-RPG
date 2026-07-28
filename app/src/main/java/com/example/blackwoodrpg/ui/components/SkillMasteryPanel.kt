package com.example.blackwoodrpg.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.model.SkillData
import com.example.blackwoodrpg.model.SkillType

/**
 * Renders the Classless Skill Progression & Mastery overview.
 */
@Composable
fun SkillMasteryPanel(
    skillsMap: Map<SkillType, SkillData>,
    totalLevel: Int,
    totalXp: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1915))
            .border(1.dp, Color(0xFF3D3025), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Header summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "⚔️ Skill Mastery System",
                    color = Color(0xFFE8C180),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Equipment dictates what skill levels up",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF38291A))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Total Skill Lv: $totalLevel",
                    color = Color(0xFFFFD700),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Skills List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(skillsMap.values.toList(), key = { it.skillType.name }) { skill ->
                SkillItemCard(skill = skill)
            }
        }
    }
}

@Composable
fun SkillItemCard(
    skill: SkillData
) {
    val progress = skill.getXpProgressInCurrentLevel()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "XpProgress")

    val reqNextXp = skill.getRequiredXpForNextLevel()

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B221B)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = skill.skillType.iconSymbol,
                fontSize = 26.sp,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Info & Progress Bar
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = skill.skillType.displayName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lv. ${skill.level} / ${skill.maxLevel}",
                        color = Color(0xFFE8C180),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Linear Progress
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFE5A638),
                    trackColor = Color(0xFF423428)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = skill.skillType.description,
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "XP: ${skill.currentXp} / $reqNextXp",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
