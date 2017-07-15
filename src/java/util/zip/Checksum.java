/**
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.zip;

public interface Checksum{
    public void update(int b);

    public void update(byte[] b,int off,int len);

    public long getValue();

    public void reset();
}
