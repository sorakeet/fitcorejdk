/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management;

import com.sun.jmx.mbeanserver.GetPropertyAction;
import com.sun.jmx.mbeanserver.Util;

import java.io.*;
import java.security.AccessController;
import java.util.*;

@SuppressWarnings("serial") // don't complain serialVersionUID not constant
public class ObjectName implements Comparable<ObjectName>, QueryExp{
    public static final ObjectName WILDCARD=Util.newObjectName("*:*");
    // Inner classes <========================================
    // Private fields ---------------------------------------->
    // Serialization compatibility stuff -------------------->
    // Two serial forms are supported in this class. The selected form depends
    // on system property "jmx.serial.form":
    //  - "1.0" for JMX 1.0
    //  - any other value for JMX 1.1 and higher
    //
    // Serial version for old serial form
    private static final long oldSerialVersionUID=-5467795090068647408L;
    //
    // Serial version for new serial form
    private static final long newSerialVersionUID=1081892073854801359L;
    //
    // Serializable fields in old serial form
    private static final ObjectStreamField[] oldSerialPersistentFields=
            {
                    new ObjectStreamField("domain",String.class),
                    new ObjectStreamField("propertyList",Hashtable.class),
                    new ObjectStreamField("propertyListString",String.class),
                    new ObjectStreamField("canonicalName",String.class),
                    new ObjectStreamField("pattern",Boolean.TYPE),
                    new ObjectStreamField("propertyPattern",Boolean.TYPE)
            };
    //
    // Serializable fields in new serial form
    private static final ObjectStreamField[] newSerialPersistentFields={};
    //
    // Actual serial version and serial form
    private static final long serialVersionUID;
    private static final ObjectStreamField[] serialPersistentFields;
    //
    // Serialization compatibility stuff <==============================
    // Class private fields ----------------------------------->
    static final private Property[] _Empty_property_array=new Property[0];
    private static boolean compat=false;

    static{
        try{
            GetPropertyAction act=new GetPropertyAction("jmx.serial.form");
            String form=AccessController.doPrivileged(act);
            compat=(form!=null&&form.equals("1.0"));
        }catch(Exception e){
            // OK: exception means no compat with 1.0, too bad
        }
        if(compat){
            serialPersistentFields=oldSerialPersistentFields;
            serialVersionUID=oldSerialVersionUID;
        }else{
            serialPersistentFields=newSerialPersistentFields;
            serialVersionUID=newSerialVersionUID;
        }
    }

    // Class private fields <==============================
    // Instance private fields ----------------------------------->
    private transient String _canonicalName;
    private transient Property[] _kp_array;
    private transient Property[] _ca_array;
    private transient int _domain_length=0;
    private transient Map<String,String> _propertyList;
    private transient boolean _domain_pattern=false;
    private transient boolean _property_list_pattern=false;
    private transient boolean _property_value_pattern=false;
    public ObjectName(String name)
            throws MalformedObjectNameException{
        construct(name);
    }
    // Instance private fields <=======================================
    // Private fields <========================================
    //  Private methods ---------------------------------------->
    // Category : Instance construction ------------------------->

    public ObjectName(String domain,String key,String value)
            throws MalformedObjectNameException{
        // If key or value are null a NullPointerException
        // will be thrown by the put method in Hashtable.
        //
        Map<String,String> table=Collections.singletonMap(key,value);
        construct(domain,table);
    }

