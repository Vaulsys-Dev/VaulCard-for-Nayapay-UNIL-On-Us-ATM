package vaulsys.util;

import vaulsys.network.remote.RemoteMessageManager;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class FileUtility {
	
	private static FileUtility instance = null; 
	 private final static Logger logger = Logger.getLogger(FileUtility.class);
	 
	private FileUtility(){
		
	}
	
	public static FileUtility getInstance(){		
		if(instance == null){
			instance = new FileUtility();
		}
		return instance;
	}
	
	public static void makeDir(String urlFolder) throws Exception{	
	    File directory = new File(urlFolder);
	    if (directory.exists() && directory.isFile())
	    {
	        logger.info("The dir with name could not be created as it is a normal file");
	    }else{
	        
	    	try{
	            if (!directory.exists()){
	                directory.mkdir();
	            }
	        }catch (Exception e){
	            logger.error("prompt for error");
	            throw e;
	        }
	    }
	}
}
