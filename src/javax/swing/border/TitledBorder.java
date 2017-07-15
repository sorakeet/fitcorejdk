/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.border;

import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import java.awt.*;
import java.awt.geom.Path2D;
import java.beans.ConstructorProperties;

@SuppressWarnings("serial")
public class TitledBorder extends AbstractBorder{
    static public final int DEFAULT_POSITION=0;
    static public final int ABOVE_TOP=1;
    static public final int TOP=2;
    static public final int BELOW_TOP=3;
    static public final int ABOVE_BOTTOM=4;
    static public final int BOTTOM=5;
    static public final int BELOW_BOTTOM=6;
    static public final int DEFAULT_JUSTIFICATION=0;
    static public final int LEFT=1;
    static public final int CENTER=2;
    static public final int RIGHT=3;
    static public final int LEADING=4;
    static public final int TRAILING=5;
    // Space between the border and the component's edge
    static protected final int EDGE_SPACING=2;
    // Space between the border and text
    static protected final int TEXT_SPACING=2;
    // Horizontal inset of text that is left or right justified
    static protected final int TEXT_INSET_H=5;
    private final JLabel label;
    protected String title;
    protected Border border;
    protected int titlePosition;
    protected int titleJustification;
    protected Font titleFont;
    protected Color titleColor;

    public TitledBorder(String title){
        this(null,title,LEADING,DEFAULT_POSITION,null,null);
    }

    @ConstructorProperties({"border","title","titleJustification","titlePosition","titleFont","titleColor"})
    public TitledBorder(Border border,
                        String title,
                        int titleJustification,
                        int titlePosition,
                        Font titleFont,
                        Color titleColor){
        this.title=title;
        this.border=border;
        this.titleFont=titleFont;
        this.titleColor=titleColor;
        setTitleJustification(titleJustification);
        setTitlePosition(titlePosition);
        this.label=new JLabel();
        this.label.setOpaque(false);
        this.label.putClientProperty(BasicHTML.propertyKey,null);
    }

    public TitledBorder(Border border){
        this(border,"",LEADING,DEFAULT_POSITION,null,null);
    }

    public TitledBorder(Border border,String title){
        this(border,title,LEADING,DEFAULT_POSITION,null,null);
    }

    public TitledBorder(Border border,
                        String title,
                        int titleJustification,
                        int titlePosition){
        this(border,title,titleJustification,
                titlePosition,null,null);
    }

    public TitledBorder(Border border,
                        String title,
                        int titleJustification,
                        int titlePosition,
                        Font titleFont){
        this(border,title,titleJustification,
                titlePosition,titleFont,null);
    }

    public void paintBorder(Component c,Graphics g,int x,int y,int width,int height){
        Border border=getBorder();
        String title=getTitle();
        if((title!=null)&&!title.isEmpty()){
            int edge=(border instanceof TitledBorder)?0:EDGE_SPACING;
            JLabel label=getLabel(c);
            Dimension size=label.getPreferredSize();
            Insets insets=getBorderInsets(border,c,new Insets(0,0,0,0));
            int borderX=x+edge;
            int borderY=y+edge;
            int borderW=width-edge-edge;
            int borderH=height-edge-edge;
            int labelY=y;
            int labelH=size.height;
            int position=getPosition();
            switch(position){
                case ABOVE_TOP:
                    insets.left=0;
                    insets.right=0;
                    borderY+=labelH-edge;
                    borderH-=labelH-edge;
                    break;
                case TOP:
                    insets.top=edge+insets.top/2-labelH/2;
                    if(insets.top<edge){
                        borderY-=insets.top;
                        borderH+=insets.top;
                    }else{
                        labelY+=insets.top;
                    }
                    break;
                case BELOW_TOP:
                    labelY+=insets.top+edge;
                    break;
                case ABOVE_BOTTOM:
                    labelY+=height-labelH-insets.bottom-edge;
                    break;
                case BOTTOM:
                    labelY+=height-labelH;
                    insets.bottom=edge+(insets.bottom-labelH)/2;
                    if(insets.bottom<edge){
                        borderH+=insets.bottom;
                    }else{
                        labelY-=insets.bottom;
                    }
                    break;
                case BELOW_BOTTOM:
                    insets.left=0;
                    insets.right=0;
                    labelY+=height-labelH;
                    borderH-=labelH-edge;
                    break;
            }
            insets.left+=edge+TEXT_INSET_H;
            insets.right+=edge+TEXT_INSET_H;
            int labelX=x;
            int labelW=width-insets.left-insets.right;
            if(labelW>size.width){
                labelW=size.width;
            }
            switch(getJustification(c)){
                case LEFT:
                    labelX+=insets.left;
                    break;
                case RIGHT:
                    labelX+=width-insets.right-labelW;
                    break;
                case CENTER:
                    labelX+=(width-labelW)/2;
                    break;
            }
            if(border!=null){
                if((position!=TOP)&&(position!=BOTTOM)){
                    border.paintBorder(c,g,borderX,borderY,borderW,borderH);
                }else{
                    Graphics g2=g.create();
                    if(g2 instanceof Graphics2D){
                        Graphics2D g2d=(Graphics2D)g2;
                        Path2D path=new Path2D.Float();
                        path.append(new Rectangle(borderX,borderY,borderW,labelY-borderY),false);
                        path.append(new Rectangle(borderX,labelY,labelX-borderX-TEXT_SPACING,labelH),false);
                        path.append(new Rectangle(labelX+labelW+TEXT_SPACING,labelY,borderX-labelX+borderW-labelW-TEXT_SPACING,labelH),false);
                        path.append(new Rectangle(borderX,labelY+labelH,borderW,borderY-labelY+borderH-labelH),false);
                        g2d.clip(path);
                    }
                    border.paintBorder(c,g2,borderX,borderY,borderW,borderH);
                    g2.dispose();
                }
            }
            g.translate(labelX,labelY);
            label.setSize(labelW,labelH);
            label.paint(g);
            g.translate(-labelX,-labelY);
        }else if(border!=null){
            border.paintBorder(c,g,x,y,width,height);
        }
    }

