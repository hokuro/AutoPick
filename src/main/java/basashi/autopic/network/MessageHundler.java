package basashi.autopic.network;

import basashi.autopic.core.ModCommon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class MessageHundler {
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ModCommon.MOD_ID, "main_channel"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();

	public static void register()
	{
		int disc = 0;

		HANDLER.registerMessage(disc++, Message_GetItem.class, Message_GetItem::encode, Message_GetItem::decode, Message_GetItem.Handler::handle);

	}

	public static void send_MSG_GetItem(){
		HANDLER.sendToServer(new Message_GetItem());
	}
}
