package hantonik.fbp.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import hantonik.fbp.FancyBlockParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// FBP's own rain/snow particles are spawned from MixinClientLevel.tickWeatherEffects
@Mixin(WeatherEffectRenderer.class)
public abstract class MixinWeatherEffectRenderer {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getPrecipitationAt(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/biome/Biome$Precipitation;"), method = "extractRenderState")
    private Biome.Precipitation getPrecipitationAt(ClientLevel level, BlockPos pos, Operation<Biome.Precipitation> original) {
        var precipitation = original.call(level, pos);

        if (FancyBlockParticles.CONFIG.global.isEnabled()) {
            if (precipitation == Biome.Precipitation.RAIN && FancyBlockParticles.CONFIG.rain.isEnabled())
                return Biome.Precipitation.NONE;

            if (precipitation == Biome.Precipitation.SNOW && FancyBlockParticles.CONFIG.snow.isEnabled())
                return Biome.Precipitation.NONE;
        }

        return precipitation;
    }
}
