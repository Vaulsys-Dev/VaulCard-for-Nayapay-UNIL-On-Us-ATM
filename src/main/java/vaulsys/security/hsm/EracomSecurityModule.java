/*
 * Copyright (c) 2000 jPOS.org.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *    "This product includes software developed by the jPOS project
 *    (http://www.jpos.org/)". Alternately, this acknowledgment may
 *    appear in the software itself, if and wherever such third-party
 *    acknowledgments normally appear.
 *
 * 4. The names "jPOS" and "jPOS.org" must not be used to endorse
 *    or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    license@jpos.org.
 *
 * 5. Products derived from this software may not be called "jPOS",
 *    nor may "jPOS" appear in their name, without prior written
 *    permission of the jPOS project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE JPOS PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the jPOS Project.  For more
 * information please see <http://www.jpos.org/>.
 */

package vaulsys.security.hsm;

import vaulsys.security.exception.SMException;
import vaulsys.security.hsm.eracom.ESMDriver;
import vaulsys.security.hsm.eracom.KeySpec.KeyHandler;
import vaulsys.security.hsm.eracom.KeySpec.KeySpecifier;
import vaulsys.security.hsm.eracom.base.AlgorithmQualifier;
import vaulsys.security.hsm.eracom.base.HSMUtil;
import vaulsys.security.hsm.eracom.base.Padding;
import vaulsys.security.securekey.HSMStoredSecureDESKey;
import vaulsys.security.securekey.SecureDESKey;
import vaulsys.security.ssm.base.BaseSMAdapter;
import vaulsys.security.ssm.base.EncryptedPIN;
import vaulsys.security.ssm.base.KeySerialNumber;
import vaulsys.util.MyInteger;

import java.util.HashMap;
import java.util.Map;


/**
 * <p> EracomSecurityModule is an security module interface upon the dedicated Eracom Hardware
 * Security Module (HSM). </p>
 *
 * @author Ali Lahijani
 */
public class EracomSecurityModule extends BaseSMAdapter {

    ESMDriver driver = ESMDriver.getInstance();

    private static final int MAC_LENGTH = 8;
    private static final int MAC_ALGORITHM = AlgorithmQualifier.Retail_MAC_Method;
    private static final int PADDING = Padding.Zero_Padding;

    protected SecureDESKey generateKeyImpl(short keyLength, String keyType) throws SMException {
        return driver.generateKey(keyLength, keyType);
    }

    private static final Map<String, Integer> keyTypes = new HashMap<String, Integer>();

    static {
        keyTypes.put("DPK", 0);
        keyTypes.put("PPK", 1);
        keyTypes.put("MPK", 2);
        keyTypes.put("KTM", 5);
        keyTypes.put("TAK", 6);
    }

    /**
     * mode =0 => ISO -> ECB => format 11
     * mode =1 => ISO -> CBC => format 13
     */
    int desMode = 0;

    protected SecureDESKey importKeyImpl(short keyLength, String keyType, byte[] encryptedKey, SecureDESKey kek, boolean checkParity) throws SMException {
        int kekIndex = 0;

        if (kek instanceof HSMStoredSecureDESKey) {
            HSMStoredSecureDESKey hsmStoredKd1 = (HSMStoredSecureDESKey) kek;
            kekIndex = hsmStoredKd1.getIndex();
        }
        byte[] response = driver.KEY_IMPORT(kekIndex, kek.getBKeyBytes(), desMode, getKeyType(keyType), encryptedKey);
        KeySpecifier keySpec = KeyHandler.getKeySpec(response, 1);

        MyInteger pointer = new MyInteger(1);
        int keyFieldLength = HSMUtil.getLengthOfVarField(response, pointer);
        int checkValueStart = pointer.value + keyFieldLength;
        byte[] checkValue = new byte[]{
                response[checkValueStart],
                response[checkValueStart + 1],
                response[checkValueStart + 2]
        };
        return new SecureDESKey((short) keySpec.length, keyType, keySpec.getByteArray(), checkValue);
    }

    private int getKeyType(String keyType) {
        Integer integer = keyTypes.get(keyType);
        if (integer == null) {
            throw new IllegalArgumentException("Unknown key type: " + keyType);
        }
        return integer;
    }

    protected byte[] exportKeyImpl(SecureDESKey key, SecureDESKey kek) throws SMException {
        return super.exportKeyImpl(key, kek);
    }

//    protected EncryptedPIN encryptPINImpl(String pin, String accountNumber) throws SMException {
//        byte[] pinBlock = driver.CLR_PIN_ENCRYPT(pin, accountNumber, );
//        return new EncryptedPIN(pinBlock, , accountNumber);
//    }

    protected String decryptPINImpl(EncryptedPIN pinUnderLmk) throws SMException {
        return super.decryptPINImpl(pinUnderLmk);
    }

    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        return super.importPINImpl(pinUnderKd1, kd1);
    }

    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        int inputIndex = 0;
        int outputIndex = 0;

        if (kd1 instanceof HSMStoredSecureDESKey) {
            HSMStoredSecureDESKey hsmStoredKd1 = (HSMStoredSecureDESKey) kd1;
            inputIndex = hsmStoredKd1.getIndex();
        }
        if (kd2 instanceof HSMStoredSecureDESKey) {
            HSMStoredSecureDESKey hsmStoredKd2 = (HSMStoredSecureDESKey) kd2;
            outputIndex = hsmStoredKd2.getIndex();
        }
        String accountNumber = pinUnderKd1.getAccountNumber();

        byte[] translatedPinBlock = driver.translatePIN(pinUnderKd1.getPINBlock(), inputIndex,
                kd1.getBKeyBytes(), pinUnderKd1.getPINBlockFormat(), accountNumber,
                destinationPINBlockFormat, outputIndex, kd2.getBKeyBytes(), null);
        return new EncryptedPIN(translatedPinBlock, destinationPINBlockFormat, accountNumber);
    }

    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk) throws SMException {
        return super.importPINImpl(pinUnderDuk, ksn, bdk);
    }

    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        return super.translatePINImpl(pinUnderDuk, ksn, bdk, kd2, destinationPINBlockFormat);
    }

    protected EncryptedPIN exportPINImpl(EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        return super.exportPINImpl(pinUnderLmk, kd2, destinationPINBlockFormat);
    }

    protected byte[] generateCBC_MACImpl(byte[] data, SecureDESKey kd) throws SMException {
        int keyIndex = 0;

        if (kd instanceof HSMStoredSecureDESKey) {
            HSMStoredSecureDESKey hsmStoredKd1 = (HSMStoredSecureDESKey) kd;
            keyIndex = hsmStoredKd1.getIndex();
        }
        return driver.generateMAC(keyIndex, kd.getBKeyBytes(), data, MAC_ALGORITHM, new byte[MAC_LENGTH], MAC_LENGTH, PADDING, null);
    }

}
