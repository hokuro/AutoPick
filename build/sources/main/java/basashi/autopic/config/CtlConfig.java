package basashi.autopic.config;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import basashi.autopic.core.ModCommon;
import basashi.autopic.core.log.ModLog;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;


public class CtlConfig {
	private static Class<?> _cls =null;
	private static File _file=null;
	private static long _lastModify = 0L;
	private static long _checkTime = 0L;


	public static void configure(Class<?> cls, FMLPreInitializationEvent event){
		ModLog.log().debug("start");
		_cls = cls;
		_file = event.getSuggestedConfigurationFile();
		loadConfig();
		ModLog.log().debug("end");
	}

	public static boolean reloadConfig(){
		//ModLog.log().debug("start");
		long n = System.currentTimeMillis() - _checkTime;
		if (n < ModCommon.MOD_CONFIG_RELOAD) {
			return false;
		}
		if (!_file.isFile()) {
			return false;
		}
		if (_lastModify == _file.lastModified()) {
			return false;
		}
		loadConfig();
		_checkTime = System.currentTimeMillis();

		//ModLog.log().debug("end");
		return true;
	}

	public static int getConfigKey(String name){
		//ModLog.log().debug("start");
		if (FMLCommonHandler.instance().getSide() != Side.CLIENT) {
			return -1;
		}
		Field[] fs = null;
		try {
			Class<?> cls = Class.forName("org.lwjgl.input.Keyboard");
			fs = cls.getFields();
		} catch (ClassNotFoundException e1) {
			return -1;
		}
		if (null == fs) {
			return -1;
		}
		int key_no = 0;
		for (Field f : fs) {
			if (Modifier.isStatic(f.getModifiers())) {
				String s = f.getName();
				if (0 == s.indexOf("KEY_")) {
					if (s.equalsIgnoreCase(name)) {
						try {
							key_no = f.getInt(null);
						} catch (IllegalArgumentException localIllegalArgumentException) {
						} catch (IllegalAccessException localIllegalAccessException) {
						}
					}
				}
			}
		}
		ModLog.log().info("keyChange("+name+")="+ key_no);
		//ModLog.log().debug("end");
		return key_no;
	}


	private static void loadConfig(){
		ModLog.log().debug("start");

		boolean isSave = (!_file.isFile()) || (_file.length() <= 0L);
		Configuration config = new Configuration(_file);
		config.load();
		Field[] fields = _cls.getFields();
		for (Field fld : fields){
			ConfigProperty prop = (ConfigProperty)fld.getAnnotation(ConfigProperty.class);
			if ( prop == null){continue;}

			if (Modifier.isStatic(fld.getModifiers())){
				Class<?> type = fld.getType();
				Property p = null;
				try {
					if (!config.hasCategory(prop.category())){
						isSave = true;
					}
					String comment=prop.comment();
					if (Integer.TYPE.equals(type)){
						p = config.get(prop.category(), fld.getName(), fld.getInt(null));
						fld.setInt(null, p.getInt());
						ModLog.log().debug("config Int : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getInt(null));
					}else if (Double.TYPE.equals(type)){
						p = config.get(prop.category(), fld.getName(), fld.getDouble(null));
						fld.setDouble(null, p.getDouble(0.0D));
						ModLog.log().debug("config Double : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getDouble(null));
					}else if (String.class.equals(type)){
						p = config.get(prop.category(),fld.getName(),fld.get(null).toString());
						fld.set(null, p.getString());
						ModLog.log().debug("config String : "+prop.category() + ":" + fld.getName() + " : value ="+fld.get(null).toString());
					}else if (Boolean.TYPE.equals(type)){
						p = config.get(prop.category(),fld.getName(),fld.getBoolean(null));
						fld.setBoolean(null, p.getBoolean(fld.getBoolean(null)));
						ModLog.log().debug("config Bool : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getBoolean(null));
					}else{
						ModLog.log().warn("unknowntype :"+type.getCanonicalName());
					}
					if ((null != p) && (null != comment)) {
						p.setComment(comment);
					}
				} catch (IllegalArgumentException localIllegalArgumentException) {
				} catch (IllegalAccessException localIllegalAccessException) {
				}
			}
		}

		if (isSave) {
			config.save();
		}
		_lastModify = _file.lastModified();
		_checkTime = System.currentTimeMillis();

		ModLog.log().debug("end");
	}


	public static void saveConfig(){
		ModLog.log().debug("start");
		CustomConfiguration config = new CustomConfiguration(_file);
		config.load();
		Field[] fields = _cls.getFields();
		for (Field fld : fields){
			ConfigProperty prop = (ConfigProperty)fld.getAnnotation(ConfigProperty.class);
			if ( prop == null){continue;}

			if (Modifier.isStatic(fld.getModifiers())){
				Class<?> type = fld.getType();
				Property p = null;
				try {
					String comment=prop.comment();
					if (Integer.TYPE.equals(type)){
						p = config.get(prop.category(), fld.getName(), fld.getInt(null));
						if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.getInt(null));
						ModLog.log().debug("config Int : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getInt(null));
					}else if (Double.TYPE.equals(type)){
						p = config.get(prop.category(), fld.getName(), fld.getDouble(null));
						if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.getDouble(null));
						ModLog.log().debug("config Double : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getDouble(null));
					}else if (String.class.equals(type)){
						p = config.get(prop.category(),fld.getName(),fld.get(null).toString());
						if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.get(null).toString());
						ModLog.log().debug("config String : "+prop.category() + ":" + fld.getName() + " : value ="+fld.toString());
					}else if (Boolean.TYPE.equals(type)){
						p = config.get(prop.category(),fld.getName(),fld.getBoolean(null));
						if(prop.isSave()) config.set(prop.category(), fld.getName(), fld.getBoolean(null));
						ModLog.log().debug("config Bool : "+prop.category() + ":" + fld.getName() + " : value ="+fld.getBoolean(null));
					}else{
						ModLog.log().warn("unknowntype :"+type.getCanonicalName());
					}
					if ((null != p) && (null != comment)) {
						p.setComment(comment);
					}
				} catch (IllegalArgumentException localIllegalArgumentException) {
				} catch (IllegalAccessException localIllegalAccessException) {
				}
			}
		}
		config.save();
		_lastModify = _file.lastModified();
		_checkTime = System.currentTimeMillis();

		ModLog.log().debug("end");
	}
}