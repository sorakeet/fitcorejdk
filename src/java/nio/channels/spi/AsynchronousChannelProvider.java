/**
 * Copyright (c) 2007, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public abstract class AsynchronousChannelProvider{
    protected AsynchronousChannelProvider(){
        this(checkPermission());
    }

    private static Void checkPermission(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null)
            sm.checkPermission(new RuntimePermission("asynchronousChannelProvider"));
        return null;
    }

    private AsynchronousChannelProvider(Void ignore){
    }

    public static AsynchronousChannelProvider provider(){
        return ProviderHolder.provider;
    }

    public abstract AsynchronousChannelGroup
    openAsynchronousChannelGroup(int nThreads,ThreadFactory threadFactory) throws IOException;

    public abstract AsynchronousChannelGroup
    openAsynchronousChannelGroup(ExecutorService executor,int initialSize) throws IOException;

    public abstract AsynchronousServerSocketChannel openAsynchronousServerSocketChannel
            (AsynchronousChannelGroup group) throws IOException;

    public abstract AsynchronousSocketChannel openAsynchronousSocketChannel
            (AsynchronousChannelGroup group) throws IOException;

    // lazy initialization of default provider
    private static class ProviderHolder{
        static final AsynchronousChannelProvider provider=load();

        private static AsynchronousChannelProvider load(){
            return AccessController
                    .doPrivileged(new PrivilegedAction<AsynchronousChannelProvider>(){
                        public AsynchronousChannelProvider run(){
                            AsynchronousChannelProvider p;
                            p=loadProviderFromProperty();
                            if(p!=null)
                                return p;
                            p=loadProviderAsService();
                            if(p!=null)
                                return p;
                            return sun.nio.ch.DefaultAsynchronousChannelProvider.create();
                        }
                    });
        }

        private static AsynchronousChannelProvider loadProviderFromProperty(){
            String cn=System.getProperty("java.nio.channels.spi.AsynchronousChannelProvider");
            if(cn==null)
                return null;
            try{
                Class<?> c=Class.forName(cn,true,
                        ClassLoader.getSystemClassLoader());
                return (AsynchronousChannelProvider)c.newInstance();
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

        private static AsynchronousChannelProvider loadProviderAsService(){
            ServiceLoader<AsynchronousChannelProvider> sl=
                    ServiceLoader.load(AsynchronousChannelProvider.class,
                            ClassLoader.getSystemClassLoader());
            Iterator<AsynchronousChannelProvider> i=sl.iterator();
            for(;;){
                try{
                    return (i.hasNext())?i.next():null;
                }catch(ServiceConfigurationError sce){
                    if(sce.getCause() instanceof SecurityException){
                        // Ignore the security exception, try the next provider
                        continue;
                    }
                    throw sce;
                }
            }
        }
    }
}
