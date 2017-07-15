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
 * ===========================================================================
 * <p>
 * (C) Copyright IBM Corp. 2003 All Rights Reserved.
 * <p>
 * ===========================================================================
 * <p>
 * $Id: URIDereferencer.java,v 1.5 2005/05/10 15:47:42 mullan Exp $
 */
/**
 * ===========================================================================
 *
 * (C) Copyright IBM Corp. 2003 All Rights Reserved.
 *
 * ===========================================================================
 */
/**
 * $Id: URIDereferencer.java,v 1.5 2005/05/10 15:47:42 mullan Exp $
 */
package javax.xml.crypto;

public interface URIDereferencer{
    Data dereference(URIReference uriReference,XMLCryptoContext context)
            throws URIReferenceException;
}
