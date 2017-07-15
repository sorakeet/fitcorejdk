/**
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.LdapCtx;

import javax.naming.NamingException;
import java.io.IOException;

final public class SortResponseControl extends BasicControl{
    public static final String OID="1.2.840.113556.1.4.474";
    private static final long serialVersionUID=5142939176006310877L;
    private int resultCode=0;
    private String badAttrId=null;

    public SortResponseControl(String id,boolean criticality,byte[] value)
            throws IOException{
        super(id,criticality,value);
        // decode value
        BerDecoder ber=new BerDecoder(value,0,value.length);
        ber.parseSeq(null);
        resultCode=ber.parseEnumeration();
        if((ber.bytesLeft()>0)&&(ber.peekByte()==Ber.ASN_CONTEXT)){
            badAttrId=ber.parseStringWithTag(Ber.ASN_CONTEXT,true,null);
        }
    }

    public boolean isSorted(){
        return (resultCode==0); // a result code of zero indicates success
    }

    public int getResultCode(){
        return resultCode;
    }

    public String getAttributeID(){
        return badAttrId;
    }

    public NamingException getException(){
        return LdapCtx.mapErrorCode(resultCode,null);
    }
}