    private void construct(String domain,Map<String,String> props)
            throws MalformedObjectNameException{
        // The domain cannot be null
        if(domain==null)
            throw new NullPointerException("domain cannot be null");
        // The key property list cannot be null
        if(props==null)
            throw new NullPointerException("key property list cannot be null");
        // The key property list cannot be empty
        if(props.isEmpty())
            throw new MalformedObjectNameException(
                    "key property list cannot be empty");
        // checks domain validity
        if(!isDomain(domain))
            throw new MalformedObjectNameException("Invalid domain: "+domain);
        // init canonicalname
        final StringBuilder sb=new StringBuilder();
        sb.append(domain).append(':');
        _domain_length=domain.length();
        // allocates the property array
        int nb_props=props.size();
        _kp_array=new Property[nb_props];
        String[] keys=new String[nb_props];
        final Map<String,Property> keys_map=new HashMap<String,Property>();
        Property prop;
        int key_index;
        int i=0;
        for(Map.Entry<String,String> entry : props.entrySet()){
            if(sb.length()>0)
                sb.append(",");
            String key=entry.getKey();
            String value;
            try{
                value=entry.getValue();
            }catch(ClassCastException e){
                throw new MalformedObjectNameException(e.getMessage());
            }
            key_index=sb.length();
            checkKey(key);
            sb.append(key);
            keys[i]=key;
            sb.append("=");
            boolean value_pattern=checkValue(value);
            sb.append(value);
            if(!value_pattern){
                prop=new Property(key_index,
                        key.length(),
                        value.length());
            }else{
                _property_value_pattern=true;
                prop=new PatternProperty(key_index,
                        key.length(),
                        value.length());
            }
            addProperty(prop,i,keys_map,key);
            i++;
        }
        // initialize canonical name and data structure
        int len=sb.length();
        char[] initial_chars=new char[len];
        sb.getChars(0,len,initial_chars,0);
        char[] canonical_chars=new char[len];
        System.arraycopy(initial_chars,0,canonical_chars,0,
                _domain_length+1);
        setCanonicalName(initial_chars,canonical_chars,keys,keys_map,
                _domain_length+1,_kp_array.length);
    }
    // Category : Instance construction <==============================
    // Category : Internal utilities ------------------------------>

    private static boolean checkValue(String val)
            throws MalformedObjectNameException{
        if(val==null) throw new
                NullPointerException("Invalid value (null)");
        final int len=val.length();
        if(len==0)
            return false;
        final char[] s=val.toCharArray();
        final int[] result=parseValue(s,0);
        final int endValue=result[0];
        final boolean value_pattern=result[1]==1;
        if(endValue<len) throw new
                MalformedObjectNameException("Invalid character in value: `"+
                s[endValue]+"'");
        return value_pattern;
    }

    private static int[] parseValue(final char[] s,final int startValue)
            throws MalformedObjectNameException{
        boolean value_pattern=false;
        int next=startValue;
        int endValue=startValue;
        final int len=s.length;
        final char q=s[startValue];
        if(q=='"'){
            // quoted value
            if(++next==len) throw new
                    MalformedObjectNameException("Invalid quote");
            while(next<len){
                char last=s[next];
                if(last=='\\'){
                    if(++next==len) throw new
                            MalformedObjectNameException(
                            "Invalid unterminated quoted character sequence");
                    last=s[next];
                    switch(last){
                        case '\\':
                        case '?':
                        case '*':
                        case 'n':
                            break;
                        case '\"':
                            // We have an escaped quote. If this escaped
                            // quote is the last character, it does not
                            // qualify as a valid termination quote.
                            //
                            if(next+1==len) throw new
                                    MalformedObjectNameException(
                                    "Missing termination quote");
                            break;
                        default:
                            throw new
                                    MalformedObjectNameException(
                                    "Invalid quoted character sequence '\\"+
                                            last+"'");
                    }
                }else if(last=='\n'){
                    throw new MalformedObjectNameException(
                            "Newline in quoted value");
                }else if(last=='\"'){
                    next++;
                    break;
                }else{
                    switch(last){
                        case '?':
                        case '*':
                            value_pattern=true;
                            break;
                    }
                }
                next++;
                // Check that last character is a termination quote.
                // We have already handled the case were the last
                // character is an escaped quote earlier.
                //
                if((next>=len)&&(last!='\"')) throw new
                        MalformedObjectNameException("Missing termination quote");
            }
            endValue=next;
            if(next<len){
                if(s[next++]!=',') throw new
                        MalformedObjectNameException("Invalid quote");
            }
        }else{
            // Non quoted value.
            while(next<len){
                final char v=s[next++];
                switch(v){
                    case '*':
                    case '?':
                        value_pattern=true;
                        if(next<len) continue;
                        else endValue=next;
                        break;
                    case '=':
                    case ':':
                    case '\n':
                        final String ichar=((v=='\n')?"\\n":""+v);
                        throw new
                                MalformedObjectNameException("Invalid character `"+
                                ichar+"' in value");
                    case ',':
                        endValue=next-1;
                        break;
                    default:
                        if(next<len) continue;
                        else endValue=next;
                }
                break;
            }
        }
        return new int[]{endValue,value_pattern?1:0};
    }

