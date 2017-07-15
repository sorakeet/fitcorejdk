/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.security.BasicPermission;

public class MBeanTrustPermission extends BasicPermission{
    private static final long serialVersionUID=-2952178077029018140L;

    public MBeanTrustPermission(String name){
        this(name,null);
    }

    public MBeanTrustPermission(String name,String actions){
        super(name,actions);
        validate(name,actions);
    }

    private static void validate(String name,String actions){
        /** Check that actions is a null empty string */
        if(actions!=null&&actions.length()>0){
            throw new IllegalArgumentException("MBeanTrustPermission actions must be null: "+
                    actions);
        }
        if(!name.equals("register")&&!name.equals("*")){
            throw new IllegalArgumentException("MBeanTrustPermission: Unknown target name "+
                    "["+name+"]");
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        // Reading private fields of base class
        in.defaultReadObject();
        try{
            validate(super.getName(),super.getActions());
        }catch(IllegalArgumentException e){
            throw new InvalidObjectException(e.getMessage());
        }
    }
}
