/**
 * Copyright (c) 2000, 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
// AttributesImpl.java - default implementation of Attributes.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the public domain.
// $Id: AttributesImpl.java,v 1.2 2004/11/03 22:53:08 jsuttor Exp $
package org.xml.sax.helpers;

import org.xml.sax.Attributes;

public class AttributesImpl implements Attributes{
    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////
    int length;
    String data[];
    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.Attributes.
    ////////////////////////////////////////////////////////////////////

    public AttributesImpl(){
        length=0;
        data=null;
    }

    public AttributesImpl(Attributes atts){
        setAttributes(atts);
    }

    public void setAttributes(Attributes atts){
        clear();
        length=atts.getLength();
        if(length>0){
            data=new String[length*5];
            for(int i=0;i<length;i++){
                data[i*5]=atts.getURI(i);
                data[i*5+1]=atts.getLocalName(i);
                data[i*5+2]=atts.getQName(i);
                data[i*5+3]=atts.getType(i);
                data[i*5+4]=atts.getValue(i);
            }
        }
    }

    public void clear(){
        if(data!=null){
            for(int i=0;i<(length*5);i++)
                data[i]=null;
        }
        length=0;
    }

    public int getLength(){
        return length;
    }

    public String getURI(int index){
        if(index>=0&&index<length){
            return data[index*5];
        }else{
            return null;
        }
    }

    public String getLocalName(int index){
        if(index>=0&&index<length){
            return data[index*5+1];
        }else{
            return null;
        }
    }

    public String getQName(int index){
        if(index>=0&&index<length){
            return data[index*5+2];
        }else{
            return null;
        }
    }

    public String getType(int index){
        if(index>=0&&index<length){
            return data[index*5+3];
        }else{
            return null;
        }
    }

    public String getValue(int index){
        if(index>=0&&index<length){
            return data[index*5+4];
        }else{
            return null;
        }
    }

    public int getIndex(String uri,String localName){
        int max=length*5;
        for(int i=0;i<max;i+=5){
            if(data[i].equals(uri)&&data[i+1].equals(localName)){
                return i/5;
            }
        }
        return -1;
    }

    public int getIndex(String qName){
        int max=length*5;
        for(int i=0;i<max;i+=5){
            if(data[i+2].equals(qName)){
                return i/5;
            }
        }
        return -1;
    }
    ////////////////////////////////////////////////////////////////////
    // Manipulators.
    ////////////////////////////////////////////////////////////////////

    public String getType(String uri,String localName){
        int max=length*5;
        for(int i=0;i<max;i+=5){
            if(data[i].equals(uri)&&data[i+1].equals(localName)){
                return data[i+3];
            }
        }
        return null;
    }

    public String getType(String qName){
        int max=length*5;
        for(int i=0;i<max;i+=5){
            if(data[i+2].equals(qName)){
                return data[i+3];
            }
        }
        return null;
    }

    public String getValue(String uri,String localName){
        int max=length*5;
        for(int i=0;i<max;i+=5){
            if(data[i].equals(uri)&&data[i+1].equals(localName)){
                return data[i+4];
            }
        }
        return null;
    }

    public String getValue(String qName){
        int max=length*5;
        for(int i=0;i<max;i+=5){
            if(data[i+2].equals(qName)){
                return data[i+4];
            }
        }
        return null;
    }

    public void addAttribute(String uri,String localName,String qName,
                             String type,String value){
        ensureCapacity(length+1);
        data[length*5]=uri;
        data[length*5+1]=localName;
        data[length*5+2]=qName;
        data[length*5+3]=type;
        data[length*5+4]=value;
        length++;
    }

    private void ensureCapacity(int n){
        if(n<=0){
            return;
        }
        int max;
        if(data==null||data.length==0){
            max=25;
        }else if(data.length>=n*5){
            return;
        }else{
            max=data.length;
        }
        while(max<n*5){
            max*=2;
        }
        String newData[]=new String[max];
        if(length>0){
            System.arraycopy(data,0,newData,0,length*5);
        }
        data=newData;
    }

    public void setAttribute(int index,String uri,String localName,
                             String qName,String type,String value){
        if(index>=0&&index<length){
            data[index*5]=uri;
            data[index*5+1]=localName;
            data[index*5+2]=qName;
            data[index*5+3]=type;
            data[index*5+4]=value;
        }else{
            badIndex(index);
        }
    }

    private void badIndex(int index)
            throws ArrayIndexOutOfBoundsException{
        String msg=
                "Attempt to modify attribute at illegal index: "+index;
        throw new ArrayIndexOutOfBoundsException(msg);
    }

    public void removeAttribute(int index){
        if(index>=0&&index<length){
            if(index<length-1){
                System.arraycopy(data,(index+1)*5,data,index*5,
                        (length-index-1)*5);
            }
            index=(length-1)*5;
            data[index++]=null;
            data[index++]=null;
            data[index++]=null;
            data[index++]=null;
            data[index]=null;
            length--;
        }else{
            badIndex(index);
        }
    }

    public void setURI(int index,String uri){
        if(index>=0&&index<length){
            data[index*5]=uri;
        }else{
            badIndex(index);
        }
    }
    ////////////////////////////////////////////////////////////////////
    // Internal methods.
    ////////////////////////////////////////////////////////////////////

    public void setLocalName(int index,String localName){
        if(index>=0&&index<length){
            data[index*5+1]=localName;
        }else{
            badIndex(index);
        }
    }

    public void setQName(int index,String qName){
        if(index>=0&&index<length){
            data[index*5+2]=qName;
        }else{
            badIndex(index);
        }
    }

    public void setType(int index,String type){
        if(index>=0&&index<length){
            data[index*5+3]=type;
        }else{
            badIndex(index);
        }
    }

    public void setValue(int index,String value){
        if(index>=0&&index<length){
            data[index*5+4]=value;
        }else{
            badIndex(index);
        }
    }
}
// end of AttributesImpl.java
