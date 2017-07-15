/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.prefs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
// These imports needed only as a workaround for a JavaDoc bug

public abstract class Preferences{
    public static final int MAX_KEY_LENGTH=80;
    public static final int MAX_VALUE_LENGTH=8*1024;
    public static final int MAX_NAME_LENGTH=80;
    private static final PreferencesFactory factory=factory();
    private static Permission prefsPerm=new RuntimePermission("preferences");

    protected Preferences(){
    }

    private static PreferencesFactory factory(){
        // 1. Try user-specified system property
        String factoryName=AccessController.doPrivileged(
                new PrivilegedAction<String>(){
                    public String run(){
                        return System.getProperty(
                                "java.util.prefs.PreferencesFactory");
                    }
                });
        if(factoryName!=null){
            // FIXME: This code should be run in a doPrivileged and
            // not use the context classloader, to avoid being
            // dependent on the invoking thread.
            // Checking AllPermission also seems wrong.
            try{
                return (PreferencesFactory)
                        Class.forName(factoryName,false,
                                ClassLoader.getSystemClassLoader())
                                .newInstance();
            }catch(Exception ex){
                try{
                    // workaround for javaws, plugin,
                    // load factory class using non-system classloader
                    SecurityManager sm=System.getSecurityManager();
                    if(sm!=null){
                        sm.checkPermission(new java.security.AllPermission());
                    }
                    return (PreferencesFactory)
                            Class.forName(factoryName,false,
                                    Thread.currentThread()
                                            .getContextClassLoader())
                                    .newInstance();
                }catch(Exception e){
                    throw new InternalError(
                            "Can't instantiate Preferences factory "
                                    +factoryName,e);
                }
            }
        }
        return AccessController.doPrivileged(
                new PrivilegedAction<PreferencesFactory>(){
                    public PreferencesFactory run(){
                        return factory1();
                    }
                });
    }

    private static PreferencesFactory factory1(){
        // 2. Try service provider interface
        Iterator<PreferencesFactory> itr=ServiceLoader
                .load(PreferencesFactory.class,ClassLoader.getSystemClassLoader())
                .iterator();
        // choose first provider instance
        while(itr.hasNext()){
            try{
                return itr.next();
            }catch(ServiceConfigurationError sce){
                if(sce.getCause() instanceof SecurityException){
                    // Ignore the security exception, try the next provider
                    continue;
                }
                throw sce;
            }
        }
        // 3. Use platform-specific system-wide default
        String osName=System.getProperty("os.name");
        String platformFactory;
        if(osName.startsWith("Windows")){
            platformFactory="java.util.prefs.WindowsPreferencesFactory";
        }else if(osName.contains("OS X")){
            platformFactory="java.util.prefs.MacOSXPreferencesFactory";
        }else{
            platformFactory="java.util.prefs.FileSystemPreferencesFactory";
        }
        try{
            return (PreferencesFactory)
                    Class.forName(platformFactory,false,
                            Preferences.class.getClassLoader()).newInstance();
        }catch(Exception e){
            throw new InternalError(
                    "Can't instantiate platform default Preferences factory "
                            +platformFactory,e);
        }
    }

    public static Preferences userNodeForPackage(Class<?> c){
        return userRoot().node(nodeName(c));
    }

    private static String nodeName(Class<?> c){
        if(c.isArray())
            throw new IllegalArgumentException(
                    "Arrays have no associated preferences node.");
        String className=c.getName();
        int pkgEndIndex=className.lastIndexOf('.');
        if(pkgEndIndex<0)
            return "/<unnamed>";
        String packageName=className.substring(0,pkgEndIndex);
        return "/"+packageName.replace('.','/');
    }

    public static Preferences userRoot(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null)
            security.checkPermission(prefsPerm);
        return factory.userRoot();
    }

    public static Preferences systemNodeForPackage(Class<?> c){
        return systemRoot().node(nodeName(c));
    }

    public static Preferences systemRoot(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null)
            security.checkPermission(prefsPerm);
        return factory.systemRoot();
    }

    public static void importPreferences(InputStream is)
            throws IOException, InvalidPreferencesFormatException{
        XmlSupport.importPreferences(is);
    }

    public abstract void put(String key,String value);

    public abstract String get(String key,String def);

    public abstract void remove(String key);

    public abstract void clear() throws BackingStoreException;

    public abstract void putInt(String key,int value);

    public abstract int getInt(String key,int def);

    public abstract void putLong(String key,long value);

    public abstract long getLong(String key,long def);

    public abstract void putBoolean(String key,boolean value);

    public abstract boolean getBoolean(String key,boolean def);

    public abstract void putFloat(String key,float value);

    public abstract float getFloat(String key,float def);

    public abstract void putDouble(String key,double value);

    public abstract double getDouble(String key,double def);

    public abstract void putByteArray(String key,byte[] value);

    public abstract byte[] getByteArray(String key,byte[] def);

    public abstract String[] keys() throws BackingStoreException;

    public abstract String[] childrenNames() throws BackingStoreException;

    public abstract Preferences parent();

    public abstract Preferences node(String pathName);

    public abstract boolean nodeExists(String pathName)
            throws BackingStoreException;

    public abstract void removeNode() throws BackingStoreException;

    public abstract String name();

    public abstract String absolutePath();

    public abstract boolean isUserNode();

    public abstract String toString();

    public abstract void flush() throws BackingStoreException;

    public abstract void sync() throws BackingStoreException;

    public abstract void addPreferenceChangeListener(
            PreferenceChangeListener pcl);

    public abstract void removePreferenceChangeListener(
            PreferenceChangeListener pcl);

    public abstract void addNodeChangeListener(NodeChangeListener ncl);

    public abstract void removeNodeChangeListener(NodeChangeListener ncl);

    public abstract void exportNode(OutputStream os)
            throws IOException, BackingStoreException;

    public abstract void exportSubtree(OutputStream os)
            throws IOException, BackingStoreException;
}