    public Insets getBorderInsets(Component c,Insets insets){
        Border border=getBorder();
        insets=getBorderInsets(border,c,insets);
        String title=getTitle();
        if((title!=null)&&!title.isEmpty()){
            int edge=(border instanceof TitledBorder)?0:EDGE_SPACING;
            JLabel label=getLabel(c);
            Dimension size=label.getPreferredSize();
            switch(getPosition()){
                case ABOVE_TOP:
                    insets.top+=size.height-edge;
                    break;
                case TOP:{
                    if(insets.top<size.height){
                        insets.top=size.height-edge;
                    }
                    break;
                }
                case BELOW_TOP:
                    insets.top+=size.height;
                    break;
                case ABOVE_BOTTOM:
                    insets.bottom+=size.height;
                    break;
                case BOTTOM:{
                    if(insets.bottom<size.height){
                        insets.bottom=size.height-edge;
                    }
                    break;
                }
                case BELOW_BOTTOM:
                    insets.bottom+=size.height-edge;
                    break;
            }
            insets.top+=edge+TEXT_SPACING;
            insets.left+=edge+TEXT_SPACING;
            insets.right+=edge+TEXT_SPACING;
            insets.bottom+=edge+TEXT_SPACING;
        }
        return insets;
    }

    public boolean isBorderOpaque(){
        return false;
    }

