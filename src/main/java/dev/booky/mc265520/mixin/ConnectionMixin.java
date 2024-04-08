package dev.booky.mc265520.mixin;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.flow.FlowControlHandler;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin extends SimpleChannelInboundHandler<Packet<?>> {

    @Redirect(
            method = "configurePacketHandler",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/netty/channel/ChannelPipeline;addLast([Lio/netty/channel/ChannelHandler;)Lio/netty/channel/ChannelPipeline;"
            )
    )
    private ChannelPipeline removeFCHandler(ChannelPipeline instance, ChannelHandler[] handlers) {
        return instance; // NO-OP
    }

    @Inject(
            method = "configureSerialization",
            at = @At("TAIL")
    )
    private static void addFCHandlerMultiplayer(
            ChannelPipeline pipeline, PacketFlow flow,
            BandwidthDebugMonitor monitor, CallbackInfo ci
    ) {
        // add flow control handler right after the frame decoder
        pipeline.addAfter("splitter", null, new FlowControlHandler());
    }

    @Inject(
            method = "configureInMemoryPacketValidation",
            at = @At("TAIL")
    )
    private static void addFCHandlerSingleplayer(
            ChannelPipeline pipeline, PacketFlow flow, CallbackInfo ci
    ) {
        // add flow control handler before validator (which also switches protocols)
        pipeline.addBefore("validator", null, new FlowControlHandler());
    }
}
