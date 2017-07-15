/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// SAX default implementation for AttributeList.
// http://www.saxproject.org
// No warranty; no copyright -- use this as you will.
// $Id: AttributeListImpl.java,v 1.2 2004/11/03 22:53:08 jsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.AttributeList;

import java.util.Vector;

public class AttributeListImpl implements AttributeList{
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    Vector names=new Vector();
    Vector types=new Vector();
    ////////////////////////////////////////////////////////////////////
    // Methods specific to this class.
    ////////////////////////////////////////////////////////////////////
    Vector values=new Vector();

    public AttributeListImpl(){
    }

    public AttributeListImpl(AttributeList atts){
        setAttributeList(atts);
    }

    public void setAttributeList(AttributeList atts){
        int count=atts.getLength();
        clear();
        for(int i=0;i<count;i++){
            addAttribute(atts.getName(i),atts.getType(i),atts.getValue(i));
        }
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.AttributeList
    ////////////////////////////////////////////////////////////////////

    public void addAttribute(String name,String type,String value){
        names.addElement(name);
        types.addElement(type);
        values.addElement(value);
    }

    public void clear(){
        names.removeAllElements();
        types.removeAllElements();
        values.removeAllElements();
    }

    public void removeAttribute(String name){
        int i=names.indexOf(name);
        if(i>=0){
            names.removeElementAt(i);
            types.removeElementAt(i);
            values.removeElementAt(i);
        }
    }

    public int getLength(){
        return names.size();
    }

    public String getName(int i){
        if(i<0){
            return null;
        }
        try{
            return (String)names.elementAt(i);
        }catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public String getType(int i){
        if(i<0){
            return null;
        }
        try{
            return (String)types.elementAt(i);
        }catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public String getValue(int i){
        if(i<0){
            return null;
        }
        try{
            return (String)values.elementAt(i);
        }catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public String getType(String name){
        return getType(names.indexOf(name));
    }

    public String getValue(String name){
        return getValue(names.indexOf(name));
    }
}
// end of AttributeListImpl.java
