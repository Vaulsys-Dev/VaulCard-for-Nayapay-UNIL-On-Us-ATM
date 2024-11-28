package vaulsys.mtn;

import com.ibm.icu.util.StringTokenizer;
import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.calendar.DateTime;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.security.component.SecurityComponent;
import vaulsys.transaction.ClearingState;
import vaulsys.transaction.SettledState;
import vaulsys.transaction.Transaction;
import vaulsys.user.User;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.NotUsed;
import vaulsys.util.ConfigUtil;
import vaulsys.util.encoders.Hex;
import vaulsys.wfe.ProcessContext;
import vaulsys.mtn.util.rightel.RightelCharge;
import vaulsys.mtn.util.charge10000RL.MCI10000RCharge;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MTNChargeService {
    private static final Logger logger = Logger.getLogger(MTNChargeService.class);
    private static final Integer MAX_CACHE_SIZE = 50;
    private static Map<Long, Map<Long, Queue<Long>>> companyMap;
    private static Map<Long, Map<Long, DateTime>> nextTimeAllowedForDBCheck = new ConcurrentHashMap<Long, Map<Long, DateTime>>();

    @NotUsed
    public MTNCharge getChargeBySerial(Long serial) {
        String query = "from MTNCharge m where " +
                " m.cardSerialNo = :serial ";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("serial", serial);
        return (MTNCharge) GeneralDao.Instance.findObject(query, params);
    }

    private static final String[] headersNew =
            {"PackageId", "Quantity", "CardPrefix", "FaceValue", "StartDate", "StopDate",
                    "Currency", "ResID1", "Resleft1", "ResActiveDays1",
                    "ResID2", "Resleft2", "ResActiveDays2",
                    "ResID3", "Resleft3", "ResActiveDays3",
                    "ResID4", "Resleft4", "ResActiveDays4",
                    "ResID5", "Resleft5", "ResActiveDays5",
                    "ResID6", "Resleft6", "ResActiveDays6",
                    "ResID7", "Resleft7", "ResActiveDays7",
                    "ResID8", "Resleft8", "ResActiveDays8",
                    "ResID9", "Resleft9", "ResActiveDays9",
                    "ResID10", "Resleft10", "ResActiveDays10",
                    "Start_Sequence", "[BEGIN]"
            };
    private static final String[] headersOld =
            {"Batch", "Quantity", "Created Date", "Expired Date", "Face Value", "Validity",
                    "Service Provider", "Card Distributor", "Card Currency", "Start SN", ""};


    enum HeaderFormat {
        FormatOld,
        FormatNew
    }


    public static List<InsertedChargeReport> insertingCharge(String inputFile, Integer companyCode, Integer pinLen, Long chargeCredit, User user, boolean isFanapRepository) throws Exception {
//		HeaderFormat headerFormat = HeaderFormat.FormatNew;
        logger.debug("inputFile is : " + inputFile);
        InsertedChargeReport chargeData = new InsertedChargeReport();
        List<InsertedChargeReport> reports = new ArrayList<InsertedChargeReport>();
        DateTime date = DateTime.now();
        String[] headers = headersNew;
        boolean correctCreditl = true;
        String query = "select mtn from MTNCharge mtn where mtn.cardSerialNo = :serialNo and mtn.entity=:org";
        if (chargeCredit != null)
            chargeData.setAmount(chargeCredit);
        chargeData.setCompanyCode(companyCode);
        chargeData.setInsertDate(Long.valueOf(MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate())));
        chargeData.setCreatedDateTime(DateTime.now());
        chargeData.setUserAcc(user);
        String fileName = inputFile;
        for (int j = 0; j < inputFile.length(); j++) {
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.indexOf("/") + 1);
            } else
                break;
        }
        chargeData.setFileName(fileName);

        //*********************** MCI 10000 RLs
        if (companyCode.equals(9913) && isFanapRepository && chargeCredit.equals(10000L)) {
            chargeData.setRepository(true);
            logger.info("This is 10000L MCI's Charge...");
            chargeData.setErrorMsg(MCI10000RCharge.parseAndSaveCharges(new File(fileName)));
            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.save(chargeData);
            GeneralDao.Instance.endTransaction();
        }


        //****************** Rightel ****************
        else if (companyCode.equals(9920)) {
            chargeData.setRepository(true);
            logger.info("This is Rightel's charge ...");
            String msg = RightelCharge.parseAndSaveCharges(new File(fileName));
            chargeData.setErrorMsg(msg.length() > 255 ? msg.substring(0, 255) : msg);
            GeneralDao.Instance.beginTransaction();
            GeneralDao.Instance.save(chargeData);
            GeneralDao.Instance.endTransaction();
        }


        /******MCI********/
        else if (companyCode.equals(9912) && !isFanapRepository) {
            chargeData.setRepository(false);
            logger.info("This is MCI's Charge...");

            HeaderFormat headerformat = null;
            if (pinLen.equals(13))
                headerformat = HeaderFormat.FormatOld;
            else if (pinLen.equals(15))
                headerformat = HeaderFormat.FormatNew;
            else {
                logger.error("invalid pinlen for MCI Charge " + pinLen);
                chargeData.setErrorMsg("invalid pinlen for MCI Charge " + pinLen);
                throw new Exception("invalid pinlen for MCI Charge " + pinLen);
            }
            Long[] acceptableCredit = new Long[]{20000L, 50000L, 100000L, 200000L};
            for (int i = 0; i < acceptableCredit.length; i++) {
                if (acceptableCredit[i].equals(chargeCredit))
                    break;
                else if (i == acceptableCredit.length - 1) {
                    logger.error("invalid charge credit " + chargeCredit);
                    chargeData.setErrorMsg("invalid charge credit " + chargeCredit);
                    throw new Exception("invalid charge credit " + chargeCredit);
                }
            }

            if (!inputFile.contains(".encrypted")) {
                logger.error("The specified file is not encrypted");
                chargeData.setErrorMsg("The specified file is not encrypted");
                throw new Exception("The specified file is not encrypted");
            }
            BufferedReader in = new BufferedReader(new FileReader(inputFile));
            /*String res = countLinesOfFiles(inputFile);
            if (res != null) {
                chargeData.setErrorMsg(res);
                return Arrays.asList(chargeData);
                //throw new Exception("number of input file lines exceeded max! ");
            }*/
            GeneralDao.Instance.beginTransaction();  //1

            String batch = "";
            String createdDate = "";
            Long faceValue = 0L;

            String[] parts = null;


            for (int i = 0; i < headers.length; i++) {
                String strHeader = in.readLine();
                if (strHeader.startsWith("Batch:") || strHeader.startsWith("PackageId:")) {
                    parts = strHeader.split(":");
                    batch = parts[1].trim();
                } else if (strHeader.startsWith("Created Date:")) {
                    parts = strHeader.split(":");
                    createdDate = parts[1].trim().substring(4);
                } else if (strHeader.startsWith("StartDate:")) {
                    parts = strHeader.split(":");
                    createdDate = parts[1].trim().substring(0, 4);
                } else if (strHeader.startsWith("Face Value:") || strHeader.startsWith("FaceValue:")) {
                    parts = strHeader.split(":");
                    faceValue = Long.parseLong(parts[1].trim());
                }
            }

            if (headerformat.equals(HeaderFormat.FormatOld)) {
                switch (chargeCredit.intValue()) {
                    case 10000:
                        System.exit(0);
                    case 20000:
                        if (583 + faceValue != chargeCredit) {
                            logger.error("invalid face value: " + faceValue);
                            chargeData.setErrorMsg("invalid face value: " + faceValue);
                            throw new Exception();
                        }
                        break;
                    case 50000:
                        if (1456 + faceValue != chargeCredit) {
                            logger.error("invalid face value: " + faceValue);
                            chargeData.setErrorMsg("invalid face value: " + faceValue);
                            throw new Exception();
                        }
                        break;
                    case 100000:
                        if (2913 + faceValue != chargeCredit) {
                            logger.error("invalid face value: " + faceValue);
                            chargeData.setErrorMsg("invalid face value: " + faceValue);
                            throw new Exception();
                        }
                        break;
                    case 200000:
                        if (5825 + faceValue != chargeCredit) {
                            logger.error("invalid face value: " + faceValue);
                            chargeData.setErrorMsg("invalid face value: " + faceValue);
                            throw new Exception();
                        }
                        break;
                }
            } else if (headerformat.equals(HeaderFormat.FormatNew)) {
                if (!faceValue.equals(chargeCredit)) {
                    logger.error("invalid face value: " + faceValue);
                    chargeData.setErrorMsg("invalid face value: " + faceValue);
                    throw new Exception();
                }
            }


            Integer fileId = new Long(Long.parseLong(batch) % 10000).intValue();

            int rowNo = 0;

            String line;

            while ((line = in.readLine()) != null) {
                try {
                    rowNo++;

                    String[] serial_pin = line.split("[ ]");

                    if (serial_pin.length == 2) {
                        MTNCharge charge = new MTNCharge();
                        MTNCharge chargeInDB = new MTNCharge();
                        Map<String, Object> param = new HashMap<String, Object>();
                        try {
                            param.put("serialNo", Long.valueOf(serial_pin[0]));
                            Organization org = OrganizationService.findOrganizationByCode(Long.valueOf(companyCode), OrganizationType.MTNIRANCELL);
                            param.put("org", org);
                            chargeInDB = (MTNCharge) GeneralDao.Instance.findUnique(query, param);

                            if (chargeInDB != null) {
//                                logger.error("duplicate input file ...");
//                                throw new Exception("duplicate input file ...");
                                logger.error("duplicate record in file ...");
                                if (rowNo % 50 == 0)
                                    logger.info("Importing Record No: " + rowNo);
                                chargeData.setErrorMsg("duplicate record until line :" + rowNo);
                                continue;
                            }

                            if (serial_pin[0] != null && serial_pin[0].length() >= 12)
                                charge.setCardSerialNo(Long.parseLong(serial_pin[0]));
                            else {
                                logger.error("invalid card serial number");
                                throw new Exception("invalid card serial number");
                            }

                            charge.setCredit(chargeCredit);


                            byte[] actualPin = null;

                            try {
                                actualPin = SecurityComponent.rsaDecrypt(Hex.decode(serial_pin[1]));
                            } catch (Exception e) {
                                logger.error("invalid encrypted pin...");
                                throw new Exception("invalid encrypted pin...", e);
                            }
                            if (HeaderFormat.FormatNew.equals(headerformat) && actualPin.length == 15) {
                                charge.setCardPIN(serial_pin[1]);
                            } else if (HeaderFormat.FormatOld.equals(headerformat) && actualPin.length == 13) {
                                charge.setCardPIN(serial_pin[1]);
                            } else {
                                logger.error("invalid encrypted pin lengh for header format " + headerformat);
                                throw new Exception("invalid encrypted pin lengh for header format " + headerformat);
                            }
                            charge.setFileId(fileId);
                            if (createdDate != null) {
                                charge.setYear(Integer.parseInt(createdDate));
                            } else {
                                logger.error("Created date is null");
                                throw new Exception("Created date is null");
                            }

                            if (org != null)
                                charge.setEntity(org);
                            else {
                                logger.error("invalid companycode:" + companyCode);
                                throw new Exception("invalid companycode:" + companyCode);
                            }

                            charge.setStateDate(DateTime.now());
                            // charge.setState(MTNChargeState.NOT_ASSIGNED);
                            charge.setState(MTNChargeState.LOCKED);
                            if (pinLen != null && HeaderFormat.FormatNew.equals(headerformat) && pinLen.equals(15))
                                charge.setPinlen(pinLen);
                            else if (pinLen != null && HeaderFormat.FormatOld.equals(headerformat) && pinLen.equals(13))
                                charge.setPinlen(pinLen);
                            else {
                                logger.error("invalid pinLen");
                                throw new Exception("invalid pinLen");
                            }

                            GeneralDao.Instance.save(charge);
                        } catch (Exception e) {
                            logger.error(e);
                            chargeData.setErrorMsg(e.getMessage());
                            int c = rowNo % 10000;
                            chargeData.setNumberOfCharges(c - 1);
                            GeneralDao.Instance.save(chargeData);
//							GeneralDao.Instance.endTransaction();
                            throw e;
                        }
                    } else {
                        if (headerformat.equals(HeaderFormat.FormatNew) && serial_pin[0].equals("[END]")) {
                        } else {
                            logger.error("Invalid serial/pass: line no\n");
                            in.close();
                            throw new Exception("Invalid serial/pass: line no\n");
                        }
                    }

                    try {
                        if (rowNo % 50 == 0) {
                            System.out.println("Importing Record No: " + rowNo);
                            GeneralDao.Instance.flush();
                        }

                        if ((rowNo % 10000) == 0) {
//							int temp = rowNo/10000;
//							temp = rowNo/temp;
//							chargeData.setNumberOfCharges(temp);
//							GeneralDao.Instance.save(chargeData);
                            GeneralDao.Instance.endTransaction();
                            GeneralDao.Instance.beginTransaction();  //2
                        }
                    } catch (Exception e) {
                        chargeData.setErrorMsg(e.getMessage());
                        int c3 = rowNo % 10000;
                        chargeData.setNumberOfCharges(c3 - 1);
                        GeneralDao.Instance.save(chargeData);
                        logger.error(e.getStackTrace());
                        throw new Exception();
                    }
                } catch (Exception e) {
                    logger.error(e);
                    chargeData.setErrorMsg(e.getMessage());
                    break;

                }
            }
            in.close();
            if (chargeData.getErrorMsg() != null) {
                chargeData.setNumberOfCharges(rowNo - 1);
//					GeneralDao.Instance.save(chargeData);

            } else
                chargeData.setNumberOfCharges(rowNo);
            GeneralDao.Instance.save(chargeData);
            GeneralDao.Instance.endTransaction();

        }

        /********MTN********/
        else if (companyCode.equals(9935)) {
            boolean err[] = new boolean[5];
            int[] numberOfEachAmount = new int[5];
            logger.info("This is MTN's Charge...");
            Long[] acceptableCredit = new Long[]{10000L, 20000L, 50000L, 100000L, 200000L};
            if (!inputFile.contains(".encrypted")) {
                logger.error("The specified file is not encrypted");
                chargeData.setErrorMsg("The specified file is not encrypted");
                //throw new Exception("The specified file is not encrypted");
                return Arrays.asList(chargeData);
            }
            BufferedReader br = new BufferedReader(new FileReader(inputFile));
            /* String res = countLinesOfFiles(inputFile);
            if (res != null) {
                chargeData.setErrorMsg(res);
                //throw new Exception("number of input file lines exceeded max! ");
                return Arrays.asList(chargeData);
            }*/
            GeneralDao.Instance.beginTransaction();

            String line = "";

            int rowNo = 0;
            String seprator = "|";
            Long amount = 0L;
            while ((line = br.readLine()) != null) {
                try {
                    MTNCharge charge = new MTNCharge();
                    rowNo++;
                    StringTokenizer tokenizer = new StringTokenizer(line, seprator);
                    String entity = tokenizer.nextToken().trim();
                    Organization org = null;
                    if (("9" + entity).equals(companyCode.toString()))
                        org = OrganizationService.findOrganizationByCode(Long.valueOf(companyCode), OrganizationType.MTNIRANCELL);
                    else {
                        logger.error("invalid companycode :" + companyCode + "or invalid comapny in input file: " + entity);
                        //chargeData.setErrorMsg("invalid companycode :" + companyCode + "or invalid comapny in input file: " + entity);
                        throw new Exception("invalid companycode :" + companyCode + "or invalid comapny in input file: " + entity);
                    }
                    if (org != null)
                        charge.setEntity(org);
                    else {
                        logger.error("invalid comapnycode : " + companyCode);
                        //chargeData.setErrorMsg("invalid comapnycode : " + companyCode);
                        throw new Exception("invalid comapnycode : " + companyCode);
                    }

                    amount = Long.valueOf(ISOUtil.zeroUnPad(tokenizer.nextToken().trim()));

                    for (int i = 0; i < acceptableCredit.length; i++) {
                        if (acceptableCredit[i].equals(amount)) {
                            charge.setCredit(amount);
                            if (amount.equals(10000L))
                                numberOfEachAmount[0]++;
                            if (amount.equals(20000L))
                                numberOfEachAmount[1]++;
                            if (amount.equals(50000L))
                                numberOfEachAmount[2]++;
                            if (amount.equals(100000L))
                                numberOfEachAmount[3]++;
                            if (amount.equals(200000L))
                                numberOfEachAmount[4]++;
                            break;
                        } else if (i == acceptableCredit.length) {
                            logger.error("invalid charge credit " + amount);
                            chargeData.setErrorMsg("invalid charge credit " + amount);
                            throw new Exception("invalid charge credit " + amount);
                        }
                    }
                    fileName = tokenizer.nextToken();//secondamount
                    charge.setFileId(Integer.parseInt(tokenizer.nextToken().trim()));
                    charge.setYear(Integer.parseInt(tokenizer.nextToken().trim()));
                    fileName = tokenizer.nextToken();//bankBin
                    fileName = tokenizer.nextToken();//ZeroFix

                    String cardSerialno = tokenizer.nextToken().trim();
                    MTNCharge chargeInDB;
                    Map<String, Object> param = new HashMap<String, Object>();
                    param.put("serialNo", Long.valueOf(cardSerialno));
                    param.put("org", org);
                    chargeInDB = (MTNCharge) GeneralDao.Instance.findUnique(query, param);
                    if (chargeInDB != null) {
                        /*logger.error("duplicate inpute file");
                    throw new Exception("duplicate inpute file");*/
                        logger.error("duplicate record in file ...");
                        chargeData.setErrorMsg("duplicate record until line :" + rowNo);
                        continue;
                    } else if (cardSerialno.length() >= 12)
                        charge.setCardSerialNo(Long.valueOf(cardSerialno));
                    else {
                        logger.error("invalid lengh for serial number...");
                        throw new Exception("invalid lengh for serial number...");
                    }

                    byte[] actualPin = null;
                    String pin = tokenizer.nextToken().trim();

                    try {
                        actualPin = SecurityComponent.rsaDecrypt(Hex.decode(pin));
                    } catch (Exception e) {
                        logger.error("invalid encrypted pin...");
                        throw new Exception("invalid encrypted pin...");
                    }
                    charge.setCardPIN(pin);
                    fileName = tokenizer.nextToken();//CFix
                    charge.setHelpDesk(tokenizer.nextToken().trim());
                    charge.setIr(Integer.parseInt(tokenizer.nextToken().trim()));

                    charge.setStateDate(DateTime.now());
                    //charge.setState(MTNChargeState.NOT_ASSIGNED);
                    charge.setState(MTNChargeState.LOCKED);
                    if (pinLen != null && pinLen.equals(16))
                        charge.setPinlen(pinLen);
                    else {
                        logger.error("invalid pinLen");
                        throw new Exception("invalid pinLen");
                    }

                    GeneralDao.Instance.save(charge);


                    try {
                        if (rowNo % 50 == 0) {
                            //System.out.println("Importing Record No: " + rowNo);
                            logger.info("Importing Record No: " + rowNo);
                            GeneralDao.Instance.flush();
                        }

                        if ((rowNo % 10000) == 0) {
                            logger.info("Importing Record No: " + rowNo);
                            GeneralDao.Instance.endTransaction();
                            GeneralDao.Instance.beginTransaction();
                        }
                    } catch (Exception e) {
                        logger.error("Charge has not been added to DB: Line[" + rowNo + "] SerialNo:" + charge.getCardSerialNo());
                        logger.error(e.getStackTrace());
                        throw new Exception("Charge has not been added to DB: Line[" + rowNo + "] SerialNo:" + charge.getCardSerialNo(), e);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    chargeData.setErrorMsg(e.getMessage());
                    if (amount.equals(10000L))
                        err[0] = true;
                    if (amount.equals(20000L))
                        err[1] = true;
                    if (amount.equals(50000L))
                        err[2] = true;
                    if (amount.equals(100000L))
                        err[3] = true;
                    if (amount.equals(200000L))
                        err[4] = true;
                    break;
                }
            }
            br.close();
            if (numberOfEachAmount[0] > 0) {
                chargeData.setAmount(10000L);
                chargeData.setNumberOfCharges(numberOfEachAmount[0]);
                if (err[0])
                    chargeData.setNumberOfCharges(numberOfEachAmount[0] - 1);
                GeneralDao.Instance.save(chargeData);
                //GeneralDao.Instance.endTransaction();
            }
            if (numberOfEachAmount[1] > 0) {

                InsertedChargeReport chargeData2 = new InsertedChargeReport();
                chargeData2.setCompanyCode(chargeData.getCompanyCode());
                chargeData2.setInsertDate(Long.valueOf(MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate())));
                chargeData2.setCreatedDateTime(DateTime.now());
                chargeData2.setUserAcc(chargeData.getUserAcc());
                chargeData2.setFileName(chargeData.getFileName());
                chargeData2.setAmount(20000L);
                chargeData2.setNumberOfCharges(numberOfEachAmount[1]);
                if (err[1]) {
                    chargeData2.setErrorMsg(chargeData.getErrorMsg());
                    //chargeData.setErrorMsg(null);
                    chargeData2.setNumberOfCharges(numberOfEachAmount[1] - 1);
                }
                GeneralDao.Instance.save(chargeData2);
                reports.add(chargeData2);
            }
            if (numberOfEachAmount[2] > 0) {

                InsertedChargeReport chargeData2 = new InsertedChargeReport();
                chargeData2.setCompanyCode(chargeData.getCompanyCode());
                chargeData2.setInsertDate(Long.valueOf(MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate())));
                chargeData2.setCreatedDateTime(DateTime.now());
                chargeData2.setUserAcc(chargeData.getUserAcc());
                chargeData2.setFileName(chargeData.getFileName());
                chargeData2.setAmount(50000L);
                chargeData2.setNumberOfCharges(numberOfEachAmount[2]);
                if (err[2]) {
                    chargeData2.setErrorMsg(chargeData.getErrorMsg());
                    //chargeData.setErrorMsg(null);
                    chargeData2.setNumberOfCharges(numberOfEachAmount[2] - 1);
                }
                GeneralDao.Instance.save(chargeData2);
                reports.add(chargeData2);
            }
            if (numberOfEachAmount[3] > 0) {
                InsertedChargeReport chargeData2 = new InsertedChargeReport();
                chargeData2.setCompanyCode(chargeData.getCompanyCode());
                chargeData2.setInsertDate(Long.valueOf(MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate())));
                chargeData2.setCreatedDateTime(DateTime.now());
                chargeData2.setUserAcc(chargeData.getUserAcc());
                chargeData2.setFileName(chargeData.getFileName());
                chargeData2.setAmount(100000L);
                chargeData2.setNumberOfCharges(numberOfEachAmount[3]);
                if (err[3]) {
                    chargeData2.setErrorMsg(chargeData.getErrorMsg());
                    //chargeData.setErrorMsg(null);
                    chargeData2.setNumberOfCharges(numberOfEachAmount[3] - 1);
                }
                GeneralDao.Instance.save(chargeData2);
                reports.add(chargeData2);
            }
            if (numberOfEachAmount[4] > 0) {
                InsertedChargeReport chargeData2 = new InsertedChargeReport();
                chargeData2.setCompanyCode(chargeData.getCompanyCode());
                chargeData2.setInsertDate(Long.valueOf(MyDateFormatNew.format("yyyyMMddHHmmss", DateTime.now().toDate())));
                chargeData2.setCreatedDateTime(DateTime.now());
                chargeData2.setUserAcc(chargeData.getUserAcc());
                chargeData2.setFileName(chargeData.getFileName());
                chargeData2.setAmount(200000L);
                chargeData2.setNumberOfCharges(numberOfEachAmount[4]);
                if (err[4]) {
                    chargeData2.setErrorMsg(chargeData.getErrorMsg());
                    //chargeData.setErrorMsg(null);
                    chargeData2.setNumberOfCharges(numberOfEachAmount[4] - 1);
                }
                GeneralDao.Instance.save(chargeData2);
                reports.add(chargeData2);
            }
            /* GeneralDao.Instance.save(chargeData);*/
            GeneralDao.Instance.endTransaction();


        } else if ((companyCode.equals(9936) || companyCode.equals(9912)) && isFanapRepository) {
            logger.info("This is Fanap's Charge...");

            chargeData.setRepository(true);
            BufferedReader in = new BufferedReader(new FileReader(inputFile));

            /*String res = countLinesOfFiles(inputFile);
            if (res != null) {
                chargeData.setErrorMsg(res);
                return Arrays.asList(chargeData);
                //throw new Exception("number of input file lines exceeded max! ");

            }*/
            GeneralDao.Instance.beginTransaction();
            int rowNo = 0;

            in.readLine();
            String line;
            while ((line = in.readLine()) != null && !line.equals(",,,,,,")) {
                try {
                    rowNo++;
//					chargeData.setFileName(inputFile);
                    chargeData.setUserAcc(user);
                    String[] serial_pin = line.split(",");
                    if (companyCode.equals(9912) && serial_pin[0].equals("MTN"))
                        throw new Exception("invalid file type for MCI charge ");
                    else if (companyCode.equals(9936) && serial_pin[0].equals("MCI"))
                        throw new Exception("invalid file type for MTN charge ");

                    if (serial_pin.length == 7) {
                        //logger.info("file elem length is 7");
                        MTNCharge charge = new MTNCharge();
                        Organization org = OrganizationService.findOrganizationByCode(Long.valueOf(companyCode), OrganizationType.MTNIRANCELL);
                        //logger.info("org name is : " + org.getName());
                        if (org != null) {
                            if (serial_pin[0].equals("MCI")) {
                                charge.setEntity(org);
                            } else if (serial_pin[0].equals("MTN")) {
                                charge.setEntity(org);
                            } else {
                                logger.error("Unknown Entity");
                                throw new Exception("Unknown Entity");
                            }
                        } else {
                            logger.error("invalid compay code " + companyCode);
                            throw new Exception("invalid compay code " + companyCode);
                        }
                        chargeData.setCompanyCode(companyCode);
                        Long[] acceptableCredit = new Long[]{20000L, 50000L, 100000L, 200000L};
                        for (int i = 0; i < acceptableCredit.length; i++) {
                            if (acceptableCredit[i].equals(Long.parseLong(serial_pin[1]))) {
                                charge.setCredit(Long.parseLong(serial_pin[1]));
                                chargeData.setAmount(Long.parseLong(serial_pin[1]));
                                break;
                            } else if (i == acceptableCredit.length) {
                                logger.error("invalid charge credit " + serial_pin[1]);
                                throw new Exception("invalid charge credit " + serial_pin[1]);
                            }
                        }
                        //logger.info("setting charge pinlen ...");
                        if (serial_pin[0].equals("MCI") && Integer.parseInt(serial_pin[2]) == 15) {
                            charge.setPinlen(Integer.parseInt(serial_pin[2]));
                        } else if (serial_pin[0].equals("MTN") && Integer.parseInt(serial_pin[2]) == 16) {
                            charge.setPinlen(Integer.parseInt(serial_pin[2]));
                        } else {
                            logger.error("Invalid pin len");
                            throw new Exception("Invalid pin len");
                        }
                        logger.info("setting charge file Id ...");
                        charge.setFileId((int) (Long.parseLong(serial_pin[3]) % 10000));
                        /*Check is Duplicate Or not*/
                        MTNCharge chargeInDB;
                        Map<String, Object> param = new HashMap<String, Object>();
                        param.put("serialNo", Long.valueOf(serial_pin[4]));
                        param.put("org", org);
                        chargeInDB = (MTNCharge) GeneralDao.Instance.findUnique(query, param);

                        if (chargeInDB != null) {
                            logger.error("duplicate record in file ...");
                            chargeData.setErrorMsg("duplicate record until line :" + rowNo);
                            continue;
                            //throw new Exception("duplicate input file ...");
                        }

                        if (serial_pin[4].length() >= 12)
                            charge.setCardSerialNo(Long.parseLong(serial_pin[4]));
                        else {
                            logger.error("invalid pin");
                            throw new Exception("invalid pin");
                        }

                        byte[] actualPin = null;

                        try {
                            actualPin = SecurityComponent.rsaDecrypt(Hex.decode(serial_pin[6]));
                            String pinStr = new String(actualPin);
                            Long.valueOf(pinStr);
                        } catch (Exception e) {
                            logger.error("invalid encrypted pin...");
                            throw new Exception("invalid encrypted pin...");
                        }
//						if(actualPin.length == 15){//chechk beshe ke cheghadr bayad bashe
                        charge.setCardPIN(serial_pin[6]);
//						}
//						else{
//							logger.error("invalid encrypted pin lengh");
//							throw new Exception("invalid encrypted pin lengh");
//						}

                        charge.setYear(DateTime.now().getDayDate().getYear());

                        charge.setStateDate(DateTime.now());
                        //charge.setState(MTNChargeState.NOT_ASSIGNED);
                        //logger.info("setting charge state ...");
                        charge.setState(MTNChargeState.LOCKED);

                        GeneralDao.Instance.save(charge);
                    } else {
                        chargeData.setErrorMsg("file elem length is not 7");
                        break;
                    }
                    try {
                        if (rowNo % 50 == 0) {
                            logger.info("Importing Record No: " + rowNo);
                            GeneralDao.Instance.flush();
                        }

                        if ((rowNo % 10000) == 0) {
                            GeneralDao.Instance.endTransaction();
                            GeneralDao.Instance.beginTransaction();
                        }
                    } catch (Exception e) {
                        logger.error(e.getStackTrace());
                        GeneralDao.Instance.rollback();
                        throw new Exception(e);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    chargeData.setErrorMsg(e.getMessage() + "at line: " + rowNo);
                    break;
                }
            }
            in.close();
            if (chargeData.getErrorMsg() != null)
                chargeData.setNumberOfCharges(rowNo - 1);
            else
                chargeData.setNumberOfCharges(rowNo);
            GeneralDao.Instance.save(chargeData);
            GeneralDao.Instance.endTransaction();

        }
        reports.add(chargeData);
        return reports;
//		}catch(Exception e){
//			logger.error(e);
//			chargeData.setError(e.getMessage());
//			GeneralDao.Instance.save(chargeData);
//
//		}
//
    }

    private static String countLinesOfFiles(String inputFile) {
        String msg = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            File temp = new File(inputFile + "1");
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            String str = "";
            int count = 0;
            while ((str = reader.readLine()) != null) {
                if (!str.equals(",,,,,,")) {
                    count++;
                    writer.write(str);
                }
            }
            System.out.println(count);
            /*LineNumberReader lnr = new LineNumberReader(new FileReader(inputFile));
            String str2 = "";
            while ((str2 = lnr.readLine()) != null) {

            }
            int cnt = lnr.getLineNumber();*/
            //lnr.skip(Long.MAX_VALUE);
            logger.info("input file line numbers : " + count);
            //lnr.close();
            reader.close();
            if (count > 50000)
                msg = "تعداد خطوط فایل ورودی از50000 نمیتواند بزرگتر باشد";
            //System.out.println(lnr.getLineNumber());
        } catch (Exception exp) {
            throw new RuntimeException();
        }
        return msg;
    }

    public static MTNCharge getCharge(Long credit, Organization companyCode) {
        MTNCharge charge = null;
        while(charge == null) {
            Long cardSerialNo = getFirstChargeFromQueue(companyCode, credit);

            if (cardSerialNo == null) {
                logger.warn("No Charge Available with Credit: " + credit);
                return null;
            } else {
                logger.debug("Charge with srialNo assigned:" + cardSerialNo);
            }

            charge = GeneralDao.Instance.load(MTNCharge.class, cardSerialNo, LockMode.UPGRADE);

//			if(isSoldBefore(charge)){
//				logger.warn("Charge is assigned to another trx, assigning another one: " + charge.getCardSerialNo());
//				charge.setState(MTNChargeState.SOLD_BEFORE);
//				GeneralDao.Instance.saveOrUpdate(charge);
////				GeneralDao.Instance.releaseLock(charge);
//				charge = null;
//			}
        }
        return charge;
    }

    public static boolean isSoldBefore(MTNCharge charge) {
        boolean result = false;
        Map< String, Object> params = new HashMap<String, Object>();
        params.put("chargeSerial", charge.getCardSerialNo());
        String queryString = "select count(*) " +
                " from ifx i " +
                " inner join IFX_EMV_RS_DATA rs on rs.id = i.EMVRSDATA " +
                " inner join ifx_cell_charge_data c on c.id = i.charge " +
                " where " +
                " i.direction = 1 and " +
                " rs.rscode = 00 and " +
                " c.charge = :chargeSerial";

        List<BigDecimal> count = GeneralDao.Instance.executeSqlQuery(queryString,params);
        result = !count.get(0).equals(new BigDecimal(0));

        if(result == true){
            logger.warn("First query says that charge is assigned to another trx: " + charge.getCardSerialNo());
        }else{
            return result;
        }
        List<Integer> clrState = new ArrayList<Integer>();
        /*clrState.add(ClearingState.NOT_CLEARED.getState());
        clrState.add(ClearingState.CLEARED.getState());
        clrState.add(ClearingState.SUSPECTED_DISPUTE.getState());
        params.put("clrState", clrState);

        queryString = "select count(*) " +
                " from ifx i " +
                " inner join IFX_EMV_RS_DATA rs on rs.id = i.EMVRSDATA " +
                " inner join ifx_cell_charge_data c on c.id = i.charge " +
                " inner join trx_transaxion t on t.id = i.trx " +
                " inner join trx_flg_clearing tc on tc.id = t.src_clr_flg " +
                " where " +
                " i.direction = 1 and " +
                " rs.rscode = 00 and " +
                " tc.clr_state in (:clrState) " +
                " and " +
                " c.charge = :chargeSerial";

        count = GeneralDao.Instance.executeSqlQuery(queryString,params);
        logger.warn("Second query count(*): " + count);

        result = !count.get(0).equals(new BigDecimal(0));

//		return result;
        if(result == true){
            logger.warn("Second query says that charge has ok trx: " + charge.getCardSerialNo());
        }else{
            return result;
        }*/

        params = new HashMap<String, Object>();
        params.put("chargeSerial", charge.getCardSerialNo());
        clrState = new ArrayList<Integer>();
        clrState.add(ClearingState.DISAGREEMENT.getState());
//        clrState.add(ClearingState.DISPUTE.getState());
        params.put("clrState", clrState);

        queryString = "select count(*) " +
                " from ifx i " +
                " inner join IFX_EMV_RS_DATA rs on rs.id = i.EMVRSDATA " +
                " inner join ifx_cell_charge_data c on c.id = i.charge " +
                " inner join trx_transaxion t on t.id = i.trx " +
                " inner join trx_flg_clearing tc on tc.id = t.src_clr_flg " +
                " where " +
                " i.direction = 1 and " +
                " rs.rscode = 00 and " +
                " tc.clr_state in (:clrState) " +
                " and " +
                " c.charge = :chargeSerial" +
                " and " +
                " t.begin_date=tc.clr_date";

        count = GeneralDao.Instance.executeSqlQuery(queryString,params);
        logger.warn("Third query count(*): " + count);

        //result = !count.get(0).equals(new BigDecimal(0));
        return count.get(0).intValue() == 0;
//        return !result;
    }

    public static void unlockCharge(Ifx ifx, Transaction transaction) {
        unlockCharge(ifx, transaction, false);
    }

    public static void unlockCharge(Ifx ifx, Transaction transaction, boolean isSuccessfull) {
        unlockCharge(ifx, null, transaction, isSuccessfull);
    }

    public static void unlockCharge(Ifx ifx, Ifx refIfx, Transaction transaction) {
        unlockCharge(ifx, refIfx, transaction, false);
    }

    public static void unlockCharge(Ifx ifx, Ifx refIfx, Transaction transaction, boolean isSuccessfull) {
        boolean isReverse = (ISOFinalMessageType.isPurchaseChargeAndReversalMessage(ifx.getIfxType()) ||
                (ifx.getTransaction() != null &&
                        ifx.getTransaction().getReferenceTransaction() != null &&
                        ifx.getTransaction().getReferenceTransaction().getIncomingIfx() != null &&
                        ISOFinalMessageType.isPurchaseChargeAndReversalMessage(ifx.getTransaction().getReferenceTransaction().getIncomingIfx().getIfxType())))
                && ISOFinalMessageType.isReversalMessage(ifx.getIfxType());
        boolean refIfxType = refIfx != null ? ISOFinalMessageType.isPurchaseChargeMessage(refIfx.getIfxType()) : false;
        if(!refIfxType){
            if(ifx.getTransaction() != null && ifx.getTransaction().getReferenceTransaction() != null
                    && ifx.getTransaction().getReferenceTransaction().getIncomingIfx() != null
                    && ISOFinalMessageType.isPurchaseChargeMessage(ifx.getTransaction().getReferenceTransaction().getIncomingIfx().getIfxType()))
                refIfxType = true;
        }
        if (ifx != null
                && (ISOFinalMessageType.isPurchaseChargeAndReversalMessage(ifx.getIfxType()) || refIfxType)
                && ifx.getCharge() != null
                && transaction.getLifeCycle() != null
                && ifx.getCharge().getLifeCycle() != null
                && transaction.getLifeCycle().equals(ifx.getCharge().getLifeCycle())
                ) {

            MTNCharge charge = ifx.getCharge();
            GeneralDao.Instance.lockReadAndWrite(charge);
            if(isReverse && MTNChargeState.IN_ASSIGNED.equals(charge.getState())){
                ifx.setChargeStatePrv(charge.getState());
                charge.setState(MTNChargeState.NOT_ASSIGNED);
                ifx.setChargeStateNxt(charge.getState());
            } else if(ISOFinalMessageType.isResponseMessage(ifx.getIfxType()) && !isSuccessfull) {
                if(MTNChargeState.IN_ASSIGNED.equals(charge.getState())){
                    ifx.setChargeStatePrv(charge.getState());
                    charge.setState(MTNChargeState.NOT_ASSIGNED);
                    ifx.setChargeStateNxt(charge.getState());
                }
            } else {
                if(!ISOFinalMessageType.isResponseMessage(ifx.getIfxType())){
                    if(!MTNChargeState.NOT_ASSIGNED.equals(charge.getState())){
                        ifx.setChargeStatePrv(charge.getState());
                        if(ifx.getTransaction().getReferenceTransaction() != null && ifx.getTransaction().getReferenceTransaction().getThirdPartySettleInfo() != null && SettledState.SETTLED.equals(ifx.getTransaction().getReferenceTransaction().getThirdPartySettleInfo().getSettledState()) )
                            charge.setState(MTNChargeState.SETTLED_SUSPICIOUS);
                        else
                            charge.setState(MTNChargeState.UN_SETTLED_SUSPICIOUS);
                        ifx.setChargeStateNxt(charge.getState());
                    }
                } else if(isSuccessfull && MTNChargeState.UN_SETTLED_SUSPICIOUS.equals(charge.getState())){
                    ifx.setChargeStatePrv(charge.getState());
                    charge.setState(MTNChargeState.NOT_ASSIGNED);
                    ifx.setChargeStateNxt(charge.getState());
                }
            }

            logger.debug("Charge with srialNo: " + charge.getCardSerialNo() + "changed to: " + charge.getState());
            GeneralDao.Instance.saveOrUpdate(charge);

            //TODO: this code is replaced just for the case that we have only MTN
            // The problem is actually came from our way of getting charge in which
            // Organization is null!!! Therefore, in unlocking charge we should consider null organization
//			addChargeToQueue(charge.getEntity(), charge.getCredit(), charge.getCardSerialNo());
//			addChargeToQueue(null, charge.getCredit(), charge.getCardSerialNo());
        }
    }

    public static Long getRealChargeCredit(Long credit, Long companyCode) {
        Long chargeTax = null;

        try {
            chargeTax = ProcessContext.get().getMTNChargeTax(credit, companyCode);
        } catch (Exception e) {
        }

        if (chargeTax == null)
            chargeTax = 0L;
        return credit - chargeTax;
    }

    synchronized private static Long getFirstChargeFromQueue(Organization company, Long amount){
        Queue<Long> chargeQueue = getChargeQueue(company, amount, true);
        Long cardSerialNo = null;
        if (chargeQueue != null && !chargeQueue.isEmpty())
            cardSerialNo = chargeQueue.remove();
        if(cardSerialNo != null)
            logger.debug("getFirstChargeFromQueue():" + cardSerialNo);

        return cardSerialNo;
    }

    synchronized private static Queue<Long> getChargeQueue(Organization company, Long amount, boolean fillIfEmpty){
        if (companyMap == null){
            companyMap = new ConcurrentHashMap<Long, Map<Long, Queue<Long>>>();
        }

        Long companyCode = null;
        if(company != null){
            companyCode = company.getCode();
            //TODO check it later!? Noroozi
//			companyCode = new Long(0L);
            logger.debug("companyCode is: " + companyCode);
        } else {
//			companyCode = 0L;
            logger.error("OHOH charge company is null....");
            return null;
        }

        Map<Long, Queue<Long>> chargeMap = null;
        chargeMap = companyMap.get(companyCode);

        if (chargeMap == null){
            chargeMap = new ConcurrentHashMap<Long, Queue<Long>>();
            companyMap.put(companyCode, chargeMap);
        }

        boolean isFirstTime = false;

        Queue<Long> chargeQueue = chargeMap.get(amount);
        if (chargeQueue == null){
            chargeQueue = new ConcurrentLinkedQueue<Long>();
            isFirstTime = true;
            chargeMap.put(amount, chargeQueue);
        }

        logger.debug("charge cache size company: "+companyCode+"("+amount+"): "+chargeQueue.size());

        if(fillIfEmpty && chargeQueue.isEmpty()){

            List<BigDecimal> charges = queryForCharges(amount, companyCode, isFirstTime);

            if(charges == null || charges.size() == 0){
                if(isFirstTime){
                    //If it is first time call, query db again without firsttime to get result
                    charges = queryForCharges(amount, companyCode, false);
                }
            }

            if(charges == null || charges.size() == 0){
                return chargeQueue;
            }


            String strCharges = "";
            List<Long> lCharges = new ArrayList<Long>();
            for(int i=0; i<charges.size()-1; i++){
                lCharges.add(charges.get(i).longValue());
                strCharges += charges.get(i) + ", ";
            }

            lCharges.add(charges.get(charges.size()-1).longValue());
            strCharges += charges.get(charges.size()-1);

            chargeQueue.addAll(lCharges);

            DateTime now= DateTime.now();
            String query = "update mtn_charge m set m.charge_state = " + MTNChargeState.CACHED.getType() +
                    ", state_date= "+now.getDayDate().getDate() +
                    ", state_time= "+now.getDayTime().getDayTime() +
                    " where m.credit = " + amount +
                    " and m.cardserialno in( "+ strCharges +" )";

            int executeSqlUpdate = GeneralDao.Instance.executeSqlUpdate(query);
            logger.debug("Num affected rows: " +executeSqlUpdate);
        }

        return chargeQueue;
    }


    private static List<BigDecimal> queryForCharges(Long amount, Long companyCode, boolean isFirstTime) {
        if (nextTimeAllowedForDBCheck.containsKey(companyCode)) {
            Map<Long, DateTime> amountsMap = nextTimeAllowedForDBCheck.get(companyCode);
            if (amountsMap.containsKey(amount)) {
                if (DateTime.now().compareTo(amountsMap.get(amount)) < 0)
                    return new ArrayList<BigDecimal>();
            }
        }

        StringBuilder query = new StringBuilder("select m.cardserialno from mtn_charge m where m.credit = ").append(amount);
//		String query = "select m.cardserialno from mtn_charge m " + "where m.credit = " + amount;

        //TODO Check it later!? Noroozi
        query.append(" and m.company = ").append(companyCode).append(" and ");
//		query += " and m.company = " + companyCode + " and ";

        if(isFirstTime) {
            //if it is first time of creating cache, we get charges with cached state
            //to avoid having unusable charges due to bad killing of switch in it's
            //previous run
//				query += "(m.charge_state = " + MTNChargeState.NOT_ASSIGNED.getType() +
//				" or m.charge_state = " + MTNChargeState.CACHED.getType() +
//				" ) ";
            query.append("m.charge_state = ").append(MTNChargeState.CACHED.getType()).append(" for update nowait");
//			query += "m.charge_state = " + MTNChargeState.CACHED.getType() +" ";
//			query += " for update";				
        }else{
            query.append("m.charge_state = ").append(MTNChargeState.NOT_ASSIGNED.getType()).append("and rownum <= ").append(MAX_CACHE_SIZE).append(" for update nowait");
//			query += "m.charge_state = " + MTNChargeState.NOT_ASSIGNED.getType() +" ";
////				query += " and rownum <= "+ MAX_CACHE_SIZE + " for update";				
//			query += " and rownum <= "+ MAX_CACHE_SIZE + " for update";				
        }



        logger.debug("before filling charge cache...("+amount+")");
        List<BigDecimal> charges = GeneralDao.Instance.executeSqlQuery(query.toString());
        logger.debug("after filling charge cache("+amount+"): num added charges: "+charges.size());
        if (!isFirstTime && charges.isEmpty()) {
            DateTime now = DateTime.now();
            now.increase(ConfigUtil.getInteger(ConfigUtil.CHARGE_EMPTY_DB_CHECK_PERIOD));

            if (!nextTimeAllowedForDBCheck.containsKey(companyCode)) {
                nextTimeAllowedForDBCheck.put(companyCode,  new ConcurrentHashMap<Long, DateTime>());
            }

            nextTimeAllowedForDBCheck.get(companyCode).put(amount, now);
        }
        return charges;
    }

