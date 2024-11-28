package vaulsys.security.securekey;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOUtil;
import vaulsys.security.ssm.base.SMAdapter;
import vaulsys.terminal.impl.Terminal;

import java.io.PrintStream;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

@Entity
public class RSAPublicKey extends SecureKey {

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "terminal", nullable = true, updatable = true)
//	@ForeignKey(name="secdeskey_terminal_fk")
//    private Terminal terminal;

    public RSAPublicKey() {
    	super();
	}
    
    /**
     * Constructs an RSAPublicKey
     *
     * @param keyLength     e.g. LENGTH_RSA_PUBLIC_1024, LENGTH_RSA_PUBLIC_2048, LENGTH_RSA_PUBLIC_4096
     * @param keyType
     * @param keyBytes      Public key
     * @see SMAdapter
     */
    public RSAPublicKey(short keyLength, String keyType, byte[] keyBytes) {
        setKeyLength(keyLength);
        setKeyType(keyType);
        setBKeyBytes(keyBytes);
    }

    /**
     * Constructs an RSAPublicKey
     *
     * @param keyLength
     * @param keyType
     * @param keyHexString           secure key represented as HexString instead of byte[]
     */
    public RSAPublicKey(short keyLength, String keyType, String keyHexString) {
        this(keyLength, keyType, ISOUtil.hex2byte(keyHexString));
    }


    /**
     * dumps RSAPublicKey basic information
     *
     * @param p      a PrintStream usually supplied by Logger
     * @param indent indention string, usually suppiled by Logger
     */
    public void dump(PrintStream p, String indent) {
        String inner = indent + "  ";
        p.print(indent + "<rsa-public-key");
        p.print(" length=\"" + getKeyLength() + "\"");
        p.print(" type=\"" + keyType + "\"");
        p.println(">");
        p.println(inner + "<data>" + getKeyBytes() + "</data>");
        p.println(indent + "</rsa-public-key>");
    }    

//	public Terminal getTerminal() {
//		return terminal;
//	}
//
//	public void setTerminal(Terminal terminal) {
//		this.terminal = terminal;
//	}
}
