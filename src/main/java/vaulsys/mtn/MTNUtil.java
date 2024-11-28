package vaulsys.mtn;

import vaulsys.billpayment.consts.OrganizationType;
import vaulsys.entity.OrganizationService;
import vaulsys.entity.impl.Organization;
import vaulsys.mtn.consts.MTNChargeState;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.MyInteger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class MTNUtil {

	Logger logger = Logger.getLogger(this.getClass());
	public final String SEPERATOR = "|";

	public List<MTNCharge> parse(File fileIn, File schema, OrganizationType companyType) {
		List<MTNCharge> result = new ArrayList<MTNCharge>();

		SAXReader reader = new SAXReader();
		Document documentSchema = null;
		try {
			documentSchema = reader.read(schema);
			Element rootSchema = documentSchema.getRootElement();

			BufferedReader br = new BufferedReader(new FileReader(fileIn));
			String line = "";

			while (br.ready()) {
				MTNCharge charge = new MTNCharge();
				MyInteger index = new MyInteger(0);
				String data = "";
				line = br.readLine();

				Iterator fieldItr = rootSchema.elementIterator("field");
				for (; fieldItr.hasNext();) {

					try {
						Element field = (Element) fieldItr.next();
						String name = field.attribute("name").getValue();
						String lengthStr = field.attribute("length").getValue();
						String type = field.attribute("type").getValue();
						Integer length = 0;

						if (lengthStr.trim().isEmpty()) {
							data = readUntilSeperator(line, index);
						} else {
							length = Integer.parseInt(lengthStr);
							data = line.substring(index.value,
									index.value = index.value + length);
						}
						if (type != null && type.equalsIgnoreCase("Integer")) {
							Integer.parseInt(data);
						} else if (type != null
								&& type.equalsIgnoreCase("Long")) {
							Long.parseLong(data);
						} else if (type != null
								&& type.equalsIgnoreCase("String")) {
						} else if (type != null
								&& type.equalsIgnoreCase("Binary")) {
						} else if (type != null
								&& type.equalsIgnoreCase("Char")) {
						} else if (type != null && type.equalsIgnoreCase("Fix")) {
							String value = field.attribute("value").getValue();
							if (!data.equals(value)) {
								logger.error("Fix exception! expect " + value
										+ " but " + data + " is found!");
							}
						} else if (type != null
								&& type.equalsIgnoreCase("Delimiter")) {
							for (int i = 0; i < length; i++) {
								if (!data.substring(i, i + 1).equals(SEPERATOR)) {
									logger
											.error("Format Exception! seperator expected but "
													+ data + " is found!");
								}
							}
						}

						if (name.equalsIgnoreCase("SerialNo")) {
							charge.setCardSerialNo(Long.parseLong(data));
						} else if (name.equalsIgnoreCase("CreditAmount1")) {
							charge.setCredit(Long.parseLong(data));
						} else if (name.equalsIgnoreCase("Pin")) {
							charge.setCardPIN(data);
						} else if (name.equalsIgnoreCase("FileNo")) {
							charge.setFileId(Integer.parseInt(data));
						} else if (name.equalsIgnoreCase("Year")) {
							charge.setYear(Integer.parseInt(data));
						} else if (name.equalsIgnoreCase("L")) {
							charge.setIr(Integer.parseInt(data));
						} else if (name.equalsIgnoreCase("HelpDesk")) {
							charge.setHelpDesk(data);
						}
					} catch (Exception e) {
						logger.error("Format Exception: " + e);
					}

				}
				Organization entity = OrganizationService.findOrganizationByType(companyType).get(0);
				charge.setEntity(entity);
				charge.setState(MTNChargeState.NOT_ASSIGNED);
				GeneralDao.Instance.saveOrUpdate(charge);
				result.add(charge);
			}
		} catch (DocumentException e) {
			logger.error("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		} catch (FileNotFoundException e) {
			logger.error("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		} catch (IOException e) {
			logger.error("Encouter with an exception( "+ e.getClass().getSimpleName()+": "+ e.getMessage()+")" ,e);
		}
		return result;
	}

	private String readUntilSeperator(String line, MyInteger index) {
		String result = "";
		for (;; index.value++) {
			String data = line.substring(index.value, index.value + 1);
			if (!data.equals(SEPERATOR))
				result += data;
			else
				break;
		}
		return result;
	}
}