    private static void checkKey(String key)
            throws MalformedObjectNameException{
        if(key==null) throw new
                NullPointerException("Invalid key (null)");
        final int len=key.length();
        if(len==0) throw new
                MalformedObjectNameException("Invalid key (empty)");
        final char[] k=key.toCharArray();
        final int endKey=parseKey(k,0);
        if(endKey<len) throw new
                MalformedObjectNameException("Invalid character in value: `"+
                k[endKey]+"'");
    }

    private static int parseKey(final char[] s,final int startKey)
            throws MalformedObjectNameException{
        int next=startKey;
        int endKey=startKey;
        final int len=s.length;
        while(next<len){
            final char k=s[next++];
            switch(k){
                case '*':
                case '?':
                case ',':
                case ':':
                case '\n':
                    final String ichar=((k=='\n')?"\\n":""+k);
                    throw new
                            MalformedObjectNameException("Invalid character in key: `"
                            +ichar+"'");
                case '=':
                    // we got the key.
                    endKey=next-1;
                    break;
                default:
                    if(next<len) continue;
                    else endKey=next;
            }
            break;
        }
        return endKey;
    }

    private boolean isDomain(String domain){
        if(domain==null) return true;
        final int len=domain.length();
        int next=0;
        while(next<len){
            final char c=domain.charAt(next++);
            switch(c){
                case ':':
                case '\n':
                    return false;
                case '*':
                case '?':
                    _domain_pattern=true;
                    break;
            }
        }
        return true;
    }

    public ObjectName(String domain,Hashtable<String,String> table)
            throws MalformedObjectNameException{
        construct(domain,table);
        /** The exception for when a key or value in the table is not a
         String is now ClassCastException rather than
         MalformedObjectNameException.  This was not previously
         specified.  */
    }
    // Category : Internal utilities <==============================
    // Category : Internal accessors ------------------------------>

    public static ObjectName getInstance(String name)
            throws MalformedObjectNameException, NullPointerException{
        return new ObjectName(name);
    }
    // Category : Internal accessors <==============================
    // Category : Serialization ----------------------------------->

    public static ObjectName getInstance(String domain,String key,
                                         String value)
            throws MalformedObjectNameException{
        return new ObjectName(domain,key,value);
    }

    public static ObjectName getInstance(String domain,
                                         Hashtable<String,String> table)
            throws MalformedObjectNameException{
        return new ObjectName(domain,table);
    }
    //  Category : Serialization <===================================
    // Private methods <========================================
    // Public methods ---------------------------------------->
    // Category : ObjectName Construction ------------------------------>

    public static ObjectName getInstance(ObjectName name){
        if(name.getClass().equals(ObjectName.class))
            return name;
        return Util.newObjectName(name.getSerializedNameString());
    }

    public static String quote(String s){
        final StringBuilder buf=new StringBuilder("\"");
        final int len=s.length();
        for(int i=0;i<len;i++){
            char c=s.charAt(i);
            switch(c){
                case '\n':
                    c='n';
                    buf.append('\\');
                    break;
                case '\\':
                case '\"':
                case '*':
                case '?':
                    buf.append('\\');
                    break;
            }
            buf.append(c);
        }
        buf.append('"');
        return buf.toString();
    }

