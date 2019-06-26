package basashi.autopick.core;
import java.awt.event.KeyEvent;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class CommonProxy {
	public CommonProxy(){

	}

    public void preInit(){ }

    public static class Client extends CommonProxy{
    	Client(){}

    	private static final KeyBinding KEYBINDING_ENABLE = new KeyBinding("autopic.key.switch", KeyEvent.VK_C, "autopic.key.category");
    	@Override
    	public void preInit(){
    		InitializeKey();
    	}

    	@OnlyIn(Dist.CLIENT)
    	public void InitializeKey(){
    		ClientRegistry.registerKeyBinding(KEYBINDING_ENABLE);
    	}

    	@OnlyIn(Dist.CLIENT)
    	public static boolean Press_Key_Enable(){
    		return KEYBINDING_ENABLE.isPressed();
    	}

    }

    static class Server extends CommonProxy{
    	Server(){}
    	public void preInit(){}
    }
}
