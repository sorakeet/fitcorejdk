/**
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

// NOTE: Don't rely on this class staying in this location. It is likely
// to move to a different package in the future.
public class DesktopProperty implements UIDefaults.ActiveValue{
    private static final ReferenceQueue<DesktopProperty> queue=new ReferenceQueue<DesktopProperty>();
    private static boolean updatePending;
    private final String key;
    private final Object fallback;
    private WeakPCL pcl;
    private Object value;

    public DesktopProperty(String key,Object fallback){
        this.key=key;
        this.fallback=fallback;
        // The only sure fire way to clear our references is to create a
        // Thread and wait for a reference to be added to the queue.
        // Because it is so rare that you will actually change the look
        // and feel, this stepped is forgoed and a middle ground of
        // flushing references from the constructor is instead done.
        // The implication is that once one DesktopProperty is created
        // there will most likely be n (number of DesktopProperties created
        // by the LookAndFeel) WeakPCLs around, but this number will not
        // grow past n.
        flushUnreferencedProperties();
    }

    static void flushUnreferencedProperties(){
        WeakPCL pcl;
        while((pcl=(WeakPCL)queue.poll())!=null){
            pcl.dispose();
        }
    }

    private static synchronized boolean isUpdatePending(){
        return updatePending;
    }

    private static synchronized void setUpdatePending(boolean update){
        updatePending=update;
    }

    private static void updateAllUIs(){
        // Check if the current UI is WindowsLookAndfeel and flush the XP style map.
        // Note: Change the package test if this class is moved to a different package.
        Class uiClass=UIManager.getLookAndFeel().getClass();
        if(uiClass.getPackage().equals(DesktopProperty.class.getPackage())){
            XPStyle.invalidateStyle();
        }
        Frame appFrames[]=Frame.getFrames();
        for(Frame appFrame : appFrames){
            updateWindowUI(appFrame);
        }
    }

    private static void updateWindowUI(Window window){
        SwingUtilities.updateComponentTreeUI(window);
        Window ownedWins[]=window.getOwnedWindows();
        for(Window ownedWin : ownedWins){
            updateWindowUI(ownedWin);
        }
    }

    public Object createValue(UIDefaults table){
        if(value==null){
            value=configureValue(getValueFromDesktop());
            if(value==null){
                value=configureValue(getDefaultValue());
            }
        }
        return value;
    }

    protected Object getValueFromDesktop(){
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        if(pcl==null){
            pcl=new WeakPCL(this,getKey(),UIManager.getLookAndFeel());
            toolkit.addPropertyChangeListener(getKey(),pcl);
        }
        return toolkit.getDesktopProperty(getKey());
    }

    protected String getKey(){
        return key;
    }

    protected Object getDefaultValue(){
        return fallback;
    }

    protected Object configureValue(Object value){
        if(value!=null){
            if(value instanceof Color){
                return new ColorUIResource((Color)value);
            }else if(value instanceof Font){
                return new FontUIResource((Font)value);
            }else if(value instanceof UIDefaults.LazyValue){
                value=((UIDefaults.LazyValue)value).createValue(null);
            }else if(value instanceof UIDefaults.ActiveValue){
                value=((UIDefaults.ActiveValue)value).createValue(null);
            }
        }
        return value;
    }

    public void invalidate(LookAndFeel laf){
        invalidate();
    }

    public void invalidate(){
        value=null;
    }

    protected void updateUI(){
        if(!isUpdatePending()){
            setUpdatePending(true);
            Runnable uiUpdater=new Runnable(){
                public void run(){
                    updateAllUIs();
                    setUpdatePending(false);
                }
            };
            SwingUtilities.invokeLater(uiUpdater);
        }
    }

    private static class WeakPCL extends WeakReference<DesktopProperty>
            implements PropertyChangeListener{
        private String key;
        private LookAndFeel laf;

        WeakPCL(DesktopProperty target,String key,LookAndFeel laf){
            super(target,queue);
            this.key=key;
            this.laf=laf;
        }

        public void propertyChange(PropertyChangeEvent pce){
            DesktopProperty property=get();
            if(property==null||laf!=UIManager.getLookAndFeel()){
                // The property was GC'ed, we're no longer interested in
                // PropertyChanges, remove the listener.
                dispose();
            }else{
                property.invalidate(laf);
                property.updateUI();
            }
        }

        void dispose(){
            Toolkit.getDefaultToolkit().removePropertyChangeListener(key,this);
        }
    }
}
