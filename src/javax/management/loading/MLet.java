/**
 * Copyright (c) 1999, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.management.loading;
// Java import

import com.sun.jmx.defaults.ServiceName;
import com.sun.jmx.remote.util.EnvHelp;

import javax.management.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.logging.Level;

import static com.sun.jmx.defaults.JmxProperties.MLET_LIB_DIR;
import static com.sun.jmx.defaults.JmxProperties.MLET_LOGGER;

public class MLet extends java.net.URLClassLoader
        implements MLetMBean, MBeanRegistration, Externalizable{
    private static final long serialVersionUID=3636148327800330130L;
    private MBeanServer server=null;
    private List<MLetContent> mletList=new ArrayList<MLetContent>();
    private String libraryDirectory;
    private ObjectName mletObjectName=null;
    private URL[] myUrls=null;
    private transient ClassLoaderRepository currentClr;
    private transient boolean delegateToCLR;
    private Map<String,Class<?>> primitiveClasses=
            new HashMap<String,Class<?>>(8);

    {
        primitiveClasses.put(Boolean.TYPE.toString(),Boolean.class);
        primitiveClasses.put(Character.TYPE.toString(),Character.class);
        primitiveClasses.put(Byte.TYPE.toString(),Byte.class);
        primitiveClasses.put(Short.TYPE.toString(),Short.class);
        primitiveClasses.put(Integer.TYPE.toString(),Integer.class);
        primitiveClasses.put(Long.TYPE.toString(),Long.class);
        primitiveClasses.put(Float.TYPE.toString(),Float.class);
        primitiveClasses.put(Double.TYPE.toString(),Double.class);
    }

    /**
     * ------------------------------------------
     *  CONSTRUCTORS
     * ------------------------------------------
     */
    public MLet(){
        this(new URL[0]);
    }

    public MLet(URL[] urls){
        this(urls,true);
    }

    public MLet(URL[] urls,boolean delegateToCLR){
        super(urls);
        init(delegateToCLR);
    }

    private void init(boolean delegateToCLR){
        this.delegateToCLR=delegateToCLR;
        try{
            libraryDirectory=System.getProperty(MLET_LIB_DIR);
            if(libraryDirectory==null)
                libraryDirectory=getTmpDir();
        }catch(SecurityException e){
            // OK : We don't do AccessController.doPrivileged, but we don't
            //      stop the user from creating an MLet just because they
            //      can't read the MLET_LIB_DIR or java.io.tmpdir properties
            //      either.
        }
    }

    private String getTmpDir(){
        // JDK 1.4
        String tmpDir=System.getProperty("java.io.tmpdir");
        if(tmpDir!=null) return tmpDir;
        // JDK < 1.4
        File tmpFile=null;
        try{
            // Try to guess the system temporary dir...
            tmpFile=File.createTempFile("tmp","jmx");
            if(tmpFile==null) return null;
            final File tmpDirFile=tmpFile.getParentFile();
            if(tmpDirFile==null) return null;
            return tmpDirFile.getAbsolutePath();
        }catch(Exception x){
            MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                    "getTmpDir","Failed to determine system temporary dir");
            return null;
        }finally{
            // Cleanup ...
            if(tmpFile!=null){
                try{
                    boolean deleted=tmpFile.delete();
                    if(!deleted){
                        MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                                "getTmpDir","Failed to delete temp file");
                    }
                }catch(Exception x){
                    MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                            "getTmpDir","Failed to delete temporary file",x);
                }
            }
        }
    }

    public MLet(URL[] urls,ClassLoader parent){
        this(urls,parent,true);
    }

    public MLet(URL[] urls,ClassLoader parent,boolean delegateToCLR){
        super(urls,parent);
        init(delegateToCLR);
    }

    public MLet(URL[] urls,
                ClassLoader parent,
                URLStreamHandlerFactory factory){
        this(urls,parent,factory,true);
    }

    public MLet(URL[] urls,
                ClassLoader parent,
                URLStreamHandlerFactory factory,
                boolean delegateToCLR){
        super(urls,parent,factory);
        init(delegateToCLR);
    }

    public void addURL(URL url){
        if(!Arrays.asList(getURLs()).contains(url))
            super.addURL(url);
    }    public void addURL(String url) throws ServiceNotFoundException{
        try{
            URL ur=new URL(url);
            if(!Arrays.asList(getURLs()).contains(ur))
                super.addURL(ur);
        }catch(MalformedURLException e){
            if(MLET_LOGGER.isLoggable(Level.FINEST)){
                MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                        "addUrl","Malformed URL: "+url,e);
            }
            throw new
                    ServiceNotFoundException("The specified URL is malformed");
        }
    }

    public URL[] getURLs(){
        return super.getURLs();
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException{
        /** currentClr is context sensitive - used to avoid recursion
         in the class loader repository.  (This is no longer
         necessary with the new CLR semantics but is kept for
         compatibility with code that might have called the
         two-parameter loadClass explicitly.)  */
        return findClass(name,currentClr);
    }    public Set<Object> getMBeansFromURL(URL url)
            throws ServiceNotFoundException{
        if(url==null){
            throw new ServiceNotFoundException("The specified URL is null");
        }
        return getMBeansFromURL(url.toString());
    }

    Class<?> findClass(String name,ClassLoaderRepository clr)
            throws ClassNotFoundException{
        Class<?> c=null;
        MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),"findClass",name);
        // Try looking in the JAR:
        try{
            c=super.findClass(name);
            if(MLET_LOGGER.isLoggable(Level.FINER)){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),
                        "findClass",
                        "Class "+name+" loaded through MLet classloader");
            }
        }catch(ClassNotFoundException e){
            // Drop through
            if(MLET_LOGGER.isLoggable(Level.FINEST)){
                MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                        "findClass",
                        "Class "+name+" not found locally");
            }
        }
        // if we are not called from the ClassLoaderRepository
        if(c==null&&delegateToCLR&&clr!=null){
            // Try the classloader repository:
            //
            try{
                if(MLET_LOGGER.isLoggable(Level.FINEST)){
                    MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                            "findClass",
                            "Class "+name+" : looking in CLR");
                }
                c=clr.loadClassBefore(this,name);
                // The loadClassBefore method never returns null.
                // If the class is not found we get an exception.
                if(MLET_LOGGER.isLoggable(Level.FINER)){
                    MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),
                            "findClass",
                            "Class "+name+" loaded through "+
                                    "the default classloader repository");
                }
            }catch(ClassNotFoundException e){
                // Drop through
                if(MLET_LOGGER.isLoggable(Level.FINEST)){
                    MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                            "findClass",
                            "Class "+name+" not found in CLR");
                }
            }
        }
        if(c==null){
            MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                    "findClass","Failed to load class "+name);
            throw new ClassNotFoundException(name);
        }
        return c;
    }    public Set<Object> getMBeansFromURL(String url)
            throws ServiceNotFoundException{
        String mth="getMBeansFromURL";
        if(server==null){
            throw new IllegalStateException("This MLet MBean is not "+
                    "registered with an MBeanServer.");
        }
        // Parse arguments
        if(url==null){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),
                    mth,"URL is null");
            throw new ServiceNotFoundException("The specified URL is null");
        }else{
            url=url.replace(File.separatorChar,'/');
        }
        if(MLET_LOGGER.isLoggable(Level.FINER)){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),
                    mth,"<URL = "+url+">");
        }
        // Parse URL
        try{
            MLetParser parser=new MLetParser();
            mletList=parser.parseURL(url);
        }catch(Exception e){
            final String msg=
                    "Problems while parsing URL ["+url+
                            "], got exception ["+e.toString()+"]";
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,msg);
            throw EnvHelp.initCause(new ServiceNotFoundException(msg),e);
        }
        // Check that the list of MLets is not empty
        if(mletList.size()==0){
            final String msg=
                    "File "+url+" not found or MLET tag not defined in file";
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,msg);
            throw new ServiceNotFoundException(msg);
        }
        // Walk through the list of MLets
        Set<Object> mbeans=new HashSet<Object>();
        for(MLetContent elmt : mletList){
            // Initialize local variables
            String code=elmt.getCode();
            if(code!=null){
                if(code.endsWith(".class")){
                    code=code.substring(0,code.length()-6);
                }
            }
            String name=elmt.getName();
            URL codebase=elmt.getCodeBase();
            String version=elmt.getVersion();
            String serName=elmt.getSerializedObject();
            String jarFiles=elmt.getJarFiles();
            URL documentBase=elmt.getDocumentBase();
            // Display debug information
            if(MLET_LOGGER.isLoggable(Level.FINER)){
                final StringBuilder strb=new StringBuilder()
                        .append("\n\tMLET TAG     = ").append(elmt.getAttributes())
                        .append("\n\tCODEBASE     = ").append(codebase)
                        .append("\n\tARCHIVE      = ").append(jarFiles)
                        .append("\n\tCODE         = ").append(code)
                        .append("\n\tOBJECT       = ").append(serName)
                        .append("\n\tNAME         = ").append(name)
                        .append("\n\tVERSION      = ").append(version)
                        .append("\n\tDOCUMENT URL = ").append(documentBase);
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),
                        mth,strb.toString());
            }
            // Load classes from JAR files
            StringTokenizer st=new StringTokenizer(jarFiles,",",false);
            while(st.hasMoreTokens()){
                String tok=st.nextToken().trim();
                if(MLET_LOGGER.isLoggable(Level.FINER)){
                    MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                            "Load archive for codebase <"+codebase+
                                    ">, file <"+tok+">");
                }
                // Check which is the codebase to be used for loading the jar file.
                // If we are using the base MLet implementation then it will be
                // always the remote server but if the service has been extended in
                // order to support caching and versioning then this method will
                // return the appropriate one.
                //
                try{
                    codebase=check(version,codebase,tok,elmt);
                }catch(Exception ex){
                    MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                            mth,"Got unexpected exception",ex);
                    mbeans.add(ex);
                    continue;
                }
                // Appends the specified JAR file URL to the list of
                // URLs to search for classes and resources.
                try{
                    if(!Arrays.asList(getURLs())
                            .contains(new URL(codebase.toString()+tok))){
                        addURL(codebase+tok);
                    }
                }catch(MalformedURLException me){
                    // OK : Ignore jar file if its name provokes the
                    // URL to be an invalid one.
                }
            }
            // Instantiate the class specified in the
            // CODE or OBJECT section of the MLet tag
            //
            Object o;
            ObjectInstance objInst;
            if(code!=null&&serName!=null){
                final String msg=
                        "CODE and OBJECT parameters cannot be specified at the "+
                                "same time in tag MLET";
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,msg);
                mbeans.add(new Error(msg));
                continue;
            }
            if(code==null&&serName==null){
                final String msg=
                        "Either CODE or OBJECT parameter must be specified in "+
                                "tag MLET";
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,msg);
                mbeans.add(new Error(msg));
                continue;
            }
            try{
                if(code!=null){
                    List<String> signat=elmt.getParameterTypes();
                    List<String> stringPars=elmt.getParameterValues();
                    List<Object> objectPars=new ArrayList<Object>();
                    for(int i=0;i<signat.size();i++){
                        objectPars.add(constructParameter(stringPars.get(i),
                                signat.get(i)));
                    }
                    if(signat.isEmpty()){
                        if(name==null){
                            objInst=server.createMBean(code,null,
                                    mletObjectName);
                        }else{
                            objInst=server.createMBean(code,
                                    new ObjectName(name),
                                    mletObjectName);
                        }
                    }else{
                        Object[] parms=objectPars.toArray();
                        String[] signature=new String[signat.size()];
                        signat.toArray(signature);
                        if(MLET_LOGGER.isLoggable(Level.FINEST)){
                            final StringBuilder strb=new StringBuilder();
                            for(int i=0;i<signature.length;i++){
                                strb.append("\n\tSignature     = ")
                                        .append(signature[i])
                                        .append("\t\nParams        = ")
                                        .append(parms[i]);
                            }
                            MLET_LOGGER.logp(Level.FINEST,
                                    MLet.class.getName(),
                                    mth,strb.toString());
                        }
                        if(name==null){
                            objInst=
                                    server.createMBean(code,null,mletObjectName,
                                            parms,signature);
                        }else{
                            objInst=
                                    server.createMBean(code,new ObjectName(name),
                                            mletObjectName,parms,
                                            signature);
                        }
                    }
                }else{
                    o=loadSerializedObject(codebase,serName);
                    if(name==null){
                        server.registerMBean(o,null);
                    }else{
                        server.registerMBean(o,new ObjectName(name));
                    }
                    objInst=new ObjectInstance(name,o.getClass().getName());
                }
            }catch(ReflectionException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "ReflectionException",ex);
                mbeans.add(ex);
                continue;
            }catch(InstanceAlreadyExistsException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "InstanceAlreadyExistsException",ex);
                mbeans.add(ex);
                continue;
            }catch(MBeanRegistrationException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "MBeanRegistrationException",ex);
                mbeans.add(ex);
                continue;
            }catch(MBeanException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "MBeanException",ex);
                mbeans.add(ex);
                continue;
            }catch(NotCompliantMBeanException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "NotCompliantMBeanException",ex);
                mbeans.add(ex);
                continue;
            }catch(InstanceNotFoundException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "InstanceNotFoundException",ex);
                mbeans.add(ex);
                continue;
            }catch(IOException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "IOException",ex);
                mbeans.add(ex);
                continue;
            }catch(SecurityException ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "SecurityException",ex);
                mbeans.add(ex);
                continue;
            }catch(Exception ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "Exception",ex);
                mbeans.add(ex);
                continue;
            }catch(Error ex){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        "Error",ex);
                mbeans.add(ex);
                continue;
            }
            mbeans.add(objInst);
        }
        return mbeans;
    }

    public ObjectName preRegister(MBeanServer server,ObjectName name)
            throws Exception{
        // Initialize local pointer to the MBean server
        setMBeanServer(server);
        // If no name is specified return a default name for the MLet
        if(name==null){
            name=new ObjectName(server.getDefaultDomain()+":"+ServiceName.MLET);
        }
        this.mletObjectName=name;
        return this.mletObjectName;
    }    public synchronized String getLibraryDirectory(){
        return libraryDirectory;
    }

    public void postRegister(Boolean registrationDone){
    }    public synchronized void setLibraryDirectory(String libdir){
        libraryDirectory=libdir;
    }

    public void preDeregister() throws Exception{
    }

    public void postDeregister(){
    }

    private synchronized void setMBeanServer(final MBeanServer server){
        this.server=server;
        PrivilegedAction<ClassLoaderRepository> act=
                new PrivilegedAction<ClassLoaderRepository>(){
                    public ClassLoaderRepository run(){
                        return server.getClassLoaderRepository();
                    }
                };
        currentClr=AccessController.doPrivileged(act);
    }

    public void writeExternal(ObjectOutput out)
            throws IOException, UnsupportedOperationException{
        throw new UnsupportedOperationException("MLet.writeExternal");
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException,
            UnsupportedOperationException{
        throw new UnsupportedOperationException("MLet.readExternal");
    }

    public synchronized Class<?> loadClass(String name,
                                           ClassLoaderRepository clr)
            throws ClassNotFoundException{
        final ClassLoaderRepository before=currentClr;
        try{
            currentClr=clr;
            return loadClass(name);
        }finally{
            currentClr=before;
        }
    }

    protected String findLibrary(String libname){
        String abs_path;
        String mth="findLibrary";
        // Get the platform-specific string representing a native library.
        //
        String nativelibname=System.mapLibraryName(libname);
        //
        // See if the native library is accessible as a resource through the JAR file.
        //
        if(MLET_LOGGER.isLoggable(Level.FINER)){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                    "Search "+libname+" in all JAR files");
        }
        // First try to locate the library in the JAR file using only
        // the native library name.  e.g. if user requested a load
        // for "foo" on Solaris SPARC 5.7 we try to load "libfoo.so"
        // from the JAR file.
        //
        if(MLET_LOGGER.isLoggable(Level.FINER)){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                    "loadLibraryAsResource("+nativelibname+")");
        }
        abs_path=loadLibraryAsResource(nativelibname);
        if(abs_path!=null){
            if(MLET_LOGGER.isLoggable(Level.FINER)){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        nativelibname+" loaded, absolute path = "+abs_path);
            }
            return abs_path;
        }
        // Next try to locate it using the native library name and
        // the architecture-specific path name.  e.g. if user
        // requested a load for "foo" on Solaris SPARC 5.7 we try to
        // load "SunOS/sparc/5.7/lib/libfoo.so" from the JAR file.
        //
        nativelibname=removeSpace(System.getProperty("os.name"))+File.separator+
                removeSpace(System.getProperty("os.arch"))+File.separator+
                removeSpace(System.getProperty("os.version"))+File.separator+
                "lib"+File.separator+nativelibname;
        if(MLET_LOGGER.isLoggable(Level.FINER)){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                    "loadLibraryAsResource("+nativelibname+")");
        }
        abs_path=loadLibraryAsResource(nativelibname);
        if(abs_path!=null){
            if(MLET_LOGGER.isLoggable(Level.FINER)){
                MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                        nativelibname+" loaded, absolute path = "+abs_path);
            }
            return abs_path;
        }
        //
        // All paths exhausted, library not found in JAR file.
        //
        if(MLET_LOGGER.isLoggable(Level.FINER)){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                    libname+" not found in any JAR file");
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),mth,
                    "Search "+libname+" along the path "+
                            "specified as the java.library.path property");
        }
        // Let the VM search the library along the path
        // specified as the java.library.path property.
        //
        return null;
    }

    private synchronized String loadLibraryAsResource(String libname){
        try{
            InputStream is=getResourceAsStream(
                    libname.replace(File.separatorChar,'/'));
            if(is!=null){
                try{
                    File directory=new File(libraryDirectory);
                    directory.mkdirs();
                    File file=Files.createTempFile(directory.toPath(),
                            libname+".",null)
                            .toFile();
                    file.deleteOnExit();
                    FileOutputStream fileOutput=new FileOutputStream(file);
                    try{
                        byte[] buf=new byte[4096];
                        int n;
                        while((n=is.read(buf))>=0){
                            fileOutput.write(buf,0,n);
                        }
                    }finally{
                        fileOutput.close();
                    }
                    if(file.exists()){
                        return file.getAbsolutePath();
                    }
                }finally{
                    is.close();
                }
            }
        }catch(Exception e){
            MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                    "loadLibraryAsResource",
                    "Failed to load library : "+libname,e);
            return null;
        }
        return null;
    }

    private static String removeSpace(String s){
        return s.trim().replace(" ","");
    }









    protected URL check(String version,URL codebase,String jarfile,
                        MLetContent mlet)
            throws Exception{
        return codebase;
    }

    private Object loadSerializedObject(URL codebase,String filename)
            throws IOException, ClassNotFoundException{
        if(filename!=null){
            filename=filename.replace(File.separatorChar,'/');
        }
        if(MLET_LOGGER.isLoggable(Level.FINER)){
            MLET_LOGGER.logp(Level.FINER,MLet.class.getName(),
                    "loadSerializedObject",codebase.toString()+filename);
        }
        InputStream is=getResourceAsStream(filename);
        if(is!=null){
            try{
                ObjectInputStream ois=new MLetObjectInputStream(is,this);
                Object serObject=ois.readObject();
                ois.close();
                return serObject;
            }catch(IOException e){
                if(MLET_LOGGER.isLoggable(Level.FINEST)){
                    MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                            "loadSerializedObject",
                            "Exception while deserializing "+filename,e);
                }
                throw e;
            }catch(ClassNotFoundException e){
                if(MLET_LOGGER.isLoggable(Level.FINEST)){
                    MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                            "loadSerializedObject",
                            "Exception while deserializing "+filename,e);
                }
                throw e;
            }
        }else{
            if(MLET_LOGGER.isLoggable(Level.FINEST)){
                MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                        "loadSerializedObject","Error: File "+filename+
                                " containing serialized object not found");
            }
            throw new Error("File "+filename+" containing serialized object not found");
        }
    }

    private Object constructParameter(String param,String type){
        // check if it is a primitive type
        Class<?> c=primitiveClasses.get(type);
        if(c!=null){
            try{
                Constructor<?> cons=
                        c.getConstructor(String.class);
                Object[] oo=new Object[1];
                oo[0]=param;
                return (cons.newInstance(oo));
            }catch(Exception e){
                MLET_LOGGER.logp(Level.FINEST,MLet.class.getName(),
                        "constructParameter","Got unexpected exception",e);
            }
        }
        if(type.compareTo("java.lang.Boolean")==0)
            return Boolean.valueOf(param);
        if(type.compareTo("java.lang.Byte")==0)
            return new Byte(param);
        if(type.compareTo("java.lang.Short")==0)
            return new Short(param);
        if(type.compareTo("java.lang.Long")==0)
            return new Long(param);
        if(type.compareTo("java.lang.Integer")==0)
            return new Integer(param);
        if(type.compareTo("java.lang.Float")==0)
            return new Float(param);
        if(type.compareTo("java.lang.Double")==0)
            return new Double(param);
        if(type.compareTo("java.lang.String")==0)
            return param;
        return param;
    }


}
