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
 * $Id: RetrievalMethod.java,v 1.8 2005/05/10 16:35:35 mullan Exp $
 */
/**
 * $Id: RetrievalMethod.java,v 1.8 2005/05/10 16:35:35 mullan Exp $
 */
package javax.xml.crypto.dsig.keyinfo;

import javax.xml.crypto.*;
import java.util.List;

public interface RetrievalMethod extends URIReference, XMLStructure{
    @SuppressWarnings("rawtypes")
    List getTransforms();

    String getURI();

    Data dereference(XMLCryptoContext context) throws URIReferenceException;
}
