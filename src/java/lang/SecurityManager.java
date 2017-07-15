/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import sun.reflect.CallerSensitive;
import sun.security.util.SecurityConstants;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.lang.reflect.Member;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.security.*;
import java.util.PropertyPermission;

public class SecurityManager{
    private static final Object packageAccessLock=new Object();
    private static final Object packageDefinitionLock=new Object();
    private static ThreadGroup rootGroup=getRootGroup();
    private static boolean packageAccessValid=false;
    private static String[] packageAccess;
    private static boolean packageDefinitionValid=false;
    private static String[] packageDefinition;
    @Deprecated
    protected boolean inCheck;
    private boolean initialized=false;

    public SecurityManager(){
        synchronized(SecurityManager.class){
            SecurityManager sm=System.getSecurityManager();
            if(sm!=null){
                // ask the currently installed security manager if we
                // can create a new one.
                sm.checkPermission(new RuntimePermission
                        ("createSecurityManager"));
            }
            initialized=true;
        }
    }

    private static ThreadGroup getRootGroup(){
        ThreadGroup root=Thread.currentThread().getThreadGroup();
        while(root.getParent()!=null){
            root=root.getParent();
        }
        return root;
    }

    @Deprecated
    public boolean getInCheck(){
        return inCheck;
    }

    @Deprecated
    protected Class<?> currentLoadedClass(){
        Class<?> c=currentLoadedClass0();
        if((c!=null)&&hasAllPermission())
            c=null;
        return c;
    }

    private boolean hasAllPermission(){
        try{
            checkPermission(SecurityConstants.ALL_PERMISSION);
            return true;
        }catch(SecurityException se){
            return false;
        }
    }

    public void checkPermission(Permission perm){
        AccessController.checkPermission(perm);
    }

    private native Class<?> currentLoadedClass0();

    @Deprecated
    protected int classLoaderDepth(){
        int depth=classLoaderDepth0();
        if(depth!=-1){
            if(hasAllPermission())
                depth=-1;
            else
                depth--; // make sure we don't include ourself
        }
        return depth;
    }

    private native int classLoaderDepth0();

    @Deprecated
    protected boolean inClass(String name){
        return classDepth(name)>=0;
    }

    @Deprecated
    protected native int classDepth(String name);

    @Deprecated
    protected boolean inClassLoader(){
        return currentClassLoader()!=null;
    }

    @Deprecated
    protected ClassLoader currentClassLoader(){
        ClassLoader cl=currentClassLoader0();
        if((cl!=null)&&hasAllPermission())
            cl=null;
        return cl;
    }

    private native ClassLoader currentClassLoader0();

    public Object getSecurityContext(){
        return AccessController.getContext();
    }

    public void checkCreateClassLoader(){
        checkPermission(SecurityConstants.CREATE_CLASSLOADER_PERMISSION);
    }

    public void checkAccess(Thread t){
        if(t==null){
            throw new NullPointerException("thread can't be null");
        }
        if(t.getThreadGroup()==rootGroup){
            checkPermission(SecurityConstants.MODIFY_THREAD_PERMISSION);
        }else{
            // just return
        }
    }

    public void checkAccess(ThreadGroup g){
        if(g==null){
            throw new NullPointerException("thread group can't be null");
        }
        if(g==rootGroup){
            checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }else{
            // just return
        }
    }

    public void checkExit(int status){
        checkPermission(new RuntimePermission("exitVM."+status));
    }

    public void checkExec(String cmd){
        File f=new File(cmd);
        if(f.isAbsolute()){
            checkPermission(new FilePermission(cmd,
                    SecurityConstants.FILE_EXECUTE_ACTION));
        }else{
            checkPermission(new FilePermission("<<ALL FILES>>",
                    SecurityConstants.FILE_EXECUTE_ACTION));
        }
    }

    public void checkLink(String lib){
        if(lib==null){
            throw new NullPointerException("library can't be null");
        }
        checkPermission(new RuntimePermission("loadLibrary."+lib));
    }

    public void checkRead(FileDescriptor fd){
        if(fd==null){
            throw new NullPointerException("file descriptor can't be null");
        }
        checkPermission(new RuntimePermission("readFileDescriptor"));
    }

    public void checkRead(String file){
        checkPermission(new FilePermission(file,
                SecurityConstants.FILE_READ_ACTION));
    }

