/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.SunToolkit;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.*;
import java.util.ArrayList;
import java.util.Hashtable;

@SuppressWarnings("serial")
public class JLayeredPane extends JComponent implements Accessible{
    /// Watch the values in getObjectForLayer()
    public final static Integer DEFAULT_LAYER=new Integer(0);
    public final static Integer PALETTE_LAYER=new Integer(100);
    public final static Integer MODAL_LAYER=new Integer(200);
    public final static Integer POPUP_LAYER=new Integer(300);
    public final static Integer DRAG_LAYER=new Integer(400);
    public final static Integer FRAME_CONTENT_LAYER=new Integer(-30000);
    public final static String LAYER_PROPERTY="layeredContainerLayer";
    // Hashtable to store layer values for non-JComponent components
    private Hashtable<Component,Integer> componentToLayer;
    private boolean optimizedDrawingPossible=true;

    //////////////////////////////////////////////////////////////////////////////
//// Container Override methods
//////////////////////////////////////////////////////////////////////////////
    public JLayeredPane(){
        setLayout(null);
    }

    //////////////////////////////////////////////////////////////////////////////
//// New methods for managing layers
//////////////////////////////////////////////////////////////////////////////
    public static void putLayer(JComponent c,int layer){
        /// MAKE SURE THIS AND setLayer(Component c, int layer, int position)  are SYNCED
        Integer layerObj;
        layerObj=new Integer(layer);
        c.putClientProperty(LAYER_PROPERTY,layerObj);
    }

    public static int getLayer(JComponent c){
        Integer i;
        if((i=(Integer)c.getClientProperty(LAYER_PROPERTY))!=null)
            return i.intValue();
        return DEFAULT_LAYER.intValue();
    }

    public static JLayeredPane getLayeredPaneAbove(Component c){
        if(c==null) return null;
        Component parent=c.getParent();
        while(parent!=null&&!(parent instanceof JLayeredPane))
            parent=parent.getParent();
        return (JLayeredPane)parent;
    }

    protected void addImpl(Component comp,Object constraints,int index){
        int layer;
        int pos;
        if(constraints instanceof Integer){
            layer=((Integer)constraints).intValue();
            setLayer(comp,layer);
        }else
            layer=getLayer(comp);
        pos=insertIndexForLayer(layer,index);
        super.addImpl(comp,constraints,pos);
        comp.validate();
        comp.repaint();
        validateOptimizedDrawing();
    }

    public void remove(int index){
        Component c=getComponent(index);
        super.remove(index);
        if(c!=null&&!(c instanceof JComponent)){
            getComponentToLayer().remove(c);
        }
        validateOptimizedDrawing();
    }

    private void validateOptimizedDrawing(){
        boolean layeredComponentFound=false;
        synchronized(getTreeLock()){
            Integer layer;
            for(Component c : getComponents()){
                layer=null;
                if(SunToolkit.isInstanceOf(c,"javax.swing.JInternalFrame")||
                        (c instanceof JComponent&&
                                (layer=(Integer)((JComponent)c).
                                        getClientProperty(LAYER_PROPERTY))!=null)){
                    if(layer!=null&&layer.equals(FRAME_CONTENT_LAYER))
                        continue;
                    layeredComponentFound=true;
                    break;
                }
            }
        }
        if(layeredComponentFound)
            optimizedDrawingPossible=false;
        else
            optimizedDrawingPossible=true;
    }

    public void removeAll(){
        Component[] children=getComponents();
        Hashtable<Component,Integer> cToL=getComponentToLayer();
        for(int counter=children.length-1;counter>=0;counter--){
            Component c=children[counter];
            if(c!=null&&!(c instanceof JComponent)){
                cToL.remove(c);
            }
        }
        super.removeAll();
    }

    protected Hashtable<Component,Integer> getComponentToLayer(){
        if(componentToLayer==null)
            componentToLayer=new Hashtable<Component,Integer>(4);
        return componentToLayer;
    }

    public void setLayer(Component c,int layer){
        setLayer(c,layer,-1);
    }

    public void setLayer(Component c,int layer,int position){
        Integer layerObj;
        layerObj=getObjectForLayer(layer);
        if(layer==getLayer(c)&&position==getPosition(c)){
            repaint(c.getBounds());
            return;
        }
        /// MAKE SURE THIS AND putLayer(JComponent c, int layer) are SYNCED
        if(c instanceof JComponent)
            ((JComponent)c).putClientProperty(LAYER_PROPERTY,layerObj);
        else
            getComponentToLayer().put(c,layerObj);
        if(c.getParent()==null||c.getParent()!=this){
            repaint(c.getBounds());
            return;
        }
        int index=insertIndexForLayer(c,layer,position);
        setComponentZOrder(c,index);
        repaint(c.getBounds());
    }

    public int getIndexOf(Component c){
        int i, count;
        count=getComponentCount();
        for(i=0;i<count;i++){
            if(c==getComponent(i))
                return i;
        }
        return -1;
    }

    public void moveToFront(Component c){
        setPosition(c,0);
    }

    public void moveToBack(Component c){
        setPosition(c,-1);
    }

    public void setPosition(Component c,int position){
        setLayer(c,getLayer(c),position);
    }

