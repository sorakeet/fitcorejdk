/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// jmx import
//

public class TabularType extends OpenType<TabularData>{
    static final long serialVersionUID=6554071860220659261L;
    private CompositeType rowType;
    private List<String> indexNames;
    private transient Integer myHashCode=null; // As this instance is immutable, these two values
    private transient String myToString=null; // need only be calculated once.

    public TabularType(String typeName,
                       String description,
                       CompositeType rowType,
                       String[] indexNames) throws OpenDataException{
        // Check and initialize state defined by parent.
        //
        super(TabularData.class.getName(),typeName,description,false);
        // Check rowType is not null
        //
        if(rowType==null){
            throw new IllegalArgumentException("Argument rowType cannot be null.");
        }
        // Check indexNames is neither null nor empty and does not contain any null element or empty string
        //
        checkForNullElement(indexNames,"indexNames");
        checkForEmptyString(indexNames,"indexNames");
        // Check all indexNames values are valid item names for rowType
        //
        for(int i=0;i<indexNames.length;i++){
            if(!rowType.containsKey(indexNames[i])){
                throw new OpenDataException("Argument's element value indexNames["+i+"]=\""+indexNames[i]+
                        "\" is not a valid item name for rowType.");
            }
        }
        // initialize rowType
        //
        this.rowType=rowType;
        // initialize indexNames (copy content so that subsequent
        // modifs to the array referenced by the indexNames parameter
        // have no impact)
        //
        List<String> tmpList=new ArrayList<String>(indexNames.length+1);
        for(int i=0;i<indexNames.length;i++){
            tmpList.add(indexNames[i]);
        }
        this.indexNames=Collections.unmodifiableList(tmpList);
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

    public boolean isValue(Object obj){
        // if obj is null or not a TabularData, return false
        //
        if(!(obj instanceof TabularData))
            return false;
        // if obj is not a TabularData, return false
        //
        TabularData value=(TabularData)obj;
        TabularType valueType=value.getTabularType();
        return isAssignableFrom(valueType);
    }

    @Override
    boolean isAssignableFrom(OpenType<?> ot){
        if(!(ot instanceof TabularType))
            return false;
        TabularType tt=(TabularType)ot;
        if(!getTypeName().equals(tt.getTypeName())||
                !getIndexNames().equals(tt.getIndexNames()))
            return false;
        return getRowType().isAssignableFrom(tt.getRowType());
    }

    public CompositeType getRowType(){
        return rowType;
    }

    public List<String> getIndexNames(){
        return indexNames;
    }

    public boolean equals(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // if obj is not a TabularType, return false
        //
        TabularType other;
        try{
            other=(TabularType)obj;
        }catch(ClassCastException e){
            return false;
        }
        // Now, really test for equality between this TabularType instance and the other:
        //
        // their names should be equal
        if(!this.getTypeName().equals(other.getTypeName())){
            return false;
        }
        // their row types should be equal
        if(!this.rowType.equals(other.rowType)){
            return false;
        }
        // their index names should be equal and in the same order (ensured by List.equals())
        if(!this.indexNames.equals(other.indexNames)){
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
            value+=this.rowType.hashCode();
            for(String index : indexNames)
                value+=index.hashCode();
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
            final StringBuilder result=new StringBuilder()
                    .append(this.getClass().getName())
                    .append("(name=")
                    .append(getTypeName())
                    .append(",rowType=")
                    .append(rowType.toString())
                    .append(",indexNames=(");
            String sep="";
            for(String index : indexNames){
                result.append(sep).append(index);
                sep=",";
            }
            result.append("))");
            myToString=result.toString();
        }
        // return always the same string representation for this instance (immutable)
        //
        return myToString;
    }
}
