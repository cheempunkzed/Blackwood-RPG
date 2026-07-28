package com.example.blackwoodrpg.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.floor
import kotlin.math.pow

/**
 * Skill types supported in Blackwood RPG classless progression system.
 */
enum class SkillType(
    val displayName: String,
    val description: String,
    val iconSymbol: String
) {
    SWORDS("Swords", "Melee blade mastery, increasing melee damage & critical hits", "⚔️"),
    BOWS("Bows", "Ranged marksmanship, granting precision & attack speed", "🏹"),
    HEAVY_ARMOR("Heavy Armor", "Armor mitigation, physical resilience & max health", "🛡️"),
    MINING("Mining", "Prospecting & extracting high-grade metal ores", "⛏️"),
    CRAFTING("Crafting", "Forging Weapons, Armor & Tools at the Anvil", "🔨")
}

/**
 * Holds skill progress data including level, experience, and progress calculations.
 */
data class SkillData(
    val skillType: SkillType,
    var level: Int = 1,
    var currentXp: Long = 0L
) {
    val maxLevel: Int = 99

    /**
     * Exponential XP formula inspired by classic sandbox RPGs (RuneScape / Albion Online).
     */
    fun getRequiredXpForNextLevel(): Long {
        if (level >= maxLevel) return Long.MAX_VALUE
        return calculateRequiredXpForLevel(level + 1)
    }

    /**
     * Get required cumulative XP to reach [targetLevel].
     */
    fun getRequiredXpForLevel(targetLevel: Int): Long {
        return calculateRequiredXpForLevel(targetLevel)
    }

    /**
     * Calculate 0.0f .. 1.0f progress bar ratio for current level.
     */
    fun getXpProgressInCurrentLevel(): Float {
        if (level >= maxLevel) return 1.0f
        val currentLevelXp = calculateRequiredXpForLevel(level)
        val nextLevelXp = calculateRequiredXpForLevel(level + 1)
        val xpInLevel = (currentXp - currentLevelXp).coerceAtLeast(0L)
        val xpNeededInLevel = (nextLevelXp - currentLevelXp).coerceAtLeast(1L)
        return (xpInLevel.toFloat() / xpNeededInLevel.toFloat()).coerceIn(0.0f, 1.0f)
    }

    companion object {
        fun calculateRequiredXpForLevel(targetLevel: Int): Long {
            if (targetLevel <= 1) return 0L
            var totalXp = 0.0
            for (lvl in 1 until targetLevel) {
                totalXp += floor(lvl + 300.0 * 2.0.pow(lvl / 7.0))
            }
            return (totalXp / 4.0).toLong()
        }
    }
}

/**
 * Result returned whenever a skill gains experience.
 */
data class LevelUpResult(
    val skillType: SkillType,
    val oldLevel: Int,
    val newLevel: Int,
    val totalXp: Long,
    val gainedXp: Long
)
