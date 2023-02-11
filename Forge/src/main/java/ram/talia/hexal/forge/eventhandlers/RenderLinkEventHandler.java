package ram.talia.hexal.forge.eventhandlers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ram.talia.hexal.client.LinkablePacketHolder;

public class RenderLinkEventHandler {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        LinkablePacketHolder.maybeRetry();
    }
}
