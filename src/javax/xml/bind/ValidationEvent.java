/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

public interface ValidationEvent{
    public static final int WARNING=0;
    public static final int ERROR=1;
    public static final int FATAL_ERROR=2;

    public int getSeverity();

    public String getMessage();

    public Throwable getLinkedException();

    public ValidationEventLocator getLocator();
}
