/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.io;

import sun.reflect.misc.ReflectUtil;

import java.io.ObjectStreamClass.WeakClassKey;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.io.ObjectStreamClass.processQueue;

public class ObjectOutputStream
        extends OutputStream implements ObjectOutput, ObjectStreamConstants{
    private static final boolean extendedDebugInfo=
            AccessController.doPrivileged(
                    new sun.security.action.GetBooleanAction(
                            "sun.io.serialization.extendedDebugInfo")).booleanValue();
    private final BlockDataOutputStream bout;
    private final HandleTable handles;
    private final ReplaceTable subs;
    private final boolean enableOverride;
    private final DebugTraceInfoStack debugInfoStack;
    private int protocol=PROTOCOL_VERSION_2;
    private int depth;
    private byte[] primVals;
    private boolean enableReplace;
    // values below valid only during upcalls to writeObject()/writeExternal()
    private SerialCallbackContext curContext;
    private PutFieldImpl curPut;

    public ObjectOutputStream(OutputStream out) throws IOException{
        verifySubclass();
        bout=new BlockDataOutputStream(out);
        handles=new HandleTable(10,(float)3.00);
        subs=new ReplaceTable(10,(float)3.00);
        enableOverride=false;
        writeStreamHeader();
        bout.setBlockDataMode(true);
        if(extendedDebugInfo){
            debugInfoStack=new DebugTraceInfoStack();
        }else{
            debugInfoStack=null;
        }
    }

    protected void writeStreamHeader() throws IOException{
        bout.writeShort(STREAM_MAGIC);
        bout.writeShort(STREAM_VERSION);
    }

    private void verifySubclass(){
        Class<?> cl=getClass();
        if(cl==ObjectOutputStream.class){
            return;
        }
        SecurityManager sm=System.getSecurityManager();
        if(sm==null){
            return;
        }
        processQueue(Caches.subclassAuditsQueue,Caches.subclassAudits);
        WeakClassKey key=new WeakClassKey(cl,Caches.subclassAuditsQueue);
        Boolean result=Caches.subclassAudits.get(key);
        if(result==null){
            result=Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key,result);
        }
        if(result.booleanValue()){
            return;
        }
        sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
    }

    private static boolean auditSubclass(final Class<?> subcl){
        Boolean result=AccessController.doPrivileged(
                new PrivilegedAction<Boolean>(){
                    public Boolean run(){
                        for(Class<?> cl=subcl;
                            cl!=ObjectOutputStream.class;
                            cl=cl.getSuperclass()){
                            try{
                                cl.getDeclaredMethod(
                                        "writeUnshared",new Class<?>[]{Object.class});
                                return Boolean.FALSE;
                            }catch(NoSuchMethodException ex){
                            }
                            try{
                                cl.getDeclaredMethod("putFields",(Class<?>[])null);
                                return Boolean.FALSE;
                            }catch(NoSuchMethodException ex){
                            }
                        }
                        return Boolean.TRUE;
                    }
                }
        );
        return result.booleanValue();
    }

    protected ObjectOutputStream() throws IOException, SecurityException{
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        bout=null;
        handles=null;
        subs=null;
        enableOverride=true;
        debugInfoStack=null;
    }

    // REMIND: remove once hotspot inlines Float.floatToIntBits
    private static native void floatsToBytes(float[] src,int srcpos,
                                             byte[] dst,int dstpos,
                                             int nfloats);

    // REMIND: remove once hotspot inlines Double.doubleToLongBits
    private static native void doublesToBytes(double[] src,int srcpos,
                                              byte[] dst,int dstpos,
                                              int ndoubles);

    public void useProtocolVersion(int version) throws IOException{
        if(handles.size()!=0){
            // REMIND: implement better check for pristine stream?
            throw new IllegalStateException("stream non-empty");
        }
        switch(version){
            case PROTOCOL_VERSION_1:
            case PROTOCOL_VERSION_2:
                protocol=version;
                break;
            default:
                throw new IllegalArgumentException(
                        "unknown version: "+version);
        }
    }

    public final void writeObject(Object obj) throws IOException{
        if(enableOverride){
            writeObjectOverride(obj);
            return;
        }
        try{
            writeObject0(obj,false);
        }catch(IOException ex){
            if(depth==0){
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    protected void writeObjectOverride(Object obj) throws IOException{
    }

    public void writeUnshared(Object obj) throws IOException{
        try{
            writeObject0(obj,true);
        }catch(IOException ex){
            if(depth==0){
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    public void defaultWriteObject() throws IOException{
        SerialCallbackContext ctx=curContext;
        if(ctx==null){
            throw new NotActiveException("not in call to writeObject");
        }
        Object curObj=ctx.getObj();
        ObjectStreamClass curDesc=ctx.getDesc();
        bout.setBlockDataMode(false);
        defaultWriteFields(curObj,curDesc);
        bout.setBlockDataMode(true);
    }

    public PutField putFields() throws IOException{
        if(curPut==null){
            SerialCallbackContext ctx=curContext;
            if(ctx==null){
                throw new NotActiveException("not in call to writeObject");
            }
            Object curObj=ctx.getObj();
            ObjectStreamClass curDesc=ctx.getDesc();
            curPut=new PutFieldImpl(curDesc);
        }
        return curPut;
    }

    public void writeFields() throws IOException{
        if(curPut==null){
            throw new NotActiveException("no current PutField object");
        }
        bout.setBlockDataMode(false);
        curPut.writeFields();
        bout.setBlockDataMode(true);
    }

    public void reset() throws IOException{
        if(depth!=0){
            throw new IOException("stream active");
        }
        bout.setBlockDataMode(false);
        bout.writeByte(TC_RESET);
        clear();
        bout.setBlockDataMode(true);
    }

    private void clear(){
        subs.clear();
        handles.clear();
    }

    protected void annotateClass(Class<?> cl) throws IOException{
    }

    protected void annotateProxyClass(Class<?> cl) throws IOException{
    }

    protected Object replaceObject(Object obj) throws IOException{
        return obj;
    }

    protected boolean enableReplaceObject(boolean enable)
            throws SecurityException{
        if(enable==enableReplace){
            return enable;
        }
        if(enable){
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null){
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        enableReplace=enable;
        return !enableReplace;
    }    public void write(byte[] buf) throws IOException{
        bout.write(buf,0,buf.length,false);
    }

    protected void writeClassDescriptor(ObjectStreamClass desc)
            throws IOException{
        desc.writeNonProxy(this);
    }

    public void write(int val) throws IOException{
        bout.write(val);
    }    public void write(byte[] buf,int off,int len) throws IOException{
        if(buf==null){
            throw new NullPointerException();
        }
        int endoff=off+len;
        if(off<0||len<0||endoff>buf.length||endoff<0){
            throw new IndexOutOfBoundsException();
        }
        bout.write(buf,off,len,false);
    }

    protected void drain() throws IOException{
        bout.drain();
    }

    public void writeBoolean(boolean val) throws IOException{
        bout.writeBoolean(val);
    }    public void flush() throws IOException{
        bout.flush();
    }

    int getProtocolVersion(){
        return protocol;
    }

    void writeTypeString(String str) throws IOException{
        int handle;
        if(str==null){
            writeNull();
        }else if((handle=handles.lookup(str))!=-1){
            writeHandle(handle);
        }else{
            writeString(str,false);
        }
    }

    private void writeNull() throws IOException{
        bout.writeByte(TC_NULL);
    }    public void close() throws IOException{
        flush();
        clear();
        bout.close();
    }

    private void writeHandle(int handle) throws IOException{
        bout.writeByte(TC_REFERENCE);
        bout.writeInt(baseWireHandle+handle);
    }

    private void writeString(String str,boolean unshared) throws IOException{
        handles.assign(unshared?null:str);
        long utflen=bout.getUTFLength(str);
        if(utflen<=0xFFFF){
            bout.writeByte(TC_STRING);
            bout.writeUTF(str,utflen);
        }else{
            bout.writeByte(TC_LONGSTRING);
            bout.writeLongUTF(str,utflen);
        }
    }

    private void writeObject0(Object obj,boolean unshared)
            throws IOException{
        boolean oldMode=bout.setBlockDataMode(false);
        depth++;
        try{
            // handle previously written and non-replaceable objects
            int h;
            if((obj=subs.lookup(obj))==null){
                writeNull();
                return;
            }else if(!unshared&&(h=handles.lookup(obj))!=-1){
                writeHandle(h);
                return;
            }else if(obj instanceof Class){
                writeClass((Class)obj,unshared);
                return;
            }else if(obj instanceof ObjectStreamClass){
                writeClassDesc((ObjectStreamClass)obj,unshared);
                return;
            }
            // check for replacement object
            Object orig=obj;
            Class<?> cl=obj.getClass();
            ObjectStreamClass desc;
            for(;;){
                // REMIND: skip this check for strings/arrays?
                Class<?> repCl;
                desc=ObjectStreamClass.lookup(cl,true);
                if(!desc.hasWriteReplaceMethod()||
                        (obj=desc.invokeWriteReplace(obj))==null||
                        (repCl=obj.getClass())==cl){
                    break;
                }
                cl=repCl;
            }
            if(enableReplace){
                Object rep=replaceObject(obj);
                if(rep!=obj&&rep!=null){
                    cl=rep.getClass();
                    desc=ObjectStreamClass.lookup(cl,true);
                }
                obj=rep;
            }
            // if object replaced, run through original checks a second time
            if(obj!=orig){
                subs.assign(orig,obj);
                if(obj==null){
                    writeNull();
                    return;
                }else if(!unshared&&(h=handles.lookup(obj))!=-1){
                    writeHandle(h);
                    return;
                }else if(obj instanceof Class){
                    writeClass((Class)obj,unshared);
                    return;
                }else if(obj instanceof ObjectStreamClass){
                    writeClassDesc((ObjectStreamClass)obj,unshared);
                    return;
                }
            }
            // remaining cases
            if(obj instanceof String){
                writeString((String)obj,unshared);
            }else if(cl.isArray()){
                writeArray(obj,desc,unshared);
            }else if(obj instanceof Enum){
                writeEnum((Enum<?>)obj,desc,unshared);
            }else if(obj instanceof Serializable){
                writeOrdinaryObject(obj,desc,unshared);
            }else{
                if(extendedDebugInfo){
                    throw new NotSerializableException(
                            cl.getName()+"\n"+debugInfoStack.toString());
                }else{
                    throw new NotSerializableException(cl.getName());
                }
            }
        }finally{
            depth--;
            bout.setBlockDataMode(oldMode);
        }
    }    public void writeByte(int val) throws IOException{
        bout.writeByte(val);
    }

    private void writeClass(Class<?> cl,boolean unshared) throws IOException{
        bout.writeByte(TC_CLASS);
        writeClassDesc(ObjectStreamClass.lookup(cl,true),false);
        handles.assign(unshared?null:cl);
    }

    private void writeClassDesc(ObjectStreamClass desc,boolean unshared)
            throws IOException{
        int handle;
        if(desc==null){
            writeNull();
        }else if(!unshared&&(handle=handles.lookup(desc))!=-1){
            writeHandle(handle);
        }else if(desc.isProxy()){
            writeProxyDesc(desc,unshared);
        }else{
            writeNonProxyDesc(desc,unshared);
        }
    }    public void writeShort(int val) throws IOException{
        bout.writeShort(val);
    }

    private boolean isCustomSubclass(){
        // Return true if this class is a custom subclass of ObjectOutputStream
        return getClass().getClassLoader()
                !=ObjectOutputStream.class.getClassLoader();
    }

    private void writeProxyDesc(ObjectStreamClass desc,boolean unshared)
            throws IOException{
        bout.writeByte(TC_PROXYCLASSDESC);
        handles.assign(unshared?null:desc);
        Class<?> cl=desc.forClass();
        Class<?>[] ifaces=cl.getInterfaces();
        bout.writeInt(ifaces.length);
        for(int i=0;i<ifaces.length;i++){
            bout.writeUTF(ifaces[i].getName());
        }
        bout.setBlockDataMode(true);
        if(cl!=null&&isCustomSubclass()){
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateProxyClass(cl);
        bout.setBlockDataMode(false);
        bout.writeByte(TC_ENDBLOCKDATA);
        writeClassDesc(desc.getSuperDesc(),false);
    }    public void writeChar(int val) throws IOException{
        bout.writeChar(val);
    }

    private void writeNonProxyDesc(ObjectStreamClass desc,boolean unshared)
            throws IOException{
        bout.writeByte(TC_CLASSDESC);
        handles.assign(unshared?null:desc);
        if(protocol==PROTOCOL_VERSION_1){
            // do not invoke class descriptor write hook with old protocol
            desc.writeNonProxy(this);
        }else{
            writeClassDescriptor(desc);
        }
        Class<?> cl=desc.forClass();
        bout.setBlockDataMode(true);
        if(cl!=null&&isCustomSubclass()){
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateClass(cl);
        bout.setBlockDataMode(false);
        bout.writeByte(TC_ENDBLOCKDATA);
        writeClassDesc(desc.getSuperDesc(),false);
    }

    private void writeArray(Object array,
                            ObjectStreamClass desc,
                            boolean unshared)
            throws IOException{
        bout.writeByte(TC_ARRAY);
        writeClassDesc(desc,false);
        handles.assign(unshared?null:array);
        Class<?> ccl=desc.forClass().getComponentType();
        if(ccl.isPrimitive()){
            if(ccl==Integer.TYPE){
                int[] ia=(int[])array;
                bout.writeInt(ia.length);
                bout.writeInts(ia,0,ia.length);
            }else if(ccl==Byte.TYPE){
                byte[] ba=(byte[])array;
                bout.writeInt(ba.length);
                bout.write(ba,0,ba.length,true);
            }else if(ccl==Long.TYPE){
                long[] ja=(long[])array;
                bout.writeInt(ja.length);
                bout.writeLongs(ja,0,ja.length);
            }else if(ccl==Float.TYPE){
                float[] fa=(float[])array;
                bout.writeInt(fa.length);
                bout.writeFloats(fa,0,fa.length);
            }else if(ccl==Double.TYPE){
                double[] da=(double[])array;
                bout.writeInt(da.length);
                bout.writeDoubles(da,0,da.length);
            }else if(ccl==Short.TYPE){
                short[] sa=(short[])array;
                bout.writeInt(sa.length);
                bout.writeShorts(sa,0,sa.length);
            }else if(ccl==Character.TYPE){
                char[] ca=(char[])array;
                bout.writeInt(ca.length);
                bout.writeChars(ca,0,ca.length);
            }else if(ccl==Boolean.TYPE){
                boolean[] za=(boolean[])array;
                bout.writeInt(za.length);
                bout.writeBooleans(za,0,za.length);
            }else{
                throw new InternalError();
            }
        }else{
            Object[] objs=(Object[])array;
            int len=objs.length;
            bout.writeInt(len);
            if(extendedDebugInfo){
                debugInfoStack.push(
                        "array (class \""+array.getClass().getName()+
                                "\", size: "+len+")");
            }
            try{
                for(int i=0;i<len;i++){
                    if(extendedDebugInfo){
                        debugInfoStack.push(
                                "element of array (index: "+i+")");
                    }
                    try{
                        writeObject0(objs[i],false);
                    }finally{
                        if(extendedDebugInfo){
                            debugInfoStack.pop();
                        }
                    }
                }
            }finally{
                if(extendedDebugInfo){
                    debugInfoStack.pop();
                }
            }
        }
    }    public void writeInt(int val) throws IOException{
        bout.writeInt(val);
    }

    private void writeEnum(Enum<?> en,
                           ObjectStreamClass desc,
                           boolean unshared)
            throws IOException{
        bout.writeByte(TC_ENUM);
        ObjectStreamClass sdesc=desc.getSuperDesc();
        writeClassDesc((sdesc.forClass()==Enum.class)?desc:sdesc,false);
        handles.assign(unshared?null:en);
        writeString(en.name(),false);
    }

    private void writeOrdinaryObject(Object obj,
                                     ObjectStreamClass desc,
                                     boolean unshared)
            throws IOException{
        if(extendedDebugInfo){
            debugInfoStack.push(
                    (depth==1?"root ":"")+"object (class \""+
                            obj.getClass().getName()+"\", "+obj.toString()+")");
        }
        try{
            desc.checkSerialize();
            bout.writeByte(TC_OBJECT);
            writeClassDesc(desc,false);
            handles.assign(unshared?null:obj);
            if(desc.isExternalizable()&&!desc.isProxy()){
                writeExternalData((Externalizable)obj);
            }else{
                writeSerialData(obj,desc);
            }
        }finally{
            if(extendedDebugInfo){
                debugInfoStack.pop();
            }
        }
    }    public void writeLong(long val) throws IOException{
        bout.writeLong(val);
    }

    private void writeExternalData(Externalizable obj) throws IOException{
        PutFieldImpl oldPut=curPut;
        curPut=null;
        if(extendedDebugInfo){
            debugInfoStack.push("writeExternal data");
        }
        SerialCallbackContext oldContext=curContext;
        try{
            curContext=null;
            if(protocol==PROTOCOL_VERSION_1){
                obj.writeExternal(this);
            }else{
                bout.setBlockDataMode(true);
                obj.writeExternal(this);
                bout.setBlockDataMode(false);
                bout.writeByte(TC_ENDBLOCKDATA);
            }
        }finally{
            curContext=oldContext;
            if(extendedDebugInfo){
                debugInfoStack.pop();
            }
        }
        curPut=oldPut;
    }

    private void writeSerialData(Object obj,ObjectStreamClass desc)
            throws IOException{
        ObjectStreamClass.ClassDataSlot[] slots=desc.getClassDataLayout();
        for(int i=0;i<slots.length;i++){
            ObjectStreamClass slotDesc=slots[i].desc;
            if(slotDesc.hasWriteObjectMethod()){
                PutFieldImpl oldPut=curPut;
                curPut=null;
                SerialCallbackContext oldContext=curContext;
                if(extendedDebugInfo){
                    debugInfoStack.push(
                            "custom writeObject data (class \""+
                                    slotDesc.getName()+"\")");
                }
                try{
                    curContext=new SerialCallbackContext(obj,slotDesc);
                    bout.setBlockDataMode(true);
                    slotDesc.invokeWriteObject(obj,this);
                    bout.setBlockDataMode(false);
                    bout.writeByte(TC_ENDBLOCKDATA);
                }finally{
                    curContext.setUsed();
                    curContext=oldContext;
                    if(extendedDebugInfo){
                        debugInfoStack.pop();
                    }
                }
                curPut=oldPut;
            }else{
                defaultWriteFields(obj,slotDesc);
            }
        }
    }    public void writeFloat(float val) throws IOException{
        bout.writeFloat(val);
    }

    private void defaultWriteFields(Object obj,ObjectStreamClass desc)
            throws IOException{
        Class<?> cl=desc.forClass();
        if(cl!=null&&obj!=null&&!cl.isInstance(obj)){
            throw new ClassCastException();
        }
        desc.checkDefaultSerialize();
        int primDataSize=desc.getPrimDataSize();
        if(primVals==null||primVals.length<primDataSize){
            primVals=new byte[primDataSize];
        }
        desc.getPrimFieldValues(obj,primVals);
        bout.write(primVals,0,primDataSize,false);
        ObjectStreamField[] fields=desc.getFields(false);
        Object[] objVals=new Object[desc.getNumObjFields()];
        int numPrimFields=fields.length-objVals.length;
        desc.getObjFieldValues(obj,objVals);
        for(int i=0;i<objVals.length;i++){
            if(extendedDebugInfo){
                debugInfoStack.push(
                        "field (class \""+desc.getName()+"\", name: \""+
                                fields[numPrimFields+i].getName()+"\", type: \""+
                                fields[numPrimFields+i].getType()+"\")");
            }
            try{
                writeObject0(objVals[i],
                        fields[numPrimFields+i].isUnshared());
            }finally{
                if(extendedDebugInfo){
                    debugInfoStack.pop();
                }
            }
        }
    }

    private void writeFatalException(IOException ex) throws IOException{
        /**
         * Note: the serialization specification states that if a second
         * IOException occurs while attempting to serialize the original fatal
         * exception to the stream, then a StreamCorruptedException should be
         * thrown (section 2.1).  However, due to a bug in previous
         * implementations of serialization, StreamCorruptedExceptions were
         * rarely (if ever) actually thrown--the "root" exceptions from
         * underlying streams were thrown instead.  This historical behavior is
         * followed here for consistency.
         */
        clear();
        boolean oldMode=bout.setBlockDataMode(false);
        try{
            bout.writeByte(TC_EXCEPTION);
            writeObject0(ex,false);
            clear();
        }finally{
            bout.setBlockDataMode(oldMode);
        }
    }    public void writeDouble(double val) throws IOException{
        bout.writeDouble(val);
    }

    private static class Caches{
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits=
                new ConcurrentHashMap<>();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue=
                new ReferenceQueue<>();
    }

    public static abstract class PutField{
        public abstract void put(String name,boolean val);

        public abstract void put(String name,byte val);

        public abstract void put(String name,char val);

        public abstract void put(String name,short val);

        public abstract void put(String name,int val);

        public abstract void put(String name,long val);

        public abstract void put(String name,float val);

        public abstract void put(String name,double val);

        public abstract void put(String name,Object val);

        @Deprecated
        public abstract void write(ObjectOutput out) throws IOException;
    }    public void writeBytes(String str) throws IOException{
        bout.writeBytes(str);
    }

    private static class BlockDataOutputStream
            extends OutputStream implements DataOutput{
        private static final int MAX_BLOCK_SIZE=1024;
        private static final int MAX_HEADER_SIZE=5;
        private static final int CHAR_BUF_SIZE=256;
        private final byte[] buf=new byte[MAX_BLOCK_SIZE];
        private final byte[] hbuf=new byte[MAX_HEADER_SIZE];
        private final char[] cbuf=new char[CHAR_BUF_SIZE];
        private final OutputStream out;
        private final DataOutputStream dout;
        private boolean blkmode=false;
        private int pos=0;

        BlockDataOutputStream(OutputStream out){
            this.out=out;
            dout=new DataOutputStream(this);
        }

        boolean setBlockDataMode(boolean mode) throws IOException{
            if(blkmode==mode){
                return blkmode;
            }
            drain();
            blkmode=mode;
            return !blkmode;
        }

        void drain() throws IOException{
            if(pos==0){
                return;
            }
            if(blkmode){
                writeBlockHeader(pos);
            }
            out.write(buf,0,pos);
            pos=0;
        }

        /** ----------------- generic output stream methods ----------------- */
        private void writeBlockHeader(int len) throws IOException{
            if(len<=0xFF){
                hbuf[0]=TC_BLOCKDATA;
                hbuf[1]=(byte)len;
                out.write(hbuf,0,2);
            }else{
                hbuf[0]=TC_BLOCKDATALONG;
                Bits.putInt(hbuf,1,len);
                out.write(hbuf,0,5);
            }
        }

        boolean getBlockDataMode(){
            return blkmode;
        }        public void write(int b) throws IOException{
            if(pos>=MAX_BLOCK_SIZE){
                drain();
            }
            buf[pos++]=(byte)b;
        }

        void writeBooleans(boolean[] v,int off,int len) throws IOException{
            int endoff=off+len;
            while(off<endoff){
                if(pos>=MAX_BLOCK_SIZE){
                    drain();
                }
                int stop=Math.min(endoff,off+(MAX_BLOCK_SIZE-pos));
                while(off<stop){
                    Bits.putBoolean(buf,pos++,v[off++]);
                }
            }
        }

        void writeShorts(short[] v,int off,int len) throws IOException{
            int limit=MAX_BLOCK_SIZE-2;
            int endoff=off+len;
            while(off<endoff){
                if(pos<=limit){
                    int avail=(MAX_BLOCK_SIZE-pos)>>1;
                    int stop=Math.min(endoff,off+avail);
                    while(off<stop){
                        Bits.putShort(buf,pos,v[off++]);
                        pos+=2;
                    }
                }else{
                    dout.writeShort(v[off++]);
                }
            }
        }        public void write(byte[] b) throws IOException{
            write(b,0,b.length,false);
        }

        void writeInts(int[] v,int off,int len) throws IOException{
            int limit=MAX_BLOCK_SIZE-4;
            int endoff=off+len;
            while(off<endoff){
                if(pos<=limit){
                    int avail=(MAX_BLOCK_SIZE-pos)>>2;
                    int stop=Math.min(endoff,off+avail);
                    while(off<stop){
                        Bits.putInt(buf,pos,v[off++]);
                        pos+=4;
                    }
                }else{
                    dout.writeInt(v[off++]);
                }
            }
        }

        void writeFloats(float[] v,int off,int len) throws IOException{
            int limit=MAX_BLOCK_SIZE-4;
            int endoff=off+len;
            while(off<endoff){
                if(pos<=limit){
                    int avail=(MAX_BLOCK_SIZE-pos)>>2;
                    int chunklen=Math.min(endoff-off,avail);
                    floatsToBytes(v,off,buf,pos,chunklen);
                    off+=chunklen;
                    pos+=chunklen<<2;
                }else{
                    dout.writeFloat(v[off++]);
                }
            }
        }        public void write(byte[] b,int off,int len) throws IOException{
            write(b,off,len,false);
        }

        void writeLongs(long[] v,int off,int len) throws IOException{
            int limit=MAX_BLOCK_SIZE-8;
            int endoff=off+len;
            while(off<endoff){
                if(pos<=limit){
                    int avail=(MAX_BLOCK_SIZE-pos)>>3;
                    int stop=Math.min(endoff,off+avail);
                    while(off<stop){
                        Bits.putLong(buf,pos,v[off++]);
                        pos+=8;
                    }
                }else{
                    dout.writeLong(v[off++]);
                }
            }
        }

        void writeDoubles(double[] v,int off,int len) throws IOException{
            int limit=MAX_BLOCK_SIZE-8;
            int endoff=off+len;
            while(off<endoff){
                if(pos<=limit){
                    int avail=(MAX_BLOCK_SIZE-pos)>>3;
                    int chunklen=Math.min(endoff-off,avail);
                    doublesToBytes(v,off,buf,pos,chunklen);
                    off+=chunklen;
                    pos+=chunklen<<3;
                }else{
                    dout.writeDouble(v[off++]);
                }
            }
        }        public void flush() throws IOException{
            drain();
            out.flush();
        }

        /** ----------------- primitive data output methods ----------------- */
        long getUTFLength(String s){
            int len=s.length();
            long utflen=0;
            for(int off=0;off<len;){
                int csize=Math.min(len-off,CHAR_BUF_SIZE);
                s.getChars(off,off+csize,cbuf,0);
                for(int cpos=0;cpos<csize;cpos++){
                    char c=cbuf[cpos];
                    if(c>=0x0001&&c<=0x007F){
                        utflen++;
                    }else if(c>0x07FF){
                        utflen+=3;
                    }else{
                        utflen+=2;
                    }
                }
                off+=csize;
            }
            return utflen;
        }

        void writeUTF(String s,long utflen) throws IOException{
            if(utflen>0xFFFFL){
                throw new UTFDataFormatException();
            }
            writeShort((int)utflen);
            if(utflen==(long)s.length()){
                writeBytes(s);
            }else{
                writeUTFBody(s);
            }
        }        public void close() throws IOException{
            flush();
            out.close();
        }

        void writeLongUTF(String s) throws IOException{
            writeLongUTF(s,getUTFLength(s));
        }

        void writeLongUTF(String s,long utflen) throws IOException{
            writeLong(utflen);
            if(utflen==(long)s.length()){
                writeBytes(s);
            }else{
                writeUTFBody(s);
            }
        }        void write(byte[] b,int off,int len,boolean copy)
                throws IOException{
            if(!(copy||blkmode)){           // write directly
                drain();
                out.write(b,off,len);
                return;
            }
            while(len>0){
                if(pos>=MAX_BLOCK_SIZE){
                    drain();
                }
                if(len>=MAX_BLOCK_SIZE&&!copy&&pos==0){
                    // avoid unnecessary copy
                    writeBlockHeader(MAX_BLOCK_SIZE);
                    out.write(b,off,MAX_BLOCK_SIZE);
                    off+=MAX_BLOCK_SIZE;
                    len-=MAX_BLOCK_SIZE;
                }else{
                    int wlen=Math.min(len,MAX_BLOCK_SIZE-pos);
                    System.arraycopy(b,off,buf,pos,wlen);
                    pos+=wlen;
                    off+=wlen;
                    len-=wlen;
                }
            }
        }

        private void writeUTFBody(String s) throws IOException{
            int limit=MAX_BLOCK_SIZE-3;
            int len=s.length();
            for(int off=0;off<len;){
                int csize=Math.min(len-off,CHAR_BUF_SIZE);
                s.getChars(off,off+csize,cbuf,0);
                for(int cpos=0;cpos<csize;cpos++){
                    char c=cbuf[cpos];
                    if(pos<=limit){
                        if(c<=0x007F&&c!=0){
                            buf[pos++]=(byte)c;
                        }else if(c>0x07FF){
                            buf[pos+2]=(byte)(0x80|((c>>0)&0x3F));
                            buf[pos+1]=(byte)(0x80|((c>>6)&0x3F));
                            buf[pos+0]=(byte)(0xE0|((c>>12)&0x0F));
                            pos+=3;
                        }else{
                            buf[pos+1]=(byte)(0x80|((c>>0)&0x3F));
                            buf[pos+0]=(byte)(0xC0|((c>>6)&0x1F));
                            pos+=2;
                        }
                    }else{    // write one byte at a time to normalize block
                        if(c<=0x007F&&c!=0){
                            write(c);
                        }else if(c>0x07FF){
                            write(0xE0|((c>>12)&0x0F));
                            write(0x80|((c>>6)&0x3F));
                            write(0x80|((c>>0)&0x3F));
                        }else{
                            write(0xC0|((c>>6)&0x1F));
                            write(0x80|((c>>0)&0x3F));
                        }
                    }
                }
                off+=csize;
            }
        }





        public void writeBoolean(boolean v) throws IOException{
            if(pos>=MAX_BLOCK_SIZE){
                drain();
            }
            Bits.putBoolean(buf,pos++,v);
        }



        public void writeByte(int v) throws IOException{
            if(pos>=MAX_BLOCK_SIZE){
                drain();
            }
            buf[pos++]=(byte)v;
        }



        public void writeChar(int v) throws IOException{
            if(pos+2<=MAX_BLOCK_SIZE){
                Bits.putChar(buf,pos,(char)v);
                pos+=2;
            }else{
                dout.writeChar(v);
            }
        }



        public void writeShort(int v) throws IOException{
            if(pos+2<=MAX_BLOCK_SIZE){
                Bits.putShort(buf,pos,(short)v);
                pos+=2;
            }else{
                dout.writeShort(v);
            }
        }



        public void writeInt(int v) throws IOException{
            if(pos+4<=MAX_BLOCK_SIZE){
                Bits.putInt(buf,pos,v);
                pos+=4;
            }else{
                dout.writeInt(v);
            }
        }

        public void writeFloat(float v) throws IOException{
            if(pos+4<=MAX_BLOCK_SIZE){
                Bits.putFloat(buf,pos,v);
                pos+=4;
            }else{
                dout.writeFloat(v);
            }
        }

        public void writeLong(long v) throws IOException{
            if(pos+8<=MAX_BLOCK_SIZE){
                Bits.putLong(buf,pos,v);
                pos+=8;
            }else{
                dout.writeLong(v);
            }
        }

        public void writeDouble(double v) throws IOException{
            if(pos+8<=MAX_BLOCK_SIZE){
                Bits.putDouble(buf,pos,v);
                pos+=8;
            }else{
                dout.writeDouble(v);
            }
        }

        public void writeBytes(String s) throws IOException{
            int endoff=s.length();
            int cpos=0;
            int csize=0;
            for(int off=0;off<endoff;){
                if(cpos>=csize){
                    cpos=0;
                    csize=Math.min(endoff-off,CHAR_BUF_SIZE);
                    s.getChars(off,off+csize,cbuf,0);
                }
                if(pos>=MAX_BLOCK_SIZE){
                    drain();
                }
                int n=Math.min(csize-cpos,MAX_BLOCK_SIZE-pos);
                int stop=pos+n;
                while(pos<stop){
                    buf[pos++]=(byte)cbuf[cpos++];
                }
                off+=n;
            }
        }

        public void writeChars(String s) throws IOException{
            int endoff=s.length();
            for(int off=0;off<endoff;){
                int csize=Math.min(endoff-off,CHAR_BUF_SIZE);
                s.getChars(off,off+csize,cbuf,0);
                writeChars(cbuf,0,csize);
                off+=csize;
            }
        }

        public void writeUTF(String s) throws IOException{
            writeUTF(s,getUTFLength(s));
        }

        /** -------------- primitive data array output methods -------------- */
        void writeChars(char[] v,int off,int len) throws IOException{
            int limit=MAX_BLOCK_SIZE-2;
            int endoff=off+len;
            while(off<endoff){
                if(pos<=limit){
                    int avail=(MAX_BLOCK_SIZE-pos)>>1;
                    int stop=Math.min(endoff,off+avail);
                    while(off<stop){
                        Bits.putChar(buf,pos,v[off++]);
                        pos+=2;
                    }
                }else{
                    dout.writeChar(v[off++]);
                }
            }
        }
    }

    private static class HandleTable{
        private final float loadFactor;
        private int size;
        private int threshold;
        private int[] spine;
        private int[] next;
        private Object[] objs;

        HandleTable(int initialCapacity,float loadFactor){
            this.loadFactor=loadFactor;
            spine=new int[initialCapacity];
            next=new int[initialCapacity];
            objs=new Object[initialCapacity];
            threshold=(int)(initialCapacity*loadFactor);
            clear();
        }

        void clear(){
            Arrays.fill(spine,-1);
            Arrays.fill(objs,0,size,null);
            size=0;
        }

        int assign(Object obj){
            if(size>=next.length){
                growEntries();
            }
            if(size>=threshold){
                growSpine();
            }
            insert(obj,size);
            return size++;
        }

        int lookup(Object obj){
            if(size==0){
                return -1;
            }
            int index=hash(obj)%spine.length;
            for(int i=spine[index];i>=0;i=next[i]){
                if(objs[i]==obj){
                    return i;
                }
            }
            return -1;
        }

        private int hash(Object obj){
            return System.identityHashCode(obj)&0x7FFFFFFF;
        }

        int size(){
            return size;
        }

        private void insert(Object obj,int handle){
            int index=hash(obj)%spine.length;
            objs[handle]=obj;
            next[handle]=spine[index];
            spine[index]=handle;
        }

        private void growSpine(){
            spine=new int[(spine.length<<1)+1];
            threshold=(int)(spine.length*loadFactor);
            Arrays.fill(spine,-1);
            for(int i=0;i<size;i++){
                insert(objs[i],i);
            }
        }

        private void growEntries(){
            int newLength=(next.length<<1)+1;
            int[] newNext=new int[newLength];
            System.arraycopy(next,0,newNext,0,size);
            next=newNext;
            Object[] newObjs=new Object[newLength];
            System.arraycopy(objs,0,newObjs,0,size);
            objs=newObjs;
        }
    }    public void writeChars(String str) throws IOException{
        bout.writeChars(str);
    }

    private static class ReplaceTable{
        private final HandleTable htab;
        private Object[] reps;

        ReplaceTable(int initialCapacity,float loadFactor){
            htab=new HandleTable(initialCapacity,loadFactor);
            reps=new Object[initialCapacity];
        }

        void assign(Object obj,Object rep){
            int index=htab.assign(obj);
            while(index>=reps.length){
                grow();
            }
            reps[index]=rep;
        }

        private void grow(){
            Object[] newReps=new Object[(reps.length<<1)+1];
            System.arraycopy(reps,0,newReps,0,reps.length);
            reps=newReps;
        }

        Object lookup(Object obj){
            int index=htab.lookup(obj);
            return (index>=0)?reps[index]:obj;
        }

        void clear(){
            Arrays.fill(reps,0,htab.size(),null);
            htab.clear();
        }

        int size(){
            return htab.size();
        }
    }

    private static class DebugTraceInfoStack{
        private final List<String> stack;

        DebugTraceInfoStack(){
            stack=new ArrayList<>();
        }

        void clear(){
            stack.clear();
        }

        void pop(){
            stack.remove(stack.size()-1);
        }

        void push(String entry){
            stack.add("\t- "+entry);
        }

        public String toString(){
            StringBuilder buffer=new StringBuilder();
            if(!stack.isEmpty()){
                for(int i=stack.size();i>0;i--){
                    buffer.append(stack.get(i-1)+((i!=1)?"\n":""));
                }
            }
            return buffer.toString();
        }
    }    public void writeUTF(String str) throws IOException{
        bout.writeUTF(str);
    }

    private class PutFieldImpl extends PutField{
        private final ObjectStreamClass desc;
        private final byte[] primVals;
        private final Object[] objVals;

        PutFieldImpl(ObjectStreamClass desc){
            this.desc=desc;
            primVals=new byte[desc.getPrimDataSize()];
            objVals=new Object[desc.getNumObjFields()];
        }

        public void put(String name,boolean val){
            Bits.putBoolean(primVals,getFieldOffset(name,Boolean.TYPE),val);
        }

        public void put(String name,byte val){
            primVals[getFieldOffset(name,Byte.TYPE)]=val;
        }

        public void put(String name,char val){
            Bits.putChar(primVals,getFieldOffset(name,Character.TYPE),val);
        }

        public void put(String name,short val){
            Bits.putShort(primVals,getFieldOffset(name,Short.TYPE),val);
        }

        public void put(String name,int val){
            Bits.putInt(primVals,getFieldOffset(name,Integer.TYPE),val);
        }

        public void put(String name,long val){
            Bits.putLong(primVals,getFieldOffset(name,Long.TYPE),val);
        }

        public void put(String name,float val){
            Bits.putFloat(primVals,getFieldOffset(name,Float.TYPE),val);
        }

        public void put(String name,double val){
            Bits.putDouble(primVals,getFieldOffset(name,Double.TYPE),val);
        }

        public void put(String name,Object val){
            objVals[getFieldOffset(name,Object.class)]=val;
        }

        // deprecated in ObjectOutputStream.PutField
        public void write(ObjectOutput out) throws IOException{
            /**
             * Applications should *not* use this method to write PutField
             * data, as it will lead to stream corruption if the PutField
             * object writes any primitive data (since block data mode is not
             * unset/set properly, as is done in OOS.writeFields()).  This
             * broken implementation is being retained solely for behavioral
             * compatibility, in order to support applications which use
             * OOS.PutField.write() for writing only non-primitive data.
             *
             * Serialization of unshared objects is not implemented here since
             * it is not necessary for backwards compatibility; also, unshared
             * semantics may not be supported by the given ObjectOutput
             * instance.  Applications which write unshared objects using the
             * PutField API must use OOS.writeFields().
             */
            if(ObjectOutputStream.this!=out){
                throw new IllegalArgumentException("wrong stream");
            }
            out.write(primVals,0,primVals.length);
            ObjectStreamField[] fields=desc.getFields(false);
            int numPrimFields=fields.length-objVals.length;
            // REMIND: warn if numPrimFields > 0?
            for(int i=0;i<objVals.length;i++){
                if(fields[numPrimFields+i].isUnshared()){
                    throw new IOException("cannot write unshared object");
                }
                out.writeObject(objVals[i]);
            }
        }

        private int getFieldOffset(String name,Class<?> type){
            ObjectStreamField field=desc.getField(name,type);
            if(field==null){
                throw new IllegalArgumentException("no such field "+name+
                        " with type "+type);
            }
            return field.getOffset();
        }

        void writeFields() throws IOException{
            bout.write(primVals,0,primVals.length,false);
            ObjectStreamField[] fields=desc.getFields(false);
            int numPrimFields=fields.length-objVals.length;
            for(int i=0;i<objVals.length;i++){
                if(extendedDebugInfo){
                    debugInfoStack.push(
                            "field (class \""+desc.getName()+"\", name: \""+
                                    fields[numPrimFields+i].getName()+"\", type: \""+
                                    fields[numPrimFields+i].getType()+"\")");
                }
                try{
                    writeObject0(objVals[i],
                            fields[numPrimFields+i].isUnshared());
                }finally{
                    if(extendedDebugInfo){
                        debugInfoStack.pop();
                    }
                }
            }
        }
    }




























}
