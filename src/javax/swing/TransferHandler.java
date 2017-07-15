/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.misc.JavaSecurityAccess;
import sun.misc.SharedSecrets;
import sun.reflect.misc.MethodUtil;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2;
import sun.swing.UIAction;

import javax.swing.event.EventListenerList;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.TooManyListenersException;

@SuppressWarnings("serial")
public class TransferHandler implements Serializable{
    public static final int NONE=DnDConstants.ACTION_NONE;
    public static final int COPY=DnDConstants.ACTION_COPY;
    public static final int MOVE=DnDConstants.ACTION_MOVE;
    public static final int COPY_OR_MOVE=DnDConstants.ACTION_COPY_OR_MOVE;
    public static final int LINK=DnDConstants.ACTION_LINK;
    static final Action cutAction=new TransferAction("cut");
    static final Action copyAction=new TransferAction("copy");

    ;
    static final Action pasteAction=new TransferAction("paste");
    private static SwingDragGestureRecognizer recognizer=null;
    private Image dragImage;
    private Point dragImageOffset;
    private String propertyName;

    protected TransferHandler(){
        this(null);
    }

    public TransferHandler(String property){
        propertyName=property;
    }

    public static Action getCutAction(){
        return cutAction;
    }

    public static Action getCopyAction(){
        return copyAction;
    }

    public static Action getPasteAction(){
        return pasteAction;
    }

    private static DropTargetListener getDropTargetListener(){
        synchronized(DropHandler.class){
            DropHandler handler=
                    (DropHandler)AppContext.getAppContext().get(DropHandler.class);
            if(handler==null){
                handler=new DropHandler();
                AppContext.getAppContext().put(DropHandler.class,handler);
            }
            return handler;
        }
    }

    public Image getDragImage(){
        return dragImage;
    }

    public void setDragImage(Image img){
        dragImage=img;
    }

    public Point getDragImageOffset(){
        if(dragImageOffset==null){
            return new Point(0,0);
        }
        return new Point(dragImageOffset);
    }

    public void setDragImageOffset(Point p){
        dragImageOffset=new Point(p);
    }

    public void exportAsDrag(JComponent comp,InputEvent e,int action){
        int srcActions=getSourceActions(comp);
        // only mouse events supported for drag operations
        if(!(e instanceof MouseEvent)
                // only support known actions
                ||!(action==COPY||action==MOVE||action==LINK)
                // only support valid source actions
                ||(srcActions&action)==0){
            action=NONE;
        }
        if(action!=NONE&&!GraphicsEnvironment.isHeadless()){
            if(recognizer==null){
                recognizer=new SwingDragGestureRecognizer(new DragHandler());
            }
            recognizer.gestured(comp,(MouseEvent)e,srcActions,action);
        }else{
            exportDone(comp,null,NONE);
        }
    }

    public int getSourceActions(JComponent c){
        PropertyDescriptor prop=getPropertyDescriptor(c);
        if(prop!=null){
            return COPY;
        }
        return NONE;
    }

    private PropertyDescriptor getPropertyDescriptor(JComponent comp){
        if(propertyName==null){
            return null;
        }
        Class<?> k=comp.getClass();
        BeanInfo bi;
        try{
            bi=Introspector.getBeanInfo(k);
        }catch(IntrospectionException ex){
            return null;
        }
        PropertyDescriptor props[]=bi.getPropertyDescriptors();
        for(int i=0;i<props.length;i++){
            if(propertyName.equals(props[i].getName())){
                Method reader=props[i].getReadMethod();
                if(reader!=null){
                    Class<?>[] params=reader.getParameterTypes();
                    if(params==null||params.length==0){
                        // found the desired descriptor
                        return props[i];
                    }
                }
            }
        }
        return null;
    }

    protected void exportDone(JComponent source,Transferable data,int action){
    }

    public void exportToClipboard(JComponent comp,Clipboard clip,int action)
            throws IllegalStateException{
        if((action==COPY||action==MOVE)
                &&(getSourceActions(comp)&action)!=0){
            Transferable t=createTransferable(comp);
            if(t!=null){
                try{
                    clip.setContents(t,null);
                    exportDone(comp,t,action);
                    return;
                }catch(IllegalStateException ise){
                    exportDone(comp,t,NONE);
                    throw ise;
                }
            }
        }
        exportDone(comp,null,NONE);
    }

