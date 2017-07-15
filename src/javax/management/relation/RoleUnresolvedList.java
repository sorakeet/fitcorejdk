/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.relation;

import com.sun.jmx.mbeanserver.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoleUnresolvedList extends ArrayList<Object>{
    private static final long serialVersionUID=4054902803091433324L;
    private transient boolean typeSafe;
    private transient boolean tainted;
    //
    // Constructors
    //

    public RoleUnresolvedList(){
        super();
    }

    public RoleUnresolvedList(int initialCapacity){
        super(initialCapacity);
    }

    public RoleUnresolvedList(List<RoleUnresolved> list)
            throws IllegalArgumentException{
        // Check for null parameter
        //
        if(list==null)
            throw new IllegalArgumentException("Null parameter");
        // Check for non-RoleUnresolved objects
        //
        checkTypeSafe(list);
        // Build the List<RoleUnresolved>
        //
        super.addAll(list);
    }

    private static void checkTypeSafe(Collection<?> c){
        try{
            RoleUnresolved r;
            for(Object o : c)
                r=(RoleUnresolved)o;
        }catch(ClassCastException e){
            throw new IllegalArgumentException(e);
        }
    }
    //
    // Accessors
    //

    @SuppressWarnings("unchecked")
    public List<RoleUnresolved> asList(){
        if(!typeSafe){
            if(tainted)
                checkTypeSafe(this);
            typeSafe=true;
        }
        return Util.cast(this);
    }

    public void add(RoleUnresolved role)
            throws IllegalArgumentException{
        if(role==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        super.add(role);
    }

    public void add(int index,
                    RoleUnresolved role)
            throws IllegalArgumentException,
            IndexOutOfBoundsException{
        if(role==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        super.add(index,role);
    }

    public void set(int index,
                    RoleUnresolved role)
            throws IllegalArgumentException,
            IndexOutOfBoundsException{
        if(role==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        super.set(index,role);
    }

    public boolean addAll(RoleUnresolvedList roleList)
            throws IndexOutOfBoundsException{
        if(roleList==null){
            return true;
        }
        return (super.addAll(roleList));
    }

    public boolean addAll(int index,
                          RoleUnresolvedList roleList)
            throws IllegalArgumentException,
            IndexOutOfBoundsException{
        if(roleList==null){
            String excMsg="Invalid parameter";
            throw new IllegalArgumentException(excMsg);
        }
        return (super.addAll(index,roleList));
    }

    @Override
    public Object set(int index,Object element){
        if(!tainted)
            tainted=isTainted(element);
        if(typeSafe)
            checkTypeSafe(element);
        return super.set(index,element);
    }

    @Override
    public boolean add(Object o){
        if(!tainted)
            tainted=isTainted(o);
        if(typeSafe)
            checkTypeSafe(o);
        return super.add(o);
    }

    @Override
    public void add(int index,Object element){
        if(!tainted)
            tainted=isTainted(element);
        if(typeSafe)
            checkTypeSafe(element);
        super.add(index,element);
    }

    @Override
    public boolean addAll(Collection<?> c){
        if(!tainted)
            tainted=isTainted(c);
        if(typeSafe)
            checkTypeSafe(c);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index,Collection<?> c){
        if(!tainted)
            tainted=isTainted(c);
        if(typeSafe)
            checkTypeSafe(c);
        return super.addAll(index,c);
    }

    private static boolean isTainted(Collection<?> c){
        try{
            checkTypeSafe(c);
        }catch(IllegalArgumentException e){
            return true;
        }
        return false;
    }

    private static boolean isTainted(Object o){
        try{
            checkTypeSafe(o);
        }catch(IllegalArgumentException e){
            return true;
        }
        return false;
    }

    private static void checkTypeSafe(Object o){
        try{
            o=(RoleUnresolved)o;
        }catch(ClassCastException e){
            throw new IllegalArgumentException(e);
        }
    }
}
