package eu.ha3.matmos.core.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import eu.ha3.matmos.Matmos;
import eu.ha3.matmos.core.ducks.ISoundManager;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

@Mixin(SoundManager.class)
abstract class MixinSoundManager implements ISoundManager {

    @Accessor("loaded")
    public abstract boolean isLoaded();
    
    private SoundSystem soundSystemAccessor;

    @Shadow
    private List<String> pausedChannels;

    @Shadow
    private boolean loaded;
    
    @Invoker("getVolume")
    @Override
    public abstract float invokeGetVolume(SoundCategory category);

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        Matmos.LOGGER.debug("Running mixin for SoundManager constructor!");
        SoundSystemConfig.setNumberStreamingChannels(11);
        SoundSystemConfig.setNumberNormalChannels(32 - 11);
        SoundSystemConfig.setStreamQueueFormatsMatch(true);
    }

    /**
     * Applies Forge's fix for MC-35856. Required for LiteLoader. Redundant if we're
     * using Forge, but should be harmless.
     */
    @Inject(method = "stopAllSounds", at = @At("RETURN"))
    public void stopAllSounds(CallbackInfo ci) {
        Matmos.LOGGER.debug("Running mixin for SoundManager.stopAllSounds!");
        if (this.loaded) {
            this.pausedChannels.clear(); // Forge: MC-35856 Fixed paused sounds repeating when switching worlds
        }
    }
    
    @Override
    public SoundSystem getSoundSystem() {
        return soundSystemAccessor;
    }
    
    @Override
    public void setSoundSystemAccessor(SoundSystem sndSystem) {
        this.soundSystemAccessor = sndSystem;
    }
}
