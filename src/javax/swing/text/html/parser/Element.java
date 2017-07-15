/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html.parser;

import sun.awt.AppContext;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Hashtable;

public final class Element implements DTDConstants, Serializable{
    private static final Object MAX_INDEX_KEY=new Object();
    static Hashtable<String,Integer> contentTypes=new Hashtable<String,Integer>();

    static{
        contentTypes.put("CDATA",Integer.valueOf(CDATA));
        contentTypes.put("RCDATA",Integer.valueOf(RCDATA));
        contentTypes.put("EMPTY",Integer.valueOf(EMPTY));
        contentTypes.put("ANY",Integer.valueOf(ANY));
    }

    public int index;
    public String name;
    public boolean oStart;
    public boolean oEnd;
    public BitSet inclusions;
    public BitSet exclusions;
    public int type=ANY;
    public ContentModel content;
    public AttributeList atts;
    public Object data;

    Element(){
    }

    Element(String name,int index){
        this.name=name;
        this.index=index;
        if(index>getMaxIndex()){
            AppContext.getAppContext().put(MAX_INDEX_KEY,index);
        }
    }

    static int getMaxIndex(){
        Integer value=(Integer)AppContext.getAppContext().get(MAX_INDEX_KEY);
        return (value!=null)
                ?value.intValue()
                :0;
    }

    public static int name2type(String nm){
        Integer val=contentTypes.get(nm);
        return (val!=null)?val.intValue():0;
    }

    public String getName(){
        return name;
    }

    public boolean omitStart(){
        return oStart;
    }

    public boolean omitEnd(){
        return oEnd;
    }

    public int getType(){
        return type;
    }

    public ContentModel getContent(){
        return content;
    }

    public AttributeList getAttributes(){
        return atts;
    }

    public int getIndex(){
        return index;
    }

    public boolean isEmpty(){
        return type==EMPTY;
    }

    public String toString(){
        return name;
    }

    public AttributeList getAttribute(String name){
        for(AttributeList a=atts;a!=null;a=a.next){
            if(a.name.equals(name)){
                return a;
            }
        }
        return null;
    }

    public AttributeList getAttributeByValue(String name){
        for(AttributeList a=atts;a!=null;a=a.next){
            if((a.values!=null)&&a.values.contains(name)){
                return a;
            }
        }
        return null;
    }
}
