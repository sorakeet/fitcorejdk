/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

import java.util.Enumeration;
import java.util.Properties;

public class CompoundName implements Name{
    private static final long serialVersionUID=3513100557083972036L;
    protected transient NameImpl impl;
    protected transient Properties mySyntax;

    protected CompoundName(Enumeration<String> comps,Properties syntax){
        if(syntax==null){
            throw new NullPointerException();
        }
        mySyntax=syntax;
        impl=new NameImpl(syntax,comps);
    }

    public CompoundName(String n,Properties syntax) throws InvalidNameException{
        if(syntax==null){
            throw new NullPointerException();
        }
        mySyntax=syntax;
        impl=new NameImpl(syntax,n);
    }

    public int hashCode(){
        return impl.hashCode();
    }

    public boolean equals(Object obj){
        // %%% check syntax too?
        return (obj!=null&&
                obj instanceof CompoundName&&
                impl.equals(((CompoundName)obj).impl));
    }

    public Object clone(){
        return (new CompoundName(getAll(),mySyntax));
    }

    public String toString(){
        return (impl.toString());
    }

    public int compareTo(Object obj){
        if(!(obj instanceof CompoundName)){
            throw new ClassCastException("Not a CompoundName");
        }
        return impl.compareTo(((CompoundName)obj).impl);
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
        return (new CompoundName(comps,mySyntax));
    }

    public Name getSuffix(int posn){
        Enumeration<String> comps=impl.getSuffix(posn);
        return (new CompoundName(comps,mySyntax));
    }

    public boolean startsWith(Name n){
        if(n instanceof CompoundName){
            return (impl.startsWith(n.size(),n.getAll()));
        }else{
            return false;
        }
    }

    public boolean endsWith(Name n){
        if(n instanceof CompoundName){
            return (impl.endsWith(n.size(),n.getAll()));
        }else{
            return false;
        }
    }

    public Name addAll(Name suffix) throws InvalidNameException{
        if(suffix instanceof CompoundName){
            impl.addAll(suffix.getAll());
            return this;
        }else{
            throw new InvalidNameException("Not a compound name: "+
                    suffix.toString());
        }
    }

    public Name addAll(int posn,Name n) throws InvalidNameException{
        if(n instanceof CompoundName){
            impl.addAll(posn,n.getAll());
            return this;
        }else{
            throw new InvalidNameException("Not a compound name: "+
                    n.toString());
        }
    }

    public Name add(String comp) throws InvalidNameException{
        impl.add(comp);
        return this;
    }

    public Name add(int posn,String comp) throws InvalidNameException{
        impl.add(posn,comp);
        return this;
    }

    public Object remove(int posn) throws InvalidNameException{
        return impl.remove(posn);
    }

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException{
        s.writeObject(mySyntax);
        s.writeInt(size());
        Enumeration<String> comps=getAll();
        while(comps.hasMoreElements()){
            s.writeObject(comps.nextElement());
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException{
        mySyntax=(Properties)s.readObject();
        impl=new NameImpl(mySyntax);
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
 //   For testing

 public static void main(String[] args) {
 Properties dotSyntax = new Properties();
 dotSyntax.put("jndi.syntax.direction", "right_to_left");
 dotSyntax.put("jndi.syntax.separator", ".");
 dotSyntax.put("jndi.syntax.ignorecase", "true");
 dotSyntax.put("jndi.syntax.escape", "\\");
 //      dotSyntax.put("jndi.syntax.beginquote", "\"");
 //      dotSyntax.put("jndi.syntax.beginquote2", "'");

 Name first = null;
 try {
 for (int i = 0; i < args.length; i++) {
 Name name;
 Enumeration e;
 System.out.println("Given name: " + args[i]);
 name = new CompoundName(args[i], dotSyntax);
 if (first == null) {
 first = name;
 }
 e = name.getComponents();
 while (e.hasMoreElements()) {
 System.out.println("Element: " + e.nextElement());
 }
 System.out.println("Constructed name: " + name.toString());

 System.out.println("Compare " + first.toString() + " with "
 + name.toString() + " = " + first.compareTo(name));
 }
 } catch (Exception ne) {
 ne.printStackTrace();
 }
 }
 */
}
