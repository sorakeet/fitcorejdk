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
 * $Id: CanonicalizationMethod.java,v 1.6 2005/05/10 16:03:45 mullan Exp $
 */
/**
 * $Id: CanonicalizationMethod.java,v 1.6 2005/05/10 16:03:45 mullan Exp $
 */
package javax.xml.crypto.dsig;

import java.security.spec.AlgorithmParameterSpec;

public interface CanonicalizationMethod extends Transform{
    final static String INCLUSIVE=
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    final static String INCLUSIVE_WITH_COMMENTS=
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    final static String EXCLUSIVE=
            "http://www.w3.org/2001/10/xml-exc-c14n#";
    final static String EXCLUSIVE_WITH_COMMENTS=
            "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";

    AlgorithmParameterSpec getParameterSpec();
}
