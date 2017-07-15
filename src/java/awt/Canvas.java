/**
 * Copyright (c) 1995, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.image.BufferStrategy;
import java.awt.peer.CanvasPeer;

public class Canvas extends Component implements Accessible{
    private static final String base="canvas";
    private static final long serialVersionUID=-2284879212465893870L;
    private static int nameCounter=0;

    public Canvas(GraphicsConfiguration config){
        this();
        setGraphicsConfiguration(config);
    }

    public Canvas(){
    }

    String constructComponentName(){
        synchronized(Canvas.class){
            return base+nameCounter++;
        }
    }

    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc){
        synchronized(getTreeLock()){
            CanvasPeer peer=(CanvasPeer)getPeer();
            if(peer!=null){
                gc=peer.getAppropriateGraphicsConfiguration(gc);
            }
            super.setGraphicsConfiguration(gc);
        }
    }

    public void paint(Graphics g){
        g.clearRect(0,0,width,height);
    }

    public void update(Graphics g){
        g.clearRect(0,0,width,height);
        paint(g);
    }

    public void createBufferStrategy(int numBuffers){
        super.createBufferStrategy(numBuffers);
    }

    public void createBufferStrategy(int numBuffers,
                                     BufferCapabilities caps) throws AWTException{
        super.createBufferStrategy(numBuffers,caps);
    }

    public BufferStrategy getBufferStrategy(){
        return super.getBufferStrategy();
    }

    boolean postsOldMouseEvents(){
        return true;
    }

    public void addNotify(){
        synchronized(getTreeLock()){
            if(peer==null)
                peer=getToolkit().createCanvas(this);
            super.addNotify();
        }
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleAWTCanvas();
        }
        return accessibleContext;
    }

    protected class AccessibleAWTCanvas extends AccessibleAWTComponent{
        private static final long serialVersionUID=-6325592262103146699L;

        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.CANVAS;
        }
    } // inner class AccessibleAWTCanvas
}
