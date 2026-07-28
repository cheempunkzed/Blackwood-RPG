package com.example.blackwoodrpg.domain

import com.example.blackwoodrpg.model.CombatLog
import com.example.blackwoodrpg.model.CraftRecipe
import com.example.blackwoodrpg.model.EnemyEntity
import com.example.blackwoodrpg.model.EquipmentSlot
import com.example.blackwoodrpg.model.Item
import com.example.blackwoodrpg.model.ItemCatalog
import com.example.blackwoodrpg.model.LevelUpResult
import com.example.blackwoodrpg.model.LogType
import com.example.blackwoodrpg.model.MiningNode
import com.example.blackwoodrpg.model.SkillType
import com.example.blackwoodrpg.model.Vector2i
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Result of an attack action against an enemy.
 */
data class AttackResult(
    val damageDealt: Int,
    val isCritical: Boolean,
    val enemyKilled: Boolean,
    val expGained: Long,
    val trainedSkill: SkillType,
    val levelUp: LevelUpResult?
)

/**
 * Result of a mining node interaction.
 */
data class MiningResult(
    val oreHarvested: Int,
    val item: Item.Resource,
    val expGained: Long,
    val nodeDepleted: Boolean,
    val levelUp: LevelUpResult?
)

/**
 * Result of a crafting action.
 */
data class CraftResult(
    val success: Boolean,
    val craftedItem: Item?,
    val expGained: Long,
    val message: String,
    val levelUp: LevelUpResult?
)

/**
 * Core Player Entity class holding attributes, inventory, skills, equipment, and combat mechanics.
 */
