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
 * $Id: X509IssuerSerial.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
/**
 * $Id: X509IssuerSerial.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
package javax.xml.crypto.dsig.keyinfo;

import javax.xml.crypto.XMLStructure;
import java.math.BigInteger;

public interface X509IssuerSerial extends XMLStructure{
    String getIssuerName();

    BigInteger getSerialNumber();
}
