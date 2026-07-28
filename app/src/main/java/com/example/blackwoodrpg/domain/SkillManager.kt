package com.example.blackwoodrpg.domain

import com.example.blackwoodrpg.model.LevelUpResult
import com.example.blackwoodrpg.model.SkillData
import com.example.blackwoodrpg.model.SkillType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Core manager storing and computing skill mastery progression in Blackwood RPG.
 */
class SkillManager(
    initialSkills: Map<SkillType, SkillData>? = null
) {
    private val skillsMap = ConcurrentHashMap<SkillType, SkillData>()

    private val _skillsState = MutableStateFlow<Map<SkillType, SkillData>>(emptyMap())
    val skillsState: StateFlow<Map<SkillType, SkillData>> = _skillsState.asStateFlow()

    init {
        SkillType.entries.forEach { skillType ->
            val data = initialSkills?.get(skillType) ?: SkillData(skillType = skillType, level = 1, currentXp = 0L)
            skillsMap[skillType] = data
        }
        publishState()
    }

    private fun publishState() {
        _skillsState.value = skillsMap.mapValues { entry -> entry.value.copy() }
    }

    fun getLevel(skillType: SkillType): Int {
        return skillsMap[skillType]?.level ?: 1
    }

    fun getSkillData(skillType: SkillType): SkillData {
        return skillsMap[skillType]?.copy() ?: SkillData(skillType)
    }

    fun getTotalLevel(): Int {
        return skillsMap.values.sumOf { it.level }
    }

    fun getTotalXp(): Long {
        return skillsMap.values.sumOf { it.currentXp }
    }

    @Synchronized
    fun addExperience(skillType: SkillType, amount: Long): LevelUpResult? {
        if (amount <= 0L) return null

        val currentData = skillsMap[skillType] ?: SkillData(skillType).also { skillsMap[skillType] = it }
        val oldLevel = currentData.level

        if (oldLevel >= currentData.maxLevel) {
            return null
        }

        currentData.currentXp += amount

        var newLevel = oldLevel
        while (newLevel < currentData.maxLevel) {
            val reqXpNext = currentData.getRequiredXpForLevel(newLevel + 1)
            if (currentData.currentXp >= reqXpNext) {
                newLevel++
            } else {
                break
            }
        }

        currentData.level = newLevel
        publishState()

        return if (newLevel > oldLevel) {
            LevelUpResult(
                skillType = skillType,
                oldLevel = oldLevel,
                newLevel = newLevel,
                totalXp = currentData.currentXp,
                gainedXp = amount
            )
        } else {
            null
        }
    }

    fun getSkillsSnapshotJson(): String {
        return skillsMap.entries.joinToString(";") { "${it.key.name}:${it.value.level}:${it.value.currentXp}" }
    }

    fun loadSkillsFromSnapshotJson(json: String) {
        if (json.isBlank()) return
        json.split(";").forEach { token ->
            val parts = token.split(":")
            if (parts.size == 3) {
                val skillType = runCatching { SkillType.valueOf(parts[0]) }.getOrNull()
                val lvl = parts[1].toIntOrNull() ?: 1
                val xp = parts[2].toLongOrNull() ?: 0L
                if (skillType != null) {
                    skillsMap[skillType] = SkillData(skillType = skillType, level = lvl, currentXp = xp)
                }
            }
        }
        publishState()
    }
}
