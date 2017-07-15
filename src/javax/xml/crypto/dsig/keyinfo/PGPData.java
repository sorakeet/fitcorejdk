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
 * $Id: PGPData.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
/**
 * $Id: PGPData.java,v 1.4 2005/05/10 16:35:35 mullan Exp $
 */
package javax.xml.crypto.dsig.keyinfo;

import javax.xml.crypto.XMLStructure;
import java.util.List;

public interface PGPData extends XMLStructure{
    final static String TYPE="http://www.w3.org/2000/09/xmldsig#PGPData";

    byte[] getKeyId();

    byte[] getKeyPacket();

    @SuppressWarnings("rawtypes")
    List getExternalElements();
}
