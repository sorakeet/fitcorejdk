/**
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.reflect;

import sun.reflect.ReflectionFactory;

import java.security.AccessController;

public class Modifier{
    public static final int PUBLIC=0x00000001;
    public static final int PRIVATE=0x00000002;
    public static final int PROTECTED=0x00000004;
    public static final int STATIC=0x00000008;
    public static final int FINAL=0x00000010;
    public static final int SYNCHRONIZED=0x00000020;
    public static final int VOLATILE=0x00000040;
    public static final int TRANSIENT=0x00000080;
    public static final int NATIVE=0x00000100;
    public static final int INTERFACE=0x00000200;
    public static final int ABSTRACT=0x00000400;
    public static final int STRICT=0x00000800;
    // Bits not (yet) exposed in the public API either because they
    // have different meanings for fields and methods and there is no
    // way to distinguish between the two in this class, or because
    // they are not Java programming language keywords
    static final int BRIDGE=0x00000040;
    static final int VARARGS=0x00000080;
    static final int SYNTHETIC=0x00001000;
    static final int ANNOTATION=0x00002000;
    static final int ENUM=0x00004000;
    static final int MANDATED=0x00008000;
    static final int ACCESS_MODIFIERS=
            Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE;
    // Note on the FOO_MODIFIERS fields and fooModifiers() methods:
    // the sets of modifiers are not guaranteed to be constants
    // across time and Java SE releases. Therefore, it would not be
    // appropriate to expose an external interface to this information
    // that would allow the values to be treated as Java-level
    // constants since the values could be constant folded and updates
    // to the sets of modifiers missed. Thus, the fooModifiers()
    // methods return an unchanging values for a given release, but a
    // value that can potentially change over time.
    private static final int CLASS_MODIFIERS=
            Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE|
                    Modifier.ABSTRACT|Modifier.STATIC|Modifier.FINAL|
                    Modifier.STRICT;
    private static final int INTERFACE_MODIFIERS=
            Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE|
                    Modifier.ABSTRACT|Modifier.STATIC|Modifier.STRICT;
    private static final int CONSTRUCTOR_MODIFIERS=
            Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE;
    private static final int METHOD_MODIFIERS=
            Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE|
                    Modifier.ABSTRACT|Modifier.STATIC|Modifier.FINAL|
                    Modifier.SYNCHRONIZED|Modifier.NATIVE|Modifier.STRICT;
    private static final int FIELD_MODIFIERS=
            Modifier.PUBLIC|Modifier.PROTECTED|Modifier.PRIVATE|
                    Modifier.STATIC|Modifier.FINAL|Modifier.TRANSIENT|
                    Modifier.VOLATILE;
    private static final int PARAMETER_MODIFIERS=
            Modifier.FINAL;

    /**
     * Bootstrapping protocol between java.lang and java.lang.reflect
     *  packages
     */
    static{
        ReflectionFactory factory=
                AccessController.doPrivileged(
                        new ReflectionFactory.GetReflectionFactoryAction());
        factory.setLangReflectAccess(new ReflectAccess());
    }

    public static boolean isPublic(int mod){
        return (mod&PUBLIC)!=0;
    }

    public static boolean isPrivate(int mod){
        return (mod&PRIVATE)!=0;
    }

    public static boolean isProtected(int mod){
        return (mod&PROTECTED)!=0;
    }

    public static boolean isStatic(int mod){
        return (mod&STATIC)!=0;
    }

    public static boolean isFinal(int mod){
        return (mod&FINAL)!=0;
    }

    public static boolean isSynchronized(int mod){
        return (mod&SYNCHRONIZED)!=0;
    }

    public static boolean isVolatile(int mod){
        return (mod&VOLATILE)!=0;
    }

    public static boolean isTransient(int mod){
        return (mod&TRANSIENT)!=0;
    }

    public static boolean isNative(int mod){
        return (mod&NATIVE)!=0;
    }

    public static boolean isInterface(int mod){
        return (mod&INTERFACE)!=0;
    }

    public static boolean isAbstract(int mod){
        return (mod&ABSTRACT)!=0;
    }

    public static boolean isStrict(int mod){
        return (mod&STRICT)!=0;
    }

    public static String toString(int mod){
        StringBuilder sb=new StringBuilder();
        int len;
        if((mod&PUBLIC)!=0) sb.append("public ");
        if((mod&PROTECTED)!=0) sb.append("protected ");
        if((mod&PRIVATE)!=0) sb.append("private ");
        /** Canonical order */
        if((mod&ABSTRACT)!=0) sb.append("abstract ");
        if((mod&STATIC)!=0) sb.append("static ");
        if((mod&FINAL)!=0) sb.append("final ");
        if((mod&TRANSIENT)!=0) sb.append("transient ");
        if((mod&VOLATILE)!=0) sb.append("volatile ");
        if((mod&SYNCHRONIZED)!=0) sb.append("synchronized ");
        if((mod&NATIVE)!=0) sb.append("native ");
        if((mod&STRICT)!=0) sb.append("strictfp ");
        if((mod&INTERFACE)!=0) sb.append("interface ");
        if((len=sb.length())>0)    /** trim trailing space */
            return sb.toString().substring(0,len-1);
        return "";
    }

    static boolean isSynthetic(int mod){
        return (mod&SYNTHETIC)!=0;
    }

    static boolean isMandated(int mod){
        return (mod&MANDATED)!=0;
    }

    public static int classModifiers(){
        return CLASS_MODIFIERS;
    }

    public static int interfaceModifiers(){
        return INTERFACE_MODIFIERS;
    }

    public static int constructorModifiers(){
        return CONSTRUCTOR_MODIFIERS;
    }

    public static int methodModifiers(){
        return METHOD_MODIFIERS;
    }

    public static int fieldModifiers(){
        return FIELD_MODIFIERS;
    }

    public static int parameterModifiers(){
        return PARAMETER_MODIFIERS;
    }
}
