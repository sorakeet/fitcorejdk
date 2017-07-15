/**
 * Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// Attributes2Impl.java - extended AttributesImpl
// http://www.saxproject.org
// Public Domain: no warranty.
// $Id: Attributes2Impl.java,v 1.3 2005/02/24 11:20:18 gg156739 Exp $
package org.xml.sax.ext;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class Attributes2Impl extends AttributesImpl implements Attributes2{
    private boolean declared[];
    private boolean specified[];

    public Attributes2Impl(){
        specified=null;
        declared=null;
    }

    public Attributes2Impl(Attributes atts){
        super(atts);
    }
    ////////////////////////////////////////////////////////////////////
    // Implementation of Attributes2
    ////////////////////////////////////////////////////////////////////

    // javadoc mostly from interface
    public boolean isDeclared(int index){
        if(index<0||index>=getLength())
            throw new ArrayIndexOutOfBoundsException(
                    "No attribute at index: "+index);
        return declared[index];
    }

    // javadoc mostly from interface
    public boolean isDeclared(String qName){
        int index=getIndex(qName);
        if(index<0)
            throw new IllegalArgumentException(
                    "No such attribute: "+qName);
        return declared[index];
    }

    // javadoc mostly from interface
    public boolean isDeclared(String uri,String localName){
        int index=getIndex(uri,localName);
        if(index<0)
            throw new IllegalArgumentException(
                    "No such attribute: local="+localName
                            +", namespace="+uri);
        return declared[index];
    }

    public boolean isSpecified(int index){
        if(index<0||index>=getLength())
            throw new ArrayIndexOutOfBoundsException(
                    "No attribute at index: "+index);
        return specified[index];
    }

    public boolean isSpecified(String uri,String localName){
        int index=getIndex(uri,localName);
        if(index<0)
            throw new IllegalArgumentException(
                    "No such attribute: local="+localName
                            +", namespace="+uri);
        return specified[index];
    }

    public boolean isSpecified(String qName){
        int index=getIndex(qName);
        if(index<0)
            throw new IllegalArgumentException(
                    "No such attribute: "+qName);
        return specified[index];
    }
    ////////////////////////////////////////////////////////////////////
    // Manipulators
    ////////////////////////////////////////////////////////////////////

    public void setAttributes(Attributes atts){
        int length=atts.getLength();
        super.setAttributes(atts);
        declared=new boolean[length];
        specified=new boolean[length];
        if(atts instanceof Attributes2){
            Attributes2 a2=(Attributes2)atts;
            for(int i=0;i<length;i++){
                declared[i]=a2.isDeclared(i);
                specified[i]=a2.isSpecified(i);
            }
        }else{
            for(int i=0;i<length;i++){
                declared[i]=!"CDATA".equals(atts.getType(i));
                specified[i]=true;
            }
        }
    }

    public void addAttribute(String uri,String localName,String qName,
                             String type,String value){
        super.addAttribute(uri,localName,qName,type,value);
        int length=getLength();
        if(specified==null){
            specified=new boolean[length];
            declared=new boolean[length];
        }else if(length>specified.length){
            boolean newFlags[];
            newFlags=new boolean[length];
            System.arraycopy(declared,0,newFlags,0,declared.length);
            declared=newFlags;
            newFlags=new boolean[length];
            System.arraycopy(specified,0,newFlags,0,specified.length);
            specified=newFlags;
        }
        specified[length-1]=true;
        declared[length-1]=!"CDATA".equals(type);
    }

    // javadoc entirely from superclass
    public void removeAttribute(int index){
        int origMax=getLength()-1;
        super.removeAttribute(index);
        if(index!=origMax){
            System.arraycopy(declared,index+1,declared,index,
                    origMax-index);
            System.arraycopy(specified,index+1,specified,index,
                    origMax-index);
        }
    }

    public void setDeclared(int index,boolean value){
        if(index<0||index>=getLength())
            throw new ArrayIndexOutOfBoundsException(
                    "No attribute at index: "+index);
        declared[index]=value;
    }

    public void setSpecified(int index,boolean value){
        if(index<0||index>=getLength())
            throw new ArrayIndexOutOfBoundsException(
                    "No attribute at index: "+index);
        specified[index]=value;
    }
}
