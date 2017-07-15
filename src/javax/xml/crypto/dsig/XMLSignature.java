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
 * $Id: XMLSignature.java,v 1.10 2005/05/10 16:03:48 mullan Exp $
 */
/**
 * ===========================================================================
 *
 * (C) Copyright IBM Corp. 2003 All Rights Reserved.
 *
 * ===========================================================================
 */
/**
 * $Id: XMLSignature.java,v 1.10 2005/05/10 16:03:48 mullan Exp $
 */
package javax.xml.crypto.dsig;

import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import java.util.List;

public interface XMLSignature extends XMLStructure{
    final static String XMLNS="http://www.w3.org/2000/09/xmldsig#";

    boolean validate(XMLValidateContext validateContext)
            throws XMLSignatureException;

    KeyInfo getKeyInfo();

    SignedInfo getSignedInfo();

    @SuppressWarnings("rawtypes")
    List getObjects();

    String getId();

    SignatureValue getSignatureValue();

    void sign(XMLSignContext signContext) throws MarshalException,
            XMLSignatureException;

    KeySelectorResult getKeySelectorResult();

    public interface SignatureValue extends XMLStructure{
        String getId();

        byte[] getValue();

        boolean validate(XMLValidateContext validateContext)
                throws XMLSignatureException;
    }
}
