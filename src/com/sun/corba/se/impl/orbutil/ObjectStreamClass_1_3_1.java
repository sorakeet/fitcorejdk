/**
 * Copyright (c) 2001, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.orbutil;

import com.sun.corba.se.impl.io.ObjectStreamClass;
import com.sun.corba.se.impl.io.ValueUtility;
import org.omg.CORBA.ValueMember;

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

public class ObjectStreamClass_1_3_1 implements Serializable{
    public static final long kDefaultUID=-1;
    public static final ObjectStreamField[] NO_FIELDS=
            new ObjectStreamField[0];
    // private static native long getSerialVersionUIDField(Class cl);
    private static final long serialVersionUID=-6120832682080437368L;
    private static Object noArgsList[]={};
    private static Class<?> noTypesList[]={};
    private static Hashtable translatedFields;
    static private ObjectStreamClassEntry[] descriptorFor=new ObjectStreamClassEntry[61];
    private static Comparator compareClassByName=
            new CompareClassByName();
    private static Comparator compareMemberByName=
            new CompareMemberByName();
    boolean forProxyClass;
    int primBytes;
    int objFields;
    Method writeObjectMethod;
    Method readObjectMethod;
    private String name;
    private ObjectStreamClass_1_3_1 superclass;
    private boolean serializable;
    private boolean externalizable;
    private ObjectStreamField[] fields;
    private Class<?> ofClass;
    private long suid=kDefaultUID;
    private String suidStr=null;
    private long actualSuid=kDefaultUID;
    private String actualSuidStr=null;
    private Object lock=new Object();
    private boolean hasWriteObjectMethod;
    private boolean hasExternalizableBlockData;
    private transient Method writeReplaceObjectMethod;
    private transient Method readResolveObjectMethod;
    private ObjectStreamClass_1_3_1 localClassDesc;

    private ObjectStreamClass_1_3_1(Class<?> cl,ObjectStreamClass_1_3_1 superdesc,
                                    boolean serial,boolean extern){
        ofClass=cl;           /** created from this class */
        if(Proxy.isProxyClass(cl)){
            forProxyClass=true;
        }
        name=cl.getName();
        superclass=superdesc;
        serializable=serial;
        if(!forProxyClass){
            // proxy classes are never externalizable
            externalizable=extern;
        }
        /**
         * Enter this class in the table of known descriptors.
         * Otherwise, when the fields are read it may recurse
         * trying to find the descriptor for itself.
         */
        insertDescriptorFor(this);
        /**
         * The remainder of initialization occurs in init(), which is called
         * after the lock on the global class descriptor table has been
         * released.
         */
    }

    private static void insertDescriptorFor(ObjectStreamClass_1_3_1 desc){
        // Make sure not already present
        if(findDescriptorFor(desc.ofClass)!=null){
            return;
        }
        int hash=desc.ofClass.hashCode();
        int index=(hash&0x7FFFFFFF)%descriptorFor.length;
        ObjectStreamClassEntry e=new ObjectStreamClassEntry(desc);
        e.next=descriptorFor[index];
        descriptorFor[index]=e;
    }

    private static ObjectStreamClass_1_3_1 findDescriptorFor(Class<?> cl){
        int hash=cl.hashCode();
        int index=(hash&0x7FFFFFFF)%descriptorFor.length;
        ObjectStreamClassEntry e;
        ObjectStreamClassEntry prev;
        /** Free any initial entries whose refs have been cleared */
        while((e=descriptorFor[index])!=null&&e.get()==null){
            descriptorFor[index]=e.next;
        }
        /** Traverse the chain looking for a descriptor with ofClass == cl.
         * unlink entries that are unresolved.
         */
        prev=e;
        while(e!=null){
            ObjectStreamClass_1_3_1 desc=(ObjectStreamClass_1_3_1)(e.get());
            if(desc==null){
                // This entry has been cleared,  unlink it
                prev.next=e.next;
            }else{
                if(desc.ofClass==cl)
                    return desc;
                prev=e;
            }
            e=e.next;
        }
        return null;
    }

    ObjectStreamClass_1_3_1(String n,long s){
        name=n;
        suid=s;
        superclass=null;
    }

    static final ObjectStreamClass_1_3_1 lookup(Class<?> cl){
        ObjectStreamClass_1_3_1 desc=lookupInternal(cl);
        if(desc.isSerializable()||desc.isExternalizable())
            return desc;
        return null;
    }

    static ObjectStreamClass_1_3_1 lookupInternal(Class<?> cl){
        /** Synchronize on the hashtable so no two threads will do
         * this at the same time.
         */
        ObjectStreamClass_1_3_1 desc=null;
        synchronized(descriptorFor){
            /** Find the matching descriptor if it already known */
            desc=findDescriptorFor(cl);
            if(desc!=null){
                return desc;
            }
            /** Check if it's serializable */
            boolean serializable=Serializable.class.isAssignableFrom(cl);
            /** If the class is only Serializable,
             * lookup the descriptor for the superclass.
             */
            ObjectStreamClass_1_3_1 superdesc=null;
            if(serializable){
                Class<?> superclass=cl.getSuperclass();
                if(superclass!=null)
                    superdesc=lookup(superclass);
            }
            /** Check if its' externalizable.
             * If it's Externalizable, clear the serializable flag.
             * Only one or the other may be set in the protocol.
             */
            boolean externalizable=false;
            if(serializable){
                externalizable=
                        ((superdesc!=null)&&superdesc.isExternalizable())||
                                Externalizable.class.isAssignableFrom(cl);
                if(externalizable){
                    serializable=false;
                }
            }
            /** Create a new version descriptor,
             * it put itself in the known table.
             */
            desc=new ObjectStreamClass_1_3_1(cl,superdesc,
                    serializable,externalizable);
        }
        desc.init();
        return desc;
    }

    public static final long getSerialVersionUID(Class<?> clazz){
        ObjectStreamClass_1_3_1 theosc=ObjectStreamClass_1_3_1.lookup(clazz);
        if(theosc!=null){
            return theosc.getSerialVersionUID();
        }
        return 0;
    }

    public static final long getActualSerialVersionUID(Class<?> clazz){
        ObjectStreamClass_1_3_1 theosc=ObjectStreamClass_1_3_1.lookup(clazz);
        if(theosc!=null){
            return theosc.getActualSerialVersionUID();
        }
        return 0;
    }

    private static Object[] translateFields(Object objs[])
            throws NoSuchFieldException{
        try{
            java.io.ObjectStreamField fields[]=(java.io.ObjectStreamField[])objs;
            Object translation[]=null;
            if(translatedFields==null)
                translatedFields=new Hashtable();
            translation=(Object[])translatedFields.get(fields);
            if(translation!=null)
                return translation;
            else{
                Class<?> osfClass=ObjectStreamField.class;
                translation=(Object[])Array.newInstance(osfClass,objs.length);
                Object arg[]=new Object[2];
                Class<?> types[]={String.class,Class.class};
                Constructor constructor=osfClass.getDeclaredConstructor(types);
                for(int i=fields.length-1;i>=0;i--){
                    arg[0]=fields[i].getName();
                    arg[1]=fields[i].getType();
                    translation[i]=constructor.newInstance(arg);
                }
                translatedFields.put(fields,translation);
            }
            return (Object[])translation;
        }catch(Throwable t){
            throw new NoSuchFieldException();
        }
    }

    private static long computeStructuralUID(ObjectStreamClass_1_3_1 osc,Class<?> cl){
        ByteArrayOutputStream devnull=new ByteArrayOutputStream(512);
        long h=0;
        try{
            if((!Serializable.class.isAssignableFrom(cl))||
                    (cl.isInterface())){
                return 0;
            }
            if(Externalizable.class.isAssignableFrom(cl)){
                return 1;
            }
            MessageDigest md=MessageDigest.getInstance("SHA");
            DigestOutputStream mdo=new DigestOutputStream(devnull,md);
            DataOutputStream data=new DataOutputStream(mdo);
            // Get SUID of parent
            Class<?> parent=cl.getSuperclass();
            if((parent!=null))
            // SerialBug 1; acc. to spec the one for
            // java.lang.object
            // should be computed and put
            //     && (parent != java.lang.Object.class))
            {
                //data.writeLong(computeSerialVersionUID(null,parent));
                data.writeLong(computeStructuralUID(lookup(parent),parent));
            }
            if(osc.hasWriteObject())
                data.writeInt(2);
            else
                data.writeInt(1);
            /** Sort the field names to get a deterministic order */
            // Field[] field = ObjectStreamClass_1_3_1.getDeclaredFields(cl);
            ObjectStreamField[] fields=osc.getFields();
            // Must make sure that the Field array we allocate
            // below is exactly the right size.  Bug fix for
            // 4397133.
            int numNonNullFields=0;
            for(int i=0;i<fields.length;i++)
                if(fields[i].getField()!=null)
                    numNonNullFields++;
            Field[] field=new Field[numNonNullFields];
            for(int i=0, fieldNum=0;i<fields.length;i++){
                if(fields[i].getField()!=null){
                    field[fieldNum++]=fields[i].getField();
                }
            }
            if(field.length>1)
                Arrays.sort(field,compareMemberByName);
            for(int i=0;i<field.length;i++){
                Field f=field[i];
                /** Include in the hash all fields except those that are
                 * transient
                 */
                int m=f.getModifiers();
                //Serial 6
                //if (Modifier.isTransient(m) || Modifier.isStatic(m))
                // spec reference 00-01-06.pdf, 1.3.5.6, states non-static
                // non-transient, public fields are mapped to Java IDL.
                //
                // Here's the quote from the first paragraph:
                // Java non-static non-transient public fields are mapped to
                // OMG IDL public data members, and other Java fields are
                // not mapped.
                // if (Modifier.isTransient(m) || Modifier.isStatic(m))
                //     continue;
                data.writeUTF(f.getName());
                data.writeUTF(getSignature(f.getType()));
            }
            /** Compute the hash value for this class.
             * Use only the first 64 bits of the hash.
             */
            data.flush();
            byte hasharray[]=md.digest();
            // int minimum = Math.min(8, hasharray.length);
            // SerialBug 3: SHA computation is wrong; for loop reversed
            //for (int i = minimum; i > 0; i--)
            for(int i=0;i<Math.min(8,hasharray.length);i++){
                h+=(long)(hasharray[i]&255)<<(i*8);
            }
        }catch(IOException ignore){
            /** can't happen, but be deterministic anyway. */
            h=-1;
        }catch(NoSuchAlgorithmException complain){
            throw new SecurityException(complain.getMessage());
        }
        return h;
    }

    static String getSignature(Class<?> clazz){
        String type=null;
        if(clazz.isArray()){
            Class<?> cl=clazz;
            int dimensions=0;
            while(cl.isArray()){
                dimensions++;
                cl=cl.getComponentType();
            }
            StringBuffer sb=new StringBuffer();
            for(int i=0;i<dimensions;i++){
                sb.append("[");
            }
            sb.append(getSignature(cl));
            type=sb.toString();
        }else if(clazz.isPrimitive()){
            if(clazz==Integer.TYPE){
                type="I";
            }else if(clazz==Byte.TYPE){
                type="B";
            }else if(clazz==Long.TYPE){
                type="J";
            }else if(clazz==Float.TYPE){
                type="F";
            }else if(clazz==Double.TYPE){
                type="D";
            }else if(clazz==Short.TYPE){
                type="S";
            }else if(clazz==Character.TYPE){
                type="C";
            }else if(clazz==Boolean.TYPE){
                type="Z";
            }else if(clazz==Void.TYPE){
                type="V";
            }
        }else{
            type="L"+clazz.getName().replace('.','/')+";";
        }
        return type;
    }

    static String getSignature(Method meth){
        StringBuffer sb=new StringBuffer();
        sb.append("(");
        Class<?>[] params=meth.getParameterTypes(); // avoid clone
        for(int j=0;j<params.length;j++){
            sb.append(getSignature(params[j]));
        }
        sb.append(")");
        sb.append(getSignature(meth.getReturnType()));
        return sb.toString();
    }

    static String getSignature(Constructor cons){
        StringBuffer sb=new StringBuffer();
        sb.append("(");
        Class<?>[] params=cons.getParameterTypes(); // avoid clone
        for(int j=0;j<params.length;j++){
            sb.append(getSignature(params[j]));
        }
        sb.append(")V");
        return sb.toString();
    }

    private static Field[] getDeclaredFields(final Class clz){
        return (Field[])AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return clz.getDeclaredFields();
            }
        });
    }

    public final String getName(){
        return name;
    }

    public final long getSerialVersionUID(){
        return suid;
    }

    public final String getSerialVersionUIDStr(){
        if(suidStr==null)
            suidStr=Long.toHexString(suid).toUpperCase();
        return suidStr;
    }

    public final long getActualSerialVersionUID(){
        return actualSuid;
    }

    public final String getActualSerialVersionUIDStr(){
        if(actualSuidStr==null)
            actualSuidStr=Long.toHexString(actualSuid).toUpperCase();
        return actualSuidStr;
    }

    public final Class<?> forClass(){
        return ofClass;
    }

    public ObjectStreamField[] getFields(){
        // Return a copy so the caller can't change the fields.
        if(fields.length>0){
            ObjectStreamField[] dup=new ObjectStreamField[fields.length];
            System.arraycopy(fields,0,dup,0,fields.length);
            return dup;
        }else{
            return fields;
        }
    }

    public boolean hasField(ValueMember field){
        for(int i=0;i<fields.length;i++){
            try{
                if(fields[i].getName().equals(field.name)){
                    if(fields[i].getSignature().equals(ValueUtility.getSignature(field)))
                        return true;
                }
            }catch(Throwable t){
            }
        }
        return false;
    }

    final ObjectStreamField[] getFieldsNoCopy(){
        return fields;
    }

    public final ObjectStreamField getField(String name){
        /** Binary search of fields by name.
         */
        for(int i=fields.length-1;i>=0;i--){
            if(name.equals(fields[i].getName())){
                return fields[i];
            }
        }
        return null;
    }

    public Serializable writeReplace(Serializable value){
        if(writeReplaceObjectMethod!=null){
            try{
                return (Serializable)writeReplaceObjectMethod.invoke(value,noArgsList);
            }catch(Throwable t){
                throw new RuntimeException(t.getMessage());
            }
        }else return value;
    }

    public Object readResolve(Object value){
        if(readResolveObjectMethod!=null){
            try{
                return readResolveObjectMethod.invoke(value,noArgsList);
            }catch(Throwable t){
                throw new RuntimeException(t.getMessage());
            }
        }else return value;
    }

    public final String toString(){
        StringBuffer sb=new StringBuffer();
        sb.append(name);
        sb.append(": static final long serialVersionUID = ");
        sb.append(Long.toString(suid));
        sb.append("L;");
        return sb.toString();
    }

    private void init(){
        synchronized(lock){
            final Class<?> cl=ofClass;
            if(fields!=null) // already initialized
                return;
            if(!serializable||
                    externalizable||
                    forProxyClass||
                    name.equals("java.lang.String")){
                fields=NO_FIELDS;
            }else if(serializable){
                /** Ask for permission to override field access checks.
                 */
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        /** Fill in the list of persistent fields.
                         * If it is declared, use the declared serialPersistentFields.
                         * Otherwise, extract the fields from the class itself.
                         */
                        try{
                            Field pf=cl.getDeclaredField("serialPersistentFields");
                            // serial bug 7; the serialPersistentFields were not
                            // being read and stored as Accessible bit was not set
                            pf.setAccessible(true);
                            // serial bug 7; need to find if the field is of type
                            // java.io.ObjectStreamField
                            java.io.ObjectStreamField[] f=
                                    (java.io.ObjectStreamField[])pf.get(cl);
                            int mods=pf.getModifiers();
                            if((Modifier.isPrivate(mods))&&
                                    (Modifier.isStatic(mods))&&
                                    (Modifier.isFinal(mods))){
                                fields=(ObjectStreamField[])translateFields((Object[])pf.get(cl));
                            }
                        }catch(NoSuchFieldException e){
                            fields=null;
                        }catch(IllegalAccessException e){
                            fields=null;
                        }catch(IllegalArgumentException e){
                            fields=null;
                        }catch(ClassCastException e){
                            /** Thrown if a field serialPersistentField exists
                             * but it is not of type ObjectStreamField.
                             */
                            fields=null;
                        }
                        if(fields==null){
                            /** Get all of the declared fields for this
                             * Class. setAccessible on all fields so they
                             * can be accessed later.  Create a temporary
                             * ObjectStreamField array to hold each
                             * non-static, non-transient field. Then copy the
                             * temporary array into an array of the correct
                             * size once the number of fields is known.
                             */
                            Field[] actualfields=cl.getDeclaredFields();
                            int numFields=0;
                            ObjectStreamField[] tempFields=
                                    new ObjectStreamField[actualfields.length];
                            for(int i=0;i<actualfields.length;i++){
                                int modifiers=actualfields[i].getModifiers();
                                if(!Modifier.isStatic(modifiers)&&
                                        !Modifier.isTransient(modifiers)){
                                    tempFields[numFields++]=
                                            new ObjectStreamField(actualfields[i]);
                                }
                            }
                            fields=new ObjectStreamField[numFields];
                            System.arraycopy(tempFields,0,fields,0,numFields);
                        }else{
                            // For each declared persistent field, look for an actual
                            // reflected Field. If there is one, make sure it's the correct
                            // type and cache it in the ObjectStreamClass_1_3_1 for that field.
                            for(int j=fields.length-1;j>=0;j--){
                                try{
                                    Field reflField=cl.getDeclaredField(fields[j].getName());
                                    if(fields[j].getType()==reflField.getType()){
                                        // reflField.setAccessible(true);
                                        fields[j].setField(reflField);
                                    }
                                }catch(NoSuchFieldException e){
                                    // Nothing to do
                                }
                            }
                        }
                        return null;
                    }
                });
                if(fields.length>1)
                    Arrays.sort(fields);
                /** Set up field data for use while writing using the API api. */
                computeFieldInfo();
            }
            /** Get the serialVersionUID from the class.
             * It uses the access override mechanism so make sure
             * the field objects is only used here.
             *
             * NonSerializable classes have a serialVerisonUID of 0L.
             */
            if(isNonSerializable()){
                suid=0L;
            }else{
                // Lookup special Serializable members using reflection.
                AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        if(forProxyClass){
                            // proxy classes always have serialVersionUID of 0L
                            suid=0L;
                        }else{
                            try{
                                final Field f=cl.getDeclaredField("serialVersionUID");
                                int mods=f.getModifiers();
                                // SerialBug 5:  static final SUID should be read
                                if(Modifier.isStatic(mods)&&
                                        Modifier.isFinal(mods)){
                                    f.setAccessible(true);
                                    suid=f.getLong(cl);
                                    // get rid of native code
                                    // suid = getSerialVersionUIDField(cl);
                                    // SerialBug 2: should be computed after writeObject
                                    // actualSuid = computeStructuralUID(cl);
                                }else{
                                    suid=ObjectStreamClass.getSerialVersionUID(cl);
                                    // SerialBug 2: should be computed after writeObject
                                    // actualSuid = computeStructuralUID(cl);
                                }
                            }catch(NoSuchFieldException ex){
                                suid=ObjectStreamClass.getSerialVersionUID(cl);
                                // SerialBug 2: should be computed after writeObject
                                // actualSuid = computeStructuralUID(cl);
                            }catch(IllegalAccessException ex){
                                suid=ObjectStreamClass.getSerialVersionUID(cl);
                            }
                        }
                        try{
                            writeReplaceObjectMethod=cl.getDeclaredMethod("writeReplace",noTypesList);
                            if(Modifier.isStatic(writeReplaceObjectMethod.getModifiers())){
                                writeReplaceObjectMethod=null;
                            }else{
                                writeReplaceObjectMethod.setAccessible(true);
                            }
                        }catch(NoSuchMethodException e2){
                        }
                        try{
                            readResolveObjectMethod=cl.getDeclaredMethod("readResolve",noTypesList);
                            if(Modifier.isStatic(readResolveObjectMethod.getModifiers())){
                                readResolveObjectMethod=null;
                            }else{
                                readResolveObjectMethod.setAccessible(true);
                            }
                        }catch(NoSuchMethodException e2){
                        }
                        /** Cache lookup of writeObject and readObject for
                         * Serializable classes. (Do not lookup for
                         * Externalizable)
                         */
                        if(serializable&&!forProxyClass){
                            /** Look for the writeObject method
                             * Set the accessible flag on it here. ObjectOutputStream
                             * will call it as necessary.
                             */
                            try{
                                Class<?>[] args={java.io.ObjectOutputStream.class};
                                writeObjectMethod=cl.getDeclaredMethod("writeObject",args);
                                hasWriteObjectMethod=true;
                                int mods=writeObjectMethod.getModifiers();
                                // Method must be private and non-static
                                if(!Modifier.isPrivate(mods)||
                                        Modifier.isStatic(mods)){
                                    writeObjectMethod=null;
                                    hasWriteObjectMethod=false;
                                }
                            }catch(NoSuchMethodException e){
                            }
                            /** Look for the readObject method
                             * set the access override and save the reference for
                             * ObjectInputStream so it can all the method directly.
                             */
                            try{
                                Class<?>[] args={java.io.ObjectInputStream.class};
                                readObjectMethod=cl.getDeclaredMethod("readObject",args);
                                int mods=readObjectMethod.getModifiers();
                                // Method must be private and non-static
                                if(!Modifier.isPrivate(mods)||
                                        Modifier.isStatic(mods)){
                                    readObjectMethod=null;
                                }
                            }catch(NoSuchMethodException e){
                            }
                            // Compute the structural UID.  This must be done after the
                            // calculation for writeObject.  Fixed 4/20/2000, eea1
                            // SerialBug 2: to have correct value in RepId
                        }
                        return null;
                    }
                });
            }
            actualSuid=computeStructuralUID(this,cl);
        }
    }

    final boolean typeEquals(ObjectStreamClass_1_3_1 other){
        return (suid==other.suid)&&
                compareClassNames(name,other.name,'.');
    }

    static boolean compareClassNames(String streamName,
                                     String localName,
                                     char pkgSeparator){
        /** compare the class names, stripping off package names. */
        int streamNameIndex=streamName.lastIndexOf(pkgSeparator);
        if(streamNameIndex<0)
            streamNameIndex=0;
        int localNameIndex=localName.lastIndexOf(pkgSeparator);
        if(localNameIndex<0)
            localNameIndex=0;
        return streamName.regionMatches(false,streamNameIndex,
                localName,localNameIndex,
                streamName.length()-streamNameIndex);
    }

    final ObjectStreamClass_1_3_1 getSuperclass(){
        return superclass;
    }

    final void setSuperclass(ObjectStreamClass_1_3_1 s){
        superclass=s;
    }

    final boolean isCustomMarshaled(){
        return (hasWriteObject()||isExternalizable());
    }

    final boolean hasWriteObject(){
        return hasWriteObjectMethod;
    }

    boolean isExternalizable(){
        return externalizable;
    }

    boolean hasExternalizableBlockDataMode(){
        return hasExternalizableBlockData;
    }

    final ObjectStreamClass_1_3_1 localClassDescriptor(){
        return localClassDesc;
    }

    boolean isSerializable(){
        return serializable;
    }

    boolean isNonSerializable(){
        return !(externalizable||serializable);
    }

    private void computeFieldInfo(){
        primBytes=0;
        objFields=0;
        for(int i=0;i<fields.length;i++){
            switch(fields[i].getTypeCode()){
                case 'B':
                case 'Z':
                    primBytes+=1;
                    break;
                case 'C':
                case 'S':
                    primBytes+=2;
                    break;
                case 'I':
                case 'F':
                    primBytes+=4;
                    break;
                case 'J':
                case 'D':
                    primBytes+=8;
                    break;
                case 'L':
                case '[':
                    objFields+=1;
                    break;
            }
        }
    }

    private static class ObjectStreamClassEntry // extends java.lang.ref.SoftReference
    {
        ObjectStreamClassEntry next;
        private ObjectStreamClass_1_3_1 c;

        ObjectStreamClassEntry(ObjectStreamClass_1_3_1 c){
            //super(c);
            this.c=c;
        }

        public Object get(){
            return c;
        }
    }

    private static class CompareClassByName implements Comparator{
        public int compare(Object o1,Object o2){
            Class<?> c1=(Class)o1;
            Class<?> c2=(Class)o2;
            return (c1.getName()).compareTo(c2.getName());
        }
    }

    private static class CompareMemberByName implements Comparator{
        public int compare(Object o1,Object o2){
            String s1=((Member)o1).getName();
            String s2=((Member)o2).getName();
            if(o1 instanceof Method){
                s1+=getSignature((Method)o1);
                s2+=getSignature((Method)o2);
            }else if(o1 instanceof Constructor){
                s1+=getSignature((Constructor)o1);
                s2+=getSignature((Constructor)o2);
            }
            return s1.compareTo(s2);
        }
    }

    private static class MethodSignature implements Comparator{
        Member member;
        String signature;      // cached parameter signature

        private MethodSignature(Member m){
            member=m;
            if(isConstructor()){
                signature=ObjectStreamClass_1_3_1.getSignature((Constructor)m);
            }else{
                signature=ObjectStreamClass_1_3_1.getSignature((Method)m);
            }
        }

        /** Given an array of Method or Constructor members,
         return a sorted array of the non-private members.*/
        static MethodSignature[] removePrivateAndSort(Member[] m){
            int numNonPrivate=0;
            for(int i=0;i<m.length;i++){
                if(!Modifier.isPrivate(m[i].getModifiers())){
                    numNonPrivate++;
                }
            }
            MethodSignature[] cm=new MethodSignature[numNonPrivate];
            int cmi=0;
            for(int i=0;i<m.length;i++){
                if(!Modifier.isPrivate(m[i].getModifiers())){
                    cm[cmi]=new MethodSignature(m[i]);
                    cmi++;
                }
            }
            if(cmi>0)
                Arrays.sort(cm,cm[0]);
            return cm;
        }        public int compare(Object o1,Object o2){
            /** Arrays.sort calls compare when o1 and o2 are equal.*/
            if(o1==o2)
                return 0;
            MethodSignature c1=(MethodSignature)o1;
            MethodSignature c2=(MethodSignature)o2;
            int result;
            if(isConstructor()){
                result=c1.signature.compareTo(c2.signature);
            }else{ // is a Method.
                result=c1.member.getName().compareTo(c2.member.getName());
                if(result==0)
                    result=c1.signature.compareTo(c2.signature);
            }
            return result;
        }

        final private boolean isConstructor(){
            return member instanceof Constructor;
        }


    }
}
