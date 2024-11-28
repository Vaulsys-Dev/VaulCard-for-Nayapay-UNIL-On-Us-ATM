package vaulsys.mtn.util.charge10000RL;


import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.GeneralDao;
import org.apache.log4j.Logger;

import javax.crypto.Cipher;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class MCI10000RCharge {

    public static  Logger logger = Logger.getLogger(MCI10000RCharge.class);

    private static Map<String, String> monthNametoMonthNum;

    static {
        monthNametoMonthNum = new HashMap<String, String>();
        monthNametoMonthNum.put("JAN", "01");
        monthNametoMonthNum.put("FEB", "02");
        monthNametoMonthNum.put("MAR", "03");
        monthNametoMonthNum.put("APR", "04");
        monthNametoMonthNum.put("MAY", "05");
        monthNametoMonthNum.put("JUN", "06");
        monthNametoMonthNum.put("JUL", "07");
        monthNametoMonthNum.put("AGU", "08");
        monthNametoMonthNum.put("SEP", "09");
        monthNametoMonthNum.put("OCT", "10");
        monthNametoMonthNum.put("NOV", "11");
        monthNametoMonthNum.put("DEC", "12");
    }

    public static PrivateKey getPrivateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("JCEKS");

        char[] password = "$3cureP@$$".toCharArray();
        InputStream is = new FileInputStream("LMK.jceks");

        store.load(is, password);

        PrivateKey priv = (PrivateKey) store.getKey("private-key", password);
        return priv;
    }

    public static void main(String[] args) throws Exception {
        String inFileName;

        if (args.length < 1) {
            inFileName = "C:/Users/Kamelia/Desktop/Tasks/Task57-mci1000Format/test/output206_33_37.dat.decr.encrypted";
        } else {
            inFileName = args[0];
        }

        Provider provider = (Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance();
        Security.addProvider(provider);

        PrivateKey priv = getPrivateKey();

        parseAndSaveCharges(new File(inFileName));

        Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, priv);

//		byte[] plainText = cipher.doFinal(Hex
//						.decode("76e058e4a53c5eb99e4f02bd6782cdd2fb3383a57c51d6c11ebcc5cf66b14740b589fea1ec066104ce302a2e3754735032452b312841f674180ce0cf20938c2a6297f556400a3bd3a2ba1a2ced2b027f71aa1905a64f7c4b37f191e839a51add29cd917c0a17e07559203fe998e8dee8cf1cfaa9c198ed145a3cdbd02c40042d"));
//		System.out.println("plain : " + new String(plainText));
        System.out.println("Completed Successfully...");
        System.exit(0);
    }

    public static String parseAndSaveCharges(File fileIn) throws Exception {
        //Session session = GeneralDao.Instance.getCurrentSession();
        // Transaction trx = session.beginTransaction();
        StringBuilder errBuilder = new StringBuilder();
        Long entity = 9913L;
        long count[] = new long[6];
        //org.hibernate.Query q = session.createQuery("select mtn from MTNCharge mtn where mtn.cardSerialNo = :serialNo");
        String query = "select mtn from MTNCharge mtn where mtn.cardSerialNo = :serialNo and mtn.entity=:org";
        Map<String, Object> param = new HashMap<String, Object>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileIn));
            String line = "";

            int rowNo = 0;
            int quantity = 0;
            /************************************** parse charge data ************************************/
            GeneralDao.Instance.beginTransaction();
            while ((line = br.readLine()) != null) {
                MTNCharge charge = new MTNCharge();

                rowNo++;
                Organization org = OrganizationService.findOrganizationByCode(entity, OrganizationType.MTNIRANCELL);
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                String cardSerialNo = tokenizer.nextToken().trim();
                //q.setParameter("serialNo", Long.valueOf(cardSerialNo));
                param.put("serialNo", Long.valueOf(cardSerialNo));
                param.put("org",org);
                MTNCharge mtnCharge = (MTNCharge) GeneralDao.Instance.findUnique(query, param);
                if (mtnCharge != null) {
                    logger.error("duplicate record in file : " + cardSerialNo);
                    errBuilder.append("duplicate record in file row number: ").append(rowNo).append("  ");
                    continue;
                }

                charge.setCardSerialNo(Long.valueOf(cardSerialNo));
                /*************************************************/
                String batch = cardSerialNo.substring(0, 6);
                Integer fileId = new Long(Long.parseLong(batch) % 10000).intValue();
                charge.setFileId(fileId);
                /*************************************************/
                charge.setCardPIN(tokenizer.nextToken());
                /*************************************************/
                String pinType = tokenizer.nextToken();
                /*************************************************/
                String credit = tokenizer.nextToken().trim();
                credit = credit.substring(0, credit.indexOf("."));    //10000.0
                charge.setCredit(Long.valueOf(credit));
                /*************************************************/
                String currency = tokenizer.nextToken().trim();
                /******************* expiryDate ******************/
                String dateToken = tokenizer.nextToken().trim();
                String[] date = dateToken.split("-");
                String day = date[0];
                String month = monthNametoMonthNum.get(date[1]);
                String year = date[2];
                String expiry = year + month + day;
                charge.setYear(Integer.valueOf(year));
                /*************************************************/
                charge.setPinlen(Integer.parseInt(tokenizer.nextToken().trim()));    //14

                charge.setEntity(org);
                charge.setStateDate(DateTime.now());
                charge.setState(MTNChargeState.LOCKED);

                try {
                    GeneralDao.Instance.save(charge);
                    logger.info("charge with serialNo: " + cardSerialNo + " inserted in dateBase!");
                    if (rowNo % 50 == 0) {
                        logger.info("Importing Record No: " + rowNo);
                        GeneralDao.Instance.flush();
                    }

                    if ((rowNo % 1000) == 0) {
                        try {
                            GeneralDao.Instance.endTransaction();
                            GeneralDao.Instance.beginTransaction();
                        } catch (Exception e) {
                            logger.error(e);
                            errBuilder.append(e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Charge has not been added to DB: Line[" + rowNo + "] SerialNo:" + charge.getCardSerialNo());
                    errBuilder.append(e.getMessage());
                }
            }
            /*if (rowNo < 50)
                GeneralDao.Instance.flush();*/
            GeneralDao.Instance.flush();
            GeneralDao.Instance.endTransaction();


            /*********************************************************************************************/
            logger.info("rowNo: " + rowNo);
        } catch (Exception e) {
            logger.fatal(e);
            errBuilder.append(e.getMessage());
        }
        logger.info("=================");
        logger.info(Arrays.toString(count));

        return errBuilder.toString();
    }

}
