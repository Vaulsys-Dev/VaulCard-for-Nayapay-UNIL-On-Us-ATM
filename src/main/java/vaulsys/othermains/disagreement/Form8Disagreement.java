package vaulsys.othermains.disagreement;

import vaulsys.clearing.report.ShetabDisagreementService;

public class Form8Disagreement {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			System.out.println("begin find disagreement");
			String path_switch = "C:/Users/Kamelia/Desktop/Tasks/Task148- Kiosk in disagreement forms/930131/PAS930131.txt";
			String path_shetab = "C:/Users/Kamelia/Desktop/Tasks/Task148- Kiosk in disagreement forms/930131";
			ShetabDisagreementService.compareForm8(path_switch, path_shetab);
			
		} catch(Exception e) {
			e.printStackTrace();
		}

	}

}
