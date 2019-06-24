package basashi.autopic.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigValue{
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final General GENERAL = new General(BUILDER);
	public static final ForgeConfigSpec spec = BUILDER.build();
	private static int p_DefaultValue;

	public static int DefaultMode(){
		return p_DefaultValue;
	}
	public static double Vertical(){
		return GENERAL.vertical.get();
	}
	public static double Horizontal(){
		return GENERAL.horizontal.get();
	}
	public static String ItemLists(){
		return GENERAL.itemlist.get();
	}

	public static void setDefaultMode(int value){
		p_DefaultValue = value;
	}

	public static class General{

		public final ForgeConfigSpec.ConfigValue<Integer> DefaultMode;
		public final ForgeConfigSpec.ConfigValue<Double> vertical;
		public final ForgeConfigSpec.ConfigValue<Double> horizontal;
		public final ForgeConfigSpec.ConfigValue<String> itemlist;
		public ResourceLocation locations;

		public General(ForgeConfigSpec.Builder builder){
			builder.push("General");
			DefaultMode = builder
					.comment("auto pick mode[0:unenable,1:ignore item in list,2:allow only item in list]")
					.defineInRange("defaultmode",1,0,1);

			vertical = builder
					.comment("vertical range of pick")
					.define("vertical",1.0D);

			horizontal = builder
					.comment("horizontal range of pick")
					.define("horizontal",2.0D);

			itemlist = builder
					.comment("itemlist")
					.define("itemlist","");
			builder.pop();

		}

		private List<ResourceLocation> makeResourceLocation(){
			List<ResourceLocation> retList = new ArrayList<ResourceLocation>();
			String[] itmlst = itemlist.get().split(",");

			for (int i = 0; i < itmlst.length; i++){
				if (itmlst[i].contains(":")){
					String[] slp = itmlst[i].split(":");
					retList.add(new ResourceLocation(slp[0],slp[1]));
				}else{
					retList.add(new ResourceLocation("minecraft",itmlst[i]));
				}
			}

			return retList;
		}

		public boolean AddOrRemoveItem(ResourceLocation name){
			int idx = -1;
			boolean ret = true;
			List<ResourceLocation> resource = makeResourceLocation();
			for ( int i = 0; i < resource.size(); i++){
				if (resource.get(i).toString().equals(name.toString())){
					idx = i;
				}
			}
			if (idx < 0){
				// 新規登録
				resource.add(name);
			}else{
				//　既存削除
				resource.remove(idx);
				ret = false;
			}

			// リストの中身を文字列に変換
			String itmlst = "";
			if (resource.size() != 0){
				itmlst = resource.get(0).toString();
				for ( int i = 1; i < resource.size(); i++){
					itmlst += ","+resource.get(i);
				}
			}
			itemlist.next().push(itmlst);
			return ret;
		}
	}
}