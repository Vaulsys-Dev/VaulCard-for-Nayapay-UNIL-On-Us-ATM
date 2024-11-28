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

package vaulsys.security.ssm.base;

import vaulsys.security.exception.SMException;
import vaulsys.security.securekey.SecureDESKey;
import javassist.NotFoundException;

import org.apache.log4j.Logger;

/**
 * <p>
 * Provides base functionality for the actual Security Module Adapter.
 * </p>
 * <p>
 * You adapter needs to override the methods that end with "Impl"
 * </p>
 *
 * @author Hani S. Kirollos
 * @version $Revision: 1.1 $ $Date: 2007/02/27 12:46:14 $
 */
public class BaseSMAdapter implements SMAdapter {
    protected Logger logger = Logger.getLogger(BaseSMAdapter.class);
    protected String realm = null;
    private String name;

    public BaseSMAdapter() {
        super();
    }

    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getRealm() {
        return realm;
    }

    /**
     * associates this SMAdapter with a name using NameRegistrar
     *
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName(String name) {
        this.name = name;
        // NameRegistrar.register("s-m-adapter." + name, this);
    }

    /**
     * @return this SMAdapter's name ("" if no name was set)
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     * @return SMAdapter instance with given name.
     * @throws NotFoundException
     * @see NameRegistrar
     */
    public static SMAdapter getSMAdapter(String name) // throws NameRegistrar.NotFoundException
    {
        return null;
        // return (SMAdapter)NameRegistrar.get("s-m-adapter." + name);
    }

