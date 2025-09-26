package me.sqqo.mixins;

import me.sqqo.events.PreUpdateEvent;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.sqqo.Pepita.mc;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void onUpdate(CallbackInfo cbInfo) {
        if (mc.theWorld.isBlockLoaded(new BlockPos(mc.thePlayer.posX, 0, mc.thePlayer.posZ))) {
            MinecraftForge.EVENT_BUS.post(new PreUpdateEvent());
        }
    }
}
