package vaulsys.initializer;

import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.customizationdata.EnhancedParameterData;
import vaulsys.terminal.atm.customizationdata.FITData;
import vaulsys.terminal.atm.customizationdata.ScreenData;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.terminal.atm.customizationdata.TimerData;
import vaulsys.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadConfigData {
	
	public int CONFIG_ID = 110;

	public static void main(String[] args) {
		
		GeneralDao.Instance.beginTransaction();
//		String fileName = "C:/Documents and Settings/saber/My Documents/ATM-Config/TaatATMConfig.2010-02-22.xlsx";
//		String fileName = "C:/Documents and Settings/saber/My Documents/ATM-Config/PasargadATMConfig.2010-03-29.xlsx";
//		String fileName = "C:/Documents and Settings/saber/My Documents/ATM-Config/Yaraneh-PasargadATMConfig.2010-04-07.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/PasargadATMConfigMCI.2010-08-09.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/state table/PasargadATMConfigHoma3.2011-01-27.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/TESTDEPOSIT2.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/GardeshgaryATMConfig.2011-04-07.xlsx";
		
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/state table/TaavonATMConfigEng.2011-05-25.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/state table/lastone/TaavonATMConfigEng.2011-05-25.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/state table/lastone/PasargadATMConfigThirdParty.2011-12-07.xlsx";
		String fileName = "/home/PasargadATMConfigThirdParty.2011-12-07.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/state table/lastone/DEP-TaavonATMConfigEng.2011-05-25.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/PasargadATMConfigMCI.2011-06-06.xlsx";
		fileName = "C:\\Users\\f.gholami\\Desktop\\pasargad\\NCR\\ncr\\PAS-NCR-ATMConfig.xlsx";
//		String fileName = "C:/Documents and Settings/Administrator/Desktop/state table/GardeshgaryATMConfigMCI.2011-08-17.xlsx";
		
		try {
			// new ReadConfigData().startFromAccess(fileName);
			new ReadConfigData().startFromExcel(fileName);
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}

	public void startFromExcel(String fileName) throws IOException {
		XSSFWorkbook wb1;
		try {
			wb1 = new XSSFWorkbook(fileName);
			XSSFSheet sheet;
			
//			sheet = wb1.getSheet("Parameters");
//			System.out.println("------- Reading Parameter Data -------");
//			List<EnhancedParameterData> paramList = readParameterDataFromExcelDB(sheet, fileName);
//
//			sheet = wb1.getSheet("FITS");
//			System.out.println("------- Reading FIT Data -------");
//			List<FITData> fitList = readFITDataFromExcelDB(sheet, fileName);
//			
//			sheet = wb1.getSheet("Timers");
//			System.out.println("------- Reading Timer Data -------");
//			List<TimerData> timerList = readTimerDataFromExcelDB(sheet, fileName);

			sheet = wb1.getSheet("States");
			System.out.println("------- Reading State Data -------");
			List<StateData> staList = readStateDataFromExcelDB(sheet, fileName);

			sheet = wb1.getSheet("Screens");
			System.out.println("------- Reading Screen Data -------");
			List<ScreenData> scrList = readScreenDataFromExcelDB(sheet, fileName);

			
			
			ATMConfiguration atmConfig = getGeneralDao().load(ATMConfiguration.class, 9L);
			
			for (ScreenData scrData : scrList) {
				atmConfig.addScreen(scrData);
			}

			for (StateData staData : staList) {
				atmConfig.addState(staData);
			}
			
//			for (FITData fitData : fitList) {
//				atmConfig.addFIT(fitData);
//			}
//			
//			for (EnhancedParameterData paramData : paramList) {
//				atmConfig.addParam(paramData);
//			}
//			
//			for (TimerData timerData : timerList) {
//				atmConfig.addTimer(timerData);
//			}
			
			getGeneralDao().saveOrUpdate(atmConfig);
			System.out.println("------- DONE! -------");
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<ScreenData> readScreenDataFromExcelDB(XSSFSheet sheet, String fileName) {
		XSSFRow row;
		List<ScreenData> resultList = new ArrayList<ScreenData>();
		int rows; // No of rows
		rows = sheet.getPhysicalNumberOfRows();
		
		System.out.println("the total number of rows are " + rows);
		
		String[] columnNames = new String[]{"NUMBER", "DBVALUE", "CONFIGID", "DESC"};
		int[] colIndexes = new int[columnNames.length];
		String[] values = new String[columnNames.length];
				
		XSSFRow header = sheet.getRow(0);
		
		for(int i=0; i < columnNames.length; i++){
			for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
				if (columnNames[i].equals(header.getCell(c).toString().toUpperCase())) {
					colIndexes[i] = c;
					break;
				}
			}
		}
			
		for (int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			values[0] = values[1] = null;
			
			if (row != null) {
				for(int i=0; i<colIndexes.length; i++){
					XSSFCell cellData = row.getCell(colIndexes[i]);
					if (cellData != null) {
						values[i] = cellData.getStringCellValue();
					}					
				}
				if (Util.hasText(values[0]) && Util.hasText(values[1])) {
					if(Integer.parseInt(values[2]) != CONFIG_ID)
						continue;
					
					String strOut = "r: ";
					for(int i=0; i<values.length; i++)
						strOut += values[i] + ", ";
					
					System.out.println(strOut);
					ScreenData paramData = new ScreenData();
					paramData.setNumber(values[0]);
					paramData.setValue(values[1]);
					paramData.setConfigid(Integer.parseInt(values[2]));
					paramData.setDescription(values[3]);
					resultList.add(paramData);
				}
			}
		}
		return resultList;
	}

	private List<StateData> readStateDataFromExcelDB(XSSFSheet sheet, String fileName) {
		XSSFRow row;
		List<StateData> resultList = new ArrayList<StateData>();
		int rows; // No of rows
		rows = sheet.getPhysicalNumberOfRows();
		
		System.out.println("the total number of rows are " + rows);
		
		String[] columnNames = new String[]{"STATENUMBER", "DBVALUE", "CONFIGID", "DESC"};
		int[] colIndexes = new int[columnNames.length];
		String[] values = new String[columnNames.length];
				
		XSSFRow header = sheet.getRow(0);
		
		for(int i=0; i < columnNames.length; i++){
			for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
				if (columnNames[i].equals(header.getCell(c).toString().toUpperCase())) {
					colIndexes[i] = c;
					break;
				}
			}
		}
			
		for (int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			values[0] = values[1] = null;
			
			if (row != null) {
				for(int i=0; i<colIndexes.length; i++){
					XSSFCell cellData = row.getCell(colIndexes[i]);
					if (cellData != null) {
						values[i] = cellData.getStringCellValue();
					}					
				}
				if (Util.hasText(values[0]) && Util.hasText(values[1])) {
					if(Integer.parseInt(values[2]) != CONFIG_ID)
						continue;

					String strOut = "r: ";
					for(int i=0; i<values.length; i++)
						strOut += values[i] + ", ";
					System.out.println(strOut);
					
					StateData paramData = new StateData();
					paramData.setNumber(values[0]);
					paramData.setValue(values[1]);
					paramData.setConfigid(Integer.parseInt(values[2]));
					paramData.setDescription(values[3]);
					resultList.add(paramData);
				}
			}
		}
		return resultList;
	}
	
	private List<FITData> readFITDataFromExcelDB(XSSFSheet sheet, String fileName) {
		XSSFRow row;
		List<FITData> resultList = new ArrayList<FITData>();
		int rows; // No of rows
		rows = sheet.getPhysicalNumberOfRows();
		
		System.out.println("the total number of rows are " + rows);
		
		String[] columnNames = new String[]{"NUM", "DBVALUE", "CONFIGID", "DESC"};
		int[] colIndexes = new int[columnNames.length];
		String[] values = new String[columnNames.length];
				
		XSSFRow header = sheet.getRow(0);
		
		for(int i=0; i < columnNames.length; i++){
			for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
				if (columnNames[i].equals(header.getCell(c).toString().toUpperCase())) {
					colIndexes[i] = c;
					break;
				}
			}
		}
			
		for (int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			values[0] = values[1] = null;
			
			if (row != null) {
				for(int i=0; i<colIndexes.length; i++){
					XSSFCell cellData = row.getCell(colIndexes[i]);
					if (cellData != null) {
						values[i] = cellData.getStringCellValue();
					}					
				}
				if (Util.hasText(values[0]) && Util.hasText(values[1])) {
					if(values[2].equals("0"))
						continue;

					String strOut = "r: ";
					for(int i=0; i<values.length; i++)
						strOut += values[i] + ", ";
					System.out.println(strOut);
					
					FITData paramData = new FITData();
					paramData.setNumber(values[0]);
					paramData.setValue(values[1]);
					paramData.setConfigid(Integer.parseInt(values[2]));
					paramData.setDescription(values[3]);
					resultList.add(paramData);
				}
			}
		}
				
		return resultList;
	}
	
	private List<EnhancedParameterData> readParameterDataFromExcelDB(XSSFSheet sheet, String fileName) {
		XSSFRow row;
		List<EnhancedParameterData> resultList = new ArrayList<EnhancedParameterData>();
		int rows; // No of rows
		rows = sheet.getPhysicalNumberOfRows();
		
		System.out.println("the total number of rows are " + rows);
		
		String[] columnNames = new String[]{"NUMBER", "DBVALUE", "CONFIGID", "DESC", "DESC2"};
		int[] colIndexes = new int[columnNames.length];
		String[] values = new String[columnNames.length];
				
		XSSFRow header = sheet.getRow(0);
		
		for(int i=0; i < columnNames.length; i++){
			for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
				if (columnNames[i].equals(header.getCell(c).toString().toUpperCase())) {
					colIndexes[i] = c;
					break;
				}
			}
		}
			
		for (int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			values[0] = values[1] = null;
			
			if (row != null) {
				for(int i=0; i<colIndexes.length; i++){
					XSSFCell cellData = row.getCell(colIndexes[i]);
					if (cellData != null) {
						values[i] = cellData.getStringCellValue();
					}					
				}
				if (Util.hasText(values[0]) && Util.hasText(values[1])) {
					if(values[2].equals("0"))
						continue;
					
					String strOut = "r: ";
					for(int i=0; i<values.length; i++)
						strOut += values[i] + ", ";
					System.out.println(strOut);
					
					EnhancedParameterData paramData = new EnhancedParameterData();
					paramData.setNumber(values[0]);
					paramData.setValue(values[1]);
					paramData.setConfigid(Integer.parseInt(values[2]));
					paramData.setDescription(values[3]+":"+values[4]);
					resultList.add(paramData);
				}
			}
		}
				
		return resultList;
	}
	
	private List<TimerData> readTimerDataFromExcelDB(XSSFSheet sheet, String fileName) {
		XSSFRow row;
		List<TimerData> resultList = new ArrayList<TimerData>();
		int rows; // No of rows
		rows = sheet.getPhysicalNumberOfRows();
		
		System.out.println("the total number of rows are " + rows);
		
		String[] columnNames = new String[]{"TIMER NUMBER", "DBVALUE", "CONFIGID", "DESC"};
		int[] colIndexes = new int[columnNames.length];
		String[] values = new String[columnNames.length];
				
		XSSFRow header = sheet.getRow(0);
		
		for(int i=0; i < columnNames.length; i++){
			for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
				if (columnNames[i].equals(header.getCell(c).toString().toUpperCase())) {
					colIndexes[i] = c;
					break;
				}
			}
		}
			
		for (int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			values[0] = values[1] = null;
			
			if (row != null) {
				for(int i=0; i<colIndexes.length; i++){
					XSSFCell cellData = row.getCell(colIndexes[i]);
					if (cellData != null) {
						values[i] = cellData.getStringCellValue();
					}					
				}
				if (Util.hasText(values[0]) && Util.hasText(values[1])) {
					if(values[2].equals("0"))
						continue;

					String strOut = "r: ";
					for(int i=0; i<values.length; i++)
						strOut += values[i] + ", ";
					System.out.println(strOut);
					
					TimerData paramData = new TimerData();
					paramData.setNumber(values[0]);
					paramData.setValue(values[1]);
					paramData.setConfigid(Integer.parseInt(values[2]));
					paramData.setDescription(values[3]);
					resultList.add(paramData);
				}
			}
		}
				
		return resultList;
	}

