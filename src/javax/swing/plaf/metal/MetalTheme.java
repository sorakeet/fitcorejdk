/**
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public abstract class MetalTheme{
    // Contants identifying the various Fonts that are Theme can support
    static final int CONTROL_TEXT_FONT=0;
    static final int SYSTEM_TEXT_FONT=1;
    static final int USER_TEXT_FONT=2;
    static final int MENU_TEXT_FONT=3;
    static final int WINDOW_TITLE_FONT=4;
    static final int SUB_TEXT_FONT=5;
    static ColorUIResource white=new ColorUIResource(255,255,255);
    private static ColorUIResource black=new ColorUIResource(0,0,0);

    public abstract String getName();

    public abstract FontUIResource getControlTextFont();

    public abstract FontUIResource getSystemTextFont();

    public abstract FontUIResource getUserTextFont();

    public abstract FontUIResource getMenuTextFont();

    public abstract FontUIResource getWindowTitleFont();

    public abstract FontUIResource getSubTextFont();

    public ColorUIResource getFocusColor(){
        return getPrimary2();
    }

    protected abstract ColorUIResource getPrimary2();

    public ColorUIResource getDesktopColor(){
        return getPrimary2();
    }

    public ColorUIResource getControl(){
        return getSecondary3();
    }

    protected abstract ColorUIResource getSecondary3();

    public ColorUIResource getControlShadow(){
        return getSecondary2();
    }

    protected abstract ColorUIResource getSecondary2();

    public ColorUIResource getControlDarkShadow(){
        return getSecondary1();
    }

    protected abstract ColorUIResource getSecondary1();  // these are gray in Metal Default Theme

    public ColorUIResource getControlHighlight(){
        return getWhite();
    }

    protected ColorUIResource getWhite(){
        return white;
    }

    public ColorUIResource getPrimaryControl(){
        return getPrimary3();
    }

    protected abstract ColorUIResource getPrimary3();

    public ColorUIResource getPrimaryControlShadow(){
        return getPrimary2();
    }

    public ColorUIResource getPrimaryControlDarkShadow(){
        return getPrimary1();
    }

    protected abstract ColorUIResource getPrimary1();  // these are blue in Metal Default Theme

    public ColorUIResource getPrimaryControlInfo(){
        return getBlack();
    }

    protected ColorUIResource getBlack(){
        return black;
    }

    public ColorUIResource getPrimaryControlHighlight(){
        return getWhite();
    }

    public ColorUIResource getSystemTextColor(){
        return getBlack();
    }

    public ColorUIResource getInactiveControlTextColor(){
        return getControlDisabled();
    }

    public ColorUIResource getControlDisabled(){
        return getSecondary2();
    }

    public ColorUIResource getInactiveSystemTextColor(){
        return getSecondary2();
    }

    public ColorUIResource getUserTextColor(){
        return getBlack();
    }

    public ColorUIResource getTextHighlightColor(){
        return getPrimary3();
    }

    public ColorUIResource getHighlightedTextColor(){
        return getControlTextColor();
    }

    public ColorUIResource getControlTextColor(){
        return getControlInfo();
    }

    public ColorUIResource getControlInfo(){
        return getBlack();
    }

    public ColorUIResource getWindowBackground(){
        return getWhite();
    }

    public ColorUIResource getWindowTitleBackground(){
        return getPrimary3();
    }

    public ColorUIResource getWindowTitleForeground(){
        return getBlack();
    }

    public ColorUIResource getWindowTitleInactiveBackground(){
        return getSecondary3();
    }

    public ColorUIResource getWindowTitleInactiveForeground(){
        return getBlack();
    }

    public ColorUIResource getMenuBackground(){
        return getSecondary3();
    }

    public ColorUIResource getMenuForeground(){
        return getBlack();
    }

    public ColorUIResource getMenuSelectedBackground(){
        return getPrimary2();
    }

    public ColorUIResource getMenuSelectedForeground(){
        return getBlack();
    }

    public ColorUIResource getMenuDisabledForeground(){
        return getSecondary2();
    }

    public ColorUIResource getSeparatorBackground(){
        return getWhite();
    }

    public ColorUIResource getSeparatorForeground(){
        return getPrimary1();
    }

    public ColorUIResource getAcceleratorForeground(){
        return getPrimary1();
    }

    public ColorUIResource getAcceleratorSelectedForeground(){
        return getBlack();
    }

    public void addCustomEntriesToTable(UIDefaults table){
    }

    void install(){
    }

    boolean isSystemTheme(){
        return false;
    }
}
