/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

public class StyledEditorKit extends DefaultEditorKit{
    private static final ViewFactory defaultFactory=new StyledViewFactory();
    // --- Action implementations ---------------------------------
    private static final Action[] defaultActions={
            new FontFamilyAction("font-family-SansSerif","SansSerif"),
            new FontFamilyAction("font-family-Monospaced","Monospaced"),
            new FontFamilyAction("font-family-Serif","Serif"),
            new FontSizeAction("font-size-8",8),
            new FontSizeAction("font-size-10",10),
            new FontSizeAction("font-size-12",12),
            new FontSizeAction("font-size-14",14),
            new FontSizeAction("font-size-16",16),
            new FontSizeAction("font-size-18",18),
            new FontSizeAction("font-size-24",24),
            new FontSizeAction("font-size-36",36),
            new FontSizeAction("font-size-48",48),
            new AlignmentAction("left-justify",StyleConstants.ALIGN_LEFT),
            new AlignmentAction("center-justify",StyleConstants.ALIGN_CENTER),
            new AlignmentAction("right-justify",StyleConstants.ALIGN_RIGHT),
            new BoldAction(),
            new ItalicAction(),
            new StyledInsertBreakAction(),
            new UnderlineAction()
    };
    Element currentRun;
    // --- EditorKit methods ---------------------------
    Element currentParagraph;
    MutableAttributeSet inputAttributes;
    private AttributeTracker inputAttributeUpdater;

    public StyledEditorKit(){
        createInputAttributeUpdated();
        createInputAttributes();
    }

    private void createInputAttributes(){
        inputAttributes=new SimpleAttributeSet(){
            public AttributeSet getResolveParent(){
                return (currentParagraph!=null)?
                        currentParagraph.getAttributes():null;
            }

            public Object clone(){
                return new SimpleAttributeSet(this);
            }
        };
    }

    private void createInputAttributeUpdated(){
        inputAttributeUpdater=new AttributeTracker();
    }

    public Element getCharacterAttributeRun(){
        return currentRun;
    }

    public ViewFactory getViewFactory(){
        return defaultFactory;
    }

    public Action[] getActions(){
        return TextAction.augmentList(super.getActions(),this.defaultActions);
    }

    public Document createDefaultDocument(){
        return new DefaultStyledDocument();
    }

    public MutableAttributeSet getInputAttributes(){
        return inputAttributes;
    }

    public Object clone(){
        StyledEditorKit o=(StyledEditorKit)super.clone();
        o.currentRun=o.currentParagraph=null;
        o.createInputAttributeUpdated();
        o.createInputAttributes();
        return o;
    }

    public void install(JEditorPane c){
        c.addCaretListener(inputAttributeUpdater);
        c.addPropertyChangeListener(inputAttributeUpdater);
        Caret caret=c.getCaret();
        if(caret!=null){
            inputAttributeUpdater.updateInputAttributes
                    (caret.getDot(),caret.getMark(),c);
        }
    }

    public void deinstall(JEditorPane c){
        c.removeCaretListener(inputAttributeUpdater);
        c.removePropertyChangeListener(inputAttributeUpdater);
        // remove references to current document so it can be collected.
        currentRun=null;
        currentParagraph=null;
    }

    protected void createInputAttributes(Element element,
                                         MutableAttributeSet set){
        if(element.getAttributes().getAttributeCount()>0
                ||element.getEndOffset()-element.getStartOffset()>1
                ||element.getEndOffset()<element.getDocument().getLength()){
            set.removeAttributes(set);
            set.addAttributes(element.getAttributes());
            set.removeAttribute(StyleConstants.ComponentAttribute);
            set.removeAttribute(StyleConstants.IconAttribute);
            set.removeAttribute(AbstractDocument.ElementNameAttribute);
            set.removeAttribute(StyleConstants.ComposedTextAttribute);
        }
    }
    // ---- default ViewFactory implementation ---------------------

    static class StyledViewFactory implements ViewFactory{
        public View create(Element elem){
            String kind=elem.getName();
            if(kind!=null){
                if(kind.equals(AbstractDocument.ContentElementName)){
                    return new LabelView(elem);
                }else if(kind.equals(AbstractDocument.ParagraphElementName)){
                    return new ParagraphView(elem);
                }else if(kind.equals(AbstractDocument.SectionElementName)){
                    return new BoxView(elem,View.Y_AXIS);
                }else if(kind.equals(StyleConstants.ComponentElementName)){
                    return new ComponentView(elem);
                }else if(kind.equals(StyleConstants.IconElementName)){
                    return new IconView(elem);
                }
            }
            // default to text display
            return new LabelView(elem);
        }
    }

