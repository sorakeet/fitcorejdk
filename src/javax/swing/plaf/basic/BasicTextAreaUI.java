/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;

public class BasicTextAreaUI extends BasicTextUI{
    public BasicTextAreaUI(){
        super();
    }

    public static ComponentUI createUI(JComponent ta){
        return new BasicTextAreaUI();
    }

    protected void propertyChange(PropertyChangeEvent evt){
        super.propertyChange(evt);
        if(evt.getPropertyName().equals("lineWrap")||
                evt.getPropertyName().equals("wrapStyleWord")||
                evt.getPropertyName().equals("tabSize")){
            // rebuild the view
            modelChanged();
        }else if("editable".equals(evt.getPropertyName())){
            updateFocusTraversalKeys();
        }
    }

    protected String getPropertyPrefix(){
        return "TextArea";
    }

    protected void installDefaults(){
        super.installDefaults();
        //the fix for 4785160 is undone
    }

    public Dimension getPreferredSize(JComponent c){
        return super.getPreferredSize(c);
        //the fix for 4785160 is undone
    }

    public Dimension getMinimumSize(JComponent c){
        return super.getMinimumSize(c);
        //the fix for 4785160 is undone
    }

    public View create(Element elem){
        Document doc=elem.getDocument();
        Object i18nFlag=doc.getProperty("i18n"/**AbstractDocument.I18NProperty*/);
        if((i18nFlag!=null)&&i18nFlag.equals(Boolean.TRUE)){
            // build a view that support bidi
            return createI18N(elem);
        }else{
            JTextComponent c=getComponent();
            if(c instanceof JTextArea){
                JTextArea area=(JTextArea)c;
                View v;
                if(area.getLineWrap()){
                    v=new WrappedPlainView(elem,area.getWrapStyleWord());
                }else{
                    v=new PlainView(elem);
                }
                return v;
            }
        }
        return null;
    }

    View createI18N(Element elem){
        String kind=elem.getName();
        if(kind!=null){
            if(kind.equals(AbstractDocument.ContentElementName)){
                return new PlainParagraph(elem);
            }else if(kind.equals(AbstractDocument.ParagraphElementName)){
                return new BoxView(elem,View.Y_AXIS);
            }
        }
        return null;
    }

    public int getBaseline(JComponent c,int width,int height){
        super.getBaseline(c,width,height);
        Object i18nFlag=((JTextComponent)c).getDocument().
                getProperty("i18n");
        Insets insets=c.getInsets();
        if(Boolean.TRUE.equals(i18nFlag)){
            View rootView=getRootView((JTextComponent)c);
            if(rootView.getViewCount()>0){
                height=height-insets.top-insets.bottom;
                int baseline=insets.top;
                int fieldBaseline=BasicHTML.getBaseline(
                        rootView.getView(0),width-insets.left-
                                insets.right,height);
                if(fieldBaseline<0){
                    return -1;
                }
                return baseline+fieldBaseline;
            }
            return -1;
        }
        FontMetrics fm=c.getFontMetrics(c.getFont());
        return insets.top+fm.getAscent();
    }

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c){
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    static class PlainParagraph extends ParagraphView{
        PlainParagraph(Element elem){
            super(elem);
            layoutPool=new LogicalView(elem);
            layoutPool.setParent(this);
        }

        public void setParent(View parent){
            super.setParent(parent);
            if(parent!=null){
                setPropertiesFromAttributes();
            }
        }

        protected void setPropertiesFromAttributes(){
            Component c=getContainer();
            if((c!=null)&&(!c.getComponentOrientation().isLeftToRight())){
                setJustification(StyleConstants.ALIGN_RIGHT);
            }else{
                setJustification(StyleConstants.ALIGN_LEFT);
            }
        }

        public int getFlowSpan(int index){
            Component c=getContainer();
            if(c instanceof JTextArea){
                JTextArea area=(JTextArea)c;
                if(!area.getLineWrap()){
                    // no limit if unwrapped
                    return Integer.MAX_VALUE;
                }
            }
            return super.getFlowSpan(index);
        }

        protected SizeRequirements calculateMinorAxisRequirements(int axis,
                                                                  SizeRequirements r){
            SizeRequirements req=super.calculateMinorAxisRequirements(axis,r);
            Component c=getContainer();
            if(c instanceof JTextArea){
                JTextArea area=(JTextArea)c;
                if(!area.getLineWrap()){
                    // min is pref if unwrapped
                    req.minimum=req.preferred;
                }else{
                    req.minimum=0;
                    req.preferred=getWidth();
                    if(req.preferred==Integer.MAX_VALUE){
                        // We have been initially set to MAX_VALUE, but we
                        // don't want this as our preferred.
                        req.preferred=100;
                    }
                }
            }
            return req;
        }

        public void setSize(float width,float height){
            if((int)width!=getWidth()){
                preferenceChanged(null,true,true);
            }
            super.setSize(width,height);
        }

        static class LogicalView extends CompositeView{
            LogicalView(Element elem){
                super(elem);
            }

            protected void loadChildren(ViewFactory f){
                Element elem=getElement();
                if(elem.getElementCount()>0){
                    super.loadChildren(f);
                }else{
                    View v=new GlyphView(elem);
                    append(v);
                }
            }

            protected boolean isBefore(int x,int y,Rectangle alloc){
                return false;
            }

            protected boolean isAfter(int x,int y,Rectangle alloc){
                return false;
            }

            protected View getViewAtPoint(int x,int y,Rectangle alloc){
                return null;
            }

            protected void childAllocation(int index,Rectangle a){
            }
            // The following methods don't do anything useful, they
            // simply keep the class from being abstract.

            protected int getViewIndexAtPosition(int pos){
                Element elem=getElement();
                if(elem.getElementCount()>0){
                    return elem.getElementIndex(pos);
                }
                return 0;
            }

            public float getPreferredSpan(int axis){
                if(getViewCount()!=1)
                    throw new Error("One child view is assumed.");
                View v=getView(0);
                return v.getPreferredSpan(axis);
            }

            public void paint(Graphics g,Shape allocation){
            }

            protected boolean updateChildren(DocumentEvent.ElementChange ec,
                                             DocumentEvent e,ViewFactory f){
                return false;
            }

            protected void forwardUpdateToView(View v,DocumentEvent e,
                                               Shape a,ViewFactory f){
                v.setParent(this);
                super.forwardUpdateToView(v,e,a,f);
            }
        }
    }
}
