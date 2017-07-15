/**
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingException;

public class AttributeModificationException extends NamingException{
    private static final long serialVersionUID=8060676069678710186L;
    private ModificationItem[] unexecs=null;

    public AttributeModificationException(String explanation){
        super(explanation);
    }

    public AttributeModificationException(){
        super();
    }

    public ModificationItem[] getUnexecutedModifications(){
        return unexecs;
    }

    public void setUnexecutedModifications(ModificationItem[] e){
        unexecs=e;
    }

    public String toString(){
        String orig=super.toString();
        if(unexecs!=null){
            orig+=("First unexecuted modification: "+
                    unexecs[0].toString());
        }
        return orig;
    }
}
