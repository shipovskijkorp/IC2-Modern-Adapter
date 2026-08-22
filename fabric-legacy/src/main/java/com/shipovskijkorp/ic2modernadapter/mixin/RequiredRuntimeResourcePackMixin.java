package com.shipovskijkorp.ic2modernadapter.mixin;

import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the BRRP-backed IC2 runtime resource pack behave like an ALWAYS_ENABLED Fabric pack.
 *
 * <p>BRRP intentionally publishes BEFORE_USER packs as ordinary optional pack profiles so they
 * remain visible in the resource-pack screen. IC2MA's runtime pack is different: it contains the
 * models, textures, translations, and other resources required for the registered IC2 content to
 * render correctly, so disabling it would leave the mod in an invalid client state.</p>
 *
 * <p>Only the required flag is overridden. BRRP keeps ownership of the pack's normal BEFORE_USER
 * ordering, which means user-selected resource packs can still override IC2MA visuals.</p>
 */
@Mixin(Pack.class)
abstract class RequiredRuntimeResourcePackMixin {
    private static final String IC2MA_RUNTIME_PACK_ID =
            "ic2_modern_adapter:original_ic2_runtime";

    @Inject(method = "isRequired", at = @At("HEAD"), cancellable = true)
    private void ic2ma$forceRuntimePackRequired(CallbackInfoReturnable<Boolean> cir) {
        Pack self = (Pack) (Object) this;
        if (IC2MA_RUNTIME_PACK_ID.equals(self.getId())) {
            cir.setReturnValue(true);
        }
    }
}
