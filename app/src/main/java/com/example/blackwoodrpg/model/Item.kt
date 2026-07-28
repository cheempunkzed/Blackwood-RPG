package com.example.blackwoodrpg.model

/**
 * Equipment Slots available on the player character doll.
 */
enum class EquipmentSlot(val slotName: String) {
    MAIN_HAND("Main Hand"),
    OFF_HAND("Off Hand"),
    HEAD("Head"),
    CHEST("Chest"),
    LEGS("Legs")
}

/**
 * Stat modifiers provided by items & equipment.
 */
data class StatBonus(
    val attackPower: Int = 0,
    val defensePower: Int = 0,
    val attackSpeedBonus: Float = 0f,
    val criticalChanceBonus: Float = 0f,
    val maxHealthBonus: Int = 0,
    val maxStaminaBonus: Int = 0,
    val miningPower: Int = 0
) {
    operator fun plus(other: StatBonus): StatBonus {
        return StatBonus(
            attackPower = this.attackPower + other.attackPower,
            defensePower = this.defensePower + other.defensePower,
            attackSpeedBonus = this.attackSpeedBonus + other.attackSpeedBonus,
            criticalChanceBonus = this.criticalChanceBonus + other.criticalChanceBonus,
            maxHealthBonus = this.maxHealthBonus + other.maxHealthBonus,
            maxStaminaBonus = this.maxStaminaBonus + other.maxStaminaBonus,
            miningPower = this.miningPower + other.miningPower
        )
    }
}

/**
 * Item hierarchy for Blackwood RPG.
 */
sealed class Item(
    open val id: String,
    open val name: String,
    open val description: String,
    open val tier: Int, // Tier 1 to Tier 6
    open val iconSymbol: String,
    open val maxStack: Int = 1
) {
    data class Resource(
        override val id: String,
        override val name: String,
        override val description: String,
        override val tier: Int,
        override val iconSymbol: String,
        override val maxStack: Int = 99,
        val vendorValue: Int = 5
    ) : Item(id, name, description, tier, iconSymbol, maxStack)

    abstract class Equipable(
        override val id: String,
        override val name: String,
        override val description: String,
        override val tier: Int,
        override val iconSymbol: String,
        val equipmentSlot: EquipmentSlot,
        val requiredSkill: SkillType,
        val requiredLevel: Int,
        val stats: StatBonus
    ) : Item(id, name, description, tier, iconSymbol, 1)

    data class Weapon(
        override val id: String,
        override val name: String,
        override val description: String,
        override val tier: Int,
        override val iconSymbol: String,
        val slot: EquipmentSlot = EquipmentSlot.MAIN_HAND,
        val skill: SkillType, // SWORDS or BOWS
        val reqLevel: Int,
        val weaponStats: StatBonus
    ) : Equipable(id, name, description, tier, iconSymbol, slot, skill, reqLevel, weaponStats)

    data class Tool(
        override val id: String,
        override val name: String,
        override val description: String,
        override val tier: Int,
        override val iconSymbol: String,
        val slot: EquipmentSlot = EquipmentSlot.MAIN_HAND,
        val skill: SkillType = SkillType.MINING,
        val reqLevel: Int,
        val toolStats: StatBonus
    ) : Equipable(id, name, description, tier, iconSymbol, slot, skill, reqLevel, toolStats)

    data class Armor(
        override val id: String,
        override val name: String,
        override val description: String,
        override val tier: Int,
        override val iconSymbol: String,
        val slot: EquipmentSlot,
        val skill: SkillType = SkillType.HEAVY_ARMOR,
        val reqLevel: Int,
        val armorStats: StatBonus
    ) : Equipable(id, name, description, tier, iconSymbol, slot, skill, reqLevel, armorStats)
}

/**
 * Recipe definition for Crafting System.
 */
data class CraftRecipe(
    val id: String,
    val resultItem: Item,
    val resultCount: Int = 1,
    val requiredCraftingLevel: Int,
    val ingredients: Map<String, Int>, // Item ID -> Required Quantity
    val xpReward: Long
)

/**
 * Catalog of predefined items & recipes for Blackwood RPG.
 */
