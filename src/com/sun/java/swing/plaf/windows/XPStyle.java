/**
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>These classes are designed to be used while the
 * corresponding <code>LookAndFeel</code> class has been installed
 * (<code>UIManager.setLookAndFeel(new <i>XXX</i>LookAndFeel())</code>).
 * Using them while a different <code>LookAndFeel</code> is installed
 * may produce unexpected results, including exceptions.
 * Additionally, changing the <code>LookAndFeel</code>
 * maintained by the <code>UIManager</code> without updating the
 * corresponding <code>ComponentUI</code> of any
 * <code>JComponent</code>s may also produce unexpected results,
 * such as the wrong colors showing up, and is generally not
 * encouraged.
 */
/**
 * <p>These classes are designed to be used while the
 * corresponding <code>LookAndFeel</code> class has been installed
 * (<code>UIManager.setLookAndFeel(new <i>XXX</i>LookAndFeel())</code>).
 * Using them while a different <code>LookAndFeel</code> is installed
 * may produce unexpected results, including exceptions.
 * Additionally, changing the <code>LookAndFeel</code>
 * maintained by the <code>UIManager</code> without updating the
 * corresponding <code>ComponentUI</code> of any
 * <code>JComponent</code>s may also produce unexpected results,
 * such as the wrong colors showing up, and is generally not
 * encouraged.
 *
 */
package com.sun.java.swing.plaf.windows;

import sun.awt.image.SunWritableRaster;
import sun.awt.windows.ThemeReader;
import sun.security.action.GetPropertyAction;
import sun.swing.CachedPainter;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.security.AccessController;
import java.util.HashMap;

import static com.sun.java.swing.plaf.windows.TMSchema.*;

class XPStyle{
    // Singleton instance of this class
    private static XPStyle xp;
    // Singleton instance of SkinPainter
    private static SkinPainter skinPainter=new SkinPainter();
    private static Boolean themeActive=null;

    static{
        invalidateStyle();
    }

    private HashMap<String,Border> borderMap;
    private HashMap<String,Color> colorMap;
    private boolean flatMenus;

    // Private constructor
    private XPStyle(){
        flatMenus=getSysBoolean(Prop.FLATMENUS);
        colorMap=new HashMap<String,Color>();
        borderMap=new HashMap<String,Border>();
        // Note: All further access to the maps must be synchronized
    }

    private static boolean getSysBoolean(Prop prop){
        // We can use any widget name here, I guess.
        return ThemeReader.getSysBoolean("window",prop.getValue());
    }

    static synchronized void invalidateStyle(){
        xp=null;
        themeActive=null;
        skinPainter.flush();
    }

    static boolean isVista(){
        XPStyle xp=XPStyle.getXP();
        return (xp!=null&&xp.isSkinDefined(null,Part.CP_DROPDOWNBUTTONRIGHT));
    }

    static synchronized XPStyle getXP(){
        if(themeActive==null){
            Toolkit toolkit=Toolkit.getDefaultToolkit();
            themeActive=
                    (Boolean)toolkit.getDesktopProperty("win.xpstyle.themeActive");
            if(themeActive==null){
                themeActive=Boolean.FALSE;
            }
            if(themeActive.booleanValue()){
                GetPropertyAction propertyAction=
                        new GetPropertyAction("swing.noxp");
                if(AccessController.doPrivileged(propertyAction)==null&&
                        ThemeReader.isThemed()&&
                        !(UIManager.getLookAndFeel()
                                instanceof WindowsClassicLookAndFeel)){
                    xp=new XPStyle();
                }
            }
        }
        return ThemeReader.isXPStyleEnabled()?xp:null;
    }

    static Dimension getPartSize(Part part,State state){
        return ThemeReader.getPartSize(part.getControlName(null),part.getValue(),
                State.getValue(part,state));
    }

    String getString(Component c,Part part,State state,Prop prop){
        return getTypeEnumName(c,part,state,prop);
    }

