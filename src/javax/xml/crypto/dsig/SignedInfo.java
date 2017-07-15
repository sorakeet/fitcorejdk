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
 * $Id: SignedInfo.java,v 1.7 2005/05/10 16:03:47 mullan Exp $
 */
/**
 * $Id: SignedInfo.java,v 1.7 2005/05/10 16:03:47 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.XMLStructure;
import java.io.InputStream;
import java.util.List;

public interface SignedInfo extends XMLStructure{
    CanonicalizationMethod getCanonicalizationMethod();

    SignatureMethod getSignatureMethod();

    @SuppressWarnings("rawtypes")
    List getReferences();

    String getId();

    InputStream getCanonicalizedData();
}
