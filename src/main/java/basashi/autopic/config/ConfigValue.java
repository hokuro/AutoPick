package basashi.autopic.config;


public class ConfigValue{

//	@ConfigProperty(comment="kye to changeng mode")
//	public static String ModeChangeKey ="KEY_P";
//	@ConfigProperty(comment="key for adding or removing item to itemlist")
//	public static String RegistKey ="KEY_O";
	@ConfigProperty(comment="auto pick mode[0:unenable,1:ignore item in list,2:allow only item in list]")
	public static int DefaultMode = 1;
	@ConfigProperty(comment="vertical range of pick")
	public static double vertical = 1.0D;
	@ConfigProperty(comment="horizontal range of pick")
	public static double horizontal = 2.0D;
	@ConfigProperty(comment="itemlist", isSave=true)
	public static String itemlist = "";

	public static boolean AddOrRemoveItem(String name){
		int idx = 0;
		if ((idx = itemlist.indexOf(name)) < 0){
			if (!"".equals(itemlist)){itemlist += ",";}
			itemlist += name;
			return true;
		}else{
			String[] w = itemlist.split(",");
			itemlist = "";
			for(String s : w){
				if (!s.equals(name)){
					itemlist += s+",";
				}
			}
			if (!"".equals(itemlist)){
				itemlist = itemlist.substring(0,itemlist.length()-1);
			}
			return false;
		}
	}
}