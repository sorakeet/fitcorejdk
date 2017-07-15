/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.presentation.rmi;

import com.sun.corba.se.spi.orbutil.proxy.InvocationHandlerFactory;

import javax.rmi.CORBA.Tie;
import java.lang.reflect.Method;
import java.util.Map;

public interface PresentationManager{
    ClassData getClassData(Class cls);

    DynamicMethodMarshaller getDynamicMethodMarshaller(Method method);

    StubFactoryFactory getStubFactoryFactory(boolean isDynamic);

    void setStubFactoryFactory(boolean isDynamic,StubFactoryFactory sff);

    Tie getTie();

    boolean useDynamicStubs();

    public interface StubFactoryFactory{
        String getStubName(String className);

        StubFactory createStubFactory(String className,
                                      boolean isIDLStub,String remoteCodeBase,Class expectedClass,
                                      ClassLoader classLoader);

        Tie getTie(Class cls);

        boolean createsDynamicStubs();
    }

    public interface StubFactory{
        org.omg.CORBA.Object makeStub();

        String[] getTypeIds();
    }

    public interface ClassData{
        Class getMyClass();

        IDLNameTranslator getIDLNameTranslator();

        String[] getTypeIds();

        InvocationHandlerFactory getInvocationHandlerFactory();

        Map getDictionary();
    }
}
