package basashi.autopick.network;

import java.util.function.Supplier;

import basashi.autopick.event.EventHook;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class Message_GetItem {

	public Message_GetItem()
	{
	}

	public static void encode(Message_GetItem pkt, PacketBuffer buf)
	{

	}

	public static Message_GetItem decode(PacketBuffer buf)
	{

		return new Message_GetItem();
	}

	public static class Handler
	{
		public static void handle(final Message_GetItem pkt, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				EventHook.setPlayerEntity(ctx.get().getSender());
			});
			ctx.get().setPacketHandled(true);
		}
	}

}
