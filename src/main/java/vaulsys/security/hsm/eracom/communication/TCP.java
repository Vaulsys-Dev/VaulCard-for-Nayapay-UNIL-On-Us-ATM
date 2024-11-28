package vaulsys.security.hsm.eracom.communication;

import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.util.MyInteger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class TCP {
    //    public static Socket socket;
    public static String ip;
    public static int port;

    public static Socket socket;
    public static DataOutputStream os;
    public static DataInputStream is;


    public static void setConnection(String ipC, int portC) {
        ip = ipC;
        port = portC;
    }

    public TCP() {
    }


    public static byte[] sendRequest(byte[] request, boolean keepConnection) {
        byte[] response;

        try {
            //TODO: socket should not be static        
            if (socket == null) {
                socket = new Socket(ip, port);

                os = new DataOutputStream(socket.getOutputStream());
                is = new DataInputStream(socket.getInputStream());
            }

            //            PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
            //            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            //            out.println( request );
            //            os.writeBytes( request );
            //            os.flush();
            //            socket.getOutputStream().flush();

            byte[] actualRequest = new byte[6 + request.length];
            int i = 0;
            actualRequest[i++] = 1;
            actualRequest[i++] = 1; // Version numbrt
            actualRequest[i++] = 0; // Sequence number MSB
            actualRequest[i++] = 0; // Sequence number LSB
            actualRequest[i++] =
                    (byte) (request.length / 256); //Message length MSB
            actualRequest[i++] =
                    (byte) (request.length % 256); // Message Length LSB


            for (int j = 0; j < request.length; j++)
                actualRequest[i++] = request[j];

            //            os.write(header);
            //            os.flush();

            byte[] messageBuffer = new byte[actualRequest.length * 2];
            System.arraycopy(actualRequest, 0, messageBuffer, 0, actualRequest.length);
            System.arraycopy(actualRequest, 0, messageBuffer, actualRequest.length, actualRequest.length);

            actualRequest = messageBuffer;

            System.out.println(b2s(actualRequest));

            os.write(actualRequest);
            os.flush();

            /*
                        out.write(1);//SOH character
                        out.write(1);//Version Number
                        out.write(0);//Sequence Number MSB
                        out.write(0);//Sequence Number LSB
                        out.write(request.length / 256);//Message Length MSB
                        out.println(request.length % 256);//Message Length LSB

                        out.println();
                        out.write(request.toString());
                        out.flush();
            */
            byte[] resp = new byte[2048];
            int j = 0;
            resp[j++] = is.readByte();

            while (is.available() > 0)
                resp[j++] = is.readByte();

            // The first 6 bytes are message header
            // The second 9 bytes are Meta Function header
            // The first two bytes should be 1 like actualRequest[0] and [1]

            if (resp[0] != 1 || resp[1] != 1) {
                response = new byte[]{-1};
                return response;
            }
            // Bytes [2] and [3] are sequence number that should be equal to 
            // the sequence number sent in request
            if (resp[2] != actualRequest[2] || resp[3] != actualRequest[3]) {
                response = new byte[]{-1};
                return response;
            }
            // Bytes[4] and [5] indicate the length of rest of response messge
            // Note that byte data type is signed and therefore there is a
            // possibility of number below than zero that should be converted
            int respLength =
                    (resp[4] >= 0 ? resp[4] : resp[4] + 256) * 256 + (resp[5] >=
                            0 ? resp[5] :
                            resp[5] +
                                    256);
            if ((respLength + 6) != j) {
                response = new byte[]{-1};
                return response;
            }

            //Meta function Header

            if (resp[6] != (byte) 0xE3) {
                response = new byte[]{-1};
                return response;
            }


            if (resp[7] != request[1]) {
                response = new byte[]{-1};
                return response;
            }

            if (resp[8] != request[2]) {
                response = new byte[]{-1};
                return response;
            }

            if (resp[9] != request[3]) {
                response = new byte[]{-1};
                return response;
            }

            // return code
            if (resp[10] != (byte) 0x00) {
                response = new byte[]{resp[10]};
                return response;
            }

            for (int msgID = 0; msgID < 4; msgID++) {
                if (resp[11 + msgID] != request[4 + msgID]) {
                    response = new byte[]{-1};
                    return response;
                }
            }


            MyInteger offfset = new MyInteger(15);
            int dataLength = HSMUtil.getLengthOfVarField(resp, offfset);


            response = new byte[dataLength];
            System.arraycopy(resp, offfset.value, response, 0, dataLength);

//            
//            for (int k = 0; k < respLength; k++)
//                response[k] = resp[k + offfset.value];

            /*            List<Byte> ar =  Arrays.asList(response);

            byte[] buffer = new byte[1024];
            int j;
            ar.add( is.readByte() );

            while(is.available()>0) {
                ar.add(is.readByte());
            }

            ar.toArray(response);
  */
            /*

            String resp;
            byte[] buff = new byte[2048];
            int k = 0;
            while(is.available()>0) {
                buff[k++] = is.readByte();
            }
  */
            if (keepConnection == false) {
                os.close();
                is.close();
                socket.close();
                socket = null;
            }

            return response;
        } catch (IOException e) {
            System.err.println("Exception happened : " + e.getMessage());
        }
        return null;
    }

    private static String b2s(byte[] key) {
        String result = "";
        for (int i = 0; i < key.length; i++) {
            result += key[i] + " ";
        }
        return result;
    }
}
