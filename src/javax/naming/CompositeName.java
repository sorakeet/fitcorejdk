/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Enumeration;

public class CompositeName implements Name{
    private static final long serialVersionUID=1667768148915813118L;
    private transient NameImpl impl;

    protected CompositeName(Enumeration<String> comps){
        impl=new NameImpl(null,comps); // null means use default syntax
    }

    public CompositeName(String n) throws InvalidNameException{
        impl=new NameImpl(null,n);  // null means use default syntax
    }

    public CompositeName(){
        impl=new NameImpl(null);  // null means use default syntax
    }

    public int hashCode(){
        return impl.hashCode();
    }

    public boolean equals(Object obj){
        return (obj!=null&&
                obj instanceof CompositeName&&
                impl.equals(((CompositeName)obj).impl));
    }

    public Object clone(){
        return (new CompositeName(getAll()));
    }

    public String toString(){
        return impl.toString();
    }

    public int compareTo(Object obj){
        if(!(obj instanceof CompositeName)){
            throw new ClassCastException("Not a CompositeName");
        }
        return impl.compareTo(((CompositeName)obj).impl);
    }

    public int size(){
        return (impl.size());
    }

    public boolean isEmpty(){
        return (impl.isEmpty());
    }

    public Enumeration<String> getAll(){
        return (impl.getAll());
    }

    public String get(int posn){
        return (impl.get(posn));
    }

    public Name getPrefix(int posn){
        Enumeration<String> comps=impl.getPrefix(posn);
        return (new CompositeName(comps));
    }

    public Name getSuffix(int posn){
        Enumeration<String> comps=impl.getSuffix(posn);
        return (new CompositeName(comps));
    }

    public boolean startsWith(Name n){
        if(n instanceof CompositeName){
            return (impl.startsWith(n.size(),n.getAll()));
        }else{
            return false;
        }
    }

    public boolean endsWith(Name n){
        if(n instanceof CompositeName){
            return (impl.endsWith(n.size(),n.getAll()));
        }else{
            return false;
        }
    }

    public Name addAll(Name suffix)
            throws InvalidNameException{
        if(suffix instanceof CompositeName){
            impl.addAll(suffix.getAll());
            return this;
        }else{
            throw new InvalidNameException("Not a composite name: "+
                    suffix.toString());
        }
    }

    public Name addAll(int posn,Name n)
            throws InvalidNameException{
        if(n instanceof CompositeName){
            impl.addAll(posn,n.getAll());
            return this;
        }else{
            throw new InvalidNameException("Not a composite name: "+
                    n.toString());
        }
    }

    public Name add(String comp) throws InvalidNameException{
        impl.add(comp);
        return this;
    }

    public Name add(int posn,String comp)
            throws InvalidNameException{
        impl.add(posn,comp);
        return this;
    }

    public Object remove(int posn) throws InvalidNameException{
        return impl.remove(posn);
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        s.writeInt(size());
        Enumeration<String> comps=getAll();
        while(comps.hasMoreElements()){
            s.writeObject(comps.nextElement());
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        impl=new NameImpl(null);  // null means use default syntax
        int n=s.readInt();    // number of components
        try{
            while(--n>=0){
                add((String)s.readObject());
            }
        }catch(InvalidNameException e){
            throw (new java.io.StreamCorruptedException("Invalid name"));
        }
    }
/**
 // %%% Test code for serialization.
 public static void main(String[] args) throws Exception {
 CompositeName c = new CompositeName("aaa/bbb");
 java.io.FileOutputStream f1 = new java.io.FileOutputStream("/tmp/ser");
 java.io.ObjectOutputStream s1 = new java.io.ObjectOutputStream(f1);
 s1.writeObject(c);
 s1.close();
 java.io.FileInputStream f2 = new java.io.FileInputStream("/tmp/ser");
 java.io.ObjectInputStream s2 = new java.io.ObjectInputStream(f2);
 c = (CompositeName)s2.readObject();

 System.out.println("Size: " + c.size());
 System.out.println("Size: " + c.snit);
 }
 */
/**
 %%% Testing code
 public static void main(String[] args) {
 try {
 for (int i = 0; i < args.length; i++) {
 Name name;
 Enumeration e;
 System.out.println("Given name: " + args[i]);
 name = new CompositeName(args[i]);
 e = name.getComponents();
 while (e.hasMoreElements()) {
 System.out.println("Element: " + e.nextElement());
 }
 System.out.println("Constructed name: " + name.toString());
 }
 } catch (Exception ne) {
 ne.printStackTrace();
 }
 }
 */
}
