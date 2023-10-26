package town.marsh.mcnvim.mixin;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import town.marsh.mcnvim.Neovim;

@Mixin(Screen.class)
public abstract class MixinScreen implements ContainerEventHandler {
    @Inject(at = @At("HEAD"), method = "shouldCloseOnEsc", cancellable = true)
    private void shouldCloseOnEscape(CallbackInfoReturnable<Boolean> cir) {
        String mode = Neovim.INSTANCE.getClient().getMode();

        Neovim.INSTANCE.getLOGGER().info(mode);

        cir.setReturnValue(mode.equals("n"));
        cir.cancel();
    }
}