    public void checkRead(String file,Object context){
        checkPermission(
                new FilePermission(file,SecurityConstants.FILE_READ_ACTION),
                context);
    }

    public void checkPermission(Permission perm,Object context){
        if(context instanceof AccessControlContext){
            ((AccessControlContext)context).checkPermission(perm);
        }else{
            throw new SecurityException();
        }
    }

    public void checkWrite(FileDescriptor fd){
        if(fd==null){
            throw new NullPointerException("file descriptor can't be null");
        }
        checkPermission(new RuntimePermission("writeFileDescriptor"));
    }

    public void checkWrite(String file){
        checkPermission(new FilePermission(file,
                SecurityConstants.FILE_WRITE_ACTION));
    }

    public void checkDelete(String file){
        checkPermission(new FilePermission(file,
                SecurityConstants.FILE_DELETE_ACTION));
    }

    public void checkConnect(String host,int port){
        if(host==null){
            throw new NullPointerException("host can't be null");
        }
        if(!host.startsWith("[")&&host.indexOf(':')!=-1){
            host="["+host+"]";
        }
        if(port==-1){
            checkPermission(new SocketPermission(host,
                    SecurityConstants.SOCKET_RESOLVE_ACTION));
        }else{
            checkPermission(new SocketPermission(host+":"+port,
                    SecurityConstants.SOCKET_CONNECT_ACTION));
        }
    }

    public void checkConnect(String host,int port,Object context){
        if(host==null){
            throw new NullPointerException("host can't be null");
        }
        if(!host.startsWith("[")&&host.indexOf(':')!=-1){
            host="["+host+"]";
        }
        if(port==-1)
            checkPermission(new SocketPermission(host,
                            SecurityConstants.SOCKET_RESOLVE_ACTION),
                    context);
        else
            checkPermission(new SocketPermission(host+":"+port,
                            SecurityConstants.SOCKET_CONNECT_ACTION),
                    context);
    }

    public void checkListen(int port){
        checkPermission(new SocketPermission("localhost:"+port,
                SecurityConstants.SOCKET_LISTEN_ACTION));
    }

    public void checkAccept(String host,int port){
        if(host==null){
            throw new NullPointerException("host can't be null");
        }
        if(!host.startsWith("[")&&host.indexOf(':')!=-1){
            host="["+host+"]";
        }
        checkPermission(new SocketPermission(host+":"+port,
                SecurityConstants.SOCKET_ACCEPT_ACTION));
    }

    public void checkMulticast(InetAddress maddr){
        String host=maddr.getHostAddress();
        if(!host.startsWith("[")&&host.indexOf(':')!=-1){
            host="["+host+"]";
        }
        checkPermission(new SocketPermission(host,
                SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION));
    }

    @Deprecated
    public void checkMulticast(InetAddress maddr,byte ttl){
        String host=maddr.getHostAddress();
        if(!host.startsWith("[")&&host.indexOf(':')!=-1){
            host="["+host+"]";
        }
        checkPermission(new SocketPermission(host,
                SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION));
    }

    public void checkPropertiesAccess(){
        checkPermission(new PropertyPermission("*",
                SecurityConstants.PROPERTY_RW_ACTION));
    }

    public void checkPropertyAccess(String key){
        checkPermission(new PropertyPermission(key,
                SecurityConstants.PROPERTY_READ_ACTION));
    }

    @Deprecated
    public boolean checkTopLevelWindow(Object window){
        if(window==null){
            throw new NullPointerException("window can't be null");
        }
        Permission perm=SecurityConstants.AWT.TOPLEVEL_WINDOW_PERMISSION;
        if(perm==null){
            perm=SecurityConstants.ALL_PERMISSION;
        }
        try{
            checkPermission(perm);
            return true;
        }catch(SecurityException se){
            // just return false
        }
        return false;
    }

    public void checkPrintJobAccess(){
        checkPermission(new RuntimePermission("queuePrintJob"));
    }

    @Deprecated
    public void checkSystemClipboardAccess(){
        Permission perm=SecurityConstants.AWT.ACCESS_CLIPBOARD_PERMISSION;
        if(perm==null){
            perm=SecurityConstants.ALL_PERMISSION;
        }
        checkPermission(perm);
    }

