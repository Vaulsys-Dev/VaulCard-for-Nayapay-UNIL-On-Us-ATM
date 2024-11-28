package vaulsys.security.hsm.eracom.status;

import vaulsys.security.hsm.eracom.HSMFuncs;


public class Status {
    public static HSMStatusResp HSM_Status() {
        HSMFuncs func = new HSMFuncs(new byte[]{1}, null);
        func.sendRequest();

        HSMStatusResp statusResp = new HSMStatusResp(func.response, 1);
        return statusResp;
    }

    public static HSMErrLogStatResp HSM_Err_Log_Status() {
        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xFF, (byte) 0xF0, (byte) 0x00},
                        null);

        func.sendRequest();

        HSMErrLogStatResp errLogStatResp =
                new HSMErrLogStatResp(func.response, 2);
        return errLogStatResp;
    }

    public static HSMGetErrLogResp HSM_Get_Err_Log(int errorLogFileNo,
                                                   int errorLogIndex,
                                                   byte getLogsBeforeOrAfter) /*0=before, 1=after*/ {
        return /*dummy*/ /*dummy*/Get_Err_Log(errorLogFileNo, errorLogIndex,
                "01012000", "240000",
                getLogsBeforeOrAfter);
    }

    public static HSMGetErrLogResp HSM_Get_Err_Log(int errorLogFileNo,
                                                   String errorLogDate,
                                                   /*ddmmyyyy*/String errorLogTime,
                                                   /*hhmmss*/byte getLogsBeforeOrAfter) /*0=before, 1=after*/ {
        return Get_Err_Log(errorLogFileNo, 0, errorLogDate, errorLogTime,
                getLogsBeforeOrAfter);
    }

    private static HSMGetErrLogResp Get_Err_Log(int errorLogFileNo,
                                                int errorLogIndex,
                                                String errorLogDate,
                                                /*ddmmyyyy*/String errorLogTime,
                                                /*hhmmss*/byte getLogsBeforeOrAfter) /*0=before, 1=after*/ {

        if (errorLogFileNo < 0 || errorLogFileNo > 100)
            return null;
        if (errorLogIndex < 0 || errorLogIndex > 65536)
            return null;
        if (errorLogDate.length() != 8)
            return null;
        if (errorLogTime.length() != 6)
            return null;

        byte[] parameters = new byte[18];
        //setting parameters
        int i = 0;
        parameters[i++] = (byte) errorLogFileNo;
        parameters[i++] = (byte) (errorLogIndex / 256);
        parameters[i++] = (byte) (errorLogIndex % 256);
        for (int j = 0; j < errorLogDate.length(); j++)
            parameters[i++] = (byte) errorLogDate.charAt(j);
        for (int j = 0; j < errorLogTime.length(); j++)
            parameters[i++] = (byte) errorLogTime.charAt(j);
        parameters[i++] = getLogsBeforeOrAfter;

        HSMFuncs func =
                new HSMFuncs(new byte[]{(byte) 0xFF, (byte) 0xF1, (byte) 0x00},
                        parameters);

        //sending requrest
        func.sendRequest();

        HSMGetErrLogResp resp = new HSMGetErrLogResp(func.response, 2);
        return resp;
    }
}
