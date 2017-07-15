/**
 * Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: X509Data.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
/**
 * $Id: X509Data.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
package javax.xml.crypto.dsig.keyinfo;

import javax.xml.crypto.XMLStructure;
import java.util.List;

//@@@ check for illegal combinations of data violating MUSTs in W3c spec
public interface X509Data extends XMLStructure{
    final static String TYPE="http://www.w3.org/2000/09/xmldsig#X509Data";
    final static String RAW_X509_CERTIFICATE_TYPE=
            "http://www.w3.org/2000/09/xmldsig#rawX509Certificate";

    @SuppressWarnings("rawtypes")
    List getContent();
}
