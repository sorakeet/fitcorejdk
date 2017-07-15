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

public class BasicTextFieldUI extends BasicTextUI{
    public BasicTextFieldUI(){
        super();
    }

    public static ComponentUI createUI(JComponent c){
        return new BasicTextFieldUI();
    }

    protected String getPropertyPrefix(){
        return "TextField";
    }

    public View create(Element elem){
        Document doc=elem.getDocument();
        Object i18nFlag=doc.getProperty("i18n"/**AbstractDocument.I18NProperty*/);
        if(Boolean.TRUE.equals(i18nFlag)){
            // To support bidirectional text, we build a more heavyweight
            // representation of the field.
            String kind=elem.getName();
            if(kind!=null){
                if(kind.equals(AbstractDocument.ContentElementName)){
                    return new GlyphView(elem);
                }else if(kind.equals(AbstractDocument.ParagraphElementName)){
                    return new I18nFieldView(elem);
                }
            }
            // this shouldn't happen, should probably throw in this case.
        }
        return new FieldView(elem);
    }

    public int getBaseline(JComponent c,int width,int height){
        super.getBaseline(c,width,height);
        View rootView=getRootView((JTextComponent)c);
        if(rootView.getViewCount()>0){
            Insets insets=c.getInsets();
            height=height-insets.top-insets.bottom;
            if(height>0){
                int baseline=insets.top;
                View fieldView=rootView.getView(0);
                int vspan=(int)fieldView.getPreferredSpan(View.Y_AXIS);
                if(height!=vspan){
                    int slop=height-vspan;
                    baseline+=slop/2;
                }
                if(fieldView instanceof I18nFieldView){
                    int fieldBaseline=BasicHTML.getBaseline(
                            fieldView,width-insets.left-insets.right,
                            height);
                    if(fieldBaseline<0){
                        return -1;
                    }
                    baseline+=fieldBaseline;
                }else{
                    FontMetrics fm=c.getFontMetrics(c.getFont());
                    baseline+=fm.getAscent();
                }
                return baseline;
            }
        }
        return -1;
    }

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c){
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CENTER_OFFSET;
    }

    static class I18nFieldView extends ParagraphView{
        I18nFieldView(Element elem){
            super(elem);
        }

        protected void setJustification(int j){
            // Justification is done in adjustAllocation(), so disable
            // ParagraphView's justification handling by doing nothing here.
        }

        public int getFlowSpan(int index){
            return Integer.MAX_VALUE;
        }

        public void paint(Graphics g,Shape a){
            Rectangle r=(Rectangle)a;
            g.clipRect(r.x,r.y,r.width,r.height);
            super.paint(g,adjustAllocation(a));
        }

        Shape adjustAllocation(Shape a){
            if(a!=null){
                Rectangle bounds=a.getBounds();
                int vspan=(int)getPreferredSpan(Y_AXIS);
                int hspan=(int)getPreferredSpan(X_AXIS);
                if(bounds.height!=vspan){
                    int slop=bounds.height-vspan;
                    bounds.y+=slop/2;
                    bounds.height-=slop;
                }
                // horizontal adjustments
                Component c=getContainer();
                if(c instanceof JTextField){
                    JTextField field=(JTextField)c;
                    BoundedRangeModel vis=field.getHorizontalVisibility();
                    int max=Math.max(hspan,bounds.width);
                    int value=vis.getValue();
                    int extent=Math.min(max,bounds.width-1);
                    if((value+extent)>max){
                        value=max-extent;
                    }
                    vis.setRangeProperties(value,extent,vis.getMinimum(),
                            max,false);
                    if(hspan<bounds.width){
                        // horizontally align the interior
                        int slop=bounds.width-1-hspan;
                        int align=((JTextField)c).getHorizontalAlignment();
                        if(isLeftToRight(c)){
                            if(align==LEADING){
                                align=LEFT;
                            }else if(align==TRAILING){
                                align=RIGHT;
                            }
                        }else{
                            if(align==LEADING){
                                align=RIGHT;
                            }else if(align==TRAILING){
                                align=LEFT;
                            }
                        }
                        switch(align){
                            case SwingConstants.CENTER:
                                bounds.x+=slop/2;
                                bounds.width-=slop;
                                break;
                            case SwingConstants.RIGHT:
                                bounds.x+=slop;
                                bounds.width-=slop;
                                break;
                        }
                    }else{
                        // adjust the allocation to match the bounded range.
                        bounds.width=hspan;
                        bounds.x-=vis.getValue();
                    }
                }
                return bounds;
            }
            return null;
        }

        static boolean isLeftToRight(Component c){
            return c.getComponentOrientation().isLeftToRight();
        }
        // --- View methods -------------------------------------------

        public int getResizeWeight(int axis){
            if(axis==View.X_AXIS){
                return 1;
            }
            return 0;
        }

        public Shape modelToView(int pos,Shape a,Position.Bias b) throws BadLocationException{
            return super.modelToView(pos,adjustAllocation(a),b);
        }

        public int viewToModel(float fx,float fy,Shape a,Position.Bias[] bias){
            return super.viewToModel(fx,fy,adjustAllocation(a),bias);
        }

        public Shape modelToView(int p0,Position.Bias b0,
                                 int p1,Position.Bias b1,Shape a)
                throws BadLocationException{
            return super.modelToView(p0,b0,p1,b1,adjustAllocation(a));
        }

        public void insertUpdate(DocumentEvent changes,Shape a,ViewFactory f){
            super.insertUpdate(changes,adjustAllocation(a),f);
            updateVisibilityModel();
        }

        void updateVisibilityModel(){
            Component c=getContainer();
            if(c instanceof JTextField){
                JTextField field=(JTextField)c;
                BoundedRangeModel vis=field.getHorizontalVisibility();
                int hspan=(int)getPreferredSpan(X_AXIS);
                int extent=vis.getExtent();
                int maximum=Math.max(hspan,extent);
                extent=(extent==0)?maximum:extent;
                int value=maximum-extent;
                int oldValue=vis.getValue();
                if((oldValue+extent)>maximum){
                    oldValue=maximum-extent;
                }
                value=Math.max(0,Math.min(value,oldValue));
                vis.setRangeProperties(value,extent,0,maximum,false);
            }
        }

        public void removeUpdate(DocumentEvent changes,Shape a,ViewFactory f){
            super.removeUpdate(changes,adjustAllocation(a),f);
            updateVisibilityModel();
        }
    }
}
