/**
 * 
 */
package com.redisapp.util;

import java.io.IOException;
import java.util.Properties;


public class ConfigProperties {
	
	public final static Properties prop = new Properties();
	 
	static {
		try {
			prop.load(ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	} 
	
}
