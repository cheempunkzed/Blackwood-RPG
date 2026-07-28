package com.example.blackwoodrpg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackwoodrpg.model.CraftRecipe
import com.example.blackwoodrpg.model.ItemCatalog

/**
 * Anvil Forging & Crafting UI Panel.
 */
@Composable
fun CraftingPanel(
    currentCraftingLevel: Int,
    recipes: List<CraftRecipe> = ItemCatalog.RECIPES,
    onCraftClicked: (CraftRecipe) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1915))
            .border(1.dp, Color(0xFF3B2E24), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🔨 Blackwood Anvil & Forge",
                    color = Color(0xFFE8C180),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Craft gear & bars to raise Crafting Skill",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "Crafting Lv. $currentCraftingLevel",
                color = Color(0xFFFFD700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recipes, key = { it.id }) { recipe ->
                RecipeCard(
                    recipe = recipe,
                    playerCraftingLevel = currentCraftingLevel,
                    onCraftClicked = { onCraftClicked(recipe) }
                )
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: CraftRecipe,
    playerCraftingLevel: Int,
    onCraftClicked: () -> Unit
) {
    val isLevelMet = playerCraftingLevel >= recipe.requiredCraftingLevel

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282019)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = recipe.resultItem.iconSymbol, fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp))
                Column {
                    Text(
                        text = "${recipe.resultItem.name} x${recipe.resultCount}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Req: Crafting Lv. ${recipe.requiredCraftingLevel} | +${recipe.xpReward} XP",
                        color = if (isLevelMet) Color(0xFF88FF88) else Color(0xFFFF6666),
                        fontSize = 11.sp
                    )

                    // Ingredients
                    val ingStr = recipe.ingredients.map { (id, qty) ->
                        val item = ItemCatalog.getItemById(id)
                        "${item?.iconSymbol ?: ""} ${item?.name ?: id} x$qty"
                    }.joinToString(", ")

                    Text(
                        text = "Materials: $ingStr",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onCraftClicked,
                enabled = isLevelMet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4932B),
                    contentColor = Color.Black,
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(text = "Craft", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