    protected Transferable createTransferable(JComponent c){
        PropertyDescriptor property=getPropertyDescriptor(c);
        if(property!=null){
            return new PropertyTransferable(property,c);
        }
        return null;
    }

    public boolean importData(TransferSupport support){
        return support.getComponent() instanceof JComponent
                ?importData((JComponent)support.getComponent(),support.getTransferable())
                :false;
    }

    public boolean importData(JComponent comp,Transferable t){
        PropertyDescriptor prop=getPropertyDescriptor(comp);
        if(prop!=null){
            Method writer=prop.getWriteMethod();
            if(writer==null){
                // read-only property. ignore
                return false;
            }
            Class<?>[] params=writer.getParameterTypes();
            if(params.length!=1){
                // zero or more than one argument, ignore
                return false;
            }
            DataFlavor flavor=getPropertyDataFlavor(params[0],t.getTransferDataFlavors());
            if(flavor!=null){
                try{
                    Object value=t.getTransferData(flavor);
                    Object[] args={value};
                    MethodUtil.invoke(writer,comp,args);
                    return true;
                }catch(Exception ex){
                    System.err.println("Invocation failed");
                    // invocation code
                }
            }
        }
        return false;
    }

    private DataFlavor getPropertyDataFlavor(Class<?> k,DataFlavor[] flavors){
        for(int i=0;i<flavors.length;i++){
            DataFlavor flavor=flavors[i];
            if("application".equals(flavor.getPrimaryType())&&
                    "x-java-jvm-local-objectref".equals(flavor.getSubType())&&
                    k.isAssignableFrom(flavor.getRepresentationClass())){
                return flavor;
            }
        }
        return null;
    }

    public boolean canImport(TransferSupport support){
        return support.getComponent() instanceof JComponent
                ?canImport((JComponent)support.getComponent(),support.getDataFlavors())
                :false;
    }

    public boolean canImport(JComponent comp,DataFlavor[] transferFlavors){
        PropertyDescriptor prop=getPropertyDescriptor(comp);
        if(prop!=null){
            Method writer=prop.getWriteMethod();
            if(writer==null){
                // read-only property. ignore
                return false;
            }
            Class<?>[] params=writer.getParameterTypes();
            if(params.length!=1){
                // zero or more than one argument, ignore
                return false;
            }
            DataFlavor flavor=getPropertyDataFlavor(params[0],transferFlavors);
            if(flavor!=null){
                return true;
            }
        }
        return false;
    }

    public Icon getVisualRepresentation(Transferable t){
        return null;
    }

    interface HasGetTransferHandler{
        public TransferHandler getTransferHandler();
    }

    public static class DropLocation{
        private final Point dropPoint;

        protected DropLocation(Point dropPoint){
            if(dropPoint==null){
                throw new IllegalArgumentException("Point cannot be null");
            }
            this.dropPoint=new Point(dropPoint);
        }

        public final Point getDropPoint(){
            return new Point(dropPoint);
        }

        public String toString(){
            return getClass().getName()+"[dropPoint="+dropPoint+"]";
        }
    }

    public final static class TransferSupport{
        private boolean isDrop;
        private Component component;
        private boolean showDropLocationIsSet;
        private boolean showDropLocation;
        private int dropAction=-1;
        private Object source;
        private DropLocation dropLocation;

        private TransferSupport(Component component,
                                DropTargetEvent event){
            isDrop=true;
            setDNDVariables(component,event);
        }