    public abstract static class StyledTextAction extends TextAction{
        public StyledTextAction(String nm){
            super(nm);
        }

        protected final JEditorPane getEditor(ActionEvent e){
            JTextComponent tcomp=getTextComponent(e);
            if(tcomp instanceof JEditorPane){
                return (JEditorPane)tcomp;
            }
            return null;
        }

        protected final void setCharacterAttributes(JEditorPane editor,
                                                    AttributeSet attr,boolean replace){
            int p0=editor.getSelectionStart();
            int p1=editor.getSelectionEnd();
            if(p0!=p1){
                StyledDocument doc=getStyledDocument(editor);
                doc.setCharacterAttributes(p0,p1-p0,attr,replace);
            }
            StyledEditorKit k=getStyledEditorKit(editor);
            MutableAttributeSet inputAttributes=k.getInputAttributes();
            if(replace){
                inputAttributes.removeAttributes(inputAttributes);
            }
            inputAttributes.addAttributes(attr);
        }

        protected final StyledDocument getStyledDocument(JEditorPane e){
            Document d=e.getDocument();
            if(d instanceof StyledDocument){
                return (StyledDocument)d;
            }
            throw new IllegalArgumentException("document must be StyledDocument");
        }

        protected final StyledEditorKit getStyledEditorKit(JEditorPane e){
            EditorKit k=e.getEditorKit();
            if(k instanceof StyledEditorKit){
                return (StyledEditorKit)k;
            }
            throw new IllegalArgumentException("EditorKit must be StyledEditorKit");
        }

        protected final void setParagraphAttributes(JEditorPane editor,
                                                    AttributeSet attr,boolean replace){
            int p0=editor.getSelectionStart();
            int p1=editor.getSelectionEnd();
            StyledDocument doc=getStyledDocument(editor);
            doc.setParagraphAttributes(p0,p1-p0,attr,replace);
        }
    }

    public static class FontFamilyAction extends StyledTextAction{
        private String family;

        public FontFamilyAction(String nm,String family){
            super(nm);
            this.family=family;
        }

        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                String family=this.family;
                if((e!=null)&&(e.getSource()==editor)){
                    String s=e.getActionCommand();
                    if(s!=null){
                        family=s;
                    }
                }
                if(family!=null){
                    MutableAttributeSet attr=new SimpleAttributeSet();
                    StyleConstants.setFontFamily(attr,family);
                    setCharacterAttributes(editor,attr,false);
                }else{
                    UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }
    }

    public static class FontSizeAction extends StyledTextAction{
        private int size;

        public FontSizeAction(String nm,int size){
            super(nm);
            this.size=size;
        }        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                int size=this.size;
                if((e!=null)&&(e.getSource()==editor)){
                    String s=e.getActionCommand();
                    try{
                        size=Integer.parseInt(s,10);
                    }catch(NumberFormatException nfe){
                    }
                }
                if(size!=0){
                    MutableAttributeSet attr=new SimpleAttributeSet();
                    StyleConstants.setFontSize(attr,size);
                    setCharacterAttributes(editor,attr,false);
                }else{
                    UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }


    }

    public static class ForegroundAction extends StyledTextAction{
        private Color fg;

