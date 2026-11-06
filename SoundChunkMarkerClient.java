package ru.soundchunkmarker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SoundChunkMarkerClient implements ClientModInitializer {
    private static final Map<Long, Long> MARKED_CHUNKS = new ConcurrentHashMap<>();
    private static final long MARK_DURATION_TICKS = 20L * 30L;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                MARKED_CHUNKS.clear();
                return;
            }
            long now = client.world.getTime();
            MARKED_CHUNKS.entrySet().removeIf(entry -> now - entry.getValue() > MARK_DURATION_TICKS);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null || context.matrixStack() == null) return;

            Vec3d camera = context.camera().getPos();
            VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
            double bottomY = client.world.getBottomY();
            double topY = bottomY + client.world.getHeight();

            for (Map.Entry<Long, Long> entry : MARKED_CHUNKS.entrySet()) {
                ChunkPos chunk = new ChunkPos(entry.getKey());
                int x = chunk.getStartX();
                int z = chunk.getStartZ();

                Box box = new Box(x, bottomY, z, x + 16, topY, z + 16)
                        .offset(-camera.x, -camera.y, -camera.z);

                VertexRendering.drawBox(context.matrixStack(), lines, box,
                        1.0f, 0.05f, 0.05f, 0.95f);
            }
        });
    }

    public static void markSound(double x, double y, double z) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        ChunkPos chunk = new ChunkPos(BlockPos.ofFloored(x, y, z));
        MARKED_CHUNKS.put(chunk.toLong(), client.world.getTime());
    }
}
