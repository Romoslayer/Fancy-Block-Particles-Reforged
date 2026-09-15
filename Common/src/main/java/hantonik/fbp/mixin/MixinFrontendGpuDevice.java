package hantonik.fbp.mixin;

import hantonik.fbp.platform.Services;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Missing sampler with Iris workaround
@Mixin(targets = "com.mojang.renderpearl.frontend.FrontendGpuDevice")
public abstract class MixinFrontendGpuDevice {
    @Final
    @Mutable
    @Shadow
    public static boolean STRICT_VALIDATION;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void init(CallbackInfo callback) {
        try {
            Services.class.getClassLoader().loadClass("net.irisshaders.iris.api.v0.IrisApi");

            STRICT_VALIDATION = false;
        } catch (ClassNotFoundException e) {
            STRICT_VALIDATION = true;
        }

        LogManager.getLogger("FrontendGpuDevice").info("Setting RenderPearl's FrontendGpuDevice.STRICT_VALIDATION to [{}]", STRICT_VALIDATION);
    }
}
