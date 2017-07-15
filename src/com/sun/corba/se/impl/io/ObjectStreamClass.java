/**
 * Copyright (c) 1998, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright IBM Corp. 1998 2012  All Rights Reserved
 */
/**
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 2012  All Rights Reserved
 *
 */
package com.sun.corba.se.impl.io;

import com.sun.corba.se.impl.util.RepositoryId;
import org.omg.CORBA.ValueMember;
import sun.corba.Bridge;

import java.io.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.Arrays;
import java.util.Comparator;

public class ObjectStreamClass implements Serializable{
    public static final long kDefaultUID=-1;
    public static final int CLASS_MASK=Modifier.PUBLIC|Modifier.FINAL|
            Modifier.INTERFACE|Modifier.ABSTRACT;
    public static final int FIELD_MASK=Modifier.PUBLIC|Modifier.PRIVATE|
            Modifier.PROTECTED|Modifier.STATIC|Modifier.FINAL|
            Modifier.TRANSIENT|Modifier.VOLATILE;
    public static final int METHOD_MASK=Modifier.PUBLIC|Modifier.PRIVATE|
            Modifier.PROTECTED|Modifier.STATIC|Modifier.FINAL|
            Modifier.SYNCHRONIZED|Modifier.NATIVE|Modifier.ABSTRACT|
            Modifier.STRICT;
    public static final ObjectStreamField[] NO_FIELDS=
            new ObjectStreamField[0];
    private static final boolean DEBUG_SVUID=false;
    private static final Bridge bridge=
            AccessController.doPrivileged(
                    new PrivilegedAction<Bridge>(){
                        public Bridge run(){
                            return Bridge.get();
                        }
                    }
            );
    private static final PersistentFieldsValue persistentFieldsValue=
            new PersistentFieldsValue();
    private static final long serialVersionUID=-6120832682080437368L;
    private final static Comparator compareObjStrFieldsByName
            =new CompareObjStrFieldsByName();
    private static Object noArgsList[]={};
    private static Class<?> noTypesList[]={};
    static private ObjectStreamClassEntry[] descriptorFor=new ObjectStreamClassEntry[61];
    private static Method hasStaticInitializerMethod=null;
    private static Comparator compareClassByName=
            new CompareClassByName();
    private static Comparator compareMemberByName=
            new CompareMemberByName();
    boolean forProxyClass;
    int primBytes;
    int objFields;
    Method writeObjectMethod;
    Method readObjectMethod;
    private boolean isEnum;
    private String name;
    private ObjectStreamClass superclass;
    private boolean serializable;
    private boolean externalizable;
    private ObjectStreamField[] fields;
    private Class<?> ofClass;
    private long suid=kDefaultUID;
    private String suidStr=null;
    private long actualSuid=kDefaultUID;
    private String actualSuidStr=null;
    private boolean initialized=false;
    private Object lock=new Object();
    private boolean hasExternalizableBlockData;
    private transient Method writeReplaceObjectMethod;
    private transient Method readResolveObjectMethod;
    private Constructor cons;
    private String rmiiiopOptionalDataRepId=null;
    private ObjectStreamClass localClassDesc;

