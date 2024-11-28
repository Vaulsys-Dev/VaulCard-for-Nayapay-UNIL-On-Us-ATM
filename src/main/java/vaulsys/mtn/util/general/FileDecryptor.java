package vaulsys.mtn.util.general;

import vaulsys.mtn.util.irancell.IranCellMTNCharge;
import vaulsys.mtn.util.irancell.hibernate.HibernateUtil;
import vaulsys.mtn.util.irancell.hibernate.MySession;
import vaulsys.calendar.DateTime;
import vaulsys.mtn.InsertedChargeReport;
import vaulsys.security.component.SecurityComponent;
import vaulsys.user.User;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.encoders.Hex;
import org.apache.log4j.Logger;
import org.hibernate.Query;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FileDecryptor {

    public static final Logger logger = Logger.getLogger(FileDecryptor.class);
    public static final String DELIMITER = ",";
    public static final Long CREDIT_BASE = 10000L;
    public static final String INVALID_FORMAT = ".";
    public static final String FILE_PAS = ".csv";

    public static final String ERR_COMPANY = "[Invalid_Comapany   ]: ";
    public static final String ERR_CREDIT = "[Invalid_Credit     ]: ";
    public static final String ERR_SERIAL1 = "[Repetitive_Serial  ]: ";
    public static final String ERR_SERIAL2 = "[Invalid_Serial     ]: ";
    public static final String ERR_PIN = "[Invalid_Pin        ]: ";
    public static final String ERR_PROVIDER = "[Invali_Provider    ]: ";
    public static final String ERR_UNKNOWN = "[Unknown_Error      ]: ";

    public static final List<String> validCompany = new ArrayList<String>() {{
        add("9912");
        add("9913");
        add("9920");
        add("9935");
        add("9936");
        add("9932");
    }};

    public static void main(String[] args) throws Exception {
        String path = "D:\\mirkamali\\Charge\\test\\insertSample11.csv";

        String recordsWithErr = parseAndSaveCharges(path, null);
        String errFilePath = path.substring(0, path.indexOf(FILE_PAS)) + "Error" + FILE_PAS;
        /********************************************/
        File file = new File(errFilePath);
        BufferedWriter errors = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e);
            }
        }

        try {
            errors = new BufferedWriter(new FileWriter(file));
        } catch (IOException e2) {
            e2.printStackTrace();
            logger.error(e2);
        }
        try {
            errors.append(recordsWithErr);
            errors.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e);
        }
        /********************************************/

    }

    public static String parseAndSaveCharges(String path, User user) throws Exception {
        MySession session = HibernateUtil.getCurrentSession();

        StringBuilder error = new StringBuilder();
        String line = "";
        int rowNo = 1;
        int rowNoBatch = 0;
        int invalidRowNo = 0;
        InsertedChargeReport chargeData = new InsertedChargeReport();
        Long start = System.currentTimeMillis();
        ChargeField chargeField = null;
        try {
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            IranCellMTNCharge charge;
            List<IranCellMTNCharge> chargesList = new ArrayList<IranCellMTNCharge>();
            List<Long> serials = new ArrayList<Long>();
            List<Long> chargesInDB = new ArrayList<Long>();
            logger.info("Reading Header ignore it ...");
            br.readLine();
            session.beginTransaction();
            while (br.ready()) {
                if ((line = br.readLine()).length() > 0) {
                    charge = new IranCellMTNCharge();
                    chargeField = new ChargeField();
                    try {
                        rowNo++;
                        //logger.debug("processing charge in line: " + rowNo);
                        String[] s = line.split(DELIMITER);
                        int i = -1;
                        //CompanyCode
                        chargeField.company = s[++i];
                        if (!validCompany.contains(chargeField.company)) {
                            error.append(ERR_COMPANY).append(line).append("\r\n");
                            invalidRowNo++;
                            continue;
                        }
                        //Credit
                        chargeField.credit = Long.valueOf(s[++i]);
                        if (chargeField.credit % CREDIT_BASE != 0L) {
                            error.append(ERR_CREDIT).append(line).append("\r\n");
                            invalidRowNo++;
                            continue;
                        }
                        //pin_length
                        chargeField.pinLen = Integer.valueOf(s[++i]);
                        ++i;    //unessential batchNo
                        //Serial
                        chargeField.serial = Long.valueOf(s[++i]);
                        //pin
                        ++i;    //unessential pin
                        chargeField.pin = s[++i];
                        ///Sahar Hoseini
                        String actualPin = null;

                        try {
                            actualPin = new String(SecurityComponent.rsaDecrypt(Hex.decode(chargeField.pin)));
                            Long.valueOf(actualPin);
                        } catch (Exception e) {
                            logger.error("invalid encrypted pin : " + chargeField.pin);
                            error.append(ERR_PIN).append(line).append("\r\n");
                            invalidRowNo++;
                            continue;
                        }
                        //year
                        if (s.length > i + 1) {
                            chargeField.createdDate = s[++i];
                            if (chargeField.createdDate == null || "".equals(chargeField.createdDate))
                                chargeField.createdDate = DateTime.now().toString();
                        } else
                            chargeField.createdDate = DateTime.now().toString();
                        chargeField.createdDate = chargeField.createdDate.replace("/", "").replace(":", "").substring(0, 4);
                        //expireDate
                        if (s.length > i + 1) {
                            String expDate = s[++i];
//							if(chargeField.expireDate == null || "".equals(chargeField.expireDate))
//							fields.expireDate = new DateTime()
                        } else {

                        }

                        //fileId
                        if (s.length > i + 1) {
                            chargeField.fileId = s[++i];
                            if (chargeField.fileId == null || "".equals(chargeField.fileId))
                                chargeField.fileId = chargeField.createdDate;
                        } else
                            chargeField.fileId = chargeField.createdDate;

                        //provider
                        if (s.length > i + 1) {
                            chargeField.provider = s[++i];
                        }/* else {
                            error.append(ERR_PROVIDER).append(line).append("\r\n");
                            invalidRowNo++;
                            continue;
                        }*/

                        charge.setEntity(Long.valueOf(chargeField.company));
                        charge.setCredit(chargeField.credit);
                        charge.setPinlen(chargeField.pinLen);
                        charge.setCardSerialNo(chargeField.serial);
                        charge.setCardPIN(chargeField.pin);
                        charge.setYear(Integer.valueOf(chargeField.createdDate));
                        charge.setFileId(new Long(Long.parseLong(chargeField.fileId) % 10000).intValue());
                        charge.setProvider(chargeField.provider != null ? chargeField.provider.trim() : null);
                        charge.setStateDate(DateTime.now());
                        charge.setState(IranCellMTNCharge.LOCKED_VALUE);
                        logger.info("add charge :" + charge.toString() + "to chargeList");
                        chargesList.add(charge);
                        serials.add(chargeField.serial);

                    } catch (Exception e) {
                        logger.error("An error occure in parsing line: " + line);
                        logger.error(e);
                        error.append(ERR_UNKNOWN).append(line).append("\r\n");
                        invalidRowNo++;
                        continue;
                    }
                    try {
                        if ((chargesList.size() % 1000) == 0) {
                            logger.info("Trying to insert 1000 in database [" + (++rowNoBatch) + "] ....");
                            logger.info("number of charge inserted : " + chargesList.size());
                            session = HibernateUtil.getCurrentSession();
                            session.beginTransaction();
                            Long l1 = System.currentTimeMillis();
                            Query query = session.createQuery("select ch.cardSerialNo from IranCellMTNCharge ch where ch.cardSerialNo in (:list)");
                            logger.debug("time of execute query is :" + String.valueOf(System.currentTimeMillis() - l1));
                            query.setParameterList("list", serials);
                            if (serials.size() > 0)
                                chargesInDB = (List<Long>) query.list();

                            if (chargesInDB.size() > 0) {
                                invalidRowNo += chargesInDB.size();
                                for (IranCellMTNCharge ch : chargesList) {
                                    if (chargesInDB.contains(ch.getCardSerialNo())) {
                                        error.append(ERR_SERIAL1).append(ch.getCardSerialNo()).append("\r\n");
                                        continue;
                                    } else {
                                        logger.info("save charge :" + ch.toString());
                                        session.save(ch);
                                    }
                                }
                            } else {
                                for (IranCellMTNCharge ch : chargesList) {
                                    logger.info("save charge :" + charge.toString());
                                    session.save(ch);
                                }
                            }
                            //session.commitTransaction();
                            HibernateUtil.endTransaction();
                            chargesList = new ArrayList<IranCellMTNCharge>();
                            serials = new ArrayList<Long>();
//							session = HibernateUtil.getCurrentSession();
//							session.beginTransaction();
                        }
                    } catch (Exception e) {
//						session = HibernateUtil.getCurrentSession();
//						session.beginTransaction();
                        logger.error(e);
                        //System.err.println(e.getMessage());
                        chargesList = new ArrayList<IranCellMTNCharge>();
                        serials = new ArrayList<Long>();
                    }
                }
            }
            //session.beginTransaction();
            try {
                //System.out.println("Trying to insert remaining charge ....");
                session = HibernateUtil.getCurrentSession();
                session.beginTransaction();
                Query query = session.createQuery("select ch.cardSerialNo from IranCellMTNCharge ch where ch.cardSerialNo in (:list)");
                query.setParameterList("list", serials);
                if (serials.size() > 0)
                    chargesInDB = (List<Long>) query.list();
                for (IranCellMTNCharge ch : chargesList) {
                    if (chargesInDB.contains(ch.getCardSerialNo())) {
                        error.append(ERR_SERIAL1).append(ch.getCardSerialNo()).append("\r\n");
                        continue;
                    } else
                        session.save(ch);
                }
                HibernateUtil.endTransaction();
            } catch (Exception e) {
                logger.error(e);
            }

        } catch (FileNotFoundException e) {
            logger.fatal(e);
        } catch (IOException e) {
            logger.fatal(e);
        }
        session = HibernateUtil.getCurrentSession();
        //session.commitTransaction();
        session.beginTransaction();
        chargeData.setAmount(chargeField != null ? chargeField.credit : null);
        chargeData.setCompanyCode(chargeField != null ? Integer.valueOf(chargeField.company) : null);
        chargeData.setInsertDate(Long.valueOf(MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate())));
        chargeData.setCreatedDateTime(DateTime.now());
        chargeData.setUserAcc(user);
        chargeData.setFileName(path.substring(path.lastIndexOf("\\") + 1));
        chargeData.setNumberOfCharges(rowNo - invalidRowNo - 1);
        chargeData.setErrorMsg(error.toString().length() > 154 ? (error.toString().substring(0, 154)) : error.toString());
        chargeData.setRepository(true);
        session.save(chargeData);
        //session.commitTransaction();
        HibernateUtil.endTransaction();

        //session.close();
        logger.info("--------------------------------------------------------------");
        logger.info("Time: " + (System.currentTimeMillis() - start));
        logger.info("Number of all rows in file: " + rowNo);
        logger.info("Number of invalid row: " + invalidRowNo);
        logger.info("NUmber of charges inserted in database successfully: " + (rowNo - invalidRowNo));

        return error.toString();
    }

}
