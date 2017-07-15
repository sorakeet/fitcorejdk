/**
 * Copyright (c) 1999, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.naming.internal;

import javax.naming.NamingException;
import java.util.List;

// no need to implement Enumeration since this is only for internal use
public final class FactoryEnumeration{
    // List<NamedWeakReference<Class | Object>>
    private List<NamedWeakReference<Object>> factories;
    private int posn=0;
    private ClassLoader loader;

    FactoryEnumeration(List<NamedWeakReference<Object>> factories,
                       ClassLoader loader){
        this.factories=factories;
        this.loader=loader;
    }

    public Object next() throws NamingException{
        synchronized(factories){
            NamedWeakReference<Object> ref=factories.get(posn++);
            Object answer=ref.get();
            if((answer!=null)&&!(answer instanceof Class)){
                return answer;
            }
            String className=ref.getName();
            try{
                if(answer==null){   // reload class if weak ref cleared
                    Class<?> cls=Class.forName(className,true,loader);
                    answer=cls;
                }
                // Instantiate Class to get factory
                answer=((Class)answer).newInstance();
                ref=new NamedWeakReference<>(answer,className);
                factories.set(posn-1,ref);  // replace Class object or null
                return answer;
            }catch(ClassNotFoundException e){
                NamingException ne=
                        new NamingException("No longer able to load "+className);
                ne.setRootCause(e);
                throw ne;
            }catch(InstantiationException e){
                NamingException ne=
                        new NamingException("Cannot instantiate "+answer);
                ne.setRootCause(e);
                throw ne;
            }catch(IllegalAccessException e){
                NamingException ne=new NamingException("Cannot access "+answer);
                ne.setRootCause(e);
                throw ne;
            }
        }
    }

    public boolean hasMore(){
        synchronized(factories){
            return posn<factories.size();
        }
    }
}