        private void setDNDVariables(Component component,
                                     DropTargetEvent event){
            assert isDrop;
            this.component=component;
            this.source=event;
            dropLocation=null;
            dropAction=-1;
            showDropLocationIsSet=false;
            if(source==null){
                return;
            }
            assert source instanceof DropTargetDragEvent||
                    source instanceof DropTargetDropEvent;
            Point p=source instanceof DropTargetDragEvent
                    ?((DropTargetDragEvent)source).getLocation()
                    :((DropTargetDropEvent)source).getLocation();
            if(SunToolkit.isInstanceOf(component,"javax.swing.text.JTextComponent")){
                dropLocation=SwingAccessor.getJTextComponentAccessor().
                        dropLocationForPoint((JTextComponent)component,p);
            }else if(component instanceof JComponent){
                dropLocation=((JComponent)component).dropLocationForPoint(p);
            }
            /**
             * The drop location may be null at this point if the component
             * doesn't return custom drop locations. In this case, a point-only
             * drop location will be created lazily when requested.
             */
        }

        public TransferSupport(Component component,Transferable transferable){
            if(component==null){
                throw new NullPointerException("component is null");
            }
            if(transferable==null){
                throw new NullPointerException("transferable is null");
            }
            isDrop=false;
            this.component=component;
            this.source=transferable;
        }

        public boolean isDrop(){
            return isDrop;
        }

        public Component getComponent(){
            return component;
        }

        public DropLocation getDropLocation(){
            assureIsDrop();
            if(dropLocation==null){
                /**
                 * component didn't give us a custom drop location,
                 * so lazily create a point-only location
                 */
                Point p=source instanceof DropTargetDragEvent
                        ?((DropTargetDragEvent)source).getLocation()
                        :((DropTargetDropEvent)source).getLocation();
                dropLocation=new DropLocation(p);
            }
            return dropLocation;
        }

        private void assureIsDrop(){
            if(!isDrop){
                throw new IllegalStateException("Not a drop");
            }
        }

        public void setShowDropLocation(boolean showDropLocation){
            assureIsDrop();
            this.showDropLocation=showDropLocation;
            this.showDropLocationIsSet=true;
        }

        public int getDropAction(){
            return dropAction==-1?getUserDropAction():dropAction;
        }

        public void setDropAction(int dropAction){
            assureIsDrop();
            int action=dropAction&getSourceDropActions();
            if(!(action==COPY||action==MOVE||action==LINK)){
                throw new IllegalArgumentException("unsupported drop action: "+dropAction);
            }
            this.dropAction=dropAction;
        }

        public int getSourceDropActions(){
            assureIsDrop();
            return (source instanceof DropTargetDragEvent)
                    ?((DropTargetDragEvent)source).getSourceActions()
                    :((DropTargetDropEvent)source).getSourceActions();
        }

        public int getUserDropAction(){
            assureIsDrop();
            return (source instanceof DropTargetDragEvent)
                    ?((DropTargetDragEvent)source).getDropAction()
                    :((DropTargetDropEvent)source).getDropAction();
        }

        public DataFlavor[] getDataFlavors(){
            if(isDrop){
                if(source instanceof DropTargetDragEvent){
                    return ((DropTargetDragEvent)source).getCurrentDataFlavors();
                }else{
                    return ((DropTargetDropEvent)source).getCurrentDataFlavors();
                }
            }
            return ((Transferable)source).getTransferDataFlavors();
        }

        public boolean isDataFlavorSupported(DataFlavor df){
            if(isDrop){
                if(source instanceof DropTargetDragEvent){
                    return ((DropTargetDragEvent)source).isDataFlavorSupported(df);
                }else{
                    return ((DropTargetDropEvent)source).isDataFlavorSupported(df);
                }
            }
            return ((Transferable)source).isDataFlavorSupported(df);
        }

        public Transferable getTransferable(){
            if(isDrop){
                if(source instanceof DropTargetDragEvent){
                    return ((DropTargetDragEvent)source).getTransferable();
                }else{
                    return ((DropTargetDropEvent)source).getTransferable();
                }
            }
            return (Transferable)source;
        }
    }

    static class PropertyTransferable implements Transferable{
        JComponent component;
        // --- Transferable methods ----------------------------------------------
        PropertyDescriptor property;

        PropertyTransferable(PropertyDescriptor p,JComponent c){
            property=p;
            component=c;
        }

