package vaulsys.mtn.util.rightel;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.MTNCharge;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.GeneralDao;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.crypto.Cipher;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.util.*;

public class RightelCharge {

    public static Logger logger = Logger
            .getLogger(RightelCharge.class);


    public static PrivateKey getPrivateKey() throws Exception {
        KeyStore store = KeyStore.getInstance("JCEKS");
        // char[] password = "tHiS!s#aPa$$w0&F@r#nCr*ps!0N".toCharArray();
        // InputStream is = new FileInputStream("private.key");

        char[] password = "$3cureP@$$".toCharArray();
        InputStream is = new FileInputStream("LMK.jceks");

        store.load(is, password);

        PrivateKey priv = (PrivateKey) store.getKey("private-key", password);
        return priv;
    }

    public static void main(String[] args) throws Exception {
        String inFileName;

        if (args.length < 1) {
            inFileName = "E:/Share/Mrs.Pakravan/Rightel/Test/realTest/Decrypt of 220120801001_dst.dat.encrypted";
        } else {
            inFileName = args[0];
        }

        Provider provider = (Provider) Class.forName(
                "org.bouncycastle.jce.provider.BouncyCastleProvider")
                .newInstance();
        Security.addProvider(provider);

        PrivateKey priv = getPrivateKey();

        parseAndSaveCharges(new File(inFileName));

        Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, priv);

//		byte[] plainText = cipher
//				.doFinal(Hex
//						.decode("4e55413ad1448d16f6ea7a473b6e5feee46463909cc44f1eeddfea1bc0d35e6012307d6e3879a58573e2b1ad0a6a5affa914e7e24d7b4dea31ea74b9c9c48930da4659eb4aec38e3e50455f06ae0e11f51a91543832f5e5b2c752f0242321bf9f7819be7bc07df1570156202b8f8ba8d60cec829eb6c252f2a88183046e92307"));
//		System.out.println("plain : " + new String(plainText));
        System.out.println("Completed Successfully...");
        System.exit(0);
    }

    public static String parseAndSaveCharges(File fileIn) throws Exception {
        Session session = GeneralDao.Instance.getCurrentSession();

        StringBuilder errBuilder = new StringBuilder();
        Long entity = 9920L;
        long count[] = new long[6];

        List<String> headers = new ArrayList<String>();
        headers.add("quantity");
        headers.add("par value");
        headers.add("batchno");
        headers.add("startcardno");
        headers.add("create date");

        //org.hibernate.Query q = session.createQuery("select mtn from MTNCharge mtn where mtn.cardSerialNo = :serialNo");
        String query = "select mtn from MTNCharge mtn where mtn.cardSerialNo = :serialNo and mtn.entity=:org";
        Map<String, Object> param = new HashMap<String, Object>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileIn));
            String line = "";

            int rowNo = 0;
            int quantity = 0;
            Long credit = 0L;
            int year = 0;
            /***************************************** parse header **************************************/
            try {
                for (int i = 0; i < headers.size(); i++) {
                    line = br.readLine();
                    logger.info("Parsing Line: " + line);
                    String[] split = line.split(":");
                    if (headers.contains(split[0])) {
                        if ("quantity".equals(split[0].toLowerCase())) {
                            quantity = Integer.parseInt(split[1].trim());
                        } else if ("par value".equals(split[0].toLowerCase())) {
                            credit = Long.parseLong(split[1].trim());
                        } else if ("create date".equals(split[0].toLowerCase())) {
                            year = Integer.parseInt(split[1].substring(0, 4));
                        }
                        rowNo++;
                    } else
                        break;
                }
            } catch (Exception e) {
                logger.error(e);
                errBuilder.append(e.getMessage());
            }
            /*********************************************************************************************/


            /************************************** parse charge data ************************************/
            GeneralDao.Instance.beginTransaction();
            while ((line = br.readLine()) != null) {
                MTNCharge charge = new MTNCharge();
//				line = br.readLine();

                rowNo++;

                if (line.toLowerCase().contains("start")) {
                    logger.info("Start parsing charge data and save ....");
                    continue;
                }
                if (line.toLowerCase().contains("end")) {
                    logger.info("End parsing charge data and save ....");
                    break;
                }
                StringTokenizer tokenizer = new StringTokenizer(line, ";");
                Organization org = OrganizationService.findOrganizationByCode(entity, OrganizationType.MTNIRANCELL);
                if (tokenizer.hasMoreTokens()) {
                    String cardSerialNo = tokenizer.nextToken().trim();

                    param.put("serialNo", Long.valueOf(cardSerialNo));
                    param.put("org", org);
                    MTNCharge mtnCharge = (MTNCharge) GeneralDao.Instance.findUnique(query, param);
                    //q.setParameter("serialNo", Long.valueOf(cardSerialNo));
                    if (mtnCharge != null) {
                        logger.error("duplicate record in file : " + cardSerialNo);
                        errBuilder.append("duplicate:").append(rowNo).append("  ");
                        continue;
                    }
                    charge.setCardSerialNo(Long.valueOf(cardSerialNo));
                    charge.setCardPIN(tokenizer.nextToken());
                    tokenizer.nextToken();
                    charge.setPinlen(Integer.parseInt(tokenizer.nextToken()));
                    if (credit != 0)
                        charge.setCredit(credit);
                    else {
                        logger.error("Charge file don't define charge credit(par value).Any charge saved!");
                        break;
                    }
                    charge.setYear(year);
                }

                charge.setEntity(org);
                charge.setStateDate(DateTime.now());
                charge.setState(MTNChargeState.LOCKED);

                try {
                    session.save(charge);
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
                            logger.error(e.getMessage());
                            errBuilder.append(e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.error("Charge has not been added to DB: Line["
                            + rowNo + "] SerialNo:" + charge.getCardSerialNo());
                    logger.error(e.getStackTrace());
                    errBuilder.append(e.getMessage());
                }
            }

            // if (rowNo < 50) {
            GeneralDao.Instance.flush();
            GeneralDao.Instance.endTransaction();
            // }

            /*********************************************************************************************/
            logger.info("rowNo: " + rowNo);
        } catch (Exception e) {
            logger.fatal(e);
            GeneralDao.Instance.endTransaction();
            errBuilder.append(e.getMessage());
            throw new RuntimeException(e);
        }
        logger.info("=================");
        logger.info(Arrays.toString(count));

        return errBuilder.toString();
    }

}
