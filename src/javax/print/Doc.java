/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import javax.print.attribute.DocAttributeSet;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public interface Doc{
    public DocFlavor getDocFlavor();

    public Object getPrintData() throws IOException;

    public DocAttributeSet getAttributes();

    public Reader getReaderForText() throws IOException;

    public InputStream getStreamForBytes() throws IOException;
}