//	public void startFromAccess(String fileName) throws IOException {
//		System.out.println("------- Opening Access File -------");
//		Database database = null;
//		database = Database.open(new File(fileName.trim()));
//
//		Set<String> tableNames = database.getTableNames();
//
//		ATMConfiguration atmConfig = getGeneralDao().load(ATMConfiguration.class, 1L);
//
//		System.out.println("------- Reading Screen Data -------");
//		List<ScreenData> scrList = readScreenDataFromAccessDB(database, fileName);
//
//		System.out.println("------- Reading State Data -------");
//		List<StateData> staList = readStateDataFromAccessDB(database, fileName);
//
//		for (ScreenData scrData : scrList) {
//			atmConfig.addScreen(scrData);
//		}
//
//		for (StateData staData : staList) {
//			atmConfig.addState(staData);
//		}
//
//		getGeneralDao().saveOrUpdate(atmConfig);
//		if (database != null)
//			database.close();
//
//		System.out.println("------- Done -------");
//	}
//
//	private List<StateData> readStateDataFromAccessDB(Database database, String fileName) throws IOException {
//		Map<String, Object> row;
//		List<StateData> resultList = new ArrayList<StateData>();
//
//		Table stateTable = database.getTable("States");
//		StateData stData;
//
//		Iterator<Map<String, Object>> iterator = stateTable.iterator();
//		while (iterator.hasNext()) {
//			row = iterator.next();
//			stData = new StateData();
//
//			if (row.get("StateNumber") != null)
//				stData.setNumber(row.get("StateNumber").toString());
//			else
//				continue;
//
//			if (row.get("DBValue") != null)
//				stData.setValue(row.get("DBValue").toString());
//			else
//				continue;
//
//			resultList.add(stData);
//		}
//
//		return resultList;
//	}
//
//	private List<ScreenData> readScreenDataFromAccessDB(Database database, String fileName) throws IOException {
//		Map<String, Object> row;
//		List<ScreenData> resultList = new ArrayList<ScreenData>();
//
//		Table screenTable = database.getTable("Screens");
//		ScreenData scrData;
//
//		Iterator<Map<String, Object>> iterator = screenTable.iterator();
//		while (iterator.hasNext()) {
//			row = iterator.next();
//			scrData = new ScreenData();
//			if (row.get("Number") != null)
//				scrData.setNumber(row.get("Number").toString());
//			else
//				continue;
//			if (row.get("DBValue") != null)
//				scrData.setValue(row.get("DBValue").toString());
//			else
//				continue;
//
//			resultList.add(scrData);
//		}
//
//		return resultList;
//	}

	private GeneralDao getGeneralDao() {
		return GeneralDao.Instance;
	}
}
