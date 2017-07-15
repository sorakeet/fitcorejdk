/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

public class MimeHeader{
    private String name;
    private String value;

    public MimeHeader(String name,String value){
        this.name=name;
        this.value=value;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }
}
