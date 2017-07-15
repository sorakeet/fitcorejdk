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
 * $Id: KeyInfo.java,v 1.7 2005/05/10 16:35:34 mullan Exp $
 */
/**
 * $Id: KeyInfo.java,v 1.7 2005/05/10 16:35:34 mullan Exp $
 */
package javax.xml.crypto.dsig.keyinfo;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import java.util.List;

public interface KeyInfo extends XMLStructure{
    @SuppressWarnings("rawtypes")
    List getContent();

    String getId();

    void marshal(XMLStructure parent,XMLCryptoContext context)
            throws MarshalException;
}
