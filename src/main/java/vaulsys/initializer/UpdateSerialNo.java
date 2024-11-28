package vaulsys.initializer;

import vaulsys.persistence.GeneralDao;
import vaulsys.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class UpdateSerialNo {
	public static void main(String[] args) throws Exception {
		String fileName = "C:/Documents and Settings/saber/My Documents/MigrationPasargad/Returned/PSP/Tehran.xlsx";
		boolean force = true;
		boolean doublyForced = true;
		
//		Long parentGroup = 386795901L; //parsa
//		Long parentGroup = 386795901L; //infotech
		GeneralDao.Instance.beginTransaction();
		
		GeneralDao.Instance.executeSqlQuery("select count(*) from epay.job_info");

//		GeneralDao.Instance.rollback();
//		GeneralDao.Instance.endTransaction();
//		GeneralDao.Instance.rollback();
//		GeneralDao.Instance.beginTransaction();

		GeneralDao.Instance.executeSqlQuery("select count(*) from epay.job_info");

		XSSFWorkbook wb1 = null;
		
		try {			
			List<String[]> results = new ArrayList<String[]>();
			wb1 = new XSSFWorkbook(fileName);
			XSSFSheet sheet = wb1.getSheetAt(0);
			XSSFRow row;

			int rows; // No of rows
			rows = sheet.getPhysicalNumberOfRows();
			
			System.out.println("the total number of rows are " + rows);

			String[] columnNames = new String[]{
					"CardAcceptor",		//0 
					"TerminalID", 		//1
					"Seporde",			//2
					"Address", 			//3
					"Phone", 
					"SerialNo", 		//5
					"Name_pazirandeh", 
					"CAPOSTCODE",		//7 
					"EnableOrDisable", 
					"CITYCODE",			//9 
					"CITYNAME", 
					"City_FromGroup",	//11 
					"Ostan_FromGroup", 
					"IsSagem",			//13 
					"tedad_trx", 
					"KARGOZAR",			//15 
					"SENF", 
					"SENF2",			//17 
					"Country", 
					"alaki", 			//19
					"MerchantCode", 	//20
					"TerminalCode",		//21 
					"Serial"			//22
					};
			int[] colIndexes = new int[columnNames.length];
			String[] values = new String[columnNames.length];
					
			XSSFRow header = sheet.getRow(0);
			
			for(int i=0; i < columnNames.length; i++){
				for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
					if (header.getCell(c) != null && columnNames[i].equalsIgnoreCase(header.getCell(c).toString())) {
						colIndexes[i] = c;
						break;
					}
				}
			}
				
			for (int r = 1; r < rows && results.size() < 1000; r++) {
				row = sheet.getRow(r);
				
				try{
					for(int j=0; j<values.length; j++)
						values[j] = null;
									
					if (row != null) {
						for(int i=0; i<colIndexes.length; i++){
							XSSFCell cellData = row.getCell(colIndexes[i]);
							if (cellData != null) {
								if(cellData.getCellType() == 1){
									values[i] = cellData.getStringCellValue().trim();
								}else if(cellData.getCellType() == 0){
									values[i] =""+new Double(cellData.getNumericCellValue()).longValue();
								}else{
								}
							}					
						}
						if (Util.hasText(values[20]) && Util.hasText(values[21]) && Util.hasText(values[22])) {
							int numUpdated = GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and SERIALNO is null and owner = "+values[20]);
							if(numUpdated != 1){
								String serial = ((List<String>) GeneralDao.Instance.executeSqlQuery("select SERIALNO from epay.term_pos where code = "+values[21].trim() + " and owner = "+values[20])).get(0);
								if(!serial.equals(values[22].trim())){
									if(force){
										BigDecimal status = ((List<BigDecimal>) GeneralDao.Instance.executeSqlQuery("select status from epay.term_terminal where code = "+values[21].trim())).get(0);

										if(status.equals(new BigDecimal(2))){
											GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and owner = "+values[20]);
											System.out.println("Changing terminal serial no. term: "+ values[21]+" from: "+serial+" to: "+values[22].trim());
										}else{
											if(doublyForced){
												String appver = ((List<String>) GeneralDao.Instance.executeSqlQuery("select app_ver from epay.term_pos where code = "+values[21].trim())).get(0);

												if(Util.hasText(appver)){
													System.out.println("Terminal is installed and we cannot change it's serial no term: "+ values[21]);
												}else{
													GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and owner = "+values[20]);
													System.out.println("Changing terminal serial no. term: "+ values[21]+" from: "+serial+" to: "+values[22].trim());													
												}
											}else{
												System.out.println("Terminal is installed and we cannot change it's serial no term: "+ values[21]);
											}
										}
									}else{
										System.out.println("Terminal has another serila no. Term: "+ values[21] +" Serial: "+values[22]+ " db serail: "+serial);
									}
								}else{
//									System.out.println("Terminal already has the requested serila no. "+ values[21]);
								}
							}
							if(numUpdated == 1){
								System.out.println("Terminal has been updated: "+ values[21]);
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					System.err.println(e);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(e);
		}
		GeneralDao.Instance.endTransaction();
	}
}
