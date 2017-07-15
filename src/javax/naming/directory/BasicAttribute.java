/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

public class BasicAttribute implements Attribute{
    private static final long serialVersionUID=6743528196119291326L;
    protected String attrID;
    protected transient Vector<Object> values;
    protected boolean ordered=false;

    public BasicAttribute(String id){
        this(id,false);
    }

    public BasicAttribute(String id,boolean ordered){
        attrID=id;
        values=new Vector<>();
        this.ordered=ordered;
    }

    public BasicAttribute(String id,Object value){
        this(id,value,false);
    }

    public BasicAttribute(String id,Object value,boolean ordered){
        this(id,ordered);
        values.addElement(value);
    }

    public int hashCode(){
        int hash=attrID.hashCode();
        int num=values.size();
        Object val;
        for(int i=0;i<num;i++){
            val=values.elementAt(i);
            if(val!=null){
                if(val.getClass().isArray()){
                    Object it;
                    int len=Array.getLength(val);
                    for(int j=0;j<len;j++){
                        it=Array.get(val,j);
                        if(it!=null){
                            hash+=it.hashCode();
                        }
                    }
                }else{
                    hash+=val.hashCode();
                }
            }
        }
        return hash;
    }

    public boolean equals(Object obj){
        if((obj!=null)&&(obj instanceof Attribute)){
            Attribute target=(Attribute)obj;
            // Check order first
            if(isOrdered()!=target.isOrdered()){
                return false;
            }
            int len;
            if(attrID.equals(target.getID())&&
                    (len=size())==target.size()){
                try{
                    if(isOrdered()){
                        // Go through both list of values
                        for(int i=0;i<len;i++){
                            if(!valueEquals(get(i),target.get(i))){
                                return false;
                            }
                        }
                    }else{
                        // order is not relevant; check for existence
                        Enumeration<?> theirs=target.getAll();
                        while(theirs.hasMoreElements()){
                            if(find(theirs.nextElement())<0)
                                return false;
                        }
                    }
                }catch(NamingException e){
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Object clone(){
        BasicAttribute attr;
        try{
            attr=(BasicAttribute)super.clone();
        }catch(CloneNotSupportedException e){
            attr=new BasicAttribute(attrID,ordered);
        }
        attr.values=(Vector<Object>)values.clone();
        return attr;
    }

    public String toString(){
        StringBuffer answer=new StringBuffer(attrID+": ");
        if(values.size()==0){
            answer.append("No values");
        }else{
            boolean start=true;
            for(Enumeration<Object> e=values.elements();e.hasMoreElements();){
                if(!start)
                    answer.append(", ");
                answer.append(e.nextElement());
                start=false;
            }
        }
        return answer.toString();
    }

    // For finding first element that has a null in JDK1.1 Vector.
    // In the Java 2 platform, can just replace this with Vector.indexOf(target);
    private int find(Object target){
        Class<?> cl;
        if(target==null){
            int ct=values.size();
            for(int i=0;i<ct;i++){
                if(values.elementAt(i)==null)
                    return i;
            }
        }else if((cl=target.getClass()).isArray()){
            int ct=values.size();
            Object it;
            for(int i=0;i<ct;i++){
                it=values.elementAt(i);
                if(it!=null&&cl==it.getClass()
                        &&arrayEquals(target,it))
                    return i;
            }
        }else{
            return values.indexOf(target,0);
        }
        return -1;  // not found
    }

    private static boolean valueEquals(Object obj1,Object obj2){
        if(obj1==obj2){
            return true; // object references are equal
        }
        if(obj1==null){
            return false; // obj2 was not false
        }
        if(obj1.getClass().isArray()&&
                obj2.getClass().isArray()){
            return arrayEquals(obj1,obj2);
        }
        return (obj1.equals(obj2));
    }

    private static boolean arrayEquals(Object a1,Object a2){
        int len;
        if((len=Array.getLength(a1))!=Array.getLength(a2))
            return false;
        for(int j=0;j<len;j++){
            Object i1=Array.get(a1,j);
            Object i2=Array.get(a2,j);
            if(i1==null||i2==null){
                if(i1!=i2)
                    return false;
            }else if(!i1.equals(i2)){
                return false;
            }
        }
        return true;
    }

    public NamingEnumeration<?> getAll() throws NamingException{
        return new ValuesEnumImpl();
    }

    public Object get() throws NamingException{
        if(values.size()==0){
            throw new
                    NoSuchElementException("Attribute "+getID()+" has no value");
        }else{
            return values.elementAt(0);
        }
    }

    public int size(){
        return values.size();
    }

    public String getID(){
        return attrID;
    }

    public boolean contains(Object attrVal){
        return (find(attrVal)>=0);
    }

    public boolean add(Object attrVal){
        if(isOrdered()||(find(attrVal)<0)){
            values.addElement(attrVal);
            return true;
        }else{
            return false;
        }
    }

    public boolean remove(Object attrval){
        // For the Java 2 platform, can just use "return removeElement(attrval);"
        // Need to do the following to handle null case
        int i=find(attrval);
        if(i>=0){
            values.removeElementAt(i);
            return true;
        }
        return false;
    }
//  ---- ordering methods

    public void clear(){
        values.setSize(0);
    }

    public DirContext getAttributeSyntaxDefinition() throws NamingException{
        throw new OperationNotSupportedException("attribute syntax");
    }

    public DirContext getAttributeDefinition() throws NamingException{
        throw new OperationNotSupportedException("attribute definition");
    }

    public boolean isOrdered(){
        return ordered;
    }

    public Object get(int ix) throws NamingException{
        return values.elementAt(ix);
    }
// ----------------- Schema methods

    public Object remove(int ix){
        Object answer=values.elementAt(ix);
        values.removeElementAt(ix);
        return answer;
    }

    public void add(int ix,Object attrVal){
        if(!isOrdered()&&contains(attrVal)){
            throw new IllegalStateException(
                    "Cannot add duplicate to unordered attribute");
        }
        values.insertElementAt(attrVal,ix);
    }
//  ---- serialization methods

    public Object set(int ix,Object attrVal){
        if(!isOrdered()&&contains(attrVal)){
            throw new IllegalStateException(
                    "Cannot add duplicate to unordered attribute");
        }
        Object answer=values.elementAt(ix);
        values.setElementAt(attrVal,ix);
        return answer;
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        s.defaultWriteObject(); // write out the attrID
        s.writeInt(values.size());
        for(int i=0;i<values.size();i++){
            s.writeObject(values.elementAt(i));
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();  // read in the attrID
        int n=s.readInt();    // number of values
        values=new Vector<>(n);
        while(--n>=0){
            values.addElement(s.readObject());
        }
    }

    class ValuesEnumImpl implements NamingEnumeration<Object>{
        Enumeration<Object> list;

        ValuesEnumImpl(){
            list=values.elements();
        }

        public boolean hasMoreElements(){
            return list.hasMoreElements();
        }

        public Object nextElement(){
            return (list.nextElement());
        }

        public Object next() throws NamingException{
            return list.nextElement();
        }

        public boolean hasMore() throws NamingException{
            return list.hasMoreElements();
        }

        public void close() throws NamingException{
            list=null;
        }
    }
}
