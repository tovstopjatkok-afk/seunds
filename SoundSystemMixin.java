package ru.soundchunkmarker.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.soundchunkmarker.SoundChunkMarkerClient;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
    // In 1.21.8 SoundSystem.play(SoundInstance) returns SoundSystem.PlayResult.
    @Inject(
        method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
        at = @At("HEAD")
    )
    private void soundchunkmarker$onPlay(SoundInstance sound, CallbackInfo ci) {
        if (sound == null) return;

        Identifier id = sound.getId();
        String path = id.getPath();

        // Ignore bats and zombie-family sounds.
        if (path.startsWith("entity.bat.")
                || path.startsWith("entity.zombie.")
                || path.startsWith("entity.drowned.")
                || path.startsWith("entity.husk.")
                || path.startsWith("entity.zombified_piglin.")) {
            return;
        }

        boolean swordAttack = path.startsWith("entity.player.attack.");
        boolean mobSound = path.startsWith("entity.") && !path.startsWith("entity.player.");

        if (swordAttack || mobSound) {
            SoundChunkMarkerClient.markSound(sound.getX(), sound.getY(), sound.getZ());
        }
    }
}
