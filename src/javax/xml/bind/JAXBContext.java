/**
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.bind;

import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class JAXBContext{
    public static final String JAXB_CONTEXT_FACTORY=
            "javax.xml.bind.context.factory";

    protected JAXBContext(){
    }

    public static JAXBContext newInstance(String contextPath)
            throws JAXBException{
        //return newInstance( contextPath, JAXBContext.class.getClassLoader() );
        return newInstance(contextPath,getContextClassLoader());
    }

    public static JAXBContext newInstance(String contextPath,ClassLoader classLoader) throws JAXBException{
        return newInstance(contextPath,classLoader,Collections.<String,Object>emptyMap());
    }

    public static JAXBContext newInstance(String contextPath,ClassLoader classLoader,Map<String,?> properties)
            throws JAXBException{
        return ContextFinder.find(
                /** The default property name according to the JAXB spec */
                JAXB_CONTEXT_FACTORY,
                /** the context path supplied by the client app */
                contextPath,
                /** class loader to be used */
                classLoader,
                properties);
    }
// TODO: resurrect this once we introduce external annotations
//    /**
//     * <p>
//     * Obtain a new instance of a <tt>JAXBContext</tt> class.
//     *
//     * <p>
//     * The client application must supply a list of classes that the new
//     * context object needs to recognize.
//     *
//     * Not only the new context will recognize all the classes specified,
//     * but it will also recognize any classes that are directly/indirectly
//     * referenced statically from the specified classes.
//     *
//     * For example, in the following Java code, if you do
//     * <tt>newInstance(Foo.class)</tt>, the newly created {@link JAXBContext}
//     * will recognize both <tt>Foo</tt> and <tt>Bar</tt>, but not <tt>Zot</tt>:
//     * <pre>
//     * class Foo {
//     *      Bar b;
//     * }
//     * class Bar { int x; }
//     * class Zot extends Bar { int y; }
//     * </pre>
//     *
//     * Therefore, a typical client application only needs to specify the
//     * top-level classes, but it needs to be careful.
//     *
//     * TODO: if we are to define other mechanisms, refer to them.
//     *
//     * @param externalBindings
//     *      list of external binding files. Can be null or empty if none is used.
//     *      when specified, those files determine how the classes are bound.
//     *
//     * @param classesToBeBound
//     *      list of java classes to be recognized by the new {@link JAXBContext}.
//     *      Can be empty, in which case a {@link JAXBContext} that only knows about
//     *      spec-defined classes will be returned.
//     *
//     * @return
//     *      A new instance of a <tt>JAXBContext</tt>. Always non-null valid object.
//     *
//     * @throws JAXBException
//     *      if an error was encountered while creating the
//     *      <tt>JAXBContext</tt>, such as (but not limited to):
//     * <ol>
//     *  <li>No JAXB implementation was discovered
//     *  <li>Classes use JAXB annotations incorrectly
//     *  <li>Classes have colliding annotations (i.e., two classes with the same type name)
//     *  <li>Specified external bindings are incorrect
//     *  <li>The JAXB implementation was unable to locate
//     *      provider-specific out-of-band information (such as additional
//     *      files generated at the development time.)
//     * </ol>
//     *
//     * @throws IllegalArgumentException
//     *      if the parameter contains {@code null} (i.e., {@code newInstance(null);})
//     *
//     * @since JAXB2.0
//     */
//    public static JAXBContext newInstance( Source[] externalBindings, Class... classesToBeBound )
//        throws JAXBException {
//
//        // empty class list is not an error, because the context will still include
//        // spec-specified classes like String and Integer.
//        // if(classesToBeBound.length==0)
//        //    throw new IllegalArgumentException();
//
//        // but it is an error to have nulls in it.
//        for( int i=classesToBeBound.length-1; i>=0; i-- )
//            if(classesToBeBound[i]==null)
//                throw new IllegalArgumentException();
//
//        return ContextFinder.find(externalBindings,classesToBeBound);
//    }

    private static ClassLoader getContextClassLoader(){
        if(System.getSecurityManager()==null){
            return Thread.currentThread().getContextClassLoader();
        }else{
            return (ClassLoader)java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction(){
                        public Object run(){
                            return Thread.currentThread().getContextClassLoader();
                        }
                    });
        }
    }

    public static JAXBContext newInstance(Class... classesToBeBound)
            throws JAXBException{
        return newInstance(classesToBeBound,Collections.<String,Object>emptyMap());
    }

    public static JAXBContext newInstance(Class[] classesToBeBound,Map<String,?> properties)
            throws JAXBException{
        if(classesToBeBound==null){
            throw new IllegalArgumentException();
        }
        // but it is an error to have nulls in it.
        for(int i=classesToBeBound.length-1;i>=0;i--){
            if(classesToBeBound[i]==null){
                throw new IllegalArgumentException();
            }
        }
        return ContextFinder.find(classesToBeBound,properties);
    }

    public abstract Unmarshaller createUnmarshaller() throws JAXBException;

    public abstract Marshaller createMarshaller() throws JAXBException;

    public abstract Validator createValidator() throws JAXBException;

    public Binder<Node> createBinder(){
        return createBinder(Node.class);
    }

    public <T> Binder<T> createBinder(Class<T> domType){
        // to make JAXB 1.0 implementations work, this method must not be
        // abstract
        throw new UnsupportedOperationException();
    }

    public JAXBIntrospector createJAXBIntrospector(){
        // to make JAXB 1.0 implementations work, this method must not be
        // abstract
        throw new UnsupportedOperationException();
    }

    public void generateSchema(SchemaOutputResolver outputResolver) throws IOException{
        // to make JAXB 1.0 implementations work, this method must not be
        // abstract
        throw new UnsupportedOperationException();
    }
}
