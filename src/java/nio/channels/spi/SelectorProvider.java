/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels.spi;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public abstract class SelectorProvider{
    private static final Object lock=new Object();
    private static SelectorProvider provider=null;

    protected SelectorProvider(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null)
            sm.checkPermission(new RuntimePermission("selectorProvider"));
    }

    public static SelectorProvider provider(){
        synchronized(lock){
            if(provider!=null)
                return provider;
            return AccessController.doPrivileged(
                    new PrivilegedAction<SelectorProvider>(){
                        public SelectorProvider run(){
                            if(loadProviderFromProperty())
                                return provider;
                            if(loadProviderAsService())
                                return provider;
                            provider=sun.nio.ch.DefaultSelectorProvider.create();
                            return provider;
                        }
                    });
        }
    }

    private static boolean loadProviderFromProperty(){
        String cn=System.getProperty("java.nio.channels.spi.SelectorProvider");
        if(cn==null)
            return false;
        try{
            Class<?> c=Class.forName(cn,true,
                    ClassLoader.getSystemClassLoader());
            provider=(SelectorProvider)c.newInstance();
            return true;
        }catch(ClassNotFoundException x){
            throw new ServiceConfigurationError(null,x);
        }catch(IllegalAccessException x){
            throw new ServiceConfigurationError(null,x);
        }catch(InstantiationException x){
            throw new ServiceConfigurationError(null,x);
        }catch(SecurityException x){
            throw new ServiceConfigurationError(null,x);
        }
    }

    private static boolean loadProviderAsService(){
        ServiceLoader<SelectorProvider> sl=
                ServiceLoader.load(SelectorProvider.class,
                        ClassLoader.getSystemClassLoader());
        Iterator<SelectorProvider> i=sl.iterator();
        for(;;){
            try{
                if(!i.hasNext())
                    return false;
                provider=i.next();
                return true;
            }catch(ServiceConfigurationError sce){
                if(sce.getCause() instanceof SecurityException){
                    // Ignore the security exception, try the next provider
                    continue;
                }
                throw sce;
            }
        }
    }

    public abstract DatagramChannel openDatagramChannel()
            throws IOException;

    public abstract DatagramChannel openDatagramChannel(ProtocolFamily family)
            throws IOException;

    public abstract Pipe openPipe()
            throws IOException;

    public abstract AbstractSelector openSelector()
            throws IOException;

    public abstract ServerSocketChannel openServerSocketChannel()
            throws IOException;

    public abstract SocketChannel openSocketChannel()
            throws IOException;

    public Channel inheritedChannel() throws IOException{
        return null;
    }
}
