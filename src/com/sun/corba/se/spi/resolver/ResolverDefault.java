/**
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.resolver;

import com.sun.corba.se.impl.resolver.*;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.Operation;
import com.sun.corba.se.spi.orb.StringPair;

import java.io.File;

public class ResolverDefault{
    public static LocalResolver makeLocalResolver(){
        return new LocalResolverImpl();
    }

    public static Resolver makeORBInitRefResolver(Operation urlOperation,
                                                  StringPair[] initRefs){
        return new ORBInitRefResolverImpl(urlOperation,initRefs);
    }

    public static Resolver makeORBDefaultInitRefResolver(Operation urlOperation,
                                                         String defaultInitRef){
        return new ORBDefaultInitRefResolverImpl(urlOperation,
                defaultInitRef);
    }

    public static Resolver makeBootstrapResolver(ORB orb,String host,int port){
        return new BootstrapResolverImpl(orb,host,port);
    }

    public static Resolver makeCompositeResolver(Resolver first,Resolver second){
        return new CompositeResolverImpl(first,second);
    }

    public static Operation makeINSURLOperation(ORB orb,Resolver bootstrapResolver){
        return new INSURLOperationImpl(
                (ORB)orb,bootstrapResolver);
    }

    public static LocalResolver makeSplitLocalResolver(Resolver resolver,
                                                       LocalResolver localResolver){
        return new SplitLocalResolverImpl(resolver,localResolver);
    }

    public static Resolver makeFileResolver(ORB orb,File file){
        return new FileResolverImpl(orb,file);
    }
}
