/**
 * Copyright (c) 2004, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.Util;

import java.io.InvalidObjectException;
import java.lang.reflect.Array;
import java.util.*;

public class ImmutableDescriptor implements Descriptor{
    public static final ImmutableDescriptor EMPTY_DESCRIPTOR=
            new ImmutableDescriptor();
    private static final long serialVersionUID=8853308591080540165L;
    private final String[] names;
    private final Object[] values;
    private transient int hashCode=-1;

    public ImmutableDescriptor(String[] fieldNames,Object[] fieldValues){
        this(makeMap(fieldNames,fieldValues));
    }

    public ImmutableDescriptor(Map<String,?> fields){
        if(fields==null)
            throw new IllegalArgumentException("Null Map");
        SortedMap<String,Object> map=
                new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
        for(Map.Entry<String,?> entry : fields.entrySet()){
            String name=entry.getKey();
            if(name==null||name.equals(""))
                throw new IllegalArgumentException("Empty or null field name");
            if(map.containsKey(name))
                throw new IllegalArgumentException("Duplicate name: "+name);
            map.put(name,entry.getValue());
        }
        int size=map.size();
        this.names=map.keySet().toArray(new String[size]);
        this.values=map.values().toArray(new Object[size]);
    }

    private static SortedMap<String,?> makeMap(String[] fieldNames,
                                               Object[] fieldValues){
        if(fieldNames==null||fieldValues==null)
            throw new IllegalArgumentException("Null array parameter");
        if(fieldNames.length!=fieldValues.length)
            throw new IllegalArgumentException("Different size arrays");
        SortedMap<String,Object> map=
                new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
        for(int i=0;i<fieldNames.length;i++){
            String name=fieldNames[i];
            if(name==null||name.equals(""))
                throw new IllegalArgumentException("Empty or null field name");
            Object old=map.put(name,fieldValues[i]);
            if(old!=null){
                throw new IllegalArgumentException("Duplicate field name: "+
                        name);
            }
        }
        return map;
    }

    public ImmutableDescriptor(String... fields){
        this(makeMap(fields));
    }

    private static SortedMap<String,?> makeMap(String[] fields){
        if(fields==null)
            throw new IllegalArgumentException("Null fields parameter");
        String[] fieldNames=new String[fields.length];
        String[] fieldValues=new String[fields.length];
        for(int i=0;i<fields.length;i++){
            String field=fields[i];
            int eq=field.indexOf('=');
            if(eq<0){
                throw new IllegalArgumentException("Missing = character: "+
                        field);
            }
            fieldNames[i]=field.substring(0,eq);
            // makeMap will catch the case where the name is empty
            fieldValues[i]=field.substring(eq+1);
        }
        return makeMap(fieldNames,fieldValues);
    }

    public static ImmutableDescriptor union(Descriptor... descriptors){
        // Optimize the case where exactly one Descriptor is non-Empty
        // and it is immutable - we can just return it.
        int index=findNonEmpty(descriptors,0);
        if(index<0)
            return EMPTY_DESCRIPTOR;
        if(descriptors[index] instanceof ImmutableDescriptor
                &&findNonEmpty(descriptors,index+1)<0)
            return (ImmutableDescriptor)descriptors[index];
        Map<String,Object> map=
                new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
        ImmutableDescriptor biggestImmutable=EMPTY_DESCRIPTOR;
        for(Descriptor d : descriptors){
            if(d!=null){
                String[] names;
                if(d instanceof ImmutableDescriptor){
                    ImmutableDescriptor id=(ImmutableDescriptor)d;
                    names=id.names;
                    if(id.getClass()==ImmutableDescriptor.class
                            &&names.length>biggestImmutable.names.length)
                        biggestImmutable=id;
                }else
                    names=d.getFieldNames();
                for(String n : names){
                    Object v=d.getFieldValue(n);
                    Object old=map.put(n,v);
                    if(old!=null){
                        boolean equal;
                        if(old.getClass().isArray()){
                            equal=Arrays.deepEquals(new Object[]{old},
                                    new Object[]{v});
                        }else
                            equal=old.equals(v);
                        if(!equal){
                            final String msg=
                                    "Inconsistent values for descriptor field "+
                                            n+": "+old+" :: "+v;
                            throw new IllegalArgumentException(msg);
                        }
                    }
                }
            }
        }
        if(biggestImmutable.names.length==map.size())
            return biggestImmutable;
        return new ImmutableDescriptor(map);
    }

    private static int findNonEmpty(Descriptor[] ds,int start){
        for(int i=start;i<ds.length;i++){
            if(!isEmpty(ds[i]))
                return i;
        }
        return -1;
    }

    private static boolean isEmpty(Descriptor d){
        if(d==null)
            return true;
        else if(d instanceof ImmutableDescriptor)
            return ((ImmutableDescriptor)d).names.length==0;
        else
            return (d.getFieldNames().length==0);
    }

    static Descriptor nonNullDescriptor(Descriptor d){
        if(d==null)
            return EMPTY_DESCRIPTOR;
        else
            return d;
    }

    private Object readResolve() throws InvalidObjectException{
        boolean bad=false;
        if(names==null||values==null||names.length!=values.length)
            bad=true;
        if(!bad){
            if(names.length==0&&getClass()==ImmutableDescriptor.class)
                return EMPTY_DESCRIPTOR;
            final Comparator<String> compare=String.CASE_INSENSITIVE_ORDER;
            String lastName=""; // also catches illegal null name
            for(int i=0;i<names.length;i++){
                if(names[i]==null||
                        compare.compare(lastName,names[i])>=0){
                    bad=true;
                    break;
                }
                lastName=names[i];
            }
        }
        if(bad)
            throw new InvalidObjectException("Bad names or values");
        return this;
    }    private int fieldIndex(String name){
        return Arrays.binarySearch(names,name,String.CASE_INSENSITIVE_ORDER);
    }

    // Note: this Javadoc is copied from javax.management.Descriptor
    //       due to 6369229.
    @Override
    public int hashCode(){
        if(hashCode==-1){
            hashCode=Util.hashCode(names,values);
        }
        return hashCode;
    }    public final Object getFieldValue(String fieldName){
        checkIllegalFieldName(fieldName);
        int i=fieldIndex(fieldName);
        if(i<0)
            return null;
        Object v=values[i];
        if(v==null||!v.getClass().isArray())
            return v;
        if(v instanceof Object[])
            return ((Object[])v).clone();
        // clone the primitive array, could use an 8-way if/else here
        int len=Array.getLength(v);
        Object a=Array.newInstance(v.getClass().getComponentType(),len);
        System.arraycopy(v,0,a,0,len);
        return a;
    }

    // Note: this Javadoc is copied from javax.management.Descriptor
    //       due to 6369229.
    @Override
    public boolean equals(Object o){
        if(o==this)
            return true;
        if(!(o instanceof Descriptor))
            return false;
        String[] onames;
        if(o instanceof ImmutableDescriptor){
            onames=((ImmutableDescriptor)o).names;
        }else{
            onames=((Descriptor)o).getFieldNames();
            Arrays.sort(onames,String.CASE_INSENSITIVE_ORDER);
        }
        if(names.length!=onames.length)
            return false;
        for(int i=0;i<names.length;i++){
            if(!names[i].equalsIgnoreCase(onames[i]))
                return false;
        }
        Object[] ovalues;
        if(o instanceof ImmutableDescriptor)
            ovalues=((ImmutableDescriptor)o).values;
        else
            ovalues=((Descriptor)o).getFieldValues(onames);
        return Arrays.deepEquals(values,ovalues);
    }    public final String[] getFields(){
        String[] result=new String[names.length];
        for(int i=0;i<result.length;i++){
            Object value=values[i];
            if(value==null)
                value="";
            else if(!(value instanceof String))
                value="("+value+")";
            result[i]=names[i]+"="+value;
        }
        return result;
    }

    @Override
    public Descriptor clone(){
        return this;
    }    public final Object[] getFieldValues(String... fieldNames){
        if(fieldNames==null)
            return values.clone();
        Object[] result=new Object[fieldNames.length];
        for(int i=0;i<fieldNames.length;i++){
            String name=fieldNames[i];
            if(name!=null&&!name.equals(""))
                result[i]=getFieldValue(name);
        }
        return result;
    }

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder("{");
        for(int i=0;i<names.length;i++){
            if(i>0)
                sb.append(", ");
            sb.append(names[i]).append("=");
            Object v=values[i];
            if(v!=null&&v.getClass().isArray()){
                String s=Arrays.deepToString(new Object[]{v});
                s=s.substring(1,s.length()-1); // remove [...]
                v=s;
            }
            sb.append(String.valueOf(v));
        }
        return sb.append("}").toString();
    }    public final String[] getFieldNames(){
        return names.clone();
    }







    public boolean isValid(){
        return true;
    }



    public final void setFields(String[] fieldNames,Object[] fieldValues)
            throws RuntimeOperationsException{
        if(fieldNames==null||fieldValues==null)
            illegal("Null argument");
        if(fieldNames.length!=fieldValues.length)
            illegal("Different array sizes");
        for(int i=0;i<fieldNames.length;i++)
            checkIllegalFieldName(fieldNames[i]);
        for(int i=0;i<fieldNames.length;i++)
            setField(fieldNames[i],fieldValues[i]);
    }

    public final void setField(String fieldName,Object fieldValue)
            throws RuntimeOperationsException{
        checkIllegalFieldName(fieldName);
        int i=fieldIndex(fieldName);
        if(i<0)
            unsupported();
        Object value=values[i];
        if((value==null)?
                (fieldValue!=null):
                !value.equals(fieldValue))
            unsupported();
    }

    public final void removeField(String fieldName){
        if(fieldName!=null&&fieldIndex(fieldName)>=0)
            unsupported();
    }



    private static void checkIllegalFieldName(String name){
        if(name==null||name.equals(""))
            illegal("Null or empty field name");
    }

    private static void unsupported(){
        UnsupportedOperationException uoe=
                new UnsupportedOperationException("Descriptor is read-only");
        throw new RuntimeOperationsException(uoe);
    }

    private static void illegal(String message){
        IllegalArgumentException iae=new IllegalArgumentException(message);
        throw new RuntimeOperationsException(iae);
    }
}
