package vaulsys.util;

import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DBConfigUtil {

	public static final Key DB_URL = new Key("db.url", true);
	public static final Key DB_USERNAME = new Key("db.username", true);
	public static final Key DB_PASSWORD = new Key("db.password", true);
	public static final Key DB_SHOW_SQL = new Key("db.showSQL", true);
	public static final Key DB_HBM2DDL = new Key("db.hbm2ddl", true);
	public static final Key DB_SCHEMA = new Key("db.schema", true);
	public static final Key DB_USERNAME_SETTLE = new Key("db.username_settle", true);
	public static final Key DB_PASSWORD_SETTLE = new Key("db.password_settle", true);
	public static final Key DB_DRIVER = new Key("db.driver", true);
	
	private static final Properties PROPERTIES = new Properties();

	static {
		try {
			PROPERTIES.load(new FileInputStream("config/config.properties"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String getProperty(Key key) {
		if (PROPERTIES.containsKey(key.getProp()))
			return PROPERTIES.getProperty(key.getProp());
		else
			throw new IllegalArgumentException(String.format("[%s] key not found in config.properties!", key.getProp()));
	}

	public static String getProperty(Key key, String prefix) {
		String name = prefix+"."+ key.getProp();
		if (PROPERTIES.containsKey(name))
			return PROPERTIES.getProperty(name);
		else
			return null;
	}
	
	public static Boolean getBoolean(Key key) {
		return Boolean.valueOf(getProperty(key));
	}

	public static Integer getInteger(Key key){
		return Integer.valueOf(getProperty(key));
	}
	
	public static Long getLong(Key key){
		return Long.valueOf(getProperty(key));
	}
	
	public static String getDecProperty(Key key) {
		try {
			String aesKey = "4B6250655368566D597133743677397A244226452948404D635166546A576E5A";
			String cipherData = getProperty(key);

			if (Util.hasText(cipherData)) {
				return WSEncrptionUtil.AES256GCMDecryptWithoutVector(cipherData, aesKey);
			} else {
				return null;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean isDecryptionEnabled() {
		String dec = PROPERTIES.getProperty("general.decryption.enabled", Boolean.TRUE.toString());
		return Boolean.valueOf(dec);
	}

	private static String getGroup() {
		return PROPERTIES.getProperty("general.group", "bank");
	}
	
	public static class Key {
		private String prop;
		private boolean groupy;
		private String groupName;

		private Key(String prop, boolean groupy) {
			this.prop = prop;
			this.groupy = groupy;
		}

		public String getProp() {
			if (groupy)
				return String.format("%s.%s", getGroup(), prop);
			else
				return prop;
		}
	}
	
	public static Map<String, String> getProperties(String prefix){
		Map<String, String> map = new HashMap<String, String>();
		String pattern = "^"+prefix+"[.]\\w+";
		for (Object k: PROPERTIES.keySet()){
			String key =  k.toString();
			if (key.matches(pattern))
				map.put(key, PROPERTIES.getProperty(key));
		}
		return map;
	}
}
