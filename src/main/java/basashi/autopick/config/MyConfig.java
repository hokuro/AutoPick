package basashi.autopick.config;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class MyConfig{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final General GENERAL = new General(BUILDER);
	public static final ForgeConfigSpec spec = BUILDER.build();
	private static int p_DefaultValue;
	private static boolean p_init = false;

	public static int DefaultMode(){
		if (!p_init){
			p_DefaultValue = GENERAL.DefaultMode.get();
			p_init = true;
		}
		return p_DefaultValue;
	}
	public static double Vertical(){
		return GENERAL.vertical.get();
	}
	public static double Horizontal(){
		return GENERAL.horizontal.get();
	}

	public static void setDefaultMode(int value){
		p_DefaultValue = value;
	}

	public static class General{

		public final ForgeConfigSpec.ConfigValue<Integer> DefaultMode;
		public final ForgeConfigSpec.ConfigValue<Double> vertical;
		public final ForgeConfigSpec.ConfigValue<Double> horizontal;
		public ResourceLocation locations;

		public General(ForgeConfigSpec.Builder builder){
			builder.push("General");
			DefaultMode = builder
					.comment("auto pick mode[0:unenable,1:ignore item in list,2:allow only item in list]")
					.defineInRange("defaultmode",1,0,1);

			vertical = builder
					.comment("vertical range of pick")
					.define("vertical",4.0D);

			horizontal = builder
					.comment("horizontal range of pick")
					.define("horizontal",4.0D);

			builder.pop();
		}
	}
}