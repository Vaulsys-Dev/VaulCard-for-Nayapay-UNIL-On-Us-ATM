package vaulsys.protocols.ndc.constants;

import java.util.ArrayList;

public class NDCPrinterData {
    private ArrayList<String> dontPrintList;
    private ArrayList<String> printOnJournalPrinterOnlyList;
    private ArrayList<String> printOnCustomerPrinterOnlyList;
    private ArrayList<String> printOnbothprintersList;
    private ArrayList<String> printOnDepositList;

    public ArrayList<String> getDontPrintList() {
        return dontPrintList;
    }

    public void setDontPrintList(ArrayList<String> dontPrintList) {
        this.dontPrintList = dontPrintList;
    }

    public ArrayList<String> getPrintOnJournalPrinterOnlyList() {
        return printOnJournalPrinterOnlyList;
    }

    public void setPrintOnJournalPrinterOnlyList(
            ArrayList<String> printOnJournalPrinterOnlyList) {
        this.printOnJournalPrinterOnlyList = printOnJournalPrinterOnlyList;
    }

    public ArrayList<String> getPrintOnCustomerPrinterOnlyList() {
        return printOnCustomerPrinterOnlyList;
    }

    public void setPrintOnCustomerPrinterOnlyList(
            ArrayList<String> printOnCustomerPrinterOnlyList) {
        this.printOnCustomerPrinterOnlyList = printOnCustomerPrinterOnlyList;
    }

    public ArrayList<String> getPrintOnbothprintersList() {
        return printOnbothprintersList;
    }

    public void setPrintOnbothprintersList(
            ArrayList<String> printOnbothprintersList) {
        this.printOnbothprintersList = printOnbothprintersList;
    }

    public ArrayList<String> getPrintOnDepositList() {
        return printOnDepositList;
    }

    public void setPrintOnDepositList(ArrayList<String> printOnDepositList) {
        this.printOnDepositList = printOnDepositList;
    }

}
