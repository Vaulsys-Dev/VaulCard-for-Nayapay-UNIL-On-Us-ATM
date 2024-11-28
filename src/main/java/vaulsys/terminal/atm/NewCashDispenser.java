package vaulsys.terminal.atm;

import vaulsys.entity.impl.Branch;
import vaulsys.protocols.ndc.base.config.ErrorSeverity;
import vaulsys.protocols.ndc.constants.NDCSupplyStatusConstants;
import vaulsys.terminal.atm.device.Cassette;
import vaulsys.terminal.atm.device.DeviceLocation;
import vaulsys.util.MathUtil;
import vaulsys.wfe.GlobalContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewCashDispenser {
	private static NewCashDispenser instance;

	public static NewCashDispenser getInstance() {
		if (instance == null)
			instance = new NewCashDispenser();
		return instance;
	}

	public static long getGoodLcm4(Cassette a, Cassette b, Cassette c, Cassette d){
		ArrayList<Cassette> goods = new ArrayList<Cassette>();
		if(a != null )
			goods.add(a);
		if(b != null )
			goods.add(b);
		if(c != null )
			goods.add(c);
		if(d != null )
			goods.add(d);
//		if(a != null && !DeviceStatus.FATAL.equals(a.getStatus()) && a.getNotes()!=0)
//			goods.add(a);
//		if(b != null && !DeviceStatus.FATAL.equals(b.getStatus()) && b.getNotes()!=0)
//			goods.add(b);
//		if(c != null && !DeviceStatus.FATAL.equals(c.getStatus()) && c.getNotes()!=0)
//			goods.add(c);
//		if(d != null && !DeviceStatus.FATAL.equals(d.getStatus()) && d.getNotes()!=0)
//			goods.add(d);
		
		if(goods.size() == 4)
			return MathUtil.lcm(goods.get(0).getDenomination(), goods.get(1).getDenomination(), goods.get(2).getDenomination(), goods.get(3).getDenomination());

		if(goods.size() == 3)
			return MathUtil.lcm(goods.get(0).getDenomination(), goods.get(1).getDenomination(), goods.get(2).getDenomination());
		
		if(goods.size() == 2)
			return MathUtil.lcm(goods.get(0).getDenomination(), goods.get(1).getDenomination());

		return goods.get(0).getDenomination();
	}
	
	public void getNearestPossible(long amount, int[] result, List<Cassette> cassettes) {
		Cassette cassetteA = cassettes.get(0);
		Cassette cassetteB = cassettes.get(1);
		Cassette cassetteC = cassettes.get(2);
		Cassette cassetteD = cassettes.get(3);

		isPossibleToCashByAndByAndByAndBy(amount, result, cassetteA, cassetteB, cassetteC, cassetteD, false);
	}

	private static final long minAmountForNotForceUsingCassetteD = 800000L;
	private static final long maxAmountForForceUsingOnlyCassetteD = 300000L;

    public boolean isPossible(long amount, int[] result, List<Cassette> cassettes) {
        Cassette cassetteA = cassettes.get(0);
        Cassette cassetteB = cassettes.get(1);
        Cassette cassetteC = cassettes.get(2);
        Cassette cassetteD = cassettes.get(3);
        
        boolean applyNewStrategy = GlobalContext.getInstance().getMyInstitution().getBin().equals(502229L) && cassetteA.getAtm().getOwner().getCoreBranchCode().equals("950");
        
        if (((Long)amount).compareTo(maxAmountForForceUsingOnlyCassetteD) < 0) {
            Cassette tempCassetteA = new Cassette(cassetteA.getDenomination(), 0, cassetteA.getErrorSeverity(), cassetteA.getTotalErrorSeverity(), cassetteA.getLocation());
            Cassette tempCassetteB = new Cassette(cassetteB.getDenomination(), 0, cassetteB.getErrorSeverity(), cassetteB.getTotalErrorSeverity(), cassetteB.getLocation());
            Cassette tempCassetteC = new Cassette(cassetteC.getDenomination(), 0, cassetteC.getErrorSeverity(), cassetteC.getTotalErrorSeverity(), cassetteC.getLocation());
            
        	if (isPossibleToCashByAndByAndByAndBy(amount, result, tempCassetteA, tempCassetteB, tempCassetteC, cassetteD, true)) {
            	cassetteA = tempCassetteA;
            	cassetteB = tempCassetteB; 
            	cassetteC = tempCassetteC;
        	}
        }

        boolean b = isPossibleToCashByAndByAndByAndBy(amount, result, cassetteA, cassetteB, cassetteC, cassetteD, true);
        boolean c = false;
        long lcm;
        long num;
        int[] defaultPattern = null;

        if( b == true ) {
        	defaultPattern = Arrays.copyOf(result, result.length);
                if (result[0] > 0 && result[1] == 0 && result[2] == 0 && result[3] == 0) {
                        lcm = getGoodLcm4(cassetteA, cassetteB, cassetteC, cassetteD);
                        num = lcm / cassetteA.getDenomination();

                        if(num <= result[0]) {
                            c = isPossibleToCashByAndByAndBy(lcm, result, cassetteB, cassetteC, cassetteD, true);
                        }
                        if (c == true) {
                            if(result[1] > 0 && result[2] == 0 && result [3] == 0){
                                c = false;
                                result[0] -= num;
                                lcm = getGoodLcm4(null, cassetteB, cassetteC, cassetteD);
                                num = lcm / cassetteB.getDenomination();
                                if(num < result[1]) {
                                    c = isPossibleToCashByAndBy(lcm, result, cassetteC, cassetteD, true);
                                }

	                            if (c == true) {
	                            	if(result[2] > 0 && result [3] == 0 && (!applyNewStrategy || ((Long)amount).compareTo(minAmountForNotForceUsingCassetteD) < 0)){
	                                    c = false;
	                                    result[1] -= num;
	                                    lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
	                                    num = lcm / cassetteC.getDenomination();
	                                    if(num < result[2]){
	                                        c= isPossibleToCashBy(lcm, result, cassetteD, true);
	                                    }
	                                    if(c == true){
                                            result[2] -= num;
	                                    }else{
                                            lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                            num = lcm / cassetteB.getDenomination();
                                            if(num < result[1])
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                            if(c == true)
                                                result[1] -= num;
	                                    }
	                            	} else {
	                            		result[1] -= num;
	                                }
	                            }else{
	                                lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
	                                num = lcm / cassetteB.getDenomination();
	                                if(num < result[1]) {
	                                    c = isPossibleToCashBy(lcm, result, cassetteD, true);
	                                }
	                                if (c == true) {
	                                    result[1] -= num;
	                                }
	                            }
                        }else if(result[1] == 0 && result[2] > 0 && result[3] == 0){
                            c = false;
                            result[0] -= num;
                            lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                            num = lcm / cassetteC.getDenomination();
                            if(num < result[2])
                                c= isPossibleToCashBy(lcm, result, cassetteD, true);
                            if(c == true)
                                result[2] -= num;

                        }else if(result[1] == 0 && result[2] == 0 && result[3] > 0){
                            c = false;
                            result[0] -= num;
                        }else{
                                if(result[1] > 0 && result[2] > 0 && result[3] == 0 && (!applyNewStrategy || ((Long)amount).compareTo(minAmountForNotForceUsingCassetteD) < 0)){
                                        c = false;
                                        result[0] -= num;
                                        lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                        num = lcm /cassetteC.getDenomination();
                                        if (num < result[2])
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        if (c == true)
                                                result[2] -= num;
                                        else{
                                                lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                                 num = lcm / cassetteB.getDenomination();
                                                 if(num < result[1])
                                                         c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                                 if(c == true)
                                                         result[1] -= num;
                                                 else{
                                                         lcm = getGoodLcm4(cassetteA, null, null, cassetteD);
                                                         num = lcm / cassetteA.getDenomination();
                                                         if (num < result[0])
                                                                 c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                                         if (c == true)
                                                                 result[0] -= num;
                                                 }
                                        }

                                }else{

                                if(c == true)
                                        result[0] -= num;
                                }


                        }

                        }else{
                                c = false;
                                lcm = getGoodLcm4(cassetteA, null, cassetteC, cassetteD);
                                num = lcm / cassetteA.getDenomination();
                                if(num < result[0]) {
                                        c = isPossibleToCashByAndBy(lcm, result, cassetteC, cassetteD, true);
                                }
                                if (c == true) {
                                        result[0] -= num;
                                }else{
                                        c = false;
                                        lcm = getGoodLcm4(cassetteA, null, cassetteC, cassetteD);
                                        num = lcm / cassetteA.getDenomination();
                                        if(num < result[0]) {
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        }
                                        if (c == true) {
                                                result[0] -= num;
                                        }
                                }
                        }
                        return b;
                } else if (result[0] == 0 && result[1] > 0 && result[2] == 0 && result[3] == 0) {
                        c = false;
                        lcm = getGoodLcm4(null,cassetteB, cassetteC, cassetteD);
                        num = lcm / cassetteB.getDenomination();
                        if(num < result[1]) {
                                c = isPossibleToCashByAndBy(lcm, result, cassetteC, cassetteD, true);
                        }
                        if (c == true) {
                                c = false;
                                result[1] -= num;
                                lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                num = lcm / cassetteC.getDenomination();
                                if(num < result[2])
                                        c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                if (c == true)
                                        result[2] -=num;
                                else{
                                        lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                        num = lcm / cassetteB.getDenomination();
                                        if(num < result[2])
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        if( c == true)
                                                result[1] -= num;
                                }

                        }
                        return b;
                } else if(result[0] == 0 && result[1] == 0 && result[2]> 0 && result[3] == 0){
                        c = false;
                        lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                        num = lcm / cassetteC.getDenomination();
                        if(num < result[2])
                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                        if( c == true)
                                result[2] -= num;
                        else{
                                lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                num = lcm / cassetteB.getDenomination();
                                if(num < result[1])
                                        c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                if( c == true)
                                        result[1] -= num;
                        }

                }else {
                        if (result[0] > 0 && result[1] > 0 && result[2] == 0 && result[3] == 0) {
                                c = false;
                                lcm = getGoodLcm4(null,cassetteB, cassetteC, cassetteD);
                                num = lcm / cassetteB.getDenomination();
                                if(num < result[1]) {
                                        c = isPossibleToCashByAndBy(lcm, result, cassetteC, cassetteD, true);
                                }
                                if (c == true) {
                                        result[1] -= num;
                                }
                                
                                if (result[0] > 0 && result[1] > 0 && result[2] > 0 && result[3] == 0 && (!applyNewStrategy || ((Long)amount).compareTo(minAmountForNotForceUsingCassetteD) < 0)){
                                        c= false;
                                        lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                        num = lcm / cassetteC.getDenomination();
                                        if(num < result[2])
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        if(c == true)
                                                result[2] -= num;
                                        else{
                                                lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                                num = lcm / cassetteB.getDenomination();
                                                if( num < result[1])
                                                        c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                                if(c == true)
                                                        result[1] -= num;
                                                else{
                                                        lcm = getGoodLcm4(cassetteA, null, null, cassetteD);
                                                        num = lcm / cassetteA.getDenomination();
                                                        if(num < result[0])
                                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                                        if(c == true)
                                                                result[0] -= num;

                                                }
                                        }
                                }
                                return b;
                        }else if (result[0] > 0 && result[1] == 0 && result[2] > 0 && result[3] == 0) {
                                c = false;
                                lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                num = lcm / cassetteC.getDenomination();
                                if(num < result[2])
                                        c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                if (c == true)
                                        result[2] -= num;

                        }else if(result[0] == 0 && result[1] > 0 && result[2] > 0 && result[3]== 0){
                                c = false;
                                lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                num = lcm /cassetteC.getDenomination();
                                if(num < result[2]){
                                        c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                }if (c == true)
                                        result[2]-= num;
                                else{
                                        c = false;
                                        lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                        num = lcm / cassetteB.getDenomination();
                                        if(num < result[1])
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        if(c == true)
                                                result[1] -= num;

                                }
                        }else{
                                if (result[0] > 0 && result[1] > 0 && result[2] > 0 && result[3] == 0 && (!applyNewStrategy || ((Long)amount).compareTo(minAmountForNotForceUsingCassetteD) < 0)) {
                                        c = false;
                                        lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                        num = lcm / cassetteC.getDenomination();
                                        if(num < result[2])
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        if (c == true)
                                                result[2] -= num;
                                        else{
                                                lcm = getGoodLcm4(null, cassetteB, null, cassetteD);
                                                num = lcm / cassetteB.getDenomination();
                                                if(num < result[1])
                                                        c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                                if(c == true)
                                                        result[1] -= num;
                                                else{
                                                        lcm = getGoodLcm4(cassetteA, null, null, cassetteD);
                                                        num = lcm / cassetteA.getDenomination();
                                                        if (num < result[0])
                                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                                        if (c == true)
                                                                result[0] -= num;
                                                }
                                        }
                                        return b;
                                }

                        }
                }
                
                if ((result[0] + result[1] + result[2] + result[3]) > 40) {
                	for (int i = 0; i < defaultPattern.length; i++) {
						result[i] = defaultPattern[i];
					}
                }
        }else{//TODO:zamane rah andazeie getNearestPossible dispense dar in ghesmat check shavad
                b = isPossibleToCashByAndByAndBy(amount, result, cassetteB, cassetteC, cassetteD, true);
                if( b == true ) {
                        if (result[0] == 0 && result[1] > 0 && result[2] == 0 && result[3] == 0) {
                                c = false;
                                lcm = getGoodLcm4(null, cassetteB, cassetteC, cassetteD);
                                num = lcm / cassetteB.getDenomination();
                                if(num < result[1]) {
                                        c = isPossibleToCashByAndBy(lcm, result, cassetteC, cassetteD, true);
                                }
                                if (c == true) {
                                        result[1] -= num;
                                }
                                return b;
                        }
                }else{
                        b = isPossibleToCashByAndBy(amount, result, cassetteC, cassetteD, true);
                        if( b == true ) {
                                if (result[0] == 0 && result[1] == 0 && result[2] > 0 && result[3] == 0) {
                                        c = false;
                                        lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
                                        num = lcm / cassetteC.getDenomination();
                                        if(num < result[2]) {
                                                c = isPossibleToCashBy(lcm, result, cassetteD, true);
                                        }
                                        if (c == true) {
                                                result[2] -= num;
                                        }
                                        return b;
                                }
                        }else{
                                b = isPossibleToCashBy(amount, result, cassetteD, true);
                        }
                }
        }
        
        return b;
}

	private static boolean isPossibleToCashByAndByAndByAndBy(long neededCash, int[] result, Cassette cassetteA, Cassette cassetteB,
			Cassette cassetteC, Cassette cassetteD, boolean forcePossibility) {
		long remindeFromUnitN = 0;
		long i=0;
		long lcm = getGoodLcm4(cassetteA, cassetteB, cassetteC, cassetteD);
		long num = lcm / cassetteA.getDenomination();
		
		boolean r = false;
		
		int notesA = cassetteA.getNotes();
		if (ErrorSeverity.FATAL.equals(cassetteA.getTotalErrorSeverity()) ||
				ErrorSeverity.FATAL.equals(cassetteA.getErrorSeverity()) ||
//				DeviceLocation.OUT.equals(cassetteA.getLocation()) ||
				NDCSupplyStatusConstants.NOT_CONFIGURED.equals(cassetteA.getSupplyStatus()) || 
				NDCSupplyStatusConstants.MEDIA_OUT.equals(cassetteA.getSupplyStatus()) ||
				notesA < 0)
//			if (DeviceStatus.FATAL.equals(cassetteA.getStatus()))
			notesA = 0;

		while(r != true && i < num) {
			remindeFromUnitN = neededCash % cassetteA.getDenomination();
			remindeFromUnitN += i*cassetteA.getDenomination();
			if(remindeFromUnitN > neededCash){
				return false;
			}

			if ((neededCash - remindeFromUnitN) / cassetteA.getDenomination() > notesA) {
				remindeFromUnitN = neededCash - (cassetteA.getDenomination() * notesA);
			} else{ 
				if (((neededCash - remindeFromUnitN) / cassetteA.getDenomination() <= notesA) && remindeFromUnitN == 0
						&& (neededCash - remindeFromUnitN) / cassetteA.getDenomination() != 0) {
					result[0] += (neededCash - remindeFromUnitN) / cassetteA.getDenomination();
					return true;
				}
			}
	
			r = isPossibleToCashByAndByAndBy(remindeFromUnitN, result, cassetteB, cassetteC, cassetteD, forcePossibility);
			if (forcePossibility && r && (neededCash - remindeFromUnitN) / cassetteA.getDenomination() != 0){
				result[0] += (neededCash - remindeFromUnitN) / cassetteA.getDenomination();
				return true;
			}else if(!forcePossibility){
				result[0] += (neededCash - remindeFromUnitN) / cassetteA.getDenomination();
				return false;
			}
			i++;
		}
		
		return r;
	}

	private static boolean isPossibleToCashByAndByAndBy(long neededCash, int[] result, Cassette cassetteB, Cassette cassetteC,
			Cassette cassetteD, boolean forcePossibility) {
		long remindeFromUnitN = 0;

		int notesB = cassetteB.getNotes();
		if (ErrorSeverity.FATAL.equals(cassetteB.getTotalErrorSeverity()) ||
				ErrorSeverity.FATAL.equals(cassetteB.getErrorSeverity()) ||
//				DeviceLocation.OUT.equals(cassetteB.getLocation()) ||
				NDCSupplyStatusConstants.NOT_CONFIGURED.equals(cassetteB.getSupplyStatus()) ||
				NDCSupplyStatusConstants.MEDIA_OUT.equals(cassetteB.getSupplyStatus()) ||
				notesB < 0)
//			if (DeviceStatus.FATAL.equals(cassetteB.getStatus()))
			notesB = 0;
		
		long lcm = getGoodLcm4(null, cassetteB, cassetteC, cassetteD);
		long num = lcm / cassetteB.getDenomination();
		long i=0;
		
		boolean r = false;

		while(r != true && i < num) {
			remindeFromUnitN = neededCash % cassetteB.getDenomination();
			remindeFromUnitN += i*cassetteB.getDenomination();
			if(remindeFromUnitN > neededCash){
				return false;
			}
			
			if ((neededCash - remindeFromUnitN) / cassetteB.getDenomination() > notesB) {
				remindeFromUnitN = neededCash - (cassetteB.getDenomination() * notesB);
			} else{ 
				if (((neededCash - remindeFromUnitN) / cassetteB.getDenomination() <= notesB) && remindeFromUnitN == 0
					&& (neededCash - remindeFromUnitN) / cassetteB.getDenomination() != 0) {
					result[1] += (neededCash - remindeFromUnitN) / cassetteB.getDenomination();
					return true;
				}
			}

			r = isPossibleToCashByAndBy(remindeFromUnitN, result, cassetteC, cassetteD, forcePossibility);
			if (forcePossibility && r && (neededCash - remindeFromUnitN) / cassetteB.getDenomination() != 0){
				result[1] += (neededCash - remindeFromUnitN) / cassetteB.getDenomination();
				return true;
			}
			else if(!forcePossibility){
				result[1] += (neededCash - remindeFromUnitN) / cassetteB.getDenomination();
				return false;
			}
			i++;
		}
		
		return r;
	}

	private static boolean isPossibleToCashByAndBy(long neededCash, int[] result, Cassette cassetteC, Cassette cassetteD, boolean forcePossibility) {
		long remindeFromUnitN = 0;

		int notesC = cassetteC.getNotes();
		if (ErrorSeverity.FATAL.equals(cassetteC.getTotalErrorSeverity()) ||
				ErrorSeverity.FATAL.equals(cassetteC.getErrorSeverity()) ||
//				DeviceLocation.OUT.equals(cassetteC.getLocation()) ||
				NDCSupplyStatusConstants.NOT_CONFIGURED.equals(cassetteC.getSupplyStatus()) || 
				NDCSupplyStatusConstants.MEDIA_OUT.equals(cassetteC.getSupplyStatus()) ||
				notesC < 0)
//			if (DeviceStatus.FATAL.equals(cassetteC.getStatus()))
			notesC = 0;
		
		long lcm = getGoodLcm4(null, null, cassetteC, cassetteD);
		long num = lcm / cassetteC.getDenomination();
		long i=0;
		
		boolean r = false;

		while(r != true && i < num) {
			remindeFromUnitN = neededCash % cassetteC.getDenomination();
			remindeFromUnitN += i*cassetteC.getDenomination();
			if(remindeFromUnitN > neededCash){
				return false;
			}

			if ((neededCash - remindeFromUnitN) / cassetteC.getDenomination() > notesC) {
//				remindeFromUnitN = neededCash - (cassetteC.getDenomination() * notesC);
				remindeFromUnitN = neededCash;
				int j=0;
				while(j+(lcm/cassetteC.getDenomination()) <= notesC){
//					if(j+(lcm/cassetteC.getDenomination()) > notesC){
						remindeFromUnitN -= lcm;
						j+=lcm/cassetteC.getDenomination();
//					}
				}
			} else {
				if (((neededCash - remindeFromUnitN) / cassetteC.getDenomination() <= notesC) && remindeFromUnitN == 0
						&& (neededCash - remindeFromUnitN) / cassetteC.getDenomination() != 0) {
					result[2] += (neededCash - remindeFromUnitN) / cassetteC.getDenomination();
					return true;
				}
			}
	
			r = isPossibleToCashBy(remindeFromUnitN, result, cassetteD, forcePossibility);
			if (forcePossibility && r && (neededCash - remindeFromUnitN) / cassetteC.getDenomination() != 0){
				result[2] += (neededCash - remindeFromUnitN) / cassetteC.getDenomination();
				return true;
			}else if(!forcePossibility){
				result[2] += (neededCash - remindeFromUnitN) / cassetteC.getDenomination();
				return false;
			}
			i++;
		}
		
		return r;
	}

	private static boolean isPossibleToCashBy(long neededCash, int[] result, Cassette cassetteD, boolean forcePossibility ) {
		int notesD = cassetteD.getNotes();
		if (ErrorSeverity.FATAL.equals(cassetteD.getTotalErrorSeverity()) ||
				ErrorSeverity.FATAL.equals(cassetteD.getErrorSeverity()) ||
//				DeviceLocation.OUT.equals(cassetteD.getLocation()) ||
				NDCSupplyStatusConstants.NOT_CONFIGURED.equals(cassetteD.getSupplyStatus()) || 
				NDCSupplyStatusConstants.MEDIA_OUT.equals(cassetteD.getSupplyStatus()) ||
				notesD < 0)
//			if (DeviceStatus.FATAL.equals(cassetteD.getStatus()))
			notesD = 0;
		
		if ((neededCash % cassetteD.getDenomination() == 0)
				&& (notesD * cassetteD.getDenomination() >= neededCash)
				&& neededCash / cassetteD.getDenomination() != 0) {
//			result[3] += neededCash / cassetteD.getDenomination();
			result[3] += (neededCash / cassetteD.getDenomination())-result[3];
			return true;
		}else if(!forcePossibility && neededCash / cassetteD.getDenomination() != 0){
//			result[3] += Math.min(neededCash / cassetteD.getDenomination(), notesD);
			result[3] += Math.min(neededCash / cassetteD.getDenomination(), notesD)-result[3];
		}
		return false;
	}

}
