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
 * $Id: AlgorithmMethod.java,v 1.4 2005/05/10 15:47:41 mullan Exp $
 */
/**
 * $Id: AlgorithmMethod.java,v 1.4 2005/05/10 15:47:41 mullan Exp $
 */
package javax.xml.crypto;

import java.security.spec.AlgorithmParameterSpec;

public interface AlgorithmMethod{
    String getAlgorithm();

    AlgorithmParameterSpec getParameterSpec();
}
