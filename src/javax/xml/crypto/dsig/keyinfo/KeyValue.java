/**
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * $Id: KeyValue.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
/**
 * $Id: KeyValue.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
package javax.xml.crypto.dsig.keyinfo;

import javax.xml.crypto.XMLStructure;
import java.security.KeyException;
import java.security.PublicKey;

public interface KeyValue extends XMLStructure{
    final static String DSA_TYPE=
            "http://www.w3.org/2000/09/xmldsig#DSAKeyValue";
    final static String RSA_TYPE=
            "http://www.w3.org/2000/09/xmldsig#RSAKeyValue";

    PublicKey getPublicKey() throws KeyException;
}
