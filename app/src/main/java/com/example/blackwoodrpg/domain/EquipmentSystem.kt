package com.example.blackwoodrpg.domain

import com.example.blackwoodrpg.model.EquipmentSlot
import com.example.blackwoodrpg.model.Item
import com.example.blackwoodrpg.model.ItemCatalog
import com.example.blackwoodrpg.model.SkillType
import com.example.blackwoodrpg.model.StatBonus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class EquipResult {
    data class Success(val equippedItem: Item.Equipable, val unequippedPreviousItem: Item.Equipable?) : EquipResult()
    data class RequirementNotMet(val requiredSkill: SkillType, val requiredLevel: Int, val currentLevel: Int) : EquipResult()
    data class InvalidSlot(val message: String) : EquipResult()
}

class EquipmentSystem(
    initialEquipment: Map<EquipmentSlot, Item.Equipable?> = emptyMap()
) {
    private val equipmentMap = mutableMapOf<EquipmentSlot, Item.Equipable?>()

    private val _equipmentState = MutableStateFlow<Map<EquipmentSlot, Item.Equipable?>>(emptyMap())
    val equipmentState: StateFlow<Map<EquipmentSlot, Item.Equipable?>> = _equipmentState.asStateFlow()

    init {
        synchronized(equipmentMap) {
            EquipmentSlot.entries.forEach { slot ->
                equipmentMap[slot] = initialEquipment[slot]
            }
        }
        publishState()
    }

    private fun publishState() {
        synchronized(equipmentMap) {
            _equipmentState.value = equipmentMap.toMap()
        }
    }

    fun getItemInSlot(slot: EquipmentSlot): Item.Equipable? = synchronized(equipmentMap) { equipmentMap[slot] }

    @Synchronized
    fun equipItem(item: Item.Equipable, skillManager: SkillManager): EquipResult {
        val currentSkillLevel = skillManager.getLevel(item.requiredSkill)
        if (currentSkillLevel < item.requiredLevel) {
            return EquipResult.RequirementNotMet(
                requiredSkill = item.requiredSkill,
                requiredLevel = item.requiredLevel,
                currentLevel = currentSkillLevel
            )
        }

        val slot = item.equipmentSlot
        val previousItem = synchronized(equipmentMap) {
            val prev = equipmentMap[slot]
            equipmentMap[slot] = item
            prev
        }
        publishState()

        return EquipResult.Success(equippedItem = item, unequippedPreviousItem = previousItem)
    }

    @Synchronized
    fun unequipSlot(slot: EquipmentSlot): Item.Equipable? {
        val previousItem = synchronized(equipmentMap) {
            val item = equipmentMap[slot] ?: return@synchronized null
            equipmentMap[slot] = null
            item
        }
        if (previousItem != null) {
            publishState()
        }
        return previousItem
    }

    fun getPrimaryTrainedSkill(): SkillType {
        val mainHandItem = synchronized(equipmentMap) { equipmentMap[EquipmentSlot.MAIN_HAND] } ?: return SkillType.SWORDS

        return when (mainHandItem) {
            is Item.Weapon -> mainHandItem.skill
            is Item.Tool -> mainHandItem.skill
            else -> mainHandItem.requiredSkill
        }
    }

    fun getTotalStats(): StatBonus {
        var total = StatBonus()
        val items = synchronized(equipmentMap) { equipmentMap.values.filterNotNull() }
        items.forEach { equipable ->
            total += equipable.stats
        }
        return total
    }

    fun getEquipmentSnapshotJson(): String {
        return synchronized(equipmentMap) {
            equipmentMap.entries.joinToString(";") { "${it.key.name}:${it.value?.id ?: ""}" }
        }
    }

    fun loadEquipmentFromSnapshotJson(json: String) {
        if (json.isBlank()) return
        synchronized(equipmentMap) {
            json.split(";").forEach { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val slot = runCatching { EquipmentSlot.valueOf(parts[0]) }.getOrNull()
                    val itemId = parts[1]
                    val item = if (itemId.isNotEmpty()) ItemCatalog.getItemById(itemId) as? Item.Equipable else null
                    if (slot != null) {
                        equipmentMap[slot] = item
                    }
                }
            }
        }
        publishState()
    }
}
