/**
 * Copyright (c) 2001, 2009, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.metal;

import java.awt.*;

class MetalFontDesktopProperty extends com.sun.java.swing.plaf.windows.DesktopProperty{
    private static final String[] propertyMapping={
            "win.ansiVar.font.height",
            "win.tooltip.font.height",
            "win.ansiVar.font.height",
            "win.menu.font.height",
            "win.frame.captionFont.height",
            "win.menu.font.height"
    };
    private int type;

    MetalFontDesktopProperty(int type){
        this(propertyMapping[type],type);
    }

    MetalFontDesktopProperty(String key,int type){
        super(key,null);
        this.type=type;
    }

    protected Object getDefaultValue(){
        return new Font(DefaultMetalTheme.getDefaultFontName(type),
                DefaultMetalTheme.getDefaultFontStyle(type),
                DefaultMetalTheme.getDefaultFontSize(type));
    }

    protected Object configureValue(Object value){
        if(value instanceof Integer){
            value=new Font(DefaultMetalTheme.getDefaultFontName(type),
                    DefaultMetalTheme.getDefaultFontStyle(type),
                    ((Integer)value).intValue());
        }
        return super.configureValue(value);
    }
}
