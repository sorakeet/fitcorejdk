/**
 * Copyright (c) 2000, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputMethodEvent;
import java.awt.im.InputContext;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.*;
import java.util.Date;

public class JFormattedTextField extends JTextField{
    public static final int COMMIT=0;
    public static final int COMMIT_OR_REVERT=1;
    public static final int REVERT=2;
    public static final int PERSIST=3;
    private static final String uiClassID="FormattedTextFieldUI";
    private static final Action[] defaultActions=
            {new CommitAction(),new CancelAction()};
    private AbstractFormatterFactory factory;
    private AbstractFormatter format;
    private Object value;
    private boolean editValid;
    private int focusLostBehavior;
    private boolean edited;
    private DocumentListener documentListener;
    private Object mask;
    private ActionMap textFormatterActionMap;
    private boolean composedTextExists=false;
    private FocusLostHandler focusLostHandler;

    public JFormattedTextField(Object value){
        this();
        setValue(value);
    }

    public JFormattedTextField(Format format){
        this();
        setFormatterFactory(getDefaultFormatterFactory(format));
    }

    public JFormattedTextField(){
        super();
        enableEvents(AWTEvent.FOCUS_EVENT_MASK);
        setFocusLostBehavior(COMMIT_OR_REVERT);
    }

    private AbstractFormatterFactory getDefaultFormatterFactory(Object type){
        if(type instanceof DateFormat){
            return new DefaultFormatterFactory(new DateFormatter
                    ((DateFormat)type));
        }
        if(type instanceof NumberFormat){
            return new DefaultFormatterFactory(new NumberFormatter(
                    (NumberFormat)type));
        }
        if(type instanceof Format){
            return new DefaultFormatterFactory(new InternationalFormatter(
                    (Format)type));
        }
        if(type instanceof Date){
            return new DefaultFormatterFactory(new DateFormatter());
        }
        if(type instanceof Number){
            AbstractFormatter displayFormatter=new NumberFormatter();
            ((NumberFormatter)displayFormatter).setValueClass(type.getClass());
            AbstractFormatter editFormatter=new NumberFormatter(
                    new DecimalFormat("#.#"));
            ((NumberFormatter)editFormatter).setValueClass(type.getClass());
            return new DefaultFormatterFactory(displayFormatter,
                    displayFormatter,editFormatter);
        }
        return new DefaultFormatterFactory(new DefaultFormatter());
    }

    public JFormattedTextField(AbstractFormatter formatter){
        this(new DefaultFormatterFactory(formatter));
    }

    public JFormattedTextField(AbstractFormatterFactory factory){
        this();
        setFormatterFactory(factory);
    }

    public JFormattedTextField(AbstractFormatterFactory factory,
                               Object currentValue){
        this(currentValue);
        setFormatterFactory(factory);
    }

    public int getFocusLostBehavior(){
        return focusLostBehavior;
    }

    public void setFocusLostBehavior(int behavior){
        if(behavior!=COMMIT&&behavior!=COMMIT_OR_REVERT&&
                behavior!=PERSIST&&behavior!=REVERT){
            throw new IllegalArgumentException("setFocusLostBehavior must be one of: JFormattedTextField.COMMIT, JFormattedTextField.COMMIT_OR_REVERT, JFormattedTextField.PERSIST or JFormattedTextField.REVERT");
        }
        focusLostBehavior=behavior;
    }    public void setFormatterFactory(AbstractFormatterFactory tf){
        AbstractFormatterFactory oldFactory=factory;
        factory=tf;
        firePropertyChange("formatterFactory",oldFactory,tf);
        setValue(getValue(),true,false);
    }

    public void commitEdit() throws ParseException{
        AbstractFormatter format=getFormatter();
        if(format!=null){
            setValue(format.stringToValue(getText()),false,true);
        }
    }    public AbstractFormatterFactory getFormatterFactory(){
        return factory;
    }

    public AbstractFormatter getFormatter(){
        return format;
    }

    protected void setFormatter(AbstractFormatter format){
        AbstractFormatter oldFormat=this.format;
        if(oldFormat!=null){
            oldFormat.uninstall();
        }
        setEditValid(true);
        this.format=format;
        if(format!=null){
            format.install(this);
        }
        setEdited(false);
        firePropertyChange("textFormatter",oldFormat,format);
    }

    public boolean isEditValid(){
        return editValid;
    }    public void setValue(Object value){
        if(value!=null&&getFormatterFactory()==null){
            setFormatterFactory(getDefaultFormatterFactory(value));
        }
        setValue(value,true,true);
    }

    private void setEditValid(boolean isValid){
        if(isValid!=editValid){
            editValid=isValid;
            firePropertyChange("editValid",Boolean.valueOf(!isValid),
                    Boolean.valueOf(isValid));
        }
    }    public Object getValue(){
        return value;
    }

    protected void invalidEdit(){
        UIManager.getLookAndFeel().provideErrorFeedback(JFormattedTextField.this);
    }

    protected void processInputMethodEvent(InputMethodEvent e){
        AttributedCharacterIterator text=e.getText();
        int commitCount=e.getCommittedCharacterCount();
        // Keep track of the composed text
        if(text!=null){
            int begin=text.getBeginIndex();
            int end=text.getEndIndex();
            composedTextExists=((end-begin)>commitCount);
        }else{
            composedTextExists=false;
        }
        super.processInputMethodEvent(e);
    }

    protected void processFocusEvent(FocusEvent e){
        super.processFocusEvent(e);
        // ignore temporary focus event
        if(e.isTemporary()){
            return;
        }
        if(isEdited()&&e.getID()==FocusEvent.FOCUS_LOST){
            InputContext ic=getInputContext();
            if(focusLostHandler==null){
                focusLostHandler=new FocusLostHandler();
            }
            // if there is a composed text, process it first
            if((ic!=null)&&composedTextExists){
                ic.endComposition();
                EventQueue.invokeLater(focusLostHandler);
            }else{
                focusLostHandler.run();
            }
        }else if(!isEdited()){
            // reformat
            setValue(getValue(),true,true);
        }
    }

    private boolean isEdited(){
        return edited;
    }

    private void setEdited(boolean edited){
        this.edited=edited;
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }

    public String getUIClassID(){
        return uiClassID;
    }

    public void setDocument(Document doc){
        if(documentListener!=null&&getDocument()!=null){
            getDocument().removeDocumentListener(documentListener);
        }
        super.setDocument(doc);
        if(documentListener==null){
            documentListener=new DocumentHandler();
        }
        doc.addDocumentListener(documentListener);
    }

    public Action[] getActions(){
        return TextAction.augmentList(super.getActions(),defaultActions);
    }

    private void setFormatterActions(Action[] actions){
        if(actions==null){
            if(textFormatterActionMap!=null){
                textFormatterActionMap.clear();
            }
        }else{
            if(textFormatterActionMap==null){
                ActionMap map=getActionMap();
                textFormatterActionMap=new ActionMap();
                while(map!=null){
                    ActionMap parent=map.getParent();
                    if(parent instanceof UIResource||parent==null){
                        map.setParent(textFormatterActionMap);
                        textFormatterActionMap.setParent(parent);
                        break;
                    }
                    map=parent;
                }
            }
            for(int counter=actions.length-1;counter>=0;
                counter--){
                Object key=actions[counter].getValue(Action.NAME);
                if(key!=null){
                    textFormatterActionMap.put(key,actions[counter]);
                }
            }
        }
    }

    public static abstract class AbstractFormatterFactory{
        public abstract AbstractFormatter getFormatter(JFormattedTextField tf);
    }

    public static abstract class AbstractFormatter implements Serializable{
        private JFormattedTextField ftf;

        public void install(JFormattedTextField ftf){
            if(this.ftf!=null){
                uninstall();
            }
            this.ftf=ftf;
            if(ftf!=null){
                try{
                    ftf.setText(valueToString(ftf.getValue()));
                }catch(ParseException pe){
                    ftf.setText("");
                    setEditValid(false);
                }
                installDocumentFilter(getDocumentFilter());
                ftf.setNavigationFilter(getNavigationFilter());
                ftf.setFormatterActions(getActions());
            }
        }

        public void uninstall(){
            if(this.ftf!=null){
                installDocumentFilter(null);
                this.ftf.setNavigationFilter(null);
                this.ftf.setFormatterActions(null);
            }
        }

        public abstract Object stringToValue(String text) throws
                ParseException;

        public abstract String valueToString(Object value) throws
                ParseException;

        protected void invalidEdit(){
            JFormattedTextField ftf=getFormattedTextField();
            if(ftf!=null){
                ftf.invalidEdit();
            }
        }

        protected JFormattedTextField getFormattedTextField(){
            return ftf;
        }

        protected void setEditValid(boolean valid){
            JFormattedTextField ftf=getFormattedTextField();
            if(ftf!=null){
                ftf.setEditValid(valid);
            }
        }

        protected Action[] getActions(){
            return null;
        }

        protected DocumentFilter getDocumentFilter(){
            return null;
        }

        protected NavigationFilter getNavigationFilter(){
            return null;
        }

        protected Object clone() throws CloneNotSupportedException{
            AbstractFormatter formatter=(AbstractFormatter)super.clone();
            formatter.ftf=null;
            return formatter;
        }

        private void installDocumentFilter(DocumentFilter filter){
            JFormattedTextField ftf=getFormattedTextField();
            if(ftf!=null){
                Document doc=ftf.getDocument();
                if(doc instanceof AbstractDocument){
                    ((AbstractDocument)doc).setDocumentFilter(filter);
                }
                doc.putProperty(DocumentFilter.class,null);
            }
        }
    }

    static class CommitAction extends NotifyAction{
        public void actionPerformed(ActionEvent e){
            JTextComponent target=getFocusedComponent();
            if(target instanceof JFormattedTextField){
                // Attempt to commit the value
                try{
                    ((JFormattedTextField)target).commitEdit();
                }catch(ParseException pe){
                    ((JFormattedTextField)target).invalidEdit();
                    // value not committed, don't notify ActionListeners
                    return;
                }
            }
            // Super behavior.
            super.actionPerformed(e);
        }

        public boolean isEnabled(){
            JTextComponent target=getFocusedComponent();
            if(target instanceof JFormattedTextField){
                JFormattedTextField ftf=(JFormattedTextField)target;
                if(!ftf.isEdited()){
                    return false;
                }
                return true;
            }
            return super.isEnabled();
        }
    }    private void setValue(Object value,boolean createFormat,boolean firePC){
        Object oldValue=this.value;
        this.value=value;
        if(createFormat){
            AbstractFormatterFactory factory=getFormatterFactory();
            AbstractFormatter atf;
            if(factory!=null){
                atf=factory.getFormatter(this);
            }else{
                atf=null;
            }
            setFormatter(atf);
        }else{
            // Assumed to be valid
            setEditValid(true);
        }
        setEdited(false);
        if(firePC){
            firePropertyChange("value",oldValue,value);
        }
    }

    private static class CancelAction extends TextAction{
        public CancelAction(){
            super("reset-field-edit");
        }

        public void actionPerformed(ActionEvent e){
            JTextComponent target=getFocusedComponent();
            if(target instanceof JFormattedTextField){
                JFormattedTextField ftf=(JFormattedTextField)target;
                ftf.setValue(ftf.getValue());
            }
        }

        public boolean isEnabled(){
            JTextComponent target=getFocusedComponent();
            if(target instanceof JFormattedTextField){
                JFormattedTextField ftf=(JFormattedTextField)target;
                if(!ftf.isEdited()){
                    return false;
                }
                return true;
            }
            return super.isEnabled();
        }
    }

    private class FocusLostHandler implements Runnable, Serializable{
        public void run(){
            int fb=JFormattedTextField.this.getFocusLostBehavior();
            if(fb==JFormattedTextField.COMMIT||
                    fb==JFormattedTextField.COMMIT_OR_REVERT){
                try{
                    JFormattedTextField.this.commitEdit();
                    // Give it a chance to reformat.
                    JFormattedTextField.this.setValue(
                            JFormattedTextField.this.getValue(),true,true);
                }catch(ParseException pe){
                    if(fb==JFormattedTextField.this.COMMIT_OR_REVERT){
                        JFormattedTextField.this.setValue(
                                JFormattedTextField.this.getValue(),true,true);
                    }
                }
            }else if(fb==JFormattedTextField.REVERT){
                JFormattedTextField.this.setValue(
                        JFormattedTextField.this.getValue(),true,true);
            }
        }
    }

    private class DocumentHandler implements DocumentListener, Serializable{
        public void insertUpdate(DocumentEvent e){
            setEdited(true);
        }

        public void removeUpdate(DocumentEvent e){
            setEdited(true);
        }

        public void changedUpdate(DocumentEvent e){
        }
    }










}
