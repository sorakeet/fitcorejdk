/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AWTAccessor;
import sun.security.action.GetBooleanAction;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.RootPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.security.AccessController;

/// PENDING(klobad) Who should be opaque in this component?
@SuppressWarnings("serial")
public class JRootPane extends JComponent implements Accessible{
    public static final int NONE=0;
    public static final int FRAME=1;
    public static final int PLAIN_DIALOG=2;
    public static final int INFORMATION_DIALOG=3;
    public static final int ERROR_DIALOG=4;
    public static final int COLOR_CHOOSER_DIALOG=5;
    public static final int FILE_CHOOSER_DIALOG=6;
    public static final int QUESTION_DIALOG=7;
    public static final int WARNING_DIALOG=8;
    private static final String uiClassID="RootPaneUI";
    private static final boolean LOG_DISABLE_TRUE_DOUBLE_BUFFERING;
    private static final boolean IGNORE_DISABLE_TRUE_DOUBLE_BUFFERING;

    static{
        LOG_DISABLE_TRUE_DOUBLE_BUFFERING=
                AccessController.doPrivileged(new GetBooleanAction(
                        "swing.logDoubleBufferingDisable"));
        IGNORE_DISABLE_TRUE_DOUBLE_BUFFERING=
                AccessController.doPrivileged(new GetBooleanAction(
                        "swing.ignoreDoubleBufferingDisable"));
    }

    protected JMenuBar menuBar;
    protected Container contentPane;
    protected JLayeredPane layeredPane;
    protected Component glassPane;
    protected JButton defaultButton;
    @Deprecated
    protected DefaultAction defaultPressAction;
    @Deprecated
    protected DefaultAction defaultReleaseAction;
    boolean useTrueDoubleBuffering=true;
    private int windowDecorationStyle;

    public JRootPane(){
        setGlassPane(createGlassPane());
        setLayeredPane(createLayeredPane());
        setContentPane(createContentPane());
        setLayout(createRootLayout());
        setDoubleBuffered(true);
        updateUI();
    }

