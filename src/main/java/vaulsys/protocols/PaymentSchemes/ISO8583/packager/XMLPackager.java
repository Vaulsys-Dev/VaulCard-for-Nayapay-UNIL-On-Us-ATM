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

package vaulsys.protocols.PaymentSchemes.ISO8583.packager;

import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOComponent;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOPackager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * packs/unpacks ISOMsgs into XML representation
 *
 * @author apr@cs.com.uy
 * @version $Id: XMLPackager.java,v 1.1 2007/02/27 12:46:12 omid Exp $
 * @see ISOPackager
 */
public class XMLPackager implements ISOPackager {
    protected transient Logger logger = Logger.getLogger(XMLPackager.class);
    protected String realm = null;
    private ByteArrayOutputStream out;
    private PrintStream p;
//    private XMLReader reader = null;
//    private Stack<ISOMsg> stk;

    public static final String ISOMSG_TAG = "isomsg";
    public static final String ISOFIELD_TAG = "field";
    public static final String ID_ATTR = "id";
    public static final String VALUE_ATTR = "value";
    public static final String TYPE_ATTR = "type";
    public static final String TYPE_BINARY = "binary";
    public static final String TYPE_BITMAP = "bitmap";
    public static final String HEADER_TAG = "header";

    public XMLPackager() throws ISOException {
        super();
        out = new ByteArrayOutputStream();
        p = new PrintStream(out);
//        stk = new Stack<ISOMsg>();
//        try {
//            reader = createXMLReader();
//        } catch (Exception e) {
//            throw new ISOException(e.toString(), e);
//        }
    }

    public byte[] pack(ISOComponent c) throws ISOException {
        try {
            if (!(c instanceof ISOMsg))
                throw new ISOException("cannot pack " + c.getClass());
            ISOMsg m = (ISOMsg) c;
            byte[] b;
            synchronized (this) {
                m.setDirection(0);  // avoid "direction=xxxxxx" in XML msg
                m.dump(p, "");
                b = out.toByteArray();
                out.reset();
            }
//            if (logger != null)
//                logger.debug(m);
            return b;
        } catch (ISOException e) {
            logger.error("Exception", e);
            throw e;
        } finally {
        }
    }

    public synchronized int unpack(ISOComponent c, byte[] b)
            throws ISOException {
    	return 0;
//        try {
//            if (!(c instanceof ISOMsg))
//                throw new ISOException
//                        ("Can't call packager on non Composite");
//
//            while (!stk.empty())    // purge from possible previous error
//                stk.pop();
//
//            InputSource src = new InputSource(new ByteArrayInputStream(b));
//            reader.parse(src);
//            if (stk.empty())
//                throw new ISOException("error parsing");
//
//            ISOMsg m = (ISOMsg) c;
//            ISOMsg m1 = stk.pop();
//            m.merge(m1);
//
////            if (logger != null)
////                logger.debug(m);
//            return b.length;
//        } catch (ISOException e) {
//            logger.error("Exception", e);
//            throw e;
//        } catch (IOException e) {
//            logger.error("Exception", e);
//            throw new ISOException(e.toString(), e);
//        } catch (SAXException e) {
//            logger.error("Exception", e);
//            throw new ISOException(e.toString(), e);
//        } finally {
//        }
    }

    public synchronized void unpack(ISOComponent c, InputStream in)
            throws ISOException, IOException {
//        try {
//            if (!(c instanceof ISOMsg))
//                throw new ISOException
//                        ("Can't call packager on non Composite");
//
//            while (!stk.empty())    // purge from possible previous error
//                stk.pop();
//
//            reader.parse(new InputSource(in));
//            if (stk.empty())
//                throw new ISOException("error parsing");
//
//            ISOMsg m = (ISOMsg) c;
//            m.merge(stk.pop());
//
////            if (logger != null)
////                logger.debug(m);
//        } catch (ISOException e) {
//            logger.error("Exception", e);
//            throw e;
//        } catch (SAXException e) {
//            logger.error("Exception", e);
//            throw new ISOException(e.toString(), e);
//        } finally {
//        }
    }

//    public void startElement
//            (String ns, String name, String qName, Attributes atts)
//            throws SAXException {
//        int fieldNumber = -1;
//        try {
//            String id = atts.getValue(ID_ATTR);
//            if (id != null) {
//                try {
//                    fieldNumber = Integer.parseInt(id);
//                } catch (NumberFormatException ex) {/*ignore*/}
//            }
//            if (name.equals(ISOMSG_TAG)) {
//                if (fieldNumber >= 0) {
//                    if (stk.empty())
//                        throw new SAXException("inner without outter");
//
//                    ISOMsg inner = new ISOMsg(fieldNumber);
//                    (stk.peek()).set(inner);
//                    stk.push(inner);
//                } else {
//                    stk.push(new ISOMsg(0));
//                }
//            } else if (name.equals(ISOFIELD_TAG)) {
//                ISOMsg m = stk.peek();
//                String value = atts.getValue(VALUE_ATTR);
//                String type = atts.getValue(TYPE_ATTR);
//                if (id == null || value == null)
//                    throw new SAXException("invalid field");
//                if (TYPE_BINARY.equals(type)) {
//                    m.set(new ISOBinaryField(
//                            fieldNumber,
//                            ISOUtil.hex2byte(
//                                    value.getBytes(), 0, value.length() / 2
//                            )
//                    )
//                    );
//                } else {
//                    m.set(new ISOField(fieldNumber, value));
//                }
//
//            } else if (HEADER_TAG.equals(name)) {
////                stk.push (new BaseHeader());
//            }
//        } catch (ISOException e) {
//            throw new SAXException
//                    ("ISOException unpacking " + fieldNumber, e);
//        }
//    }
//
//    public void characters(char ch[], int start, int length) {
//    }
//
//    public void endElement(String ns, String name, String qname)
//            throws SAXException {
//        if (name.equals(ISOMSG_TAG)) {
//            ISOMsg m = stk.pop();
//            if (stk.empty())
//                stk.push(m); // push outter message
//        } else if (HEADER_TAG.equals(name)) {
//        }
//    }
//
    public String getFieldDescription(ISOComponent m, int fldNumber) {
        return "<notavailable/>";
    }

    public void setLogger(Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

    public Logger getLogger() {
        return logger;
    }

//    private XMLReader createXMLReader() throws SAXException {
//        XMLReader reader;
//        try {
//            reader = XMLReaderFactory.createXMLReader();
//        } catch (SAXException e) {
//            reader = XMLReaderFactory.createXMLReader(
//                    System.getProperty(
//                            "org.xml.sax.driver",
//                            "org.apache.crimson.parser.XMLReaderImpl"
//                    )
//            );
//        }
//        reader.setFeature("http://xml.org/sax/features/validation", false);
//        reader.setContentHandler(this);
//        reader.setErrorHandler(this);
//        return reader;
//    }
}

