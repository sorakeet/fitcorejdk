/**
 * Copyright (c) 2007, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

import java.io.IOException;

public class FileSystemException
        extends IOException{
    static final long serialVersionUID=-3055425747967319812L;
    private final String file;
    private final String other;

    public FileSystemException(String file){
        super((String)null);
        this.file=file;
        this.other=null;
    }

    public FileSystemException(String file,String other,String reason){
        super(reason);
        this.file=file;
        this.other=other;
    }

    public String getFile(){
        return file;
    }

    public String getOtherFile(){
        return other;
    }

    @Override
    public String getMessage(){
        if(file==null&&other==null)
            return getReason();
        StringBuilder sb=new StringBuilder();
        if(file!=null)
            sb.append(file);
        if(other!=null){
            sb.append(" -> ");
            sb.append(other);
        }
        if(getReason()!=null){
            sb.append(": ");
            sb.append(getReason());
        }
        return sb.toString();
    }

    public String getReason(){
        return super.getMessage();
    }
}