        public DataFlavor[] getTransferDataFlavors(){
            DataFlavor[] flavors=new DataFlavor[1];
            Class<?> propertyType=property.getPropertyType();
            String mimeType=DataFlavor.javaJVMLocalObjectMimeType+";class="+propertyType.getName();
            try{
                flavors[0]=new DataFlavor(mimeType);
            }catch(ClassNotFoundException cnfe){
                flavors=new DataFlavor[0];
            }
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor){
            Class<?> propertyType=property.getPropertyType();
            if("application".equals(flavor.getPrimaryType())&&
                    "x-java-jvm-local-objectref".equals(flavor.getSubType())&&
                    flavor.getRepresentationClass().isAssignableFrom(propertyType)){
                return true;
            }
            return false;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException{
            if(!isDataFlavorSupported(flavor)){
                throw new UnsupportedFlavorException(flavor);
            }
            Method reader=property.getReadMethod();
            Object value=null;
            try{
                value=MethodUtil.invoke(reader,component,(Object[])null);
            }catch(Exception ex){
                throw new IOException("Property read failed: "+property.getName());
            }
            return value;
        }
    }

    static class SwingDropTarget extends DropTarget implements UIResource{
        private EventListenerList listenerList;

        SwingDropTarget(Component c){
            super(c,COPY_OR_MOVE|LINK,null);
            try{
                // addDropTargetListener is overridden
                // we specifically need to add to the superclass
                super.addDropTargetListener(getDropTargetListener());
            }catch(TooManyListenersException tmle){
            }
        }

        public void addDropTargetListener(DropTargetListener dtl) throws TooManyListenersException{
            // Since the super class only supports one DropTargetListener,
            // and we add one from the constructor, we always add to the
            // extended list.
            if(listenerList==null){
                listenerList=new EventListenerList();
            }
            listenerList.add(DropTargetListener.class,dtl);
        }
        // --- DropTargetListener methods (multicast) --------------------------

        public void removeDropTargetListener(DropTargetListener dtl){
            if(listenerList!=null){
                listenerList.remove(DropTargetListener.class,dtl);
            }
        }

        public void dragEnter(DropTargetDragEvent e){
            super.dragEnter(e);
            if(listenerList!=null){
                Object[] listeners=listenerList.getListenerList();
                for(int i=listeners.length-2;i>=0;i-=2){
                    if(listeners[i]==DropTargetListener.class){
                        ((DropTargetListener)listeners[i+1]).dragEnter(e);
                    }
                }
            }
        }

        public void dragOver(DropTargetDragEvent e){
            super.dragOver(e);
            if(listenerList!=null){
                Object[] listeners=listenerList.getListenerList();
                for(int i=listeners.length-2;i>=0;i-=2){
                    if(listeners[i]==DropTargetListener.class){
                        ((DropTargetListener)listeners[i+1]).dragOver(e);
                    }
                }
            }
        }

        public void dropActionChanged(DropTargetDragEvent e){
            super.dropActionChanged(e);
            if(listenerList!=null){
                Object[] listeners=listenerList.getListenerList();
                for(int i=listeners.length-2;i>=0;i-=2){
                    if(listeners[i]==DropTargetListener.class){
                        ((DropTargetListener)listeners[i+1]).dropActionChanged(e);
                    }
                }
            }
        }

        public void dragExit(DropTargetEvent e){
            super.dragExit(e);
            if(listenerList!=null){
                Object[] listeners=listenerList.getListenerList();
                for(int i=listeners.length-2;i>=0;i-=2){
                    if(listeners[i]==DropTargetListener.class){
                        ((DropTargetListener)listeners[i+1]).dragExit(e);
                    }
                }
            }
            if(!isActive()){
                // If the Drop target is inactive the dragExit will not be dispatched to the dtListener,
                // so make sure that we clean up the dtListener anyway.
                DropTargetListener dtListener=getDropTargetListener();
                if(dtListener!=null&&dtListener instanceof DropHandler){
                    ((DropHandler)dtListener).cleanup(false);
                }
            }
        }

        public void drop(DropTargetDropEvent e){
            super.drop(e);
            if(listenerList!=null){
                Object[] listeners=listenerList.getListenerList();
                for(int i=listeners.length-2;i>=0;i-=2){
                    if(listeners[i]==DropTargetListener.class){
                        ((DropTargetListener)listeners[i+1]).drop(e);
                    }
                }
            }
        }
    }

