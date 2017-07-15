/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
// jmx import
//

public class CompositeType extends OpenType<CompositeData>{
    static final long serialVersionUID=-5366242454346948798L;
    private TreeMap<String,String> nameToDescription;
    private TreeMap<String,OpenType<?>> nameToType;
    private transient Integer myHashCode=null;
    private transient String myToString=null;
    private transient Set<String> myNamesSet=null;

    public CompositeType(String typeName,
                         String description,
                         String[] itemNames,
                         String[] itemDescriptions,
                         OpenType<?>[] itemTypes) throws OpenDataException{
        // Check and construct state defined by parent
        //
        super(CompositeData.class.getName(),typeName,description,false);
        // Check the 3 arrays are not null or empty (ie length==0) and that there is no null element or empty string in them
        //
        checkForNullElement(itemNames,"itemNames");
        checkForNullElement(itemDescriptions,"itemDescriptions");
        checkForNullElement(itemTypes,"itemTypes");
        checkForEmptyString(itemNames,"itemNames");
        checkForEmptyString(itemDescriptions,"itemDescriptions");
        // Check the sizes of the 3 arrays are the same
        //
        if((itemNames.length!=itemDescriptions.length)||(itemNames.length!=itemTypes.length)){
            throw new IllegalArgumentException("Array arguments itemNames[], itemDescriptions[] and itemTypes[] "+
                    "should be of same length (got "+itemNames.length+", "+
                    itemDescriptions.length+" and "+itemTypes.length+").");
        }
        // Initialize internal "names to descriptions" and "names to types" sorted maps,
        // and, by doing so, check there are no duplicate item names
        //
        nameToDescription=new TreeMap<String,String>();
        nameToType=new TreeMap<String,OpenType<?>>();
        String key;
        for(int i=0;i<itemNames.length;i++){
            key=itemNames[i].trim();
            if(nameToDescription.containsKey(key)){
                throw new OpenDataException("Argument's element itemNames["+i+"]=\""+itemNames[i]+
                        "\" duplicates a previous item names.");
            }
            nameToDescription.put(key,itemDescriptions[i].trim());
            nameToType.put(key,itemTypes[i]);
        }
    }

    private static void checkForNullElement(Object[] arg,String argName){
        if((arg==null)||(arg.length==0)){
            throw new IllegalArgumentException("Argument "+argName+"[] cannot be null or empty.");
        }
        for(int i=0;i<arg.length;i++){
            if(arg[i]==null){
                throw new IllegalArgumentException("Argument's element "+argName+"["+i+"] cannot be null.");
            }
        }
    }

    private static void checkForEmptyString(String[] arg,String argName){
        for(int i=0;i<arg.length;i++){
            if(arg[i].trim().equals("")){
                throw new IllegalArgumentException("Argument's element "+argName+"["+i+"] cannot be an empty string.");
            }
        }
    }

    public boolean containsKey(String itemName){
        if(itemName==null){
            return false;
        }
        return nameToDescription.containsKey(itemName);
    }

    public String getDescription(String itemName){
        if(itemName==null){
            return null;
        }
        return nameToDescription.get(itemName);
    }

    public boolean isValue(Object obj){
        // if obj is null or not CompositeData, return false
        //
        if(!(obj instanceof CompositeData)){
            return false;
        }
        // if obj is not a CompositeData, return false
        //
        CompositeData value=(CompositeData)obj;
        // test value's CompositeType is assignable to this CompositeType instance
        //
        CompositeType valueType=value.getCompositeType();
        return this.isAssignableFrom(valueType);
    }

    @Override
    boolean isAssignableFrom(OpenType<?> ot){
        if(!(ot instanceof CompositeType))
            return false;
        CompositeType ct=(CompositeType)ot;
        if(!ct.getTypeName().equals(getTypeName()))
            return false;
        for(String key : keySet()){
            OpenType<?> otItemType=ct.getType(key);
            OpenType<?> thisItemType=getType(key);
            if(otItemType==null||
                    !thisItemType.isAssignableFrom(otItemType))
                return false;
        }
        return true;
    }

    public OpenType<?> getType(String itemName){
        if(itemName==null){
            return null;
        }
        return (OpenType<?>)nameToType.get(itemName);
    }

    public Set<String> keySet(){
        // Initializes myNamesSet on first call
        if(myNamesSet==null){
            myNamesSet=Collections.unmodifiableSet(nameToDescription.keySet());
        }
        return myNamesSet; // always return the same value
    }

    public boolean equals(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // if obj is not a CompositeType, return false
        //
        CompositeType other;
        try{
            other=(CompositeType)obj;
        }catch(ClassCastException e){
            return false;
        }
        // Now, really test for equality between this CompositeType instance and the other
        //
        // their names should be equal
        if(!this.getTypeName().equals(other.getTypeName())){
            return false;
        }
        // their items names and types should be equal
        if(!this.nameToType.equals(other.nameToType)){
            return false;
        }
        // All tests for equality were successfull
        //
        return true;
    }

    public int hashCode(){
        // Calculate the hash code value if it has not yet been done (ie 1st call to hashCode())
        //
        if(myHashCode==null){
            int value=0;
            value+=this.getTypeName().hashCode();
            for(String key : nameToDescription.keySet()){
                value+=key.hashCode();
                value+=this.nameToType.get(key).hashCode();
            }
            myHashCode=Integer.valueOf(value);
        }
        // return always the same hash code for this instance (immutable)
        //
        return myHashCode.intValue();
    }

    public String toString(){
        // Calculate the string representation if it has not yet been done (ie 1st call to toString())
        //
        if(myToString==null){
            final StringBuilder result=new StringBuilder();
            result.append(this.getClass().getName());
            result.append("(name=");
            result.append(getTypeName());
            result.append(",items=(");
            int i=0;
            Iterator<String> k=nameToType.keySet().iterator();
            String key;
            while(k.hasNext()){
                key=k.next();
                if(i>0) result.append(",");
                result.append("(itemName=");
                result.append(key);
                result.append(",itemType=");
                result.append(nameToType.get(key).toString()+")");
                i++;
            }
            result.append("))");
            myToString=result.toString();
        }
        // return always the same string representation for this instance (immutable)
        //
        return myToString;
    }
}
