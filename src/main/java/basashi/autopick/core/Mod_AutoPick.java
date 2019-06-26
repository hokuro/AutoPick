package basashi.autopick.core;

import basashi.autopick.config.MyConfig;
import basashi.autopick.event.EventHook;
import basashi.autopick.network.MessageHundler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ModCommon.MOD_ID)
public class Mod_AutoPick
{
	private static CommonProxy proxy;
    // Directly reference a log4j logger.
    public static  EventHook event_instance;

    public Mod_AutoPick() {
    	ModLoadingContext.get().
        registerConfig(
        		net.minecraftforge.fml.config.ModConfig.Type.COMMON,
        		MyConfig.spec);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

    	//event_instance = new EventHook();
    	MinecraftForge.EVENT_BUS.register(EventHook.instance);
    	MessageHundler.register();
    }

    private void setup(final FMLCommonSetupEvent event)
    {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        proxy = new CommonProxy.Client();
        proxy.preInit();
    }
}
