package vaulsys.clearing.parser;

import vaulsys.util.ConfigUtil;
import vaulsys.util.NotUsed;
import vaulsys.util.SwitchContext;
import vaulsys.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class Parse extends SwitchContext {

    Logger logger = Logger.getLogger(Parse.class);

    private final String delimit = "/";
    private final String root = ConfigUtil.getProperty(ConfigUtil.GLOBAL_PATH_FILE_REPORT);
    private final String clearingFile = "clearing";
//    private final String merchantStr = "Merchant";
//    private final String institutionStr = "Institution";
//    private final String Settlement = "Settlement";
//    private final String generalReport = "GeneralReport";
    private final String shetabXML = "shetabxml";
//    private final String shetabSchema = ConfigUtil.getProperty(ConfigUtil.GLOBALPATHS_SCHEMA_SHETAB);

    public Document parse(String data, File schema) {

        logger.debug("Reconcilement Message:" + data);

//        data = data.substring(15 + 6 + 1 + Integer.parseInt(data.substring(15+6, 15+6+1)));
        SAXReader reader = new SAXReader();
        Document documentSchema = null;
        try {
            documentSchema = reader.read(schema);
            Element rootSchema = documentSchema.getRootElement();

            Document documentTxt = DocumentHelper.createDocument();
            Element rootTxt = documentTxt.addElement("root");
            String line = "";
            StringTokenizer tokenizer;
            StringTokenizer tokenizerRule;

            StringTokenizer tokenizerLine = new StringTokenizer(data, rootSchema.attributeValue("delimiterLine"));
            while (tokenizerLine.hasMoreTokens()) {
                tokenizer = new StringTokenizer(tokenizerLine.nextToken(), rootSchema.attributeValue("delimiterElement"));

                String element = tokenizer.nextToken();
                Element row = rootTxt.addElement("Row").addAttribute("Number", String.valueOf(Integer.parseInt(element)));
                Iterator fieldItr = rootSchema.elementIterator("field");
                fieldItr.next();
                for (/*
						 * Iterator fieldItr = rootSchema.elementIterator("field")
						 */; fieldItr.hasNext();) {
                    element = tokenizer.nextToken();
                    Element field = (Element) fieldItr.next();
                    String name = field.attribute("name").getValue();
                    Element rule_elm = field.element("rule");
                    String rule_name = rule_elm.attribute("name").getValue();
                    if (rule_name.equals("padding")) {
                        String rule_type = rule_elm.attribute("type").getValue();
                        String rule_char = rule_elm.attribute("char").getValue();
                        String value = padding(element, rule_type, rule_char);
                        row.addElement(name).setText(value);
                    } else if (rule_name.equals("extract")) {
                        Element newElement = row.addElement(name);
                        tokenizerRule = new StringTokenizer(element, rule_elm.attributeValue("delimiter"));
                        for (Iterator fieldRuleItr = rule_elm.elementIterator("field"); fieldRuleItr.hasNext();) {
                            String elementInExtractRule = tokenizerRule.nextToken();

                            newElement.addAttribute(((Element) fieldRuleItr.next()).attribute("name").getValue(), elementInExtractRule);
                        }
                    }
                }
            }
            return documentTxt;
        } catch (DocumentException e) {
        	logger.error("Encounter with an exception ("+e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//            e.printStackTrace();
        }
        return null;
    }

    @NotUsed
    public void parse(File fileIn, File schemaIn/* String URL */) {
        String URL = fileIn.toURI().toString();
        String suffix = URL.substring(URL.indexOf(".") + 1);
        suffix = suffix.replace('.', '_');
        // File fileIn = new File(URL);
        // File schemaIn = new File(shetabSchema + suffix + ".xml");
        // File fileOut = new File("C:\\ClearingXML\\" + suffix + ".xml");

        SAXReader reader = new SAXReader();
        Document documentSchema = null;
        try {
            documentSchema = reader.read(schemaIn);
            Element rootSchema = documentSchema.getRootElement();

            BufferedReader br = new BufferedReader(new FileReader(fileIn));
            Document documentTxt = DocumentHelper.createDocument();
            Element rootTxt = documentTxt.addElement("root");
            String line = "";
            StringTokenizer tokenizer;
            StringTokenizer tokenizerRule;

            while (br.ready()) {
                line = br.readLine();
                tokenizer = new StringTokenizer(line, rootSchema.attributeValue("delimiterElement"));
//                MyDateFormat formatYYMMDD = new MyDateFormat("yyMMdd");
                String dt = "";

                String data = tokenizer.nextToken();
                Element row = rootTxt.addElement("Row").addAttribute("Number", String.valueOf(Integer.parseInt(data)));
                Iterator fieldItr = rootSchema.elementIterator("field");
                fieldItr.next();
                for (/*
						 * Iterator fieldItr = rootSchema.elementIterator("field")
						 */; fieldItr.hasNext();) {
                    data = tokenizer.nextToken();
                    Element field = (Element) fieldItr.next();
                    String name = field.attribute("name").getValue();
                    Element rule_elm = field.element("rule");
                    String rule_name = rule_elm.attribute("name").getValue();
                    if (rule_name.equals("padding")) {
                        String rule_type = rule_elm.attribute("type").getValue();
                        String rule_char = rule_elm.attribute("char").getValue();
                        String value = padding(data, rule_type, rule_char);
                        row.addElement(name).setText(value);
                    } else if (rule_name.equals("extract")) {
                        Element newElement = row.addElement(name);
                        tokenizerRule = new StringTokenizer(data, rule_elm.attributeValue("delimiter"));
                        for (Iterator fieldRuleItr = rule_elm.elementIterator("field"); fieldRuleItr.hasNext();) {
                            String dataInExtractRule = tokenizerRule.nextToken();

                            newElement.addAttribute(((Element) fieldRuleItr.next()).attribute("name").getValue(), dataInExtractRule);
                        }
                    }
                }
            }
            String folderStr = root + delimit + clearingFile + delimit + shetabXML;
            File file = new File(folderStr);
            file.mkdirs();
            FileWriter fileWriter = new FileWriter(folderStr + delimit + /* suffix + */"test.xml");

            XMLWriter writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
            writer.write(documentTxt);
            writer.close();
            fileWriter.close();

        } catch (FileNotFoundException e) {
        	logger.error("Encounter with an exception ("+e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//            e.printStackTrace();
        } catch (IOException e) {
        	logger.error("Encounter with an exception ("+e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//            e.printStackTrace();
        } catch (DocumentException e) {
        	logger.error("Encounter with an exception ("+e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//            e.printStackTrace();
        }

    }

    public String padding(String data, String paddingType, String paddingChar) {
        String returnData = "";
        if (paddingChar.equals("blank")) {
            String regex = "(\\s)";
            returnData = data.replaceAll(regex, "");
        } else if (paddingType.equals("left")) {
            returnData = String.valueOf(Util.longValueOf(data));
        } else if (paddingType.equals("fix")) {
            returnData = data; //String.valueOf(Util.longValueOf(data));
        }
        return returnData;
    }

//    public void generateMerchantFile(Merchant merchant, Ifx ifx) {
//
//        FileWriter fw;
//        try {
//
//            StringFormat format6 = new StringFormat(6, StringFormat.JUST_RIGHT);
//            StringFormat format8 = new StringFormat(8, StringFormat.JUST_RIGHT);
//            StringFormat format12 = new StringFormat(12, StringFormat.JUST_RIGHT);
//            StringFormat format19 = new StringFormat(19, StringFormat.JUST_LEFT);
//            StringFormat format9 = new StringFormat(9, StringFormat.JUST_LEFT);
//
//            MyDateFormat dateFormatYYMMDD = new MyDateFormat("yy/MM/dd");
//            MyDateFormat dateFormatYYMMDD2 = new MyDateFormat("MMdd");
//            MyDateFormat dateFormatHHMMDD = new MyDateFormat("HH:mm:ss");
//            MyDateFormat dateFormatyear = new MyDateFormat("yyyy");
//
//            List<DayDate> clearingRange = getFinancialEntityService().getClearingRange(merchant);
//            Date stlDt = clearingRange.get(clearingRange.size() - 1).toDate();
//            String stlDtStr = dateFormatYYMMDD2.format(stlDt);
//            String year = dateFormatyear.format(stlDt);
//
//            String folderStr = root + delimit + clearingFile + delimit + year + delimit + merchantStr + delimit + "merchant=" + merchant.getCode();
//
//            String fileStr = folderStr + delimit + stlDtStr;
//
//            File file = new File(folderStr);
//            file.mkdirs();
//
//            fw = new FileWriter(fileStr, true);
//
//            fw.write(format6.format(String.valueOf(Util.countLine(fileStr) + 1), '0') + "|");
//            fw.write(dateFormatYYMMDD.format(ifx.getOrigDt().toDate()) + "|");
//            fw.write(dateFormatHHMMDD.format(ifx.getOrigDt().toDate()) + "|");
//            fw.write(format6.format(ifx.getSrc_TrnSeqCntr(), '0') + "|");
//            fw.write(format19.format(ifx.getAppPAN()) + "|");
//            fw.write(format9.format(ifx.getBankId().toString()) + "|");
//            fw.write(format8.format(ifx.getTerminalId(), '0') + "|");
//            fw.write(format12.format(ifx.getAuth_Amt(), '0') + "|");
//            String type = "";
//            if (ShetabFinalMessageType.isPurchaseMessage(ifx.getIfxType())) {
//                type = "P";
//            } else if (ShetabFinalMessageType.isBalanceInqueryMessage(ifx.getIfxType())) {
//                type = "B";
//            } else if (ShetabFinalMessageType.isPurchaseReverseMessage(ifx.getIfxType())) {
//                type = "R";
//            }
//            fw.write(type + "\r\n");
//
//            fw.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static long calcTotalAmountWithoutFee(ReconcilementInfo item) {
//        long debitAmount = item == null ? 0 : item.getDebitAmount();
//        long creditAmount = item == null ? 0 : item.getCreditAmount();
//        long debitReversalAmount = item == null ? 0 : item.getDebitReversalAmount();
//        long creditReversalAmount = item == null ? 0 : item.getCreditReversalAmount();
////        long debitFee = item == null ? 0 : item.getDebitFee();
////        long creditFee = item == null ? 0 : item.getCreditFee();
//        return debitAmount - debitReversalAmount - creditAmount + creditReversalAmount;
//    }
//
//    public static long calcSettlementAmount(ReconcilementInfo item) {
//    	long debitAmount = item == null ? 0 : item.getDebitAmount();
//    	long creditAmount = item == null ? 0 : item.getCreditAmount();
//    	long debitReversalAmount = item == null ? 0 : item.getDebitReversalAmount();
//    	long creditReversalAmount = item == null ? 0 : item.getCreditReversalAmount();
//        long debitFee = item == null ? 0 : item.getDebitFee();
//        long creditFee = item == null ? 0 : item.getCreditFee();
//        return creditAmount - creditReversalAmount - creditFee - debitAmount + debitReversalAmount + debitFee;
//    }
//
//    public static long calcFeeAmount(ReconcilementInfo item) {
//        long debitFee = item == null ? 0 : item.getDebitFee();
//        long creditFee = item == null ? 0 : item.getCreditFee();
//        return creditFee - debitFee;
//    }

}