    public static String unquote(String q){
        final StringBuilder buf=new StringBuilder();
        final int len=q.length();
        if(len<2||q.charAt(0)!='"'||q.charAt(len-1)!='"')
            throw new IllegalArgumentException("Argument not quoted");
        for(int i=1;i<len-1;i++){
            char c=q.charAt(i);
            if(c=='\\'){
                if(i==len-2)
                    throw new IllegalArgumentException("Trailing backslash");
                c=q.charAt(++i);
                switch(c){
                    case 'n':
                        c='\n';
                        break;
                    case '\\':
                    case '\"':
                    case '*':
                    case '?':
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Bad character '"+c+"' after backslash");
                }
            }else{
                switch(c){
                    case '*':
                    case '?':
                    case '\"':
                    case '\n':
                        throw new IllegalArgumentException(
                                "Invalid unescaped character '"+c+
                                        "' in the string to unquote");
                }
            }
            buf.append(c);
        }
        return buf.toString();
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException{
        String cn;
        if(compat){
            // Read an object serialized in the old serial form
            //
            //in.defaultReadObject();
            final ObjectInputStream.GetField fields=in.readFields();
            String propListString=
                    (String)fields.get("propertyListString","");
            // 6616825: take care of property patterns
            final boolean propPattern=
                    fields.get("propertyPattern",false);
            if(propPattern){
                propListString=
                        (propListString.length()==0?"*":(propListString+",*"));
            }
            cn=(String)fields.get("domain","default")+
                    ":"+propListString;
        }else{
            // Read an object serialized in the new serial form
            //
            in.defaultReadObject();
            cn=(String)in.readObject();
        }
        try{
            construct(cn);
        }catch(NullPointerException e){
            throw new InvalidObjectException(e.toString());
        }catch(MalformedObjectNameException e){
            throw new InvalidObjectException(e.toString());
        }
    }

    private void construct(String name)
            throws MalformedObjectNameException{
        // The name cannot be null
        if(name==null)
            throw new NullPointerException("name cannot be null");
        // Test if the name is empty
        if(name.length()==0){
            // this is equivalent to the whole word query object name.
            _canonicalName="*:*";
            _kp_array=_Empty_property_array;
            _ca_array=_Empty_property_array;
            _domain_length=1;
            _propertyList=null;
            _domain_pattern=true;
            _property_list_pattern=true;
            _property_value_pattern=false;
            return;
        }
        // initialize parsing of the string
        final char[] name_chars=name.toCharArray();
        final int len=name_chars.length;
        final char[] canonical_chars=new char[len]; // canonical form will
        // be same length at most
        int cname_index=0;
        int index=0;
        char c, c1;
        // parses domain part
        domain_parsing:
        while(index<len){
            switch(name_chars[index]){
                case ':':
                    _domain_length=index++;
                    break domain_parsing;
                case '=':
                    // ":" omission check.
                    //
                    // Although "=" is a valid character in the domain part
                    // it is true that it is rarely used in the real world.
                    // So check straight away if the ":" has been omitted
                    // from the ObjectName. This allows us to provide a more
                    // accurate exception message.
                    int i=++index;
                    while((i<len)&&(name_chars[i++]!=':'))
                        if(i==len)
                            throw new MalformedObjectNameException(
                                    "Domain part must be specified");
                    break;
                case '\n':
                    throw new MalformedObjectNameException(
                            "Invalid character '\\n' in domain name");
                case '*':
                case '?':
                    _domain_pattern=true;
                    index++;
                    break;
                default:
                    index++;
                    break;
            }
        }
        // check for non-empty properties
        if(index==len)
            throw new MalformedObjectNameException(
                    "Key properties cannot be empty");
        // we have got the domain part, begins building of _canonicalName
        System.arraycopy(name_chars,0,canonical_chars,0,_domain_length);
        canonical_chars[_domain_length]=':';
        cname_index=_domain_length+1;
        // parses property list
        Property prop;
        Map<String,Property> keys_map=new HashMap<String,Property>();
        String[] keys;
        String key_name;
        boolean quoted_value;
        int property_index=0;
        int in_index;
        int key_index, key_length, value_index, value_length;
        keys=new String[10];
        _kp_array=new Property[10];
        _property_list_pattern=false;
        _property_value_pattern=false;
        while(index<len){
            c=name_chars[index];
            // case of pattern properties
            if(c=='*'){
                if(_property_list_pattern)
                    throw new MalformedObjectNameException(
                            "Cannot have several '*' characters in pattern "+
                                    "property list");
                else{
                    _property_list_pattern=true;
                    if((++index<len)&&(name_chars[index]!=','))
                        throw new MalformedObjectNameException(
                                "Invalid character found after '*': end of "+
                                        "name or ',' expected");
                    else if(index==len){
                        if(property_index==0){
                            // empty properties case
                            _kp_array=_Empty_property_array;
                            _ca_array=_Empty_property_array;
                            _propertyList=Collections.emptyMap();
                        }
                        break;
                    }else{
                        // correct pattern spec in props, continue
                        index++;
                        continue;
                    }
                }
            }
            // standard property case, key part
            in_index=index;
            key_index=in_index;
            if(name_chars[in_index]=='=')
                throw new MalformedObjectNameException("Invalid key (empty)");
            while((in_index<len)&&((c1=name_chars[in_index++])!='='))
                switch(c1){
                    // '=' considered to introduce value part
                    case '*':
                    case '?':
                    case ',':
                    case ':':
                    case '\n':
                        final String ichar=((c1=='\n')?"\\n":""+c1);
                        throw new MalformedObjectNameException(
                                "Invalid character '"+ichar+
                                        "' in key part of property");
                }
            if(name_chars[in_index-1]!='=')
                throw new MalformedObjectNameException(
                        "Unterminated key property part");
            value_index=in_index; // in_index pointing after '=' char
            key_length=value_index-key_index-1; // found end of key
            // standard property case, value part
            boolean value_pattern=false;
            if(in_index<len&&name_chars[in_index]=='\"'){
                quoted_value=true;
                // the case of quoted value part
                quoted_value_parsing:
                while((++in_index<len)&&
                        ((c1=name_chars[in_index])!='\"')){
                    // the case of an escaped character
                    if(c1=='\\'){
                        if(++in_index==len)
                            throw new MalformedObjectNameException(
                                    "Unterminated quoted value");
                        switch(c1=name_chars[in_index]){
                            case '\\':
                            case '\"':
                            case '?':
                            case '*':
                            case 'n':
                                break; // valid character
                            default:
                                throw new MalformedObjectNameException(
                                        "Invalid escape sequence '\\"+
                                                c1+"' in quoted value");
                        }
                    }else if(c1=='\n'){
                        throw new MalformedObjectNameException(
                                "Newline in quoted value");
                    }else{
                        switch(c1){
                            case '?':
                            case '*':
                                value_pattern=true;
                                break;
                        }
                    }
                }
                if(in_index==len)
                    throw new MalformedObjectNameException(
                            "Unterminated quoted value");
                else value_length=++in_index-value_index;
            }else{
                // the case of standard value part
                quoted_value=false;
                while((in_index<len)&&((c1=name_chars[in_index])!=','))
                    switch(c1){
                        // ',' considered to be the value separator
                        case '*':
                        case '?':
                            value_pattern=true;
                            in_index++;
                            break;
                        case '=':
                        case ':':
                        case '"':
                        case '\n':
                            final String ichar=((c1=='\n')?"\\n":""+c1);
                            throw new MalformedObjectNameException(
                                    "Invalid character '"+ichar+
                                            "' in value part of property");
                        default:
                            in_index++;
                            break;
                    }
                value_length=in_index-value_index;
            }
            // Parsed property, checks the end of name
            if(in_index==len-1){
                if(quoted_value)
                    throw new MalformedObjectNameException(
                            "Invalid ending character `"+
                                    name_chars[in_index]+"'");
                else throw new MalformedObjectNameException(
                        "Invalid ending comma");
            }else in_index++;
            // we got the key and value part, prepare a property for this
            if(!value_pattern){
                prop=new Property(key_index,key_length,value_length);
            }else{
                _property_value_pattern=true;
                prop=new PatternProperty(key_index,key_length,value_length);
            }
            key_name=name.substring(key_index,key_index+key_length);
            if(property_index==keys.length){
                String[] tmp_string_array=new String[property_index+10];
                System.arraycopy(keys,0,tmp_string_array,0,property_index);
                keys=tmp_string_array;
            }
            keys[property_index]=key_name;
            addProperty(prop,property_index,keys_map,key_name);
            property_index++;
            index=in_index;
        }
        // computes and set canonical name
        setCanonicalName(name_chars,canonical_chars,keys,
                keys_map,cname_index,property_index);
    }

    private void addProperty(Property prop,int index,
                             Map<String,Property> keys_map,String key_name)
            throws MalformedObjectNameException{
        if(keys_map.containsKey(key_name)) throw new
                MalformedObjectNameException("key `"+
                key_name+"' already defined");
        // if no more space for property arrays, have to increase it
        if(index==_kp_array.length){
            Property[] tmp_prop_array=new Property[index+10];
            System.arraycopy(_kp_array,0,tmp_prop_array,0,index);
            _kp_array=tmp_prop_array;
        }
        _kp_array[index]=prop;
        keys_map.put(key_name,prop);
    }

    private void setCanonicalName(char[] specified_chars,
                                  char[] canonical_chars,
                                  String[] keys,Map<String,Property> keys_map,
                                  int prop_index,int nb_props){
        // Sort the list of found properties
        if(_kp_array!=_Empty_property_array){
            String[] tmp_keys=new String[nb_props];
            Property[] tmp_props=new Property[nb_props];
            System.arraycopy(keys,0,tmp_keys,0,nb_props);
            Arrays.sort(tmp_keys);
            keys=tmp_keys;
            System.arraycopy(_kp_array,0,tmp_props,0,nb_props);
            _kp_array=tmp_props;
            _ca_array=new Property[nb_props];
            // now assigns _ca_array to the sorted list of keys
            // (there cannot be two identical keys in an objectname.
            for(int i=0;i<nb_props;i++)
                _ca_array[i]=keys_map.get(keys[i]);
            // now we build the canonical name and set begin indexes of
            // properties to reflect canonical form
            int last_index=nb_props-1;
            int prop_len;
            Property prop;
            for(int i=0;i<=last_index;i++){
                prop=_ca_array[i];
                // length of prop including '=' char
                prop_len=prop._key_length+prop._value_length+1;
                System.arraycopy(specified_chars,prop._key_index,
                        canonical_chars,prop_index,prop_len);
                prop.setKeyIndex(prop_index);
                prop_index+=prop_len;
                if(i!=last_index){
                    canonical_chars[prop_index]=',';
                    prop_index++;
                }
            }
        }
        // terminate canonicalname with '*' in case of pattern
        if(_property_list_pattern){
            if(_kp_array!=_Empty_property_array)
                canonical_chars[prop_index++]=',';
            canonical_chars[prop_index++]='*';
        }
        // we now build the canonicalname string
        _canonicalName=(new String(canonical_chars,0,prop_index)).intern();
    }
    // Category : ObjectName Construction <==============================
    // Category : Getter methods ------------------------------>

    private void writeObject(ObjectOutputStream out)
            throws IOException{
        if(compat){
            // Serializes this instance in the old serial form
            // Read CR 6441274 before making any changes to this code
            ObjectOutputStream.PutField fields=out.putFields();
            fields.put("domain",_canonicalName.substring(0,_domain_length));
            fields.put("propertyList",getKeyPropertyList());
            fields.put("propertyListString",getKeyPropertyListString());
            fields.put("canonicalName",_canonicalName);
            fields.put("pattern",(_domain_pattern||_property_list_pattern));
            fields.put("propertyPattern",_property_list_pattern);
            out.writeFields();
        }else{
            // Serializes this instance in the new serial form
            //
            out.defaultWriteObject();
            out.writeObject(getSerializedNameString());
        }
    }

    // CR 6441274 depends on the modification property defined above
    public Hashtable<String,String> getKeyPropertyList(){
        return new Hashtable<String,String>(_getKeyPropertyList());
    }

    private Map<String,String> _getKeyPropertyList(){
        synchronized(this){
            if(_propertyList==null){
                // build (lazy eval) the property list from the canonical
                // properties array
                _propertyList=new HashMap<String,String>();
                int len=_ca_array.length;
                Property prop;
                for(int i=len-1;i>=0;i--){
                    prop=_ca_array[i];
                    _propertyList.put(prop.getKeyString(_canonicalName),
                            prop.getValueString(_canonicalName));
                }
            }
        }
        return _propertyList;
    }

    public String getKeyPropertyListString(){
        // BEWARE : we rebuild the propertyliststring at each call !!
        if(_kp_array.length==0) return "";
        // the size of the string is the canonical one minus domain
        // part and pattern part
        final int total_size=_canonicalName.length()-_domain_length-1
                -(_property_list_pattern?2:0);
        final char[] dest_chars=new char[total_size];
        final char[] value=_canonicalName.toCharArray();
        writeKeyPropertyListString(value,dest_chars,0);
        return new String(dest_chars);
    }

    private int writeKeyPropertyListString(char[] canonicalChars,
                                           char[] data,int offset){
        if(_kp_array.length==0) return offset;
        final char[] dest_chars=data;
        final char[] value=canonicalChars;
        int index=offset;
        final int len=_kp_array.length;
        final int last=len-1;
        for(int i=0;i<len;i++){
            final Property prop=_kp_array[i];
            final int prop_len=prop._key_length+prop._value_length+1;
            System.arraycopy(value,prop._key_index,dest_chars,index,
                    prop_len);
            index+=prop_len;
            if(i<last) dest_chars[index++]=',';
        }
        return index;
    }

    private String getSerializedNameString(){
        // the size of the string is the canonical one
        final int total_size=_canonicalName.length();
        final char[] dest_chars=new char[total_size];
        final char[] value=_canonicalName.toCharArray();
        final int offset=_domain_length+1;
        // copy "domain:" into dest_chars
        //
        System.arraycopy(value,0,dest_chars,0,offset);
        // Add property list string
        final int end=writeKeyPropertyListString(value,dest_chars,offset);
        // Add ",*" if necessary
        if(_property_list_pattern){
            if(end==offset){
                // Property list string is empty.
                dest_chars[end]='*';
            }else{
                // Property list string is not empty.
                dest_chars[end]=',';
                dest_chars[end+1]='*';
            }
        }
        return new String(dest_chars);
    }

    public boolean isPattern(){
        return (_domain_pattern||
                _property_list_pattern||
                _property_value_pattern);
    }

    public boolean isDomainPattern(){
        return _domain_pattern;
    }

    public boolean isPropertyPattern(){
        return _property_list_pattern||_property_value_pattern;
    }

    public boolean isPropertyListPattern(){
        return _property_list_pattern;
    }

    public boolean isPropertyValuePattern(){
        return _property_value_pattern;
    }

    public boolean isPropertyValuePattern(String property){
        if(property==null)
            throw new NullPointerException("key property can't be null");
        for(int i=0;i<_ca_array.length;i++){
            Property prop=_ca_array[i];
            String key=prop.getKeyString(_canonicalName);
            if(key.equals(property))
                return (prop instanceof PatternProperty);
        }
        throw new IllegalArgumentException("key property not found");
    }

    @Override
    public int hashCode(){
        return _canonicalName.hashCode();
    }

    @Override
    public boolean equals(Object object){
        // same object case
        if(this==object) return true;
        // object is not an object name case
        if(!(object instanceof ObjectName)) return false;
        // equality when canonical names are the same
        // (because usage of intern())
        ObjectName on=(ObjectName)object;
        String on_string=on._canonicalName;
        if(_canonicalName==on_string) return true;  // ES: OK
        // Because we are sharing canonical form between object names,
        // we have finished the comparison at this stage ==> unequal
        return false;
    }

    @Override
    public String toString(){
        return getSerializedNameString();
    }
    // Category : Getter methods <===================================
    // Category : Utilities ---------------------------------------->

    public boolean apply(ObjectName name){
        if(name==null) throw new NullPointerException();
        if(name._domain_pattern||
                name._property_list_pattern||
                name._property_value_pattern)
            return false;
        // No pattern
        if(!_domain_pattern&&
                !_property_list_pattern&&
                !_property_value_pattern)
            return _canonicalName.equals(name._canonicalName);
        return matchDomains(name)&&matchKeys(name);
    }

    private final boolean matchDomains(ObjectName name){
        if(_domain_pattern){
            // wildmatch domains
            // This ObjectName is the pattern
            // The other ObjectName is the string.
            return Util.wildmatch(name.getDomain(),getDomain());
        }
        return getDomain().equals(name.getDomain());
    }

    public String getDomain(){
        return _canonicalName.substring(0,_domain_length);
    }

    private final boolean matchKeys(ObjectName name){
        // If key property value pattern but not key property list
        // pattern, then the number of key properties must be equal
        //
        if(_property_value_pattern&&
                !_property_list_pattern&&
                (name._ca_array.length!=_ca_array.length))
            return false;
        // If key property value pattern or key property list pattern,
        // then every property inside pattern should exist in name
        //
        if(_property_value_pattern||_property_list_pattern){
            final Map<String,String> nameProps=name._getKeyPropertyList();
            final Property[] props=_ca_array;
            final String cn=_canonicalName;
            for(int i=props.length-1;i>=0;i--){
                // Find value in given object name for key at current
                // index in receiver
                //
                final Property p=props[i];
                final String k=p.getKeyString(cn);
                final String v=nameProps.get(k);
                // Did we find a value for this key ?
                //
                if(v==null) return false;
                // If this property is ok (same key, same value), go to next
                //
                if(_property_value_pattern&&(p instanceof PatternProperty)){
                    // wildmatch key property values
                    // p is the property pattern, v is the string
                    if(Util.wildmatch(v,p.getValueString(cn)))
                        continue;
                    else
                        return false;
                }
                if(v.equals(p.getValueString(cn))) continue;
                return false;
            }
            return true;
        }
        // If no pattern, then canonical names must be equal
        //
        final String p1=name.getCanonicalKeyPropertyListString();
        final String p2=getCanonicalKeyPropertyListString();
        return (p1.equals(p2));
    }

    public String getCanonicalKeyPropertyListString(){
        if(_ca_array.length==0) return "";
        int len=_canonicalName.length();
        if(_property_list_pattern) len-=2;
        return _canonicalName.substring(_domain_length+1,len);
    }

    public void setMBeanServer(MBeanServer mbs){
    }
    // Category : Utilities <===================================
    // Category : QueryExp Interface ---------------------------------------->

    public int compareTo(ObjectName name){
        // Quick optimization:
        //
        if(name==this) return 0;
        // (1) Compare domains
        //
        int domainValue=this.getDomain().compareTo(name.getDomain());
        if(domainValue!=0)
            return domainValue;
        // (2) Compare "type=" keys
        //
        // Within a given domain, all names with missing or empty "type="
        // come before all names with non-empty type.
        //
        // When both types are missing or empty, canonical-name ordering
        // applies which is a total order.
        //
        String thisTypeKey=this.getKeyProperty("type");
        String anotherTypeKey=name.getKeyProperty("type");
        if(thisTypeKey==null)
            thisTypeKey="";
        if(anotherTypeKey==null)
            anotherTypeKey="";
        int typeKeyValue=thisTypeKey.compareTo(anotherTypeKey);
        if(typeKeyValue!=0)
            return typeKeyValue;
        // (3) Compare canonical names
        //
        return this.getCanonicalName().compareTo(name.getCanonicalName());
    }

    public String getCanonicalName(){
        return _canonicalName;
    }

    public String getKeyProperty(String property){
        return _getKeyPropertyList().get(property);
    }

    private static class Property{
        int _key_index;
        int _key_length;
        int _value_length;

        Property(int key_index,int key_length,int value_length){
            _key_index=key_index;
            _key_length=key_length;
            _value_length=value_length;
        }

        void setKeyIndex(int key_index){
            _key_index=key_index;
        }

        String getKeyString(String name){
            return name.substring(_key_index,_key_index+_key_length);
        }

        String getValueString(String name){
            int in_begin=_key_index+_key_length+1;
            int out_end=in_begin+_value_length;
            return name.substring(in_begin,out_end);
        }
    }
    // Category : QueryExp Interface <=========================
    // Category : Comparable Interface ---------------------------------------->

    private static class PatternProperty extends Property{
        PatternProperty(int key_index,int key_length,int value_length){
            super(key_index,key_length,value_length);
        }
    }
    // Category : Comparable Interface <=========================
    // Public methods <========================================
}
