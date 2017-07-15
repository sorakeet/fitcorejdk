/**
 * Copyright (c) 1997, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.beans.beancontext;

import java.util.EventObject;

public abstract class BeanContextEvent extends EventObject{
    private static final long serialVersionUID=7267998073569045052L;
    protected BeanContext propagatedFrom;

    protected BeanContextEvent(BeanContext bc){
        super(bc);
    }

    public BeanContext getBeanContext(){
        return (BeanContext)getSource();
    }

    public synchronized BeanContext getPropagatedFrom(){
        return propagatedFrom;
    }

    public synchronized void setPropagatedFrom(BeanContext bc){
        propagatedFrom=bc;
    }

    public synchronized boolean isPropagated(){
        return propagatedFrom!=null;
    }
}
