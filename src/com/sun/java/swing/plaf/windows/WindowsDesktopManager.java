/**
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.java.swing.plaf.windows;

import javax.swing.*;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;

public class WindowsDesktopManager extends DefaultDesktopManager
        implements java.io.Serializable, javax.swing.plaf.UIResource{
    private WeakReference<JInternalFrame> currentFrameRef;

    public void activateFrame(JInternalFrame f){
        JInternalFrame currentFrame=currentFrameRef!=null?
                currentFrameRef.get():null;
        try{
            super.activateFrame(f);
            if(currentFrame!=null&&f!=currentFrame){
                // If the current frame is maximized, transfer that
                // attribute to the frame being activated.
                if(currentFrame.isMaximum()&&
                        (f.getClientProperty("JInternalFrame.frameType")!=
                                "optionDialog")){
                    //Special case.  If key binding was used to select next
                    //frame instead of minimizing the icon via the minimize
                    //icon.
                    if(!currentFrame.isIcon()){
                        currentFrame.setMaximum(false);
                        if(f.isMaximizable()){
                            if(!f.isMaximum()){
                                f.setMaximum(true);
                            }else if(f.isMaximum()&&f.isIcon()){
                                f.setIcon(false);
                            }else{
                                f.setMaximum(false);
                            }
                        }
                    }
                }
                if(currentFrame.isSelected()){
                    currentFrame.setSelected(false);
                }
            }
            if(!f.isSelected()){
                f.setSelected(true);
            }
        }catch(PropertyVetoException e){
        }
        if(f!=currentFrame){
            currentFrameRef=new WeakReference<JInternalFrame>(f);
        }
    }
}
