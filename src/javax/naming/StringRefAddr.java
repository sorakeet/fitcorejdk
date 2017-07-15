/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class StringRefAddr extends RefAddr{
    private static final long serialVersionUID=-8913762495138505527L;
    private String contents;

    public StringRefAddr(String addrType,String addr){
        super(addrType);
        contents=addr;
    }

    public Object getContent(){
        return contents;
    }
}
