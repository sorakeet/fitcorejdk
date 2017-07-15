/**
 * Copyright (c) 2000, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ext.LexicalHandler;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;

public interface TransformerHandler
        extends ContentHandler, LexicalHandler, DTDHandler{
    public void setResult(Result result) throws IllegalArgumentException;

    public String getSystemId();

    public void setSystemId(String systemID);

    public Transformer getTransformer();
}
