package vaulsys.security.hsm.eracom.status;

public class HSMErrLogStatResp {
    public byte returnCode;
    public byte numErrorLogFiles;

    class ErrorLogFile {
        public byte errorLogFileNo;
        public short totalNumOfErrorsLogged;
        public String firstErrorLogDate;
        public String firstErrorLogTime;
        public String lastErrorLogDate;
        public String lastErrorLogTime;
    }

    ErrorLogFile[] errorLogFile;

    public HSMErrLogStatResp(byte[] result, int offset) {
        if ((returnCode = result[offset++]) != 0) {
            //an error occured so all the variables are set to zero
            numErrorLogFiles = -1;
            errorLogFile = null;
            return;
        }

        numErrorLogFiles = result[offset++];

        errorLogFile = new ErrorLogFile[numErrorLogFiles];

        for (int i = 0; i < numErrorLogFiles; i++) {
            errorLogFile[i] = new ErrorLogFile();
            errorLogFile[i].errorLogFileNo = result[offset++];
            errorLogFile[i].totalNumOfErrorsLogged =
                    (short) ((result[offset] >= 0 ? result[offset++] :
                            result[offset++] + 256) +
                            256 * (result[offset] >= 0 ? result[offset++] :
                                    result[offset++] + 256));

            errorLogFile[i].firstErrorLogDate = "";
            for (int j = 0; j < 8; j++)
                errorLogFile[i].firstErrorLogDate += (char) result[offset++];

            errorLogFile[i].firstErrorLogTime = "";
            for (int j = 0; j < 6; j++)
                errorLogFile[i].firstErrorLogTime += (char) result[offset++];

            errorLogFile[i].lastErrorLogDate = "";
            for (int j = 0; j < 8; j++)
                errorLogFile[i].lastErrorLogDate += (char) result[offset++];

            errorLogFile[i].lastErrorLogTime = "";
            for (int j = 0; j < 6; j++)
                errorLogFile[i].lastErrorLogTime += (char) result[offset++];
        }
    }

    public String toString() {
        String stroutput = "";
        stroutput += "numErrorLogFiles: " + numErrorLogFiles + "\n";

        for (int i = 0; i < numErrorLogFiles; i++) {
            stroutput +=
                    "errorLogFile[" + i + "].errorLogFileNo: " + errorLogFile[i].errorLogFileNo;
            stroutput +=
                    "errorLogFile[" + i + "].totalNumOfErrorsLogged: " + errorLogFile[i].totalNumOfErrorsLogged;
            stroutput +=
                    "errorLogFile[" + i + "].firstErrorLogDate: " + errorLogFile[i].firstErrorLogDate;
            stroutput +=
                    "errorLogFile[" + i + "].firstErrorLogTime: " + errorLogFile[i].firstErrorLogTime;
            stroutput +=
                    "errorLogFile[" + i + "].lastErrorLogDate: " + errorLogFile[i].lastErrorLogDate;
            stroutput +=
                    "errorLogFile[" + i + "].lastErrorLogTime: " + errorLogFile[i].lastErrorLogTime;
            stroutput += "\n";
        }

        return stroutput;
    }
}
