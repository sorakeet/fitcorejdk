/**
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.invoke;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import static java.lang.invoke.MethodHandleNatives.Constants.*;
import static java.lang.invoke.MethodHandleStatics.TRACE_METHOD_LINKAGE;
import static java.lang.invoke.MethodHandles.Lookup.IMPL_LOOKUP;

class MethodHandleNatives{
    static final boolean COUNT_GWT;
    /// MemberName support

    static{
        registerNatives();
        COUNT_GWT=getConstant(Constants.GC_COUNT_GWT)!=0;
        // The JVM calls MethodHandleNatives.<clinit>.  Cascade the <clinit> calls as needed:
        MethodHandleImpl.initStatics();
    }

    static{
        final int HR_MASK=((1<<REF_getField)|
                (1<<REF_putField)|
                (1<<REF_invokeVirtual)|
                (1<<REF_invokeSpecial)|
                (1<<REF_invokeInterface)
        );
        for(byte refKind=REF_NONE+1;refKind<REF_LIMIT;refKind++){
            assert (refKindHasReceiver(refKind)==(((1<<refKind)&HR_MASK)!=0)):refKind;
        }
    }

    static{
        assert (verifyConstants());
    }

    private MethodHandleNatives(){
    } // static only

    static native void init(MemberName self,Object ref);

    static native void expand(MemberName self);

    static native MemberName resolve(MemberName self,Class<?> caller) throws LinkageError, ClassNotFoundException;

    static native int getMembers(Class<?> defc,String matchName,String matchSig,
                                 int matchFlags,Class<?> caller,int skip,MemberName[] results);
    /// MethodHandle support

    /// Field layout queries parallel to sun.misc.Unsafe:
    static native long objectFieldOffset(MemberName self);  // e.g., returns vmindex

    static native long staticFieldOffset(MemberName self);  // e.g., returns vmindex
    /// CallSite support

    static native Object staticFieldBase(MemberName self);  // e.g., returns clazz

    static native Object getMemberVMInfo(MemberName self);  // returns {vmindex,vmtarget}

    static native int getConstant(int which);

    static native void setCallSiteTargetNormal(CallSite site,MethodHandle target);

    static native void setCallSiteTargetVolatile(CallSite site,MethodHandle target);

    private static native void registerNatives();

    static boolean refKindIsSetter(byte refKind){
        return refKindIsField(refKind)&&!refKindIsGetter(refKind);
    }

    static boolean refKindIsField(byte refKind){
        assert (refKindIsValid(refKind));
        return (refKind<=REF_putStatic);
    }

    static boolean refKindIsValid(int refKind){
        return (refKind>REF_NONE&&refKind<REF_LIMIT);
    }

    static boolean refKindIsGetter(byte refKind){
        assert (refKindIsValid(refKind));
        return (refKind<=REF_getStatic);
    }

    static boolean refKindIsMethod(byte refKind){
        return !refKindIsField(refKind)&&(refKind!=REF_newInvokeSpecial);
    }

    static boolean refKindIsConstructor(byte refKind){
        return (refKind==REF_newInvokeSpecial);
    }

    static boolean refKindIsStatic(byte refKind){
        return !refKindHasReceiver(refKind)&&(refKind!=REF_newInvokeSpecial);
    }

    static boolean refKindHasReceiver(byte refKind){
        assert (refKindIsValid(refKind));
        return (refKind&1)!=0;
    }

    static boolean refKindDoesDispatch(byte refKind){
        assert (refKindIsValid(refKind));
        return (refKind==REF_invokeVirtual||
                refKind==REF_invokeInterface);
    }

    static String refKindName(byte refKind){
        assert (refKindIsValid(refKind));
        switch(refKind){
            case REF_getField:
                return "getField";
            case REF_getStatic:
                return "getStatic";
            case REF_putField:
                return "putField";
            case REF_putStatic:
                return "putStatic";
            case REF_invokeVirtual:
                return "invokeVirtual";
            case REF_invokeStatic:
                return "invokeStatic";
            case REF_invokeSpecial:
                return "invokeSpecial";
            case REF_newInvokeSpecial:
                return "newInvokeSpecial";
            case REF_invokeInterface:
                return "invokeInterface";
            default:
                return "REF_???";
        }
    }

    static boolean verifyConstants(){
        Object[] box={null};
        for(int i=0;;i++){
            box[0]=null;
            int vmval=getNamedCon(i,box);
            if(box[0]==null) break;
            String name=(String)box[0];
            try{
                Field con=Constants.class.getDeclaredField(name);
                int jval=con.getInt(null);
                if(jval==vmval) continue;
                String err=(name+": JVM has "+vmval+" while Java has "+jval);
                if(name.equals("CONV_OP_LIMIT")){
                    System.err.println("warning: "+err);
                    continue;
                }
                throw new InternalError(err);
            }catch(NoSuchFieldException|IllegalAccessException ex){
                String err=(name+": JVM has "+vmval+" which Java does not define");
                // ignore exotic ops the JVM cares about; we just wont issue them
                //System.err.println("warning: "+err);
                continue;
            }
        }
        return true;
    }

    private static native int getNamedCon(int which,Object[] name);

    static MemberName linkCallSite(Object callerObj,
                                   Object bootstrapMethodObj,
                                   Object nameObj,Object typeObj,
                                   Object staticArguments,
                                   Object[] appendixResult){
        MethodHandle bootstrapMethod=(MethodHandle)bootstrapMethodObj;
        Class<?> caller=(Class<?>)callerObj;
        String name=nameObj.toString().intern();
        MethodType type=(MethodType)typeObj;
        if(!TRACE_METHOD_LINKAGE)
            return linkCallSiteImpl(caller,bootstrapMethod,name,type,
                    staticArguments,appendixResult);
        return linkCallSiteTracing(caller,bootstrapMethod,name,type,
                staticArguments,appendixResult);
    }
    // Up-calls from the JVM.
    // These must NOT be public.

    static MemberName linkCallSiteImpl(Class<?> caller,
                                       MethodHandle bootstrapMethod,
                                       String name,MethodType type,
                                       Object staticArguments,
                                       Object[] appendixResult){
        CallSite callSite=CallSite.makeSite(bootstrapMethod,
                name,
                type,
                staticArguments,
                caller);
        if(callSite instanceof ConstantCallSite){
            appendixResult[0]=callSite.dynamicInvoker();
            return Invokers.linkToTargetMethod(type);
        }else{
            appendixResult[0]=callSite;
            return Invokers.linkToCallSiteMethod(type);
        }
    }

    // Tracing logic:
    static MemberName linkCallSiteTracing(Class<?> caller,
                                          MethodHandle bootstrapMethod,
                                          String name,MethodType type,
                                          Object staticArguments,
                                          Object[] appendixResult){
        Object bsmReference=bootstrapMethod.internalMemberName();
        if(bsmReference==null) bsmReference=bootstrapMethod;
        Object staticArglist=(staticArguments instanceof Object[]?
                java.util.Arrays.asList((Object[])staticArguments):
                staticArguments);
        System.out.println("linkCallSite "+caller.getName()+" "+
                bsmReference+" "+
                name+type+"/"+staticArglist);
        try{
            MemberName res=linkCallSiteImpl(caller,bootstrapMethod,name,type,
                    staticArguments,appendixResult);
            System.out.println("linkCallSite => "+res+" + "+appendixResult[0]);
            return res;
        }catch(Throwable ex){
            System.out.println("linkCallSite => throw "+ex);
            throw ex;
        }
    }

    static MethodType findMethodHandleType(Class<?> rtype,Class<?>[] ptypes){
        return MethodType.makeImpl(rtype,ptypes,true);
    }

    static MemberName linkMethod(Class<?> callerClass,int refKind,
                                 Class<?> defc,String name,Object type,
                                 Object[] appendixResult){
        if(!TRACE_METHOD_LINKAGE)
            return linkMethodImpl(callerClass,refKind,defc,name,type,appendixResult);
        return linkMethodTracing(callerClass,refKind,defc,name,type,appendixResult);
    }

    static MemberName linkMethodImpl(Class<?> callerClass,int refKind,
                                     Class<?> defc,String name,Object type,
                                     Object[] appendixResult){
        try{
            if(defc==MethodHandle.class&&refKind==REF_invokeVirtual){
                return Invokers.methodHandleInvokeLinkerMethod(name,fixMethodType(callerClass,type),appendixResult);
            }
        }catch(Throwable ex){
            if(ex instanceof LinkageError)
                throw (LinkageError)ex;
            else
                throw new LinkageError(ex.getMessage(),ex);
        }
        throw new LinkageError("no such method "+defc.getName()+"."+name+type);
    }

    private static MethodType fixMethodType(Class<?> callerClass,Object type){
        if(type instanceof MethodType)
            return (MethodType)type;
        else
            return MethodType.fromMethodDescriptorString((String)type,callerClass.getClassLoader());
    }

    // Tracing logic:
    static MemberName linkMethodTracing(Class<?> callerClass,int refKind,
                                        Class<?> defc,String name,Object type,
                                        Object[] appendixResult){
        System.out.println("linkMethod "+defc.getName()+"."+
                name+type+"/"+Integer.toHexString(refKind));
        try{
            MemberName res=linkMethodImpl(callerClass,refKind,defc,name,type,appendixResult);
            System.out.println("linkMethod => "+res+" + "+appendixResult[0]);
            return res;
        }catch(Throwable ex){
            System.out.println("linkMethod => throw "+ex);
            throw ex;
        }
    }

    static MethodHandle linkMethodHandleConstant(Class<?> callerClass,int refKind,
                                                 Class<?> defc,String name,Object type){
        try{
            Lookup lookup=IMPL_LOOKUP.in(callerClass);
            assert (refKindIsValid(refKind));
            return lookup.linkMethodHandleConstant((byte)refKind,defc,name,type);
        }catch(IllegalAccessException ex){
            Throwable cause=ex.getCause();
            if(cause instanceof AbstractMethodError){
                throw (AbstractMethodError)cause;
            }else{
                Error err=new IllegalAccessError(ex.getMessage());
                throw initCauseFrom(err,ex);
            }
        }catch(NoSuchMethodException ex){
            Error err=new NoSuchMethodError(ex.getMessage());
            throw initCauseFrom(err,ex);
        }catch(NoSuchFieldException ex){
            Error err=new NoSuchFieldError(ex.getMessage());
            throw initCauseFrom(err,ex);
        }catch(ReflectiveOperationException ex){
            Error err=new IncompatibleClassChangeError();
            throw initCauseFrom(err,ex);
        }
    }

    static private Error initCauseFrom(Error err,Exception ex){
        Throwable th=ex.getCause();
        if(err.getClass().isInstance(th))
            return (Error)th;
        err.initCause(th==null?ex:th);
        return err;
    }

    static boolean isCallerSensitive(MemberName mem){
        if(!mem.isInvocable()) return false;  // fields are not caller sensitive
        return mem.isCallerSensitive()||canBeCalledVirtual(mem);
    }

    static boolean canBeCalledVirtual(MemberName mem){
        assert (mem.isInvocable());
        Class<?> defc=mem.getDeclaringClass();
        switch(mem.getName()){
            case "checkMemberAccess":
                return canBeCalledVirtual(mem,SecurityManager.class);
            case "getContextClassLoader":
                return canBeCalledVirtual(mem,Thread.class);
        }
        return false;
    }

    static boolean canBeCalledVirtual(MemberName symbolicRef,Class<?> definingClass){
        Class<?> symbolicRefClass=symbolicRef.getDeclaringClass();
        if(symbolicRefClass==definingClass) return true;
        if(symbolicRef.isStatic()||symbolicRef.isPrivate()) return false;
        return (definingClass.isAssignableFrom(symbolicRefClass)||  // Msym overrides Mdef
                symbolicRefClass.isInterface());                     // Mdef implements Msym
    }

    // All compile-time constants go here.
    // There is an opportunity to check them against the JVM's idea of them.
    static class Constants{
        // MethodHandleImpl
        static final int // for getConstant
                GC_COUNT_GWT=4,
                GC_LAMBDA_SUPPORT=5;
        // MemberName
        // The JVM uses values of -2 and above for vtable indexes.
        // Field values are simple positive offsets.
        // Ref: src/share/vm/oops/methodOop.hpp
        // This value is negative enough to avoid such numbers,
        // but not too negative.
        static final int
                MN_IS_METHOD=0x00010000, // method (not constructor)
                MN_IS_CONSTRUCTOR=0x00020000, // constructor
                MN_IS_FIELD=0x00040000, // field
                MN_IS_TYPE=0x00080000, // nested type
                MN_CALLER_SENSITIVE=0x00100000, // @CallerSensitive annotation detected
                MN_REFERENCE_KIND_SHIFT=24, // refKind
                MN_REFERENCE_KIND_MASK=0x0F000000>>MN_REFERENCE_KIND_SHIFT,
        // The SEARCH_* bits are not for MN.flags but for the matchFlags argument of MHN.getMembers:
        MN_SEARCH_SUPERCLASSES=0x00100000,
                MN_SEARCH_INTERFACES=0x00200000;
        static final int
                T_BOOLEAN=4,
                T_CHAR=5,
                T_FLOAT=6,
                T_DOUBLE=7,
                T_BYTE=8,
                T_SHORT=9,
                T_INT=10,
                T_LONG=11,
                T_OBJECT=12,
        //T_ARRAY    = 13
        T_VOID=14,
        //T_ADDRESS  = 15
        T_ILLEGAL=99;
        static final byte
                CONSTANT_Utf8=1,
                CONSTANT_Integer=3,
                CONSTANT_Float=4,
                CONSTANT_Long=5,
                CONSTANT_Double=6,
                CONSTANT_Class=7,
                CONSTANT_String=8,
                CONSTANT_Fieldref=9,
                CONSTANT_Methodref=10,
                CONSTANT_InterfaceMethodref=11,
                CONSTANT_NameAndType=12,
                CONSTANT_MethodHandle=15,  // JSR 292
                CONSTANT_MethodType=16,  // JSR 292
                CONSTANT_InvokeDynamic=18,
                CONSTANT_LIMIT=19;   // Limit to tags found in classfiles
        static final char
                ACC_PUBLIC=0x0001,
                ACC_PRIVATE=0x0002,
                ACC_PROTECTED=0x0004,
                ACC_STATIC=0x0008,
                ACC_FINAL=0x0010,
                ACC_SYNCHRONIZED=0x0020,
                ACC_VOLATILE=0x0040,
                ACC_TRANSIENT=0x0080,
                ACC_NATIVE=0x0100,
                ACC_INTERFACE=0x0200,
                ACC_ABSTRACT=0x0400,
                ACC_STRICT=0x0800,
                ACC_SYNTHETIC=0x1000,
                ACC_ANNOTATION=0x2000,
                ACC_ENUM=0x4000,
        // aliases:
        ACC_SUPER=ACC_SYNCHRONIZED,
                ACC_BRIDGE=ACC_VOLATILE,
                ACC_VARARGS=ACC_TRANSIENT;
        static final byte
                REF_NONE=0,  // null value
                REF_getField=1,
                REF_getStatic=2,
                REF_putField=3,
                REF_putStatic=4,
                REF_invokeVirtual=5,
                REF_invokeStatic=6,
                REF_invokeSpecial=7,
                REF_newInvokeSpecial=8,
                REF_invokeInterface=9,
                REF_LIMIT=10;

        Constants(){
        } // static only
    }
}
