package town.marsh.mcnvim.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.msgpack.value.ArrayValue;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import town.marsh.mcnvim.Neovim;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends AbstractWidget {

    @Shadow private int cursorPos;

    public MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Redirect(method = "*", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/components/EditBox;value:Ljava/lang/String;"))
    private String getValue(EditBox instance) {
        return Neovim.INSTANCE.getClient().getCurrentLine();
    }

    @Redirect(method = "*", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/components/EditBox;value:Ljava/lang/String;"))
    private void setValue(EditBox instance, String value) {
        Neovim.INSTANCE.getClient().setCurrentLine(value);
    }

    @Redirect(method = "*", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/components/EditBox;cursorPos:I"))
    private int getCursorPosition(EditBox instance) {
        return Neovim.INSTANCE.getClient().winGetCursor(0).get(1).asIntegerValue().asInt();
    }

    @Redirect(method = "*", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/gui/components/EditBox;cursorPos:I"))
    private void setCursorPosition(EditBox instance, int column) {
        Neovim.INSTANCE.getClient().winSetCursor(0, column);
    }

    @Redirect(method = "*", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/client/gui/components/EditBox;highlightPos:I"))
    private int getHighlightPos(EditBox instance) {
        ArrayValue result = Neovim.INSTANCE.getClient().bufGetMark(0, "'>");

        if (result == null) {
            return this.getCursorPosition(instance);
        }

        return result.get(1).asIntegerValue().asInt();
    }

    @Inject(at = @At("HEAD"), method = "charTyped", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private void keyPressed(char c, int i, CallbackInfoReturnable<Boolean> cir) {
        if (Screen.hasShiftDown()) {
            Neovim.INSTANCE.getClient().input("<S-" + c);
        } else if (Screen.hasControlDown()) {
            Neovim.INSTANCE.getClient().input("<C-" + c);
        } else if (Screen.hasAltDown()) {
            Neovim.INSTANCE.getClient().input("<M-" + c);
        } else {
            Neovim.INSTANCE.getClient().feedKeys(String.valueOf(c), "n", true);
        }

        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    private void keyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (i == 257) {
            // Enter
            return;
        }

        Neovim.INSTANCE.getClient().feedIntChar(i);
        cir.setReturnValue(true);
        cir.cancel();
    }
}
