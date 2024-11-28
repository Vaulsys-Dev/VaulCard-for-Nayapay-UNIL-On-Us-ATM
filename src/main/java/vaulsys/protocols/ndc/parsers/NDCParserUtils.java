package vaulsys.protocols.ndc.parsers;

import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.MyInteger;
import vaulsys.util.Util;
import vaulsys.util.constants.ASCIIConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NDCParserUtils {

    public static void readFS(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        if (rawdata[offset.value] != ASCIIConstants.FS)
            throw new NotParsedBinaryToProtocolException("Malformed message. Expecting FS at byte " + offset.value);

        ++offset.value;
    }
    
    public static byte[] readNull(byte[] rawdata)throws NotParsedBinaryToProtocolException{
    	int a=0;
    	MyInteger offset = new MyInteger(0);
    	byte[] newRawdata = new byte[rawdata.length];
    	
    	for(int j = 0; j<rawdata.length; j++ ){
    		if(offset.value /2 == 0){
    			if(rawdata[offset.value]== 0 && rawdata[offset.value++]==0){
    				a = offset.value - 1;
    				for(int i = 0; i<rawdata.length; i++){
    					while(i < a){
    						newRawdata[i]=rawdata[i];    					
    					}
    					offset.value++;
    					newRawdata[i] = rawdata[offset.value];
    				}
    			}
    			
    		}
    	}
    	return newRawdata;
    	
    }
    
    public static void readFSOrGS(byte[] rawdata, MyInteger offset)throws NotParsedBinaryToProtocolException{
    	if(rawdata[offset.value]!= ASCIIConstants.FS)
    		if(rawdata[offset.value] != ASCIIConstants.GS)
    			throw new NotParsedBinaryToProtocolException("Malformed message. Exception FS or GS at byte" + offset.value);
    	++offset.value;
    }
    
    public static void readGS(byte[] rawdata, MyInteger offset) throws NotParsedBinaryToProtocolException {
        if (rawdata[offset.value] != ASCIIConstants.GS)
            throw new NotParsedBinaryToProtocolException("Malformed message. Expecting GS at byte " + offset.value);

        ++offset.value;
    }

    public static String readUntilFS(byte[] rawdata, MyInteger offset) {
        String output = "";

        for (; offset.value < rawdata.length && rawdata[offset.value] != ASCIIConstants.FS; ++offset.value)
            output += (char) rawdata[offset.value];

        return output;
    }
    
    public static String readUntilFSOrGS(byte[] rawdata, MyInteger offset )throws NotParsedBinaryToProtocolException{
    	String output = "";
    	for (; offset.value< rawdata.length && rawdata[offset.value] != ASCIIConstants.FS && rawdata[offset.value] != ASCIIConstants.GS; ++offset.value)
    		output += (char)rawdata[offset.value];
    	return output;
    }

    public static String readUntilGS(byte[] rawdata, MyInteger offset) {
        String output = "";

        for (; offset.value < rawdata.length && rawdata[offset.value] != ASCIIConstants.GS; ++offset.value)
            output += (char) rawdata[offset.value];

        return output;
    }

    public static byte[] getByteOfReciept(Character printerFlag, String str, Ifx ifx) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (str != null) {
        	if (printerFlag != null)
        		out.write(printerFlag);
            int index;
            for (String item : str.split("[\\[]")) {
                index = item.indexOf("]");
                if (index != -1) {
                    if (item.contains("ifx")) {
                        Object obj = ifx.get(item.substring(0, index));
                        if (obj != null)
                            out.write(obj.toString().getBytes());
                    } else {
                        out.write(ASCIIConstants.getValue(item.substring(0, index)));
                    }
                    out.write(item.substring(index + 1).getBytes());
                } else {
                    out.write(item.getBytes());
                }
            }

            return out.toByteArray();
        }
        return null;
    }

    public static byte[] getByteOfReciept(Character printerFlag, String str) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (str != null) {
            out.write(printerFlag);
            int index;
            for (String item : str.split("[\\[]")) {
                index = item.indexOf("]");
                if (index != -1) {

                    out.write(ASCIIConstants.getValue(item.substring(0, index)));

                    out.write(item.substring(index + 1).getBytes());
                } else {
                    out.write(item.getBytes());
                }
            }

            return out.toByteArray();
        }
        return null;
    }

    public static String getRealAmount(String str, String bufferB, String bufferC) throws IOException {
        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (str != null) {
            int index;
            str = str.replaceAll("bufferb", bufferB);
            str = str.replace("bufferc", bufferC);

            return str;
        }
        return null;
    }

    public static void setIfxFields(String str, String buffer, Ifx ifx) throws IOException {
        if (str != null) {
//			for (String item : str.split(";"))
//			{
            if (str.contains("ifx")) {
//					int indexOfIfx = item.indexOf("ifx.");
//					int indexOfEndIfx = item.indexOf("=");
                ifx.set(str/*.substring(indexOfIfx, indexOfEndIfx)*/, buffer);
            }
//			}
        }
    }

    public static int[] parseDispensedNote(String noteDispense, int digitNo) {
    	List<Integer> notes = new ArrayList<Integer>();
    	int[] result = new int[notes.size()];
    	if (!Util.hasText(noteDispense))
    		return result;
        int index = 0;
        while (noteDispense.length() > index) {
            int note = Integer.parseInt(noteDispense.substring(index, index + digitNo));
            notes.add(note);
            index += digitNo;
        }
        result = new int[notes.size()];
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i) == null)
                result[i] = 0;
            else
                result[i] = notes.get(i);
        }
        return result;
    }
    
    public static int[]parseDepositNote(String noteDeposite, int digitNo){
    	List<Integer> notes = new ArrayList<Integer>();
    	int[] result = new int[notes.size()];
    	
    	if (!Util.hasText(noteDeposite))
    		return result;
    	int index=2;
    	while(noteDeposite.length() > index){
    		int note = Integer.parseInt(noteDeposite.substring(index, index+digitNo));
    		notes.add(note);
    		index += digitNo;
    	}
    	result = new int[notes.size()];
    	for (int i=0; i < notes.size(); i++){
    		if (notes.get(i) == null)
    			result[i] = 0;
    		else
    			result[i] = notes.get(i);
    	}
    	
    	return result;
    }
    
    public static int[]parseNoteId(String noteDeposite, int digitNo){
    	List<Integer> notes = new ArrayList<Integer>();
    	int[] result = new int[notes.size()];
    	
    	if (!Util.hasText(noteDeposite))
    		return result;
    	int index=2;
    	while(noteDeposite.length() > index){
    		int note = Integer.parseInt(noteDeposite.substring(index, index+digitNo));
    		notes.add(note);
    		index += digitNo;
    	}
    	result = new int[notes.size()];
    	for (int i=0; i < notes.size(); i++){
    		if (notes.get(i) == null)
    			result[i] = 0;
    		else
    			result[i] = notes.get(i);
    	}
    	
    	return result;
    }

}
