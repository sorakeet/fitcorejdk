/**
 * Copyright (c) 2005, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.awt.AppContext;
import sun.awt.dnd.SunDragSourceContextPeer;

import javax.swing.*;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;

class DragRecognitionSupport{
    private int motionThreshold;
    private MouseEvent dndArmedEvent;
    private JComponent component;

    public static boolean mousePressed(MouseEvent me){
        return getDragRecognitionSupport().mousePressedImpl(me);
    }

    private static DragRecognitionSupport getDragRecognitionSupport(){
        DragRecognitionSupport support=
                (DragRecognitionSupport)AppContext.getAppContext().
                        get(DragRecognitionSupport.class);
        if(support==null){
            support=new DragRecognitionSupport();
            AppContext.getAppContext().put(DragRecognitionSupport.class,support);
        }
        return support;
    }

    public static MouseEvent mouseReleased(MouseEvent me){
        return getDragRecognitionSupport().mouseReleasedImpl(me);
    }

    public static boolean mouseDragged(MouseEvent me,BeforeDrag bd){
        return getDragRecognitionSupport().mouseDraggedImpl(me,bd);
    }

    private boolean mousePressedImpl(MouseEvent me){
        component=(JComponent)me.getSource();
        if(mapDragOperationFromModifiers(me,component.getTransferHandler())
                !=TransferHandler.NONE){
            motionThreshold=DragSource.getDragThreshold();
            dndArmedEvent=me;
            return true;
        }
        clearState();
        return false;
    }

    private void clearState(){
        dndArmedEvent=null;
        component=null;
    }

    private int mapDragOperationFromModifiers(MouseEvent me,
                                              TransferHandler th){
        if(th==null||!SwingUtilities.isLeftMouseButton(me)){
            return TransferHandler.NONE;
        }
        return SunDragSourceContextPeer.
                convertModifiersToDropAction(me.getModifiersEx(),
                        th.getSourceActions(component));
    }

    private MouseEvent mouseReleasedImpl(MouseEvent me){
        /** no recognition has been going on */
        if(dndArmedEvent==null){
            return null;
        }
        MouseEvent retEvent=null;
        if(me.getSource()==component){
            retEvent=dndArmedEvent;
        } // else component has changed unexpectedly, so return null
        clearState();
        return retEvent;
    }

    private boolean mouseDraggedImpl(MouseEvent me,BeforeDrag bd){
        /** no recognition is in progress */
        if(dndArmedEvent==null){
            return false;
        }
        /** component has changed unexpectedly, so bail */
        if(me.getSource()!=component){
            clearState();
            return false;
        }
        int dx=Math.abs(me.getX()-dndArmedEvent.getX());
        int dy=Math.abs(me.getY()-dndArmedEvent.getY());
        if((dx>motionThreshold)||(dy>motionThreshold)){
            TransferHandler th=component.getTransferHandler();
            int action=mapDragOperationFromModifiers(me,th);
            if(action!=TransferHandler.NONE){
                /** notify the BeforeDrag instance */
                if(bd!=null){
                    bd.dragStarting(dndArmedEvent);
                }
                th.exportAsDrag(component,dndArmedEvent,action);
                clearState();
            }
        }
        return true;
    }

    public static interface BeforeDrag{
        public void dragStarting(MouseEvent me);
    }
}
