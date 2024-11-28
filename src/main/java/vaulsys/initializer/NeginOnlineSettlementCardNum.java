package vaulsys.initializer;

import vaulsys.persistence.GeneralDao;
import vaulsys.util.Util;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class NeginOnlineSettlementCardNum {
	public static void main(String[] args) throws Exception {
		String fileName = "C:/Documents and Settings/saber/My Documents/OnlineSettlement/890819.xlsx";
		boolean force = true;
		boolean doublyForced = true;
		
		GeneralDao.Instance.beginTransaction();

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
					"MerchantCode", 	//0
					"TerminalCode",		//1 
					"CardNumber"		//2
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
						if (Util.hasText(values[0]) && Util.hasText(values[1]) && Util.hasText(values[2])) {
							if(values[2].length() != 16){
								System.err.println("Shop: "+ values[0] + " Invalid card: "+values[2]);
							}
								
//							int numUpdated = 1;
							int numUpdated = GeneralDao.Instance.executeSqlUpdate("update epay.fine_financial_entity set card_num = '"+values[2]+"' where " + 
//									"card_num is null and " +
									"code in (select s.code from epay.fine_shop s inner join epay.term_pos p on p.owner=s.code and p.code = "+values[1]+" and s.code="+values[0]+")");
							if(numUpdated != 1){
//								String serial = ((List<String>) GeneralDao.Instance.executeSqlQuery("select SERIALNO from epay.term_pos where code = "+values[21].trim() + " and owner = "+values[20])).get(0);
//								if(!serial.equals(values[22].trim())){
//									if(force){
//										BigDecimal status = ((List<BigDecimal>) GeneralDao.Instance.executeSqlQuery("select status from epay.term_terminal where code = "+values[21].trim())).get(0);
//
//										if(status.equals(new BigDecimal(2))){
//											GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and owner = "+values[20]);
//											System.out.println("Changing terminal serial no. term: "+ values[21]+" from: "+serial+" to: "+values[22].trim());
//										}else{
//											if(doublyForced){
//												String appver = ((List<String>) GeneralDao.Instance.executeSqlQuery("select app_ver from epay.term_pos where code = "+values[21].trim())).get(0);
//
//												if(Util.hasText(appver)){
//													System.out.println("Terminal is installed and we cannot change it's serial no term: "+ values[21]);
//												}else{
//													GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and owner = "+values[20]);
//													System.out.println("Changing terminal serial no. term: "+ values[21]+" from: "+serial+" to: "+values[22].trim());													
//												}
//											}else{
//												System.out.println("Terminal is installed and we cannot change it's serial no term: "+ values[21]);
//											}
//										}
//									}else{
//										System.out.println("Terminal has another serila no. Term: "+ values[21] +" Serial: "+values[22]+ " db serail: "+serial);
//									}
									System.out.println("Cannot updated Term: "+ values[1] +" Shop: "+values[0]+ " Card: "+values[2]);
								}else{
//									System.out.println("Shop updated: "+ values[0] + " Card: "+values[2]);
								}
							}
//							if(numUpdated == 1){
//								System.out.println("Terminal has been updated: "+ values[21]);
//							}
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
