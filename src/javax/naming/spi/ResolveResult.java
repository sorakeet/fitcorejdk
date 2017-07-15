/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.spi;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class ResolveResult implements java.io.Serializable{
    private static final long serialVersionUID=-4552108072002407559L;
    protected Object resolvedObj;
    protected Name remainingName;

    protected ResolveResult(){
        resolvedObj=null;
        remainingName=null;
    }

    public ResolveResult(Object robj,String rcomp){
        resolvedObj=robj;
        try{
            remainingName=new CompositeName(rcomp);
//          remainingName.appendComponent(rcomp);
        }catch(InvalidNameException e){
            // ignore; shouldn't happen
        }
    }

    public ResolveResult(Object robj,Name rname){
        resolvedObj=robj;
        setRemainingName(rname);
    }

    public Name getRemainingName(){
        return this.remainingName;
    }

    public void setRemainingName(Name name){
        if(name!=null)
            this.remainingName=(Name)(name.clone());
        else{
            // ??? should throw illegal argument exception
            this.remainingName=null;
        }
    }

    public Object getResolvedObj(){
        return this.resolvedObj;
    }

    public void setResolvedObj(Object obj){
        this.resolvedObj=obj;
        // ??? should check for null?
    }

    public void appendRemainingComponent(String name){
        if(name!=null){
            CompositeName rname=new CompositeName();
            try{
                rname.add(name);
            }catch(InvalidNameException e){
                // ignore; shouldn't happen for empty composite name
            }
            appendRemainingName(rname);
        }
    }

    public void appendRemainingName(Name name){
//      System.out.println("appendingRemainingName: " + name.toString());
//      Exception e = new Exception();
//      e.printStackTrace();
        if(name!=null){
            if(this.remainingName!=null){
                try{
                    this.remainingName.addAll(name);
                }catch(InvalidNameException e){
                    // ignore; shouldn't happen for composite name
                }
            }else{
                this.remainingName=(Name)(name.clone());
            }
        }
    }
}
