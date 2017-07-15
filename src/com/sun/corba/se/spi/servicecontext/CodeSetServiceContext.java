/**
 * Copyright (c) 1999, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.servicecontext;

import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.se.impl.encoding.MarshalInputStream;
import com.sun.corba.se.impl.encoding.MarshalOutputStream;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

public class CodeSetServiceContext extends ServiceContext{
    // Required SERVICE_CONTEXT_ID and getId definitions
    public static final int SERVICE_CONTEXT_ID=1;
    private CodeSetComponentInfo.CodeSetContext csc;

    public CodeSetServiceContext(CodeSetComponentInfo.CodeSetContext csc){
        this.csc=csc;
    }

    public CodeSetServiceContext(InputStream is,GIOPVersion gv){
        super(is,gv);
        csc=new CodeSetComponentInfo.CodeSetContext();
        csc.read((MarshalInputStream)in);
    }

    public int getId(){
        return SERVICE_CONTEXT_ID;
    }

    public void writeData(OutputStream os) throws SystemException{
        csc.write((MarshalOutputStream)os);
    }

    public String toString(){
        return "CodeSetServiceContext[ csc="+csc+" ]";
    }

    public CodeSetComponentInfo.CodeSetContext getCodeSetContext(){
        return csc;
    }
}