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
 * $Id: SignatureProperty.java,v 1.4 2005/05/10 16:03:46 mullan Exp $
 */
/**
 * $Id: SignatureProperty.java,v 1.4 2005/05/10 16:03:46 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.XMLStructure;
import java.util.List;

public interface SignatureProperty extends XMLStructure{
    String getTarget();

    String getId();

    @SuppressWarnings("rawtypes")
    List getContent();
}
