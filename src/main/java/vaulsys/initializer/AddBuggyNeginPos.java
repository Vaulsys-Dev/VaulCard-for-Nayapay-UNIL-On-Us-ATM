package vaulsys.initializer;

import vaulsys.calendar.DateTime;
import vaulsys.contact.Address;
import vaulsys.contact.City;
import vaulsys.contact.Contact;
import vaulsys.contact.Country;
import vaulsys.contact.PhoneNumber;
import vaulsys.contact.State;
import vaulsys.customer.Account;
import vaulsys.customer.AccountType;
import vaulsys.customer.Core;
import vaulsys.customer.Currency;
import vaulsys.customer.CustomerService;
import vaulsys.entity.Contract;
import vaulsys.entity.MerchantCategory;
import vaulsys.entity.impl.FinancialEntity;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.entity.impl.Shop;
import vaulsys.entity.impl.Visitor;
import vaulsys.entity.impl.VisitorType;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.NeginGeneralDao;
import vaulsys.terminal.impl.POSTerminal;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AddBuggyNeginPos {
	public static Map<String, MerchantCategory> merchantCategoryMap = new HashMap<String, MerchantCategory>();
	public static Map<String, City> cityMap = new HashMap<String, City>();
	public static Map<String, Country> countryMap = new HashMap<String, Country>();
	public static Map<String, State> stateMap = new HashMap<String, State>();
	public static Map<String, Visitor> visitorMap = new HashMap<String, Visitor>();
	public static List<String> migratedBranches = new ArrayList<String>();
	
	public static void main(String[] args) {
//		addPosInExcelButNotInDB();
//		updateAddressOrName();
//		insertNeginPosInfo();
		updateKargozar();
	}

	private static void updateKargozar() {
		String fileName = "C:/Documents and Settings/saber/My Documents/MigrationPasargad/old/kishware_convert2.xlsx";
		
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
					"tedad_trx", 		//14
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
					if (columnNames[i].equalsIgnoreCase(header.getCell(c).toString())) {
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
						if (Util.hasText(values[0]) && Util.hasText(values[1]) && values[8].equals("E")) {
//							int numUpdated = GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and SERIALNO is null");
//							int numUpdated = GeneralDao.Instance.executeSqlUpdate("insert into epay.kishwarepos(terminal, trxcount, NGNMERCHANTCODE, NGNTERMINALCODE) values ("+values[21].trim()+","+values[14].trim()+","+values[0].trim()+","+values[1].trim()+")");
							Visitor visitor = getVisitor(values[15].replaceAll("ي", "ی"));
							if(visitor != null){
								int numUpdated = GeneralDao.Instance.executeSqlUpdate("update epay.fine_shop s set s.visitor = "+visitor.getId()+" where s.code = "+
										"(select p.owner from epay.kishwarepos k inner join epay.term_pos p on p.code= k.terminal where k.ngnterminalcode = "+values[1]+")");

									if(numUpdated != 1){
										System.out.println("Err Term: "+ values[1] +" Kargozar: "+values[15]);
//										System.out.println("num updated: "+numUpdated);
									}
									if(numUpdated == 1){
										System.out.println("Updated Term: "+ values[1] +" Kargozar: "+values[15]);
									}								
							}else{
								System.out.println("Visitor not found: "+values[15]);
							}
						}
					}
				}catch(Exception e){
//					e.printStackTrace();
					System.err.println(e);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(e);
		}
		GeneralDao.Instance.endTransaction();
	}

	private static void insertNeginPosInfo(){
		String fileName = "C:/Documents and Settings/saber/My Documents/MigrationPasargad/KishwarePos-remained.xlsx";
		
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
					"tedad_trx", 		//14
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
					if (columnNames[i].equalsIgnoreCase(header.getCell(c).toString())) {
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
						if (Util.hasText(values[0]) && Util.hasText(values[1]) && Util.hasText(values[20]) && Util.hasText(values[21]) && Util.hasText(values[22])) {
//							int numUpdated = GeneralDao.Instance.executeSqlUpdate("update epay.term_pos set SERIALNO = '"+values[22].trim()+ "' where code = "+values[21].trim() + " and SERIALNO is null");
							int numUpdated = GeneralDao.Instance.executeSqlUpdate("insert into epay.kishwarepos(terminal, trxcount, NGNMERCHANTCODE, NGNTERMINALCODE) values ("+values[21].trim()+","+values[14].trim()+","+values[0].trim()+","+values[1].trim()+")");

							if(numUpdated != 1){
								System.out.println("Term: "+ values[21] +" Serial: "+values[22]);
								System.out.println("num updated: "+numUpdated);
							}
							if(numUpdated == 1){
								System.out.println("num updated: "+numUpdated);
							}
						}
					}
				}catch(Exception e){
//					e.printStackTrace();
					System.err.println(e);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(e);
		}
		GeneralDao.Instance.endTransaction();
	}
	
	private static void updateAddressOrName() {
		GeneralDao.Instance.beginTransaction();
		List<FinancialEntity> shops = (List<FinancialEntity>) GeneralDao.Instance.find("from FinancialEntity f where f.parentGroupId = 386595401");
		for(FinancialEntity e:shops){
			String address = e.getSafeAddress();
			String[] split = address.split(" ");
			for(String part:split){
				if(part.length() > 20){
					System.out.println("code: "+e.getCode());
					break;
				}
			}
		}
		
		GeneralDao.Instance.rollback();
	}

	private static void addPosInExcelButNotInDB() {
		String fileName = "C:/Documents and Settings/saber/My Documents/MigrationPasargad/KishwarePos-last-part-00001-10000.xlsx";

		migratedBranches.add("244");
		migratedBranches.add("325");
		migratedBranches.add("327");
		migratedBranches.add("316");
		migratedBranches.add("330");
		migratedBranches.add("331");
		migratedBranches.add("326");
		migratedBranches.add("328");
		migratedBranches.add("264");
		migratedBranches.add("334");
		
		
		GeneralDao.Instance.beginTransaction();
		List<String[]> posList=null;
		try {
			posList = readExcellFile(fileName);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(0);
		}
		GeneralDao.Instance.commit();
	}
	
	private static void addPosToDB(FinancialEntityGroup shopGroup, 
			TerminalGroup terminalGroup, 
			Currency currency,
			String[] posData, 
			XSSFRow row
			) throws Exception {
		
//		0	"CardAcceptor", 
//		1	"TerminalID", 
//		2	"Seporde", 
//		3	"Address", 
//		4	"Phone", 
//		5	"SerialNo", 
//		6	"Name_pazirandeh", 
//		7	"CAPOSTCODE", 
//		8	"EnableOrDisable", 
//		9	"CITYCODE", 
//		10	"CITYNAME", 
//		11	"City_FromGroup", 
//		12	"Ostan_FromGroup", 
//		13	"IsSagem", 
//		14	"tedad_trx", 
//		15	"KARGOZAR", 
//		16	"SENF",
//		17	"SENF2",
//		18	"Country"
		
		
		if(isShopExists(posData[20]) != null)
			return;
		
		System.out.println("Shop not exists: "+posData[20]);
		
		String accountNum = posData[2];
		String accountId = "";
		AccountType accountType;
		if(accountNum.startsWith("D")){
			accountType = AccountType.DEPOSIT;
		}else if(accountNum.startsWith("A")){
			accountType = AccountType.ACCOUNT;
		}else{
			throw new Exception("Unkown account type..."+accountNum);
		}
		
		accountId += Integer.parseInt(accountNum.substring(1, 5));
		accountId += "-";
		accountId += Integer.parseInt(accountNum.substring(5, 8));
		accountId += "-";
		accountId += Integer.parseInt(accountNum.substring(8, 16));
		accountId += "-";
		accountId += Integer.parseInt(accountNum.substring(16, 19));
		
		
		if(!posData[8].equals("E"))
			return;
		
		Shop shop = createShopBuggy(
				posData[6], 		//name
				shopGroup,
				accountId, 
				currency,
				Core.NEGIN_CORE,
				accountType,
				posData[17],		//categoryName
				posData[11],		//cityName
				posData[12],		//stateName
				posData[18],		//countryName
				posData[3],			//address
				posData[4],			//phoneNumber
				posData[15],		//visitorName
				posData[20]		//MerchantCode
				);
		GeneralDao.Instance.saveOrUpdate(shop);
		System.out.println("------- Creating SHOP " + shop.getCode() + "-------");
		
		XSSFCell cellData = row.createCell(20);
		cellData.setCellValue(shop.getCode().toString());

		POSTerminal pos = createPOS(
				shop, 
				terminalGroup,
				posData[21]		//TerminalCode
				);
		
		GeneralDao.Instance.saveOrUpdate(pos);
		System.out.println("------- Creating POS " + pos.getCode() + "-------");		
		cellData = row.createCell(21);
		cellData.setCellValue(pos.getCode().toString());
	}

	private static FinancialEntity isShopExists(String code) {
		String query = "from  "+FinancialEntity.class.getName()+" f where f.code = :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", Long.parseLong(code));
		FinancialEntity entity = (FinancialEntity) GeneralDao.Instance.findObject(query, param);
		return entity;
	}

	private static Shop createShopBuggy(
			String name, 
			FinancialEntityGroup parent,
			String accountId,
			Currency currency,
			Core core,
			AccountType accountType,
			String categoryName,
			String cityName,
			String stateName,
			String countryName,
			String address,
			String phoneNumber,
			String visitorName,
			String merchantCode) throws Exception{
		Shop shop = new Shop();
		shop.setCode(Long.parseLong(merchantCode));
		shop.setName(name);
		shop.setParentGroup(parent);
		shop.setSharedFeature(parent.getSafeSharedFeature());
		
		MerchantCategory category = getMerchantCategory(categoryName);
		if(category == null)
			throw new Exception("category is not valid"+categoryName);
		shop.setCategory(category);
		
		City city = findCity(cityName);
		if(city == null)
			throw new Exception("city is not valid"+cityName);

		State state = findState(stateName);
		if(state == null)
			throw new Exception("state is not valid"+stateName);

		Country country = findCountry(countryName);
		if(country == null)
			throw new Exception("country is not valid"+country);

		Address addr = new Address(city, state, country, null, address);
//		if(phoneNumber == null || !Util.hasText(phoneNumber.trim()))
//			throw new Exception("phoneNumber is not valid"+phoneNumber);
		
		PhoneNumber phone = null;
		if(phoneNumber != null && Util.hasText(phoneNumber.trim()))
			phone = new PhoneNumber("", phoneNumber);
		
		Contact contact = new Contact("-", addr, phone, null, null);
		shop.setContact(contact);
		

		DateTime now = DateTime.now();
		
		Contract contract = new Contract(now.getDayDate());
		shop.setContract(contract);
		
		Visitor visitor = getVisitor(visitorName);
		if(visitor == null)
			throw new Exception("visitor is not valid"+visitor);

		shop.setVisitor(visitor);
		
		VisitorType visitorType=VisitorType.SUPPORTER;
		shop.setVisitorType(visitorType);
		
//		FinancialEntity e = isDuplicateMerchant(accountId);
//		if(e != null){
//			throw new Exception("Duplicate merchant: "+e.getCode());
//		}
		
		String ownerName = checkAccount(accountId);
		if(!Util.hasText(ownerName)){
			String[] split = accountId.split("-");
			String branch = split[0];

			if(migratedBranches.contains(branch))
				throw new Exception("Account may be migrated: "+accountId);
			else
				throw new Exception("Account is not valid: "+accountId);
		}
		
		Account account = new Account(ownerName, accountId, currency, core, accountType);		
		shop.setAccount(account);

		
		shop.setEnabled(true);
		shop.setCreatedDateTime(now);
		shop.setCreatorUser(DBInitializeUtil.getUser());
		
		return shop;
	}
	
	private static POSTerminal createPOS(Shop shop, TerminalGroup terminalGroup, String terminalCode) {
		POSTerminal posTerminal = new POSTerminal();

		posTerminal.setCode(Long.parseLong(terminalCode));
		
		posTerminal.setCreatedDateTime(DateTime.now());
		posTerminal.setCreatorUser(DBInitializeUtil.getUser());
		posTerminal.setEnabled(true);
		posTerminal.setOwner(shop);
		posTerminal.setParentGroup(terminalGroup);
		posTerminal.setSharedFeature(terminalGroup.getSafeSharedFeature());
		return posTerminal;
	}

	private static FinancialEntity isDuplicateMerchant(String accountId) {
		String query = "from  "+FinancialEntity.class.getName()+" f where f.account.accountNumber = :acct";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("acct", accountId);
		return (FinancialEntity) GeneralDao.Instance.findObject(query, param);
	}
	
	private static String checkAccount(String accountId) {
		String[] split = accountId.split("-");
		String branch = split[0];
		String accType = split[1];
		String customer = split[2];
		String serial = split[3];

		List<String> accountHolder = (List<String>) NeginGeneralDao.Instance.executeSqlQuery("select TDTITLE from hesab where ABRNCHCOD="+branch+" and TBDPTYPE="+accType+" and CFCIFNO="+customer+" and TDSERIAL="+serial+"  and AISTATE=1");
		if(accountHolder != null && accountHolder.size() > 0)
			return accountHolder.get(0);
		
		return null;
	}

	public static City findCity(String name) {
		if(cityMap.containsKey(name)){
			return cityMap.get(name);
		}

		String query = "from " + City.class.getName() + " c where c.name= :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		City city = (City) GeneralDao.Instance.findUnique(query, param);
		
		cityMap.put(name, city);
		return city;
	}

	public static State findState(String name) {
		if(stateMap.containsKey(name)){
			return stateMap.get(name);
		}
		
		String query = "from " + State.class.getName() + " c where c.name= :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		State state = (State) GeneralDao.Instance.findObject(query, param);
		
		stateMap.put(name, state);
		return state;
	}

	public static Country findCountry(String name) {
		if(countryMap.containsKey(name)){
			return countryMap.get(name);
		}

		String query = "from " + Country.class.getName() + " c where c.name= :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		Country country = (Country) GeneralDao.Instance.findUnique(query, param);
		
		countryMap.put(name, country);
		return country;
	}


	private static TerminalGroup getTerminalGroup(String name) {
		String query = "from " + TerminalGroup.class.getName() + " c where c.name= :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		return (TerminalGroup) GeneralDao.Instance.findUniqueObject(query, param);
	}

	private static Visitor getVisitor(String name) {
		if(visitorMap.containsKey(name)){
			return visitorMap.get(name);
		}

		String query = "from " + Visitor.class.getName() + " c where c.name= :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		Visitor visitor= (Visitor) GeneralDao.Instance.findUniqueObject(query, param);
		
		visitorMap.put(name, visitor);
		return visitor;		
	}

	private static FinancialEntityGroup getFinancialEntityGroup(String name) {
		String query = "from " + FinancialEntityGroup.class.getName() + " c where c.name= :name";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("name", name);
		return (FinancialEntityGroup) GeneralDao.Instance.findUniqueObject(query, param);
	}

	private static MerchantCategory getMerchantCategory(String name) {
		if(merchantCategoryMap.containsKey(name)){
			return merchantCategoryMap.get(name);
		}
		
		String query = "from " + MerchantCategory.class.getName() + " ap where ap.name = :name ";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		MerchantCategory merchantCategory = (MerchantCategory) GeneralDao.Instance.findObject(query, param);
		
		merchantCategoryMap.put(name, merchantCategory);
		return merchantCategory;
	}

	private static List<String[]> readExcellFile(String fileName) throws Exception {

		FinancialEntityGroup shopGroup = getFinancialEntityGroup("فروشگاه های منتقل شده از نگین");
		TerminalGroup terminalGroup = getTerminalGroup("ترمینال های منتقل شده از نگین");
		Currency currency = CustomerService.findCurrency(364);

		
		List<String[]> results = new ArrayList<String[]>();
		XSSFWorkbook wb1 = new XSSFWorkbook(fileName);
		
		try {			
			XSSFSheet sheet = wb1.getSheetAt(0);
//			XSSFSheet sheet = wb1.getSheet("Kishwarepos");
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
					"Error"				//22
					};
			int[] colIndexes = new int[columnNames.length];
			String[] values = new String[columnNames.length];
					
			XSSFRow header = sheet.getRow(0);
			
			for(int i=0; i < columnNames.length; i++){
				for (int c = 0; c < header.getPhysicalNumberOfCells(); c++) {
					if (columnNames[i].equalsIgnoreCase(header.getCell(c).toString())) {
						colIndexes[i] = c;
						break;
					}
				}
			}
				
			for (int r = 1; r < rows; r++) {
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
						if (Util.hasText(values[0]) && Util.hasText(values[1]) && values[8].equals("E") && Util.hasText(values[20]) && Util.hasText(values[21])) {
							
							String strOut = "r("+r+"): ";
							for(int i=0; i<values.length; i++)
								strOut += values[i] + ", ";
							System.out.println(strOut);
							
							
							try{
								addPosToDB(shopGroup, terminalGroup, currency, values, row);
								results.add(values);
							}catch(Exception e){
								XSSFCell cellData = row.createCell(22);
								cellData.setCellValue(e.getMessage());
							}
						}
					}
				}catch(Exception e){
					XSSFCell cellData = row.createCell(22);
					cellData.setCellValue(e.getMessage());
				}

			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(e);
			throw new Exception(e);
		}finally{
//			FileOutputStream file = new FileOutputStream(fileName.substring(0, fileName.length()-5)+"_"+System.currentTimeMillis()+".xlsx");
//			wb1.write(file);
//			file.close();
		}
		return results;
	}
}