    public int getBaseline(Component c,int width,int height){
        if(c==null){
            throw new NullPointerException("Must supply non-null component");
        }
        if(width<0){
            throw new IllegalArgumentException("Width must be >= 0");
        }
        if(height<0){
            throw new IllegalArgumentException("Height must be >= 0");
        }
        Border border=getBorder();
        String title=getTitle();
        if((title!=null)&&!title.isEmpty()){
            int edge=(border instanceof TitledBorder)?0:EDGE_SPACING;
            JLabel label=getLabel(c);
            Dimension size=label.getPreferredSize();
            Insets insets=getBorderInsets(border,c,new Insets(0,0,0,0));
            int baseline=label.getBaseline(size.width,size.height);
            switch(getPosition()){
                case ABOVE_TOP:
                    return baseline;
                case TOP:
                    insets.top=edge+(insets.top-size.height)/2;
                    return (insets.top<edge)
                            ?baseline
                            :baseline+insets.top;
                case BELOW_TOP:
                    return baseline+insets.top+edge;
                case ABOVE_BOTTOM:
                    return baseline+height-size.height-insets.bottom-edge;
                case BOTTOM:
                    insets.bottom=edge+(insets.bottom-size.height)/2;
                    return (insets.bottom<edge)
                            ?baseline+height-size.height
                            :baseline+height-size.height+insets.bottom;
                case BELOW_BOTTOM:
                    return baseline+height-size.height;
            }
        }
        return -1;
    }

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            Component c){
        super.getBaselineResizeBehavior(c);
        switch(getPosition()){
            case TitledBorder.ABOVE_TOP:
            case TitledBorder.TOP:
            case TitledBorder.BELOW_TOP:
                return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
            case TitledBorder.ABOVE_BOTTOM:
            case TitledBorder.BOTTOM:
            case TitledBorder.BELOW_BOTTOM:
                return JComponent.BaselineResizeBehavior.CONSTANT_DESCENT;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public Border getBorder(){
        return border!=null
                ?border
                :UIManager.getBorder("TitledBorder.border");
    }

    public void setBorder(Border border){
        this.border=border;
    }
    // REMIND(aim): remove all or some of these set methods?

    private int getPosition(){
        int position=getTitlePosition();
        if(position!=DEFAULT_POSITION){
            return position;
        }
        Object value=UIManager.get("TitledBorder.position");
        if(value instanceof Integer){
            int i=(Integer)value;
            if((0<i)&&(i<=6)){
                return i;
            }
        }else if(value instanceof String){
            String s=(String)value;
            if(s.equalsIgnoreCase("ABOVE_TOP")){
                return ABOVE_TOP;
            }
            if(s.equalsIgnoreCase("TOP")){
                return TOP;
            }
            if(s.equalsIgnoreCase("BELOW_TOP")){
                return BELOW_TOP;
            }
            if(s.equalsIgnoreCase("ABOVE_BOTTOM")){
                return ABOVE_BOTTOM;
            }
            if(s.equalsIgnoreCase("BOTTOM")){
                return BOTTOM;
            }
            if(s.equalsIgnoreCase("BELOW_BOTTOM")){
                return BELOW_BOTTOM;
            }
        }
        return TOP;
    }

    public int getTitlePosition(){
        return titlePosition;
    }

    public void setTitlePosition(int titlePosition){
        switch(titlePosition){
            case ABOVE_TOP:
            case TOP:
            case BELOW_TOP:
            case ABOVE_BOTTOM:
            case BOTTOM:
            case BELOW_BOTTOM:
            case DEFAULT_POSITION:
                this.titlePosition=titlePosition;
                break;
            default:
                throw new IllegalArgumentException(titlePosition+
                        " is not a valid title position.");
        }
    }

    private int getJustification(Component c){
        int justification=getTitleJustification();
        if((justification==LEADING)||(justification==DEFAULT_JUSTIFICATION)){
            return c.getComponentOrientation().isLeftToRight()?LEFT:RIGHT;
        }
        if(justification==TRAILING){
            return c.getComponentOrientation().isLeftToRight()?RIGHT:LEFT;
        }
        return justification;
    }

    public int getTitleJustification(){
        return titleJustification;
    }

    public void setTitleJustification(int titleJustification){
        switch(titleJustification){
            case DEFAULT_JUSTIFICATION:
            case LEFT:
            case CENTER:
            case RIGHT:
            case LEADING:
            case TRAILING:
                this.titleJustification=titleJustification;
                break;
            default:
                throw new IllegalArgumentException(titleJustification+
                        " is not a valid title justification.");
        }
    }

    private JLabel getLabel(Component c){
        this.label.setText(getTitle());
        this.label.setFont(getFont(c));
        this.label.setForeground(getColor(c));
        this.label.setComponentOrientation(c.getComponentOrientation());
        this.label.setEnabled(c.isEnabled());
        return this.label;
    }

    protected Font getFont(Component c){
        Font font=getTitleFont();
        if(font!=null){
            return font;
        }
        if(c!=null){
            font=c.getFont();
            if(font!=null){
                return font;
            }
        }
        return new Font(Font.DIALOG,Font.PLAIN,12);
    }

    public Font getTitleFont(){
        return titleFont==null?UIManager.getFont("TitledBorder.font"):titleFont;
    }

    public void setTitleFont(Font titleFont){
        this.titleFont=titleFont;
    }

    private Color getColor(Component c){
        Color color=getTitleColor();
        if(color!=null){
            return color;
        }
        return (c!=null)
                ?c.getForeground()
                :null;
    }

    public Color getTitleColor(){
        return titleColor==null?UIManager.getColor("TitledBorder.titleColor"):titleColor;
    }

    public void setTitleColor(Color titleColor){
        this.titleColor=titleColor;
    }

    private static Insets getBorderInsets(Border border,Component c,Insets insets){
        if(border==null){
            insets.set(0,0,0,0);
        }else if(border instanceof AbstractBorder){
            AbstractBorder ab=(AbstractBorder)border;
            insets=ab.getBorderInsets(c,insets);
        }else{
            Insets i=border.getBorderInsets(c);
            insets.set(i.top,i.left,i.bottom,i.right);
        }
        return insets;
    }

    public Dimension getMinimumSize(Component c){
        Insets insets=getBorderInsets(c);
        Dimension minSize=new Dimension(insets.right+insets.left,
                insets.top+insets.bottom);
        String title=getTitle();
        if((title!=null)&&!title.isEmpty()){
            JLabel label=getLabel(c);
            Dimension size=label.getPreferredSize();
            int position=getPosition();
            if((position!=ABOVE_TOP)&&(position!=BELOW_BOTTOM)){
                minSize.width+=size.width;
            }else if(minSize.width<size.width){
                minSize.width+=size.width;
            }
        }
        return minSize;
    }
}
