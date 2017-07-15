/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

public class ComponentView extends View{
    // --- member variables ------------------------------------------------
    private Component createdC;
    private Invalidator c;

    public ComponentView(Element elem){
        super(elem);
    }
    // --- View methods ---------------------------------------------

    public final Component getComponent(){
        return createdC;
    }

    public float getPreferredSpan(int axis){
        if((axis!=X_AXIS)&&(axis!=Y_AXIS)){
            throw new IllegalArgumentException("Invalid axis: "+axis);
        }
        if(c!=null){
            Dimension size=c.getPreferredSize();
            if(axis==View.X_AXIS){
                return size.width;
            }else{
                return size.height;
            }
        }
        return 0;
    }

    public float getMinimumSpan(int axis){
        if((axis!=X_AXIS)&&(axis!=Y_AXIS)){
            throw new IllegalArgumentException("Invalid axis: "+axis);
        }
        if(c!=null){
            Dimension size=c.getMinimumSize();
            if(axis==View.X_AXIS){
                return size.width;
            }else{
                return size.height;
            }
        }
        return 0;
    }

    public float getMaximumSpan(int axis){
        if((axis!=X_AXIS)&&(axis!=Y_AXIS)){
            throw new IllegalArgumentException("Invalid axis: "+axis);
        }
        if(c!=null){
            Dimension size=c.getMaximumSize();
            if(axis==View.X_AXIS){
                return size.width;
            }else{
                return size.height;
            }
        }
        return 0;
    }

    public float getAlignment(int axis){
        if(c!=null){
            switch(axis){
                case View.X_AXIS:
                    return c.getAlignmentX();
                case View.Y_AXIS:
                    return c.getAlignmentY();
            }
        }
        return super.getAlignment(axis);
    }

    public void paint(Graphics g,Shape a){
        if(c!=null){
            Rectangle alloc=(a instanceof Rectangle)?
                    (Rectangle)a:a.getBounds();
            c.setBounds(alloc.x,alloc.y,alloc.width,alloc.height);
        }
    }

    public void setParent(View p){
        super.setParent(p);
        if(SwingUtilities.isEventDispatchThread()){
            setComponentParent();
        }else{
            Runnable callSetComponentParent=new Runnable(){
                public void run(){
                    Document doc=getDocument();
                    try{
                        if(doc instanceof AbstractDocument){
                            ((AbstractDocument)doc).readLock();
                        }
                        setComponentParent();
                        Container host=getContainer();
                        if(host!=null){
                            preferenceChanged(null,true,true);
                            host.repaint();
                        }
                    }finally{
                        if(doc instanceof AbstractDocument){
                            ((AbstractDocument)doc).readUnlock();
                        }
                    }
                }
            };
            SwingUtilities.invokeLater(callSetComponentParent);
        }
    }

    void setComponentParent(){
        View p=getParent();
        if(p!=null){
            Container parent=getContainer();
            if(parent!=null){
                if(c==null){
                    // try to build a component
                    Component comp=createComponent();
                    if(comp!=null){
                        createdC=comp;
                        c=new Invalidator(comp);
                    }
                }
                if(c!=null){
                    if(c.getParent()==null){
                        // components associated with the View tree are added
                        // to the hosting container with the View as a constraint.
                        parent.add(c,this);
                        parent.addPropertyChangeListener("enabled",c);
                    }
                }
            }
        }else{
            if(c!=null){
                Container parent=c.getParent();
                if(parent!=null){
                    // remove the component from its hosting container
                    parent.remove(c);
                    parent.removePropertyChangeListener("enabled",c);
                }
            }
        }
    }

    protected Component createComponent(){
        AttributeSet attr=getElement().getAttributes();
        Component comp=StyleConstants.getComponent(attr);
        return comp;
    }

    public Shape modelToView(int pos,Shape a,Position.Bias b) throws BadLocationException{
        int p0=getStartOffset();
        int p1=getEndOffset();
        if((pos>=p0)&&(pos<=p1)){
            Rectangle r=a.getBounds();
            if(pos==p1){
                r.x+=r.width;
            }
            r.width=0;
            return r;
        }
        throw new BadLocationException(pos+" not in range "+p0+","+p1,pos);
    }

    public int viewToModel(float x,float y,Shape a,Position.Bias[] bias){
        Rectangle alloc=(Rectangle)a;
        if(x<alloc.x+(alloc.width/2)){
            bias[0]=Position.Bias.Forward;
            return getStartOffset();
        }
        bias[0]=Position.Bias.Backward;
        return getEndOffset();
    }

    class Invalidator extends Container implements PropertyChangeListener{
        // NOTE: When we remove this class we are going to have to some
        // how enforce setting of the focus traversal keys on the children
        // so that they don't inherit them from the JEditorPane. We need
        // to do this as JEditorPane has abnormal bindings (it is a focus cycle
        // root) and the children typically don't want these bindings as well.

        Dimension min;
        Dimension pref;
        Dimension max;
        float yalign;
        float xalign;

        Invalidator(Component child){
            setLayout(null);
            add(child);
            cacheChildSizes();
        }

        private void cacheChildSizes(){
            if(getComponentCount()>0){
                Component child=getComponent(0);
                min=child.getMinimumSize();
                pref=child.getPreferredSize();
                max=child.getMaximumSize();
                yalign=child.getAlignmentY();
                xalign=child.getAlignmentX();
            }else{
                min=pref=max=new Dimension(0,0);
            }
        }

        public void doLayout(){
            cacheChildSizes();
        }

        public void invalidate(){
            super.invalidate();
            if(getParent()!=null){
                preferenceChanged(null,true,true);
            }
        }

        public Dimension getPreferredSize(){
            validateIfNecessary();
            return pref;
        }

        public Dimension getMinimumSize(){
            validateIfNecessary();
            return min;
        }

        public void validateIfNecessary(){
            if(!isValid()){
                validate();
            }
        }

        public Dimension getMaximumSize(){
            validateIfNecessary();
            return max;
        }

        public float getAlignmentX(){
            validateIfNecessary();
            return xalign;
        }

        public float getAlignmentY(){
            validateIfNecessary();
            return yalign;
        }

        public Set<AWTKeyStroke> getFocusTraversalKeys(int id){
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().
                    getDefaultFocusTraversalKeys(id);
        }

        public boolean isShowing(){
            return true;
        }

        public void setVisible(boolean b){
            super.setVisible(b);
            if(getComponentCount()>0){
                getComponent(0).setVisible(b);
            }
        }

        public void setBounds(int x,int y,int w,int h){
            super.setBounds(x,y,w,h);
            if(getComponentCount()>0){
                getComponent(0).setSize(w,h);
            }
            cacheChildSizes();
        }

        public void propertyChange(PropertyChangeEvent ev){
            Boolean enable=(Boolean)ev.getNewValue();
            if(getComponentCount()>0){
                getComponent(0).setEnabled(enable);
            }
        }
    }
}
