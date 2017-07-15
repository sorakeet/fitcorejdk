/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.colorchooser;

import javax.swing.*;

public class ColorChooserComponentFactory{
    private ColorChooserComponentFactory(){
    } // can't instantiate

    public static AbstractColorChooserPanel[] getDefaultChooserPanels(){
        return new AbstractColorChooserPanel[]{
                new DefaultSwatchChooserPanel(),
                new ColorChooserPanel(new ColorModelHSV()),
                new ColorChooserPanel(new ColorModelHSL()),
                new ColorChooserPanel(new ColorModel()),
                new ColorChooserPanel(new ColorModelCMYK()),
        };
    }

    public static JComponent getPreviewPanel(){
        return new DefaultPreviewPanel();
    }
}
