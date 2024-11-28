package vaulsys.othermains.disagreement;

import vaulsys.clearing.report.ShetabDisagreementService;


public class Form13Comparison {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			String path_toShetab = "D:/disagreement/shetab/910309/Comparison/PAS2rep910309.txt";
			String path_fromShetab = "D:/disagreement/shetab/910309/Comparison";
			String path_result = ShetabDisagreementService.compareForm13(path_toShetab, path_fromShetab);
		}catch(Exception e){
			System.err.println(e);
		}
		System.exit(0);

	}

}
