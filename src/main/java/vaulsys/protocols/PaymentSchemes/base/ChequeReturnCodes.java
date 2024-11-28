package vaulsys.protocols.PaymentSchemes.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Raza on 21-Jun-18.
 */
public class ChequeReturnCodes {

    public static final Map<Character,String> CheckReturnCodesList = new HashMap<Character, String>();

    public static void load()
    {
        CheckReturnCodesList.put('A',"customer does not have sufficient funds to cover the item");
        CheckReturnCodesList.put('B',"uncollected funds hold");
        CheckReturnCodesList.put('C',"Stop payment – a stop payment has been placed on the item");
        CheckReturnCodesList.put('D',"Closed account – the item’s account has been closed");
        CheckReturnCodesList.put('E',"unable to locate account");
        CheckReturnCodesList.put('F',"Frozen/blocked account – account has restrictions placed by customer or bank");
        CheckReturnCodesList.put('G',"Stale dated – the date on the item is more than 6 months old");
        CheckReturnCodesList.put('H',"Post dated – the date on the item is in the future");
        CheckReturnCodesList.put('I',"Endorsement missing");
        CheckReturnCodesList.put('J',"Endorsement irregular");
        CheckReturnCodesList.put('K',"Signature(s) missing");
        CheckReturnCodesList.put('L',"Signature(s) irregular, suspected forgery");
        CheckReturnCodesList.put('M',"Non-cash item (non negotiable)");
        CheckReturnCodesList.put('N',"Altered/fictitious item/Suspected counterfeit/Counterfeit");
        CheckReturnCodesList.put('O',"Unable to process");
        CheckReturnCodesList.put('P',"Items exceeds stated max value");
        CheckReturnCodesList.put('Q',"Not authorized RCC");
        CheckReturnCodesList.put('R',"Branch/account sold (Wrong Bank)");
        CheckReturnCodesList.put('S',"Refer to Maker");
        CheckReturnCodesList.put('T',"Item cannot be re-presented (exceeds allowable number of presentments)");
        CheckReturnCodesList.put('U',"unusable image");
        CheckReturnCodesList.put('V',"Image fails security check");
        CheckReturnCodesList.put('W',"Cannot determine amount");
        CheckReturnCodesList.put('X',"Refer to image – return reason is contained within the image of the item");
        CheckReturnCodesList.put('Y',"Duplicate presentment");
        CheckReturnCodesList.put('Z',"Forgery – an affidavit shall be available upon request");
        CheckReturnCodesList.put('1',"Does not conform with ANS X9.100-181");
        CheckReturnCodesList.put('2',"Does not conform to the Industry’s Universal Companion Document");
        CheckReturnCodesList.put('3',"Warranty Breach (includes Rule 8 & Rule 9 claims)");
        CheckReturnCodesList.put('4',"RCC Warranty Breach (Rule 8)");
        CheckReturnCodesList.put('5',"Forged and Counterfeit Warranty Breach (Rule 9)");
        CheckReturnCodesList.put('6',"Retired/Ineligible Routing Number");
    }

}
