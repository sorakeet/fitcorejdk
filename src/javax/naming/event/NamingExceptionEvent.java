/**
 * Copyright (c) 1999, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.event;

import javax.naming.NamingException;

public class NamingExceptionEvent extends java.util.EventObject{
    private static final long serialVersionUID=-4877678086134736336L;
    private NamingException exception;

    public NamingExceptionEvent(EventContext source,NamingException exc){
        super(source);
        exception=exc;
    }

    public NamingException getException(){
        return exception;
    }

    public EventContext getEventContext(){
        return (EventContext)getSource();
    }

    public void dispatch(NamingListener listener){
        listener.namingExceptionThrown(this);
    }
}
