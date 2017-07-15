/**
 * Copyright (c) 2000, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SpinnerUI;
import javax.swing.plaf.UIResource;
import javax.swing.text.InternationalFormatter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.*;
import java.util.Calendar;
import java.util.Map;

public class BasicSpinnerUI extends SpinnerUI{
    private static final ArrowButtonHandler nextButtonHandler=new ArrowButtonHandler("increment",true);
    private static final ArrowButtonHandler previousButtonHandler=new ArrowButtonHandler("decrement",false);
    private static final Dimension zeroSize=new Dimension(0,0);
    protected JSpinner spinner;
    private Handler handler;
    private PropertyChangeListener propertyChangeListener;

    public static ComponentUI createUI(JComponent c){
        return new BasicSpinnerUI();
    }

    static void loadActionMap(LazyActionMap map){
        map.put("increment",nextButtonHandler);
        map.put("decrement",previousButtonHandler);
    }

    private void maybeAdd(Component c,String s){
        if(c!=null){
            spinner.add(c,s);
        }
    }

    public void installUI(JComponent c){
        this.spinner=(JSpinner)c;
        installDefaults();
        installListeners();
        maybeAdd(createNextButton(),"Next");
        maybeAdd(createPreviousButton(),"Previous");
        maybeAdd(createEditor(),"Editor");
        updateEnabledState();
        installKeyboardActions();
    }

    public void uninstallUI(JComponent c){
        uninstallDefaults();
        uninstallListeners();
        this.spinner=null;
        c.removeAll();
    }

    protected void uninstallListeners(){
        spinner.removePropertyChangeListener(propertyChangeListener);
        spinner.removeChangeListener(handler);
        JComponent editor=spinner.getEditor();
        removeEditorBorderListener(editor);
        if(editor instanceof JSpinner.DefaultEditor){
            JTextField tf=((JSpinner.DefaultEditor)editor).getTextField();
            if(tf!=null){
                tf.removeFocusListener(nextButtonHandler);
                tf.removeFocusListener(previousButtonHandler);
            }
        }
        propertyChangeListener=null;
        handler=null;
    }

    private void removeEditorBorderListener(JComponent editor){
        if(!UIManager.getBoolean("Spinner.editorBorderPainted")){
            if(editor instanceof JPanel&&
                    editor.getComponentCount()>0){
                editor=(JComponent)editor.getComponent(0);
            }
            if(editor!=null){
                editor.removePropertyChangeListener(getHandler());
            }
        }
    }

    private Handler getHandler(){
        if(handler==null){
            handler=new Handler();
        }
        return handler;
    }

    protected void uninstallDefaults(){
        spinner.setLayout(null);
    }

    public int getBaseline(JComponent c,int width,int height){
        super.getBaseline(c,width,height);
        JComponent editor=spinner.getEditor();
        Insets insets=spinner.getInsets();
        width=width-insets.left-insets.right;
        height=height-insets.top-insets.bottom;
        if(width>=0&&height>=0){
            int baseline=editor.getBaseline(width,height);
            if(baseline>=0){
                return insets.top+baseline;
            }
        }
        return -1;
    }

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c){
        super.getBaselineResizeBehavior(c);
        return spinner.getEditor().getBaselineResizeBehavior();
    }

    protected void installListeners(){
        propertyChangeListener=createPropertyChangeListener();
        spinner.addPropertyChangeListener(propertyChangeListener);
        if(DefaultLookup.getBoolean(spinner,this,
                "Spinner.disableOnBoundaryValues",false)){
            spinner.addChangeListener(getHandler());
        }
        JComponent editor=spinner.getEditor();
        if(editor!=null&&editor instanceof JSpinner.DefaultEditor){
            JTextField tf=((JSpinner.DefaultEditor)editor).getTextField();
            if(tf!=null){
                tf.addFocusListener(nextButtonHandler);
                tf.addFocusListener(previousButtonHandler);
            }
        }
    }

    protected void installDefaults(){
        spinner.setLayout(createLayout());
        LookAndFeel.installBorder(spinner,"Spinner.border");
        LookAndFeel.installColorsAndFont(spinner,"Spinner.background","Spinner.foreground","Spinner.font");
        LookAndFeel.installProperty(spinner,"opaque",Boolean.TRUE);
    }

    protected void installNextButtonListeners(Component c){
        installButtonListeners(c,nextButtonHandler);
    }

    protected void installPreviousButtonListeners(Component c){
        installButtonListeners(c,previousButtonHandler);
    }

    private void installButtonListeners(Component c,
                                        ArrowButtonHandler handler){
        if(c instanceof JButton){
            ((JButton)c).addActionListener(handler);
        }
        c.addMouseListener(handler);
    }

    protected LayoutManager createLayout(){
        return getHandler();
    }

    protected PropertyChangeListener createPropertyChangeListener(){
        return getHandler();
    }

    protected Component createPreviousButton(){
        Component c=createArrowButton(SwingConstants.SOUTH);
        c.setName("Spinner.previousButton");
        installPreviousButtonListeners(c);
        return c;
    }

    protected Component createNextButton(){
        Component c=createArrowButton(SwingConstants.NORTH);
        c.setName("Spinner.nextButton");
        installNextButtonListeners(c);
        return c;
    }

    private Component createArrowButton(int direction){
        JButton b=new BasicArrowButton(direction);
        Border buttonBorder=UIManager.getBorder("Spinner.arrowButtonBorder");
        if(buttonBorder instanceof UIResource){
            // Wrap the border to avoid having the UIResource be replaced by
            // the ButtonUI. This is the opposite of using BorderUIResource.
            b.setBorder(new CompoundBorder(buttonBorder,null));
        }else{
            b.setBorder(buttonBorder);
        }
        b.setInheritsPopupMenu(true);
        return b;
    }

    protected JComponent createEditor(){
        JComponent editor=spinner.getEditor();
        maybeRemoveEditorBorder(editor);
        installEditorBorderListener(editor);
        editor.setInheritsPopupMenu(true);
        updateEditorAlignment(editor);
        return editor;
    }

    protected void replaceEditor(JComponent oldEditor,JComponent newEditor){
        spinner.remove(oldEditor);
        maybeRemoveEditorBorder(newEditor);
        installEditorBorderListener(newEditor);
        newEditor.setInheritsPopupMenu(true);
        spinner.add(newEditor,"Editor");
    }

    private void maybeRemoveEditorBorder(JComponent editor){
        if(!UIManager.getBoolean("Spinner.editorBorderPainted")){
            if(editor instanceof JPanel&&
                    editor.getBorder()==null&&
                    editor.getComponentCount()>0){
                editor=(JComponent)editor.getComponent(0);
            }
            if(editor!=null&&editor.getBorder() instanceof UIResource){
                editor.setBorder(null);
            }
        }
    }

    private void installEditorBorderListener(JComponent editor){
        if(!UIManager.getBoolean("Spinner.editorBorderPainted")){
            if(editor instanceof JPanel&&
                    editor.getBorder()==null&&
                    editor.getComponentCount()>0){
                editor=(JComponent)editor.getComponent(0);
            }
            if(editor!=null&&
                    (editor.getBorder()==null||
                            editor.getBorder() instanceof UIResource)){
                editor.addPropertyChangeListener(getHandler());
            }
        }
    }

    private void updateEditorAlignment(JComponent editor){
        if(editor instanceof JSpinner.DefaultEditor){
            // if editor alignment isn't set in LAF, we get 0 (CENTER) here
            int alignment=UIManager.getInt("Spinner.editorAlignment");
            JTextField text=((JSpinner.DefaultEditor)editor).getTextField();
            text.setHorizontalAlignment(alignment);
        }
    }

    private void updateEnabledState(){
        updateEnabledState(spinner,spinner.isEnabled());
    }

    private void updateEnabledState(Container c,boolean enabled){
        for(int counter=c.getComponentCount()-1;counter>=0;counter--){
            Component child=c.getComponent(counter);
            if(DefaultLookup.getBoolean(spinner,this,
                    "Spinner.disableOnBoundaryValues",false)){
                SpinnerModel model=spinner.getModel();
                if(child.getName()=="Spinner.nextButton"&&
                        model.getNextValue()==null){
                    child.setEnabled(false);
                }else if(child.getName()=="Spinner.previousButton"&&
                        model.getPreviousValue()==null){
                    child.setEnabled(false);
                }else{
                    child.setEnabled(enabled);
                }
            }else{
                child.setEnabled(enabled);
            }
            if(child instanceof Container){
                updateEnabledState((Container)child,enabled);
            }
        }
    }

    protected void installKeyboardActions(){
        InputMap iMap=getInputMap(JComponent.
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(spinner,JComponent.
                        WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                iMap);
        LazyActionMap.installLazyActionMap(spinner,BasicSpinnerUI.class,
                "Spinner.actionMap");
    }

    private InputMap getInputMap(int condition){
        if(condition==JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT){
            return (InputMap)DefaultLookup.get(spinner,this,
                    "Spinner.ancestorInputMap");
        }
        return null;
    }

    private static class ArrowButtonHandler extends AbstractAction
            implements FocusListener, MouseListener, UIResource{
        final javax.swing.Timer autoRepeatTimer;
        final boolean isNext;
        JSpinner spinner=null;
        JButton arrowButton=null;

        ArrowButtonHandler(String name,boolean isNext){
            super(name);
            this.isNext=isNext;
            autoRepeatTimer=new javax.swing.Timer(60,this);
            autoRepeatTimer.setInitialDelay(300);
        }

        public void actionPerformed(ActionEvent e){
            JSpinner spinner=this.spinner;
            if(!(e.getSource() instanceof javax.swing.Timer)){
                // Most likely resulting from being in ActionMap.
                spinner=eventToSpinner(e);
                if(e.getSource() instanceof JButton){
                    arrowButton=(JButton)e.getSource();
                }
            }else{
                if(arrowButton!=null&&!arrowButton.getModel().isPressed()
                        &&autoRepeatTimer.isRunning()){
                    autoRepeatTimer.stop();
                    spinner=null;
                    arrowButton=null;
                }
            }
            if(spinner!=null){
                try{
                    int calendarField=getCalendarField(spinner);
                    spinner.commitEdit();
                    if(calendarField!=-1){
                        ((SpinnerDateModel)spinner.getModel()).
                                setCalendarField(calendarField);
                    }
                    Object value=(isNext)?spinner.getNextValue():
                            spinner.getPreviousValue();
                    if(value!=null){
                        spinner.setValue(value);
                        select(spinner);
                    }
                }catch(IllegalArgumentException iae){
                    UIManager.getLookAndFeel().provideErrorFeedback(spinner);
                }catch(ParseException pe){
                    UIManager.getLookAndFeel().provideErrorFeedback(spinner);
                }
            }
        }

        private JSpinner eventToSpinner(AWTEvent e){
            Object src=e.getSource();
            while((src instanceof Component)&&!(src instanceof JSpinner)){
                src=((Component)src).getParent();
            }
            return (src instanceof JSpinner)?(JSpinner)src:null;
        }

        private void select(JSpinner spinner){
            JComponent editor=spinner.getEditor();
            if(editor instanceof JSpinner.DateEditor){
                JSpinner.DateEditor dateEditor=(JSpinner.DateEditor)editor;
                JFormattedTextField ftf=dateEditor.getTextField();
                Format format=dateEditor.getFormat();
                Object value;
                if(format!=null&&(value=spinner.getValue())!=null){
                    SpinnerDateModel model=dateEditor.getModel();
                    DateFormat.Field field=DateFormat.Field.ofCalendarField(
                            model.getCalendarField());
                    if(field!=null){
                        try{
                            AttributedCharacterIterator iterator=format.
                                    formatToCharacterIterator(value);
                            if(!select(ftf,iterator,field)&&
                                    field==DateFormat.Field.HOUR0){
                                select(ftf,iterator,DateFormat.Field.HOUR1);
                            }
                        }catch(IllegalArgumentException iae){
                        }
                    }
                }
            }
        }

        private boolean select(JFormattedTextField ftf,
                               AttributedCharacterIterator iterator,
                               DateFormat.Field field){
            int max=ftf.getDocument().getLength();
            iterator.first();
            do{
                Map attrs=iterator.getAttributes();
                if(attrs!=null&&attrs.containsKey(field)){
                    int start=iterator.getRunStart(field);
                    int end=iterator.getRunLimit(field);
                    if(start!=-1&&end!=-1&&start<=max&&
                            end<=max){
                        ftf.select(start,end);
                    }
                    return true;
                }
            }while(iterator.next()!=CharacterIterator.DONE);
            return false;
        }

        private int getCalendarField(JSpinner spinner){
            JComponent editor=spinner.getEditor();
            if(editor instanceof JSpinner.DateEditor){
                JSpinner.DateEditor dateEditor=(JSpinner.DateEditor)editor;
                JFormattedTextField ftf=dateEditor.getTextField();
                int start=ftf.getSelectionStart();
                JFormattedTextField.AbstractFormatter formatter=
                        ftf.getFormatter();
                if(formatter instanceof InternationalFormatter){
                    Format.Field[] fields=((InternationalFormatter)
                            formatter).getFields(start);
                    for(int counter=0;counter<fields.length;counter++){
                        if(fields[counter] instanceof DateFormat.Field){
                            int calendarField;
                            if(fields[counter]==DateFormat.Field.HOUR1){
                                calendarField=Calendar.HOUR;
                            }else{
                                calendarField=((DateFormat.Field)
                                        fields[counter]).getCalendarField();
                            }
                            if(calendarField!=-1){
                                return calendarField;
                            }
                        }
                    }
                }
            }
            return -1;
        }

        public void mouseClicked(MouseEvent e){
        }

        public void mousePressed(MouseEvent e){
            if(SwingUtilities.isLeftMouseButton(e)&&e.getComponent().isEnabled()){
                spinner=eventToSpinner(e);
                autoRepeatTimer.start();
                focusSpinnerIfNecessary();
            }
        }

        public void mouseReleased(MouseEvent e){
            autoRepeatTimer.stop();
            arrowButton=null;
            spinner=null;
        }

        public void mouseEntered(MouseEvent e){
            if(spinner!=null&&!autoRepeatTimer.isRunning()&&spinner==eventToSpinner(e)){
                autoRepeatTimer.start();
            }
        }

        public void mouseExited(MouseEvent e){
            if(autoRepeatTimer.isRunning()){
                autoRepeatTimer.stop();
            }
        }

        private void focusSpinnerIfNecessary(){
            Component fo=KeyboardFocusManager.
                    getCurrentKeyboardFocusManager().getFocusOwner();
            if(spinner.isRequestFocusEnabled()&&(
                    fo==null||
                            !SwingUtilities.isDescendingFrom(fo,spinner))){
                Container root=spinner;
                if(!root.isFocusCycleRoot()){
                    root=root.getFocusCycleRootAncestor();
                }
                if(root!=null){
                    FocusTraversalPolicy ftp=root.getFocusTraversalPolicy();
                    Component child=ftp.getComponentAfter(root,spinner);
                    if(child!=null&&SwingUtilities.isDescendingFrom(
                            child,spinner)){
                        child.requestFocus();
                    }
                }
            }
        }

        public void focusGained(FocusEvent e){
        }

        public void focusLost(FocusEvent e){
            if(spinner==eventToSpinner(e)){
                if(autoRepeatTimer.isRunning()){
                    autoRepeatTimer.stop();
                }
                spinner=null;
                if(arrowButton!=null){
                    ButtonModel model=arrowButton.getModel();
                    model.setPressed(false);
                    model.setArmed(false);
                    arrowButton=null;
                }
            }
        }
    }

    private static class Handler implements LayoutManager,
            PropertyChangeListener, ChangeListener{
        //
        // LayoutManager
        //
        private Component nextButton=null;
        private Component previousButton=null;
        private Component editor=null;

        public void addLayoutComponent(String name,Component c){
            if("Next".equals(name)){
                nextButton=c;
            }else if("Previous".equals(name)){
                previousButton=c;
            }else if("Editor".equals(name)){
                editor=c;
            }
        }

        public void removeLayoutComponent(Component c){
            if(c==nextButton){
                nextButton=null;
            }else if(c==previousButton){
                previousButton=null;
            }else if(c==editor){
                editor=null;
            }
        }

        public Dimension preferredLayoutSize(Container parent){
            Dimension nextD=preferredSize(nextButton);
            Dimension previousD=preferredSize(previousButton);
            Dimension editorD=preferredSize(editor);
            /** Force the editors height to be a multiple of 2
             */
            editorD.height=((editorD.height+1)/2)*2;
            Dimension size=new Dimension(editorD.width,editorD.height);
            size.width+=Math.max(nextD.width,previousD.width);
            Insets insets=parent.getInsets();
            size.width+=insets.left+insets.right;
            size.height+=insets.top+insets.bottom;
            return size;
        }

        private Dimension preferredSize(Component c){
            return (c==null)?zeroSize:c.getPreferredSize();
        }

        public Dimension minimumLayoutSize(Container parent){
            return preferredLayoutSize(parent);
        }

        public void layoutContainer(Container parent){
            int width=parent.getWidth();
            int height=parent.getHeight();
            Insets insets=parent.getInsets();
            if(nextButton==null&&previousButton==null){
                setBounds(editor,insets.left,insets.top,width-insets.left-insets.right,
                        height-insets.top-insets.bottom);
                return;
            }
            Dimension nextD=preferredSize(nextButton);
            Dimension previousD=preferredSize(previousButton);
            int buttonsWidth=Math.max(nextD.width,previousD.width);
            int editorHeight=height-(insets.top+insets.bottom);
            // The arrowButtonInsets value is used instead of the JSpinner's
            // insets if not null. Defining this to be (0, 0, 0, 0) causes the
            // buttons to be aligned with the outer edge of the spinner's
            // border, and leaving it as "null" places the buttons completely
            // inside the spinner's border.
            Insets buttonInsets=UIManager.getInsets("Spinner.arrowButtonInsets");
            if(buttonInsets==null){
                buttonInsets=insets;
            }
            /** Deal with the spinner's componentOrientation property.
             */
            int editorX, editorWidth, buttonsX;
            if(parent.getComponentOrientation().isLeftToRight()){
                editorX=insets.left;
                editorWidth=width-insets.left-buttonsWidth-buttonInsets.right;
                buttonsX=width-buttonsWidth-buttonInsets.right;
            }else{
                buttonsX=buttonInsets.left;
                editorX=buttonsX+buttonsWidth;
                editorWidth=width-buttonInsets.left-buttonsWidth-insets.right;
            }
            int nextY=buttonInsets.top;
            int nextHeight=(height/2)+(height%2)-nextY;
            int previousY=buttonInsets.top+nextHeight;
            int previousHeight=height-previousY-buttonInsets.bottom;
            setBounds(editor,editorX,insets.top,editorWidth,editorHeight);
            setBounds(nextButton,buttonsX,nextY,buttonsWidth,nextHeight);
            setBounds(previousButton,buttonsX,previousY,buttonsWidth,previousHeight);
        }

        private void setBounds(Component c,int x,int y,int width,int height){
            if(c!=null){
                c.setBounds(x,y,width,height);
            }
        }

        //
        // PropertyChangeListener
        //
        public void propertyChange(PropertyChangeEvent e){
            String propertyName=e.getPropertyName();
            if(e.getSource() instanceof JSpinner){
                JSpinner spinner=(JSpinner)(e.getSource());
                SpinnerUI spinnerUI=spinner.getUI();
                if(spinnerUI instanceof BasicSpinnerUI){
                    BasicSpinnerUI ui=(BasicSpinnerUI)spinnerUI;
                    if("editor".equals(propertyName)){
                        JComponent oldEditor=(JComponent)e.getOldValue();
                        JComponent newEditor=(JComponent)e.getNewValue();
                        ui.replaceEditor(oldEditor,newEditor);
                        ui.updateEnabledState();
                        if(oldEditor instanceof JSpinner.DefaultEditor){
                            JTextField tf=
                                    ((JSpinner.DefaultEditor)oldEditor).getTextField();
                            if(tf!=null){
                                tf.removeFocusListener(nextButtonHandler);
                                tf.removeFocusListener(previousButtonHandler);
                            }
                        }
                        if(newEditor instanceof JSpinner.DefaultEditor){
                            JTextField tf=
                                    ((JSpinner.DefaultEditor)newEditor).getTextField();
                            if(tf!=null){
                                if(tf.getFont() instanceof UIResource){
                                    tf.setFont(spinner.getFont());
                                }
                                tf.addFocusListener(nextButtonHandler);
                                tf.addFocusListener(previousButtonHandler);
                            }
                        }
                    }else if("enabled".equals(propertyName)||
                            "model".equals(propertyName)){
                        ui.updateEnabledState();
                    }else if("font".equals(propertyName)){
                        JComponent editor=spinner.getEditor();
                        if(editor!=null&&editor instanceof JSpinner.DefaultEditor){
                            JTextField tf=
                                    ((JSpinner.DefaultEditor)editor).getTextField();
                            if(tf!=null){
                                if(tf.getFont() instanceof UIResource){
                                    tf.setFont(spinner.getFont());
                                }
                            }
                        }
                    }else if(JComponent.TOOL_TIP_TEXT_KEY.equals(propertyName)){
                        updateToolTipTextForChildren(spinner);
                    }
                }
            }else if(e.getSource() instanceof JComponent){
                JComponent c=(JComponent)e.getSource();
                if((c.getParent() instanceof JPanel)&&
                        (c.getParent().getParent() instanceof JSpinner)&&
                        "border".equals(propertyName)){
                    JSpinner spinner=(JSpinner)c.getParent().getParent();
                    SpinnerUI spinnerUI=spinner.getUI();
                    if(spinnerUI instanceof BasicSpinnerUI){
                        BasicSpinnerUI ui=(BasicSpinnerUI)spinnerUI;
                        ui.maybeRemoveEditorBorder(c);
                    }
                }
            }
        }

        // Syncronizes the ToolTip text for the components within the spinner
        // to be the same value as the spinner ToolTip text.
        private void updateToolTipTextForChildren(JComponent spinner){
            String toolTipText=spinner.getToolTipText();
            Component[] children=spinner.getComponents();
            for(int i=0;i<children.length;i++){
                if(children[i] instanceof JSpinner.DefaultEditor){
                    JTextField tf=((JSpinner.DefaultEditor)children[i]).getTextField();
                    if(tf!=null){
                        tf.setToolTipText(toolTipText);
                    }
                }else if(children[i] instanceof JComponent){
                    ((JComponent)children[i]).setToolTipText(spinner.getToolTipText());
                }
            }
        }

        public void stateChanged(ChangeEvent e){
            if(e.getSource() instanceof JSpinner){
                JSpinner spinner=(JSpinner)e.getSource();
                SpinnerUI spinnerUI=spinner.getUI();
                if(DefaultLookup.getBoolean(spinner,spinnerUI,
                        "Spinner.disableOnBoundaryValues",false)&&
                        spinnerUI instanceof BasicSpinnerUI){
                    BasicSpinnerUI ui=(BasicSpinnerUI)spinnerUI;
                    ui.updateEnabledState();
                }
            }
        }
    }
}
