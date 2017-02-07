package basashi.autopic.client;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientProxy extends basashi.autopic.core.CommonProxy {
	private static final KeyBinding KEYBINDING_ENABLE = new KeyBinding("autopic.key.switch", Keyboard.KEY_C, "autopic.key.category");
	private static final KeyBinding KEYBINDING_REGIST = new KeyBinding("autopic.key.regist", Keyboard.KEY_T, "autopic.key.category");
	@Override
	public void preInit(){
		InitializeKey();
	}

	@SideOnly(Side.CLIENT)
	public void InitializeKey(){
		ClientRegistry.registerKeyBinding(KEYBINDING_ENABLE);
		ClientRegistry.registerKeyBinding(KEYBINDING_REGIST);
	}

	@SideOnly(Side.CLIENT)
	public static boolean Press_Key_Enable(){
		return KEYBINDING_ENABLE.func_151468_f();
	}

	@SideOnly(Side.CLIENT)
	public static boolean Press_Key_Regist(){
		return KEYBINDING_REGIST.func_151468_f();
	}
}
