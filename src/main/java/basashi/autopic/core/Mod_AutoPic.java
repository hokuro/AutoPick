package basashi.autopic.core;

import basashi.autopic.config.ConfigValue;
import basashi.autopic.config.CtlConfig;
import basashi.autopic.event.EventHook;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = ModCommon.MOD_ID, name = ModCommon.MOD_NAME, version = ModCommon.MOD_VERSION)
public class Mod_AutoPic {
	@Mod.Instance(ModCommon.MOD_ID)
	public static Mod_AutoPic instance;
	@SidedProxy(clientSide = ModCommon.MOD_PACKAGE + ModCommon.MOD_CLIENT_SIDE, serverSide = ModCommon.MOD_PACKAGE + ModCommon.MOD_SERVER_SIDE)
	public static CommonProxy proxy;

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		ModCommon.isDebug = false;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CtlConfig.configure(ConfigValue.class,event);
		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		FMLEventChannel e = NetworkRegistry.INSTANCE.newEventDrivenChannel(ModCommon.CHANEL_NAME);
		e.register(EventHook.instance);
		MinecraftForge.EVENT_BUS.register(EventHook.instance);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
	}



}
