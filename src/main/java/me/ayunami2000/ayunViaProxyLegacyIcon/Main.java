package me.ayunami2000.ayunViaProxyLegacyIcon;

import java.io.*;

import net.raphimc.viaproxy.proxy.util.ExceptionUtil;
import net.raphimc.viaproxy.util.logging.*;
import java.nio.file.*;
import net.raphimc.viaproxy.plugins.*;
import net.raphimc.viaproxy.plugins.events.*;
import net.raphimc.viaproxy.plugins.events.types.*;
import net.lenni0451.lambdaevents.*;
import io.netty.channel.*;
import net.raphimc.netminecraft.packet.impl.status.*;
import com.google.gson.*;
import java.util.*;

public class Main extends ViaProxyPlugin
{
    private static byte[] favicon;
    
    public void onEnable() {
        final File iconFile = new File("server-icon.png");
        if (!iconFile.exists()) {
            Logger.LOGGER.error("No server-icon.png found!");
            return;
        }
        try {
            Main.favicon = Files.readAllBytes(iconFile.toPath());
        }
        catch (Exception e) {
            Logger.LOGGER.error("Failed to read server-icon.png!");
            return;
        }
        PluginManager.EVENT_MANAGER.register(this);
    }
    
    @EventHandler
    public void onEvent(final Proxy2ServerChannelInitializeEvent event) {
        if (event.getType() == ITyped.Type.POST) {
            event.getChannel().pipeline().addBefore("handler", "ayun-legacy-icon-injector", new LegacyIconInjector());
        }
    }
    
    static class LegacyIconInjector extends ChannelInboundHandlerAdapter
    {
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (msg instanceof S2CStatusResponsePacket) {
                final S2CStatusResponsePacket pkt = (S2CStatusResponsePacket)msg;
                final JsonObject json = (JsonObject)JsonParser.parseString(pkt.statusJson);
                if (!json.has("favicon")) {
                    json.addProperty("favicon", "data:image/png;base64," + Base64.getEncoder().encodeToString(Main.favicon));
                }
                pkt.statusJson = json.toString();
                super.channelRead(ctx, pkt);
            }
            else {
                super.channelRead(ctx, msg);
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ExceptionUtil.handleNettyException(ctx, cause, null);
        }
    }
}
