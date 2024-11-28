package vaulsys.othermains;

import vaulsys.clearing.report.ReportGenerator;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.MyDateFormatNew;

import java.text.ParseException;
import java.util.Date;

public class CoreDisAgreeFile
{
	public static void main(String[] args)
	{
		GeneralDao.Instance.beginTransaction();
		
		Date startDate = null;
		Date endDate = null;
		
//		MyDateFormat formatFullDateTime=new MyDateFormat("yyyyMMddHHmmss");
		
		String strFromDt = "";
		for(int i =0; i<2; i++)
			strFromDt += args[i].trim();
		strFromDt = strFromDt.replace("-", "");
		strFromDt = strFromDt.replace(":", "");
		
		
		String strEndDt = "";
		if(args.length == 4)
		{
			for(int i=2; i<4; i++)
				strEndDt += args[i].trim();
			strEndDt = strEndDt.replace("-", "");
			strEndDt = strEndDt.replace(":", "");
		}

		try
		{
			startDate = MyDateFormatNew.parse("yyyyMMddHHmmss", strFromDt);
			if(args.length == 4)
				endDate = MyDateFormatNew.parse("yyyyMMddHHmmss", strEndDt);
			else
				endDate = null;
		} catch (ParseException e1)
		{
			e1.printStackTrace();
		}

		try
		{
			ReportGenerator.generateCoreReconcileFile(startDate, endDate);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		GeneralDao.Instance.endTransaction();
	}
}