    private static String getTypeEnumName(Component c,Part part,State state,Prop prop){
        int enumValue=ThemeReader.getEnum(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
        if(enumValue==-1){
            return null;
        }
        return TypeEnum.getTypeEnum(prop,enumValue).getName();
    }

    TypeEnum getTypeEnum(Component c,Part part,State state,Prop prop){
        int enumValue=ThemeReader.getEnum(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
        return TypeEnum.getTypeEnum(prop,enumValue);
    }

    Dimension getDimension(Component c,Part part,State state,Prop prop){
        Dimension d=ThemeReader.getPosition(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
        return (d!=null)?d:new Dimension();
    }

    Point getPoint(Component c,Part part,State state,Prop prop){
        Dimension d=ThemeReader.getPosition(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
        return (d!=null)?new Point(d.width,d.height):new Point();
    }

    Color getColor(Component c,Part part,State state,Prop prop,Color fallback){
        return getColor(new Skin(c,part,state),prop,fallback);
    }

    synchronized Color getColor(Skin skin,Prop prop,Color fallback){
        String key=skin.toString()+"."+prop.name();
        Part part=skin.part;
        Color color=colorMap.get(key);
        if(color==null){
            color=ThemeReader.getColor(part.getControlName(null),part.getValue(),
                    State.getValue(part,skin.state),
                    prop.getValue());
            if(color!=null){
                color=new ColorUIResource(color);
                colorMap.put(key,color);
            }
        }
        return (color!=null)?color:fallback;
    }

    synchronized Border getBorder(Component c,Part part){
        if(part==Part.MENU){
            // Special case because XP has no skin for menus
            if(flatMenus){
                // TODO: The classic border uses this color, but we should
                // create a new UI property called "PopupMenu.borderColor"
                // instead.
                return new XPFillBorder(UIManager.getColor("InternalFrame.borderShadow"),
                        1);
            }else{
                return null;    // Will cause L&F to use classic border
            }
        }
        Skin skin=new Skin(c,part,null);
        Border border=borderMap.get(skin.string);
        if(border==null){
            String bgType=getTypeEnumName(c,part,null,Prop.BGTYPE);
            if("borderfill".equalsIgnoreCase(bgType)){
                int thickness=getInt(c,part,null,Prop.BORDERSIZE,1);
                Color color=getColor(skin,Prop.BORDERCOLOR,Color.black);
                border=new XPFillBorder(color,thickness);
                if(part==Part.CP_COMBOBOX){
                    border=new XPStatefulFillBorder(color,thickness,part,Prop.BORDERCOLOR);
                }
            }else if("imagefile".equalsIgnoreCase(bgType)){
                Insets m=getMargin(c,part,null,Prop.SIZINGMARGINS);
                if(m!=null){
                    if(getBoolean(c,part,null,Prop.BORDERONLY)){
                        border=new XPImageBorder(c,part);
                    }else if(part==Part.CP_COMBOBOX){
                        border=new EmptyBorder(1,1,1,1);
                    }else{
                        if(part==Part.TP_BUTTON){
                            border=new XPEmptyBorder(new Insets(3,3,3,3));
                        }else{
                            border=new XPEmptyBorder(m);
                        }
                    }
                }
            }
            if(border!=null){
                borderMap.put(skin.string,border);
            }
        }
        return border;
    }

    int getInt(Component c,Part part,State state,Prop prop,int fallback){
        return ThemeReader.getInt(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
    }

    Insets getMargin(Component c,Part part,State state,Prop prop){
        Insets insets=ThemeReader.getThemeMargins(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
        return (insets!=null)?insets:new Insets(0,0,0,0);
    }

    private boolean getBoolean(Component c,Part part,State state,Prop prop){
        return ThemeReader.getBoolean(part.getControlName(c),part.getValue(),
                State.getValue(part,state),
                prop.getValue());
    }

    synchronized Skin getSkin(Component c,Part part){
        assert isSkinDefined(c,part):"part "+part+" is not defined";
        return new Skin(c,part,null);
    }

    boolean isSkinDefined(Component c,Part part){
        return (part.getValue()==0)
                ||ThemeReader.isThemePartDefined(
                part.getControlName(c),part.getValue(),0);
    }

    long getThemeTransitionDuration(Component c,Part part,State stateFrom,
                                    State stateTo,Prop prop){
        return ThemeReader.getThemeTransitionDuration(part.getControlName(c),
                part.getValue(),
                State.getValue(part,stateFrom),
                State.getValue(part,stateTo),
                (prop!=null)?prop.getValue():0);
    }

    static class Skin{
        final Component component;
        final Part part;
        final State state;
        private final String string;
        private Dimension size=null;

        Skin(Component component,Part part){
            this(component,part,null);
        }

        Skin(Component component,Part part,State state){
            this.component=component;
            this.part=part;
            this.state=state;
            String str=part.getControlName(component)+"."+part.name();
            if(state!=null){
                str+="("+state.name()+")";
            }
            string=str;
        }

        Skin(Part part,State state){
            this(null,part,state);
        }

        Insets getContentMargin(){
            /** idk: it seems margins are the same for all 'big enough'
             * bounding rectangles.
             */
            int boundingWidth=100;
            int boundingHeight=100;
            Insets insets=ThemeReader.getThemeBackgroundContentMargins(
                    part.getControlName(null),part.getValue(),
                    0,boundingWidth,boundingHeight);
            return (insets!=null)?insets:new Insets(0,0,0,0);
        }

        int getWidth(){
            return getWidth((state!=null)?state:State.NORMAL);
        }

        private int getWidth(State state){
            if(size==null){
                size=getPartSize(part,state);
            }
            return (size!=null)?size.width:0;
        }

        int getHeight(){
            return getHeight((state!=null)?state:State.NORMAL);
        }

        private int getHeight(State state){
            if(size==null){
                size=getPartSize(part,state);
            }
            return (size!=null)?size.height:0;
        }

        public int hashCode(){
            return string.hashCode();
        }

        public boolean equals(Object obj){
            return (obj instanceof Skin&&((Skin)obj).string.equals(string));
        }

        public String toString(){
            return string;
        }

        void paintSkin(Graphics g,int dx,int dy,State state){
            if(state==null){
                state=this.state;
            }
            paintSkin(g,dx,dy,getWidth(state),getHeight(state),state);
        }

        void paintSkin(Graphics g,int dx,int dy,int dw,int dh,State state){
            if(XPStyle.getXP()==null){
                return;
            }
            if(ThemeReader.isGetThemeTransitionDurationDefined()
                    &&component instanceof JComponent
                    &&SwingUtilities.getAncestorOfClass(CellRendererPane.class,
                    component)==null){
                AnimationController.paintSkin((JComponent)component,this,
                        g,dx,dy,dw,dh,state);
            }else{
                paintSkinRaw(g,dx,dy,dw,dh,state);
            }
        }

        void paintSkinRaw(Graphics g,int dx,int dy,int dw,int dh,State state){
            if(XPStyle.getXP()==null){
                return;
            }
            skinPainter.paint(null,g,dx,dy,dw,dh,this,state);
        }

        void paintSkin(Graphics g,Rectangle r,State state){
            paintSkin(g,r.x,r.y,r.width,r.height,state);
        }

        void paintSkin(Graphics g,int dx,int dy,int dw,int dh,State state,
                       boolean borderFill){
            if(XPStyle.getXP()==null){
                return;
            }
            if(borderFill&&"borderfill".equals(getTypeEnumName(component,part,
                    state,Prop.BGTYPE))){
                return;
            }
            skinPainter.paint(null,g,dx,dy,dw,dh,this,state);
        }
    }

    private static class SkinPainter extends CachedPainter{
        SkinPainter(){
            super(30);
            flush();
        }

        protected void paintToImage(Component c,Image image,Graphics g,
                                    int w,int h,Object[] args){
            boolean accEnabled=false;
            Skin skin=(Skin)args[0];
            Part part=skin.part;
            State state=(State)args[1];
            if(state==null){
                state=skin.state;
            }
            if(c==null){
                c=skin.component;
            }
            BufferedImage bi=(BufferedImage)image;
            WritableRaster raster=bi.getRaster();
            DataBufferInt dbi=(DataBufferInt)raster.getDataBuffer();
            // Note that stealData() requires a markDirty() afterwards
            // since we modify the data in it.
            ThemeReader.paintBackground(SunWritableRaster.stealData(dbi,0),
                    part.getControlName(c),part.getValue(),
                    State.getValue(part,state),
                    0,0,w,h,w);
            SunWritableRaster.markDirty(dbi);
        }

        protected Image createImage(Component c,int w,int h,
                                    GraphicsConfiguration config,Object[] args){
            return new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        }

        public void flush(){
            super.flush();
        }
    }

    static class GlyphButton extends JButton{
        private Skin skin;

        public GlyphButton(Component parent,Part part){
            XPStyle xp=getXP();
            skin=xp!=null?xp.getSkin(parent,part):null;
            setBorder(null);
            setContentAreaFilled(false);
            setMinimumSize(new Dimension(5,5));
            setPreferredSize(new Dimension(16,16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        }

        public boolean isFocusTraversable(){
            return false;
        }

        public void paintComponent(Graphics g){
            if(XPStyle.getXP()==null||skin==null){
                return;
            }
            Dimension d=getSize();
            skin.paintSkin(g,0,0,d.width,d.height,getState());
        }

        protected State getState(){
            State state=State.NORMAL;
            if(!isEnabled()){
                state=State.DISABLED;
            }else if(getModel().isPressed()){
                state=State.PRESSED;
            }else if(getModel().isRollover()){
                state=State.HOT;
            }
            return state;
        }

        public void setPart(Component parent,Part part){
            XPStyle xp=getXP();
            skin=xp!=null?xp.getSkin(parent,part):null;
            revalidate();
            repaint();
        }

        protected void paintBorder(Graphics g){
        }
    }

    private class XPFillBorder extends LineBorder implements UIResource{
        XPFillBorder(Color color,int thickness){
            super(color,thickness);
        }

        public Insets getBorderInsets(Component c,Insets insets){
            Insets margin=null;
            //
            // Ideally we'd have an interface defined for classes which
            // support margins (to avoid this hackery), but we've
            // decided against it for simplicity
            //
            if(c instanceof AbstractButton){
                margin=((AbstractButton)c).getMargin();
            }else if(c instanceof JToolBar){
                margin=((JToolBar)c).getMargin();
            }else if(c instanceof JTextComponent){
                margin=((JTextComponent)c).getMargin();
            }
            insets.top=(margin!=null?margin.top:0)+thickness;
            insets.left=(margin!=null?margin.left:0)+thickness;
            insets.bottom=(margin!=null?margin.bottom:0)+thickness;
            insets.right=(margin!=null?margin.right:0)+thickness;
            return insets;
        }
    }

    private class XPStatefulFillBorder extends XPFillBorder{
        private final Part part;
        private final Prop prop;

        XPStatefulFillBorder(Color color,int thickness,Part part,Prop prop){
            super(color,thickness);
            this.part=part;
            this.prop=prop;
        }

        public void paintBorder(Component c,Graphics g,int x,int y,int width,int height){
            State state=State.NORMAL;
            // special casing for comboboxes.
            // there may be more special cases in the future
            if(c instanceof JComboBox){
                JComboBox cb=(JComboBox)c;
                // note. in the future this should be replaced with a call
                // to BasicLookAndFeel.getUIOfType()
                if(cb.getUI() instanceof WindowsComboBoxUI){
                    WindowsComboBoxUI wcb=(WindowsComboBoxUI)cb.getUI();
                    state=wcb.getXPComboBoxState(cb);
                }
            }
            lineColor=getColor(c,part,state,prop,Color.black);
            super.paintBorder(c,g,x,y,width,height);
        }
    }

    private class XPImageBorder extends AbstractBorder implements UIResource{
        Skin skin;

        XPImageBorder(Component c,Part part){
            this.skin=getSkin(c,part);
        }

        public void paintBorder(Component c,Graphics g,
                                int x,int y,int width,int height){
            skin.paintSkin(g,x,y,width,height,null);
        }

        public Insets getBorderInsets(Component c,Insets insets){
            Insets margin=null;
            Insets borderInsets=skin.getContentMargin();
            if(borderInsets==null){
                borderInsets=new Insets(0,0,0,0);
            }
            //
            // Ideally we'd have an interface defined for classes which
            // support margins (to avoid this hackery), but we've
            // decided against it for simplicity
            //
            if(c instanceof AbstractButton){
                margin=((AbstractButton)c).getMargin();
            }else if(c instanceof JToolBar){
                margin=((JToolBar)c).getMargin();
            }else if(c instanceof JTextComponent){
                margin=((JTextComponent)c).getMargin();
            }
            insets.top=(margin!=null?margin.top:0)+borderInsets.top;
            insets.left=(margin!=null?margin.left:0)+borderInsets.left;
            insets.bottom=(margin!=null?margin.bottom:0)+borderInsets.bottom;
            insets.right=(margin!=null?margin.right:0)+borderInsets.right;
            return insets;
        }
    }

    private class XPEmptyBorder extends EmptyBorder implements UIResource{
        XPEmptyBorder(Insets m){
            super(m.top+2,m.left+2,m.bottom+2,m.right+2);
        }

        public Insets getBorderInsets(Component c,Insets insets){
            insets=super.getBorderInsets(c,insets);
            Insets margin=null;
            if(c instanceof AbstractButton){
                Insets m=((AbstractButton)c).getMargin();
                // if this is a toolbar button then ignore getMargin()
                // and subtract the padding added by the constructor
                if(c.getParent() instanceof JToolBar
                        &&!(c instanceof JRadioButton)
                        &&!(c instanceof JCheckBox)
                        &&m instanceof InsetsUIResource){
                    insets.top-=2;
                    insets.left-=2;
                    insets.bottom-=2;
                    insets.right-=2;
                }else{
                    margin=m;
                }
            }else if(c instanceof JToolBar){
                margin=((JToolBar)c).getMargin();
            }else if(c instanceof JTextComponent){
                margin=((JTextComponent)c).getMargin();
            }
            if(margin!=null){
                insets.top=margin.top+2;
                insets.left=margin.left+2;
                insets.bottom=margin.bottom+2;
                insets.right=margin.right+2;
            }
            return insets;
        }
    }
}