    private ObjectStreamClass(Class<?> cl,ObjectStreamClass superdesc,
                              boolean serial,boolean extern){
        ofClass=cl;           /** created from this class */
        if(Proxy.isProxyClass(cl)){
            forProxyClass=true;
        }
        name=cl.getName();
        isEnum=Enum.class.isAssignableFrom(cl);
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

    private static void insertDescriptorFor(ObjectStreamClass desc){
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

    private static ObjectStreamClass findDescriptorFor(Class<?> cl){
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
            ObjectStreamClass desc=(ObjectStreamClass)(e.get());
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

    ObjectStreamClass(String n,long s){
        name=n;
        suid=s;
        superclass=null;
    }

    static final ObjectStreamClass lookup(Class<?> cl){
        ObjectStreamClass desc=lookupInternal(cl);
        if(desc.isSerializable()||desc.isExternalizable())
            return desc;
        return null;
    }

    static ObjectStreamClass lookupInternal(Class<?> cl){
        /** Synchronize on the hashtable so no two threads will do
         * this at the same time.
         */
        ObjectStreamClass desc=null;
        synchronized(descriptorFor){
            /** Find the matching descriptor if it already known */
            desc=findDescriptorFor(cl);
            if(desc==null){
                /** Check if it's serializable */
                boolean serializable=Serializable.class.isAssignableFrom(cl);
                /** If the class is only Serializable,
                 * lookup the descriptor for the superclass.
                 */
                ObjectStreamClass superdesc=null;
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
                desc=new ObjectStreamClass(cl,superdesc,
                        serializable,externalizable);
            }
            // Must always call init.  See bug 4488137.  This code was
            // incorrectly changed to return immediately on a non-null
            // cache result.  That allowed threads to gain access to
            // unintialized instances.
            //
            // History: Note, the following init() call was originally within
            // the synchronization block, as it currently is now. Later, the
            // init() call was moved outside the synchronization block, and
            // the init() method used a private member variable lock, to
            // avoid performance problems. See bug 4165204. But that lead to
            // a deadlock situation, see bug 5104239. Hence, the init() method
            // has now been moved back into the synchronization block. The
            // right approach to solving these problems would be to rewrite
            // this class, based on the latest java.io.ObjectStreamClass.
            desc.init();
        }
        return desc;
    }

    public static final long getSerialVersionUID(Class<?> clazz){
        ObjectStreamClass theosc=ObjectStreamClass.lookup(clazz);
        if(theosc!=null){
            return theosc.getSerialVersionUID();
        }
        return 0;
    }

    public static final long getActualSerialVersionUID(Class<?> clazz){
        ObjectStreamClass theosc=ObjectStreamClass.lookup(clazz);
        if(theosc!=null){
            return theosc.getActualSerialVersionUID();
        }
        return 0;
    }

    private static Method getPrivateMethod(Class<?> cl,String name,
                                           Class<?>[] argTypes,
                                           Class<?> returnType){
        try{
            Method meth=cl.getDeclaredMethod(name,argTypes);
            meth.setAccessible(true);
            int mods=meth.getModifiers();
            return ((meth.getReturnType()==returnType)&&
                    ((mods&Modifier.STATIC)==0)&&
                    ((mods&Modifier.PRIVATE)!=0))?meth:null;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    private static Constructor getExternalizableConstructor(Class<?> cl){
        try{
            Constructor cons=cl.getDeclaredConstructor(new Class<?>[0]);
            cons.setAccessible(true);
            return ((cons.getModifiers()&Modifier.PUBLIC)!=0)?
                    cons:null;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    private static Constructor getSerializableConstructor(Class<?> cl){
        Class<?> initCl=cl;
        while(Serializable.class.isAssignableFrom(initCl)){
            if((initCl=initCl.getSuperclass())==null){
                return null;
            }
        }
        try{
            Constructor cons=initCl.getDeclaredConstructor(new Class<?>[0]);
            int mods=cons.getModifiers();
            if((mods&Modifier.PRIVATE)!=0||
                    ((mods&(Modifier.PUBLIC|Modifier.PROTECTED))==0&&
                            !packageEquals(cl,initCl))){
                return null;
            }
            cons=bridge.newConstructorForSerialization(cl,cons);
            cons.setAccessible(true);
            return cons;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    private static void msg(String str){
        System.out.println(str);
    }

    private static long _computeSerialVersionUID(Class<?> cl){
        if(DEBUG_SVUID)
            msg("Computing SerialVersionUID for "+cl);
        ByteArrayOutputStream devnull=new ByteArrayOutputStream(512);
        long h=0;
        try{
            MessageDigest md=MessageDigest.getInstance("SHA");
            DigestOutputStream mdo=new DigestOutputStream(devnull,md);
            DataOutputStream data=new DataOutputStream(mdo);
            if(DEBUG_SVUID)
                msg("\twriteUTF( \""+cl.getName()+"\" )");
            data.writeUTF(cl.getName());
            int classaccess=cl.getModifiers();
            classaccess&=(Modifier.PUBLIC|Modifier.FINAL|
                    Modifier.INTERFACE|Modifier.ABSTRACT);
            /** Workaround for javac bug that only set ABSTRACT for
             * interfaces if the interface had some methods.
             * The ABSTRACT bit reflects that the number of methods > 0.
             * This is required so correct hashes can be computed
             * for existing class files.
             * Previously this hack was previously present in the VM.
             */
            Method[] method=cl.getDeclaredMethods();
            if((classaccess&Modifier.INTERFACE)!=0){
                classaccess&=(~Modifier.ABSTRACT);
                if(method.length>0){
                    classaccess|=Modifier.ABSTRACT;
                }
            }
            // Mask out any post-1.4 attributes
            classaccess&=CLASS_MASK;
            if(DEBUG_SVUID)
                msg("\twriteInt( "+classaccess+" ) ");
            data.writeInt(classaccess);
            /**
             * Get the list of interfaces supported,
             * Accumulate their names their names in Lexical order
             * and add them to the hash
             */
            if(!cl.isArray()){
                /** In 1.2fcs, getInterfaces() was modified to return
                 * {java.lang.Cloneable, java.io.Serializable} when
                 * called on array classes.  These values would upset
                 * the computation of the hash, so we explicitly omit
                 * them from its computation.
                 */
                Class<?> interfaces[]=cl.getInterfaces();
                Arrays.sort(interfaces,compareClassByName);
                for(int i=0;i<interfaces.length;i++){
                    if(DEBUG_SVUID)
                        msg("\twriteUTF( \""+interfaces[i].getName()+"\" ) ");
                    data.writeUTF(interfaces[i].getName());
                }
            }
            /** Sort the field names to get a deterministic order */
            Field[] field=cl.getDeclaredFields();
            Arrays.sort(field,compareMemberByName);
            for(int i=0;i<field.length;i++){
                Field f=field[i];
                /** Include in the hash all fields except those that are
                 * private transient and private static.
                 */
                int m=f.getModifiers();
                if(Modifier.isPrivate(m)&&
                        (Modifier.isTransient(m)||Modifier.isStatic(m)))
                    continue;
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \""+f.getName()+"\" ) ");
                data.writeUTF(f.getName());
                // Mask out any post-1.4 bits
                m&=FIELD_MASK;
                if(DEBUG_SVUID)
                    msg("\twriteInt( "+m+" ) ");
                data.writeInt(m);
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \""+getSignature(f.getType())+"\" ) ");
                data.writeUTF(getSignature(f.getType()));
            }
            if(hasStaticInitializer(cl)){
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \"<clinit>\" ) ");
                data.writeUTF("<clinit>");
                if(DEBUG_SVUID)
                    msg("\twriteInt( "+Modifier.STATIC+" )");
                data.writeInt(Modifier.STATIC); // TBD: what modifiers does it have
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \"()V\" )");
                data.writeUTF("()V");
            }
            /**
             * Get the list of constructors including name and signature
             * Sort lexically, add all except the private constructors
             * to the hash with their access flags
             */
            MethodSignature[] constructors=
                    MethodSignature.removePrivateAndSort(cl.getDeclaredConstructors());
            for(int i=0;i<constructors.length;i++){
                MethodSignature c=constructors[i];
                String mname="<init>";
                String desc=c.signature;
                desc=desc.replace('/','.');
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \""+mname+"\" )");
                data.writeUTF(mname);
                // mask out post-1.4 modifiers
                int modifier=c.member.getModifiers()&METHOD_MASK;
                if(DEBUG_SVUID)
                    msg("\twriteInt( "+modifier+" ) ");
                data.writeInt(modifier);
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \""+desc+"\" )");
                data.writeUTF(desc);
            }
            /** Include in the hash all methods except those that are
             * private transient and private static.
             */
            MethodSignature[] methods=
                    MethodSignature.removePrivateAndSort(method);
            for(int i=0;i<methods.length;i++){
                MethodSignature m=methods[i];
                String desc=m.signature;
                desc=desc.replace('/','.');
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \""+m.member.getName()+"\" )");
                data.writeUTF(m.member.getName());
                // mask out post-1.4 modifiers
                int modifier=m.member.getModifiers()&METHOD_MASK;
                if(DEBUG_SVUID)
                    msg("\twriteInt( "+modifier+" ) ");
                data.writeInt(modifier);
                if(DEBUG_SVUID)
                    msg("\twriteUTF( \""+desc+"\" )");
                data.writeUTF(desc);
            }
            /** Compute the hash value for this class.
             * Use only the first 64 bits of the hash.
             */
            data.flush();
            byte hasharray[]=md.digest();
            for(int i=0;i<Math.min(8,hasharray.length);i++){
                h+=(long)(hasharray[i]&255)<<(i*8);
            }
        }catch(IOException ignore){
            /** can't happen, but be deterministic anyway. */
            h=-1;
        }catch(NoSuchAlgorithmException complain){
            SecurityException se=new SecurityException();
            se.initCause(complain);
            throw se;
        }
        return h;
    }

    private static long computeStructuralUID(ObjectStreamClass osc,Class<?> cl){
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
            // CORBA formal 00-11-03 10.6.2:  For each field of the
            // class that is mapped to IDL, sorted lexicographically
            // by Java field name, in increasing order...
            ObjectStreamField[] field=osc.getFields();
            if(field.length>1){
                Arrays.sort(field,compareObjStrFieldsByName);
            }
            // ...Java field name in UTF encoding, field
            // descriptor, as defined by the JVM spec...
            for(int i=0;i<field.length;i++){
                data.writeUTF(field[i].getName());
                data.writeUTF(field[i].getSignature());
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
            SecurityException se=new SecurityException();
            se.initCause(complain);
            throw se;
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

    private static Field[] getDeclaredFields(final Class<?> clz){
        return (Field[])AccessController.doPrivileged(new PrivilegedAction(){
            public Object run(){
                return clz.getDeclaredFields();
            }
        });
    }

    private static boolean hasStaticInitializer(Class<?> cl){
        if(hasStaticInitializerMethod==null){
            Class<?> classWithThisMethod=null;
            try{
                if(classWithThisMethod==null)
                    classWithThisMethod=java.io.ObjectStreamClass.class;
                hasStaticInitializerMethod=
                        classWithThisMethod.getDeclaredMethod("hasStaticInitializer",
                                new Class<?>[]{Class.class});
            }catch(NoSuchMethodException ex){
            }
            if(hasStaticInitializerMethod==null){
                // XXX I18N, logging needed
                throw new InternalError("Can't find hasStaticInitializer method on "
                        +classWithThisMethod.getName());
            }
            hasStaticInitializerMethod.setAccessible(true);
        }
        try{
            Boolean retval=(Boolean)
                    hasStaticInitializerMethod.invoke(null,new Object[]{cl});
            return retval.booleanValue();
        }catch(Exception ex){
            // XXX I18N, logging needed
            InternalError ie=new InternalError("Error invoking hasStaticInitializer");
            ie.initCause(ex);
            throw ie;
        }
    }

    private static Method getInheritableMethod(Class<?> cl,String name,
                                               Class<?>[] argTypes,
                                               Class<?> returnType){
        Method meth=null;
        Class<?> defCl=cl;
        while(defCl!=null){
            try{
                meth=defCl.getDeclaredMethod(name,argTypes);
                break;
            }catch(NoSuchMethodException ex){
                defCl=defCl.getSuperclass();
            }
        }
        if((meth==null)||(meth.getReturnType()!=returnType)){
            return null;
        }
        meth.setAccessible(true);
        int mods=meth.getModifiers();
        if((mods&(Modifier.STATIC|Modifier.ABSTRACT))!=0){
            return null;
        }else if((mods&(Modifier.PUBLIC|Modifier.PROTECTED))!=0){
            return meth;
        }else if((mods&Modifier.PRIVATE)!=0){
            return (cl==defCl)?meth:null;
        }else{
            return packageEquals(cl,defCl)?meth:null;
        }
    }

    private static boolean packageEquals(Class<?> cl1,Class<?> cl2){
        Package pkg1=cl1.getPackage(), pkg2=cl2.getPackage();
        return ((pkg1==pkg2)||((pkg1!=null)&&(pkg1.equals(pkg2))));
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
        try{
            for(int i=0;i<fields.length;i++){
                if(fields[i].getName().equals(field.name)){
                    if(fields[i].getSignature().equals(
                            ValueUtility.getSignature(field)))
                        return true;
                }
            }
        }catch(Exception exc){
            // Ignore this; all we want to do is return false
            // Note that ValueUtility.getSignature can throw checked exceptions.
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
                throw new RuntimeException(t);
            }
        }else return value;
    }

    public Object readResolve(Object value){
        if(readResolveObjectMethod!=null){
            try{
                return readResolveObjectMethod.invoke(value,noArgsList);
            }catch(Throwable t){
                throw new RuntimeException(t);
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
            // See description at definition of initialized.
            if(initialized)
                return;
            final Class<?> cl=ofClass;
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
                        fields=persistentFieldsValue.get(cl);
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
                                Field fld=actualfields[i];
                                int modifiers=fld.getModifiers();
                                if(!Modifier.isStatic(modifiers)&&
                                        !Modifier.isTransient(modifiers)){
                                    fld.setAccessible(true);
                                    tempFields[numFields++]=new ObjectStreamField(fld);
                                }
                            }
                            fields=new ObjectStreamField[numFields];
                            System.arraycopy(tempFields,0,fields,0,numFields);
                        }else{
                            // For each declared persistent field, look for an actual
                            // reflected Field. If there is one, make sure it's the correct
                            // type and cache it in the ObjectStreamClass for that field.
                            for(int j=fields.length-1;j>=0;j--){
                                try{
                                    Field reflField=cl.getDeclaredField(fields[j].getName());
                                    if(fields[j].getType()==reflField.getType()){
                                        reflField.setAccessible(true);
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
            if(isNonSerializable()||isEnum){
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
                                if(Modifier.isStatic(mods)&&Modifier.isFinal(mods)){
                                    f.setAccessible(true);
                                    suid=f.getLong(cl);
                                    // SerialBug 2: should be computed after writeObject
                                    // actualSuid = computeStructuralUID(cl);
                                }else{
                                    suid=_computeSerialVersionUID(cl);
                                    // SerialBug 2: should be computed after writeObject
                                    // actualSuid = computeStructuralUID(cl);
                                }
                            }catch(NoSuchFieldException ex){
                                suid=_computeSerialVersionUID(cl);
                                // SerialBug 2: should be computed after writeObject
                                // actualSuid = computeStructuralUID(cl);
                            }catch(IllegalAccessException ex){
                                suid=_computeSerialVersionUID(cl);
                            }
                        }
                        writeReplaceObjectMethod=ObjectStreamClass.getInheritableMethod(cl,
                                "writeReplace",noTypesList,Object.class);
                        readResolveObjectMethod=ObjectStreamClass.getInheritableMethod(cl,
                                "readResolve",noTypesList,Object.class);
                        if(externalizable)
                            cons=getExternalizableConstructor(cl);
                        else
                            cons=getSerializableConstructor(cl);
                        if(serializable&&!forProxyClass){
                            /** Look for the writeObject method
                             * Set the accessible flag on it here. ObjectOutputStream
                             * will call it as necessary.
                             */
                            writeObjectMethod=getPrivateMethod(cl,"writeObject",
                                    new Class<?>[]{java.io.ObjectOutputStream.class},Void.TYPE);
                            readObjectMethod=getPrivateMethod(cl,"readObject",
                                    new Class<?>[]{java.io.ObjectInputStream.class},Void.TYPE);
                        }
                        return null;
                    }
                });
            }
            // This call depends on a lot of information computed above!
            actualSuid=ObjectStreamClass.computeStructuralUID(this,cl);
            // If we have a write object method, precompute the
            // RMI-IIOP stream format version 2 optional data
            // repository ID.
            if(hasWriteObject())
                rmiiiopOptionalDataRepId=computeRMIIIOPOptionalDataRepId();
            // This must be done last.
            initialized=true;
        }
    }

    // Specific to RMI-IIOP
    private String computeRMIIIOPOptionalDataRepId(){
        StringBuffer sbuf=new StringBuffer("RMI:org.omg.custom.");
        sbuf.append(RepositoryId.convertToISOLatin1(this.getName()));
        sbuf.append(':');
        sbuf.append(this.getActualSerialVersionUIDStr());
        sbuf.append(':');
        sbuf.append(this.getSerialVersionUIDStr());
        return sbuf.toString();
    }

    public final String getRMIIIOPOptionalDataRepId(){
        return rmiiiopOptionalDataRepId;
    }

    final void setClass(Class<?> cl) throws InvalidClassException{
        if(cl==null){
            localClassDesc=null;
            ofClass=null;
            computeFieldInfo();
            return;
        }
        localClassDesc=lookupInternal(cl);
        if(localClassDesc==null)
            // XXX I18N, logging needed
            throw new InvalidClassException(cl.getName(),
                    "Local class not compatible");
        if(suid!=localClassDesc.suid){
            /** Check for exceptional cases that allow mismatched suid. */
            /** Allow adding Serializable or Externalizable
             * to a later release of the class.
             */
            boolean addedSerialOrExtern=
                    isNonSerializable()||localClassDesc.isNonSerializable();
            /** Disregard the serialVersionUID of an array
             * when name and cl.Name differ. If resolveClass() returns
             * an array with a different package name,
             * the serialVersionUIDs will not match since the fully
             * qualified array class is used in the
             * computation of the array's serialVersionUID. There is
             * no way to set a permanent serialVersionUID for an array type.
             */
            boolean arraySUID=(cl.isArray()&&!cl.getName().equals(name));
            if(!arraySUID&&!addedSerialOrExtern){
                // XXX I18N, logging needed
                throw new InvalidClassException(cl.getName(),
                        "Local class not compatible:"+
                                " stream classdesc serialVersionUID="+suid+
                                " local class serialVersionUID="+localClassDesc.suid);
            }
        }
        /** compare the class names, stripping off package names. */
        if(!compareClassNames(name,cl.getName(),'.'))
            // XXX I18N, logging needed
            throw new InvalidClassException(cl.getName(),
                    "Incompatible local class name. "+
                            "Expected class name compatible with "+
                            name);
        /**
         * Test that both implement either serializable or externalizable.
         */
        // The next check is more generic, since it covers the
        // Proxy case, the JDK 1.3 serialization code has
        // both checks
        //if ((serializable && localClassDesc.externalizable) ||
        //    (externalizable && localClassDesc.serializable))
        //    throw new InvalidClassException(localCl.getName(),
        //            "Serializable is incompatible with Externalizable");
        if((serializable!=localClassDesc.serializable)||
                (externalizable!=localClassDesc.externalizable)||
                (!serializable&&!externalizable))
            // XXX I18N, logging needed
            throw new InvalidClassException(cl.getName(),
                    "Serialization incompatible with Externalization");
        /** Set up the reflected Fields in the class where the value of each
         * field in this descriptor should be stored.
         * Each field in this ObjectStreamClass (the source) is located (by
         * name) in the ObjectStreamClass of the class(the destination).
         * In the usual (non-versioned case) the field is in both
         * descriptors and the types match, so the reflected Field is copied.
         * If the type does not match, a InvalidClass exception is thrown.
         * If the field is not present in the class, the reflected Field
         * remains null so the field will be read but discarded.
         * If extra fields are present in the class they are ignored. Their
         * values will be set to the default value by the object allocator.
         * Both the src and dest field list are sorted by type and name.
         */
        ObjectStreamField[] destfield=
                (ObjectStreamField[])localClassDesc.fields;
        ObjectStreamField[] srcfield=
                (ObjectStreamField[])fields;
        int j=0;
        nextsrc:
        for(int i=0;i<srcfield.length;i++){
            /** Find this field in the dest*/
            for(int k=j;k<destfield.length;k++){
                if(srcfield[i].getName().equals(destfield[k].getName())){
                    /** found match */
                    if(srcfield[i].isPrimitive()&&
                            !srcfield[i].typeEquals(destfield[k])){
                        // XXX I18N, logging needed
                        throw new InvalidClassException(cl.getName(),
                                "The type of field "+
                                        srcfield[i].getName()+
                                        " of class "+name+
                                        " is incompatible.");
                    }
                    /** Skip over any fields in the dest that are not in the src */
                    j=k;
                    srcfield[i].setField(destfield[j].getField());
                    // go on to the next source field
                    continue nextsrc;
                }
            }
        }
        /** Set up field data for use while reading from the input stream. */
        computeFieldInfo();
        /** Remember the class this represents */
        ofClass=cl;
        /** get the cache of these methods from the local class
         * implementation.
         */
        readObjectMethod=localClassDesc.readObjectMethod;
        readResolveObjectMethod=localClassDesc.readResolveObjectMethod;
    }

    final boolean typeEquals(ObjectStreamClass other){
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

    final ObjectStreamClass getSuperclass(){
        return superclass;
    }

    final void setSuperclass(ObjectStreamClass s){
        superclass=s;
    }

    final boolean hasReadObject(){
        return readObjectMethod!=null;
    }

    final boolean isCustomMarshaled(){
        return (hasWriteObject()||isExternalizable())
                ||(superclass!=null&&superclass.isCustomMarshaled());
    }

    final boolean hasWriteObject(){
        return writeObjectMethod!=null;
    }

    boolean isExternalizable(){
        return externalizable;
    }

    boolean hasExternalizableBlockDataMode(){
        return hasExternalizableBlockData;
    }

    Object newInstance()
            throws InstantiationException, InvocationTargetException,
            UnsupportedOperationException{
        if(cons!=null){
            try{
                return cons.newInstance(new Object[0]);
            }catch(IllegalAccessException ex){
                // should not occur, as access checks have been suppressed
                InternalError ie=new InternalError();
                ie.initCause(ex);
                throw ie;
            }
        }else{
            throw new UnsupportedOperationException();
        }
    }

    final ObjectStreamClass localClassDescriptor(){
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

    private static final class PersistentFieldsValue
            extends ClassValue<ObjectStreamField[]>{
        PersistentFieldsValue(){
        }

        protected ObjectStreamField[] computeValue(Class<?> type){
            try{
                Field pf=type.getDeclaredField("serialPersistentFields");
                int mods=pf.getModifiers();
                if(Modifier.isPrivate(mods)&&Modifier.isStatic(mods)&&
                        Modifier.isFinal(mods)){
                    pf.setAccessible(true);
                    java.io.ObjectStreamField[] fields=
                            (java.io.ObjectStreamField[])pf.get(type);
                    return translateFields(fields);
                }
            }catch(NoSuchFieldException|IllegalAccessException|
                    IllegalArgumentException|ClassCastException e){
            }
            return null;
        }

        private static ObjectStreamField[] translateFields(
                java.io.ObjectStreamField[] fields){
            ObjectStreamField[] translation=
                    new ObjectStreamField[fields.length];
            for(int i=0;i<fields.length;i++){
                translation[i]=new ObjectStreamField(fields[i].getName(),
                        fields[i].getType());
            }
            return translation;
        }
    }

    private static class ObjectStreamClassEntry // extends java.lang.ref.SoftReference
    {
        ObjectStreamClassEntry next;
        private ObjectStreamClass c;

        ObjectStreamClassEntry(ObjectStreamClass c){
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

    private static class CompareObjStrFieldsByName implements Comparator{
        public int compare(Object o1,Object o2){
            ObjectStreamField osf1=(ObjectStreamField)o1;
            ObjectStreamField osf2=(ObjectStreamField)o2;
            return osf1.getName().compareTo(osf2.getName());
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
                signature=ObjectStreamClass.getSignature((Constructor)m);
            }else{
                signature=ObjectStreamClass.getSignature((Method)m);
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
