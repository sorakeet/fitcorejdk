/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import java.io.IOException;

public interface MultiDoc{
    public Doc getDoc() throws IOException;

    public MultiDoc next() throws IOException;
}
