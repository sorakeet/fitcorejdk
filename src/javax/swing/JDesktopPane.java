/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.DesktopPaneUI;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

public class JDesktopPane extends JLayeredPane implements Accessible{
    public static final int LIVE_DRAG_MODE=0;
    public static final int OUTLINE_DRAG_MODE=1;
    private static final String uiClassID="DesktopPaneUI";
    transient DesktopManager desktopManager;
    private transient JInternalFrame selectedFrame=null;
    private int dragMode=LIVE_DRAG_MODE;
    private boolean dragModeSet=false;
    private transient List<JInternalFrame> framesCache;
    private boolean componentOrderCheckingEnabled=true;
    private boolean componentOrderChanged=false;

    public JDesktopPane(){
        setUIProperty("opaque",Boolean.TRUE);
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy(){
            public Component getDefaultComponent(Container c){
                JInternalFrame jifArray[]=getAllFrames();
                Component comp=null;
                for(JInternalFrame jif : jifArray){
                    comp=jif.getFocusTraversalPolicy().getDefaultComponent(jif);
                    if(comp!=null){
                        break;
                    }
                }
                return comp;
            }
        });
        updateUI();
    }

    private static Collection<JInternalFrame> getAllFrames(Container parent){
        int i, count;
        Collection<JInternalFrame> results=new LinkedHashSet<>();
        count=parent.getComponentCount();
        for(i=0;i<count;i++){
            Component next=parent.getComponent(i);
            if(next instanceof JInternalFrame){
                results.add((JInternalFrame)next);
            }else if(next instanceof JInternalFrame.JDesktopIcon){
                JInternalFrame tmp=((JInternalFrame.JDesktopIcon)next).getInternalFrame();
                if(tmp!=null){
                    results.add(tmp);
                }
            }else if(next instanceof Container){
                results.addAll(getAllFrames((Container)next));
            }
        }
        return results;
    }

    public DesktopPaneUI getUI(){
        return (DesktopPaneUI)ui;
    }

    public void setUI(DesktopPaneUI ui){
        super.setUI(ui);
    }

    public int getDragMode(){
        return dragMode;
    }

    public void setDragMode(int dragMode){
        int oldDragMode=this.dragMode;
        this.dragMode=dragMode;
        firePropertyChange("dragMode",oldDragMode,this.dragMode);
        dragModeSet=true;
    }

    public DesktopManager getDesktopManager(){
        return desktopManager;
    }

    public void setDesktopManager(DesktopManager d){
        DesktopManager oldValue=desktopManager;
        desktopManager=d;
        firePropertyChange("desktopManager",oldValue,desktopManager);
    }

    public void updateUI(){
        setUI((DesktopPaneUI)UIManager.getUI(this));
    }

    public String getUIClassID(){
        return uiClassID;
    }

    void setUIProperty(String propertyName,Object value){
        if(propertyName=="dragMode"){
            if(!dragModeSet){
                setDragMode(((Integer)value).intValue());
                dragModeSet=false;
            }
        }else{
            super.setUIProperty(propertyName,value);
        }
    }

    public JInternalFrame[] getAllFrames(){
        return getAllFrames(this).toArray(new JInternalFrame[0]);
    }

    public JInternalFrame[] getAllFramesInLayer(int layer){
        Collection<JInternalFrame> allFrames=getAllFrames(this);
        Iterator<JInternalFrame> iterator=allFrames.iterator();
        while(iterator.hasNext()){
            if(iterator.next().getLayer()!=layer){
                iterator.remove();
            }
        }
        return allFrames.toArray(new JInternalFrame[0]);
    }

    JInternalFrame getNextFrame(JInternalFrame f){
        return getNextFrame(f,true);
    }

    private JInternalFrame getNextFrame(JInternalFrame f,boolean forward){
        verifyFramesCache();
        if(f==null){
            return getTopInternalFrame();
        }
        int i=framesCache.indexOf(f);
        if(i==-1||framesCache.size()==1){
            /** error */
            return null;
        }
        if(forward){
            // navigate to the next frame
            if(++i==framesCache.size()){
                /** wrap */
                i=0;
            }
        }else{
            // navigate to the previous frame
            if(--i==-1){
                /** wrap */
                i=framesCache.size()-1;
            }
        }
        return framesCache.get(i);
    }

    private JInternalFrame getTopInternalFrame(){
        if(framesCache.size()==0){
            return null;
        }
        return framesCache.get(0);
    }

    private void verifyFramesCache(){
        // If framesCache is dirty, then recreate it.
        if(componentOrderChanged){
            componentOrderChanged=false;
            updateFramesCache();
        }
    }

    private void updateFramesCache(){
        framesCache=getFrames();
    }

    private List<JInternalFrame> getFrames(){
        Component c;
        Set<ComponentPosition> set=new TreeSet<ComponentPosition>();
        for(int i=0;i<getComponentCount();i++){
            c=getComponent(i);
            if(c instanceof JInternalFrame){
                set.add(new ComponentPosition((JInternalFrame)c,getLayer(c),
                        i));
            }else if(c instanceof JInternalFrame.JDesktopIcon){
                c=((JInternalFrame.JDesktopIcon)c).getInternalFrame();
                set.add(new ComponentPosition((JInternalFrame)c,getLayer(c),
                        i));
            }
        }
        List<JInternalFrame> frames=new ArrayList<JInternalFrame>(
                set.size());
        for(ComponentPosition position : set){
            frames.add(position.component);
        }
        return frames;
    }

    public JInternalFrame selectFrame(boolean forward){
        JInternalFrame selectedFrame=getSelectedFrame();
        JInternalFrame frameToSelect=getNextFrame(selectedFrame,forward);
        if(frameToSelect==null){
            return null;
        }
        // Maintain navigation traversal order until an
        // external stack change, such as a click on a frame.
        setComponentOrderCheckingEnabled(false);
        if(forward&&selectedFrame!=null){
            selectedFrame.moveToBack();  // For Windows MDI fidelity.
        }
        try{
            frameToSelect.setSelected(true);
        }catch(PropertyVetoException pve){
        }
        setComponentOrderCheckingEnabled(true);
        return frameToSelect;
    }

    public JInternalFrame getSelectedFrame(){
        return selectedFrame;
    }

    public void setSelectedFrame(JInternalFrame f){
        selectedFrame=f;
    }

    void setComponentOrderCheckingEnabled(boolean enable){
        componentOrderCheckingEnabled=enable;
    }

    protected void addImpl(Component comp,Object constraints,int index){
        super.addImpl(comp,constraints,index);
        if(componentOrderCheckingEnabled){
            if(comp instanceof JInternalFrame||
                    comp instanceof JInternalFrame.JDesktopIcon){
                componentOrderChanged=true;
            }
        }
    }

    public void remove(int index){
        if(componentOrderCheckingEnabled){
            Component comp=getComponent(index);
            if(comp instanceof JInternalFrame||
                    comp instanceof JInternalFrame.JDesktopIcon){
                componentOrderChanged=true;
            }
        }
        super.remove(index);
    }

    public void removeAll(){
        if(componentOrderCheckingEnabled){
            int count=getComponentCount();
            for(int i=0;i<count;i++){
                Component comp=getComponent(i);
                if(comp instanceof JInternalFrame||
                        comp instanceof JInternalFrame.JDesktopIcon){
                    componentOrderChanged=true;
                    break;
                }
            }
        }
        super.removeAll();
    }

    protected String paramString(){
        String desktopManagerString=(desktopManager!=null?
                desktopManager.toString():"");
        return super.paramString()+
                ",desktopManager="+desktopManagerString;
    }

    public AccessibleContext getAccessibleContext(){
        if(accessibleContext==null){
            accessibleContext=new AccessibleJDesktopPane();
        }
        return accessibleContext;
    }

    public void setComponentZOrder(Component comp,int index){
        super.setComponentZOrder(comp,index);
        if(componentOrderCheckingEnabled){
            if(comp instanceof JInternalFrame||
                    comp instanceof JInternalFrame.JDesktopIcon){
                componentOrderChanged=true;
            }
        }
    }

    @Override
    public void remove(Component comp){
        super.remove(comp);
        updateFramesCache();
    }

    private void writeObject(ObjectOutputStream s) throws IOException{
        s.defaultWriteObject();
        if(getUIClassID().equals(uiClassID)){
            byte count=JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this,--count);
            if(count==0&&ui!=null){
                ui.installUI(this);
            }
        }
    }
/////////////////
// Accessibility support
////////////////

    private static class ComponentPosition implements
            Comparable<ComponentPosition>{
        private final JInternalFrame component;
        private final int layer;
        private final int zOrder;

        ComponentPosition(JInternalFrame component,int layer,int zOrder){
            this.component=component;
            this.layer=layer;
            this.zOrder=zOrder;
        }

        public int compareTo(ComponentPosition o){
            int delta=o.layer-layer;
            if(delta==0){
                return zOrder-o.zOrder;
            }
            return delta;
        }
    }

    protected class AccessibleJDesktopPane extends AccessibleJComponent{
        public AccessibleRole getAccessibleRole(){
            return AccessibleRole.DESKTOP_PANE;
        }
    }
}
