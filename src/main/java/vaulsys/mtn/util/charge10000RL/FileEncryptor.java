package vaulsys.mtn.util.charge10000RL;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Hex;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.io.*;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.StringTokenizer;

public class FileEncryptor {
    public static transient Logger logger = Logger.getLogger(FileEncryptor.class);

	public static final int SERIAL_LEN = 11;
	public static final int PIN_LEN = 14;
	public static final int ONDEMAND_PIN_TYPE = 35;
	public static final int BULK_PIN_TYPE = 37;
	public static final Long AMOUNT = 10000L;
	public static final String CURRENCY = "IRR";
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		String inFileName = "";
		if(args.length < 1) {
			logger.info("Usage: encryptor <input-file-name>" );
			inFileName = "C:/Users/Kamelia/Desktop/Tasks/Task57-mci1000Format/test/output206_33_37.dat.decr";

		}else{			
			inFileName = args[0];
		}
		/******************************** cipher ********************************/
		Cipher cipher = null;
		try {
			cipher = getCipherOfPublicKey();
		} catch (Exception e) {
			logger.error("Key load problem: " + e.getMessage());
			System.exit(1);
		}
		/************************************************************************/
		File origFile = new File(inFileName);
		File encFile = new File(inFileName+".encrypted");
		FileOutputStream out = null;
		BufferedReader in = null;
		try {
			out = new FileOutputStream(encFile);
			in = new BufferedReader(new FileReader(origFile));
			String line;
			int lineNo = 0;
			int numOfValidCharge = 0;
			while((line = in.readLine())!= null) {
//				try {
					StringTokenizer tokenizer = new StringTokenizer(line, ",");
					//serial
					String serial  = tokenizer.nextToken().trim();
					if(serial.length() != SERIAL_LEN){
						logger.error("serialNum's length is not: " + SERIAL_LEN + " in line:" + line);
						throw new RuntimeException();
						
					}
					out.write((serial + ", ").getBytes());
					//pin
					String pin = tokenizer.nextToken().trim();
					if(pin.length() != PIN_LEN){
						logger.error("pin's length is not: " + PIN_LEN + " in line:" + line);
						throw new RuntimeException();
					}
					out.write(Hex.encode(cipher.doFinal(pin.getBytes())));
					out.write(", ".getBytes());
					//pinType
					int pinType = Integer.valueOf(tokenizer.nextToken().trim());
					if(pinType != ONDEMAND_PIN_TYPE && pinType != BULK_PIN_TYPE){
						logger.error("pin type is invalid in line:" + line);
						throw new RuntimeException();
					}
					out.write((pinType + ",").getBytes());
					//amount
					String amt = tokenizer.nextToken().trim();
					Long amount = 0L;
					if(amt.contains("."))
						amount = Long.valueOf(amt.substring(0, amt.indexOf(".")));
					else 
						amount = Long.valueOf(amt);
					if(!AMOUNT.equals(amount)){
						logger.info("amount is invalid in line:" + line);
						throw new RuntimeException();
					}
					out.write((amt + ", ").getBytes());
					//Currency
					String currency = tokenizer.nextToken().trim();
					if(!CURRENCY.equals(currency)){
						logger.info("currency is invalid in line:" + line);
						throw new RuntimeException();
					}
					out.write((currency + ", ").getBytes());
					//Expiry Date
					String expDt = tokenizer.nextToken().trim();
					out.write((expDt + ", ").getBytes());
					//pin len (new fields)
					out.write((pin.length() + "").getBytes());
					out.write('\n');
//				}catch(Exception e){
//					logger.info("This charge is invalid: " + line);
//					lineNo ++;
//					continue;
//				}
				numOfValidCharge ++;
				lineNo ++;
			}
			in.close();
			out.close();
			
			logger.info("Number of All charge is: " + lineNo);
			logger.info("Number of valid charge is: " + numOfValidCharge);
			logger.info("Number of invalid charge is: " + (lineNo - numOfValidCharge));
			
		} catch (Exception e) {
			logger.error("File read/write problem: " + e.getMessage());
			in.close();
			out.close();
			encFile.delete();
			System.exit(1);
		}
		logger.info(String.format("Encrypted file [%s] of original file [%s] is created successfully\n",
				encFile.getName(), origFile.getName()));
		logger.info("Time: "+(System.currentTimeMillis()-start));
		System.exit(0);
	}

	private static Cipher getCipherOfPublicKey() throws Exception{
		InputStream in = new FileInputStream("public.key");
		byte[] b = new byte[10000];
		in.read(b);
		PublicKey pub = new RSAPublicKeyImpl(b);

		Provider provider = (Provider)Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance();
        Security.addProvider(provider);
        Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
		cipher.init(Cipher.ENCRYPT_MODE, pub);
		in.close();
		return cipher;
	}
}
