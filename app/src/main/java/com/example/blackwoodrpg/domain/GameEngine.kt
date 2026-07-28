package com.example.blackwoodrpg.domain

import com.example.blackwoodrpg.model.EnemyEntity
import com.example.blackwoodrpg.model.FloatingText
import com.example.blackwoodrpg.model.MiningNode
import com.example.blackwoodrpg.model.Vector2i
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Isometric World Engine governing game entity spawns, ticks, floating texts, and combat cycles.
 */
class GameEngine(
    val player: Player = Player(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    // Entities in world grid
    private val _enemiesState = MutableStateFlow<List<EnemyEntity>>(emptyList())
    val enemiesState: StateFlow<List<EnemyEntity>> = _enemiesState.asStateFlow()

    private val _miningNodesState = MutableStateFlow<List<MiningNode>>(emptyList())
    val miningNodesState: StateFlow<List<MiningNode>> = _miningNodesState.asStateFlow()

    private val _floatingTextsState = MutableStateFlow<List<FloatingText>>(emptyList())
    val floatingTextsState: StateFlow<List<FloatingText>> = _floatingTextsState.asStateFlow()

    private var gameLoopJob: Job? = null

    init {
        seedWorldEntities()
        startGameLoop()
    }

    private fun seedWorldEntities() {
        val initialEnemies = listOf(
            EnemyEntity(
                id = "goblin_1",
                name = "Wild Blackwood Goblin",
                iconSymbol = "👺",
                currentHp = 60,
                maxHp = 60,
                level = 3,
                defensePower = 5,
                attackPower = 12,
                position = Vector2i(7, 4),
                expReward = 50L,
                dropItemId = "iron_ore"
            ),
            EnemyEntity(
                id = "skeleton_1",
                name = "Skeletal Warrior",
                iconSymbol = "💀",
                currentHp = 120,
                maxHp = 120,
                level = 8,
                defensePower = 18,
                attackPower = 28,
                position = Vector2i(3, 8),
                expReward = 140L,
                dropItemId = "steel_bar"
            ),
            EnemyEntity(
                id = "demon_1",
                name = "Darkwood Behemoth",
                iconSymbol = "👹",
                currentHp = 350,
                maxHp = 350,
                level = 20,
                defensePower = 40,
                attackPower = 55,
                position = Vector2i(8, 9),
                expReward = 450L,
                dropItemId = "ancient_ore"
            )
        )

        val initialNodes = listOf(
            MiningNode(
                id = "node_iron_1",
                name = "Iron Ore Vein",
                iconSymbol = "🪨",
                currentOre = 15,
                maxOre = 15,
                requiredSkillLevel = 1,
                oreItemId = "iron_ore",
                position = Vector2i(2, 3)
            ),
            MiningNode(
                id = "node_iron_2",
                name = "Rich Iron Deposit",
                iconSymbol = "⛰️",
                currentOre = 25,
                maxOre = 25,
                requiredSkillLevel = 5,
                oreItemId = "iron_ore",
                position = Vector2i(8, 2)
            ),
            MiningNode(
                id = "node_ancient_1",
                name = "Luminescent Mithril Vein",
                iconSymbol = "💎",
                currentOre = 10,
                maxOre = 10,
                requiredSkillLevel = 10,
                oreItemId = "ancient_ore",
                position = Vector2i(4, 9)
            )
        )

        _enemiesState.value = initialEnemies
        _miningNodesState.value = initialNodes
    }

    /**
     * Ticks isometric animations, floating texts, and periodic entity respawns.
     */
    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = scope.launch {
            var tickCount = 0L
            while (true) {
                delay(50) // 20 FPS game tick cycle
                tickCount++

                // Update floating combat numbers
                updateFloatingTexts()

                // Respawn depleted nodes & dead enemies periodically
                if (tickCount % 100L == 0L) { // Every 5 seconds
                    respawnEntities()
                }
            }
        }
    }

    private fun updateFloatingTexts() {
        val currentList = _floatingTextsState.value
        if (currentList.isEmpty()) return

        val updated = currentList.mapNotNull { ft ->
            ft.alpha -= 0.04f
            ft.offsetY -= 1.8f
            if (ft.alpha <= 0f) null else ft
        }
        _floatingTextsState.value = updated
    }

    private fun spawnFloatingText(text: String, colorHex: Long, isoX: Float, isoY: Float) {
        val ft = FloatingText(
            text = text,
            colorHex = colorHex,
            isoX = isoX,
            isoY = isoY
        )
        _floatingTextsState.value = _floatingTextsState.value + ft
    }

    /**
     * Player attacks enemy at world location or targeted instance.
     */
    fun performAttack(enemy: EnemyEntity) {
        if (enemy.isDead) return

        val result = player.attackEnemy(enemy)

        // Spawn Floating Damage Number
        val color = if (result.isCritical) 0xFFFF3333 else 0xFFFFAA00
        val textStr = if (result.isCritical) "CRIT -${result.damageDealt}" else "-${result.damageDealt}"
        spawnFloatingText(
            text = textStr,
            colorHex = color,
            isoX = enemy.position.x.toFloat(),
            isoY = enemy.position.y.toFloat()
        )

        // Floating XP indicator
        spawnFloatingText(
            text = "+${result.expGained} ${result.trainedSkill.displayName} XP",
            colorHex = 0xFF55FF55,
            isoX = player.position.x.toFloat(),
            isoY = player.position.y.toFloat()
        )

        // Enemy retaliates if still alive
        if (!enemy.isDead) {
            val enemyDmg = player.takeDamage(enemy.attackPower)
            spawnFloatingText(
                text = "-$enemyDmg HP",
                colorHex = 0xFFFF4444,
                isoX = player.position.x.toFloat(),
                isoY = player.position.y.toFloat()
            )
        }

        // Trigger State Flow refresh
        _enemiesState.value = _enemiesState.value.toList()
    }

    /**
     * Player mines ore node.
     */
    fun performMining(node: MiningNode) {
        if (node.isDepleted) return

        val result = player.mineNode(node) ?: return

        spawnFloatingText(
            text = "+${result.oreHarvested} ${result.item.name}",
            colorHex = 0xFF88CCFF,
            isoX = node.position.x.toFloat(),
            isoY = node.position.y.toFloat()
        )

        spawnFloatingText(
            text = "+${result.expGained} Mining XP",
            colorHex = 0xFF55FF55,
            isoX = player.position.x.toFloat(),
            isoY = player.position.y.toFloat()
        )

        _miningNodesState.value = _miningNodesState.value.toList()
    }

    private fun respawnEntities() {
        var updatedEnemies = false
        val enemies = _enemiesState.value.map { e ->
            if (e.isDead && Random.nextFloat() < 0.4f) {
                updatedEnemies = true
                e.copy(currentHp = e.maxHp)
            } else e
        }
        if (updatedEnemies) _enemiesState.value = enemies

        var updatedNodes = false
        val nodes = _miningNodesState.value.map { n ->
            if (n.isDepleted && Random.nextFloat() < 0.4f) {
                updatedNodes = true
                n.copy(currentOre = n.maxOre)
            } else n
        }
        if (updatedNodes) _miningNodesState.value = nodes
    }
}
