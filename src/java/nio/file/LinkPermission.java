/**
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.file;

import java.security.BasicPermission;

public final class LinkPermission extends BasicPermission{
    static final long serialVersionUID=-1441492453772213220L;

    public LinkPermission(String name){
        super(name);
        checkName(name);
    }

    private void checkName(String name){
        if(!name.equals("hard")&&!name.equals("symbolic")){
            throw new IllegalArgumentException("name: "+name);
        }
    }

    public LinkPermission(String name,String actions){
        super(name);
        checkName(name);
        if(actions!=null&&actions.length()>0){
            throw new IllegalArgumentException("actions: "+actions);
        }
    }
}
