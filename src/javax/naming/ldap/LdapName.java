/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class LdapName implements Name{
    private static final long serialVersionUID=-1595520034788997356L;
    private transient List<Rdn> rdns;   // parsed name components
    private transient String unparsed;  // if non-null, the DN in unparsed form

    public LdapName(String name) throws InvalidNameException{
        unparsed=name;
        parse();
    }

    private void parse() throws InvalidNameException{
        // rdns = (ArrayList<Rdn>) (new RFC2253Parser(unparsed)).getDN();
        rdns=new Rfc2253Parser(unparsed).parseDn();
    }

    public LdapName(List<Rdn> rdns){
        // if (rdns instanceof ArrayList<Rdn>) {
        //      this.rdns = rdns.clone();
        // } else if (rdns instanceof List<Rdn>) {
        //      this.rdns = new ArrayList<Rdn>(rdns);
        // } else {
        //      throw IllegalArgumentException(
        //              "Invalid entries, list entries must be of type Rdn");
        //  }
        this.rdns=new ArrayList<>(rdns.size());
        for(int i=0;i<rdns.size();i++){
            Object obj=rdns.get(i);
            if(!(obj instanceof Rdn)){
                throw new IllegalArgumentException("Entry:"+obj+
                        "  not a valid type;list entries must be of type Rdn");
            }
            this.rdns.add((Rdn)obj);
        }
    }

    private LdapName(String name,List<Rdn> rdns,int beg,int end){
        unparsed=name;
        // this.rdns = rdns.subList(beg, end);
        List<Rdn> sList=rdns.subList(beg,end);
        this.rdns=new ArrayList<>(sList);
    }

    public Rdn getRdn(int posn){
        return rdns.get(posn);
    }

    public boolean startsWith(List<Rdn> rdns){
        if(rdns==null){
            return false;
        }
        int len1=this.rdns.size();
        int len2=rdns.size();
        return (len1>=len2&&
                doesListMatch(0,len2,rdns));
    }

    public boolean endsWith(List<Rdn> rdns){
        if(rdns==null){
            return false;
        }
        int len1=this.rdns.size();
        int len2=rdns.size();
        return (len1>=len2&&
                doesListMatch(len1-len2,len1,rdns));
    }

    public Name addAll(List<Rdn> suffixRdns){
        return addAll(size(),suffixRdns);
    }

    public Name addAll(int posn,List<Rdn> suffixRdns){
        unparsed=null;
        for(int i=0;i<suffixRdns.size();i++){
            Object obj=suffixRdns.get(i);
            if(!(obj instanceof Rdn)){
                throw new IllegalArgumentException("Entry:"+obj+
                        "  not a valid type;suffix list entries must be of type Rdn");
            }
            rdns.add(i+posn,(Rdn)obj);
        }
        return this;
    }

    public Name add(Rdn comp){
        return add(size(),comp);
    }

    public Name add(int posn,Rdn comp){
        if(comp==null){
            throw new NullPointerException("Cannot set comp to null");
        }
        rdns.add(posn,comp);
        unparsed=null;        // no longer valid
        return this;
    }

    public List<Rdn> getRdns(){
        return Collections.unmodifiableList(rdns);
    }

    public int compareTo(Object obj){
        if(!(obj instanceof LdapName)){
            throw new ClassCastException("The obj is not a LdapName");
        }
        // check possible shortcuts
        if(obj==this){
            return 0;
        }
        LdapName that=(LdapName)obj;
        if(unparsed!=null&&unparsed.equalsIgnoreCase(
                that.unparsed)){
            return 0;
        }
        // Compare RDNs one by one, lexicographically.
        int minSize=Math.min(rdns.size(),that.rdns.size());
        for(int i=0;i<minSize;i++){
            // Compare a single pair of RDNs.
            Rdn rdn1=rdns.get(i);
            Rdn rdn2=that.rdns.get(i);
            int diff=rdn1.compareTo(rdn2);
            if(diff!=0){
                return diff;
            }
        }
        return (rdns.size()-that.rdns.size());        // longer DN wins
    }

    public int size(){
        return rdns.size();
    }

    public boolean isEmpty(){
        return rdns.isEmpty();
    }

    public Enumeration<String> getAll(){
        final Iterator<Rdn> iter=rdns.iterator();
        return new Enumeration<String>(){
            public boolean hasMoreElements(){
                return iter.hasNext();
            }

            public String nextElement(){
                return iter.next().toString();
            }
        };
    }

    public String get(int posn){
        return rdns.get(posn).toString();
    }

    public Name getPrefix(int posn){
        try{
            return new LdapName(null,rdns,0,posn);
        }catch(IllegalArgumentException e){
            throw new IndexOutOfBoundsException(
                    "Posn: "+posn+", Size: "+rdns.size());
        }
    }

    public Name getSuffix(int posn){
        try{
            return new LdapName(null,rdns,posn,rdns.size());
        }catch(IllegalArgumentException e){
            throw new IndexOutOfBoundsException(
                    "Posn: "+posn+", Size: "+rdns.size());
        }
    }

    public boolean startsWith(Name n){
        if(n==null){
            return false;
        }
        int len1=rdns.size();
        int len2=n.size();
        return (len1>=len2&&
                matches(0,len2,n));
    }

    public boolean endsWith(Name n){
        if(n==null){
            return false;
        }
        int len1=rdns.size();
        int len2=n.size();
        return (len1>=len2&&
                matches(len1-len2,len1,n));
    }

    public Name addAll(Name suffix) throws InvalidNameException{
        return addAll(size(),suffix);
    }

    public Name addAll(int posn,Name suffix)
            throws InvalidNameException{
        unparsed=null;        // no longer valid
        if(suffix instanceof LdapName){
            LdapName s=(LdapName)suffix;
            rdns.addAll(posn,s.rdns);
        }else{
            Enumeration<String> comps=suffix.getAll();
            while(comps.hasMoreElements()){
                rdns.add(posn++,
                        (new Rfc2253Parser(comps.nextElement()).
                                parseRdn()));
            }
        }
        return this;
    }

    public Name add(String comp) throws InvalidNameException{
        return add(size(),comp);
    }

    public Name add(int posn,String comp) throws InvalidNameException{
        Rdn rdn=(new Rfc2253Parser(comp)).parseRdn();
        rdns.add(posn,rdn);
        unparsed=null;        // no longer valid
        return this;
    }

    public Object remove(int posn) throws InvalidNameException{
        unparsed=null;        // no longer valid
        return rdns.remove(posn).toString();
    }

    private boolean matches(int beg,int end,Name n){
        if(n instanceof LdapName){
            LdapName ln=(LdapName)n;
            return doesListMatch(beg,end,ln.rdns);
        }else{
            for(int i=beg;i<end;i++){
                Rdn rdn;
                String rdnString=n.get(i-beg);
                try{
                    rdn=(new Rfc2253Parser(rdnString)).parseRdn();
                }catch(InvalidNameException e){
                    return false;
                }
                if(!rdn.equals(rdns.get(i))){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean doesListMatch(int beg,int end,List<Rdn> rdns){
        for(int i=beg;i<end;i++){
            if(!this.rdns.get(i).equals(rdns.get(i-beg))){
                return false;
            }
        }
        return true;
    }

    public int hashCode(){
        // Sum up the hash codes of the components.
        int hash=0;
        // For each RDN...
        for(int i=0;i<rdns.size();i++){
            Rdn rdn=rdns.get(i);
            hash+=rdn.hashCode();
        }
        return hash;
    }

    public boolean equals(Object obj){
        // check possible shortcuts
        if(obj==this){
            return true;
        }
        if(!(obj instanceof LdapName)){
            return false;
        }
        LdapName that=(LdapName)obj;
        if(rdns.size()!=that.rdns.size()){
            return false;
        }
        if(unparsed!=null&&unparsed.equalsIgnoreCase(
                that.unparsed)){
            return true;
        }
        // Compare RDNs one by one for equality
        for(int i=0;i<rdns.size();i++){
            // Compare a single pair of RDNs.
            Rdn rdn1=rdns.get(i);
            Rdn rdn2=that.rdns.get(i);
            if(!rdn1.equals(rdn2)){
                return false;
            }
        }
        return true;
    }

    public Object clone(){
        return new LdapName(unparsed,rdns,0,rdns.size());
    }

    public String toString(){
        if(unparsed!=null){
            return unparsed;
        }
        StringBuilder builder=new StringBuilder();
        int size=rdns.size();
        if((size-1)>=0){
            builder.append(rdns.get(size-1));
        }
        for(int next=size-2;next>=0;next--){
            builder.append(',');
            builder.append(rdns.get(next));
        }
        unparsed=builder.toString();
        return unparsed;
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        s.writeObject(toString());
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        unparsed=(String)s.readObject();
        try{
            parse();
        }catch(InvalidNameException e){
            // shouldn't happen
            throw new java.io.StreamCorruptedException(
                    "Invalid name: "+unparsed);
        }
    }
}
