package vaulsys.eft.base.ifxTypeProcessor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.transform.AliasToBeanResultTransformer;
import com.ghasemkiani.util.icu.PersianDateFormat;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.calendar.DayTime;
import vaulsys.eft.util.MsgProcessor;
import vaulsys.message.Message;
import vaulsys.message.MessageType;
import vaulsys.network.channel.base.Channel;
import vaulsys.network.channel.base.InputChannel;
import vaulsys.persistence.GeneralDao;
import vaulsys.persistence.GhasedakGeneralDao;
import vaulsys.protocols.ifx.enums.IfxType;
import vaulsys.protocols.ifx.imp.GhasedakData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.transaction.Transaction;
import vaulsys.webservices.ghasedak.GhasedakItemType;
import vaulsys.webservices.ghasedak.GhasedakRecord;
import vaulsys.webservices.ghasedak.GhasedakRsItem;
import vaulsys.webservices.ghasedak.GhasedakUnitType;
import vaulsys.wfe.ProcessContext;

public class GhasedakProcessor extends MessageProcessor {
	
	private static Logger logger = Logger.getLogger(GhasedakProcessor.class);
	public static final GhasedakProcessor Instance = new GhasedakProcessor();
	private GhasedakProcessor(){};
	
	@Override
	public Message createOutgoingMessage(Transaction transaction, Message incomingMessage, Channel channel,ProcessContext processContext) throws Exception {
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		Message outgoingMessage = new Message(MessageType.OUTGOING);
		outgoingMessage.setChannel(((InputChannel) incomingMessage.getChannel()).getOriginatorChannel());
		
		Ifx incomingIfx = incomingMessage.getIfx();
		GhasedakData ghasedakData = incomingIfx.getGhasedakData();
		List<GhasedakRecord> records = new ArrayList<GhasedakRecord>();
		records = getDataFromGhasedakDB(ghasedakData.getItemType());
		
		List<GhasedakRsItem> rsItemsList = new ArrayList<GhasedakRsItem>();
		GhasedakRsItem rsItem;
		
		for(GhasedakRecord record : records){
			rsItem = new GhasedakRsItem();
			rsItem.setItemType(new GhasedakItemType(record.code.intValue()));
			rsItem.setAmount(record.amount.longValue());
			rsItem.setCurrencyCode(new GhasedakUnitType(record.currency));
			int year = Integer.valueOf(record.creditDate.substring(0, 4));
			int month = Integer.valueOf(record.creditDate.substring(4, 6));
			int day = Integer.valueOf(record.creditDate.substring(6, 8));
			int hour = Integer.valueOf(record.creditDate.substring(8, 10));
			int minute = Integer.valueOf(record.creditDate.substring(10, 12));
			int second = Integer.valueOf(record.creditDate.substring(12, 14));
			DayDate dayDate = new DayDate(year, month, day);
			DayTime dayTime = new DayTime(hour, minute, second);
			DateTime dateTime = new DateTime(dayDate, dayTime);
			rsItem.setCreditDate(dateFormatPers.format(dateTime.toDate()));
			rsItem.setCreditTime(dayTime.toString());
			rsItemsList.add(rsItem);
			rsItem.setGhasedakData(ghasedakData);
			GeneralDao.Instance.saveOrUpdate(rsItem);
		}
		ghasedakData.setGhasedakRsItems(rsItemsList);
		

		Ifx outgoingIfx = MsgProcessor.processor(incomingIfx);
		outgoingIfx.setIfxType(IfxType.getResponseIfxType(outgoingIfx.getIfxType()));
		outgoingIfx.setRsCode("00");
		
		outgoingMessage.setTransaction(transaction);
		outgoingMessage.setIfx(outgoingIfx);
		outgoingMessage.setEndPointTerminal(incomingMessage.getEndPointTerminal());
		
		if (transaction.getFirstTransaction() == null)
			transaction.setFirstTransaction(transaction);
		/*********************************************************/
//		setMessageFlag(outgoingMessage, false, false, true, false);
		outgoingMessage.setRequest(false);
		outgoingMessage.setNeedResponse(false);
		outgoingMessage.setNeedToBeSent(true);
		/*********************************************************/
		transaction.addOutputMessage(outgoingMessage);
		
		GeneralDao.Instance.saveOrUpdate(incomingIfx);
		GeneralDao.Instance.saveOrUpdate(outgoingIfx);
		GeneralDao.Instance.saveOrUpdate(ghasedakData);
		GeneralDao.Instance.saveOrUpdate(outgoingMessage);
        GeneralDao.Instance.saveOrUpdate(outgoingMessage.getMsgXml());
		GeneralDao.Instance.saveOrUpdate(transaction);
		
		return outgoingMessage;
	}

