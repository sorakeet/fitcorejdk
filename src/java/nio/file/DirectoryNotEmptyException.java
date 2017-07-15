/**
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

public class DirectoryNotEmptyException
        extends FileSystemException{
    static final long serialVersionUID=3056667871802779003L;

    public DirectoryNotEmptyException(String dir){
        super(dir);
    }
}
