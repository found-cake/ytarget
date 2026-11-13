package kr.foundcake.ytarget

import org.bukkit.GameRules
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Loader : JavaPlugin() {
    override fun onEnable() {
        val flagFile = File(dataFolder, "flag.txt")
        if (!flagFile.isFile) {
            dataFolder.mkdirs()
            logger.severe("${flagFile.path} not found. Put the flag in it and restart the server.")
            server.pluginManager.disablePlugin(this)
            return
        }
        YTarget.registerFlag(flagFile.readText().trimEnd('\r', '\n'))

        protectedWorlds()
        registerEvents()

        server.scheduler.runTaskTimer(this, Runnable {
            for (player in YTarget.tick()) {
                logger.info("${player.name} (${player.uniqueId}) solved the challenge")
            }
        }, 1L, 1L)
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onQuit(event: PlayerQuitEvent) {
                YTarget.removeTicks(event.player)
            }

            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            fun onTeleport(event: PlayerTeleportEvent) {
                YTarget.removeTicks(event.player)
            }
        }, this)
    }

    private fun protectedWorlds() {
        for (world in server.worlds) {
            world.setGameRule(GameRules.MOB_GRIEFING, false)
            world.setGameRule(GameRules.TNT_EXPLODES, false)
            world.setGameRule(GameRules.PROJECTILES_CAN_BREAK_BLOCKS, false)
            world.setGameRule(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0)
            world.setGameRule(GameRules.WATER_SOURCE_CONVERSION, false)
            world.setGameRule(GameRules.LAVA_SOURCE_CONVERSION, false)
            world.setGameRule(GameRules.SPAWN_WARDENS, false)
            world.setGameRule(GameRules.RAIDS, false)
        }
    }
}
