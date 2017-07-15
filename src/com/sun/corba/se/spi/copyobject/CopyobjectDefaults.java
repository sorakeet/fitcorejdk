/**
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.corba.se.spi.copyobject;

import com.sun.corba.se.impl.copyobject.FallbackObjectCopierImpl;
import com.sun.corba.se.impl.copyobject.JavaStreamObjectCopierImpl;
import com.sun.corba.se.impl.copyobject.ORBStreamObjectCopierImpl;
import com.sun.corba.se.impl.copyobject.ReferenceObjectCopierImpl;
import com.sun.corba.se.spi.orb.ORB;

public abstract class CopyobjectDefaults{
    private static final ObjectCopier referenceObjectCopier=new ReferenceObjectCopierImpl();
    private static ObjectCopierFactory referenceObjectCopierFactory=
            new ObjectCopierFactory(){
                public ObjectCopier make(){
                    return referenceObjectCopier;
                }
            };

    private CopyobjectDefaults(){
    }

    public static ObjectCopierFactory makeORBStreamObjectCopierFactory(final ORB orb){
        return new ObjectCopierFactory(){
            public ObjectCopier make(){
                return new ORBStreamObjectCopierImpl(orb);
            }
        };
    }

    public static ObjectCopierFactory makeJavaStreamObjectCopierFactory(final ORB orb){
        return new ObjectCopierFactory(){
            public ObjectCopier make(){
                return new JavaStreamObjectCopierImpl(orb);
            }
        };
    }

    public static ObjectCopierFactory getReferenceObjectCopierFactory(){
        return referenceObjectCopierFactory;
    }

    public static ObjectCopierFactory makeFallbackObjectCopierFactory(
            final ObjectCopierFactory f1,final ObjectCopierFactory f2){
        return new ObjectCopierFactory(){
            public ObjectCopier make(){
                ObjectCopier c1=f1.make();
                ObjectCopier c2=f2.make();
                return new FallbackObjectCopierImpl(c1,c2);
            }
        };
    }
}
