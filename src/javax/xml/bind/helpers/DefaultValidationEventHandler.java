/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind.helpers;

import org.w3c.dom.Node;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import java.net.URL;

public class DefaultValidationEventHandler implements ValidationEventHandler{
    public boolean handleEvent(ValidationEvent event){
        if(event==null){
            throw new IllegalArgumentException();
        }
        // calculate the severity prefix and return value
        String severity=null;
        boolean retVal=false;
        switch(event.getSeverity()){
            case ValidationEvent.WARNING:
                severity=Messages.format(Messages.WARNING);
                retVal=true; // continue after warnings
                break;
            case ValidationEvent.ERROR:
                severity=Messages.format(Messages.ERROR);
                retVal=false; // terminate after errors
                break;
            case ValidationEvent.FATAL_ERROR:
                severity=Messages.format(Messages.FATAL_ERROR);
                retVal=false; // terminate after fatal errors
                break;
            default:
                assert false:
                        Messages.format(Messages.UNRECOGNIZED_SEVERITY,
                                event.getSeverity());
        }
        // calculate the location message
        String location=getLocation(event);
        System.out.println(
                Messages.format(Messages.SEVERITY_MESSAGE,
                        severity,
                        event.getMessage(),
                        location));
        // fail on the first error or fatal error
        return retVal;
    }

    private String getLocation(ValidationEvent event){
        StringBuffer msg=new StringBuffer();
        ValidationEventLocator locator=event.getLocator();
        if(locator!=null){
            URL url=locator.getURL();
            Object obj=locator.getObject();
            Node node=locator.getNode();
            int line=locator.getLineNumber();
            if(url!=null||line!=-1){
                msg.append("line "+line);
                if(url!=null)
                    msg.append(" of "+url);
            }else if(obj!=null){
                msg.append(" obj: "+obj.toString());
            }else if(node!=null){
                msg.append(" node: "+node.toString());
            }
        }else{
            msg.append(Messages.format(Messages.LOCATION_UNAVAILABLE));
        }
        return msg.toString();
    }
}
