/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform;

public interface ErrorListener{
    public abstract void warning(TransformerException exception)
            throws TransformerException;

    public abstract void error(TransformerException exception)
            throws TransformerException;

    public abstract void fatalError(TransformerException exception)
            throws TransformerException;
}
