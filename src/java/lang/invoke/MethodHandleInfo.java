/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.invoke;

import java.lang.invoke.MethodHandleNatives.Constants;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Objects;

import static java.lang.invoke.MethodHandleStatics.newIllegalArgumentException;

public interface MethodHandleInfo{
    public static final int
            REF_getField=Constants.REF_getField,
            REF_getStatic=Constants.REF_getStatic,
            REF_putField=Constants.REF_putField,
            REF_putStatic=Constants.REF_putStatic,
            REF_invokeVirtual=Constants.REF_invokeVirtual,
            REF_invokeStatic=Constants.REF_invokeStatic,
            REF_invokeSpecial=Constants.REF_invokeSpecial,
            REF_newInvokeSpecial=Constants.REF_newInvokeSpecial,
            REF_invokeInterface=Constants.REF_invokeInterface;

    public static String toString(int kind,Class<?> defc,String name,MethodType type){
        Objects.requireNonNull(name);
        Objects.requireNonNull(type);
        return String.format("%s %s.%s:%s",referenceKindToString(kind),defc.getName(),name,type);
    }

    public static String referenceKindToString(int referenceKind){
        if(!MethodHandleNatives.refKindIsValid(referenceKind))
            throw newIllegalArgumentException("invalid reference kind",referenceKind);
        return MethodHandleNatives.refKindName((byte)referenceKind);
    }

    public Class<?> getDeclaringClass();

    public String getName();
    // Utility methods.
    // NOTE: class/name/type and reference kind constitute a symbolic reference
    // member and modifiers are an add-on, derived from Core Reflection (or the equivalent)

    public MethodType getMethodType();

    public <T extends Member> T reflectAs(Class<T> expected,Lookup lookup);

    // spelling derived from java.lang.reflect.Executable, not MethodHandle.isVarargsCollector
    public default boolean isVarArgs(){
        // fields are never varargs:
        if(MethodHandleNatives.refKindIsField((byte)getReferenceKind()))
            return false;
        // not in the public API: Modifier.VARARGS
        final int ACC_VARARGS=0x00000080;  // from JVMS 4.6 (Table 4.20)
        assert (ACC_VARARGS==Modifier.TRANSIENT);
        return Modifier.isTransient(getModifiers());
    }

    public int getReferenceKind();

    public int getModifiers();
}
