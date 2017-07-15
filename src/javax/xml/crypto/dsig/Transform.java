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
 * $Id: Transform.java,v 1.5 2005/05/10 16:03:48 mullan Exp $
 */
/**
 * $Id: Transform.java,v 1.5 2005/05/10 16:03:48 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.Data;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import java.io.OutputStream;
import java.security.spec.AlgorithmParameterSpec;

public interface Transform extends XMLStructure, AlgorithmMethod{
    final static String BASE64="http://www.w3.org/2000/09/xmldsig#base64";
    final static String ENVELOPED=
            "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
    final static String XPATH="http://www.w3.org/TR/1999/REC-xpath-19991116";
    final static String XPATH2="http://www.w3.org/2002/06/xmldsig-filter2";
    final static String XSLT="http://www.w3.org/TR/1999/REC-xslt-19991116";

    AlgorithmParameterSpec getParameterSpec();

    public abstract Data transform(Data data,XMLCryptoContext context)
            throws TransformException;

    public abstract Data transform
            (Data data,XMLCryptoContext context,OutputStream os)
            throws TransformException;
}
