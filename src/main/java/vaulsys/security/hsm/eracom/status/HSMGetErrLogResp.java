package vaulsys.security.hsm.eracom.status;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

import java.util.ArrayList;


public class HSMGetErrLogResp {
    public byte returnCode;
    public byte errorLogFileNo;

    class ErrorLog {
        public short errorLogIndexNo;
        public String errorLogData;
    }

    ErrorLog[] errorLogs;

    public HSMGetErrLogResp(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            errorLogFileNo = -1;
            errorLogs = null;
            return;
        }

        errorLogFileNo = result[offset++];

        ArrayList arList = new ArrayList();
        ErrorLog errLog;

        //TODO: some logs are unusall there is a LF in the middle of a log
        //example: index: 100 from log file 0 (may be changed after while!)
        while (offset < result.length) {
            errLog = new ErrorLog();
            errLog.errorLogIndexNo =
                    (short) (256 * (result[offset] >= 0 ? result[offset++] :
                            result[offset++] + 256) +
                            (result[offset] >= 0 ? result[offset++] :
                                    result[offset++] + 256));

            MyInteger ofset = new MyInteger(offset);
            int length = HSMUtil.getLengthOfVarField(result, ofset);
            offset = ofset.value;
            //            offset++;

            errLog.errorLogData = "";
            // while not CRLF

            for (int counter = 0; counter < length; counter++)
                errLog.errorLogData += (char) result[offset++];
            // ignore CRLF
            //            offset++;
            arList.add(errLog);
        }

        errorLogs = new ErrorLog[arList.size()];
        for (int counter = 0; counter < errorLogs.length; counter++)
            errorLogs[counter] = (ErrorLog) arList.get(counter);

    }

    public String toString() {
        String stroutput = "";
        stroutput += "Error Log File Number: " + errorLogFileNo + "\n";

        int counter = 0;
        while (counter < errorLogs.length) {
            stroutput +=
                    "ErrorLog[" + errorLogs[counter].errorLogIndexNo + "]: " +
                            errorLogs[counter].errorLogData;
            //            stroutput += "\n";
            counter++;
        }

        return stroutput;
    }
}
