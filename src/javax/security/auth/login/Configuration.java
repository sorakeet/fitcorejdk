/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

import sun.security.jca.GetInstance;

import javax.security.auth.AuthPermission;
import java.security.*;
import java.util.Objects;

public abstract class Configuration{
    private static Configuration configuration;
    private final java.security.AccessControlContext acc=
            AccessController.getContext();

    protected Configuration(){
    }

    public static Configuration getConfiguration(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null)
            sm.checkPermission(new AuthPermission("getLoginConfiguration"));
        synchronized(Configuration.class){
            if(configuration==null){
                String config_class=null;
                config_class=AccessController.doPrivileged
                        (new PrivilegedAction<String>(){
                            public String run(){
                                return Security.getProperty
                                        ("login.configuration.provider");
                            }
                        });
                if(config_class==null){
                    config_class="sun.security.provider.ConfigFile";
                }
                try{
                    final String finalClass=config_class;
                    Configuration untrustedImpl=AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Configuration>(){
                                public Configuration run() throws ClassNotFoundException,
                                        InstantiationException,
                                        IllegalAccessException{
                                    Class<? extends Configuration> implClass=Class.forName(
                                            finalClass,false,
                                            Thread.currentThread().getContextClassLoader()
                                    ).asSubclass(Configuration.class);
                                    return implClass.newInstance();
                                }
                            });
                    AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Void>(){
                                public Void run(){
                                    setConfiguration(untrustedImpl);
                                    return null;
                                }
                            },Objects.requireNonNull(untrustedImpl.acc)
                    );
                }catch(PrivilegedActionException e){
                    Exception ee=e.getException();
                    if(ee instanceof InstantiationException){
                        throw (SecurityException)new
                                SecurityException
                                ("Configuration error:"+
                                        ee.getCause().getMessage()+
                                        "\n").initCause(ee.getCause());
                    }else{
                        throw (SecurityException)new
                                SecurityException
                                ("Configuration error: "+
                                        ee.toString()+
                                        "\n").initCause(ee);
                    }
                }
            }
            return configuration;
        }
    }

    public static void setConfiguration(Configuration configuration){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null)
            sm.checkPermission(new AuthPermission("setLoginConfiguration"));
        Configuration.configuration=configuration;
    }

    public static Configuration getInstance(String type,
                                            Parameters params)
            throws NoSuchAlgorithmException{
        checkPermission(type);
        try{
            GetInstance.Instance instance=GetInstance.getInstance
                    ("Configuration",
                            ConfigurationSpi.class,
                            type,
                            params);
            return new ConfigDelegate((ConfigurationSpi)instance.impl,
                    instance.provider,
                    type,
                    params);
        }catch(NoSuchAlgorithmException nsae){
            return handleException(nsae);
        }
    }

    private static void checkPermission(String type){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new AuthPermission
                    ("createLoginConfiguration."+type));
        }
    }

    private static Configuration handleException(NoSuchAlgorithmException nsae)
            throws NoSuchAlgorithmException{
        Throwable cause=nsae.getCause();
        if(cause instanceof IllegalArgumentException){
            throw (IllegalArgumentException)cause;
        }
        throw nsae;
    }

    public static Configuration getInstance(String type,
                                            Parameters params,
                                            String provider)
            throws NoSuchProviderException, NoSuchAlgorithmException{
        if(provider==null||provider.length()==0){
            throw new IllegalArgumentException("missing provider");
        }
        checkPermission(type);
        try{
            GetInstance.Instance instance=GetInstance.getInstance
                    ("Configuration",
                            ConfigurationSpi.class,
                            type,
                            params,
                            provider);
            return new ConfigDelegate((ConfigurationSpi)instance.impl,
                    instance.provider,
                    type,
                    params);
        }catch(NoSuchAlgorithmException nsae){
            return handleException(nsae);
        }
    }

    public static Configuration getInstance(String type,
                                            Parameters params,
                                            Provider provider)
            throws NoSuchAlgorithmException{
        if(provider==null){
            throw new IllegalArgumentException("missing provider");
        }
        checkPermission(type);
        try{
            GetInstance.Instance instance=GetInstance.getInstance
                    ("Configuration",
                            ConfigurationSpi.class,
                            type,
                            params,
                            provider);
            return new ConfigDelegate((ConfigurationSpi)instance.impl,
                    instance.provider,
                    type,
                    params);
        }catch(NoSuchAlgorithmException nsae){
            return handleException(nsae);
        }
    }

    public Provider getProvider(){
        return null;
    }

    public String getType(){
        return null;
    }

    public Parameters getParameters(){
        return null;
    }

    public abstract AppConfigurationEntry[] getAppConfigurationEntry
            (String name);

    public void refresh(){
    }

    public static interface Parameters{
    }

    private static class ConfigDelegate extends Configuration{
        private ConfigurationSpi spi;
        private Provider p;
        private String type;
        private Parameters params;

        private ConfigDelegate(ConfigurationSpi spi,Provider p,
                               String type,Parameters params){
            this.spi=spi;
            this.p=p;
            this.type=type;
            this.params=params;
        }

        public Provider getProvider(){
            return p;
        }

        public String getType(){
            return type;
        }

        public Parameters getParameters(){
            return params;
        }

        public AppConfigurationEntry[] getAppConfigurationEntry(String name){
            return spi.engineGetAppConfigurationEntry(name);
        }

        public void refresh(){
            spi.engineRefresh();
        }
    }
}
