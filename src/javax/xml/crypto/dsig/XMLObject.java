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
 * ===========================================================================
 * <p>
 * (C) Copyright IBM Corp. 2003 All Rights Reserved.
 * <p>
 * ===========================================================================
 * <p>
 * $Id: XMLObject.java,v 1.5 2005/05/10 16:03:48 mullan Exp $
 */
/**
 * ===========================================================================
 *
 * (C) Copyright IBM Corp. 2003 All Rights Reserved.
 *
 * ===========================================================================
 */
/**
 * $Id: XMLObject.java,v 1.5 2005/05/10 16:03:48 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.XMLStructure;
import java.util.List;

public interface XMLObject extends XMLStructure{
    final static String TYPE="http://www.w3.org/2000/09/xmldsig#Object";

    @SuppressWarnings("rawtypes")
    List getContent();

    String getId();

    String getMimeType();

    String getEncoding();
}
