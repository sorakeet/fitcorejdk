/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface DirectoryStream<T>
        extends Closeable, Iterable<T>{
    @Override
    Iterator<T> iterator();

    @FunctionalInterface
    public static interface Filter<T>{
        boolean accept(T entry) throws IOException;
    }
}
