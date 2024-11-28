package vaulsys.protocols.pos87.encoding;

import vaulsys.authorization.policy.Bank;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankBinToFarsi {

    private static Map<Long, byte[]> banks;
    
    static{
    	banks = new HashMap<Long, byte[]>();
    	
    	List<Bank> list = GeneralDao.Instance.find("from "+ Bank.class.getName());
    	
    	for(Bank bank : list){
    		byte[] farsi = getHasinFarsiConvertor().encode(bank.getName());
    		banks.put(new Long(bank.getBin()), farsi);
    	}
    	
    	banks.put(Long.MAX_VALUE, getHasinFarsiConvertor().encode("کارت شتاب"));
    }
    
    public static byte[] bankName(long bin) {
        byte[] farsiString = banks.get(bin);
        if (farsiString == null)
            return banks.get(Long.MAX_VALUE);
        return farsiString;
    }
    
    static EncodingConvertor getHasinFarsiConvertor(){
    	return ProcessContext.get().getConvertor("HASIN_CONVERTOR");
    }
}
