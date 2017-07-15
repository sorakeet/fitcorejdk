/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerEncoder;

import java.io.IOException;

final public class PagedResultsControl extends BasicControl{
    public static final String OID="1.2.840.113556.1.4.319";
    private static final byte[] EMPTY_COOKIE=new byte[0];
    private static final long serialVersionUID=6684806685736844298L;

    public PagedResultsControl(int pageSize,boolean criticality)
            throws IOException{
        super(OID,criticality,null);
        value=setEncodedValue(pageSize,EMPTY_COOKIE);
    }

    private byte[] setEncodedValue(int pageSize,byte[] cookie)
            throws IOException{
        // build the ASN.1 encoding
        BerEncoder ber=new BerEncoder(10+cookie.length);
        ber.beginSeq(Ber.ASN_SEQUENCE|Ber.ASN_CONSTRUCTOR);
        ber.encodeInt(pageSize);
        ber.encodeOctetString(cookie,Ber.ASN_OCTET_STR);
        ber.endSeq();
        return ber.getTrimmedBuf();
    }

    public PagedResultsControl(int pageSize,byte[] cookie,
                               boolean criticality) throws IOException{
        super(OID,criticality,null);
        if(cookie==null){
            cookie=EMPTY_COOKIE;
        }
        value=setEncodedValue(pageSize,cookie);
    }
}
