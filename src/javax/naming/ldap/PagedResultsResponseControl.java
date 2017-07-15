/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;

import java.io.IOException;

final public class PagedResultsResponseControl extends BasicControl{
    public static final String OID="1.2.840.113556.1.4.319";
    private static final long serialVersionUID=-8819778744844514666L;
    private int resultSize;
    private byte[] cookie;

    public PagedResultsResponseControl(String id,boolean criticality,
                                       byte[] value) throws IOException{
        super(id,criticality,value);
        // decode value
        BerDecoder ber=new BerDecoder(value,0,value.length);
        ber.parseSeq(null);
        resultSize=ber.parseInt();
        cookie=ber.parseOctetString(Ber.ASN_OCTET_STR,null);
    }

    public int getResultSize(){
        return resultSize;
    }

    public byte[] getCookie(){
        if(cookie.length==0){
            return null;
        }else{
            return cookie;
        }
    }
}
