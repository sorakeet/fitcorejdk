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
 * $Id: SignatureMethod.java,v 1.5 2005/05/10 16:03:46 mullan Exp $
 */
/**
 * $Id: SignatureMethod.java,v 1.5 2005/05/10 16:03:46 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.XMLStructure;
import java.security.spec.AlgorithmParameterSpec;

public interface SignatureMethod extends XMLStructure, AlgorithmMethod{
    static final String DSA_SHA1=
            "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
    static final String RSA_SHA1=
            "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    static final String HMAC_SHA1=
            "http://www.w3.org/2000/09/xmldsig#hmac-sha1";

    AlgorithmParameterSpec getParameterSpec();
}
