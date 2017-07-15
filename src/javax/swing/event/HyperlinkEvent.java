/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.text.Element;
import java.awt.event.InputEvent;
import java.net.URL;
import java.util.EventObject;

public class HyperlinkEvent extends EventObject{
    private EventType type;
    private URL u;
    private String desc;
    private Element sourceElement;
    private InputEvent inputEvent;

    public HyperlinkEvent(Object source,EventType type,URL u){
        this(source,type,u,null);
    }

    public HyperlinkEvent(Object source,EventType type,URL u,String desc){
        this(source,type,u,desc,null);
    }

    public HyperlinkEvent(Object source,EventType type,URL u,String desc,
                          Element sourceElement){
        super(source);
        this.type=type;
        this.u=u;
        this.desc=desc;
        this.sourceElement=sourceElement;
    }

    public HyperlinkEvent(Object source,EventType type,URL u,String desc,
                          Element sourceElement,InputEvent inputEvent){
        super(source);
        this.type=type;
        this.u=u;
        this.desc=desc;
        this.sourceElement=sourceElement;
        this.inputEvent=inputEvent;
    }

    public EventType getEventType(){
        return type;
    }

    public String getDescription(){
        return desc;
    }

    public URL getURL(){
        return u;
    }

    public Element getSourceElement(){
        return sourceElement;
    }

    public InputEvent getInputEvent(){
        return inputEvent;
    }

    public static final class EventType{
        public static final EventType ENTERED=new EventType("ENTERED");
        public static final EventType EXITED=new EventType("EXITED");
        public static final EventType ACTIVATED=new EventType("ACTIVATED");
        private String typeString;

        private EventType(String s){
            typeString=s;
        }

        public String toString(){
            return typeString;
        }
    }
}