object ItemCatalog {
    // Resources
    val IRON_ORE = Item.Resource("iron_ore", "Iron Ore", "Raw iron mineral extracted from rocks", 1, "🪨", 99, 5)
    val STEEL_BAR = Item.Resource("steel_bar", "Steel Bar", "Smelted metal alloy used for forging", 2, "🧱", 99, 15)
    val ANCIENT_ORE = Item.Resource("ancient_ore", "Ancient Mithril Ore", "Rare luminescent ore from deep darkwood caverns", 3, "💎", 99, 45)
    val LEATHER_STRIPS = Item.Resource("leather_strips", "Leather Strips", "Tanned hide used for hilted grips and armor straps", 1, "📜", 99, 8)

    // T1 Weapons
    val NOVICE_SWORD = Item.Weapon(
        id = "novice_sword",
        name = "Iron Broadsword",
        description = "Standard double-edged blade. Requires Swords Lv 1.",
        tier = 1,
        iconSymbol = "🗡️",
        skill = SkillType.SWORDS,
        reqLevel = 1,
        weaponStats = StatBonus(attackPower = 15, attackSpeedBonus = 0.1f)
    )

    val SHORTBOW = Item.Weapon(
        id = "shortbow",
        name = "Huntsman Shortbow",
        description = "Flexible wooden bow. Requires Bows Lv 1.",
        tier = 1,
        iconSymbol = "🏹",
        skill = SkillType.BOWS,
        reqLevel = 1,
        weaponStats = StatBonus(attackPower = 12, attackSpeedBonus = 0.25f, criticalChanceBonus = 0.05f)
    )

    val NOVICE_PICKAXE = Item.Tool(
        id = "novice_pickaxe",
        name = "Rusty Pickaxe",
        description = "Basic mining tool. Requires Mining Lv 1.",
        tier = 1,
        iconSymbol = "⛏️",
        skill = SkillType.MINING,
        reqLevel = 1,
        toolStats = StatBonus(miningPower = 10, attackPower = 5)
    )

    // T2 Weapons & Gear
    val STEEL_GREATSWORD = Item.Weapon(
        id = "steel_greatsword",
        name = "Steel Greatsword",
        description = "Heavy two-handed sword. Requires Swords Lv 10.",
        tier = 2,
        iconSymbol = "⚔️",
        skill = SkillType.SWORDS,
        reqLevel = 10,
        weaponStats = StatBonus(attackPower = 45, criticalChanceBonus = 0.12f)
    )

    val COMPOSITION_BOW = Item.Weapon(
        id = "composition_bow",
        name = "Blackwood Longbow",
        description = "Precision long-range sniper bow. Requires Bows Lv 10.",
        tier = 2,
        iconSymbol = "🎯",
        skill = SkillType.BOWS,
        reqLevel = 10,
        weaponStats = StatBonus(attackPower = 38, attackSpeedBonus = 0.35f, criticalChanceBonus = 0.18f)
    )

    val STEEL_PICKAXE = Item.Tool(
        id = "steel_pickaxe",
        name = "Tempered Pickaxe",
        description = "Efficient tool for deep ore veins. Requires Mining Lv 10.",
        tier = 2,
        iconSymbol = "⛏️",
        skill = SkillType.MINING,
        reqLevel = 10,
        toolStats = StatBonus(miningPower = 35, attackPower = 12)
    )

    // T3 Weapons
    val OBSIDIAN_DEATHBLADE = Item.Weapon(
        id = "obsidian_deathblade",
        name = "Obsidian Deathblade",
        description = "Razor-sharp demonic sword forged in volcano ash. Requires Swords Lv 25.",
        tier = 3,
        iconSymbol = "🗡️",
        skill = SkillType.SWORDS,
        reqLevel = 25,
        weaponStats = StatBonus(attackPower = 110, criticalChanceBonus = 0.25f)
    )

    // Armors
    val HEAVY_IRON_HELM = Item.Armor(
        id = "heavy_iron_helm",
        name = "Iron Knight Helm",
        description = "Visored helm protecting against lethal blows. Requires Heavy Armor Lv 5.",
        tier = 1,
        iconSymbol = "🪖",
        slot = EquipmentSlot.HEAD,
        reqLevel = 5,
        armorStats = StatBonus(defensePower = 15, maxHealthBonus = 25)
    )