    private static class DropHandler implements DropTargetListener,
            Serializable,
            ActionListener{
        private static final int AUTOSCROLL_INSET=10;
        private Timer timer;
        private Point lastPosition;
        private Rectangle outer=new Rectangle();
        private Rectangle inner=new Rectangle();
        private int hysteresis=10;
        private Component component;
        private Object state;
        private TransferSupport support=
                new TransferSupport(null,(DropTargetEvent)null);

        public void actionPerformed(ActionEvent e){
            updateAutoscrollRegion((JComponent)component);
            if(outer.contains(lastPosition)&&!inner.contains(lastPosition)){
                autoscroll((JComponent)component,lastPosition);
            }
        }

        private void updateAutoscrollRegion(JComponent c){
            // compute the outer
            Rectangle visible=c.getVisibleRect();
            outer.setBounds(visible.x,visible.y,visible.width,visible.height);
            // compute the insets
            Insets i=new Insets(0,0,0,0);
            if(c instanceof Scrollable){
                int minSize=2*AUTOSCROLL_INSET;
                if(visible.width>=minSize){
                    i.left=i.right=AUTOSCROLL_INSET;
                }
                if(visible.height>=minSize){
                    i.top=i.bottom=AUTOSCROLL_INSET;
                }
            }
            // set the inner from the insets
            inner.setBounds(visible.x+i.left,
                    visible.y+i.top,
                    visible.width-(i.left+i.right),
                    visible.height-(i.top+i.bottom));
        }

        private void autoscroll(JComponent c,Point pos){
            if(c instanceof Scrollable){
                Scrollable s=(Scrollable)c;
                if(pos.y<inner.y){
                    // scroll upward
                    int dy=s.getScrollableUnitIncrement(outer,SwingConstants.VERTICAL,-1);
                    Rectangle r=new Rectangle(inner.x,outer.y-dy,inner.width,dy);
                    c.scrollRectToVisible(r);
                }else if(pos.y>(inner.y+inner.height)){
                    // scroll downard
                    int dy=s.getScrollableUnitIncrement(outer,SwingConstants.VERTICAL,1);
                    Rectangle r=new Rectangle(inner.x,outer.y+outer.height,inner.width,dy);
                    c.scrollRectToVisible(r);
                }
                if(pos.x<inner.x){
                    // scroll left
                    int dx=s.getScrollableUnitIncrement(outer,SwingConstants.HORIZONTAL,-1);
                    Rectangle r=new Rectangle(outer.x-dx,inner.y,dx,inner.height);
                    c.scrollRectToVisible(r);
                }else if(pos.x>(inner.x+inner.width)){
                    // scroll right
                    int dx=s.getScrollableUnitIncrement(outer,SwingConstants.HORIZONTAL,1);
                    Rectangle r=new Rectangle(outer.x+outer.width,inner.y,dx,inner.height);
                    c.scrollRectToVisible(r);
                }
            }
        }

        public void dragEnter(DropTargetDragEvent e){
            state=null;
            component=e.getDropTargetContext().getComponent();
            handleDrag(e);
            if(component instanceof JComponent){
                lastPosition=e.getLocation();
                updateAutoscrollRegion((JComponent)component);
                initPropertiesIfNecessary();
            }
        }
        // --- DropTargetListener methods -----------------------------------

        private void initPropertiesIfNecessary(){
            if(timer==null){
                Toolkit t=Toolkit.getDefaultToolkit();
                Integer prop;
                prop=(Integer)
                        t.getDesktopProperty("DnD.Autoscroll.interval");
                timer=new Timer(prop==null?100:prop.intValue(),this);
                prop=(Integer)
                        t.getDesktopProperty("DnD.Autoscroll.initialDelay");
                timer.setInitialDelay(prop==null?100:prop.intValue());
                prop=(Integer)
                        t.getDesktopProperty("DnD.Autoscroll.cursorHysteresis");
                if(prop!=null){
                    hysteresis=prop.intValue();
                }
            }
        }

        private void handleDrag(DropTargetDragEvent e){
            TransferHandler importer=
                    ((HasGetTransferHandler)component).getTransferHandler();
            if(importer==null){
                e.rejectDrag();
                setComponentDropLocation(null,false);
                return;
            }
            support.setDNDVariables(component,e);
            boolean canImport=importer.canImport(support);
            if(canImport){
                e.acceptDrag(support.getDropAction());
            }else{
                e.rejectDrag();
            }
            boolean showLocation=support.showDropLocationIsSet?
                    support.showDropLocation:
                    canImport;
            setComponentDropLocation(showLocation?support:null,false);
        }

        private void setComponentDropLocation(TransferSupport support,
                                              boolean forDrop){
            DropLocation dropLocation=(support==null)
                    ?null
                    :support.getDropLocation();
            if(SunToolkit.isInstanceOf(component,"javax.swing.text.JTextComponent")){
                state=SwingAccessor.getJTextComponentAccessor().
                        setDropLocation((JTextComponent)component,dropLocation,state,forDrop);
            }else if(component instanceof JComponent){
                state=((JComponent)component).setDropLocation(dropLocation,state,forDrop);
            }
        }

        public void dragOver(DropTargetDragEvent e){
            handleDrag(e);
            if(!(component instanceof JComponent)){
                return;
            }
            Point p=e.getLocation();
            if(Math.abs(p.x-lastPosition.x)>hysteresis
                    ||Math.abs(p.y-lastPosition.y)>hysteresis){
                // no autoscroll
                if(timer.isRunning()) timer.stop();
            }else{
                if(!timer.isRunning()) timer.start();
            }
            lastPosition=p;
        }

        public void dropActionChanged(DropTargetDragEvent e){
            /**
             * Work-around for Linux bug where dropActionChanged
             * is called before dragEnter.
             */
            if(component==null){
                return;
            }
            handleDrag(e);
        }

        public void dragExit(DropTargetEvent e){
            cleanup(false);
        }

        public void drop(DropTargetDropEvent e){
            TransferHandler importer=
                    ((HasGetTransferHandler)component).getTransferHandler();
            if(importer==null){
                e.rejectDrop();
                cleanup(false);
                return;
            }
            support.setDNDVariables(component,e);
            boolean canImport=importer.canImport(support);
            if(canImport){
                e.acceptDrop(support.getDropAction());
                boolean showLocation=support.showDropLocationIsSet?
                        support.showDropLocation:
                        canImport;
                setComponentDropLocation(showLocation?support:null,false);
                boolean success;
                try{
                    success=importer.importData(support);
                }catch(RuntimeException re){
                    success=false;
                }
                e.dropComplete(success);
                cleanup(success);
            }else{
                e.rejectDrop();
                cleanup(false);
            }
        }

        private void cleanup(boolean forDrop){
            setComponentDropLocation(null,forDrop);
            if(component instanceof JComponent){
                ((JComponent)component).dndDone();
            }
            if(timer!=null){
                timer.stop();
            }
            state=null;
            component=null;
            lastPosition=null;
        }
    }

