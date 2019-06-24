package basashi.autopic.config;

import java.io.File;

import basashi.autopic.core.ModCommon;

public class CtlConfig {
//	private static Class<?> _cls =null;
	private static File _file=null;
	private static long _lastModify = 0L;
	private static long _checkTime = 0L;

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
		//loadConfig();
		_checkTime = System.currentTimeMillis();

		//ModLog.log().debug("end");
		return true;
	}
}