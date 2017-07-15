/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.helpers;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;
import java.text.MessageFormat;

public class ValidationEventImpl implements ValidationEvent{
    private int severity;
    private String message;
    private Throwable linkedException;
    private ValidationEventLocator locator;
    public ValidationEventImpl(int _severity,String _message,
                               ValidationEventLocator _locator){
        this(_severity,_message,_locator,null);
    }
    public ValidationEventImpl(int _severity,String _message,
                               ValidationEventLocator _locator,
                               Throwable _linkedException){
        setSeverity(_severity);
        this.message=_message;
        this.locator=_locator;
        this.linkedException=_linkedException;
    }

    public String toString(){
        String s;
        switch(getSeverity()){
            case WARNING:
                s="WARNING";
                break;
            case ERROR:
                s="ERROR";
                break;
            case FATAL_ERROR:
                s="FATAL_ERROR";
                break;
            default:
                s=String.valueOf(getSeverity());
                break;
        }
        return MessageFormat.format("[severity={0},message={1},locator={2}]",
                new Object[]{
                        s,
                        getMessage(),
                        getLocator()
                });
    }

    public int getSeverity(){
        return severity;
    }

    public void setSeverity(int _severity){
        if(_severity!=ValidationEvent.WARNING&&
                _severity!=ValidationEvent.ERROR&&
                _severity!=ValidationEvent.FATAL_ERROR){
            throw new IllegalArgumentException(
                    Messages.format(Messages.ILLEGAL_SEVERITY));
        }
        this.severity=_severity;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String _message){
        this.message=_message;
    }

    public Throwable getLinkedException(){
        return linkedException;
    }

    public void setLinkedException(Throwable _linkedException){
        this.linkedException=_linkedException;
    }

    public ValidationEventLocator getLocator(){
        return locator;
    }

    public void setLocator(ValidationEventLocator _locator){
        this.locator=_locator;
    }
}
