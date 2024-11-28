package vaulsys.cms.components;

/**
 * Created by HP on 6/2/2017.
 */
public class CardNumberGenerator {
    //private Random random = new Random(System.currentTimeMillis());

    public CardNumberGenerator() {
    }

    public String generate(String BIN, String strpanSequence) {

        // The number of random digits that we need to generate is equal to the
        // total length of the card number minus the start digits given by the
        // user, minus the check digit at the end.

        // Author: Asim Shahzad, Date: 20th Dec 2017, Desc: Added this code for enabling PAN range
//        CMSPANRange obj_PANRange = ProductManagerService.Instance.loadPANRange(panRangeID);



        //int randomNumberLength = length - (bin.length() + 1);

        StringBuilder builder = new StringBuilder(BIN);
        builder.append(strpanSequence);
        //=================================================================================================
//        for (int i = 0; i < randomNumberLength; i++) {
//            int digit = this.random.nextInt(10);
//            builder.append(digit);
//        }

        // Do the Luhn algorithm to generate the check digit.
        int checkDigit = this.getCheckDigit(builder.toString());
        builder.append(checkDigit);



        return builder.toString();
    }

    //m.rehman: 06-08-2020, Euronet Integration, PAN generation according to pan format defined for scheme
    public String generate(String BIN, String productCode, String strpanSequence) {

        // The number of random digits that we need to generate is equal to the
        // total length of the card number minus the start digits given by the
        // user, minus the check digit at the end.
        StringBuilder builder = new StringBuilder(BIN);
        builder.append(productCode);
        builder.append(strpanSequence);

        // Do the Luhn algorithm to generate the check digit.
        int checkDigit = this.getCheckDigit(builder.toString());
        builder.append(checkDigit);

        return builder.toString();
    }

    private int getCheckDigit(String number) {

        // Get the sum of all the digits, however we need to replace the value
        // of the first digit, and every other digit, with the same digit
        // multiplied by 2. If this multiplication yields a number greater
        // than 9, then add the two digits together to get a single digit
        // number.
        //
        // The digits we need to replace will be those in an even position for
        // card numbers whose length is an even number, or those is an odd
        // position for card numbers whose length is an odd number. This is
        // because the Luhn algorithm reverses the card number, and doubles
        // every other number starting from the second number from the last
        // position.
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {

            // Get the digit at the current position.
            int digit = Integer.parseInt(number.substring(i, (i + 1)));

            if ((i % 2) == 0) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = (digit / 10) + (digit % 10);
                }
            }
            sum += digit;
        }

        // The check digit is the number required to make the sum a multiple of
        // 10.
        int mod = sum % 10;
        return ((mod == 0) ? 0 : 10 - mod);
    }
}
