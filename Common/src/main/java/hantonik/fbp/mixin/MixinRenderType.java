package hantonik.fbp.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface MixinRenderType {
    @Invoker("create")
    static RenderType fbp$create(String name, RenderSetup state) {
        throw new AssertionError();
    }
}
