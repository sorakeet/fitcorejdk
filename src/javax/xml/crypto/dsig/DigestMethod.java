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
 * $Id: DigestMethod.java,v 1.6 2005/05/10 16:03:46 mullan Exp $
 */
/**
 * $Id: DigestMethod.java,v 1.6 2005/05/10 16:03:46 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.XMLStructure;
import java.security.spec.AlgorithmParameterSpec;

public interface DigestMethod extends XMLStructure, AlgorithmMethod{
    static final String SHA1="http://www.w3.org/2000/09/xmldsig#sha1";
    static final String SHA256="http://www.w3.org/2001/04/xmlenc#sha256";
    static final String SHA512="http://www.w3.org/2001/04/xmlenc#sha512";
    static final String RIPEMD160="http://www.w3.org/2001/04/xmlenc#ripemd160";

    AlgorithmParameterSpec getParameterSpec();
}
