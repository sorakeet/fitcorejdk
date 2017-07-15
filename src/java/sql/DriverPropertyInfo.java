/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.sql;

public class DriverPropertyInfo{
    public String name;
    public String description=null;
    public boolean required=false;
    public String value=null;
    public String[] choices=null;

    public DriverPropertyInfo(String name,String value){
        this.name=name;
        this.value=value;
    }
}
