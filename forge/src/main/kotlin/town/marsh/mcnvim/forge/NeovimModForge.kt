package town.marsh.mcnvim.forge

import dev.architectury.platform.forge.EventBuses
import town.marsh.mcnvim.Neovim
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Neovim.MOD_ID)
object NeovimModForge {
    init {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Neovim.MOD_ID, MOD_BUS)
        Neovim.init()
    }
}