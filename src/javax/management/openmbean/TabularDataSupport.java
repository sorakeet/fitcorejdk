/**
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.openmbean;
// java import
//

import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.mbeanserver.Util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.util.*;
// jmx import
//

public class TabularDataSupport
        implements TabularData, Map<Object,Object>,
        Cloneable, Serializable{
    static final long serialVersionUID=5720150593236309827L;
    private final TabularType tabularType;
    // field cannot be final because of clone method
    private Map<Object,CompositeData> dataMap;
    private transient String[] indexNamesArray;

    public TabularDataSupport(TabularType tabularType){
        this(tabularType,16,0.75f);
    }

    public TabularDataSupport(TabularType tabularType,int initialCapacity,float loadFactor){
        // Check tabularType is not null
        //
        if(tabularType==null){
            throw new IllegalArgumentException("Argument tabularType cannot be null.");
        }
        // Initialize this.tabularType (and indexNamesArray for convenience)
        //
        this.tabularType=tabularType;
        List<String> tmpNames=tabularType.getIndexNames();
        this.indexNamesArray=tmpNames.toArray(new String[tmpNames.size()]);
        // Since LinkedHashMap was introduced in SE 1.4, it's conceivable even
        // if very unlikely that we might be the server of a 1.3 client.  In
        // that case you'll need to set this property.  See CR 6334663.
        String useHashMapProp=AccessController.doPrivileged(
                new GetPropertyAction("jmx.tabular.data.hash.map"));
        boolean useHashMap="true".equalsIgnoreCase(useHashMapProp);
        // Construct the empty contents HashMap
        //
        this.dataMap=useHashMap?
                new HashMap<Object,CompositeData>(initialCapacity,loadFactor):
                new LinkedHashMap<Object,CompositeData>(initialCapacity,loadFactor);
    }

    public boolean containsKey(Object key){
        // if key is not an array of Object instances, return false
        //
        Object[] k;
        try{
            k=(Object[])key;
        }catch(ClassCastException e){
            return false;
        }
        return this.containsKey(k);
    }

    public boolean containsValue(Object value){
        return dataMap.containsValue(value);
    }

    public Object get(Object key){
        return get((Object[])key);
    }

    public Object put(Object key,Object value){
        internalPut((CompositeData)value);
        return value; // should be return internalPut(...); (5090566)
    }

    private CompositeData internalPut(CompositeData value){
        // Check value is not null, value's type is the same as this instance's row type,
        // and calculate the value's index according to this instance's tabularType and
        // check it is not already used for a mapping in the parent HashMap
        //
        List<?> index=checkValueAndIndex(value);
        // store the (key, value) mapping in the dataMap HashMap
        //
        return dataMap.put(index,value);
    }

    private List<?> checkValueAndIndex(CompositeData value){
        // Check value is valid
        //
        checkValueType(value);
        // Calculate value's index according to this instance's tabularType
        // and check it is not already used for a mapping in the parent HashMap
        //
        List<?> index=internalCalculateIndex(value);
        if(dataMap.containsKey(index)){
            throw new KeyAlreadyExistsException("Argument value's index, calculated according to this TabularData "+
                    "instance's tabularType, already refers to a value in this table.");
        }
        // The check is OK, so return the index
        //
        return index;
    }

    public Object remove(Object key){
        return remove((Object[])key);
    }

    public void putAll(Map<?,?> t){
        // if t is null or empty, just return
        //
        if((t==null)||(t.size()==0)){
            return;
        }
        // Convert the values in t into an array of <tt>CompositeData</tt>
        //
        CompositeData[] values;
        try{
            values=
                    t.values().toArray(new CompositeData[t.size()]);
        }catch(ArrayStoreException e){
            throw new ClassCastException("Map argument t contains values which are not instances of <tt>CompositeData</tt>");
        }
        // Add the array of values
        //
        putAll(values);
    }

    @SuppressWarnings("unchecked")  // historical confusion about the return type
    public Set<Entry<Object,Object>> entrySet(){
        return Util.cast(dataMap.entrySet());
    }

    public int hashCode(){
        int result=0;
        result+=this.tabularType.hashCode();
        for(Object value : values())
            result+=value.hashCode();
        return result;
    }

    public boolean equals(Object obj){
        // if obj is null, return false
        //
        if(obj==null){
            return false;
        }
        // if obj is not a TabularData, return false
        //
        TabularData other;
        try{
            other=(TabularData)obj;
        }catch(ClassCastException e){
            return false;
        }
        // Now, really test for equality between this TabularData implementation and the other:
        //
        // their tabularType should be equal
        if(!this.getTabularType().equals(other.getTabularType())){
            return false;
        }
        // their contents should be equal:
        // . same size
        // . values in this instance are in the other (we know there are no duplicate elements possible)
        // (row values comparison is enough, because keys are calculated according to tabularType)
        if(this.size()!=other.size()){
            return false;
        }
        for(CompositeData value : dataMap.values()){
            if(!other.containsValue(value)){
                return false;
            }
        }
        // All tests for equality were successfull
        //
        return true;
    }

    public TabularType getTabularType(){
        return tabularType;
    }

    public Object[] calculateIndex(CompositeData value){
        // Check value is valid
        //
        checkValueType(value);
        // Return its calculated index
        //
        return internalCalculateIndex(value).toArray();
    }

    public int size(){
        return dataMap.size();
    }

    public boolean isEmpty(){
        return (this.size()==0);
    }

    public boolean containsKey(Object[] key){
        return (key==null?false:dataMap.containsKey(Arrays.asList(key)));
    }

    public boolean containsValue(CompositeData value){
        return dataMap.containsValue(value);
    }

    public CompositeData get(Object[] key){
        // Check key is not null and valid with tabularType
        // (throws NullPointerException, InvalidKeyException)
        //
        checkKeyType(key);
        // Return the mapping stored in the parent HashMap
        //
        return dataMap.get(Arrays.asList(key));
    }

    public void put(CompositeData value){
        internalPut(value);
    }

    public CompositeData remove(Object[] key){
        // Check key is not null and valid with tabularType
        // (throws NullPointerException, InvalidKeyException)
        //
        checkKeyType(key);
        // Removes the (key, value) mapping in the parent HashMap
        //
        return dataMap.remove(Arrays.asList(key));
    }

    public void putAll(CompositeData[] values){
        // if values is null or empty, just return
        //
        if((values==null)||(values.length==0)){
            return;
        }
        // create the list of indexes corresponding to each value
        List<List<?>> indexes=
                new ArrayList<List<?>>(values.length+1);
        // Check all elements in values and build index list
        //
        List<?> index;
        for(int i=0;i<values.length;i++){
            // check value and calculate index
            index=checkValueAndIndex(values[i]);
            // check index is different of those previously calculated
            if(indexes.contains(index)){
                throw new KeyAlreadyExistsException("Argument elements values["+i+"] and values["+indexes.indexOf(index)+
                        "] have the same indexes, "+
                        "calculated according to this TabularData instance's tabularType.");
            }
            // add to index list
            indexes.add(index);
        }
        // store all (index, value) mappings in the dataMap HashMap
        //
        for(int i=0;i<values.length;i++){
            dataMap.put(indexes.get(i),values[i]);
        }
    }

    public void clear(){
        dataMap.clear();
    }

    public Set<Object> keySet(){
        return dataMap.keySet();
    }

    @SuppressWarnings("unchecked")  // historical confusion about the return type
    public Collection<Object> values(){
        return Util.cast(dataMap.values());
    }

    private void checkKeyType(Object[] key){
        // Check key is neither null nor empty
        //
        if((key==null)||(key.length==0)){
            throw new NullPointerException("Argument key cannot be null or empty.");
        }
        /** Now check key is valid with tabularType index and row type definitions: */
        // key[] should have the size expected for an index
        //
        if(key.length!=this.indexNamesArray.length){
            throw new InvalidKeyException("Argument key's length="+key.length+
                    " is different from the number of item values, which is "+indexNamesArray.length+
                    ", specified for the indexing rows in this TabularData instance.");
        }
        // each element in key[] should be a value for its corresponding open type specified in rowType
        //
        OpenType<?> keyElementType;
        for(int i=0;i<key.length;i++){
            keyElementType=tabularType.getRowType().getType(this.indexNamesArray[i]);
            if((key[i]!=null)&&(!keyElementType.isValue(key[i]))){
                throw new InvalidKeyException("Argument element key["+i+"] is not a value for the open type expected for "+
                        "this element of the index, whose name is \""+indexNamesArray[i]+
                        "\" and whose open type is "+keyElementType);
            }
        }
    }

    private List<?> internalCalculateIndex(CompositeData value){
        return Collections.unmodifiableList(Arrays.asList(value.getAll(this.indexNamesArray)));
    }

    private void checkValueType(CompositeData value){
        // Check value is not null
        //
        if(value==null){
            throw new NullPointerException("Argument value cannot be null.");
        }
        // if value's type is not the same as this instance's row type, throw InvalidOpenTypeException
        //
        if(!tabularType.getRowType().isValue(value)){
            throw new InvalidOpenTypeException("Argument value's composite type ["+value.getCompositeType()+
                    "] is not assignable to "+
                    "this TabularData instance's row type ["+tabularType.getRowType()+"].");
        }
    }

    /** ***  Commodity methods from java.lang.Object  *** */
    public Object clone(){
        try{
            TabularDataSupport c=(TabularDataSupport)super.clone();
            c.dataMap=new HashMap<Object,CompositeData>(c.dataMap);
            return c;
        }catch(CloneNotSupportedException e){
            throw new InternalError(e.toString(),e);
        }
    }

    public String toString(){
        return new StringBuilder()
                .append(this.getClass().getName())
                .append("(tabularType=")
                .append(tabularType.toString())
                .append(",contents=")
                .append(dataMap.toString())
                .append(")")
                .toString();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        List<String> tmpNames=tabularType.getIndexNames();
        indexNamesArray=tmpNames.toArray(new String[tmpNames.size()]);
    }
}
