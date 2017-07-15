/**
 * Copyright (c) 1995, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.omg.CORBA;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.OutputStream;
import sun.reflect.misc.ReflectUtil;

import java.applet.Applet;
import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

abstract public class ORB{
    //
    // This is the ORB implementation used when nothing else is specified.
    // Whoever provides this class customizes this string to
    // point at their ORB implementation.
    //
    private static final String ORBClassKey="org.omg.CORBA.ORBClass";
    private static final String ORBSingletonClassKey="org.omg.CORBA.ORBSingletonClass";
    //
    // The global instance of the singleton ORB implementation which
    // acts as a factory for typecodes for generated Helper classes.
    // TypeCodes should be immutable since they may be shared across
    // different security contexts (applets). There should be no way to
    // use a TypeCode as a storage depot for illicitly passing
    // information or Java objects between different security contexts.
    //
    static private ORB singleton;

    public static synchronized ORB init(){
        if(singleton==null){
            String className=getSystemProperty(ORBSingletonClassKey);
            if(className==null)
                className=getPropertyFromFile(ORBSingletonClassKey);
            if((className==null)||
                    (className.equals("com.sun.corba.se.impl.orb.ORBSingleton"))){
                singleton=new com.sun.corba.se.impl.orb.ORBSingleton();
            }else{
                singleton=create_impl(className);
            }
        }
        return singleton;
    }

    // Get System property
    private static String getSystemProperty(final String name){
        // This will not throw a SecurityException because this
        // class was loaded from rt.jar using the bootstrap classloader.
        String propValue=(String)AccessController.doPrivileged(
                new PrivilegedAction(){
                    public java.lang.Object run(){
                        return System.getProperty(name);
                    }
                }
        );
        return propValue;
    }

    // Get property from orb.properties in either <user.home> or <java-home>/lib
    // directories.
    private static String getPropertyFromFile(final String name){
        // This will not throw a SecurityException because this
        // class was loaded from rt.jar using the bootstrap classloader.
        String propValue=(String)AccessController.doPrivileged(
                new PrivilegedAction(){
                    private Properties getFileProperties(String fileName){
                        try{
                            File propFile=new File(fileName);
                            if(!propFile.exists())
                                return null;
                            Properties props=new Properties();
                            FileInputStream fis=new FileInputStream(propFile);
                            try{
                                props.load(fis);
                            }finally{
                                fis.close();
                            }
                            return props;
                        }catch(Exception exc){
                            return null;
                        }
                    }

                    public java.lang.Object run(){
                        String userHome=System.getProperty("user.home");
                        String fileName=userHome+File.separator+
                                "orb.properties";
                        Properties props=getFileProperties(fileName);
                        if(props!=null){
                            String value=props.getProperty(name);
                            if(value!=null)
                                return value;
                        }
                        String javaHome=System.getProperty("java.home");
                        fileName=javaHome+File.separator
                                +"lib"+File.separator+"orb.properties";
                        props=getFileProperties(fileName);
                        if(props==null)
                            return null;
                        else
                            return props.getProperty(name);
                    }
                }
        );
        return propValue;
    }

    private static ORB create_impl(String className){
        ClassLoader cl=Thread.currentThread().getContextClassLoader();
        if(cl==null)
            cl=ClassLoader.getSystemClassLoader();
        try{
            ReflectUtil.checkPackageAccess(className);
            Class<ORB> orbBaseClass=ORB.class;
            Class<?> orbClass=Class.forName(className,true,cl).asSubclass(orbBaseClass);
            return (ORB)orbClass.newInstance();
        }catch(Throwable ex){
            SystemException systemException=new INITIALIZE(
                    "can't instantiate default ORB implementation "+className);
            systemException.initCause(ex);
            throw systemException;
        }
    }

    public static ORB init(String[] args,Properties props){
        //
        // Note that there is no standard command-line argument for
        // specifying the default ORB implementation. For an
        // application you can choose an implementation either by
        // setting the CLASSPATH to pick a different org.omg.CORBA
        // and it's baked-in ORB implementation default or by
        // setting an entry in the properties object or in the
        // system properties.
        //
        String className=null;
        ORB orb;
        if(props!=null)
            className=props.getProperty(ORBClassKey);
        if(className==null)
            className=getSystemProperty(ORBClassKey);
        if(className==null)
            className=getPropertyFromFile(ORBClassKey);
        if((className==null)||
                (className.equals("com.sun.corba.se.impl.orb.ORBImpl"))){
            orb=new com.sun.corba.se.impl.orb.ORBImpl();
        }else{
            orb=create_impl(className);
        }
        orb.set_parameters(args,props);
        return orb;
    }

    public static ORB init(Applet app,Properties props){
        String className;
        ORB orb;
        className=app.getParameter(ORBClassKey);
        if(className==null&&props!=null)
            className=props.getProperty(ORBClassKey);
        if(className==null)
            className=getSystemProperty(ORBClassKey);
        if(className==null)
            className=getPropertyFromFile(ORBClassKey);
        if((className==null)||
                (className.equals("com.sun.corba.se.impl.orb.ORBImpl"))){
            orb=new com.sun.corba.se.impl.orb.ORBImpl();
        }else{
            orb=create_impl(className);
        }
        orb.set_parameters(app,props);
        return orb;
    }

    abstract protected void set_parameters(String[] args,Properties props);

    abstract protected void set_parameters(Applet app,Properties props);

    public void connect(Object obj){
        throw new NO_IMPLEMENT();
    }

    public void destroy(){
        throw new NO_IMPLEMENT();
    }

    public void disconnect(Object obj){
        throw new NO_IMPLEMENT();
    }
    //
    // ORB method implementations.
    //
    // We are trying to accomplish 2 things at once in this class.
    // It can act as a default ORB implementation front-end,
    // creating an actual ORB implementation object which is a
    // subclass of this ORB class and then delegating the method
    // implementations.
    //
    // To accomplish the delegation model, the 'delegate' private instance
    // variable is set if an instance of this class is created directly.
    //

    abstract public String[] list_initial_services();

    abstract public Object resolve_initial_references(String object_name)
            throws InvalidName;

    abstract public String object_to_string(Object obj);

    abstract public Object string_to_object(String str);

    abstract public NVList create_list(int count);

    public NVList create_operation_list(Object oper){
        // If we came here, it means that the actual ORB implementation
        // did not have a create_operation_list(...CORBA.Object oper) method,
        // so lets check if it has a create_operation_list(OperationDef oper)
        // method.
        try{
            // First try to load the OperationDef class
            String opDefClassName="org.omg.CORBA.OperationDef";
            Class<?> opDefClass=null;
            ClassLoader cl=Thread.currentThread().getContextClassLoader();
            if(cl==null)
                cl=ClassLoader.getSystemClassLoader();
            // if this throws a ClassNotFoundException, it will be caught below.
            opDefClass=Class.forName(opDefClassName,true,cl);
            // OK, we loaded OperationDef. Now try to get the
            // create_operation_list(OperationDef oper) method.
            Class<?>[] argc={opDefClass};
            java.lang.reflect.Method meth=
                    this.getClass().getMethod("create_operation_list",argc);
            // OK, the method exists, so invoke it and be happy.
            java.lang.Object[] argx={oper};
            return (NVList)meth.invoke(this,argx);
        }catch(java.lang.reflect.InvocationTargetException exs){
            Throwable t=exs.getTargetException();
            if(t instanceof Error){
                throw (Error)t;
            }else if(t instanceof RuntimeException){
                throw (RuntimeException)t;
            }else{
                throw new NO_IMPLEMENT();
            }
        }catch(RuntimeException ex){
            throw ex;
        }catch(Exception exr){
            throw new NO_IMPLEMENT();
        }
    }

    abstract public NamedValue create_named_value(String s,Any any,int flags);

    abstract public ExceptionList create_exception_list();

    abstract public ContextList create_context_list();

    abstract public Context get_default_context();

    abstract public Environment create_environment();

    abstract public OutputStream create_output_stream();

    abstract public void send_multiple_requests_oneway(Request[] req);

    abstract public void send_multiple_requests_deferred(Request[] req);

    abstract public boolean poll_next_response();

    abstract public Request get_next_response() throws WrongTransaction;

    abstract public TypeCode get_primitive_tc(TCKind tcKind);

    abstract public TypeCode create_struct_tc(String id,String name,
                                              StructMember[] members);

    abstract public TypeCode create_union_tc(String id,String name,
                                             TypeCode discriminator_type,
                                             UnionMember[] members);

    abstract public TypeCode create_enum_tc(String id,String name,String[] members);

    abstract public TypeCode create_alias_tc(String id,String name,
                                             TypeCode original_type);

    abstract public TypeCode create_exception_tc(String id,String name,
                                                 StructMember[] members);

    abstract public TypeCode create_interface_tc(String id,String name);

    abstract public TypeCode create_string_tc(int bound);

    abstract public TypeCode create_wstring_tc(int bound);

    abstract public TypeCode create_sequence_tc(int bound,TypeCode element_type);

    @Deprecated
    abstract public TypeCode create_recursive_sequence_tc(int bound,int offset);

    abstract public TypeCode create_array_tc(int length,TypeCode element_type);

    public TypeCode create_native_tc(String id,
                                     String name){
        throw new NO_IMPLEMENT();
    }

    public TypeCode create_abstract_interface_tc(
            String id,
            String name){
        throw new NO_IMPLEMENT();
    }

    public TypeCode create_fixed_tc(short digits,short scale){
        throw new NO_IMPLEMENT();
    }
    // orbos 98-01-18: Objects By Value -- begin

    public TypeCode create_value_tc(String id,
                                    String name,
                                    short type_modifier,
                                    TypeCode concrete_base,
                                    ValueMember[] members){
        throw new NO_IMPLEMENT();
    }

    public TypeCode create_recursive_tc(String id){
        // implemented in subclass
        throw new NO_IMPLEMENT();
    }

    public TypeCode create_value_box_tc(String id,
                                        String name,
                                        TypeCode boxed_type){
        // implemented in subclass
        throw new NO_IMPLEMENT();
    }
    // orbos 98-01-18: Objects By Value -- end

    abstract public Any create_any();

    @Deprecated
    public Current get_current(){
        throw new NO_IMPLEMENT();
    }

    public void run(){
        throw new NO_IMPLEMENT();
    }

    public void shutdown(boolean wait_for_completion){
        throw new NO_IMPLEMENT();
    }

    public boolean work_pending(){
        throw new NO_IMPLEMENT();
    }

    public void perform_work(){
        throw new NO_IMPLEMENT();
    }

    public boolean get_service_information(short service_type,
                                           ServiceInformationHolder service_info){
        throw new NO_IMPLEMENT();
    }
    // orbos 98-01-18: Objects By Value -- begin

    @Deprecated
    public DynAny create_dyn_any(Any value){
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public DynAny create_basic_dyn_any(TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode{
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public DynStruct create_dyn_struct(TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode{
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public DynSequence create_dyn_sequence(TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode{
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public DynArray create_dyn_array(TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode{
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public DynUnion create_dyn_union(TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode{
        throw new NO_IMPLEMENT();
    }

    @Deprecated
    public DynEnum create_dyn_enum(TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode{
        throw new NO_IMPLEMENT();
    }

    public Policy create_policy(int type,Any val)
            throws PolicyError{
        // Currently not implemented until PIORB.
        throw new NO_IMPLEMENT();
    }
}
