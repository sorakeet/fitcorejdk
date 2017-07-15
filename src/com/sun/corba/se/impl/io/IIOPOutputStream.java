/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.se.impl.io;

import com.sun.corba.se.impl.logging.UtilSystemException;
import com.sun.corba.se.impl.util.RepositoryId;
import com.sun.corba.se.impl.util.Utility;
import com.sun.corba.se.spi.logging.CORBALogDomains;
import org.omg.CORBA.portable.OutputStream;
import sun.corba.Bridge;

import javax.rmi.CORBA.Util;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Stack;

public class IIOPOutputStream
        extends OutputStreamHook{
    private static Bridge bridge=
            (Bridge)AccessController.doPrivileged(
                    new PrivilegedAction(){
                        public Object run(){
                            return Bridge.get();
                        }
                    }
            );
    private UtilSystemException wrapper=UtilSystemException.get(
            CORBALogDomains.RPC_ENCODING);
    private org.omg.CORBA_2_3.portable.OutputStream orbStream;
    private Object currentObject=null;
    private ObjectStreamClass currentClassDesc=null;
    private int recursionDepth=0;
    private int simpleWriteDepth=0;
    private IOException abortIOException=null;
    private Stack classDescStack=new Stack();
    // Used when calling an object's writeObject method
    private Object[] writeObjectArgList={this};

    public IIOPOutputStream()
            throws IOException{
        super();
    }

    final void increaseRecursionDepth(){
        recursionDepth++;
    }

    final int decreaseRecursionDepth(){
        return --recursionDepth;
    }

    public final void writeObjectOverride(Object obj)
            throws IOException{
        writeObjectState.writeData(this);
        Util.writeAbstractObject((OutputStream)orbStream,obj);
    }

    public final void reset() throws IOException{
        try{
            //orbStream.reset();
            if(currentObject!=null||currentClassDesc!=null)
                // XXX I18N, Logging needed.
                throw new IOException("Illegal call to reset");
            abortIOException=null;
            if(classDescStack==null)
                classDescStack=new Stack();
            else
                classDescStack.setSize(0);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    protected final void annotateClass(Class<?> cl) throws IOException{
        // XXX I18N, Logging needed.
        throw new IOException("Method annotateClass not supported");
    }

    protected final Object replaceObject(Object obj) throws IOException{
        // XXX I18N, Logging needed.
        throw new IOException("Method replaceObject not supported");
    }

    protected final void writeStreamHeader() throws IOException{
        // no op
    }

    public final void write(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet((byte)(data&0xFF));
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void write(byte b[]) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet_array(b,0,b.length);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void write(byte b[],int off,int len) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet_array(b,off,len);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void flush() throws IOException{
        try{
            orbStream.flush();
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    protected final void drain() throws IOException{
        // no op
    }

    public final void close() throws IOException{
        // no op
    }

    public final void writeBoolean(boolean data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_boolean(data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeByte(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_octet((byte)data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeShort(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_short((short)data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeChar(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_wchar((char)data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeInt(int data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_long(data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeLong(long data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_longlong(data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeFloat(float data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_float(data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeDouble(double data) throws IOException{
        try{
            writeObjectState.writeData(this);
            orbStream.write_double(data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeBytes(String data) throws IOException{
        try{
            writeObjectState.writeData(this);
            byte buf[]=data.getBytes();
            orbStream.write_octet_array(buf,0,buf.length);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeChars(String data) throws IOException{
        try{
            writeObjectState.writeData(this);
            char buf[]=data.toCharArray();
            orbStream.write_wchar_array(buf,0,buf.length);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    public final void writeUTF(String data) throws IOException{
        try{
            writeObjectState.writeData(this);
            internalWriteUTF(orbStream,data);
        }catch(Error e){
            IOException ioexc=new IOException(e.getMessage());
            ioexc.initCause(e);
            throw ioexc;
        }
    }

    protected void internalWriteUTF(OutputStream stream,
                                    String data){
        stream.write_wstring(data);
    }

    public final void simpleWriteObject(Object obj,byte formatVersion)
    /** throws IOException */
    {
        byte oldStreamFormatVersion=streamFormatVersion;
        streamFormatVersion=formatVersion;
        Object prevObject=currentObject;
        ObjectStreamClass prevClassDesc=currentClassDesc;
        simpleWriteDepth++;
        try{
            // if (!checkSpecialClasses(obj) && !checkSubstitutableSpecialClasses(obj))
            outputObject(obj);
        }catch(IOException ee){
            if(abortIOException==null)
                abortIOException=ee;
        }finally{
            /** Restore state of previous call incase this is a nested call */
            streamFormatVersion=oldStreamFormatVersion;
            simpleWriteDepth--;
            currentObject=prevObject;
            currentClassDesc=prevClassDesc;
        }
        /** If the recursion depth is 0, test for and clear the pending exception.
         * If there is a pending exception throw it.
         */
        IOException pending=abortIOException;
        if(simpleWriteDepth==0)
            abortIOException=null;
        if(pending!=null){
            bridge.throwException(pending);
        }
    }

    private void outputObject(final Object obj) throws IOException{
        currentObject=obj;
        Class currclass=obj.getClass();
        /** Get the Class descriptor for this class,
         * Throw a NotSerializableException if there is none.
         */
        currentClassDesc=ObjectStreamClass.lookup(currclass);
        if(currentClassDesc==null){
            // XXX I18N, Logging needed.
            throw new NotSerializableException(currclass.getName());
        }
        /** If the object is externalizable,
         * call writeExternal.
         * else do Serializable processing.
         */
        if(currentClassDesc.isExternalizable()){
            // Write format version
            orbStream.write_octet(streamFormatVersion);
            Externalizable ext=(Externalizable)obj;
            ext.writeExternal(this);
        }else{
            /** The object's classes should be processed from supertype to subtype
             * Push all the clases of the current object onto a stack.
             * Remember the stack pointer where this set of classes is being pushed.
             */
            if(currentClassDesc.forClass().getName().equals("java.lang.String")){
                this.writeUTF((String)obj);
                return;
            }
            int stackMark=classDescStack.size();
            try{
                ObjectStreamClass next;
                while((next=currentClassDesc.getSuperclass())!=null){
                    classDescStack.push(currentClassDesc);
                    currentClassDesc=next;
                }
                /**
                 * For currentClassDesc and all the pushed class descriptors
                 *    If the class is writing its own data
                 *                set blockData = true; call the class writeObject method
                 *    If not
                 *     invoke either the defaultWriteObject method.
                 */
                do{
                    WriteObjectState oldState=writeObjectState;
                    try{
                        setState(NOT_IN_WRITE_OBJECT);
                        if(currentClassDesc.hasWriteObject()){
                            invokeObjectWriter(currentClassDesc,obj);
                        }else{
                            defaultWriteObjectDelegate();
                        }
                    }finally{
                        setState(oldState);
                    }
                }while(classDescStack.size()>stackMark&&
                        (currentClassDesc=(ObjectStreamClass)classDescStack.pop())!=null);
            }finally{
                classDescStack.setSize(stackMark);
            }
        }
    }

    private void invokeObjectWriter(ObjectStreamClass osc,Object obj)
            throws IOException{
        Class c=osc.forClass();
        try{
            // Write format version
            orbStream.write_octet(streamFormatVersion);
            writeObjectState.enterWriteObject(this);
            // writeObject(obj, c, this);
            osc.writeObjectMethod.invoke(obj,writeObjectArgList);
            writeObjectState.exitWriteObject(this);
        }catch(InvocationTargetException e){
            Throwable t=e.getTargetException();
            if(t instanceof IOException)
                throw (IOException)t;
            else if(t instanceof RuntimeException)
                throw (RuntimeException)t;
            else if(t instanceof Error)
                throw (Error)t;
            else
                // XXX I18N, Logging needed.
                throw new Error("invokeObjectWriter internal error",e);
        }catch(IllegalAccessException e){
            // cannot happen
        }
    }

    public final boolean enableReplaceObjectDelegate(boolean enable)
    /** throws SecurityException */
    {
        return false;
    }

    // INTERNAL UTILITY METHODS
    private boolean checkSpecialClasses(Object obj) throws IOException{
        /**
         * If this is a class, don't allow substitution
         */
        //if (obj instanceof Class) {
        //    throw new IOException("Serialization of Class not supported");
        //}
        if(obj instanceof ObjectStreamClass){
            // XXX I18N, Logging needed.
            throw new IOException("Serialization of ObjectStreamClass not supported");
        }
        return false;
    }

    private boolean checkSubstitutableSpecialClasses(Object obj)
            throws IOException{
        if(obj instanceof String){
            orbStream.write_value((Serializable)obj);
            return true;
        }
        //if (obj.getClass().isArray()) {
        //    outputArray(obj);
        //    return true;
        //}
        return false;
    }

    void writeField(ObjectStreamField field,Object value) throws IOException{
        switch(field.getTypeCode()){
            case 'B':
                if(value==null)
                    orbStream.write_octet((byte)0);
                else
                    orbStream.write_octet(((Byte)value).byteValue());
                break;
            case 'C':
                if(value==null)
                    orbStream.write_wchar((char)0);
                else
                    orbStream.write_wchar(((Character)value).charValue());
                break;
            case 'F':
                if(value==null)
                    orbStream.write_float((float)0);
                else
                    orbStream.write_float(((Float)value).floatValue());
                break;
            case 'D':
                if(value==null)
                    orbStream.write_double((double)0);
                else
                    orbStream.write_double(((Double)value).doubleValue());
                break;
            case 'I':
                if(value==null)
                    orbStream.write_long((int)0);
                else
                    orbStream.write_long(((Integer)value).intValue());
                break;
            case 'J':
                if(value==null)
                    orbStream.write_longlong((long)0);
                else
                    orbStream.write_longlong(((Long)value).longValue());
                break;
            case 'S':
                if(value==null)
                    orbStream.write_short((short)0);
                else
                    orbStream.write_short(((Short)value).shortValue());
                break;
            case 'Z':
                if(value==null)
                    orbStream.write_boolean(false);
                else
                    orbStream.write_boolean(((Boolean)value).booleanValue());
                break;
            case '[':
            case 'L':
                // What to do if it's null?
                writeObjectField(field,value);
                break;
            default:
                // XXX I18N, Logging needed.
                throw new InvalidClassException(currentClassDesc.getName());
        }
    }

    public final void defaultWriteObjectDelegate()
    /** throws IOException */
    {
        try{
            if(currentObject==null||currentClassDesc==null)
                // XXX I18N, Logging needed.
                throw new NotActiveException("defaultWriteObjectDelegate");
            ObjectStreamField[] fields=
                    currentClassDesc.getFieldsNoCopy();
            if(fields.length>0){
                outputClassFields(currentObject,currentClassDesc.forClass(),
                        fields);
            }
        }catch(IOException ioe){
            bridge.throwException(ioe);
        }
    }

    // Required by the superclass.
    ObjectStreamField[] getFieldsNoCopy(){
        return currentClassDesc.getFieldsNoCopy();
    }

    final org.omg.CORBA_2_3.portable.OutputStream getOrbStream(){
        return orbStream;
    }

    // If using RMI-IIOP stream format version 2, this tells
    // the ORB stream (which must be a ValueOutputStream) to
    // begin a new valuetype to contain the optional data
    // of the writeObject method.
    protected void beginOptionalCustomData(){
        if(streamFormatVersion==2){
            org.omg.CORBA.portable.ValueOutputStream vout
                    =(org.omg.CORBA.portable.ValueOutputStream)orbStream;
            vout.start_value(currentClassDesc.getRMIIIOPOptionalDataRepId());
        }
    }

    final void setOrbStream(org.omg.CORBA_2_3.portable.OutputStream os){
        orbStream=os;
    }

    private void outputClassFields(Object o,Class cl,
                                   ObjectStreamField[] fields)
            throws IOException, InvalidClassException{
        for(int i=0;i<fields.length;i++){
            if(fields[i].getField()==null)
                // XXX I18N, Logging needed.
                throw new InvalidClassException(cl.getName(),
                        "Nonexistent field "+fields[i].getName());
            try{
                switch(fields[i].getTypeCode()){
                    case 'B':
                        byte byteValue=fields[i].getField().getByte(o);
                        orbStream.write_octet(byteValue);
                        break;
                    case 'C':
                        char charValue=fields[i].getField().getChar(o);
                        orbStream.write_wchar(charValue);
                        break;
                    case 'F':
                        float floatValue=fields[i].getField().getFloat(o);
                        orbStream.write_float(floatValue);
                        break;
                    case 'D':
                        double doubleValue=fields[i].getField().getDouble(o);
                        orbStream.write_double(doubleValue);
                        break;
                    case 'I':
                        int intValue=fields[i].getField().getInt(o);
                        orbStream.write_long(intValue);
                        break;
                    case 'J':
                        long longValue=fields[i].getField().getLong(o);
                        orbStream.write_longlong(longValue);
                        break;
                    case 'S':
                        short shortValue=fields[i].getField().getShort(o);
                        orbStream.write_short(shortValue);
                        break;
                    case 'Z':
                        boolean booleanValue=fields[i].getField().getBoolean(o);
                        orbStream.write_boolean(booleanValue);
                        break;
                    case '[':
                    case 'L':
                        Object objectValue=fields[i].getField().get(o);
                        writeObjectField(fields[i],objectValue);
                        break;
                    default:
                        // XXX I18N, Logging needed.
                        throw new InvalidClassException(cl.getName());
                }
            }catch(IllegalAccessException exc){
                throw wrapper.illegalFieldAccess(exc,fields[i].getName());
            }
        }
    }

    private void writeObjectField(ObjectStreamField field,
                                  Object objectValue) throws IOException{
        if(ObjectStreamClassCorbaExt.isAny(field.getTypeString())){
            Util.writeAny(orbStream,objectValue);
        }else{
            Class type=field.getType();
            int callType=ValueHandlerImpl.kValueType;
            if(type.isInterface()){
                String className=type.getName();
                if(java.rmi.Remote.class.isAssignableFrom(type)){
                    // RMI Object reference...
                    callType=ValueHandlerImpl.kRemoteType;
                }else if(org.omg.CORBA.Object.class.isAssignableFrom(type)){
                    // IDL Object reference...
                    callType=ValueHandlerImpl.kRemoteType;
                }else if(RepositoryId.isAbstractBase(type)){
                    // IDL Abstract Object reference...
                    callType=ValueHandlerImpl.kAbstractType;
                }else if(ObjectStreamClassCorbaExt.isAbstractInterface(type)){
                    callType=ValueHandlerImpl.kAbstractType;
                }
            }
            switch(callType){
                case ValueHandlerImpl.kRemoteType:
                    Util.writeRemoteObject(orbStream,objectValue);
                    break;
                case ValueHandlerImpl.kAbstractType:
                    Util.writeAbstractObject(orbStream,objectValue);
                    break;
                case ValueHandlerImpl.kValueType:
                    try{
                        orbStream.write_value((Serializable)objectValue,type);
                    }catch(ClassCastException cce){
                        if(objectValue instanceof Serializable)
                            throw cce;
                        else
                            Utility.throwNotSerializableForCorba(objectValue.getClass().getName());
                    }
            }
        }
    }
}
