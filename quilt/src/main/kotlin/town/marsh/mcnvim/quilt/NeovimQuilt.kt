package town.marsh.mcnvim.quilt

import town.marsh.mcnvim.fabriclike.NeovimFabricLike
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

object NeovimQuilt: ModInitializer {
    override fun onInitialize(mod: ModContainer?) {
        NeovimFabricLike.init()
    }
}