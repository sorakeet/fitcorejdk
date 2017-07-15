/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Hashtable;

public class CannotProceedException extends NamingException{
    private static final long serialVersionUID=1219724816191576813L;
    protected Name remainingNewName=null;
    protected Hashtable<?,?> environment=null;
    protected Name altName=null;
    protected Context altNameCtx=null;

    public CannotProceedException(String explanation){
        super(explanation);
    }

    public CannotProceedException(){
        super();
    }

    public Hashtable<?,?> getEnvironment(){
        return environment;
    }

    public void setEnvironment(Hashtable<?,?> environment){
        this.environment=environment; // %%% clone it??
    }

    public Name getRemainingNewName(){
        return remainingNewName;
    }

    public void setRemainingNewName(Name newName){
        if(newName!=null)
            this.remainingNewName=(Name)(newName.clone());
        else
            this.remainingNewName=null;
    }

    public Name getAltName(){
        return altName;
    }

    public void setAltName(Name altName){
        this.altName=altName;
    }

    public Context getAltNameCtx(){
        return altNameCtx;
    }

    public void setAltNameCtx(Context altNameCtx){
        this.altNameCtx=altNameCtx;
    }
}
