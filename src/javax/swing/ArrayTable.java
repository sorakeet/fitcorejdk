/**
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

class ArrayTable implements Cloneable{
    private static final int ARRAY_BOUNDARY=8;
    // Our field for storage
    private Object table=null;

    static void writeArrayTable(ObjectOutputStream s,ArrayTable table) throws IOException{
        Object keys[];
        if(table==null||(keys=table.getKeys(null))==null){
            s.writeInt(0);
        }else{
            // Determine how many keys have Serializable values, when
            // done all non-null values in keys identify the Serializable
            // values.
            int validCount=0;
            for(int counter=0;counter<keys.length;counter++){
                Object key=keys[counter];
                /** include in Serialization when both keys and values are Serializable */
                if((key instanceof Serializable
                        &&table.get(key) instanceof Serializable)
                        ||
                        /** include these only so that we get the appropriate exception below */
                        (key instanceof ClientPropertyKey
                                &&((ClientPropertyKey)key).getReportValueNotSerializable())){
                    validCount++;
                }else{
                    keys[counter]=null;
                }
            }
            // Write ou the Serializable key/value pairs.
            s.writeInt(validCount);
            if(validCount>0){
                for(Object key : keys){
                    if(key!=null){
                        s.writeObject(key);
                        s.writeObject(table.get(key));
                        if(--validCount==0){
                            break;
                        }
                    }
                }
            }
        }
    }

    public void put(Object key,Object value){
        if(table==null){
            table=new Object[]{key,value};
        }else{
            int size=size();
            if(size<ARRAY_BOUNDARY){              // We are an array
                if(containsKey(key)){
                    Object[] tmp=(Object[])table;
                    for(int i=0;i<tmp.length-1;i+=2){
                        if(tmp[i].equals(key)){
                            tmp[i+1]=value;
                            break;
                        }
                    }
                }else{
                    Object[] array=(Object[])table;
                    int i=array.length;
                    Object[] tmp=new Object[i+2];
                    System.arraycopy(array,0,tmp,0,i);
                    tmp[i]=key;
                    tmp[i+1]=value;
                    table=tmp;
                }
            }else{                 // We are a hashtable
                if((size==ARRAY_BOUNDARY)&&isArray()){
                    grow();
                }
                ((Hashtable<Object,Object>)table).put(key,value);
            }
        }
    }

    public int size(){
        int size;
        if(table==null)
            return 0;
        if(isArray()){
            size=((Object[])table).length/2;
        }else{
            size=((Hashtable)table).size();
        }
        return size;
    }

    private boolean isArray(){
        return (table instanceof Object[]);
    }

    public boolean containsKey(Object key){
        boolean contains=false;
        if(table!=null){
            if(isArray()){
                Object[] array=(Object[])table;
                for(int i=0;i<array.length-1;i+=2){
                    if(array[i].equals(key)){
                        contains=true;
                        break;
                    }
                }
            }else{
                contains=((Hashtable)table).containsKey(key);
            }
        }
        return contains;
    }

    private void grow(){
        Object[] array=(Object[])table;
        Hashtable<Object,Object> tmp=new Hashtable<Object,Object>(array.length/2);
        for(int i=0;i<array.length;i+=2){
            tmp.put(array[i],array[i+1]);
        }
        table=tmp;
    }

    public Object get(Object key){
        Object value=null;
        if(table!=null){
            if(isArray()){
                Object[] array=(Object[])table;
                for(int i=0;i<array.length-1;i+=2){
                    if(array[i].equals(key)){
                        value=array[i+1];
                        break;
                    }
                }
            }else{
                value=((Hashtable)table).get(key);
            }
        }
        return value;
    }

    public Object remove(Object key){
        Object value=null;
        if(key==null){
            return null;
        }
        if(table!=null){
            if(isArray()){
                // Is key on the list?
                int index=-1;
                Object[] array=(Object[])table;
                for(int i=array.length-2;i>=0;i-=2){
                    if(array[i].equals(key)){
                        index=i;
                        value=array[i+1];
                        break;
                    }
                }
                // If so,  remove it
                if(index!=-1){
                    Object[] tmp=new Object[array.length-2];
                    // Copy the list up to index
                    System.arraycopy(array,0,tmp,0,index);
                    // Copy from two past the index, up to
                    // the end of tmp (which is two elements
                    // shorter than the old list)
                    if(index<tmp.length)
                        System.arraycopy(array,index+2,tmp,index,
                                tmp.length-index);
                    // set the listener array to the new array or null
                    table=(tmp.length==0)?null:tmp;
                }
            }else{
                value=((Hashtable)table).remove(key);
            }
            if(size()==ARRAY_BOUNDARY-1&&!isArray()){
                shrink();
            }
        }
        return value;
    }

    private void shrink(){
        Hashtable<?,?> tmp=(Hashtable)table;
        Object[] array=new Object[tmp.size()*2];
        Enumeration<?> keys=tmp.keys();
        int j=0;
        while(keys.hasMoreElements()){
            Object o=keys.nextElement();
            array[j]=o;
            array[j+1]=tmp.get(o);
            j+=2;
        }
        table=array;
    }

    public void clear(){
        table=null;
    }

    public Object clone(){
        ArrayTable newArrayTable=new ArrayTable();
        if(isArray()){
            Object[] array=(Object[])table;
            for(int i=0;i<array.length-1;i+=2){
                newArrayTable.put(array[i],array[i+1]);
            }
        }else{
            Hashtable<?,?> tmp=(Hashtable)table;
            Enumeration<?> keys=tmp.keys();
            while(keys.hasMoreElements()){
                Object o=keys.nextElement();
                newArrayTable.put(o,tmp.get(o));
            }
        }
        return newArrayTable;
    }

    public Object[] getKeys(Object[] keys){
        if(table==null){
            return null;
        }
        if(isArray()){
            Object[] array=(Object[])table;
            if(keys==null){
                keys=new Object[array.length/2];
            }
            for(int i=0, index=0;i<array.length-1;i+=2,
                    index++){
                keys[index]=array[i];
            }
        }else{
            Hashtable<?,?> tmp=(Hashtable)table;
            Enumeration<?> enum_=tmp.keys();
            int counter=tmp.size();
            if(keys==null){
                keys=new Object[counter];
            }
            while(counter>0){
                keys[--counter]=enum_.nextElement();
            }
        }
        return keys;
    }
}
