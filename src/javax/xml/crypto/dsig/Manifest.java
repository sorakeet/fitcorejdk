/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * $Id: Manifest.java,v 1.7 2005/05/10 16:03:46 mullan Exp $
 */
/**
 * $Id: Manifest.java,v 1.7 2005/05/10 16:03:46 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.XMLStructure;
import java.util.List;

public interface Manifest extends XMLStructure{
    final static String TYPE="http://www.w3.org/2000/09/xmldsig#Manifest";

    String getId();

    @SuppressWarnings("rawtypes")
    List getReferences();
}
