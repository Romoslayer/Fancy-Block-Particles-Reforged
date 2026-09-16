package hantonik.fbp.particle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FBPFlameType {
    NORMAL(1.0F, 1.0F, 0.0F),
    SOUL(0.2F, 1.0F, 0.9F),
    COPPER(0.6F, 1.0F, 0.6F);

    private final float red;
    private final float green;
    private final float blue;

    @Nullable
    public static FBPFlameType byParticle(ParticleType<?> type) {
        if (type == ParticleTypes.FLAME)
            return NORMAL;

        if (type == ParticleTypes.SOUL_FIRE_FLAME)
            return SOUL;

        if (type == ParticleTypes.COPPER_FIRE_FLAME)
            return COPPER;

        return null;
    }
}
