/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.imageio.spi;

import com.sun.imageio.plugins.bmp.BMPImageReaderSpi;
import com.sun.imageio.plugins.bmp.BMPImageWriterSpi;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
import com.sun.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.sun.imageio.plugins.png.PNGImageReaderSpi;
import com.sun.imageio.plugins.png.PNGImageWriterSpi;
import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;
import com.sun.imageio.plugins.wbmp.WBMPImageWriterSpi;
import com.sun.imageio.spi.*;
import sun.awt.AppContext;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Vector;

public final class IIORegistry extends ServiceRegistry{
    private static final Vector initialCategories=new Vector(5);

    static{
        initialCategories.add(ImageReaderSpi.class);
        initialCategories.add(ImageWriterSpi.class);
        initialCategories.add(ImageTranscoderSpi.class);
        initialCategories.add(ImageInputStreamSpi.class);
        initialCategories.add(ImageOutputStreamSpi.class);
    }

    private IIORegistry(){
        super(initialCategories.iterator());
        registerStandardSpis();
        registerApplicationClasspathSpis();
    }

    private void registerStandardSpis(){
        // Hardwire standard SPIs
        registerServiceProvider(new GIFImageReaderSpi());
        registerServiceProvider(new GIFImageWriterSpi());
        registerServiceProvider(new BMPImageReaderSpi());
        registerServiceProvider(new BMPImageWriterSpi());
        registerServiceProvider(new WBMPImageReaderSpi());
        registerServiceProvider(new WBMPImageWriterSpi());
        registerServiceProvider(new PNGImageReaderSpi());
        registerServiceProvider(new PNGImageWriterSpi());
        registerServiceProvider(new JPEGImageReaderSpi());
        registerServiceProvider(new JPEGImageWriterSpi());
        registerServiceProvider(new FileImageInputStreamSpi());
        registerServiceProvider(new FileImageOutputStreamSpi());
        registerServiceProvider(new InputStreamImageInputStreamSpi());
        registerServiceProvider(new OutputStreamImageOutputStreamSpi());
        registerServiceProvider(new RAFImageInputStreamSpi());
        registerServiceProvider(new RAFImageOutputStreamSpi());
        registerInstalledProviders();
    }

    private void registerInstalledProviders(){
        /**
         We need to load installed providers from the
         system classpath (typically the <code>lib/ext</code>
         directory in in the Java installation directory)
         in the privileged mode in order to
         be able read corresponding jar files even if
         file read capability is restricted (like the
         applet context case).
         */
        PrivilegedAction doRegistration=
                new PrivilegedAction(){
                    public Object run(){
                        Iterator categories=getCategories();
                        while(categories.hasNext()){
                            Class<IIOServiceProvider> c=(Class)categories.next();
                            for(IIOServiceProvider p : ServiceLoader.loadInstalled(c)){
                                registerServiceProvider(p);
                            }
                        }
                        return this;
                    }
                };
        AccessController.doPrivileged(doRegistration);
    }

    public void registerApplicationClasspathSpis(){
        // FIX: load only from application classpath
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
        Iterator categories=getCategories();
        while(categories.hasNext()){
            Class<IIOServiceProvider> c=(Class)categories.next();
            Iterator<IIOServiceProvider> riter=
                    ServiceLoader.load(c,loader).iterator();
            while(riter.hasNext()){
                try{
                    // Note that the next() call is required to be inside
                    // the try/catch block; see 6342404.
                    IIOServiceProvider r=riter.next();
                    registerServiceProvider(r);
                }catch(ServiceConfigurationError err){
                    if(System.getSecurityManager()!=null){
                        // In the applet case, we will catch the  error so
                        // registration of other plugins can  proceed
                        err.printStackTrace();
                    }else{
                        // In the application case, we will  throw the
                        // error to indicate app/system  misconfiguration
                        throw err;
                    }
                }
            }
        }
    }

    public static IIORegistry getDefaultInstance(){
        AppContext context=AppContext.getAppContext();
        IIORegistry registry=
                (IIORegistry)context.get(IIORegistry.class);
        if(registry==null){
            // Create an instance for this AppContext
            registry=new IIORegistry();
            context.put(IIORegistry.class,registry);
        }
        return registry;
    }
}
