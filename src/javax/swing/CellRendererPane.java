/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class CellRendererPane extends Container implements Accessible{
/////////////////
// Accessibility support
////////////////
    protected AccessibleContext accessibleContext=null;

    public CellRendererPane(){
        super();
        setLayout(null);
        setVisible(false);
    }

    protected void addImpl(Component x,Object constraints,int index){
        if(x.getParent()==this){
            return;
        }else{
            super.addImpl(x,constraints,index);
        }
    }

    public void invalidate(){
    }

    public void paint(Graphics g){
    }

    public void update(Graphics g){
    }

    public void paintComponent(Graphics g,Component c,Container p,Rectangle r){
        paintComponent(g,c,p,r.x,r.y,r.width,r.height);
    }

    public void paintComponent(Graphics g,Component c,Container p,int x,int y,int w,int h){
        paintComponent(g,c,p,x,y,w,h,false);
    }

    public void paintComponent(Graphics g,Component c,Container p,int x,int y,int w,int h,boolean shouldValidate){
        if(c==null){
            if(p!=null){
                Color oldColor=g.getColor();
                g.setColor(p.getBackground());
                g.fillRect(x,y,w,h);
                g.setColor(oldColor);
            }
            return;
        }
        if(c.getParent()!=this){
            this.add(c);
        }
        c.setBounds(x,y,w,h);
        if(shouldValidate){
            c.validate();
        }
        boolean wasDoubleBuffered=false;
        if((c instanceof JComponent)&&((JComponent)c).isDoubleBuffered()){
            wasDoubleBuffered=true;
            ((JComponent)c).setDoubleBuffered(false);
        }
        Graphics cg=g.create(x,y,w,h);
        try{
            c.paint(cg);
        }finally{
            cg.dispose();
        }
        if(wasDoubleBuffered&&(c instanceof JComponent)){
            ((JComponent)c).setDoubleBuffered(true);
        }
        c.setBounds(-w,-h,0,0);
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        removeAll();
        s.defaultWriteObject();
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleCellRendererPane();
        }
        return accessibleContext;
    }

    protected class AccessibleCellRendererPane extends AccessibleAWTContainer{
        // AccessibleContext methods
        //
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.PANEL;
        }
    } // inner class AccessibleCellRendererPane
}
