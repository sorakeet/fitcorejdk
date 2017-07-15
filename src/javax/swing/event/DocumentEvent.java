/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.event;

import javax.swing.text.Document;
import javax.swing.text.Element;

public interface DocumentEvent{
    public int getOffset();

    public int getLength();

    public Document getDocument();

    public EventType getType();

    public ElementChange getChange(Element elem);

    public interface ElementChange{
        public Element getElement();

        public int getIndex();

        public Element[] getChildrenRemoved();

        public Element[] getChildrenAdded();
    }

    public static final class EventType{
        public static final EventType INSERT=new EventType("INSERT");
        public static final EventType REMOVE=new EventType("REMOVE");
        public static final EventType CHANGE=new EventType("CHANGE");
        private String typeString;

        private EventType(String s){
            typeString=s;
        }

        public String toString(){
            return typeString;
        }
    }
}
