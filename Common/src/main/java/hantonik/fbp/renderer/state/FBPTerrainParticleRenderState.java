package hantonik.fbp.renderer.state;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hantonik.fbp.mixin.MixinRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FBPTerrainParticleRenderState implements ParticleGroupRenderState {
    // Vanilla no longer exposes a public RenderType for the particle vertex format/pipelines (that's private to
    // QuadParticleFeatureRenderer), so we build our own via the same RenderPipelines particles normally use.
    private static final Map<SingleQuadParticle.Layer, RenderType> RENDER_TYPES = Maps.newHashMap();

    private final Map<SingleQuadParticle.Layer, Storage> particles = Maps.newHashMap();

    private int particleCount;

    public void add(SingleQuadParticle.Layer layer, float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light) {
        this.particles.computeIfAbsent(layer, _ -> new Storage()).add(posX, posY, posZ, rotX, rotY, rotZ, rotW, widthScale, heightScale, u0, u1, v0, v1, color, light);

        this.particleCount++;
    }

    @Override
    public void clear() {
        this.particles.values().forEach(Storage::clear);

        this.particleCount = 0;
    }

    @Override
    public void submit(SubmitNodeCollector nodeCollector, CameraRenderState state) {
        if (this.particleCount == 0)
            return;

        var poseStack = new PoseStack();

        for (var entry : this.particles.entrySet()) {
            if (entry.getValue().count() == 0)
                continue;

            var renderType = getRenderType(entry.getKey());
            var storage = entry.getValue();

            nodeCollector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> storage.forEachParticle((posX, posY, posZ, rotX, rotY, rotZ, rotW, widthScale, heightScale, u0, u1, v0, v1, color, light) -> this.renderRotatedQuad(consumer, posX, posY, posZ, rotX, rotY, rotZ, rotW, widthScale, heightScale, u0, u1, v0, v1, color, light)));
        }
    }

    private static RenderType getRenderType(SingleQuadParticle.Layer layer) {
        return RENDER_TYPES.computeIfAbsent(layer, key -> {
            var setup = RenderSetup.builder(key.pipeline()).withTexture("Sampler0", key.textureAtlasLocation()).useLightmap().createRenderSetup();

            return MixinRenderType.fbp$create("fbp_terrain_particle_" + (key.translucent() ? "translucent" : "opaque") + "_" + key.textureAtlasLocation().getPath(), setup);
        });
    }

    private void renderRotatedQuad(VertexConsumer consumer, float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light) {
        var rotation = new Quaternionf(rotX, rotY, rotZ, rotW);

        this.renderVertex(consumer, rotation, posX, posY, posZ, 1.0F, -1.0F, widthScale, heightScale, u1, v1, color, light);
        this.renderVertex(consumer, rotation, posX, posY, posZ, 1.0F, 1.0F, widthScale, heightScale, u1, v0, color, light);
        this.renderVertex(consumer, rotation, posX, posY, posZ, -1.0F, 1.0F, widthScale, heightScale, u0, v0, color, light);
        this.renderVertex(consumer, rotation, posX, posY, posZ, -1.0F, -1.0F, widthScale, heightScale, u0, v1, color, light);
    }

    private void renderVertex(VertexConsumer consumer, Quaternionf rotation, float posX, float posY, float posZ, float x, float y, float widthScale, float heightScale, float u, float v, int color, int light) {
        var vertexPos = new Vector3f(x, y, 0.0F).rotate(rotation).mul(widthScale, heightScale, widthScale).add(posX, posY, posZ);

        consumer.addVertex(vertexPos.x(), vertexPos.y(), vertexPos.z()).setUv(u, v).setColor(color).setLight(light);
    }

    @FunctionalInterface
    public interface ParticleConsumer {
        void consume(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light);
    }

    static class Storage {
        private int capacity = 1024;
        private int currentParticleIndex;

        private float[] floatValues = new float[this.capacity * 13];
        private int[] intValues = new int[this.capacity * 2];

        public void add(float posX, float posY, float posZ, float rotX, float rotY, float rotZ, float rotW, float widthScale, float heightScale, float u0, float u1, float v0, float v1, int color, int light) {
            if (this.currentParticleIndex >= this.capacity)
                this.grow();

            var index = this.currentParticleIndex * 13;

            this.floatValues[index++] = posX;
            this.floatValues[index++] = posY;
            this.floatValues[index++] = posZ;
            this.floatValues[index++] = rotX;
            this.floatValues[index++] = rotY;
            this.floatValues[index++] = rotZ;
            this.floatValues[index++] = rotW;
            this.floatValues[index++] = widthScale;
            this.floatValues[index++] = heightScale;
            this.floatValues[index++] = u0;
            this.floatValues[index++] = u1;
            this.floatValues[index++] = v0;
            this.floatValues[index] = v1;

            index = this.currentParticleIndex * 2;

            this.intValues[index++] = color;
            this.intValues[index] = light;

            this.currentParticleIndex++;
        }

        public void forEachParticle(ParticleConsumer consumer) {
            for (var i = 0; i < this.currentParticleIndex; i++) {
                var floatIndex = i * 13;
                var intIndex = i * 2;

                consumer.consume(
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex++],
                        this.floatValues[floatIndex],
                        this.intValues[intIndex++],
                        this.intValues[intIndex]
                );
            }
        }

        public void clear() {
            this.currentParticleIndex = 0;
        }

        private void grow() {
            this.capacity *= 2;

            this.floatValues = Arrays.copyOf(this.floatValues, this.capacity * 13);
            this.intValues = Arrays.copyOf(this.intValues, this.capacity * 2);
        }

        public int count() {
            return this.currentParticleIndex;
        }
    }
}
