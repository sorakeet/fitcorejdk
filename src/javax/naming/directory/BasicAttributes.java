/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

public class BasicAttributes implements Attributes{
    private static final long serialVersionUID=4980164073184639448L;
    // The 'key' in attrs is stored in the 'right case'.
    // If ignoreCase is true, key is aways lowercase.
    // If ignoreCase is false, key is stored as supplied by put().
    // %%% Not declared "private" due to bug 4064984.
    transient Hashtable<String,Attribute> attrs=new Hashtable<>(11);
    private boolean ignoreCase=false;

    public BasicAttributes(String attrID,Object val){
        this();
        this.put(new BasicAttribute(attrID,val));
    }

    public BasicAttributes(){
    }

    public BasicAttributes(String attrID,Object val,boolean ignoreCase){
        this(ignoreCase);
        this.put(new BasicAttribute(attrID,val));
    }

    public BasicAttributes(boolean ignoreCase){
        this.ignoreCase=ignoreCase;
    }

    public boolean isCaseIgnored(){
        return ignoreCase;
    }

    public int size(){
        return attrs.size();
    }

    public Attribute get(String attrID){
        Attribute attr=attrs.get(
                ignoreCase?attrID.toLowerCase(Locale.ENGLISH):attrID);
        return (attr);
    }

    public NamingEnumeration<Attribute> getAll(){
        return new AttrEnumImpl();
    }

    public NamingEnumeration<String> getIDs(){
        return new IDEnumImpl();
    }

    public Attribute put(String attrID,Object val){
        return this.put(new BasicAttribute(attrID,val));
    }

    public Attribute put(Attribute attr){
        String id=attr.getID();
        if(ignoreCase){
            id=id.toLowerCase(Locale.ENGLISH);
        }
        return attrs.put(id,attr);
    }

    public Attribute remove(String attrID){
        String id=(ignoreCase?attrID.toLowerCase(Locale.ENGLISH):attrID);
        return attrs.remove(id);
    }

    public int hashCode(){
        int hash=(ignoreCase?1:0);
        try{
            NamingEnumeration<?> all=getAll();
            while(all.hasMore()){
                hash+=all.next().hashCode();
            }
        }catch(NamingException e){
        }
        return hash;
    }

    public boolean equals(Object obj){
        if((obj!=null)&&(obj instanceof Attributes)){
            Attributes target=(Attributes)obj;
            // Check case first
            if(ignoreCase!=target.isCaseIgnored()){
                return false;
            }
            if(size()==target.size()){
                Attribute their, mine;
                try{
                    NamingEnumeration<?> theirs=target.getAll();
                    while(theirs.hasMore()){
                        their=(Attribute)theirs.next();
                        mine=get(their.getID());
                        if(!their.equals(mine)){
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
        BasicAttributes attrset;
        try{
            attrset=(BasicAttributes)super.clone();
        }catch(CloneNotSupportedException e){
            attrset=new BasicAttributes(ignoreCase);
        }
        attrset.attrs=(Hashtable<String,Attribute>)attrs.clone();
        return attrset;
    }

    public String toString(){
        if(attrs.size()==0){
            return ("No attributes");
        }else{
            return attrs.toString();
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        s.defaultWriteObject(); // write out the ignoreCase flag
        s.writeInt(attrs.size());
        Enumeration<Attribute> attrEnum=attrs.elements();
        while(attrEnum.hasMoreElements()){
            s.writeObject(attrEnum.nextElement());
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();  // read in the ignoreCase flag
        int n=s.readInt();    // number of attributes
        attrs=(n>=1)
                ?new Hashtable<String,Attribute>(n*2)
                :new Hashtable<String,Attribute>(2); // can't have initial size of 0 (grrr...)
        while(--n>=0){
            put((Attribute)s.readObject());
        }
    }

    class AttrEnumImpl implements NamingEnumeration<Attribute>{
        Enumeration<Attribute> elements;

        public AttrEnumImpl(){
            this.elements=attrs.elements();
        }

        public Attribute next() throws NamingException{
            return nextElement();
        }        public boolean hasMoreElements(){
            return elements.hasMoreElements();
        }

        public Attribute nextElement(){
            return elements.nextElement();
        }

        public boolean hasMore() throws NamingException{
            return hasMoreElements();
        }



        public void close() throws NamingException{
            elements=null;
        }
    }

    class IDEnumImpl implements NamingEnumeration<String>{
        Enumeration<Attribute> elements;

        public IDEnumImpl(){
            // Walking through the elements, rather than the keys, gives
            // us attribute IDs that have not been converted to lowercase.
            this.elements=attrs.elements();
        }

        public boolean hasMoreElements(){
            return elements.hasMoreElements();
        }

        public String nextElement(){
            Attribute attr=elements.nextElement();
            return attr.getID();
        }

        public boolean hasMore() throws NamingException{
            return hasMoreElements();
        }

        public String next() throws NamingException{
            return nextElement();
        }

        public void close() throws NamingException{
            elements=null;
        }
    }
}