/*
	private static List<BigDecimal> queryForCharges(Long amount, Long companyCode, boolean isFirstTime) {
		String query = "select m.cardserialno from mtn_charge m " +
				"where m.credit = " + amount;
				
		//TODO Check it later!? Noroozi
		query += " and m.company = " + companyCode + " and ";
		
		if(isFirstTime) {
			//if it is first time of creating cache, we get charges with cached state
			//to avoid having unusable charges due to bad killing of switch in it's
			//previous run
//				query += "(m.charge_state = " + MTNChargeState.NOT_ASSIGNED.getType() +
//				" or m.charge_state = " + MTNChargeState.CACHED.getType() +
//				" ) ";
			query += "m.charge_state = " + MTNChargeState.CACHED.getType() +" ";
			query += " for update";				
		}else{
			query += "m.charge_state = " + MTNChargeState.NOT_ASSIGNED.getType() +" ";
//				query += " and rownum <= "+ MAX_CACHE_SIZE + " for update";				
			query += " and rownum <= "+ MAX_CACHE_SIZE + " for update";				
		}


		
		logger.debug("before filling charge cache...("+amount+")");
		List<BigDecimal> charges = GeneralDao.Instance.executeSqlQuery(query);
		logger.debug("after filling charge cache("+amount+"): num added charges: "+charges.size());
		return charges;
	}	
*/
}
