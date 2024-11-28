package vaulsys.protocols.infotech;

public class InfotechRequiredFieldsRepository {

    //Should be sorted ascending, like this:!
	
	int[] msg100 = new int[]{2, 3,/*10,*/11, 12, 13,25, 32, 35, 37, 41, 42, 48,53, 64};
	int[] msg110 = new int[]{3, 7,/*10,*/11, 12, 13, 32, 35, 37, 39, 41, 42, 44, 48, 64};
	
    int[] msg200 = new int[]{2, 3, 4, 11, 12, 13, 25, 32, 35, 37, 41, 42, 48, 49, 52, 53, 98}; //MAC should not checked
    int[] msg210 = new int[]{3, 4, 7, 11, 12, 13, 32, 35, 37, 39, 41, 42, 48, 49, 54, 98};

    int[] msg400 = new int[]{2, 3, 4, 11, 12, 13, 25, 32, 35, 37, 41, 42, 48, 49, 53, 90, 95, 98};
    int[] msg410 = new int[]{2, 3, 4, 7, 11, 12, 13, 32, 37, 39, 41, 42, 48, 54};

    int[] msg500 = new int[]{3, 11, 12, 13, 25, 32, 41, 42, 48, 53};
    int[] msg510 = new int[]{3, 7, 11, 12, 13, 32, 39, 41, 42, 48};

    
    int[] msg800 = new int[]{3, 11, 12, 13, 25, 32, 41, 42, 48, 53};
    int[] msg810 = new int[]{3, 7, 11, 12, 13, 32, 39, 41, 42, 48};

    public int[] getRequiredFields(Integer mti) throws Exception {
        switch (mti) {
	        case 100:
	            return msg100;
	        case 110:
	            return msg110;
            case 200:
            case 201:
                return msg200;
            case 210:
            case 211:
                return msg210;
            case 400:
            case 420:
                return msg400;
            case 410:
            case 430:
                return msg410;
            case 500:
            case 520:
                return msg500;
            case 510:
            case 530:
                return msg510;
            case 800:
                return msg800;
            case 810:
                return msg810;
            default:
                throw new Exception("Message not supported.");
        }

    }

}
