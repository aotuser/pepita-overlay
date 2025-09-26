package me.sqqo.mixins;

import io.netty.channel.ChannelHandlerContext;
import me.sqqo.events.PacketReceiveEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void receivePacket(ChannelHandlerContext cHC, Packet<?> packet, CallbackInfo cbInfo) {
        PacketReceiveEvent receivePacketEvent = new PacketReceiveEvent(packet);
        MinecraftForge.EVENT_BUS.post(receivePacketEvent);

        if (receivePacketEvent.isCanceled()) {
            cbInfo.cancel();
        }
    }
}
