/**
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

import java.net.URI;

public interface URIException{
    public static final int URIInaccessible=1;
    public static final int URISchemeNotSupported=2;
    public static final int URIOtherProblem=-1;

    public URI getUnsupportedURI();

    public int getReason();
}
