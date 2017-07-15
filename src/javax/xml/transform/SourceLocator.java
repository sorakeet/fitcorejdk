/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.transform;

public interface SourceLocator{
    public String getPublicId();

    public String getSystemId();

    public int getLineNumber();

    public int getColumnNumber();
}