    public int getPosition(Component c){
        int i, startLayer, curLayer, startLocation, pos=0;
        getComponentCount();
        startLocation=getIndexOf(c);
        if(startLocation==-1)
            return -1;
        startLayer=getLayer(c);
        for(i=startLocation-1;i>=0;i--){
            curLayer=getLayer(getComponent(i));
            if(curLayer==startLayer)
                pos++;
            else
                return pos;
        }
        return pos;
    }

    public int highestLayer(){
        if(getComponentCount()>0)
            return getLayer(getComponent(0));
        return 0;
    }

    public int getLayer(Component c){
        Integer i;
        if(c instanceof JComponent)
            i=(Integer)((JComponent)c).getClientProperty(LAYER_PROPERTY);
        else
            i=getComponentToLayer().get(c);
        if(i==null)
            return DEFAULT_LAYER.intValue();
        return i.intValue();
    }

    public int lowestLayer(){
        int count=getComponentCount();
        if(count>0)
            return getLayer(getComponent(count-1));
        return 0;
    }

    public int getComponentCountInLayer(int layer){
        int i, count, curLayer;
        int layerCount=0;
        count=getComponentCount();
        for(i=0;i<count;i++){
            curLayer=getLayer(getComponent(i));
            if(curLayer==layer){
                layerCount++;
                /// Short circut the counting when we have them all
            }else if(layerCount>0||curLayer<layer){
                break;
            }
        }
        return layerCount;
    }

    public Component[] getComponentsInLayer(int layer){
        int i, count, curLayer;
        int layerCount=0;
        Component[] results;
        results=new Component[getComponentCountInLayer(layer)];
        count=getComponentCount();
        for(i=0;i<count;i++){
            curLayer=getLayer(getComponent(i));
            if(curLayer==layer){
                results[layerCount++]=getComponent(i);
                /// Short circut the counting when we have them all
            }else if(layerCount>0||curLayer<layer){
                break;
            }
        }
        return results;
    }

    public void paint(Graphics g){
        if(isOpaque()){
            Rectangle r=g.getClipBounds();
            Color c=getBackground();
            if(c==null)
                c=Color.lightGray;
            g.setColor(c);
            if(r!=null){
                g.fillRect(r.x,r.y,r.width,r.height);
            }else{
                g.fillRect(0,0,getWidth(),getHeight());
            }
        }
        super.paint(g);
    }
//////////////////////////////////////////////////////////////////////////////
//// Implementation Details
//////////////////////////////////////////////////////////////////////////////

    public boolean isOptimizedDrawingEnabled(){
        return optimizedDrawingPossible;
    }

    protected String paramString(){
        String optimizedDrawingPossibleString=(optimizedDrawingPossible?
                "true":"false");
        return super.paramString()+
                ",optimizedDrawingPossible="+optimizedDrawingPossibleString;
    }

    protected Integer getObjectForLayer(int layer){
        Integer layerObj;
        switch(layer){
            case 0:
                layerObj=DEFAULT_LAYER;
                break;
            case 100:
                layerObj=PALETTE_LAYER;
                break;
            case 200:
                layerObj=MODAL_LAYER;
                break;
            case 300:
                layerObj=POPUP_LAYER;
                break;
            case 400:
                layerObj=DRAG_LAYER;
                break;
            default:
                layerObj=new Integer(layer);
        }
        return layerObj;
    }

    protected int insertIndexForLayer(int layer,int position){
        return insertIndexForLayer(null,layer,position);
    }

    private int insertIndexForLayer(Component comp,int layer,int position){
        int i, count, curLayer;
        int layerStart=-1;
        int layerEnd=-1;
        int componentCount=getComponentCount();
        ArrayList<Component> compList=
                new ArrayList<Component>(componentCount);
        for(int index=0;index<componentCount;index++){
            if(getComponent(index)!=comp){
                compList.add(getComponent(index));
            }
        }
        count=compList.size();
        for(i=0;i<count;i++){
            curLayer=getLayer(compList.get(i));
            if(layerStart==-1&&curLayer==layer){
                layerStart=i;
            }
            if(curLayer<layer){
                if(i==0){
                    // layer is greater than any current layer
                    // [ ASSERT(layer > highestLayer()) ]
                    layerStart=0;
                    layerEnd=0;
                }else{
                    layerEnd=i;
                }
                break;
            }
        }
        // layer requested is lower than any current layer
        // [ ASSERT(layer < lowestLayer()) ]
        // put it on the bottom of the stack
        if(layerStart==-1&&layerEnd==-1)
            return count;
        // In the case of a single layer entry handle the degenerative cases
        if(layerStart!=-1&&layerEnd==-1)
            layerEnd=count;
        if(layerEnd!=-1&&layerStart==-1)
            layerStart=layerEnd;
        // If we are adding to the bottom, return the last element
        if(position==-1)
            return layerEnd;
        // Otherwise make sure the requested position falls in the
        // proper range
        if(position>-1&&layerStart+position<=layerEnd)
            return layerStart+position;
        // Otherwise return the end of the layer
        return layerEnd;
    }
/////////////////
// Accessibility support
////////////////

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJLayeredPane();
        }
        return accessibleContext;
    }

    @SuppressWarnings("serial")
    protected class AccessibleJLayeredPane extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.LAYERED_PANE;
        }
    }
}