    val HEAVY_IRON_CUIRASS = Item.Armor(
        id = "heavy_iron_cuirass",
        name = "Iron Plate Cuirass",
        description = "Solid iron chest armor protecting the torso. Requires Heavy Armor Lv 10.",
        tier = 2,
        iconSymbol = "🛡️",
        slot = EquipmentSlot.CHEST,
        reqLevel = 10,
        armorStats = StatBonus(defensePower = 40, maxHealthBonus = 80)
    )

    val HEAVY_IRON_GREAVES = Item.Armor(
        id = "heavy_iron_greaves",
        name = "Iron Greaves",
        description = "Reinforced leg guards. Requires Heavy Armor Lv 5.",
        tier = 1,
        iconSymbol = "🦵",
        slot = EquipmentSlot.LEGS,
        reqLevel = 5,
        armorStats = StatBonus(defensePower = 18, maxHealthBonus = 30)
    )

    val STEEL_TOWER_SHIELD = Item.Armor(
        id = "steel_tower_shield",
        name = "Steel Tower Shield",
        description = "Massive off-hand shield for blocking. Requires Heavy Armor Lv 15.",
        tier = 2,
        iconSymbol = "🛡️",
        slot = EquipmentSlot.OFF_HAND,
        reqLevel = 15,
        armorStats = StatBonus(defensePower = 55, maxHealthBonus = 100)
    )

    val ALL_ITEMS: List<Item> = listOf(
        IRON_ORE, STEEL_BAR, ANCIENT_ORE, LEATHER_STRIPS,
        NOVICE_SWORD, SHORTBOW, NOVICE_PICKAXE,
        STEEL_GREATSWORD, COMPOSITION_BOW, STEEL_PICKAXE, OBSIDIAN_DEATHBLADE,
        HEAVY_IRON_HELM, HEAVY_IRON_CUIRASS, HEAVY_IRON_GREAVES, STEEL_TOWER_SHIELD
    )

    fun getItemById(id: String): Item? = ALL_ITEMS.firstOrNull { it.id == id }

    val RECIPES: List<CraftRecipe> = listOf(
        CraftRecipe(
            id = "craft_steel_bar",
            resultItem = STEEL_BAR,
            resultCount = 2,
            requiredCraftingLevel = 1,
            ingredients = mapOf("iron_ore" to 3),
            xpReward = 45L
        ),
        CraftRecipe(
            id = "craft_steel_greatsword",
            resultItem = STEEL_GREATSWORD,
            resultCount = 1,
            requiredCraftingLevel = 5,
            ingredients = mapOf("steel_bar" to 4, "leather_strips" to 2),
            xpReward = 150L
        ),
        CraftRecipe(
            id = "craft_steel_pickaxe",
            resultItem = STEEL_PICKAXE,
            resultCount = 1,
            requiredCraftingLevel = 8,
            ingredients = mapOf("steel_bar" to 3, "iron_ore" to 2),
            xpReward = 200L
        ),
        CraftRecipe(
            id = "craft_composition_bow",
            resultItem = COMPOSITION_BOW,
            resultCount = 1,
            requiredCraftingLevel = 10,
            ingredients = mapOf("leather_strips" to 5, "steel_bar" to 2),
            xpReward = 250L
        ),
        CraftRecipe(
            id = "craft_heavy_cuirass",
            resultItem = HEAVY_IRON_CUIRASS,
            resultCount = 1,
            requiredCraftingLevel = 12,
            ingredients = mapOf("steel_bar" to 6, "leather_strips" to 4),
            xpReward = 350L
        ),
        CraftRecipe(
            id = "craft_obsidian_deathblade",
            resultItem = OBSIDIAN_DEATHBLADE,
            resultCount = 1,
            requiredCraftingLevel = 25,
            ingredients = mapOf("ancient_ore" to 8, "steel_bar" to 10),
            xpReward = 1200L
        )
    )
}
