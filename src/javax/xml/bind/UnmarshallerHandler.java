/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import org.xml.sax.ContentHandler;

public interface UnmarshallerHandler extends ContentHandler{
    Object getResult() throws JAXBException, IllegalStateException;
}
