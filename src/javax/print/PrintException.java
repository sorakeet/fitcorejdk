/**
 * Copyright (c) 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print;

public class PrintException extends Exception{
    public PrintException(){
        super();
    }

    public PrintException(String s){
        super(s);
    }

    public PrintException(Exception e){
        super(e);
    }

    public PrintException(String s,Exception e){
        super(s,e);
    }
}
