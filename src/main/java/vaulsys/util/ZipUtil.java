package vaulsys.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static byte[] getZipByteArray(String[] inputNames, byte[][] input){
		ZipOutputStream zipFile = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();	
		zipFile = new ZipOutputStream(bos); 
		try {
			for(int i=0; i<inputNames.length; i++){
				if(inputNames[i] != null && !inputNames[i].equals("")){
					zipFile.putNextEntry(new ZipEntry(inputNames[i]));
					zipFile.write(input[i]);
					zipFile.closeEntry();
				}
			}
			zipFile.close();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
}