class Player(
    val id: String = "hero_player",
    var name: String = "Hero of Blackwood",
    val skillManager: SkillManager = SkillManager(),
    val equipmentSystem: EquipmentSystem = EquipmentSystem(),
    val inventorySystem: InventorySystem = InventorySystem(capacity = 24),
    initialPosition: Vector2i = Vector2i(5, 5)
) {
    var position: Vector2i = initialPosition

    val baseMaxHp: Int = 100
    val baseMaxStamina: Int = 100

    private val _currentHp = MutableStateFlow(100)
    val healthState: StateFlow<Int> = _currentHp.asStateFlow()
    val currentHp: StateFlow<Int> = _currentHp.asStateFlow()

    private val _currentStamina = MutableStateFlow(100)
    val staminaState: StateFlow<Int> = _currentStamina.asStateFlow()
    val currentStamina: StateFlow<Int> = _currentStamina.asStateFlow()

    // Activity / Combat Log Stream
    private val _logsState = MutableStateFlow<List<CombatLog>>(emptyList())
    val logsState: StateFlow<List<CombatLog>> = _logsState.asStateFlow()

    init {
        // Equip starting gear
        inventorySystem.addItem(ItemCatalog.NOVICE_SWORD)
        inventorySystem.addItem(ItemCatalog.SHORTBOW)
        inventorySystem.addItem(ItemCatalog.NOVICE_PICKAXE)
        inventorySystem.addItem(ItemCatalog.IRON_ORE, 15)

        equipmentSystem.equipItem(ItemCatalog.NOVICE_SWORD, skillManager)

        addLog("Welcome to Blackwood RPG! Classless sandbox system online.", LogType.SYSTEM)
    }

    fun getMaxHp(): Int {
        val armorLevel = skillManager.getLevel(SkillType.HEAVY_ARMOR)
        val equipHpBonus = equipmentSystem.getTotalStats().maxHealthBonus
        return baseMaxHp + (armorLevel * 8) + equipHpBonus
    }

    fun getMaxStamina(): Int {
        val equipStaminaBonus = equipmentSystem.getTotalStats().maxStaminaBonus
        return baseMaxStamina + equipStaminaBonus
    }

    fun getEffectiveAttackPower(): Int {
        val activeSkill = equipmentSystem.getPrimaryTrainedSkill()
        val skillLevel = skillManager.getLevel(activeSkill)
        val weaponBonus = equipmentSystem.getTotalStats().attackPower
        return 10 + (skillLevel * 3) + weaponBonus
    }

    fun getEffectiveDefensePower(): Int {
        val heavyArmorLevel = skillManager.getLevel(SkillType.HEAVY_ARMOR)
        val armorBonus = equipmentSystem.getTotalStats().defensePower
        return (heavyArmorLevel * 2) + armorBonus
    }

    fun modifyHealth(delta: Int) {
        val maxHp = getMaxHp()
        _currentHp.value = (_currentHp.value + delta).coerceIn(0, maxHp)
    }

    fun modifyStamina(delta: Int): Boolean {
        if (delta < 0 && _currentStamina.value < -delta) return false
        val maxStam = getMaxStamina()
        _currentStamina.value = (_currentStamina.value + delta).coerceIn(0, maxStam)
        return true
    }

    fun takeDamage(rawDamage: Int): Int {
        val defense = getEffectiveDefensePower()
        val actualDamage = (rawDamage - (defense * 0.4)).roundToInt().coerceAtLeast(1)

        modifyHealth(-actualDamage)
        addLog("Took $actualDamage damage!", LogType.COMBAT)

        // Heavy Armor gains defensive passive XP when damaged
        val levelUp = skillManager.addExperience(SkillType.HEAVY_ARMOR, (actualDamage * 2).toLong())
        levelUp?.let { notifyLevelUp(it) }

        if (_currentHp.value <= 0) {
            addLog("You fell in battle! Respawning with full health...", LogType.SYSTEM)
            modifyHealth(getMaxHp())
        }

        return actualDamage
    }

    /**
     * Execute attack against enemy entity.
     * Core Requirement: Classless progression - XP goes directly to the skill tied to equipped weapon.
     */
    fun attackEnemy(enemy: EnemyEntity): AttackResult {
        val trainedSkill = equipmentSystem.getPrimaryTrainedSkill()
        val attackPower = getEffectiveAttackPower()
        val stats = equipmentSystem.getTotalStats()

        val critChance = 0.05f + stats.criticalChanceBonus
        val isCrit = Random.nextFloat() < critChance
        val critMultiplier = if (isCrit) 1.75f else 1.0f

        val rawDamage = (attackPower * critMultiplier * Random.nextDouble(0.85, 1.15)).roundToInt()
        val damageDealt = (rawDamage - (enemy.defensePower * 0.3)).roundToInt().coerceAtLeast(1)

        enemy.currentHp = (enemy.currentHp - damageDealt).coerceAtLeast(0)

        val xpGained = (damageDealt * 1.5).toLong().coerceAtLeast(10L)
        val levelUp = skillManager.addExperience(trainedSkill, xpGained)

        val isDead = enemy.isDead

        if (isCrit) {
            addLog("CRITICAL HIT! Dealt $damageDealt damage to ${enemy.name}", LogType.COMBAT)
        } else {
            addLog("Dealt $damageDealt damage to ${enemy.name}", LogType.COMBAT)
        }

        if (levelUp != null) notifyLevelUp(levelUp)

        if (isDead) {
            addLog("Defeated ${enemy.name}! Gained +${enemy.expReward} bonus XP.", LogType.COMBAT)
            val bonusLevelUp = skillManager.addExperience(trainedSkill, enemy.expReward)
            if (bonusLevelUp != null) notifyLevelUp(bonusLevelUp)

            enemy.dropItemId?.let { itemId ->
                ItemCatalog.getItemById(itemId)?.let { item ->
                    inventorySystem.addItem(item, 1)
                    addLog("Looted ${item.name} (${item.iconSymbol})", LogType.GATHERING)
                }
            }
        }

        return AttackResult(
            damageDealt = damageDealt,
            isCritical = isCrit,
            enemyKilled = isDead,
            expGained = xpGained,
            trainedSkill = trainedSkill,
            levelUp = levelUp
        )
    }

    /**
     * Mine Ore Node logic. Earns MINING XP and harvests minerals into inventory.
     */
    fun mineNode(node: MiningNode): MiningResult? {
        val miningSkillLevel = skillManager.getLevel(SkillType.MINING)
        if (miningSkillLevel < node.requiredSkillLevel) {
            addLog("Mining level ${node.requiredSkillLevel} required for ${node.name}!", LogType.GATHERING)
            return null
        }

        val toolStats = equipmentSystem.getTotalStats()
        val miningPower = (toolStats.miningPower + miningSkillLevel * 2).coerceAtLeast(5)

        val oreHarvested = (miningPower / 10).coerceAtLeast(1).coerceAtMost(node.currentOre)
        node.currentOre -= oreHarvested

        val oreItem = ItemCatalog.getItemById(node.oreItemId) as? Item.Resource
            ?: ItemCatalog.IRON_ORE

        inventorySystem.addItem(oreItem, oreHarvested)

        val xpGained = (oreHarvested * 25L * oreItem.tier)
        val levelUp = skillManager.addExperience(SkillType.MINING, xpGained)

        addLog("Mined $oreHarvested x ${oreItem.name} (${oreItem.iconSymbol}). +$xpGained Mining XP", LogType.GATHERING)
        if (levelUp != null) notifyLevelUp(levelUp)

        return MiningResult(
            oreHarvested = oreHarvested,
            item = oreItem,
            expGained = xpGained,
            nodeDepleted = node.isDepleted,
            levelUp = levelUp
        )
    }

    /**
     * Craft item recipe at anvil/workbench. Earns CRAFTING XP.
     */
    fun craftItem(recipe: CraftRecipe): CraftResult {
        val craftingLevel = skillManager.getLevel(SkillType.CRAFTING)
        if (craftingLevel < recipe.requiredCraftingLevel) {
            val msg = "Crafting level ${recipe.requiredCraftingLevel} required!"
            addLog(msg, LogType.CRAFTING)
            return CraftResult(false, null, 0L, msg, null)
        }

        if (!inventorySystem.hasIngredients(recipe.ingredients)) {
            val msg = "Missing required crafting resources!"
            addLog(msg, LogType.CRAFTING)
            return CraftResult(false, null, 0L, msg, null)
        }

        inventorySystem.consumeIngredients(recipe.ingredients)
        inventorySystem.addItem(recipe.resultItem, recipe.resultCount)

        val levelUp = skillManager.addExperience(SkillType.CRAFTING, recipe.xpReward)
        val msg = "Forged ${recipe.resultCount}x ${recipe.resultItem.name} (${recipe.resultItem.iconSymbol})!"
        addLog(msg, LogType.CRAFTING)

        if (levelUp != null) notifyLevelUp(levelUp)

        return CraftResult(
            success = true,
            craftedItem = recipe.resultItem,
            expGained = recipe.xpReward,
            message = msg,
            levelUp = levelUp
        )
    }

    fun equipFromInventory(slotIndex: Int) {
        val slot = inventorySystem.inventoryState.value.getOrNull(slotIndex) ?: return
        val item = slot.item as? Item.Equipable ?: return

        val result = equipmentSystem.equipItem(item, skillManager)
        when (result) {
            is com.example.blackwoodrpg.domain.EquipResult.Success -> {
                inventorySystem.removeItemAt(slotIndex, 1)
                result.unequippedPreviousItem?.let { unequipped ->
                    inventorySystem.addItem(unequipped)
                }
                addLog("Equipped ${item.name} into ${item.equipmentSlot.slotName}", LogType.EQUIPMENT)
            }
            is com.example.blackwoodrpg.domain.EquipResult.RequirementNotMet -> {
                addLog("Cannot equip ${item.name}! Requires ${result.requiredSkill.displayName} Level ${result.requiredLevel} (Your Lv: ${result.currentLevel})", LogType.EQUIPMENT)
            }
            is com.example.blackwoodrpg.domain.EquipResult.InvalidSlot -> {
                addLog("Invalid slot: ${result.message}", LogType.EQUIPMENT)
            }
        }
    }

    fun unequipToInventory(slot: EquipmentSlot) {
        val item = equipmentSystem.getItemInSlot(slot) ?: return
        if (equipmentSystem.unequipSlot(slot) != null) {
            inventorySystem.addItem(item)
            addLog("Unequipped ${item.name}", LogType.EQUIPMENT)
        }
    }

    private fun notifyLevelUp(levelUp: LevelUpResult) {
        val msg = "🎉 LEVEL UP! ${levelUp.skillType.displayName} is now Level ${levelUp.newLevel}!"
        addLog(msg, LogType.LEVEL_UP)
    }

    fun addLog(message: String, type: LogType) {
        val log = CombatLog(message = message, logType = type)
        val current = _logsState.value.toMutableList()
        if (current.size >= 50) current.removeAt(0)
        current.add(log)
        _logsState.value = current
    }
}
