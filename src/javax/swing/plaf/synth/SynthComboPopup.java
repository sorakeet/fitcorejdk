/**
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;

class SynthComboPopup extends BasicComboPopup{
    public SynthComboPopup(JComboBox combo){
        super(combo);
    }

    @Override
    protected void configureList(){
        list.setFont(comboBox.getFont());
        list.setCellRenderer(comboBox.getRenderer());
        list.setFocusable(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int selectedIndex=comboBox.getSelectedIndex();
        if(selectedIndex==-1){
            list.clearSelection();
        }else{
            list.setSelectedIndex(selectedIndex);
            list.ensureIndexIsVisible(selectedIndex);
        }
        installListListeners();
    }

    @Override
    protected Rectangle computePopupBounds(int px,int py,int pw,int ph){
        ComboBoxUI ui=comboBox.getUI();
        if(ui instanceof SynthComboBoxUI){
            SynthComboBoxUI sui=(SynthComboBoxUI)ui;
            if(sui.popupInsets!=null){
                Insets i=sui.popupInsets;
                return super.computePopupBounds(
                        px+i.left,
                        py+i.top,
                        pw-i.left-i.right,
                        ph-i.top-i.bottom);
            }
        }
        return super.computePopupBounds(px,py,pw,ph);
    }
}
