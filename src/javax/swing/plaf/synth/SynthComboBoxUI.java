/**
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.synth;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SynthComboBoxUI extends BasicComboBoxUI implements
        PropertyChangeListener, SynthUI{
    Insets popupInsets;
    private SynthStyle style;
    private boolean useListColors;
    private boolean buttonWhenNotEditable;
    private boolean pressedWhenPopupVisible;
    private ButtonHandler buttonHandler;
    private EditorFocusHandler editorFocusHandler;
    private boolean forceOpaque=false;

    public static ComponentUI createUI(JComponent c){
        return new SynthComboBoxUI();
    }

    @Override
    public void installUI(JComponent c){
        buttonHandler=new ButtonHandler();
        super.installUI(c);
    }

    @Override
    public void uninstallUI(JComponent c){
        if(popup instanceof SynthComboPopup){
            ((SynthComboPopup)popup).removePopupMenuListener(buttonHandler);
        }
        super.uninstallUI(c);
        buttonHandler=null;
    }

    @Override
    protected void installDefaults(){
        updateStyle(comboBox);
    }

    private void updateStyle(JComboBox comboBox){
        SynthStyle oldStyle=style;
        SynthContext context=getContext(comboBox,ENABLED);
        style=SynthLookAndFeel.updateStyle(context,this);
        if(style!=oldStyle){
            padding=(Insets)style.get(context,"ComboBox.padding");
            popupInsets=(Insets)style.get(context,"ComboBox.popupInsets");
            useListColors=style.getBoolean(context,
                    "ComboBox.rendererUseListColors",true);
            buttonWhenNotEditable=style.getBoolean(context,
                    "ComboBox.buttonWhenNotEditable",false);
            pressedWhenPopupVisible=style.getBoolean(context,
                    "ComboBox.pressedWhenPopupVisible",false);
            squareButton=style.getBoolean(context,
                    "ComboBox.squareButton",true);
            if(oldStyle!=null){
                uninstallKeyboardActions();
                installKeyboardActions();
            }
            forceOpaque=style.getBoolean(context,
                    "ComboBox.forceOpaque",false);
        }
        context.dispose();
        if(listBox!=null){
            SynthLookAndFeel.updateStyles(listBox);
        }
    }

    private SynthContext getContext(JComponent c,int state){
        return SynthContext.getContext(c,style,state);
    }

    @Override
    protected void installListeners(){
        comboBox.addPropertyChangeListener(this);
        comboBox.addMouseListener(buttonHandler);
        editorFocusHandler=new EditorFocusHandler(comboBox);
        super.installListeners();
    }

    @Override
    protected void uninstallDefaults(){
        SynthContext context=getContext(comboBox,ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style=null;
    }

    @Override
    protected void uninstallListeners(){
        editorFocusHandler.unregister();
        comboBox.removePropertyChangeListener(this);
        comboBox.removeMouseListener(buttonHandler);
        buttonHandler.pressed=false;
        buttonHandler.over=false;
        super.uninstallListeners();
    }

    @Override
    protected ComboPopup createPopup(){
        SynthComboPopup p=new SynthComboPopup(comboBox);
        p.addPopupMenuListener(buttonHandler);
        return p;
    }

    @Override
    protected ListCellRenderer createRenderer(){
        return new SynthComboBoxRenderer();
    }

    @Override
    protected ComboBoxEditor createEditor(){
        return new SynthComboBoxEditor();
    }

    @Override
    protected JButton createArrowButton(){
        SynthArrowButton button=new SynthArrowButton(SwingConstants.SOUTH);
        button.setName("ComboBox.arrowButton");
        button.setModel(buttonHandler);
        return button;
    }

    @Override
    public void paint(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        paint(context,g);
        context.dispose();
    }
    //
    // end UI Initialization
    //======================

    @Override
    public void paintCurrentValue(Graphics g,Rectangle bounds,boolean hasFocus){
        ListCellRenderer renderer=comboBox.getRenderer();
        Component c;
        c=renderer.getListCellRendererComponent(
                listBox,comboBox.getSelectedItem(),-1,false,false);
        // Fix for 4238829: should lay out the JPanel.
        boolean shouldValidate=false;
        if(c instanceof JPanel){
            shouldValidate=true;
        }
        if(c instanceof UIResource){
            c.setName("ComboBox.renderer");
        }
        boolean force=forceOpaque&&c instanceof JComponent;
        if(force){
            ((JComponent)c).setOpaque(false);
        }
        int x=bounds.x, y=bounds.y, w=bounds.width, h=bounds.height;
        if(padding!=null){
            x=bounds.x+padding.left;
            y=bounds.y+padding.top;
            w=bounds.width-(padding.left+padding.right);
            h=bounds.height-(padding.top+padding.bottom);
        }
        currentValuePane.paintComponent(g,c,comboBox,x,y,w,h,shouldValidate);
        if(force){
            ((JComponent)c).setOpaque(true);
        }
    }

    @Override
    protected Dimension getDefaultSize(){
        SynthComboBoxRenderer r=new SynthComboBoxRenderer();
        Dimension d=getSizeForComponent(r.getListCellRendererComponent(listBox," ",-1,false,false));
        return new Dimension(d.width,d.height);
    }
    //=================================
    // begin ComponentUI Implementation

    @Override
    public void propertyChange(PropertyChangeEvent e){
        if(SynthLookAndFeel.shouldUpdateStyle(e)){
            updateStyle(comboBox);
        }
    }

    @Override
    public void update(Graphics g,JComponent c){
        SynthContext context=getContext(c);
        SynthLookAndFeel.update(context,g);
        context.getPainter().paintComboBoxBackground(context,g,0,0,
                c.getWidth(),c.getHeight());
        paint(context,g);
        context.dispose();
    }

    @Override
    public SynthContext getContext(JComponent c){
        return getContext(c,getComponentState(c));
    }

    private int getComponentState(JComponent c){
        // currently we have a broken situation where if a developer
        // takes the border from a JComboBox and sets it on a JTextField
        // then the codepath will eventually lead back to this method
        // but pass in a JTextField instead of JComboBox! In case this
        // happens, we just return the normal synth state for the component
        // instead of doing anything special
        if(!(c instanceof JComboBox)) return SynthLookAndFeel.getComponentState(c);
        JComboBox box=(JComboBox)c;
        if(shouldActLikeButton()){
            int state=ENABLED;
            if((!c.isEnabled())){
                state=DISABLED;
            }
            if(buttonHandler.isPressed()){
                state|=PRESSED;
            }
            if(buttonHandler.isRollover()){
                state|=MOUSE_OVER;
            }
            if(box.isFocusOwner()){
                state|=FOCUSED;
            }
            return state;
        }else{
            // for editable combos the editor component has the focus not the
            // combo box its self, so we should make the combo paint focused
            // when its editor has focus
            int basicState=SynthLookAndFeel.getComponentState(c);
            if(box.isEditable()&&
                    box.getEditor().getEditorComponent().isFocusOwner()){
                basicState|=FOCUSED;
            }
            return basicState;
        }
    }

    private boolean shouldActLikeButton(){
        return buttonWhenNotEditable&&!comboBox.isEditable();
    }

    @Override
    public void paintBorder(SynthContext context,Graphics g,int x,
                            int y,int w,int h){
        context.getPainter().paintComboBoxBorder(context,g,x,y,w,h);
    }

    protected void paint(SynthContext context,Graphics g){
        hasFocus=comboBox.hasFocus();
        if(!comboBox.isEditable()){
            Rectangle r=rectangleForCurrentValue();
            paintCurrentValue(g,r,hasFocus);
        }
    }

    private static class SynthComboBoxEditor
            extends BasicComboBoxEditor.UIResource{
        @Override
        public JTextField createEditorComponent(){
            JTextField f=new JTextField("",9);
            f.setName("ComboBox.textField");
            return f;
        }
    }

    private static class EditorFocusHandler implements FocusListener,
            PropertyChangeListener{
        private JComboBox comboBox;
        private ComboBoxEditor editor=null;
        private Component editorComponent=null;

        private EditorFocusHandler(JComboBox comboBox){
            this.comboBox=comboBox;
            editor=comboBox.getEditor();
            if(editor!=null){
                editorComponent=editor.getEditorComponent();
                if(editorComponent!=null){
                    editorComponent.addFocusListener(this);
                }
            }
            comboBox.addPropertyChangeListener("editor",this);
        }

        public void unregister(){
            comboBox.removePropertyChangeListener(this);
            if(editorComponent!=null){
                editorComponent.removeFocusListener(this);
            }
        }

        public void focusGained(FocusEvent e){
            // repaint whole combo on focus gain
            comboBox.repaint();
        }

        public void focusLost(FocusEvent e){
            // repaint whole combo on focus loss
            comboBox.repaint();
        }

        public void propertyChange(PropertyChangeEvent evt){
            ComboBoxEditor newEditor=comboBox.getEditor();
            if(editor!=newEditor){
                if(editorComponent!=null){
                    editorComponent.removeFocusListener(this);
                }
                editor=newEditor;
                if(editor!=null){
                    editorComponent=editor.getEditorComponent();
                    if(editorComponent!=null){
                        editorComponent.addFocusListener(this);
                    }
                }
            }
        }
    }

    private class SynthComboBoxRenderer extends JLabel implements ListCellRenderer<Object>, UIResource{
        public SynthComboBoxRenderer(){
            super();
            setText(" ");
        }

        @Override
        public String getName(){
            // SynthComboBoxRenderer should have installed Name while constructor is working.
            // The setName invocation in the SynthComboBoxRenderer() constructor doesn't work
            // because of the opaque property is installed in the constructor based on the
            // component name (see GTKStyle.isOpaque())
            String name=super.getName();
            return name==null?"ComboBox.renderer":name;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list,Object value,
                                                      int index,boolean isSelected,boolean cellHasFocus){
            setName("ComboBox.listRenderer");
            SynthLookAndFeel.resetSelectedUI();
            if(isSelected){
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if(!useListColors){
                    SynthLookAndFeel.setSelectedUI(
                            (SynthLabelUI)SynthLookAndFeel.getUIOfType(getUI(),
                                    SynthLabelUI.class),isSelected,cellHasFocus,
                            list.isEnabled(),false);
                }
            }else{
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            if(value instanceof Icon){
                setIcon((Icon)value);
                setText("");
            }else{
                String text=(value==null)?" ":value.toString();
                if("".equals(text)){
                    text=" ";
                }
                setText(text);
            }
            // The renderer component should inherit the enabled and
            // orientation state of its parent combobox.  This is
            // especially needed for GTK comboboxes, where the
            // ListCellRenderer's state determines the visual state
            // of the combobox.
            if(comboBox!=null){
                setEnabled(comboBox.isEnabled());
                setComponentOrientation(comboBox.getComponentOrientation());
            }
            return this;
        }

        @Override
        public void paint(Graphics g){
            super.paint(g);
            SynthLookAndFeel.resetSelectedUI();
        }
    }

    private final class ButtonHandler extends DefaultButtonModel
            implements MouseListener, PopupMenuListener{
        private boolean over;
        private boolean pressed;
        //------------------------------------------------------------------
        // State Methods
        //------------------------------------------------------------------

        @Override
        public boolean isArmed(){
            boolean b=shouldActLikeButton()||
                    (pressedWhenPopupVisible&&comboBox.isPopupVisible());
            return b?isPressed():super.isArmed();
        }

        @Override
        public boolean isPressed(){
            boolean b=shouldActLikeButton()?pressed:super.isPressed();
            return b||(pressedWhenPopupVisible&&comboBox.isPopupVisible());
        }
        //------------------------------------------------------------------
        // DefaultButtonModel Methods
        //------------------------------------------------------------------

        @Override
        public boolean isRollover(){
            return shouldActLikeButton()?over:super.isRollover();
        }

        @Override
        public void setRollover(boolean b){
            super.setRollover(b);
            updateOver(b);
        }

        private void updateOver(boolean o){
            boolean old=isRollover();
            this.over=o&&isEnabled();
            boolean newo=isRollover();
            if(shouldActLikeButton()&&old!=newo){
                comboBox.repaint();
            }
        }

        @Override
        public void setPressed(boolean b){
            super.setPressed(b);
            updatePressed(b);
        }

        private void updatePressed(boolean p){
            this.pressed=p&&isEnabled();
            if(shouldActLikeButton()){
                comboBox.repaint();
            }
        }
        //------------------------------------------------------------------
        // MouseListener/MouseMotionListener Methods
        //------------------------------------------------------------------

        @Override
        public void mouseClicked(MouseEvent e){
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent){
            updatePressed(true);
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent){
            updatePressed(false);
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent){
            updateOver(true);
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent){
            updateOver(false);
        }
        //------------------------------------------------------------------
        // PopupMenuListener Methods
        //------------------------------------------------------------------

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e){
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e){
            if(shouldActLikeButton()||pressedWhenPopupVisible){
                comboBox.repaint();
            }
        }
    }
}
