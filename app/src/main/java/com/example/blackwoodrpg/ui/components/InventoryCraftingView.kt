package com.example.blackwoodrpg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.domain.InventorySlot
import com.example.blackwoodrpg.domain.InventorySystem
import com.example.blackwoodrpg.model.CraftRecipe
import com.example.blackwoodrpg.model.Item
import com.example.blackwoodrpg.model.ItemCatalog

@Composable
fun InventoryCraftingView(
    inventorySystem: InventorySystem,
    playerCraftingLevel: Int,
    onEquipItem: (Item.Equipable) -> Unit,
    onCraftRecipe: (CraftRecipe) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) }
    var selectedSlot by remember { mutableStateOf<InventorySlot?>(null) }
    val slots by inventorySystem.inventoryState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(
            selectedTabIndex = subTab,
            containerColor = Color(0xFF1E1711),
            contentColor = Color(0xFFFFD54F)
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("🎒 Inventory Grid", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("🔨 Anvil Crafting", fontWeight = FontWeight.Bold) }
            )
        }

        when (subTab) {
            0 -> InventoryGridPanel(
                slots = slots,
                selectedSlot = selectedSlot,
                onSlotClick = { slot -> selectedSlot = slot },
                onEquip = { item ->
                    onEquipItem(item)
                    selectedSlot = null
                }
            )
            1 -> CraftingAnvilPanel(
                playerCraftingLevel = playerCraftingLevel,
                inventorySystem = inventorySystem,
                onCraft = onCraftRecipe
            )
        }
    }
}

@Composable
fun InventoryGridPanel(
    slots: List<InventorySlot>,
    selectedSlot: InventorySlot?,
    onSlotClick: (InventorySlot?) -> Unit,
    onEquip: (Item.Equipable) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(slots) { _, slot ->
                val isSelected = slot.slotIndex == selectedSlot?.slotIndex
                val item = slot.item

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSlotClick(if (slot.isEmpty) null else slot) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item != null) Color(0xFF2E2218) else Color(0xFF1B140E)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F)) else null
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item != null) {
                            Text(text = item.iconSymbol, fontSize = 24.sp)
                            if (slot.count > 1) {
                                Text(
                                    text = "${slot.count}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color(0xAA000000), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        selectedSlot?.item?.let { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF231A12))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.iconSymbol} ${item.name} (T${item.tier})",
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = item.description,
                            color = Color(0xFFC0B4A2),
                            fontSize = 12.sp
                        )
                    }

                    if (item is Item.Equipable) {
                        Button(
                            onClick = { onEquip(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                        ) {
                            Text("Equip", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CraftingAnvilPanel(
    playerCraftingLevel: Int,
    inventorySystem: InventorySystem,
    onCraft: (CraftRecipe) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Blackwood Anvil Recipes (Crafting Lv. $playerCraftingLevel)",
            color = Color(0xFFE0D5C1),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        ItemCatalog.RECIPES.forEach { recipe ->
            val hasLevel = playerCraftingLevel >= recipe.requiredCraftingLevel
            val hasMats = inventorySystem.hasIngredients(recipe.ingredients)
            val canCraft = hasLevel && hasMats

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (canCraft) Color(0xFF281F17) else Color(0xFF1B140E)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = recipe.resultItem.iconSymbol, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = recipe.resultItem.name,
                                    color = Color(0xFFF5E6D3),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Req Crafting Lv.${recipe.requiredCraftingLevel} | +${recipe.xpReward} XP",
                                    color = if (hasLevel) Color(0xFF43A047) else Color(0xFFE53935),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { onCraft(recipe) },
                            enabled = canCraft,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D))
                        ) {
                            Text("Forge", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Ingredients:",
                        color = Color(0xFFA89F91),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    recipe.ingredients.forEach { (ingredientId, reqQty) ->
                        val currentQty = inventorySystem.getItemCount(ingredientId)
                        val ingName = ItemCatalog.getItemById(ingredientId)?.name ?: ingredientId
                        val ingMet = currentQty >= reqQty

                        Text(
                            text = " • $ingName: $currentQty / $reqQty",
                            color = if (ingMet) Color(0xFF81C784) else Color(0xFFE57373),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
