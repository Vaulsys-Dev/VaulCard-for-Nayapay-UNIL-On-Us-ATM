package vaulsys.othermains.disagreement;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class CompareShetabForm8 {

	private static final Logger logger = Logger.getLogger(CompareShetabForm8.class);

	public static String compareFiles(String shetabPath, String switchPath, String path){
//		String path = "D:/disagreement/form8/900220";
//		String file1 = "pas2-900220-iss-report8.txt";
//		String file2 = "PAS2900220.txt";
		boolean exactCompare = false;
		
		File switchFile = new File(switchPath);
		File shetabFile = new File(shetabPath);
		String fileName = shetabFile.getName();
		fileName = fileName.substring(0, fileName.indexOf('.'));
		String pathRes = path+"/comparison-"+fileName+"-"+System.currentTimeMillis()+".txt";
		File comparision = new File(pathRes);
		BufferedWriter errors = null;
		if(!comparision.exists()){
			try {
				comparision.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
		
		try {
			errors = new BufferedWriter(new FileWriter(comparision));
		} catch (IOException e2) {
			e2.printStackTrace();
			logger.error(e2);
		}

		String err = "No error found";
		try {
			err = compare(new BufferedReader(new FileReader(switchFile)), new BufferedReader(new FileReader(shetabFile)), exactCompare);
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		
//		System.out.println("here 1: " + err );
		try {
			errors.append(err);
			errors.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return pathRes;
	}

	private static String compare(BufferedReader switchFile, BufferedReader shetabFile, boolean exactCompare) throws IOException {
		String[] strSwitch = null;
		String[] strShetab = null;
		List<String> strF = new ArrayList<String>();
		String strTemp;
		StringBuilder difference_shetabOK_swichNOT = new StringBuilder();
		StringBuilder difference_shetabNOT_switchOK = new StringBuilder();
		//StringBuilder difference = new StringBuilder();
		
		
		while (switchFile.ready()) {
			if ((strTemp = switchFile.readLine()).length() > 0) {
				if(exactCompare){
					strF.add(strTemp);
				}else if(!strTemp.contains("/458/")){
					strTemp = strTemp.replace("/439/", "/438/");
					strTemp = strTemp.replace("/440/", "/438/");
					strTemp = strTemp.replace("/441/", "/438/");
					strTemp = strTemp.replace("/442/", "/438/");
					strTemp = strTemp.replace("/443/", "/438/");
					strF.add(strTemp);
				}
			}
		}
		
		strSwitch = new String[strF.size()];
		for(int i=0; i<strF.size(); i++){
			strSwitch[i] = strF.get(i);
		}
		strF.clear();

		while (shetabFile.ready()) {
			if ((strTemp = shetabFile.readLine()).length() > 0) {
				if(exactCompare){
					strF.add(strTemp);
				}else if(!strTemp.contains("/458/")){
					strTemp = strTemp.replace("/439/", "/438/");
					strTemp = strTemp.replace("/440/", "/438/");
					strTemp = strTemp.replace("/441/", "/438/");
					strTemp = strTemp.replace("/442/", "/438/");
					strTemp = strTemp.replace("/443/", "/438/");
					strF.add(strTemp);
				}
			}
		}
		strShetab = new String[strF.size()];
		for(int i=0; i<strF.size(); i++){
			strShetab[i] = strF.get(i);
		}

		
		Arrays.sort(strSwitch, new Comparator<String>(){
			@Override
			public int compare(String f1, String f2) {
				return f1.compareToIgnoreCase(f2);
			}
		});

		Arrays.sort(strShetab, new Comparator<String>(){
			@Override
			public int compare(String f1, String f2) {
				return f1.compareToIgnoreCase(f2);
			}
		});

		int i=0,j=0;
		for(; i<strSwitch.length && j<strShetab.length;){

			//Transfer card to account
			StringTokenizer switchToken = new StringTokenizer(strSwitch[i], "/");
			StringTokenizer shetabToken = new StringTokenizer(strShetab[j], "/");
			while(switchToken.hasMoreTokens() && switchToken.hasMoreTokens()){
				String t1 = switchToken.nextToken();
				String t2 = shetabToken.nextToken();
				if(t2.contains("5022291111111111") && !t1.contains("5022291111111111")){
					strSwitch[i] = strSwitch[i].replaceAll(t1, t2);
					break;
				}
			}


			if(strSwitch[i].equals(strShetab[j])){
				i++;
				j++;
				continue;
			}else if(exactCompare){
				if(strSwitch[i].compareToIgnoreCase(strShetab[j]) < 0){
					difference_shetabOK_swichNOT.append("Switch-OK_Shetab-NOT: "+strSwitch[i]+"\r\n");
					logger.debug(strSwitch[i]);
					i++;
				}else{
					difference_shetabNOT_switchOK.append("Shetab-OK_Switch-NOT: "+strShetab[j]+"\r\n");
					logger.debug(strShetab[j]);
					j++;					
				}				
			}else{
				StringTokenizer tokenizer1 = new StringTokenizer(strSwitch[i], "/");
				StringTokenizer tokenizer2 = new StringTokenizer(strShetab[j], "/");

				String tmpRefNum1 = tokenizer1.nextToken();
				String tmpRefNum2 = tokenizer2.nextToken();



				if(tmpRefNum1.equalsIgnoreCase(tmpRefNum2)){

					for(int i1=0; i1<4; i1++)
						tokenizer1.nextToken();
					String tmpAmount1 = tokenizer1.nextToken();
					for(int j1=0; j1<4; j1++)
						tokenizer2.nextToken();
					String tmpAmount2 = tokenizer2.nextToken();
					strSwitch[i] = strSwitch[i].replace("/"+tmpAmount1+"/", "/"+tmpAmount2+"/");

					//don't check twoDigit bank code
					for(int i1=0; i1<9; i1++)
						tokenizer1.nextToken();
					String code1 = tokenizer1.nextToken();
					for(int j1=0; j1<9; j1++)
						tokenizer2.nextToken();
					String code2 = tokenizer2.nextToken();
					strSwitch[i] = strSwitch[i].replace("/"+code1+"/", "/"+code2+"/");
						
					if(!strSwitch[i].equalsIgnoreCase(strShetab[j])){
						logger.debug(strSwitch[i]);
						logger.debug(strShetab[j]);

						difference_shetabOK_swichNOT.append("Switch-OK_Shetab-NOT: "+strSwitch[i]+"\r\n");
						difference_shetabNOT_switchOK.append("Shetab-OK_Switch-NOT: "+strShetab[j]+"\r\n");
					}
					i++;
					j++;
					continue;
				}else if(strSwitch[i].compareToIgnoreCase(strShetab[j]) < 0){
					difference_shetabOK_swichNOT.append("Switch-OK_Shetab-NOT: "+strSwitch[i]+"\r\n");
					logger.debug(strSwitch[i]);
					i++;
				}else{
					difference_shetabNOT_switchOK.append("Shetab-OK_Switch-NOT: "+strShetab[j]+"\r\n");
					logger.debug(strShetab[j]);
					j++;					
				}
			}
		}
		
		for(;i<strSwitch.length; i++){
			difference_shetabOK_swichNOT.append("Switch-OK_Shetab-NOT: "+strSwitch[i]+"\r\n");
		}

		for(;j<strShetab.length; j++){
			difference_shetabNOT_switchOK.append("Shetab-OK_Switch-NOT: "+strShetab[j]+"\r\n");
		}

		switchFile.close();
		shetabFile.close();

//		difference.append(difference_shetabOK_swichNOT);
		difference_shetabOK_swichNOT.append("****************************" + "\r\n");
		difference_shetabOK_swichNOT.append(difference_shetabNOT_switchOK);
		return difference_shetabOK_swichNOT.toString();
	}

}
