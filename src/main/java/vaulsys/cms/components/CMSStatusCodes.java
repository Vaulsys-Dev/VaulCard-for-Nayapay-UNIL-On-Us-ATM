package vaulsys.cms.components;

import vaulsys.persistence.GeneralDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Raza on 11-May-17.
 */
//This Class Loads the Card, Account and Customer Status as configured in DB.
public class CMSStatusCodes {

    public CMSStatusCodes()
    {

    }

    private String code;

    private String Description;

    public static Map<String, String> CustStausMap = new HashMap<String, String>(); //for customer status
    public static Map<String, String> AcctStausMap = new HashMap<String, String>(); //for account status
    public static Map<String, String> CardStausMap = new HashMap<String, String>(); //for card status
    //public static List<Map<String, String>> CustStausList;

    public static Map<String, String> AcctStaus = new HashMap<String, String>(); //for account status


    public String MapCustStatus(String csutstatus)
    {
        return "";
    }

    public String MapAcctStatus(String acctstatus)
    {
        return "";
    }

    public static void LoadCodes()
    {
        /*String query = "select code, description from CMS_CUSTSTCODES";
        List<Object> list = GeneralDao.Instance.executeSqlQuery(query);
        for (Object obj : list) {
            System.out.println("Object [" + obj + "]");
            //CustStausMap.put(obj. getCode(), obj.Description);
        }*/

        //String query = "select new map(ccode.code as code, ccode.description as description) from CMS_CUSTSTCODES ccode";
        //CustStausMap = GeneralDao.Instance.getCurrentSession().createSQLQuery(query).list();


        //String query = "select code, description from CMS_CUSTSTCODES";
        //CustStaus = GeneralDao.Instance.executeSqlQuery(query);
        //CustStausMap = (HashMap<String, String>)GeneralDao.Instance.executeSqlQuery(query);
        //CustStausMap = (HashMap<String, String>)GeneralDao.Instance.executeSqlQuery(query);
        //List<Object[]> CustStatusList;
        List<Object[]> templist;

        String query = "select code, description from CMS_CUSTSTCODES";
        templist = GeneralDao.Instance.executeSqlQuery(query);

        for (Object[] obj : templist) {
            //System.out.println("Cust Object Key [" + obj[0] + "]"); //Raza TEMP
            //System.out.println("Cust Object Value [" + obj[1] + "]"); //Raza TEMP
            CustStausMap.put(""+obj[0],""+obj[1]); //Map made for CustCodes
            //CustStausMap.put(obj. getCode(), obj.Description);
        }

        //List<Object[]> AcctStatusList;
        templist = null;
        //Account Codes start
        query = "select code, description from CMS_ACCTSTCODES";
        templist = GeneralDao.Instance.executeSqlQuery(query);

        for (Object[] obj : templist) {
            //System.out.println("Acct Object Key [" + obj[0] + "]"); //Raza TEMP
            //System.out.println("Acct Object Value [" + obj[1] + "]"); //Raza TEMP
            AcctStausMap.put(""+obj[0],""+obj[1]); //Map made for CustCodes
            //CustStausMap.put(obj. getCode(), obj.Description);
        }
        //Account Codes end

        //List<Object[]> CardStatusList;
        templist = null;
        //Card Codes start
        query = "select code, description from CMS_CARDSTCODES";
        templist = GeneralDao.Instance.executeSqlQuery(query);

        for (Object[] obj : templist) {
            //System.out.println("Card Object Key [" + obj[0] + "]"); //Raza TEMP
            //System.out.println("Card Object Value [" + obj[1] + "]"); //Raza TEMP
            CardStausMap.put(""+obj[0],""+obj[1]); //Map made for CustCodes
            //CustStausMap.put(obj. getCode(), obj.Description);
        }
        //Card Codes end
        /*for(int i=0; i<CustStaus.size();i++)
        {
            System.out.println("CustStatus at [" + i + "] = [" + CustStaus + "]");
        }*/

        //for (int i=0 ; i<CustStausList.size(); i++)
        //{
            //System.out.println("Element Got at i [" + i + "] = [" + CustStausList.get(i).code + "]");
            //System.out.println("Element Got at i [" + i + "] = [" + CustStausList.get(i).getDescription() + "]");
            //System.out.println("Element Got at i [" + i + "] = Values [" + CustStausList.get(i).values() + "]");
        //}


    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
