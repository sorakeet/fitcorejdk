/**
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

class DefaultFileSystem{
    public static FileSystem getFileSystem(){
        return new WinNTFileSystem();
    }
}
