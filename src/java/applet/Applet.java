/**
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.applet;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class Applet extends Panel{
    private static final long serialVersionUID=-5836846270535785031L;
    //
    // Accessibility support
    //
    AccessibleContext accessibleContext=null;
    transient private AppletStub stub;

    public Applet() throws HeadlessException{
        if(GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }
    }

    public final static AudioClip newAudioClip(URL url){
        return new sun.applet.AppletAudioClip(url);
    }

    private void readObject(ObjectInputStream s)
            throws ClassNotFoundException, IOException, HeadlessException{
        if(GraphicsEnvironment.isHeadless()){
            throw new HeadlessException();
        }
        s.defaultReadObject();
    }

    public final void setStub(AppletStub stub){
        if(this.stub!=null){
            SecurityManager s=System.getSecurityManager();
            if(s!=null){
                s.checkPermission(new AWTPermission("setAppletStub"));
            }
        }
        this.stub=stub;
    }

    public boolean isActive(){
        if(stub!=null){
            return stub.isActive();
        }else{        // If stub field not filled in, applet never active
            return false;
        }
    }

    public URL getDocumentBase(){
        return stub.getDocumentBase();
    }

    public URL getCodeBase(){
        return stub.getCodeBase();
    }

    public String getParameter(String name){
        return stub.getParameter(name);
    }

    @Override
    public boolean isValidateRoot(){
        return true;
    }

    public void showStatus(String msg){
        getAppletContext().showStatus(msg);
    }

    public AppletContext getAppletContext(){
        return stub.getAppletContext();
    }

    public Image getImage(URL url,String name){
        try{
            return getImage(new URL(url,name));
        }catch(MalformedURLException e){
            return null;
        }
    }

    public Image getImage(URL url){
        return getAppletContext().getImage(url);
    }

    public String getAppletInfo(){
        return null;
    }

    public Locale getLocale(){
        Locale locale=super.getLocale();
        if(locale==null){
            return Locale.getDefault();
        }
        return locale;
    }

    @SuppressWarnings("deprecation")
    public void resize(int width,int height){
        Dimension d=size();
        if((d.width!=width)||(d.height!=height)){
            super.resize(width,height);
            if(stub!=null){
                stub.appletResize(width,height);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void resize(Dimension d){
        resize(d.width,d.height);
    }

    public String[][] getParameterInfo(){
        return null;
    }

    public void play(URL url){
        AudioClip clip=getAudioClip(url);
        if(clip!=null){
            clip.play();
        }
    }

    public AudioClip getAudioClip(URL url){
        return getAppletContext().getAudioClip(url);
    }

    public void play(URL url,String name){
        AudioClip clip=getAudioClip(url,name);
        if(clip!=null){
            clip.play();
        }
    }

    public AudioClip getAudioClip(URL url,String name){
        try{
            return getAudioClip(new URL(url,name));
        }catch(MalformedURLException e){
            return null;
        }
    }

    public void init(){
    }

    public void start(){
    }

    public void stop(){
    }

    public void destroy(){
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleApplet();
        }
        return accessibleContext;
    }

    protected class AccessibleApplet extends AccessibleAWTPanel{
        private static final long serialVersionUID=8127374778187708896L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.FRAME;
        }

        public AccessibleStateSet getAccessibleStateSet(){
            AccessibleStateSet states=super.getAccessibleStateSet();
            states.add(AccessibleState.ACTIVE);
            return states;
        }
    }
}
