/**
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

public final class Compiler{
    static{
        registerNatives();
        java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>(){
                    public Void run(){
                        boolean loaded=false;
                        String jit=System.getProperty("java.compiler");
                        if((jit!=null)&&(!jit.equals("NONE"))&&
                                (!jit.equals(""))){
                            try{
                                System.loadLibrary(jit);
                                initialize();
                                loaded=true;
                            }catch(UnsatisfiedLinkError e){
                                System.err.println("Warning: JIT compiler \""+
                                        jit+"\" not found. Will use interpreter.");
                            }
                        }
                        String info=System.getProperty("java.vm.info");
                        if(loaded){
                            System.setProperty("java.vm.info",info+", "+jit);
                        }else{
                            System.setProperty("java.vm.info",info+", nojit");
                        }
                        return null;
                    }
                });
    }

    private Compiler(){
    }               // don't make instances

    private static native void initialize();

    private static native void registerNatives();

    public static native boolean compileClass(Class<?> clazz);

    public static native boolean compileClasses(String string);

    public static native Object command(Object any);

    public static native void enable();

    public static native void disable();
}
