/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.ldap;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class Rdn implements Serializable, Comparable<Object>{
    // The common case.
    private static final int DEFAULT_SIZE=1;
    private static final long serialVersionUID=-5994465067210009656L;
    private static final String escapees=",=+<>#;\"\\";
    private transient ArrayList<RdnEntry> entries;

    public Rdn(Attributes attrSet) throws InvalidNameException{
        if(attrSet.size()==0){
            throw new InvalidNameException("Attributes cannot be empty");
        }
        entries=new ArrayList<>(attrSet.size());
        NamingEnumeration<? extends Attribute> attrs=attrSet.getAll();
        try{
            for(int nEntries=0;attrs.hasMore();nEntries++){
                RdnEntry entry=new RdnEntry();
                Attribute attr=attrs.next();
                entry.type=attr.getID();
                entry.value=attr.get();
                entries.add(nEntries,entry);
            }
        }catch(NamingException e){
            InvalidNameException e2=new InvalidNameException(
                    e.getMessage());
            e2.initCause(e);
            throw e2;
        }
        sort(); // arrange entries for comparison
    }

    void sort(){
        if(entries.size()>1){
            Collections.sort(entries);
        }
    }

    public Rdn(String rdnString) throws InvalidNameException{
        entries=new ArrayList<>(DEFAULT_SIZE);
        (new Rfc2253Parser(rdnString)).parseRdn(this);
    }

    public Rdn(Rdn rdn){
        entries=new ArrayList<>(rdn.entries.size());
        entries.addAll(rdn.entries);
    }

    public Rdn(String type,Object value) throws InvalidNameException{
        if(value==null){
            throw new NullPointerException("Cannot set value to null");
        }
        if(type.equals("")||isEmptyValue(value)){
            throw new InvalidNameException(
                    "type or value cannot be empty, type:"+type+
                            " value:"+value);
        }
        entries=new ArrayList<>(DEFAULT_SIZE);
        put(type,value);
    }

    private boolean isEmptyValue(Object val){
        return ((val instanceof String)&&val.equals(""))||
                ((val instanceof byte[])&&(((byte[])val).length==0));
    }

    Rdn put(String type,Object value){
        // create new Entry
        RdnEntry newEntry=new RdnEntry();
        newEntry.type=type;
        if(value instanceof byte[]){  // clone the byte array
            newEntry.value=((byte[])value).clone();
        }else{
            newEntry.value=value;
        }
        entries.add(newEntry);
        return this;
    }

    // An empty constructor used by the parser
    Rdn(){
        entries=new ArrayList<>(DEFAULT_SIZE);
    }

    public static String escapeValue(Object val){
        return (val instanceof byte[])
                ?escapeBinaryValue((byte[])val)
                :escapeStringValue((String)val);
    }

    private static String escapeStringValue(String val){
        char[] chars=val.toCharArray();
        StringBuilder builder=new StringBuilder(2*val.length());
        // Find leading and trailing whitespace.
        int lead;   // index of first char that is not leading whitespace
        for(lead=0;lead<chars.length;lead++){
            if(!isWhitespace(chars[lead])){
                break;
            }
        }
        int trail;  // index of last char that is not trailing whitespace
        for(trail=chars.length-1;trail>=0;trail--){
            if(!isWhitespace(chars[trail])){
                break;
            }
        }
        for(int i=0;i<chars.length;i++){
            char c=chars[i];
            if((i<lead)||(i>trail)||(escapees.indexOf(c)>=0)){
                builder.append('\\');
            }
            builder.append(c);
        }
        return builder.toString();
    }    public String toString(){
        StringBuilder builder=new StringBuilder();
        int size=entries.size();
        if(size>0){
            builder.append(entries.get(0));
        }
        for(int next=1;next<size;next++){
            builder.append('+');
            builder.append(entries.get(next));
        }
        return builder.toString();
    }

    private static boolean isWhitespace(char c){
        return (c==' '||c=='\r');
    }

    private static String escapeBinaryValue(byte[] val){
        StringBuilder builder=new StringBuilder(1+2*val.length);
        builder.append("#");
        for(int i=0;i<val.length;i++){
            byte b=val[i];
            builder.append(Character.forDigit(0xF&(b>>>4),16));
            builder.append(Character.forDigit(0xF&b,16));
        }
        return builder.toString();
    }    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(!(obj instanceof Rdn)){
            return false;
        }
        Rdn that=(Rdn)obj;
        if(entries.size()!=that.size()){
            return false;
        }
        for(int i=0;i<entries.size();i++){
            if(!entries.get(i).equals(that.entries.get(i))){
                return false;
            }
        }
        return true;
    }

    public static Object unescapeValue(String val){
        char[] chars=val.toCharArray();
        int beg=0;
        int end=chars.length;
        // Trim off leading and trailing whitespace.
        while((beg<end)&&isWhitespace(chars[beg])){
            ++beg;
        }
        while((beg<end)&&isWhitespace(chars[end-1])){
            --end;
        }
        // Add back the trailing whitespace with a preceding '\'
        // (escaped or unescaped) that was taken off in the above
        // loop. Whether or not to retain this whitespace is decided below.
        if(end!=chars.length&&
                (beg<end)&&
                chars[end-1]=='\\'){
            end++;
        }
        if(beg>=end){
            return "";
        }
        if(chars[beg]=='#'){
            // Value is binary (eg: "#CEB1DF80").
            return decodeHexPairs(chars,++beg,end);
        }
        // Trim off quotes.
        if((chars[beg]=='\"')&&(chars[end-1]=='\"')){
            ++beg;
            --end;
        }
        StringBuilder builder=new StringBuilder(end-beg);
        int esc=-1; // index of the last escaped character
        for(int i=beg;i<end;i++){
            if((chars[i]=='\\')&&(i+1<end)){
                if(!Character.isLetterOrDigit(chars[i+1])){
                    ++i;                            // skip backslash
                    builder.append(chars[i]);       // snarf escaped char
                    esc=i;
                }else{
                    // Convert hex-encoded UTF-8 to 16-bit chars.
                    byte[] utf8=getUtf8Octets(chars,i,end);
                    if(utf8.length>0){
                        try{
                            builder.append(new String(utf8,"UTF8"));
                        }catch(java.io.UnsupportedEncodingException e){
                            // shouldn't happen
                        }
                        i+=utf8.length*3-1;
                    }else{ // no utf8 bytes available, invalid DN
                        // '/' has no meaning, throw exception
                        throw new IllegalArgumentException(
                                "Not a valid attribute string value:"+
                                        val+",improper usage of backslash");
                    }
                }
            }else{
                builder.append(chars[i]);   // snarf unescaped char
            }
        }
        // Get rid of the unescaped trailing whitespace with the
        // preceding '\' character that was previously added back.
        int len=builder.length();
        if(isWhitespace(builder.charAt(len-1))&&esc!=(end-1)){
            builder.setLength(len-1);
        }
        return builder.toString();
    }

    private static byte[] decodeHexPairs(char[] chars,int beg,int end){
        byte[] bytes=new byte[(end-beg)/2];
        for(int i=0;beg+1<end;i++){
            int hi=Character.digit(chars[beg],16);
            int lo=Character.digit(chars[beg+1],16);
            if(hi<0||lo<0){
                break;
            }
            bytes[i]=(byte)((hi<<4)+lo);
            beg+=2;
        }
        if(beg!=end){
            throw new IllegalArgumentException(
                    "Illegal attribute value: "+new String(chars));
        }
        return bytes;
    }

    private static byte[] getUtf8Octets(char[] chars,int beg,int end){
        byte[] utf8=new byte[(end-beg)/3];    // allow enough room
        int len=0;        // index of first unused byte in utf8
        while((beg+2<end)&&
                (chars[beg++]=='\\')){
            int hi=Character.digit(chars[beg++],16);
            int lo=Character.digit(chars[beg++],16);
            if(hi<0||lo<0){
                break;
            }
            utf8[len++]=(byte)((hi<<4)+lo);
        }
        if(len==utf8.length){
            return utf8;
        }else{
            byte[] res=new byte[len];
            System.arraycopy(utf8,0,res,0,len);
            return res;
        }
    }

    public Object getValue(){
        return entries.get(0).getValue();
    }

    public String getType(){
        return entries.get(0).getType();
    }

    public int compareTo(Object obj){
        if(!(obj instanceof Rdn)){
            throw new ClassCastException("The obj is not a Rdn");
        }
        if(obj==this){
            return 0;
        }
        Rdn that=(Rdn)obj;
        int minSize=Math.min(entries.size(),that.entries.size());
        for(int i=0;i<minSize;i++){
            // Compare a single pair of type/value pairs.
            int diff=entries.get(i).compareTo(that.entries.get(i));
            if(diff!=0){
                return diff;
            }
        }
        return (entries.size()-that.entries.size());  // longer RDN wins
    }

    public int hashCode(){
        // Sum up the hash codes of the components.
        int hash=0;
        // For each type/value pair...
        for(int i=0;i<entries.size();i++){
            hash+=entries.get(i).hashCode();
        }
        return hash;
    }

    public Attributes toAttributes(){
        Attributes attrs=new BasicAttributes(true);
        for(int i=0;i<entries.size();i++){
            RdnEntry entry=entries.get(i);
            Attribute attr=attrs.put(entry.getType(),entry.getValue());
            if(attr!=null){
                attr.add(entry.getValue());
                attrs.put(attr);
            }
        }
        return attrs;
    }

    public int size(){
        return entries.size();
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        s.defaultWriteObject();
        s.writeObject(toString());
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        s.defaultReadObject();
        entries=new ArrayList<>(DEFAULT_SIZE);
        String unparsed=(String)s.readObject();
        try{
            (new Rfc2253Parser(unparsed)).parseRdn(this);
        }catch(InvalidNameException e){
            // shouldn't happen
            throw new java.io.StreamCorruptedException(
                    "Invalid name: "+unparsed);
        }
    }

    private static class RdnEntry implements Comparable<RdnEntry>{
        private String type;
        private Object value;
        // If non-null, a cannonical representation of the value suitable
        // for comparison using String.compareTo()
        private String comparable=null;

        String getType(){
            return type;
        }

        Object getValue(){
            return value;
        }

        public int compareTo(RdnEntry that){
            int diff=type.compareToIgnoreCase(that.type);
            if(diff!=0){
                return diff;
            }
            if(value.equals(that.value)){     // try shortcut
                return 0;
            }
            return getValueComparable().compareTo(
                    that.getValueComparable());
        }

        public boolean equals(Object obj){
            if(obj==this){
                return true;
            }
            if(!(obj instanceof RdnEntry)){
                return false;
            }
            // Any change here must be reflected in hashCode()
            RdnEntry that=(RdnEntry)obj;
            return (type.equalsIgnoreCase(that.type))&&
                    (getValueComparable().equals(
                            that.getValueComparable()));
        }

        public int hashCode(){
            return (type.toUpperCase(Locale.ENGLISH).hashCode()+
                    getValueComparable().hashCode());
        }

        public String toString(){
            return type+"="+escapeValue(value);
        }

        private String getValueComparable(){
            if(comparable!=null){
                return comparable;              // return cached result
            }
            // cache result
            if(value instanceof byte[]){
                comparable=escapeBinaryValue((byte[])value);
            }else{
                comparable=((String)value).toUpperCase(Locale.ENGLISH);
            }
            return comparable;
        }
    }




}
