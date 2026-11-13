package kr.foundcake.ytarget

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.security.MessageDigest
import java.util.HexFormat
import java.util.Locale

object YTarget {
    const val TARGET_MD5 = "c7bd83421cc70961937a7cf680c53061"
    const val DECIMALS = 5
    const val REQUIRED_TICKS = 4
    const val CLEAR_RADIUS = 8

    private lateinit var flag: String

    /**
     * - **Key**: player entity ID
     * - **Value**: 타겟 Y좌표에 있는 tick
     */
    private val consecutiveTargetTicks = HashMap<Int, Int>()

    fun registerFlag(flag: String) {
        this.flag = flag
    }

    fun removeTicks(player: Player) {
        consecutiveTargetTicks.remove(player.entityId)
    }

    fun tick(): List<Player> {
        val solved = ArrayList<Player>()
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.gameMode != GameMode.SURVIVAL || player.world.environment != World.Environment.NORMAL) {
                continue
            }
            if (!isTarget(player.location.y)) {
                consecutiveTargetTicks.remove(player.entityId)
                continue
            }
            val ticks = consecutiveTargetTicks.merge(player.entityId, 1) { old, one -> old + one }!!
            if (ticks >= REQUIRED_TICKS) {
                solve(player)
                solved += player
            }
        }
        return solved
    }

    fun isTarget(y: Double): Boolean {
        val text = String.format(Locale.ROOT, "%.${DECIMALS}f", y)
        val digest = MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.US_ASCII))
        return HexFormat.of().formatHex(digest) == TARGET_MD5
    }

    fun solve(player: Player) {
        removeTicks(player)
        val location = player.location

        player.inventory.clear()
        player.setItemOnCursor(null)
        player.gameMode = GameMode.SPECTATOR
        removePartialBlocks(player.world, location.blockX, location.blockY, location.blockZ)
        removeItemEntities(location)

        player.inventory.addItem(flagItem())
        player.sendMessage(
            Component.text()
                .text("FLAG: ", NamedTextColor.GREEN)
                .text(flag, NamedTextColor.GOLD)
                .build()
        )
    }

    private fun removeItemEntities(center: Location) {
        val radius = CLEAR_RADIUS.toDouble()
        center.world.getNearbyEntities(center, radius, radius, radius) { it is Item }.forEach { it.remove() }
    }

    private fun removePartialBlocks(world: World, centerX: Int, centerY: Int, centerZ: Int) {
        val minY = (centerY - CLEAR_RADIUS).coerceAtLeast(world.minHeight)
        val maxY = (centerY + CLEAR_RADIUS).coerceAtMost(world.maxHeight - 1)
        for (x in centerX - CLEAR_RADIUS..centerX + CLEAR_RADIUS) {
            for (y in minY..maxY) {
                for (z in centerZ - CLEAR_RADIUS..centerZ + CLEAR_RADIUS) {
                    val block = world.getBlockAt(x, y, z)
                    if (block.type == Material.LADDER) {
                        block.type = Material.AIR
                        continue
                    }
                    val top = block.collisionShape.boundingBoxes.maxOfOrNull { it.maxY } ?: continue
                    if (top < 1.0) block.type = Material.AIR
                }
            }
        }
    }

    private fun flagItem() = ItemStack(Material.FLINT).apply {
        editMeta { it.displayName(Component.text(flag).decoration(TextDecoration.ITALIC, false)) }
    }
}
