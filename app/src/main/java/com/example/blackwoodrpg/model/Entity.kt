package com.example.blackwoodrpg.model

/**
 * Isometric 2D Integer Vector Position on map grid.
 */
data class Vector2i(
    val x: Int,
    val y: Int
)

/**
 * Types of action logs.
 */
enum class LogType {
    COMBAT,
    GATHERING,
    CRAFTING,
    LEVEL_UP,
    EQUIPMENT,
    SYSTEM
}

/**
 * Log entry for the real-time activity feed.
 */
data class CombatLog(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val timestampMs: Long = System.currentTimeMillis(),
    val message: String,
    val logType: LogType
)

/**
 * Enemy NPC entity in the isometric wilderness.
 */
data class EnemyEntity(
    val id: String,
    val name: String,
    val iconSymbol: String,
    var currentHp: Int,
    val maxHp: Int,
    val level: Int,
    val defensePower: Int,
    val attackPower: Int,
    var position: Vector2i,
    val expReward: Long,
    val dropItemId: String? = null
) {
    val isDead: Boolean get() = currentHp <= 0
}

/**
 * Interactive mining ore node in isometric space.
 */
data class MiningNode(
    val id: String,
    val name: String,
    val iconSymbol: String,
    var currentOre: Int,
    val maxOre: Int,
    val requiredSkillLevel: Int,
    val oreItemId: String,
    var position: Vector2i
) {
    val isDepleted: Boolean get() = currentOre <= 0
}

/**
 * Floating Combat Text animation instance for rendering damage / xp text over entities in Canvas.
 */
data class FloatingText(
    val id: Long = System.nanoTime(),
    val text: String,
    val colorHex: Long,
    val isoX: Float,
    val isoY: Float,
    var alpha: Float = 1.0f,
    var offsetY: Float = 0.0f
)
