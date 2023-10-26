package town.marsh.mcnvim.fabric

import town.marsh.mcnvim.fabriclike.NeovimFabricLike
import net.fabricmc.api.ModInitializer


object NeovimFabric: ModInitializer {
    override fun onInitialize() {
        NeovimFabricLike.init()
    }
}
