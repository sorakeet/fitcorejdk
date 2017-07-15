/**
 * Copyright (c) 2005, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import sun.awt.SunToolkit;
import sun.security.util.SecurityConstants;

import java.awt.peer.DesktopPeer;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Desktop{
    private DesktopPeer peer;
    ;

    private Desktop(){
        peer=Toolkit.getDefaultToolkit().createDesktopPeer(this);
    }

    public static synchronized Desktop getDesktop(){
        if(GraphicsEnvironment.isHeadless()) throw new HeadlessException();
        if(!Desktop.isDesktopSupported()){
            throw new UnsupportedOperationException("Desktop API is not "+
                    "supported on the current platform");
        }
        sun.awt.AppContext context=sun.awt.AppContext.getAppContext();
        Desktop desktop=(Desktop)context.get(Desktop.class);
        if(desktop==null){
            desktop=new Desktop();
            context.put(Desktop.class,desktop);
        }
        return desktop;
    }

    public static boolean isDesktopSupported(){
        Toolkit defaultToolkit=Toolkit.getDefaultToolkit();
        if(defaultToolkit instanceof SunToolkit){
            return ((SunToolkit)defaultToolkit).isDesktopSupported();
        }
        return false;
    }

    public void open(File file) throws IOException{
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.OPEN);
        checkFileValidation(file);
        peer.open(file);
    }

    private static void checkFileValidation(File file){
        if(file==null) throw new NullPointerException("File must not be null");
        if(!file.exists()){
            throw new IllegalArgumentException("The file: "
                    +file.getPath()+" doesn't exist.");
        }
        file.canRead();
    }

    private void checkActionSupport(Action actionType){
        if(!isSupported(actionType)){
            throw new UnsupportedOperationException("The "+actionType.name()
                    +" action is not supported on the current platform!");
        }
    }

    public boolean isSupported(Action action){
        return peer.isSupported(action);
    }

    private void checkAWTPermission(){
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new AWTPermission(
                    "showWindowWithoutWarningBanner"));
        }
    }

    private void checkExec() throws SecurityException{
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPermission(new FilePermission("<<ALL FILES>>",
                    SecurityConstants.FILE_EXECUTE_ACTION));
        }
    }

    public void edit(File file) throws IOException{
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.EDIT);
        file.canWrite();
        checkFileValidation(file);
        peer.edit(file);
    }

    public void print(File file) throws IOException{
        checkExec();
        SecurityManager sm=System.getSecurityManager();
        if(sm!=null){
            sm.checkPrintJobAccess();
        }
        checkActionSupport(Action.PRINT);
        checkFileValidation(file);
        peer.print(file);
    }

    public void browse(URI uri) throws IOException{
        SecurityException securityException=null;
        try{
            checkAWTPermission();
            checkExec();
        }catch(SecurityException e){
            securityException=e;
        }
        checkActionSupport(Action.BROWSE);
        if(uri==null){
            throw new NullPointerException();
        }
        if(securityException==null){
            peer.browse(uri);
            return;
        }
        // Calling thread doesn't have necessary priviledges.
        // Delegate to DesktopBrowse so that it can work in
        // applet/webstart.
        URL url=null;
        try{
            url=uri.toURL();
        }catch(MalformedURLException e){
            throw new IllegalArgumentException("Unable to convert URI to URL",e);
        }
        sun.awt.DesktopBrowse db=sun.awt.DesktopBrowse.getInstance();
        if(db==null){
            // Not in webstart/applet, throw the exception.
            throw securityException;
        }
        db.browse(url);
    }

    public void mail() throws IOException{
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.MAIL);
        URI mailtoURI=null;
        try{
            mailtoURI=new URI("mailto:?");
            peer.mail(mailtoURI);
        }catch(URISyntaxException e){
            // won't reach here.
        }
    }

    public void mail(URI mailtoURI) throws IOException{
        checkAWTPermission();
        checkExec();
        checkActionSupport(Action.MAIL);
        if(mailtoURI==null) throw new NullPointerException();
        if(!"mailto".equalsIgnoreCase(mailtoURI.getScheme())){
            throw new IllegalArgumentException("URI scheme is not \"mailto\"");
        }
        peer.mail(mailtoURI);
    }

    public static enum Action{
        OPEN,
        EDIT,
        PRINT,
        MAIL,
        BROWSE
    }
}
