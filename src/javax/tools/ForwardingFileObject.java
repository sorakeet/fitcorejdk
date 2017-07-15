/**
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import java.io.*;
import java.net.URI;

public class ForwardingFileObject<F extends FileObject> implements FileObject{
    protected final F fileObject;

    protected ForwardingFileObject(F fileObject){
        fileObject.getClass(); // null check
        this.fileObject=fileObject;
    }

    public URI toUri(){
        return fileObject.toUri();
    }

    public String getName(){
        return fileObject.getName();
    }

    public InputStream openInputStream() throws IOException{
        return fileObject.openInputStream();
    }

    public OutputStream openOutputStream() throws IOException{
        return fileObject.openOutputStream();
    }

    public Reader openReader(boolean ignoreEncodingErrors) throws IOException{
        return fileObject.openReader(ignoreEncodingErrors);
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException{
        return fileObject.getCharContent(ignoreEncodingErrors);
    }

    public Writer openWriter() throws IOException{
        return fileObject.openWriter();
    }

    public long getLastModified(){
        return fileObject.getLastModified();
    }

    public boolean delete(){
        return fileObject.delete();
    }
}
