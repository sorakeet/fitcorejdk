/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.sql.rowset.serial;

import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;

import javax.sql.rowset.RowSetWarning;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Vector;

public class SerialJavaObject implements Serializable, Cloneable{
    static final long serialVersionUID=-1465795139032831023L;
    Vector<RowSetWarning> chain;
    private Object obj;
    private transient Field[] fields;

    public SerialJavaObject(Object obj) throws SerialException{
        // if any static fields are found, an exception
        // should be thrown
        // get Class. Object instance should always be available
        Class<?> c=obj.getClass();
        // determine if object implements Serializable i/f
        if(!(obj instanceof Serializable)){
            setWarning(new RowSetWarning("Warning, the object passed to the constructor does not implement Serializable"));
        }
        // can only determine public fields (obviously). If
        // any of these are static, this should invalidate
        // the action of attempting to persist these fields
        // in a serialized form
        fields=c.getFields();
        if(hasStaticFields(fields)){
            throw new SerialException("Located static fields in "+
                    "object instance. Cannot serialize");
        }
        this.obj=obj;
    }

    private void setWarning(RowSetWarning e){
        if(chain==null){
            chain=new Vector<>();
        }
        chain.add(e);
    }

    private static boolean hasStaticFields(Field[] fields){
        for(Field field : fields){
            if(field.getModifiers()==Modifier.STATIC){
                return true;
            }
        }
        return false;
    }

    public Object getObject() throws SerialException{
        return this.obj;
    }

    @CallerSensitive
    public Field[] getFields() throws SerialException{
        if(fields!=null){
            Class<?> c=this.obj.getClass();
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null){
                /**
                 * Check if the caller is allowed to access the specified class's package.
                 * If access is denied, throw a SecurityException.
                 */
                Class<?> caller=Reflection.getCallerClass();
                if(ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(),
                        c.getClassLoader())){
                    ReflectUtil.checkPackageAccess(c);
                }
            }
            return c.getFields();
        }else{
            throw new SerialException("SerialJavaObject does not contain"+
                    " a serialized object instance");
        }
    }

    public int hashCode(){
        return 31+obj.hashCode();
    }

    public boolean equals(Object o){
        if(this==o){
            return true;
        }
        if(o instanceof SerialJavaObject){
            SerialJavaObject sjo=(SerialJavaObject)o;
            return obj.equals(sjo.obj);
        }
        return false;
    }

    public Object clone(){
        try{
            SerialJavaObject sjo=(SerialJavaObject)super.clone();
            sjo.fields=Arrays.copyOf(fields,fields.length);
            if(chain!=null)
                sjo.chain=new Vector<>(chain);
            return sjo;
        }catch(CloneNotSupportedException ex){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }

    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException{
        ObjectInputStream.GetField fields1=s.readFields();
        @SuppressWarnings("unchecked")
        Vector<RowSetWarning> tmp=(Vector<RowSetWarning>)fields1.get("chain",null);
        if(tmp!=null)
            chain=new Vector<>(tmp);
        obj=fields1.get("obj",null);
        if(obj!=null){
            fields=obj.getClass().getFields();
            if(hasStaticFields(fields))
                throw new IOException("Located static fields in "+
                        "object instance. Cannot serialize");
        }else{
            throw new IOException("Object cannot be null!");
        }
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException{
        ObjectOutputStream.PutField fields=s.putFields();
        fields.put("obj",obj);
        fields.put("chain",chain);
        s.writeFields();
    }
}