    public void updateUI(){
        setUI((RootPaneUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }    public int getWindowDecorationStyle(){
        return windowDecorationStyle;
    }

    public void addNotify(){
        super.addNotify();
        enableEvents(AWTEvent.KEY_EVENT_MASK);
    }    public void setWindowDecorationStyle(int windowDecorationStyle){
        if(windowDecorationStyle<0||
                windowDecorationStyle>WARNING_DIALOG){
            throw new IllegalArgumentException("Invalid decoration style");
        }
        int oldWindowDecorationStyle=getWindowDecorationStyle();
        this.windowDecorationStyle=windowDecorationStyle;
        firePropertyChange("windowDecorationStyle",
                oldWindowDecorationStyle,
                windowDecorationStyle);
    }

    public void removeNotify(){
        super.removeNotify();
    }

    @Override
    public boolean isValidateRoot(){
        return true;
    }

    public boolean isOptimizedDrawingEnabled(){
        return !glassPane.isVisible();
    }

    public void setDoubleBuffered(boolean aFlag){
        if(isDoubleBuffered()!=aFlag){
            super.setDoubleBuffered(aFlag);
            RepaintManager.currentManager(this).doubleBufferingChanged(this);
        }
    }

    protected String paramString(){
        return super.paramString();
    }

    protected JLayeredPane createLayeredPane(){
        JLayeredPane p=new JLayeredPane();
        p.setName(this.getName()+".layeredPane");
        return p;
    }

    protected Container createContentPane(){
        JComponent c=new JPanel();
        c.setName(this.getName()+".contentPane");
        c.setLayout(new BorderLayout(){
            /** This BorderLayout subclass maps a null constraint to CENTER.
             * Although the reference BorderLayout also does this, some VMs
             * throw an IllegalArgumentException.
             */
            public void addLayoutComponent(Component comp,Object constraints){
                if(constraints==null){
                    constraints=BorderLayout.CENTER;
                }
                super.addLayoutComponent(comp,constraints);
            }
        });
        return c;
    }

    protected Component createGlassPane(){
        JComponent c=new JPanel();
        c.setName(this.getName()+".glassPane");
        c.setVisible(false);
        ((JPanel)c).setOpaque(false);
        return c;
    }

    protected LayoutManager createRootLayout(){
        return new RootLayout();
    }

    public RootPaneUI getUI(){
        return (RootPaneUI)ui;
    }

    public void setUI(RootPaneUI ui){
        super.setUI(ui);
    }

    public JMenuBar getJMenuBar(){
        return menuBar;
    }

    public void setJMenuBar(JMenuBar menu){
        if(menuBar!=null&&menuBar.getParent()==layeredPane)
            layeredPane.remove(menuBar);
        menuBar=menu;
        if(menuBar!=null)
            layeredPane.add(menuBar,JLayeredPane.FRAME_CONTENT_LAYER);
    }

    @Deprecated
    public JMenuBar getMenuBar(){
        return menuBar;
    }

    @Deprecated
    public void setMenuBar(JMenuBar menu){
        if(menuBar!=null&&menuBar.getParent()==layeredPane)
            layeredPane.remove(menuBar);
        menuBar=menu;
        if(menuBar!=null)
            layeredPane.add(menuBar,JLayeredPane.FRAME_CONTENT_LAYER);
    }

    public Container getContentPane(){
        return contentPane;
    }

    public void setContentPane(Container content){
        if(content==null)
            throw new IllegalComponentStateException("contentPane cannot be set to null.");
        if(contentPane!=null&&contentPane.getParent()==layeredPane)
            layeredPane.remove(contentPane);
        contentPane=content;
        layeredPane.add(contentPane,JLayeredPane.FRAME_CONTENT_LAYER);
    }

    public JLayeredPane getLayeredPane(){
        return layeredPane;
    }

    // PENDING(klobad) Should this reparent the contentPane and MenuBar?
    public void setLayeredPane(JLayeredPane layered){
        if(layered==null)
            throw new IllegalComponentStateException("layeredPane cannot be set to null.");
        if(layeredPane!=null&&layeredPane.getParent()==this)
            this.remove(layeredPane);
        layeredPane=layered;
        this.add(layeredPane,-1);
    }

    public Component getGlassPane(){
        return glassPane;
    }

    public void setGlassPane(Component glass){
        if(glass==null){
            throw new NullPointerException("glassPane cannot be set to null.");
        }
        AWTAccessor.getComponentAccessor().setMixingCutoutShape(glass,
                new Rectangle());
        boolean visible=false;
        if(glassPane!=null&&glassPane.getParent()==this){
            this.remove(glassPane);
            visible=glassPane.isVisible();
        }
        glass.setVisible(visible);
        glassPane=glass;
        this.add(glassPane,0);
        if(visible){
            repaint();
        }
    }

    public JButton getDefaultButton(){
        return defaultButton;
    }

    public void setDefaultButton(JButton defaultButton){
        JButton oldDefault=this.defaultButton;
        if(oldDefault!=defaultButton){
            this.defaultButton=defaultButton;
            if(oldDefault!=null){
                oldDefault.repaint();
            }
            if(defaultButton!=null){
                defaultButton.repaint();
            }
        }
        firePropertyChange("defaultButton",oldDefault,defaultButton);
    }

    final boolean getUseTrueDoubleBuffering(){
        return useTrueDoubleBuffering;
    }

    final void setUseTrueDoubleBuffering(boolean useTrueDoubleBuffering){
        this.useTrueDoubleBuffering=useTrueDoubleBuffering;
    }

    final void disableTrueDoubleBuffering(){
        if(useTrueDoubleBuffering){
            if(!IGNORE_DISABLE_TRUE_DOUBLE_BUFFERING){
                if(LOG_DISABLE_TRUE_DOUBLE_BUFFERING){
                    System.out.println("Disabling true double buffering for "+
                            this);
                    Thread.dumpStack();
                }
                useTrueDoubleBuffering=false;
                RepaintManager.currentManager(this).
                        doubleBufferingChanged(this);
            }
        }
    }

    protected void addImpl(Component comp,Object constraints,int index){
        super.addImpl(comp,constraints,index);
        /// We are making sure the glassPane is on top.
        if(glassPane!=null
                &&glassPane.getParent()==this
                &&getComponent(0)!=glassPane){
            add(glassPane,0);
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJRootPane();
        }
        return accessibleContext;
    }

    @SuppressWarnings("serial")
    static class DefaultAction extends AbstractAction{
        JButton owner;
        JRootPane root;
        boolean press;

        DefaultAction(JRootPane root,boolean press){
            this.root=root;
            this.press=press;
        }

        public void setOwner(JButton owner){
            this.owner=owner;
        }

        public void actionPerformed(ActionEvent e){
            if(owner!=null&&SwingUtilities.getRootPane(owner)==root){
                ButtonModel model=owner.getModel();
                if(press){
                    model.setArmed(true);
                    model.setPressed(true);
                }else{
                    model.setPressed(false);
                }
            }
        }

        public boolean isEnabled(){
            return owner.getModel().isEnabled();
        }
    }
///////////////////////////////////////////////////////////////////////////////
//// Begin Inner Classes
///////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("serial")
    protected class RootLayout implements LayoutManager2, Serializable{
        public void addLayoutComponent(String name,Component comp){
        }

        public void removeLayoutComponent(Component comp){
        }

        public Dimension preferredLayoutSize(Container parent){
            Dimension rd, mbd;
            Insets i=getInsets();
            if(contentPane!=null){
                rd=contentPane.getPreferredSize();
            }else{
                rd=parent.getSize();
            }
            if(menuBar!=null&&menuBar.isVisible()){
                mbd=menuBar.getPreferredSize();
            }else{
                mbd=new Dimension(0,0);
            }
            return new Dimension(Math.max(rd.width,mbd.width)+i.left+i.right,
                    rd.height+mbd.height+i.top+i.bottom);
        }

        public Dimension minimumLayoutSize(Container parent){
            Dimension rd, mbd;
            Insets i=getInsets();
            if(contentPane!=null){
                rd=contentPane.getMinimumSize();
            }else{
                rd=parent.getSize();
            }
            if(menuBar!=null&&menuBar.isVisible()){
                mbd=menuBar.getMinimumSize();
            }else{
                mbd=new Dimension(0,0);
            }
            return new Dimension(Math.max(rd.width,mbd.width)+i.left+i.right,
                    rd.height+mbd.height+i.top+i.bottom);
        }

        public void layoutContainer(Container parent){
            Rectangle b=parent.getBounds();
            Insets i=getInsets();
            int contentY=0;
            int w=b.width-i.right-i.left;
            int h=b.height-i.top-i.bottom;
            if(layeredPane!=null){
                layeredPane.setBounds(i.left,i.top,w,h);
            }
            if(glassPane!=null){
                glassPane.setBounds(i.left,i.top,w,h);
            }
            // Note: This is laying out the children in the layeredPane,
            // technically, these are not our children.
            if(menuBar!=null&&menuBar.isVisible()){
                Dimension mbd=menuBar.getPreferredSize();
                menuBar.setBounds(0,0,w,mbd.height);
                contentY+=mbd.height;
            }
            if(contentPane!=null){
                contentPane.setBounds(0,contentY,w,h-contentY);
            }
        }

        public void addLayoutComponent(Component comp,Object constraints){
        }

        public Dimension maximumLayoutSize(Container target){
            Dimension rd, mbd;
            Insets i=getInsets();
            if(menuBar!=null&&menuBar.isVisible()){
                mbd=menuBar.getMaximumSize();
            }else{
                mbd=new Dimension(0,0);
            }
            if(contentPane!=null){
                rd=contentPane.getMaximumSize();
            }else{
                // This is silly, but should stop an overflow error
                rd=new Dimension(Integer.MAX_VALUE,
                        Integer.MAX_VALUE-i.top-i.bottom-mbd.height-1);
            }
            return new Dimension(Math.min(rd.width,mbd.width)+i.left+i.right,
                    rd.height+mbd.height+i.top+i.bottom);
        }

        public float getLayoutAlignmentX(Container target){
            return 0.0f;
        }

        public float getLayoutAlignmentY(Container target){
            return 0.0f;
        }

        public void invalidateLayout(Container target){
        }
    }

    @SuppressWarnings("serial")
    protected class AccessibleJRootPane extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.ROOT_PANE;
        }

        public int getAccessibleChildrenCount(){
            return super.getAccessibleChildrenCount();
        }

        public Accessible getAccessibleChild(int i){
            return super.getAccessibleChild(i);
        }
    } // inner class AccessibleJRootPane
/////////////////
// Accessibility support
////////////////




}