    private static class DragHandler implements DragGestureListener, DragSourceListener{
        private boolean scrolls;
        // --- DragGestureListener methods -----------------------------------

        public void dragGestureRecognized(DragGestureEvent dge){
            JComponent c=(JComponent)dge.getComponent();
            TransferHandler th=c.getTransferHandler();
            Transferable t=th.createTransferable(c);
            if(t!=null){
                scrolls=c.getAutoscrolls();
                c.setAutoscrolls(false);
                try{
                    Image im=th.getDragImage();
                    if(im==null){
                        dge.startDrag(null,t,this);
                    }else{
                        dge.startDrag(null,im,th.getDragImageOffset(),t,this);
                    }
                    return;
                }catch(RuntimeException re){
                    c.setAutoscrolls(scrolls);
                }
            }
            th.exportDone(c,t,NONE);
        }
        // --- DragSourceListener methods -----------------------------------

        public void dragEnter(DragSourceDragEvent dsde){
        }

        public void dragOver(DragSourceDragEvent dsde){
        }

        public void dropActionChanged(DragSourceDragEvent dsde){
        }

        public void dragExit(DragSourceEvent dsde){
        }

        public void dragDropEnd(DragSourceDropEvent dsde){
            DragSourceContext dsc=dsde.getDragSourceContext();
            JComponent c=(JComponent)dsc.getComponent();
            if(dsde.getDropSuccess()){
                c.getTransferHandler().exportDone(c,dsc.getTransferable(),dsde.getDropAction());
            }else{
                c.getTransferHandler().exportDone(c,dsc.getTransferable(),NONE);
            }
            c.setAutoscrolls(scrolls);
        }
    }

