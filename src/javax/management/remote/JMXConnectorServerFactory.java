/**
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.remote;

import com.sun.jmx.remote.util.ClassLogger;
import com.sun.jmx.remote.util.EnvHelp;

import javax.management.MBeanServer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JMXConnectorServerFactory{
    public static final String DEFAULT_CLASS_LOADER=
            JMXConnectorFactory.DEFAULT_CLASS_LOADER;
    public static final String DEFAULT_CLASS_LOADER_NAME=
            "jmx.remote.default.class.loader.name";
    public static final String PROTOCOL_PROVIDER_PACKAGES=
            "jmx.remote.protocol.provider.pkgs";
    public static final String PROTOCOL_PROVIDER_CLASS_LOADER=
            "jmx.remote.protocol.provider.class.loader";
    private static final String PROTOCOL_PROVIDER_DEFAULT_PACKAGE=
            "com.sun.jmx.remote.protocol";
    private static final ClassLogger logger=
            new ClassLogger("javax.management.remote.misc","JMXConnectorServerFactory");

    private JMXConnectorServerFactory(){
    }

    public static JMXConnectorServer
    newJMXConnectorServer(JMXServiceURL serviceURL,
                          Map<String,?> environment,
                          MBeanServer mbeanServer)
            throws IOException{
        Map<String,Object> envcopy;
        if(environment==null)
            envcopy=new HashMap<String,Object>();
        else{
            EnvHelp.checkAttributes(environment);
            envcopy=new HashMap<String,Object>(environment);
        }
        final Class<JMXConnectorServerProvider> targetInterface=
                JMXConnectorServerProvider.class;
        final ClassLoader loader=
                JMXConnectorFactory.resolveClassLoader(envcopy);
        final String protocol=serviceURL.getProtocol();
        final String providerClassName="ServerProvider";
        JMXConnectorServerProvider provider=
                JMXConnectorFactory.getProvider(serviceURL,
                        envcopy,
                        providerClassName,
                        targetInterface,
                        loader);
        IOException exception=null;
        if(provider==null){
            // Loader is null when context class loader is set to null
            // and no loader has been provided in map.
            // com.sun.jmx.remote.util.Service class extracted from j2se
            // provider search algorithm doesn't handle well null classloader.
            if(loader!=null){
                try{
                    JMXConnectorServer connection=
                            getConnectorServerAsService(loader,
                                    serviceURL,
                                    envcopy,
                                    mbeanServer);
                    if(connection!=null)
                        return connection;
                }catch(JMXProviderException e){
                    throw e;
                }catch(IOException e){
                    exception=e;
                }
            }
            provider=
                    JMXConnectorFactory.getProvider(
                            protocol,
                            PROTOCOL_PROVIDER_DEFAULT_PACKAGE,
                            JMXConnectorFactory.class.getClassLoader(),
                            providerClassName,
                            targetInterface);
        }
        if(provider==null){
            MalformedURLException e=
                    new MalformedURLException("Unsupported protocol: "+protocol);
            if(exception==null){
                throw e;
            }else{
                throw EnvHelp.initCause(e,exception);
            }
        }
        envcopy=Collections.unmodifiableMap(envcopy);
        return provider.newJMXConnectorServer(serviceURL,
                envcopy,
                mbeanServer);
    }

    private static JMXConnectorServer
    getConnectorServerAsService(ClassLoader loader,
                                JMXServiceURL url,
                                Map<String,?> map,
                                MBeanServer mbs)
            throws IOException{
        Iterator<JMXConnectorServerProvider> providers=
                JMXConnectorFactory.
                        getProviderIterator(JMXConnectorServerProvider.class,loader);
        IOException exception=null;
        while(providers.hasNext()){
            try{
                return providers.next().newJMXConnectorServer(url,map,mbs);
            }catch(JMXProviderException e){
                throw e;
            }catch(Exception e){
                if(logger.traceOn())
                    logger.trace("getConnectorAsService",
                            "URL["+url+
                                    "] Service provider exception: "+e);
                if(!(e instanceof MalformedURLException)){
                    if(exception==null){
                        if(e instanceof IOException){
                            exception=(IOException)e;
                        }else{
                            exception=EnvHelp.initCause(
                                    new IOException(e.getMessage()),e);
                        }
                    }
                }
                continue;
            }
        }
        if(exception==null)
            return null;
        else
            throw exception;
    }
}
