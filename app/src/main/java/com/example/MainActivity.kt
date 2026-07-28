package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.domain.GameEngine
import com.example.blackwoodrpg.model.SkillType
import com.example.blackwoodrpg.ui.components.CombatLogPanel
import com.example.blackwoodrpg.ui.components.CraftingPanel
import com.example.blackwoodrpg.ui.components.EquipmentInventoryPanel
import com.example.blackwoodrpg.ui.components.IsometricWorldView
import com.example.blackwoodrpg.ui.components.SkillMasteryPanel
import com.example.ui.theme.MyApplicationTheme

enum class GameTab(val title: String, val icon: String) {
    WORLD("Wilderness", "🗺️"),
    SKILLS("Mastery", "⚔️"),
    INVENTORY("Gear & Bag", "🎒"),
    CRAFTING("Forge", "🔨")
}

class MainActivity : ComponentActivity() {
    private val gameEngine = GameEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF120E0B)
                ) {
                    BlackwoodRpgMainScreen(gameEngine = gameEngine)
                }
            }
        }
    }
}

@Composable
fun BlackwoodRpgMainScreen(gameEngine: GameEngine) {
    val player = gameEngine.player

    var selectedTab by remember { mutableStateOf(GameTab.WORLD) }

    val currentHp by player.currentHp.collectAsState()
    val maxHp = player.getMaxHp()

    val skillsMap by player.skillManager.skillsState.collectAsState()
    val totalSkillLevel = player.skillManager.getTotalLevel()
    val totalXp = player.skillManager.getTotalXp()

    val equipmentMap by player.equipmentSystem.equipmentState.collectAsState()
    val inventorySlots by player.inventorySystem.inventoryState.collectAsState()
    val totalStats = player.equipmentSystem.getTotalStats()
    val trainedSkill = player.equipmentSystem.getPrimaryTrainedSkill()

    val enemies by gameEngine.enemiesState.collectAsState()
    val miningNodes by gameEngine.miningNodesState.collectAsState()
    val floatingTexts by gameEngine.floatingTextsState.collectAsState()
    val logs by player.logsState.collectAsState()

    Scaffold(
        topBar = {
            // Player Top Header Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B1511))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🛡️ ${player.name}",
                            color = Color(0xFFE8C180),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF3B2A1C))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Total Lv. $totalSkillLevel",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Currently Equipped Weapon Training Skill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E2218))
                            .border(1.dp, Color(0xFFE5A638), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Training: ${trainedSkill.iconSymbol} ${trainedSkill.displayName}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // HP Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HP: ",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFFF4444),
                        trackColor = Color(0xFF4A1818)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currentHp / $maxHp",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1B1511),
                contentColor = Color(0xFFE8C180)
            ) {
                GameTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(text = tab.icon, fontSize = 20.sp) },
                        label = { Text(text = tab.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD700),
                            selectedTextColor = Color(0xFFFFD700),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF38271B)
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFF120E0B)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp)
                .fillMaxSize()
        ) {
            // Main Active View Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    GameTab.WORLD -> {
                        IsometricWorldView(
                            playerPos = player.position,
                            enemies = enemies,
                            miningNodes = miningNodes,
                            floatingTexts = floatingTexts,
                            onEnemyClicked = { enemy -> gameEngine.performAttack(enemy) },
                            onNodeClicked = { node -> gameEngine.performMining(node) }
                        )
                    }

                    GameTab.SKILLS -> {
                        SkillMasteryPanel(
                            skillsMap = skillsMap,
                            totalLevel = totalSkillLevel,
                            totalXp = totalXp
                        )
                    }

                    GameTab.INVENTORY -> {
                        EquipmentInventoryPanel(
                            equipmentMap = equipmentMap,
                            inventorySlots = inventorySlots,
                            totalStats = totalStats,
                            primaryTrainedSkill = trainedSkill,
                            onEquipSlotClicked = { slot -> player.unequipToInventory(slot) },
                            onInventorySlotClicked = { slotIndex -> player.equipFromInventory(slotIndex) }
                        )
                    }

                    GameTab.CRAFTING -> {
                        CraftingPanel(
                            currentCraftingLevel = player.skillManager.getLevel(SkillType.CRAFTING),
                            onCraftClicked = { recipe -> player.craftItem(recipe) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sticky Bottom Activity / Combat Log Feed
            CombatLogPanel(logs = logs)
        }
    }
}

