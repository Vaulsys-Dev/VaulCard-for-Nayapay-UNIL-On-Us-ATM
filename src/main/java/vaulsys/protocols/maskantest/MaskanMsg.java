package vaulsys.protocols.maskantest;

import vaulsys.calendar.DateTime;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.util.MyDateFormatNew;

import java.text.ParseException;

public class MaskanMsg implements Cloneable, ProtocolMessage{

	String depositNumber;
	DateTime date;
	String ref1;
	String ref2;
	String branch;
	String result;
	String type;
	String xml;
	
	public MaskanMsg(){
	}
	
	@Override
	public Boolean isRequest() throws Exception {
		return type.equals("request");
	}
	
	public void unpack(byte[] data) throws ParseException{
		String strMsg = new String(data);
//        MyDateFormat dateFormatyyyyMMDDhhmmss = new MyDateFormat("yyyy/MM/dd HH:mm:ss");
        xml = strMsg;
        
		String[] parts = strMsg.split(";");
		for(String param:parts){
			String[] part = param.split("=");
			if(part[0].startsWith("depositNumber")){
				depositNumber = part[1];
			}else if(part[0].startsWith("date")){
				date = new DateTime(MyDateFormatNew.parse("yyyy/MM/dd HH:mm:ss", part[1].trim()));
			}else if(part[0].startsWith("ref1")){
				ref1 = part[1];
			}else if(part[0].startsWith("ref2")){
				ref2 = part[1];
			}else if(part[0].startsWith("branch")){
				branch = part[1];
			}else if(part[0].startsWith("type")){
				type = part[1];
			}else if(part[0].startsWith("result")){
				result = part[1];
			}
		}
		
	}
	
	public byte[] pack(){
		String out = "depositNumber="+depositNumber+";date="+date.getDayDate()+" "+date.getDayTime()+";ref2="+ref2+";ref1="+ref1+";branch="+branch+";type="+type+";";
		if(result != null && result != "")
			out += "result="+result+";";
        xml = out;
		return out.getBytes();
	}	
}
