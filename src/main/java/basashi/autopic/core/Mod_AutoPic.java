package basashi.autopic.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(value = ModCommon.MOD_ID)
public class Mod_AutoPic {

	public Mod_AutoPic(){
        MinecraftForge.EVENT_BUS.register(this);
	}


//	@Mod.Instance(ModCommon.MOD_ID)
//	public static Mod_AutoPic instance;
//	@SidedProxy(clientSide = ModCommon.MOD_PACKAGE + ModCommon.MOD_CLIENT_SIDE, serverSide = ModCommon.MOD_PACKAGE + ModCommon.MOD_SERVER_SIDE)
//	public static CommonProxy proxy;
//
//	@EventHandler
//	public void construct(FMLConstructionEvent event) {
//		ModCommon.isDebug = false;
//	}
//
//	@EventHandler
//	public void preInit(FMLPreInitializationEvent event) {
//		CtlConfig.configure(ConfigValue.class,event);
//		proxy.preInit();
//	}
//
//	@EventHandler
//	public void init(FMLInitializationEvent event) {
//		FMLEventChannel e = NetworkRegistry.INSTANCE.newEventDrivenChannel(ModCommon.CHANEL_NAME);
//		e.register(EventHook.instance);
//		MinecraftForge.EVENT_BUS.register(EventHook.instance);
//	}
//
//	@EventHandler
//	public void postInit(FMLPostInitializationEvent event){
//	}
//


}
