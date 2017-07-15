/**
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

public class InvalidClassException extends ObjectStreamException{
    private static final long serialVersionUID=-4333316296251054416L;
    public String classname;

    public InvalidClassException(String reason){
        super(reason);
    }

    public InvalidClassException(String cname,String reason){
        super(reason);
        classname=cname;
    }

    public String getMessage(){
        if(classname==null)
            return super.getMessage();
        else
            return classname+"; "+super.getMessage();
    }
}
