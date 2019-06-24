package basashi.autopic.core;

import java.awt.event.KeyEvent;

import basashi.autopic.config.ConfigValue;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {
	public CommonProxy(){

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcEnqueue);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcProcess);
	}

	public void commonSetup(FMLCommonSetupEvent event) {
    	ModLoadingContext.get().
    	registerConfig(
    			net.minecraftforge.fml.config.ModConfig.Type.COMMON,
    			ConfigValue.spec);
	}
    private void imcEnqueue(InterModEnqueueEvent event) { }
    private void imcProcess(InterModProcessEvent event) {}

    public void preInit(){ }

    public static class Client extends CommonProxy{
    	Client(){}

    	private static final KeyBinding KEYBINDING_ENABLE = new KeyBinding("autopic.key.switch", KeyEvent.VK_C, "autopic.key.category");
    	private static final KeyBinding KEYBINDING_REGIST = new KeyBinding("autopic.key.regist", KeyEvent.VK_T, "autopic.key.category");
    	@Override
    	public void preInit(){
    		InitializeKey();
    	}

    	@OnlyIn(Dist.CLIENT)
    	public void InitializeKey(){
    		ClientRegistry.registerKeyBinding(KEYBINDING_ENABLE);
    		ClientRegistry.registerKeyBinding(KEYBINDING_REGIST);
    	}

    	@OnlyIn(Dist.CLIENT)
    	public static boolean Press_Key_Enable(){
    		return KEYBINDING_ENABLE.isPressed();
    	}

    	@OnlyIn(Dist.CLIENT)
    	public static boolean Press_Key_Regist(){
    		return KEYBINDING_REGIST.isPressed();
    	}
    }

    static class Server extends CommonProxy{
    	Server(){}
    }
}
