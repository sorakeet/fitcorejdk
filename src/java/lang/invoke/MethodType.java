/**
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.invoke;

import sun.invoke.util.BytecodeDescriptor;
import sun.invoke.util.VerifyType;
import sun.invoke.util.Wrapper;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.invoke.MethodHandleStatics.UNSAFE;
import static java.lang.invoke.MethodHandleStatics.newIllegalArgumentException;

public final class MethodType implements java.io.Serializable{
    static final int MAX_JVM_ARITY=255;  // this is mandated by the JVM spec.
    // Issue:  Should we allow MH.invokeWithArguments to go to the full 255?
    static final int MAX_MH_ARITY=MAX_JVM_ARITY-1;  // deduct one for mh receiver
    static final int MAX_MH_INVOKER_ARITY=MAX_MH_ARITY-1;  // deduct one more for invoker
    static final ConcurrentWeakInternSet<MethodType> internTable=new ConcurrentWeakInternSet<>();
    static final Class<?>[] NO_PTYPES={};
    private static final long serialVersionUID=292L;  // {rtype, {ptype...}}
    private static final MethodType[] objectOnlyTypes=new MethodType[20];
    /// Serialization.
    private static final java.io.ObjectStreamField[] serialPersistentFields={};
    // Support for resetting final fields while deserializing
    private static final long rtypeOffset, ptypesOffset;

    static{
        try{
            rtypeOffset=UNSAFE.objectFieldOffset
                    (MethodType.class.getDeclaredField("rtype"));
            ptypesOffset=UNSAFE.objectFieldOffset
                    (MethodType.class.getDeclaredField("ptypes"));
        }catch(Exception ex){
            throw new Error(ex);
        }
    }

    // The rtype and ptypes fields define the structural identity of the method type:
    private final Class<?> rtype;
    private final Class<?>[] ptypes;
    // The remaining fields are caches of various sorts:
    private @Stable
    MethodTypeForm form; // erased form, plus cached data about primitives
    private @Stable
    MethodType wrapAlt;  // alternative wrapped/unwrapped version
    private @Stable
    Invokers invokers;   // cache of handy higher-order adapters
    private @Stable
    String methodDescriptor;  // cache for toMethodDescriptorString

    private MethodType(Class<?> rtype,Class<?>[] ptypes,boolean trusted){
        checkRtype(rtype);
        checkPtypes(ptypes);
        this.rtype=rtype;
        // defensively copy the array passed in by the user
        this.ptypes=trusted?ptypes:Arrays.copyOf(ptypes,ptypes.length);
    }

    private static void checkRtype(Class<?> rtype){
        Objects.requireNonNull(rtype);
    }

    private static int checkPtypes(Class<?>[] ptypes){
        int slots=0;
        for(Class<?> ptype : ptypes){
            checkPtype(ptype);
            if(ptype==double.class||ptype==long.class){
                slots++;
            }
        }
        checkSlotCount(ptypes.length+slots);
        return slots;
    }

    private static void checkPtype(Class<?> ptype){
        Objects.requireNonNull(ptype);
        if(ptype==void.class)
            throw newIllegalArgumentException("parameter type cannot be void");
    }

    static void checkSlotCount(int count){
        assert ((MAX_JVM_ARITY&(MAX_JVM_ARITY+1))==0);
        // MAX_JVM_ARITY must be power of 2 minus 1 for following code trick to work:
        if((count&MAX_JVM_ARITY)!=count)
            throw newIllegalArgumentException("bad parameter count "+count);
    }

    private MethodType(Class<?>[] ptypes,Class<?> rtype){
        this.rtype=rtype;
        this.ptypes=ptypes;
    }

    private MethodType(){
        this.rtype=null;
        this.ptypes=null;
    }

    public static MethodType methodType(Class<?> rtype,Class<?> ptype0,Class<?>... ptypes){
        Class<?>[] ptypes1=new Class<?>[1+ptypes.length];
        ptypes1[0]=ptype0;
        System.arraycopy(ptypes,0,ptypes1,1,ptypes.length);
        return makeImpl(rtype,ptypes1,true);
    }

    static MethodType makeImpl(Class<?> rtype,Class<?>[] ptypes,boolean trusted){
        MethodType mt=internTable.get(new MethodType(ptypes,rtype));
        if(mt!=null)
            return mt;
        if(ptypes.length==0){
            ptypes=NO_PTYPES;
            trusted=true;
        }
        mt=new MethodType(rtype,ptypes,trusted);
        // promote the object to the Real Thing, and reprobe
        mt.form=MethodTypeForm.findForm(mt);
        return internTable.add(mt);
    }

    public static MethodType methodType(Class<?> rtype){
        return makeImpl(rtype,NO_PTYPES,true);
    }

    public static MethodType methodType(Class<?> rtype,Class<?> ptype0){
        return makeImpl(rtype,new Class<?>[]{ptype0},true);
    }

    public static MethodType methodType(Class<?> rtype,MethodType ptypes){
        return makeImpl(rtype,ptypes.ptypes,true);
    }

    public static MethodType fromMethodDescriptorString(String descriptor,ClassLoader loader)
            throws IllegalArgumentException, TypeNotPresentException{
        if(!descriptor.startsWith("(")||  // also generates NPE if needed
                descriptor.indexOf(')')<0||
                descriptor.indexOf('.')>=0)
            throw newIllegalArgumentException("not a method descriptor: "+descriptor);
        List<Class<?>> types=BytecodeDescriptor.parseMethod(descriptor,loader);
        Class<?> rtype=types.remove(types.size()-1);
        checkSlotCount(types.size());
        Class<?>[] ptypes=listToArray(types);
        return makeImpl(rtype,ptypes,true);
    }

    private static Class<?>[] listToArray(List<Class<?>> ptypes){
        // sanity check the size before the toArray call, since size might be huge
        checkSlotCount(ptypes.size());
        return ptypes.toArray(NO_PTYPES);
    }

    static String toFieldDescriptorString(Class<?> cls){
        return BytecodeDescriptor.unparse(cls);
    }

    Class<?> rtype(){
        return rtype;
    }

    Class<?>[] ptypes(){
        return ptypes;
    }

    void setForm(MethodTypeForm f){
        form=f;
    }

    public MethodType changeParameterType(int num,Class<?> nptype){
        if(parameterType(num)==nptype) return this;
        checkPtype(nptype);
        Class<?>[] nptypes=ptypes.clone();
        nptypes[num]=nptype;
        return makeImpl(rtype,nptypes,true);
    }

    public Class<?> parameterType(int num){
        return ptypes[num];
    }

    public MethodType appendParameterTypes(Class<?>... ptypesToInsert){
        return insertParameterTypes(parameterCount(),ptypesToInsert);
    }

    public MethodType insertParameterTypes(int num,Class<?>... ptypesToInsert){
        int len=ptypes.length;
        if(num<0||num>len)
            throw newIndexOutOfBoundsException(num);
        int ins=checkPtypes(ptypesToInsert);
        checkSlotCount(parameterSlotCount()+ptypesToInsert.length+ins);
        int ilen=ptypesToInsert.length;
        if(ilen==0) return this;
        Class<?>[] nptypes=Arrays.copyOfRange(ptypes,0,len+ilen);
        System.arraycopy(nptypes,num,nptypes,num+ilen,len-num);
        System.arraycopy(ptypesToInsert,0,nptypes,num,ilen);
        return makeImpl(rtype,nptypes,true);
    }

    private static IndexOutOfBoundsException newIndexOutOfBoundsException(Object num){
        if(num instanceof Integer) num="bad index: "+num;
        return new IndexOutOfBoundsException(num.toString());
    }

    int parameterSlotCount(){
        return form.parameterSlotCount();
    }

    public int parameterCount(){
        return ptypes.length;
    }

    public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert){
        return insertParameterTypes(parameterCount(),ptypesToInsert);
    }

    public MethodType insertParameterTypes(int num,List<Class<?>> ptypesToInsert){
        return insertParameterTypes(num,listToArray(ptypesToInsert));
    }

    MethodType replaceParameterTypes(int start,int end,Class<?>... ptypesToInsert){
        if(start==end)
            return insertParameterTypes(start,ptypesToInsert);
        int len=ptypes.length;
        if(!(0<=start&&start<=end&&end<=len))
            throw newIndexOutOfBoundsException("start="+start+" end="+end);
        int ilen=ptypesToInsert.length;
        if(ilen==0)
            return dropParameterTypes(start,end);
        return dropParameterTypes(start,end).insertParameterTypes(start,ptypesToInsert);
    }

    public MethodType dropParameterTypes(int start,int end){
        int len=ptypes.length;
        if(!(0<=start&&start<=end&&end<=len))
            throw newIndexOutOfBoundsException("start="+start+" end="+end);
        if(start==end) return this;
        Class<?>[] nptypes;
        if(start==0){
            if(end==len){
                // drop all parameters
                nptypes=NO_PTYPES;
            }else{
                // drop initial parameter(s)
                nptypes=Arrays.copyOfRange(ptypes,end,len);
            }
        }else{
            if(end==len){
                // drop trailing parameter(s)
                nptypes=Arrays.copyOfRange(ptypes,0,start);
            }else{
                int tail=len-end;
                nptypes=Arrays.copyOfRange(ptypes,0,start+tail);
                System.arraycopy(ptypes,end,nptypes,start,tail);
            }
        }
        return makeImpl(rtype,nptypes,true);
    }

    MethodType asSpreaderType(Class<?> arrayType,int arrayLength){
        assert (parameterCount()>=arrayLength);
        int spreadPos=ptypes.length-arrayLength;
        if(arrayLength==0) return this;  // nothing to change
        if(arrayType==Object[].class){
            if(isGeneric()) return this;  // nothing to change
            if(spreadPos==0){
                // no leading arguments to preserve; go generic
                MethodType res=genericMethodType(arrayLength);
                if(rtype!=Object.class){
                    res=res.changeReturnType(rtype);
                }
                return res;
            }
        }
        Class<?> elemType=arrayType.getComponentType();
        assert (elemType!=null);
        for(int i=spreadPos;i<ptypes.length;i++){
            if(ptypes[i]!=elemType){
                Class<?>[] fixedPtypes=ptypes.clone();
                Arrays.fill(fixedPtypes,i,ptypes.length,elemType);
                return methodType(rtype,fixedPtypes);
            }
        }
        return this;  // arguments check out; no change
    }

    public static MethodType methodType(Class<?> rtype,Class<?>[] ptypes){
        return makeImpl(rtype,ptypes,false);
    }

    public static MethodType genericMethodType(int objectArgCount){
        return genericMethodType(objectArgCount,false);
    }

    public static MethodType genericMethodType(int objectArgCount,boolean finalArray){
        MethodType mt;
        checkSlotCount(objectArgCount);
        int ivarargs=(!finalArray?0:1);
        int ootIndex=objectArgCount*2+ivarargs;
        if(ootIndex<objectOnlyTypes.length){
            mt=objectOnlyTypes[ootIndex];
            if(mt!=null) return mt;
        }
        Class<?>[] ptypes=new Class<?>[objectArgCount+ivarargs];
        Arrays.fill(ptypes,Object.class);
        if(ivarargs!=0) ptypes[objectArgCount]=Object[].class;
        mt=makeImpl(Object.class,ptypes,true);
        if(ootIndex<objectOnlyTypes.length){
            objectOnlyTypes[ootIndex]=mt;     // cache it here also!
        }
        return mt;
    }

    boolean isGeneric(){
        return this==erase()&&!hasPrimitives();
    }

    public boolean hasPrimitives(){
        return form.hasPrimitives();
    }

    public MethodType erase(){
        return form.erasedType();
    }

    Class<?> leadingReferenceParameter(){
        Class<?> ptype;
        if(ptypes.length==0||
                (ptype=ptypes[0]).isPrimitive())
            throw newIllegalArgumentException("no leading reference parameter");
        return ptype;
    }

    MethodType asCollectorType(Class<?> arrayType,int arrayLength){
        assert (parameterCount()>=1);
        assert (lastParameterType().isAssignableFrom(arrayType));
        MethodType res;
        if(arrayType==Object[].class){
            res=genericMethodType(arrayLength);
            if(rtype!=Object.class){
                res=res.changeReturnType(rtype);
            }
        }else{
            Class<?> elemType=arrayType.getComponentType();
            assert (elemType!=null);
            res=methodType(rtype,Collections.nCopies(arrayLength,elemType));
        }
        if(ptypes.length==1){
            return res;
        }else{
            return res.insertParameterTypes(0,parameterList().subList(0,ptypes.length-1));
        }
    }

    public static MethodType methodType(Class<?> rtype,List<Class<?>> ptypes){
        boolean notrust=false;  // random List impl. could return evil ptypes array
        return makeImpl(rtype,listToArray(ptypes),notrust);
    }

    public List<Class<?>> parameterList(){
        return Collections.unmodifiableList(Arrays.asList(ptypes.clone()));
    }

    Class<?> lastParameterType(){
        int len=ptypes.length;
        return len==0?void.class:ptypes[len-1];
    }

    public MethodType changeReturnType(Class<?> nrtype){
        if(returnType()==nrtype) return this;
        return makeImpl(nrtype,ptypes,true);
    }

    public Class<?> returnType(){
        return rtype;
    }

    public boolean hasWrappers(){
        return unwrap()!=this;
    }

    public MethodType unwrap(){
        MethodType noprims=!hasPrimitives()?this:wrapWithPrims(this);
        return unwrapWithNoPrims(noprims);
    }

    private static MethodType wrapWithPrims(MethodType pt){
        assert (pt.hasPrimitives());
        MethodType wt=pt.wrapAlt;
        if(wt==null){
            // fill in lazily
            wt=MethodTypeForm.canonicalize(pt,MethodTypeForm.WRAP,MethodTypeForm.WRAP);
            assert (wt!=null);
            pt.wrapAlt=wt;
        }
        return wt;
    }

    private static MethodType unwrapWithNoPrims(MethodType wt){
        assert (!wt.hasPrimitives());
        MethodType uwt=wt.wrapAlt;
        if(uwt==null){
            // fill in lazily
            uwt=MethodTypeForm.canonicalize(wt,MethodTypeForm.UNWRAP,MethodTypeForm.UNWRAP);
            if(uwt==null)
                uwt=wt;    // type has no wrappers or prims at all
            wt.wrapAlt=uwt;
        }
        return uwt;
    }

    MethodType basicType(){
        return form.basicType();
    }    @Override
    public boolean equals(Object x){
        return this==x||x instanceof MethodType&&equals((MethodType)x);
    }

    MethodType invokerType(){
        return insertParameterTypes(0,MethodHandle.class);
    }

    public MethodType generic(){
        return genericMethodType(parameterCount());
    }    private boolean equals(MethodType that){
        return this.rtype==that.rtype
                &&Arrays.equals(this.ptypes,that.ptypes);
    }

    public MethodType wrap(){
        return hasPrimitives()?wrapWithPrims(this):this;
    }

    @Override
    public int hashCode(){
        int hashCode=31+rtype.hashCode();
        for(Class<?> ptype : ptypes)
            hashCode=31*hashCode+ptype.hashCode();
        return hashCode;
    }

    boolean isViewableAs(MethodType newType,boolean keepInterfaces){
        if(!VerifyType.isNullConversion(returnType(),newType.returnType(),keepInterfaces))
            return false;
        return parametersAreViewableAs(newType,keepInterfaces);
    }    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("(");
        for(int i=0;i<ptypes.length;i++){
            if(i>0) sb.append(",");
            sb.append(ptypes[i].getSimpleName());
        }
        sb.append(")");
        sb.append(rtype.getSimpleName());
        return sb.toString();
    }

    boolean parametersAreViewableAs(MethodType newType,boolean keepInterfaces){
        if(form==newType.form&&form.erasedType==this)
            return true;  // my reference parameters are all Object
        if(ptypes==newType.ptypes)
            return true;
        int argc=parameterCount();
        if(argc!=newType.parameterCount())
            return false;
        for(int i=0;i<argc;i++){
            if(!VerifyType.isNullConversion(newType.parameterType(i),parameterType(i),keepInterfaces))
                return false;
        }
        return true;
    }

    boolean isConvertibleTo(MethodType newType){
        MethodTypeForm oldForm=this.form();
        MethodTypeForm newForm=newType.form();
        if(oldForm==newForm)
            // same parameter count, same primitive/object mix
            return true;
        if(!canConvert(returnType(),newType.returnType()))
            return false;
        Class<?>[] srcTypes=newType.ptypes;
        Class<?>[] dstTypes=ptypes;
        if(srcTypes==dstTypes)
            return true;
        int argc;
        if((argc=srcTypes.length)!=dstTypes.length)
            return false;
        if(argc<=1){
            if(argc==1&&!canConvert(srcTypes[0],dstTypes[0]))
                return false;
            return true;
        }
        if((oldForm.primitiveParameterCount()==0&&oldForm.erasedType==this)||
                (newForm.primitiveParameterCount()==0&&newForm.erasedType==newType)){
            // Somewhat complicated test to avoid a loop of 2 or more trips.
            // If either type has only Object parameters, we know we can convert.
            assert (canConvertParameters(srcTypes,dstTypes));
            return true;
        }
        return canConvertParameters(srcTypes,dstTypes);
    }

    MethodTypeForm form(){
        return form;
    }

    private boolean canConvertParameters(Class<?>[] srcTypes,Class<?>[] dstTypes){
        for(int i=0;i<srcTypes.length;i++){
            if(!canConvert(srcTypes[i],dstTypes[i])){
                return false;
            }
        }
        return true;
    }

    static boolean canConvert(Class<?> src,Class<?> dst){
        // short-circuit a few cases:
        if(src==dst||src==Object.class||dst==Object.class) return true;
        // the remainder of this logic is documented in MethodHandle.asType
        if(src.isPrimitive()){
            // can force void to an explicit null, a la reflect.Method.invoke
            // can also force void to a primitive zero, by analogy
            if(src==void.class) return true;  //or !dst.isPrimitive()?
            Wrapper sw=Wrapper.forPrimitiveType(src);
            if(dst.isPrimitive()){
                // P->P must widen
                return Wrapper.forPrimitiveType(dst).isConvertibleFrom(sw);
            }else{
                // P->R must box and widen
                return dst.isAssignableFrom(sw.wrapperType());
            }
        }else if(dst.isPrimitive()){
            // any value can be dropped
            if(dst==void.class) return true;
            Wrapper dw=Wrapper.forPrimitiveType(dst);
            // R->P must be able to unbox (from a dynamically chosen type) and widen
            // For example:
            //   Byte/Number/Comparable/Object -> dw:Byte -> byte.
            //   Character/Comparable/Object -> dw:Character -> char
            //   Boolean/Comparable/Object -> dw:Boolean -> boolean
            // This means that dw must be cast-compatible with src.
            if(src.isAssignableFrom(dw.wrapperType())){
                return true;
            }
            // The above does not work if the source reference is strongly typed
            // to a wrapper whose primitive must be widened.  For example:
            //   Byte -> unbox:byte -> short/int/long/float/double
            //   Character -> unbox:char -> int/long/float/double
            if(Wrapper.isWrapperType(src)&&
                    dw.isConvertibleFrom(Wrapper.forWrapperType(src))){
                // can unbox from src and then widen to dst
                return true;
            }
            // We have already covered cases which arise due to runtime unboxing
            // of a reference type which covers several wrapper types:
            //   Object -> cast:Integer -> unbox:int -> long/float/double
            //   Serializable -> cast:Byte -> unbox:byte -> byte/short/int/long/float/double
            // An marginal case is Number -> dw:Character -> char, which would be OK if there were a
            // subclass of Number which wraps a value that can convert to char.
            // Since there is none, we don't need an extra check here to cover char or boolean.
            return false;
        }else{
            // R->R always works, since null is always valid dynamically
            return true;
        }
    }

    boolean explicitCastEquivalentToAsType(MethodType newType){
        if(this==newType) return true;
        if(!explicitCastEquivalentToAsType(rtype,newType.rtype)){
            return false;
        }
        Class<?>[] srcTypes=newType.ptypes;
        Class<?>[] dstTypes=ptypes;
        if(dstTypes==srcTypes){
            return true;
        }
        assert (dstTypes.length==srcTypes.length);
        for(int i=0;i<dstTypes.length;i++){
            if(!explicitCastEquivalentToAsType(srcTypes[i],dstTypes[i])){
                return false;
            }
        }
        return true;
    }

    private static boolean explicitCastEquivalentToAsType(Class<?> src,Class<?> dst){
        if(src==dst||dst==Object.class||dst==void.class) return true;
        if(src.isPrimitive()){
            // Could be a prim/prim conversion, where casting is a strict superset.
            // Or a boxing conversion, which is always to an exact wrapper class.
            return canConvert(src,dst);
        }else if(dst.isPrimitive()){
            // Unboxing behavior is different between MHs.eCA & MH.asType (see 3b).
            return false;
        }else{
            // R->R always works, but we have to avoid a check-cast to an interface.
            return !dst.isInterface()||dst.isAssignableFrom(src);
        }
    }
    /// Queries which have to do with the bytecode architecture

    Invokers invokers(){
        Invokers inv=invokers;
        if(inv!=null) return inv;
        invokers=inv=new Invokers(this);
        return inv;
    }

    int parameterSlotDepth(int num){
        if(num<0||num>ptypes.length)
            parameterType(num);  // force a range check
        return form.parameterToArgSlot(num-1);
    }

    int returnSlotCount(){
        return form.returnSlotCount();
    }

    public String toMethodDescriptorString(){
        String desc=methodDescriptor;
        if(desc==null){
            desc=BytecodeDescriptor.unparse(this);
            methodDescriptor=desc;
        }
        return desc;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();  // requires serialPersistentFields to be an empty array
        s.writeObject(returnType());
        s.writeObject(parameterArray());
    }

    public Class<?>[] parameterArray(){
        return ptypes.clone();
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException{
        s.defaultReadObject();  // requires serialPersistentFields to be an empty array
        Class<?> returnType=(Class<?>)s.readObject();
        Class<?>[] parameterArray=(Class<?>[])s.readObject();
        // Probably this object will never escape, but let's check
        // the field values now, just to be sure.
        checkRtype(returnType);
        checkPtypes(parameterArray);
        parameterArray=parameterArray.clone();  // make sure it is unshared
        MethodType_init(returnType,parameterArray);
    }

    private void MethodType_init(Class<?> rtype,Class<?>[] ptypes){
        // In order to communicate these values to readResolve, we must
        // store them into the implementation-specific final fields.
        checkRtype(rtype);
        checkPtypes(ptypes);
        UNSAFE.putObject(this,rtypeOffset,rtype);
        UNSAFE.putObject(this,ptypesOffset,ptypes);
    }

    private Object readResolve(){
        // Do not use a trusted path for deserialization:
        //return makeImpl(rtype, ptypes, true);
        // Verify all operands, and make sure ptypes is unshared:
        return methodType(rtype,ptypes);
    }

    private static class ConcurrentWeakInternSet<T>{
        private final ConcurrentMap<WeakEntry<T>,WeakEntry<T>> map;
        private final ReferenceQueue<T> stale;

        public ConcurrentWeakInternSet(){
            this.map=new ConcurrentHashMap<>();
            this.stale=new ReferenceQueue<>();
        }

        public T get(T elem){
            if(elem==null) throw new NullPointerException();
            expungeStaleElements();
            WeakEntry<T> value=map.get(new WeakEntry<>(elem));
            if(value!=null){
                T res=value.get();
                if(res!=null){
                    return res;
                }
            }
            return null;
        }

        private void expungeStaleElements(){
            Reference<? extends T> reference;
            while((reference=stale.poll())!=null){
                map.remove(reference);
            }
        }

        public T add(T elem){
            if(elem==null) throw new NullPointerException();
            // Playing double race here, and so spinloop is required.
            // First race is with two concurrent updaters.
            // Second race is with GC purging weak ref under our feet.
            // Hopefully, we almost always end up with a single pass.
            T interned;
            WeakEntry<T> e=new WeakEntry<>(elem,stale);
            do{
                expungeStaleElements();
                WeakEntry<T> exist=map.putIfAbsent(e,e);
                interned=(exist==null)?elem:exist.get();
            }while(interned==null);
            return interned;
        }

        private static class WeakEntry<T> extends WeakReference<T>{
            public final int hashcode;

            public WeakEntry(T key,ReferenceQueue<T> queue){
                super(key,queue);
                hashcode=key.hashCode();
            }

            public WeakEntry(T key){
                super(key);
                hashcode=key.hashCode();
            }

            @Override
            public boolean equals(Object obj){
                if(obj instanceof WeakEntry){
                    Object that=((WeakEntry)obj).get();
                    Object mine=get();
                    return (that==null||mine==null)?(this==obj):mine.equals(that);
                }
                return false;
            }

            @Override
            public int hashCode(){
                return hashcode;
            }
        }
    }






}