        public ForegroundAction(String nm,Color fg){
            super(nm);
            this.fg=fg;
        }        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                Color fg=this.fg;
                if((e!=null)&&(e.getSource()==editor)){
                    String s=e.getActionCommand();
                    try{
                        fg=Color.decode(s);
                    }catch(NumberFormatException nfe){
                    }
                }
                if(fg!=null){
                    MutableAttributeSet attr=new SimpleAttributeSet();
                    StyleConstants.setForeground(attr,fg);
                    setCharacterAttributes(editor,attr,false);
                }else{
                    UIManager.getLookAndFeel().provideErrorFeedback(editor);
                }
            }
        }


    }

    public static class AlignmentAction extends StyledTextAction{
        private int a;

        public AlignmentAction(String nm,int a){
            super(nm);
            this.a=a;
        }        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                int a=this.a;
                if((e!=null)&&(e.getSource()==editor)){
                    String s=e.getActionCommand();
                    try{
                        a=Integer.parseInt(s,10);
                    }catch(NumberFormatException nfe){
                    }
                }
                MutableAttributeSet attr=new SimpleAttributeSet();
                StyleConstants.setAlignment(attr,a);
                setParagraphAttributes(editor,attr,false);
            }
        }


    }

    public static class BoldAction extends StyledTextAction{
        public BoldAction(){
            super("font-bold");
        }

        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                StyledEditorKit kit=getStyledEditorKit(editor);
                MutableAttributeSet attr=kit.getInputAttributes();
                boolean bold=(StyleConstants.isBold(attr))?false:true;
                SimpleAttributeSet sas=new SimpleAttributeSet();
                StyleConstants.setBold(sas,bold);
                setCharacterAttributes(editor,sas,false);
            }
        }
    }

    public static class ItalicAction extends StyledTextAction{
        public ItalicAction(){
            super("font-italic");
        }

        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                StyledEditorKit kit=getStyledEditorKit(editor);
                MutableAttributeSet attr=kit.getInputAttributes();
                boolean italic=(StyleConstants.isItalic(attr))?false:true;
                SimpleAttributeSet sas=new SimpleAttributeSet();
                StyleConstants.setItalic(sas,italic);
                setCharacterAttributes(editor,sas,false);
            }
        }
    }

    public static class UnderlineAction extends StyledTextAction{
        public UnderlineAction(){
            super("font-underline");
        }

        public void actionPerformed(ActionEvent e){
            JEditorPane editor=getEditor(e);
            if(editor!=null){
                StyledEditorKit kit=getStyledEditorKit(editor);
                MutableAttributeSet attr=kit.getInputAttributes();
                boolean underline=(StyleConstants.isUnderline(attr))?false:true;
                SimpleAttributeSet sas=new SimpleAttributeSet();
                StyleConstants.setUnderline(sas,underline);
                setCharacterAttributes(editor,sas,false);
            }
        }
    }

    static class StyledInsertBreakAction extends StyledTextAction{
        private SimpleAttributeSet tempSet;

        StyledInsertBreakAction(){
            super(insertBreakAction);
        }

        public void actionPerformed(ActionEvent e){
            JEditorPane target=getEditor(e);
            if(target!=null){
                if((!target.isEditable())||(!target.isEnabled())){
                    UIManager.getLookAndFeel().provideErrorFeedback(target);
                    return;
                }
                StyledEditorKit sek=getStyledEditorKit(target);
                if(tempSet!=null){
                    tempSet.removeAttributes(tempSet);
                }else{
                    tempSet=new SimpleAttributeSet();
                }
                tempSet.addAttributes(sek.getInputAttributes());
                target.replaceSelection("\n");
                MutableAttributeSet ia=sek.getInputAttributes();
                ia.removeAttributes(ia);
                ia.addAttributes(tempSet);
                tempSet.removeAttributes(tempSet);
            }else{
                // See if we are in a JTextComponent.
                JTextComponent text=getTextComponent(e);
                if(text!=null){
                    if((!text.isEditable())||(!text.isEnabled())){
                        UIManager.getLookAndFeel().provideErrorFeedback(target);
                        return;
                    }
                    text.replaceSelection("\n");
                }
            }
        }
    }

    class AttributeTracker implements CaretListener, PropertyChangeListener, Serializable{
        public void propertyChange(PropertyChangeEvent evt){
            Object newValue=evt.getNewValue();
            Object source=evt.getSource();
            if((source instanceof JTextComponent)&&
                    (newValue instanceof Document)){
                // New document will have changed selection to 0,0.
                updateInputAttributes(0,0,(JTextComponent)source);
            }
        }

        void updateInputAttributes(int dot,int mark,JTextComponent c){
            // EditorKit might not have installed the StyledDocument yet.
            Document aDoc=c.getDocument();
            if(!(aDoc instanceof StyledDocument)){
                return;
            }
            int start=Math.min(dot,mark);
            // record current character attributes.
            StyledDocument doc=(StyledDocument)aDoc;
            // If nothing is selected, get the attributes from the character
            // before the start of the selection, otherwise get the attributes
            // from the character element at the start of the selection.
            Element run;
            currentParagraph=doc.getParagraphElement(start);
            if(currentParagraph.getStartOffset()==start||dot!=mark){
                // Get the attributes from the character at the selection
                // if in a different paragrah!
                run=doc.getCharacterElement(start);
            }else{
                run=doc.getCharacterElement(Math.max(start-1,0));
            }
            if(run!=currentRun){
                /**
                 * PENDING(prinz) All attributes that represent a single
                 * glyph position and can't be inserted into should be
                 * removed from the input attributes... this requires
                 * mixing in an interface to indicate that condition.
                 * When we can add things again this logic needs to be
                 * improved!!
                 */
                currentRun=run;
                createInputAttributes(currentRun,getInputAttributes());
            }
        }

        public void caretUpdate(CaretEvent e){
            updateInputAttributes(e.getDot(),e.getMark(),
                    (JTextComponent)e.getSource());
        }
    }
}
