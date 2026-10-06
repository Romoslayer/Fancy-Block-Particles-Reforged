package hantonik.fbp;

import hantonik.fbp.init.FBPKeyMappings;
import hantonik.fbp.particle.group.FBPParticleGroup;
import hantonik.fbp.screen.FBPOptionsScreen;
import hantonik.fbp.util.FBPConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(FancyBlockParticles.MOD_ID)
public final class FBPForge {
    public FBPForge(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Initializing...");

            FMLClientSetupEvent.getBus(context.getModBusGroup()).addListener(this::clientSetup);

            RegisterKeyMappingsEvent.BUS.addListener(this::onRegisterKeyMappings);
            RegisterClientReloadListenersEvent.BUS.addListener(this::onRegisterClientReloadListeners);
            AddGuiOverlayLayersEvent.BUS.addListener(this::onAddGuiOverlayLayers);

            context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((_, modsScreen) -> new FBPOptionsScreen(modsScreen)));
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Starting client setup...");

        TickEvent.ClientTickEvent.Post.BUS.addListener(this::postClientTick);
        ClientPauseChangeEvent.Post.BUS.addListener(this::postClientPauseChange);
        ScreenEvent.Init.Post.BUS.addListener(this::postScreenInit);
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(this::onClientLoggingIn);

        event.enqueueWork(() -> {
            ParticleEngine.registerParticleGroup(FBPConstants.FBP_PARTICLE_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_PARTICLE_RENDER));
            ParticleEngine.registerParticleGroup(FBPConstants.FBP_TERRAIN_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_TERRAIN_RENDER));
            ParticleEngine.registerParticleGroup(FBPConstants.FBP_ANIMATION_RENDER, engine -> new FBPParticleGroup(engine, FBPConstants.FBP_ANIMATION_RENDER));
        });

        FancyBlockParticles.LOGGER.info(FancyBlockParticles.SETUP_MARKER, "Finished client setup!");
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        FBPKeyMappings.MAPPINGS.forEach(event::register);
    }

    private void onRegisterClientReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) _ -> FancyBlockParticles.CONFIG.load());
    }

    private void onAddGuiOverlayLayers(final AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(Identifier.fromNamespaceAndPath(FancyBlockParticles.MOD_ID, "hud"), (graphics, _) -> FancyBlockParticles.onRenderHud(graphics));
    }

    private void postClientTick(final TickEvent.ClientTickEvent.Post event) {
        FancyBlockParticles.postClientTick(Minecraft.getInstance());
    }

    private void postClientPauseChange(final ClientPauseChangeEvent.Post event) {
        if (event.isPaused())
            FancyBlockParticles.onClientPause(Minecraft.getInstance().gui.screen());
    }

    private void postScreenInit(final ScreenEvent.Init.Post event) {
        FancyBlockParticles.postScreenInit(event.getScreen());
    }

    private void onClientLoggingIn(final ClientPlayerNetworkEvent.LoggingIn event) {
        FancyBlockParticles.onLevelLoad();
    }
}
