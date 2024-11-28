package vaulsys.protocols.pos87;

public class Pos87RequiredFieldsRepository {

    //Should be sorted ascending, like this:!
    int[] msg100 = new int[]{2, 3, 7, 11, 12, 13, 17, 25, 32, 33, 37, 41, 42, 48, 100};
    int[] msg110 = new int[]{2, 3, 7, 11, 12, 13, 15, 32, 33, 37, 38, 39, 41, 44, 100, 102};

    int[] msg200 = new int[]{2, 3, 4, 11, 12, 13, 25, 32, 33, 35, 41, 42, 43, 49, 52}; //MAC should not checked
    int[] msg210 = new int[]{3, 4, 7, 11, 12, 13, 15, 32, 33, 35, 37, 39, 41, 42, 43, 48, 49, 51, 54};

    int[] msg400 = new int[]{2, 3, 4, 11, 12, 13, 32, 33, 38, 39, 41, 42, 43, 49, 90, 95};
    int[] msg410 = new int[]{2, 3, 4, 7, 11, 12, 13, 15, 32, 33, 37, 39, 42, 43, 54};

    int[] msg500 = new int[]{7, 11, 15, 17, 32, 33, 50, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 97, 99, 124};
    int[] msg510 = new int[]{11, 12, 13, 25, 32, 39, 41, 42, 43, 48};
    int[] msg502 = new int[]{7, 11, 15, 17, 32, 33, 50, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 97, 99, 124};
    int[] msg512 = new int[]{7, 11, 15, 50, 66, 99};
    int[] msg800 = new int[]{7, 11, 15, 32, 33, 48, 53, 70, 96};
    int[] msg810 = new int[]{7, 11, 15, 32, 33, 39, 48, 70, 96};

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
            case 502:
            case 522:
                return msg502;
            case 512:
            case 532:
                return msg512;
            case 800:
            case 802:
                return msg800;
            case 810:
            case 830:
                return msg810;
            default:
                throw new Exception("Message not supported.");
        }

    }

}
