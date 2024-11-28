package vaulsys.initializer.atm.util;

import vaulsys.application.Application;
import vaulsys.application.BaseApp;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.atm.customizationdata.StateData;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class OperationKeyControlMain {
	
	public static void main(String[] args){
		
		
		GeneralDao.Instance.beginTransaction();
		int count=0,index=0;
		try{
			
			List<ATMRequest> atmRequests = GeneralDao.Instance.find("from ATMRequest");
			//GeneralDao.Instance.endTransaction();
			GeneralDao.Instance.commit();
			
			count = atmRequests.size();
			//System.out.println("Count : "+count);
			
			while(true){
				
				boolean isExist = false;
				String inputMessage=JOptionPane.showInputDialog(null,"لطفا مقدار کلید را همراه با فاصله ها وارد نمائید:");
				System.out.println(inputMessage);
				System.out.println(inputMessage.toUpperCase());
				if(inputMessage.toUpperCase().trim().equals("EXIT".trim()))
					break;
				
				if(inputMessage.length() != 8)
				{
					JOptionPane.showMessageDialog(null,"طول کلید باید به تعداد 8 کاراکتر باشد");
					continue;
				}
				
				for(ATMRequest atmRequest: atmRequests)
				{
					index += 1;
					
					if(index == 67)
					{
						String st="";
					}
					if(atmRequest.getOpkey() != null && atmRequest.getOpkey().toUpperCase().equals(inputMessage.toUpperCase())){
						isExist = true;
						JOptionPane.showMessageDialog(null,"غیر مجاز!!!کلید قبلا ایجاد شده است");
						break;
					}
					
					//System.out.println(index+" {OpKey}: "+atmRequest.getOpkey());
				}
				
				if(!isExist)
				{
					JOptionPane.showMessageDialog(null,"مجاز!!! کلید قبلا ایجاد نشده است");
				}
			}
			
		}catch (Exception e) {
			// TODO: handle exception
			
			System.out.println(e.getMessage());
		}finally
		{
			GeneralDao.Instance.endTransaction();
		}
	}
	
	

}