	@Override
	public void messageValidation(Ifx ifx, Message incomingMessage) throws Exception {
	}
	
	public
	static List<GhasedakRecord> getDataFromGhasedakDB(GhasedakItemType type){
		try{
//			GhasedakGeneralDao.Instance.beginTransaction();
			Map<String, Object> params = new HashMap<String, Object>();
			String query = "select "
					+ " item.name as name,"
					+ " itemdata.price as amount,"
					+ " item.unit as currency, "
					+ " itemdata.date as creditDate, "
					+ " item.code as code"
					+ " from "
					+ " Item item inner join item.itemData itemdata "
					+ " where "
					+ " item.code in (:itemCodes)"
					;
			params.put("itemCodes", type.getItemCodes());
			
//			return GhasedakGeneralDao.Instance.find(query, params, new AliasToBeanResultTransformer(GhasedakRecord.class));
			return null;
		}catch(Exception e){
			logger.error(e);
			e.printStackTrace();
		}finally {
//			GhasedakGeneralDao.Instance.endTransaction();
		}
		return null;
		
	}
	
//	private static List<> getDataFromGhasedakDB(GhasedakItemType type){
//		Connection con = null;
//		List<Long> itemCodes ;
//		itemCodes = type.getItemCodes();
//		List<GhasedakRsItem> result = new ArrayList<GhasedakRsItem>();
//		try {
//			con = getConnectionToGhasedakDB();
//			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//			connection.setAutoCommit(false);
//
//			ResultSet records = null;
//			try{
//				Statement stmt = connection.createStatement();
//				records = stmt.executeQuery("select item.name, item.title, item.unit, item_detail.date, item_detail.price " +
//						" from item left outer join item_detail on item.item_detail = item_detail.id  where 1=1 " +
//						" and item.code in ( " + itemCodes + ")");
//				GhasedakRsItem rsItem = null;
//				PersianDateFormat dateFormatPers = new PersianDateFormat("yyMMdd");
//				String date = "";
//				int year = 0;
//				int month = 0; 
//				int day = 0;
//				while(records.next()){
//					rsItem = new GhasedakRsItem();
//					rsItem.setItemType(new GhasedakItemType(records.getInt("name")));
//					rsItem.setAmount(records.getLong("price"));
//					rsItem.setCurrencyCode(new GhasedakCurrencyType(records.getInt("unit")));
//					date = records.getString("date");
//					year = Integer.valueOf(date.substring(0, 3));
//					month = Integer.valueOf(date.substring(4, 5));
//					day = Integer.valueOf(date.substring(5, 6));
//					rsItem.setCreditDate(dateFormatPers.format(new DayDate(year, month, day)));
//					
//					result.add(rsItem);
//				}
//				return result;
//			}catch(Exception e){
//				logger.error(e);
//			}
//			
//		} catch (SQLException e) {
//			e.printStackTrace();
//			logger.error("An error in getting connection to ghasedak DB");
//		}finally{
//			closeConnectionToGhasedakDB(con);
//		}
//		return result;
//	}
	
//	private static Connection getConnectionToGhasedakDB() throws SQLException {
//		try{
//			if (connection == null) {
//				String pass = SecurityComponent.decrypt(ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_PASS));
//				connection = DriverManager.getConnection(ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_URL)
//						, ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_USER), pass);
//			}
//			return connection;
//		}catch(Exception e){
//			logger.error(e);
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	private static void closeConnectionToGhasedakDB(Connection con){
//		if(con != null)
//			 try {
//                   con.close();
//               } catch (SQLException ex) {
//                   logger.debug("Error closing connection!", ex);
//               }
//	}
}