    public SecureDESKey generateKey(short keyLength, String keyType) throws SMException {
        SecureDESKey result = null;
        try {
            result = generateKeyImpl(keyLength, keyType);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public SecureDESKey importKey(short keyLength, String keyType, byte[] encryptedKey, SecureDESKey kek, boolean checkParity) throws SMException {
        SecureDESKey result = null;
        try {
            result = importKeyImpl(keyLength, keyType, encryptedKey, kek, checkParity);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public byte[] exportKey(SecureDESKey key, SecureDESKey kek) throws SMException {
        byte[] result = null;
        try {
            result = exportKeyImpl(key, kek);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public EncryptedPIN encryptPIN(String pin, String accountNumber) throws SMException {
        accountNumber = EncryptedPIN.extractAccountNumberPart(accountNumber);
        EncryptedPIN result = null;
        try {
            result = encryptPINImpl(pin, accountNumber);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public String decryptPIN(EncryptedPIN pinUnderLmk) throws SMException {
        String result = null;
        try {
            result = decryptPINImpl(pinUnderLmk);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    @Override
    public EncryptedPIN encryptPINByKey(String pin, String accountNumber, byte blockFormat, SecureDESKey toKey) throws SMException {
        accountNumber = EncryptedPIN.extractAccountNumberPart(accountNumber);
        EncryptedPIN result = null;
        try {
            result = encryptPINByKeyImpl(pin, accountNumber, blockFormat, toKey);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    @Override
    public String decryptPINByKey(EncryptedPIN pinUnderLmk, SecureDESKey inKey) throws SMException {
        String result = null;
        try {
            result = decryptPINByKeyImpl(pinUnderLmk, inKey);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public EncryptedPIN importPIN(EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderKd1, kd1);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public EncryptedPIN translatePIN(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderKd1, kd1, kd2, destinationPINBlockFormat);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e.getMessage(), e);
        }
        return result;
    }

    public EncryptedPIN importPIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk) throws SMException {
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderDuk, ksn, bdk);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public EncryptedPIN translatePIN(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat)
            throws SMException {
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderDuk, ksn, bdk, kd2, destinationPINBlockFormat);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public EncryptedPIN exportPIN(EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        EncryptedPIN result = null;
        try {
            result = exportPINImpl(pinUnderLmk, kd2, destinationPINBlockFormat);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

    public byte[] generateCBC_MAC(byte[] data, SecureDESKey kd) throws SMException {
        byte[] result = null;
        try {
            result = generateCBC_MACImpl(data, kd);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
    }

	@Override
	public byte[] decrypt(byte[] inputBlock, SecureDESKey fromKey) throws SMException {
        byte[] result = null;
        try {
            result = decryptImpl(inputBlock, fromKey);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
	}

	
	@Override
	public byte[] encrypt(byte[] input, SecureDESKey toKey, String padding) throws SMException {
        byte[] result = null;
        try {
            result = encryptImpl(input, toKey, padding);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
	}

	@Override
	public byte[] rsaDecrypt(byte[] cipherData) throws SMException {
        byte[] result = null;
        try {
            result = rsaDecryptImpl(cipherData);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        }
        return result;
	}

	
	/**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param keyLength
     * @param keyType
     * @return generated key
     * @throws SMException
     */
    protected SecureDESKey generateKeyImpl(short keyLength, String keyType) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param keyLength
     * @param keyType
     * @param encryptedKey
     * @param kek
     * @return imported key
     * @throws SMException
     */
    protected SecureDESKey importKeyImpl(short keyLength, String keyType, byte[] encryptedKey, SecureDESKey kek, boolean checkParity)
            throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param key
     * @param kek
     * @return exported key
     * @throws SMException
     */
    protected byte[] exportKeyImpl(SecureDESKey key, SecureDESKey kek) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pin
     * @param accountNumber
     * @return encrypted PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN encryptPINImpl(String pin, String accountNumber) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderLmk
     * @return clear pin as entered by card holder
     * @throws SMException
     */
    protected String decryptPINImpl(EncryptedPIN pinUnderLmk) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderKd1
     * @param kd1
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderKd1
     * @param kd1
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey kd2, byte destinationPINBlockFormat)
            throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }
    
    //TASK Task015 : HotCard
    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderKd1
     * @param kd1
     * @param kd2
     * @param destinationPINBlockFormat
     * @param secondKey
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey kd2, byte destinationPINBlockFormat,String secondKey)
            throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }    

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl(EncryptedPIN pinUnderDuk, KeySerialNumber ksn, SecureDESKey bdk, SecureDESKey kd2,
                                            byte destinationPINBlockFormat) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param pinUnderLmk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return exported pin
     * @throws SMException
     */
    protected EncryptedPIN exportPINImpl(EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     *
     * @param data
     * @param kd
     * @return generated CBC-MAC
     * @throws SMException
     */
	protected byte[] generateCBC_MACImpl(byte[] data, SecureDESKey kd) throws SMException {
		throw new SMException("Operation not supported in: " + this.getClass().getName());
	}

	protected byte[] decryptImpl(byte[] inputBlock, SecureDESKey fromKey) throws SMException {
		throw new SMException("Operation not supported in: " + this.getClass().getName());
	}

	protected byte[] encryptImpl(byte[] input, SecureDESKey toKey, String padding) throws SMException {
		throw new SMException("Operation not supported in: " + this.getClass().getName());
	}


public byte[] desDecrypt(byte[] inputBlock, byte[] desKey) throws SMException {
		throw new SMException("Operation not supported in: " + this.getClass().getName());
	}
	
	public byte[] desEncrypt(byte[] input, byte[] desKey) throws SMException {
		throw new SMException("Operation not supported in: " + this.getClass().getName());
	}

    protected EncryptedPIN encryptPINByKeyImpl(String pin, String accountNumber, byte blockFormat, SecureDESKey toKey) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }

    protected String decryptPINByKeyImpl(EncryptedPIN pinUnderLmk, SecureDESKey inKey) throws SMException {
        throw new SMException("Operation not supported in: " + this.getClass().getName());
    }
    
	protected byte[] rsaDecryptImpl(byte[] cipherData) throws SMException {
		throw new SMException("Operation not supported in: " + this.getClass().getName());
	}

	//TASK Task015 : HotCard
	@Override
	public EncryptedPIN translatePIN(EncryptedPIN pinUnderKd1,
			SecureDESKey kd1, SecureDESKey kd2, byte destinationPINBlockFormat,
			String secondKey) throws SMException {
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderKd1, kd1, kd2, destinationPINBlockFormat,secondKey);
        } catch (Exception e) {
            throw e instanceof SMException ? (SMException) e : new SMException(e.getMessage(), e);
        }
        return result;
    }
}
