/**
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.loading;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.util.List;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MBEANSERVER_LOGGER;

@Deprecated
public class DefaultLoaderRepository{
    public static Class<?> loadClass(String className)
            throws ClassNotFoundException{
        MBEANSERVER_LOGGER.logp(Level.FINEST,
                DefaultLoaderRepository.class.getName(),
                "loadClass",className);
        return load(null,className);
    }

    private static Class<?> load(ClassLoader without,String className)
            throws ClassNotFoundException{
        final List<MBeanServer> mbsList=MBeanServerFactory.findMBeanServer(null);
        for(MBeanServer mbs : mbsList){
            ClassLoaderRepository clr=mbs.getClassLoaderRepository();
            try{
                return clr.loadClassWithout(without,className);
            }catch(ClassNotFoundException e){
                // OK : Try with next one...
            }
        }
        throw new ClassNotFoundException(className);
    }

    public static Class<?> loadClassWithout(ClassLoader loader,
                                            String className)
            throws ClassNotFoundException{
        MBEANSERVER_LOGGER.logp(Level.FINEST,
                DefaultLoaderRepository.class.getName(),
                "loadClassWithout",className);
        return load(loader,className);
    }
}
