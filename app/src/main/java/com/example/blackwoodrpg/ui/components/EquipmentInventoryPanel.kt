package com.example.blackwoodrpg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.domain.InventorySlot
import com.example.blackwoodrpg.model.EquipmentSlot
import com.example.blackwoodrpg.model.Item
import com.example.blackwoodrpg.model.SkillType
import com.example.blackwoodrpg.model.StatBonus

/**
 * Character equipment paper doll & inventory bag grid panel.
 */
@Composable
fun EquipmentInventoryPanel(
    equipmentMap: Map<EquipmentSlot, Item.Equipable?>,
    inventorySlots: List<InventorySlot>,
    totalStats: StatBonus,
    primaryTrainedSkill: SkillType,
    onEquipSlotClicked: (EquipmentSlot) -> Unit,
    onInventorySlotClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItemSlotIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B1713))
            .border(1.dp, Color(0xFF382C22), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "🛡️ Equipment & Inventory Bag",
            color = Color(0xFFE8C180),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Currently Trained Skill Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A1F16))
                .border(1.dp, Color(0xFFE5A638), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚡ Primary Trained Skill: ", color = Color.White, fontSize = 12.sp)
                Text(
                    text = "${primaryTrainedSkill.iconSymbol} ${primaryTrainedSkill.displayName}",
                    color = Color(0xFFFFD700),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Paper Doll Equipment Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EquipmentSlotBox(
                slot = EquipmentSlot.HEAD,
                item = equipmentMap[EquipmentSlot.HEAD],
                slotIcon = "🪖",
                onClick = { onEquipSlotClicked(EquipmentSlot.HEAD) }
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.CHEST,
                item = equipmentMap[EquipmentSlot.CHEST],
                slotIcon = "🛡️",
                onClick = { onEquipSlotClicked(EquipmentSlot.CHEST) }
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.MAIN_HAND,
                item = equipmentMap[EquipmentSlot.MAIN_HAND],
                slotIcon = "🗡️",
                highlightBorder = true,
                onClick = { onEquipSlotClicked(EquipmentSlot.MAIN_HAND) }
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.OFF_HAND,
                item = equipmentMap[EquipmentSlot.OFF_HAND],
                slotIcon = "🛡️",
                onClick = { onEquipSlotClicked(EquipmentSlot.OFF_HAND) }
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.LEGS,
                item = equipmentMap[EquipmentSlot.LEGS],
                slotIcon = "🦵",
                onClick = { onEquipSlotClicked(EquipmentSlot.LEGS) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stats summary bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF14100D), RoundedCornerShape(6.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(text = "⚔️ Atk: +${totalStats.attackPower}", color = Color(0xFFFF7777), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "🛡️ Def: +${totalStats.defensePower}", color = Color(0xFF77AAFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "⛏️ Mining: +${totalStats.miningPower}", color = Color(0xFFFFCC44), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = "❤️ HP: +${totalStats.maxHealthBonus}", color = Color(0xFF77FF77), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Inventory Slots (Tap to Equip/Use):",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Inventory Grid (4 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            items(inventorySlots, key = { it.slotIndex }) { slot ->
                val isSelected = selectedItemSlotIndex == slot.slotIndex
                InventoryCell(
                    slot = slot,
                    isSelected = isSelected,
                    onClick = {
                        selectedItemSlotIndex = slot.slotIndex
                        onInventorySlotClicked(slot.slotIndex)
                    }
                )
            }
        }
    }
}

@Composable
fun EquipmentSlotBox(
    slot: EquipmentSlot,
    item: Item.Equipable?,
    slotIcon: String,
    highlightBorder: Boolean = false,
    onClick: () -> Unit
) {
    val borderColor = if (highlightBorder) Color(0xFFFFD700) else Color(0xFF4A3B2E)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF261D16))
                .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                Text(text = item.iconSymbol, fontSize = 24.sp)
            } else {
                Text(text = slotIcon, fontSize = 20.sp, color = Color.DarkGray)
            }
        }
        Text(
            text = slot.slotName,
            color = Color.Gray,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun InventoryCell(
    slot: InventorySlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFFFD700) else Color(0xFF3B2E24)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF221A14))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!slot.isEmpty && slot.item != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = slot.item.iconSymbol, fontSize = 18.sp)
                if (slot.count > 1) {
                    Text(
                        text = "x${slot.count}",
                        color = Color.Yellow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