    @Deprecated
    public void checkAwtEventQueueAccess(){
        Permission perm=SecurityConstants.AWT.CHECK_AWT_EVENTQUEUE_PERMISSION;
        if(perm==null){
            perm=SecurityConstants.ALL_PERMISSION;
        }
        checkPermission(perm);
    }

    public void checkPackageAccess(String pkg){
        if(pkg==null){
            throw new NullPointerException("package name can't be null");
        }
        String[] pkgs;
        synchronized(packageAccessLock){
            /**
             * Do we need to update our property array?
             */
            if(!packageAccessValid){
                String tmpPropertyStr=
                        AccessController.doPrivileged(
                                new PrivilegedAction<String>(){
                                    public String run(){
                                        return Security.getProperty(
                                                "package.access");
                                    }
                                }
                        );
                packageAccess=getPackages(tmpPropertyStr);
                packageAccessValid=true;
            }
            // Using a snapshot of packageAccess -- don't care if static field
            // changes afterwards; array contents won't change.
            pkgs=packageAccess;
        }
        /**
         * Traverse the list of packages, check for any matches.
         */
        for(int i=0;i<pkgs.length;i++){
            if(pkg.startsWith(pkgs[i])||pkgs[i].equals(pkg+".")){
                checkPermission(
                        new RuntimePermission("accessClassInPackage."+pkg));
                break;  // No need to continue; only need to check this once
            }
        }
    }

    private static String[] getPackages(String p){
        String packages[]=null;
        if(p!=null&&!p.equals("")){
            java.util.StringTokenizer tok=
                    new java.util.StringTokenizer(p,",");
            int n=tok.countTokens();
            if(n>0){
                packages=new String[n];
                int i=0;
                while(tok.hasMoreElements()){
                    String s=tok.nextToken().trim();
                    packages[i++]=s;
                }
            }
        }
        if(packages==null)
            packages=new String[0];
        return packages;
    }

    public void checkPackageDefinition(String pkg){
        if(pkg==null){
            throw new NullPointerException("package name can't be null");
        }
        String[] pkgs;
        synchronized(packageDefinitionLock){
            /**
             * Do we need to update our property array?
             */
            if(!packageDefinitionValid){
                String tmpPropertyStr=
                        AccessController.doPrivileged(
                                new PrivilegedAction<String>(){
                                    public String run(){
                                        return Security.getProperty(
                                                "package.definition");
                                    }
                                }
                        );
                packageDefinition=getPackages(tmpPropertyStr);
                packageDefinitionValid=true;
            }
            // Using a snapshot of packageDefinition -- don't care if static
            // field changes afterwards; array contents won't change.
            pkgs=packageDefinition;
        }
        /**
         * Traverse the list of packages, check for any matches.
         */
        for(int i=0;i<pkgs.length;i++){
            if(pkg.startsWith(pkgs[i])||pkgs[i].equals(pkg+".")){
                checkPermission(
                        new RuntimePermission("defineClassInPackage."+pkg));
                break; // No need to continue; only need to check this once
            }
        }
    }

    public void checkSetFactory(){
        checkPermission(new RuntimePermission("setFactory"));
    }

    @Deprecated
    @CallerSensitive
    public void checkMemberAccess(Class<?> clazz,int which){
        if(clazz==null){
            throw new NullPointerException("class can't be null");
        }
        if(which!=Member.PUBLIC){
            Class<?> stack[]=getClassContext();
            /**
             * stack depth of 4 should be the caller of one of the
             * methods in java.lang.Class that invoke checkMember
             * access. The stack should look like:
             *
             * someCaller                        [3]
             * java.lang.Class.someReflectionAPI [2]
             * java.lang.Class.checkMemberAccess [1]
             * SecurityManager.checkMemberAccess [0]
             *
             */
            if((stack.length<4)||
                    (stack[3].getClassLoader()!=clazz.getClassLoader())){
                checkPermission(SecurityConstants.CHECK_MEMBER_ACCESS_PERMISSION);
            }
        }
    }

    protected native Class[] getClassContext();

    public void checkSecurityAccess(String target){
        checkPermission(new SecurityPermission(target));
    }

    public ThreadGroup getThreadGroup(){
        return Thread.currentThread().getThreadGroup();
    }
}
