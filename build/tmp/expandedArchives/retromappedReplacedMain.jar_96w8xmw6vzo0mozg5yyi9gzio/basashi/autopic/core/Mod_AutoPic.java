package basashi.autopic.core;

import basashi.autopic.config.ConfigValue;
import basashi.autopic.config.CtlConfig;
import basashi.autopic.event.EventHook;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = ModCommon.MOD_ID, name = ModCommon.MOD_NAME, version = ModCommon.MOD_VERSION)
public class Mod_AutoPic {

	@EventHandler
	public void construct(FMLConstructionEvent event) {
		ModCommon.isDebug = true;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CtlConfig.configure(ConfigValue.class,event);
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
