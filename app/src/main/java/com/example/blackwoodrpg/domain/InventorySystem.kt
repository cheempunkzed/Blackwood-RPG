package com.example.blackwoodrpg.domain

import com.example.blackwoodrpg.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Slot representation in the player inventory.
 */
data class InventorySlot(
    val slotIndex: Int,
    val item: Item?,
    val count: Int = 0
) {
    val isEmpty: Boolean get() = item == null || count <= 0
    val isFull: Boolean get() = item != null && count >= item.maxStack
}

/**
 * InventorySystem manages inventory slots, auto-stacking, ingredient consuming, and slot swapping.
 */
class InventorySystem(
    val capacity: Int = 24
) {
    private val slots = Array(capacity) { index -> InventorySlot(index, null, 0) }

    private val _inventoryState = MutableStateFlow<List<InventorySlot>>(emptyList())
    val inventoryState: StateFlow<List<InventorySlot>> = _inventoryState.asStateFlow()

    init {
        publishState()
    }

    private fun publishState() {
        _inventoryState.value = slots.toList()
    }

    fun getSlotsSnapshot(): List<InventorySlot> = slots.toList()

    /**
     * Add item to inventory with auto-stacking.
     * @return true if item was completely added, false if inventory was full.
     */
    @Synchronized
    fun addItem(item: Item, quantity: Int = 1): Boolean {
        var remaining = quantity

        // 1. Try to stack with existing slots
        for (i in 0 until capacity) {
            val slot = slots[i]
            if (slot.item?.id == item.id && !slot.isFull) {
                val spaceAvailable = item.maxStack - slot.count
                val amountToAdd = spaceAvailable.coerceAtMost(remaining)
                slots[i] = slot.copy(count = slot.count + amountToAdd)
                remaining -= amountToAdd
                if (remaining <= 0) break
            }
        }

        // 2. Fill empty slots
        if (remaining > 0) {
            for (i in 0 until capacity) {
                val slot = slots[i]
                if (slot.isEmpty) {
                    val amountToAdd = item.maxStack.coerceAtMost(remaining)
                    slots[i] = InventorySlot(i, item, amountToAdd)
                    remaining -= amountToAdd
                    if (remaining <= 0) break
                }
            }
        }

        publishState()
        return remaining == 0
    }

    /**
     * Remove item quantity from specific slot.
     */
    @Synchronized
    fun removeItemAt(slotIndex: Int, quantity: Int = 1): Item? {
        if (slotIndex !in 0 until capacity) return null
        val slot = slots[slotIndex]
        if (slot.isEmpty || slot.item == null) return null

        val item = slot.item
        val newCount = slot.count - quantity

        if (newCount <= 0) {
            slots[slotIndex] = InventorySlot(slotIndex, null, 0)
        } else {
            slots[slotIndex] = slot.copy(count = newCount)
        }

        publishState()
        return item
    }

    /**
     * Remove item by ID and quantity across all slots.
     */
    @Synchronized
    fun removeItemById(itemId: String, quantity: Int = 1): Boolean {
        if (getItemCount(itemId) < quantity) return false

        var remainingToRemove = quantity
        for (i in 0 until capacity) {
            val slot = slots[i]
            if (slot.item?.id == itemId) {
                if (slot.count <= remainingToRemove) {
                    remainingToRemove -= slot.count
                    slots[i] = InventorySlot(i, null, 0)
                } else {
                    slots[i] = slot.copy(count = slot.count - remainingToRemove)
                    remainingToRemove = 0
                }
                if (remainingToRemove <= 0) break
            }
        }

        publishState()
        return true
    }

    /**
     * Count total items by ID in inventory.
     */
    fun getItemCount(itemId: String): Int {
        return slots.filter { it.item?.id == itemId }.sumOf { it.count }
    }

    /**
     * Check if inventory contains required ingredients.
     */
    fun hasIngredients(ingredients: Map<String, Int>): Boolean {
        return ingredients.all { (itemId, requiredQty) ->
            getItemCount(itemId) >= requiredQty
        }
    }

    /**
     * Consume item ingredients.
     */
    @Synchronized
    fun consumeIngredients(ingredients: Map<String, Int>): Boolean {
        if (!hasIngredients(ingredients)) return false
        ingredients.forEach { (itemId, requiredQty) ->
            removeItemById(itemId, requiredQty)
        }
        return true
    }

    /**
     * Swap or move items between inventory slots.
     */
    @Synchronized
    fun swapSlots(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in 0 until capacity || toIndex !in 0 until capacity || fromIndex == toIndex) return
        val temp = slots[fromIndex]
        slots[fromIndex] = slots[toIndex].copy(slotIndex = fromIndex)
        slots[toIndex] = temp.copy(slotIndex = toIndex)
        publishState()
    }
}