    private static class SwingDragGestureRecognizer extends DragGestureRecognizer{
        SwingDragGestureRecognizer(DragGestureListener dgl){
            super(DragSource.getDefaultDragSource(),null,NONE,dgl);
        }

        void gestured(JComponent c,MouseEvent e,int srcActions,int action){
            setComponent(c);
            setSourceActions(srcActions);
            appendEvent(e);
            fireDragGestureRecognized(action,e.getPoint());
        }

        protected void registerListeners(){
        }

        protected void unregisterListeners(){
        }
    }

    static class TransferAction extends UIAction implements UIResource{
        private static final JavaSecurityAccess javaSecurityAccess=
                SharedSecrets.getJavaSecurityAccess();
        private static Object SandboxClipboardKey=new Object();

        TransferAction(String name){
            super(name);
        }

        public boolean isEnabled(Object sender){
            if(sender instanceof JComponent
                    &&((JComponent)sender).getTransferHandler()==null){
                return false;
            }
            return true;
        }        public void actionPerformed(final ActionEvent e){
            final Object src=e.getSource();
            final PrivilegedAction<Void> action=new PrivilegedAction<Void>(){
                public Void run(){
                    actionPerformedImpl(e);
                    return null;
                }
            };
            final AccessControlContext stack=AccessController.getContext();
            final AccessControlContext srcAcc=AWTAccessor.getComponentAccessor().getAccessControlContext((Component)src);
            final AccessControlContext eventAcc=AWTAccessor.getAWTEventAccessor().getAccessControlContext(e);
            if(srcAcc==null){
                javaSecurityAccess.doIntersectionPrivilege(action,stack,eventAcc);
            }else{
                javaSecurityAccess.doIntersectionPrivilege(
                        new PrivilegedAction<Void>(){
                            public Void run(){
                                javaSecurityAccess.doIntersectionPrivilege(action,eventAcc);
                                return null;
                            }
                        },stack,srcAcc);
            }
        }

        private void actionPerformedImpl(ActionEvent e){
            Object src=e.getSource();
            if(src instanceof JComponent){
                JComponent c=(JComponent)src;
                TransferHandler th=c.getTransferHandler();
                Clipboard clipboard=getClipboard(c);
                String name=(String)getValue(Action.NAME);
                Transferable trans=null;
                // any of these calls may throw IllegalStateException
                try{
                    if((clipboard!=null)&&(th!=null)&&(name!=null)){
                        if("cut".equals(name)){
                            th.exportToClipboard(c,clipboard,MOVE);
                        }else if("copy".equals(name)){
                            th.exportToClipboard(c,clipboard,COPY);
                        }else if("paste".equals(name)){
                            trans=clipboard.getContents(null);
                        }
                    }
                }catch(IllegalStateException ise){
                    // clipboard was unavailable
                    UIManager.getLookAndFeel().provideErrorFeedback(c);
                    return;
                }
                // this is a paste action, import data into the component
                if(trans!=null){
                    th.importData(new TransferSupport(c,trans));
                }
            }
        }

        private Clipboard getClipboard(JComponent c){
            if(SwingUtilities2.canAccessSystemClipboard()){
                return c.getToolkit().getSystemClipboard();
            }
            Clipboard clipboard=(Clipboard)AppContext.getAppContext().
                    get(SandboxClipboardKey);
            if(clipboard==null){
                clipboard=new Clipboard("Sandboxed Component Clipboard");
                AppContext.getAppContext().put(SandboxClipboardKey,
                        clipboard);
            }
            return clipboard;
        }


    }
}
