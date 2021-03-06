/**
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

public class NoSuchFileException
        extends FileSystemException{
    static final long serialVersionUID=-1390291775875351931L;

    public NoSuchFileException(String file){
        super(file);
    }

    public NoSuchFileException(String file,String other,String reason){
        super(file,other,reason);
    }
}
