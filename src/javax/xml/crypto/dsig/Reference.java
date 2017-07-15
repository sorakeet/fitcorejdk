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
 * $Id: Reference.java,v 1.9 2005/05/10 16:03:46 mullan Exp $
 */
/**
 * $Id: Reference.java,v 1.9 2005/05/10 16:03:46 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.Data;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLStructure;
import java.io.InputStream;
import java.util.List;

public interface Reference extends URIReference, XMLStructure{
    @SuppressWarnings("rawtypes")
    List getTransforms();

    DigestMethod getDigestMethod();

    String getId();

    byte[] getDigestValue();

    byte[] getCalculatedDigestValue();

    boolean validate(XMLValidateContext validateContext)
            throws XMLSignatureException;

    Data getDereferencedData();

    InputStream getDigestInputStream();
}
