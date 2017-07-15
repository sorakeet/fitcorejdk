/**
 * Copyright (c) 1997, 2014, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import com.sun.java.swing.SwingUtilities3;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.DisplayChangedListener;
import sun.awt.SunToolkit;
import sun.java2d.SunGraphicsEnvironment;
import sun.misc.JavaSecurityAccess;
import sun.misc.SharedSecrets;
import sun.security.action.GetPropertyAction;
import sun.swing.SwingAccessor;
import sun.swing.SwingUtilities2.RepaintListener;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.InvocationEvent;
import java.awt.image.VolatileImage;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RepaintManager{
    static final boolean HANDLE_TOP_LEVEL_PAINT;
    private static final short BUFFER_STRATEGY_NOT_SPECIFIED=0;
    private static final short BUFFER_STRATEGY_SPECIFIED_ON=1;
    private static final short BUFFER_STRATEGY_SPECIFIED_OFF=2;
    private static final short BUFFER_STRATEGY_TYPE;
    private static final Object repaintManagerKey=RepaintManager.class;
    private static final int volatileBufferType;
    // The maximum number of times Swing will attempt to use the VolatileImage
    // buffer during a paint operation.
    private static final int VOLATILE_LOOP_MAX=2;
    private static final JavaSecurityAccess javaSecurityAccess=
            SharedSecrets.getJavaSecurityAccess();
    private static final DisplayChangedListener displayChangedHandler=
            new DisplayChangedHandler();
    // Whether or not a VolatileImage should be used for double-buffered painting
    static boolean volatileImageBufferEnabled=true;
    private static boolean nativeDoubleBuffering;

    static{
        SwingAccessor.setRepaintManagerAccessor(new SwingAccessor.RepaintManagerAccessor(){
            @Override
            public void addRepaintListener(RepaintManager rm,RepaintListener l){
                rm.addRepaintListener(l);
            }

            @Override
            public void removeRepaintListener(RepaintManager rm,RepaintListener l){
                rm.removeRepaintListener(l);
            }
        });
        volatileImageBufferEnabled="true".equals(AccessController.
                doPrivileged(new GetPropertyAction(
                        "swing.volatileImageBufferEnabled","true")));
        boolean headless=GraphicsEnvironment.isHeadless();
        if(volatileImageBufferEnabled&&headless){
            volatileImageBufferEnabled=false;
        }
        nativeDoubleBuffering="true".equals(AccessController.doPrivileged(
                new GetPropertyAction("awt.nativeDoubleBuffering")));
        String bs=AccessController.doPrivileged(
                new GetPropertyAction("swing.bufferPerWindow"));
        if(headless){
            BUFFER_STRATEGY_TYPE=BUFFER_STRATEGY_SPECIFIED_OFF;
        }else if(bs==null){
            BUFFER_STRATEGY_TYPE=BUFFER_STRATEGY_NOT_SPECIFIED;
        }else if("true".equals(bs)){
            BUFFER_STRATEGY_TYPE=BUFFER_STRATEGY_SPECIFIED_ON;
        }else{
            BUFFER_STRATEGY_TYPE=BUFFER_STRATEGY_SPECIFIED_OFF;
        }
        HANDLE_TOP_LEVEL_PAINT="true".equals(AccessController.doPrivileged(
                new GetPropertyAction("swing.handleTopLevelPaint","true")));
        GraphicsEnvironment ge=GraphicsEnvironment.
                getLocalGraphicsEnvironment();
        if(ge instanceof SunGraphicsEnvironment){
            ((SunGraphicsEnvironment)ge).addDisplayChangedListener(
                    displayChangedHandler);
        }
        Toolkit tk=Toolkit.getDefaultToolkit();
        if((tk instanceof SunToolkit)
                &&((SunToolkit)tk).isSwingBackbufferTranslucencySupported()){
            volatileBufferType=Transparency.TRANSLUCENT;
        }else{
            volatileBufferType=Transparency.OPAQUE;
        }
    }

    private final ProcessingRunnable processingRunnable;
    boolean doubleBufferingEnabled=true;
    // Support for both the standard and volatile offscreen buffers exists to
    // provide backwards compatibility for the [rare] programs which may be
    // calling getOffScreenBuffer() and not expecting to get a VolatileImage.
    // Swing internally is migrating to use *only* the volatile image buffer.
    // Support for standard offscreen buffer
    //
    DoubleBufferInfo standardDoubleBuffer;
    Rectangle tmp=new Rectangle();
    private Map<GraphicsConfiguration,VolatileImage> volatileMap=new
            HashMap<GraphicsConfiguration,VolatileImage>(1);
    //
    // As of 1.6 Swing handles scheduling of paint events from native code.
    // That is, SwingPaintEventDispatcher is invoked on the toolkit thread,
    // which in turn invokes nativeAddDirtyRegion.  Because this is invoked
    // from the native thread we can not invoke any public methods and so
    // we introduce these added maps.  So, any time nativeAddDirtyRegion is
    // invoked the region is added to hwDirtyComponents and a work request
    // is scheduled.  When the work request is processed all entries in
    // this map are pushed to the real map (dirtyComponents) and then
    // painted with the rest of the components.
    //
    private Map<Container,Rectangle> hwDirtyComponents;
    private Map<Component,Rectangle> dirtyComponents;
    private Map<Component,Rectangle> tmpDirtyComponents;
    private java.util.List<Component> invalidComponents;
    // List of Runnables that need to be processed before painting from AWT.
    private java.util.List<Runnable> runnableList;
    private Dimension doubleBufferMaxSize;
    private PaintManager paintManager;
    private int paintDepth=0;
    private short bufferStrategyType;
    //
    // BufferStrategyPaintManager has the unique characteristic that it
    // must deal with the buffer being lost while painting to it.  For
    // example, if we paint a component and show it and the buffer has
    // become lost we must repaint the whole window.  To deal with that
    // the PaintManager calls into repaintRoot, and if we're still in
    // the process of painting the repaintRoot field is set to the JRootPane
    // and after the current JComponent.paintImmediately call finishes
    // paintImmediately will be invoked on the repaintRoot.  In this
    // way we don't try to show garbage to the screen.
    //
    private boolean painting;
    private JComponent repaintRoot;
    private Thread paintThread;
    private java.util.List<RepaintListener> repaintListeners=new ArrayList<>(1);

    public RepaintManager(){
        // Because we can't know what a subclass is doing with the
        // volatile image we immediately punt in subclasses.  If this
        // poses a problem we'll need a more sophisticated detection algorithm,
        // or API.
        this(BUFFER_STRATEGY_SPECIFIED_OFF);
    }

    private RepaintManager(short bufferStrategyType){
        // If native doublebuffering is being used, do NOT use
        // Swing doublebuffering.
        doubleBufferingEnabled=!nativeDoubleBuffering;
        synchronized(this){
            dirtyComponents=new IdentityHashMap<Component,Rectangle>();
            tmpDirtyComponents=new IdentityHashMap<Component,Rectangle>();
            this.bufferStrategyType=bufferStrategyType;
            hwDirtyComponents=new IdentityHashMap<Container,Rectangle>();
        }
        processingRunnable=new ProcessingRunnable();
    }

    public static RepaintManager currentManager(JComponent c){
        return currentManager((Component)c);
    }

    public static RepaintManager currentManager(Component c){
        // Note: DisplayChangedRunnable passes in null as the component, so if
        // component is ever used to determine the current
        // RepaintManager, DisplayChangedRunnable will need to be modified
        // accordingly.
        return currentManager(AppContext.getAppContext());
    }

    static RepaintManager currentManager(AppContext appContext){
        RepaintManager rm=(RepaintManager)appContext.get(repaintManagerKey);
        if(rm==null){
            rm=new RepaintManager(BUFFER_STRATEGY_TYPE);
            appContext.put(repaintManagerKey,rm);
        }
        return rm;
    }

    public static void setCurrentManager(RepaintManager aRepaintManager){
        if(aRepaintManager!=null){
            SwingUtilities.appContextPut(repaintManagerKey,aRepaintManager);
        }else{
            SwingUtilities.appContextRemove(repaintManagerKey);
        }
    }

    private void displayChanged(){
        clearImages();
    }

    private void clearImages(){
        clearImages(0,0);
    }

    private void clearImages(int width,int height){
        if(standardDoubleBuffer!=null&&standardDoubleBuffer.image!=null){
            if(standardDoubleBuffer.image.getWidth(null)>width||
                    standardDoubleBuffer.image.getHeight(null)>height){
                standardDoubleBuffer.image.flush();
                standardDoubleBuffer.image=null;
            }
        }
        // Clear out the VolatileImages
        Iterator<GraphicsConfiguration> gcs=volatileMap.keySet().iterator();
        while(gcs.hasNext()){
            GraphicsConfiguration gc=gcs.next();
            VolatileImage image=volatileMap.get(gc);
            if(image.getWidth()>width||image.getHeight()>height){
                image.flush();
                gcs.remove();
            }
        }
    }

    public synchronized void addInvalidComponent(JComponent invalidComponent){
        RepaintManager delegate=getDelegate(invalidComponent);
        if(delegate!=null){
            delegate.addInvalidComponent(invalidComponent);
            return;
        }
        Component validateRoot=
                SwingUtilities.getValidateRoot(invalidComponent,true);
        if(validateRoot==null){
            return;
        }
        /** Lazily create the invalidateComponents vector and add the
         * validateRoot if it's not there already.  If this validateRoot
         * is already in the vector, we're done.
         */
        if(invalidComponents==null){
            invalidComponents=new ArrayList<Component>();
        }else{
            int n=invalidComponents.size();
            for(int i=0;i<n;i++){
                if(validateRoot==invalidComponents.get(i)){
                    return;
                }
            }
        }
        invalidComponents.add(validateRoot);
        // Queue a Runnable to invoke paintDirtyRegions and
        // validateInvalidComponents.
        scheduleProcessingRunnable(SunToolkit.targetToAppContext(invalidComponent));
    }

    private void scheduleProcessingRunnable(AppContext context){
        if(processingRunnable.markPending()){
            Toolkit tk=Toolkit.getDefaultToolkit();
            if(tk instanceof SunToolkit){
                SunToolkit.getSystemEventQueueImplPP(context).
                        postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(),
                                processingRunnable));
            }else{
                Toolkit.getDefaultToolkit().getSystemEventQueue().
                        postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(),
                                processingRunnable));
            }
        }
    }

    private RepaintManager getDelegate(Component c){
        RepaintManager delegate=SwingUtilities3.getDelegateRepaintManager(c);
        if(this==delegate){
            delegate=null;
        }
        return delegate;
    }

    public synchronized void removeInvalidComponent(JComponent component){
        RepaintManager delegate=getDelegate(component);
        if(delegate!=null){
            delegate.removeInvalidComponent(component);
            return;
        }
        if(invalidComponents!=null){
            int index=invalidComponents.indexOf(component);
            if(index!=-1){
                invalidComponents.remove(index);
            }
        }
    }

    public void addDirtyRegion(Window window,int x,int y,int w,int h){
        addDirtyRegion0(window,x,y,w,h);
    }

    public void addDirtyRegion(Applet applet,int x,int y,int w,int h){
        addDirtyRegion0(applet,x,y,w,h);
    }

    void scheduleHeavyWeightPaints(){
        Map<Container,Rectangle> hws;
        synchronized(this){
            if(hwDirtyComponents.size()==0){
                return;
            }
            hws=hwDirtyComponents;
            hwDirtyComponents=new IdentityHashMap<Container,Rectangle>();
        }
        for(Container hw : hws.keySet()){
            Rectangle dirty=hws.get(hw);
            if(hw instanceof Window){
                addDirtyRegion((Window)hw,dirty.x,dirty.y,
                        dirty.width,dirty.height);
            }else if(hw instanceof Applet){
                addDirtyRegion((Applet)hw,dirty.x,dirty.y,
                        dirty.width,dirty.height);
            }else{ // SwingHeavyWeight
                addDirtyRegion0(hw,dirty.x,dirty.y,
                        dirty.width,dirty.height);
            }
        }
    }

    //
    // This is called from the toolkit thread when a native expose is
    // received.
    //
    void nativeAddDirtyRegion(AppContext appContext,Container c,
                              int x,int y,int w,int h){
        if(w>0&&h>0){
            synchronized(this){
                Rectangle dirty=hwDirtyComponents.get(c);
                if(dirty==null){
                    hwDirtyComponents.put(c,new Rectangle(x,y,w,h));
                }else{
                    hwDirtyComponents.put(c,SwingUtilities.computeUnion(
                            x,y,w,h,dirty));
                }
            }
            scheduleProcessingRunnable(appContext);
        }
    }

    //
    // This is called from the toolkit thread when awt needs to run a
    // Runnable before we paint.
    //
    void nativeQueueSurfaceDataRunnable(AppContext appContext,
                                        final Component c,final Runnable r){
        synchronized(this){
            if(runnableList==null){
                runnableList=new LinkedList<Runnable>();
            }
            runnableList.add(new Runnable(){
                public void run(){
                    AccessControlContext stack=AccessController.getContext();
                    AccessControlContext acc=
                            AWTAccessor.getComponentAccessor().getAccessControlContext(c);
                    javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){
                        public Void run(){
                            r.run();
                            return null;
                        }
                    },stack,acc);
                }
            });
        }
        scheduleProcessingRunnable(appContext);
    }

    public void markCompletelyDirty(JComponent aComponent){
        RepaintManager delegate=getDelegate(aComponent);
        if(delegate!=null){
            delegate.markCompletelyDirty(aComponent);
            return;
        }
        addDirtyRegion(aComponent,0,0,Integer.MAX_VALUE,Integer.MAX_VALUE);
    }

    public void addDirtyRegion(JComponent c,int x,int y,int w,int h){
        RepaintManager delegate=getDelegate(c);
        if(delegate!=null){
            delegate.addDirtyRegion(c,x,y,w,h);
            return;
        }
        addDirtyRegion0(c,x,y,w,h);
    }

    private void addDirtyRegion0(Container c,int x,int y,int w,int h){
        /** Special cases we don't have to bother with.
         */
        if((w<=0)||(h<=0)||(c==null)){
            return;
        }
        if((c.getWidth()<=0)||(c.getHeight()<=0)){
            return;
        }
        if(extendDirtyRegion(c,x,y,w,h)){
            // Component was already marked as dirty, region has been
            // extended, no need to continue.
            return;
        }
        /** Make sure that c and all it ancestors (up to an Applet or
         * Window) are visible.  This loop has the same effect as
         * checking c.isShowing() (and note that it's still possible
         * that c is completely obscured by an opaque ancestor in
         * the specified rectangle).
         */
        Component root=null;
        // Note: We can't synchronize around this, Frame.getExtendedState
        // is synchronized so that if we were to synchronize around this
        // it could lead to the possibility of getting locks out
        // of order and deadlocking.
        for(Container p=c;p!=null;p=p.getParent()){
            if(!p.isVisible()||(p.getPeer()==null)){
                return;
            }
            if((p instanceof Window)||(p instanceof Applet)){
                // Iconified frames are still visible!
                if(p instanceof Frame&&
                        (((Frame)p).getExtendedState()&Frame.ICONIFIED)==
                                Frame.ICONIFIED){
                    return;
                }
                root=p;
                break;
            }
        }
        if(root==null) return;
        synchronized(this){
            if(extendDirtyRegion(c,x,y,w,h)){
                // In between last check and this check another thread
                // queued up runnable, can bail here.
                return;
            }
            dirtyComponents.put(c,new Rectangle(x,y,w,h));
        }
        // Queue a Runnable to invoke paintDirtyRegions and
        // validateInvalidComponents.
        scheduleProcessingRunnable(SunToolkit.targetToAppContext(c));
    }

    private synchronized boolean extendDirtyRegion(
            Component c,int x,int y,int w,int h){
        Rectangle r=dirtyComponents.get(c);
        if(r!=null){
            // A non-null r implies c is already marked as dirty,
            // and that the parent is valid. Therefore we can
            // just union the rect and bail.
            SwingUtilities.computeUnion(x,y,w,h,r);
            return true;
        }
        return false;
    }

    public void markCompletelyClean(JComponent aComponent){
        RepaintManager delegate=getDelegate(aComponent);
        if(delegate!=null){
            delegate.markCompletelyClean(aComponent);
            return;
        }
        synchronized(this){
            dirtyComponents.remove(aComponent);
        }
    }

    public boolean isCompletelyDirty(JComponent aComponent){
        RepaintManager delegate=getDelegate(aComponent);
        if(delegate!=null){
            return delegate.isCompletelyDirty(aComponent);
        }
        Rectangle r;
        r=getDirtyRegion(aComponent);
        if(r.width==Integer.MAX_VALUE&&
                r.height==Integer.MAX_VALUE)
            return true;
        else
            return false;
    }

    public Rectangle getDirtyRegion(JComponent aComponent){
        RepaintManager delegate=getDelegate(aComponent);
        if(delegate!=null){
            return delegate.getDirtyRegion(aComponent);
        }
        Rectangle r;
        synchronized(this){
            r=dirtyComponents.get(aComponent);
        }
        if(r==null)
            return new Rectangle(0,0,0,0);
        else
            return new Rectangle(r);
    }

    public void validateInvalidComponents(){
        final java.util.List<Component> ic;
        synchronized(this){
            if(invalidComponents==null){
                return;
            }
            ic=invalidComponents;
            invalidComponents=null;
        }
        int n=ic.size();
        for(int i=0;i<n;i++){
            final Component c=ic.get(i);
            AccessControlContext stack=AccessController.getContext();
            AccessControlContext acc=
                    AWTAccessor.getComponentAccessor().getAccessControlContext(c);
            javaSecurityAccess.doIntersectionPrivilege(
                    new PrivilegedAction<Void>(){
                        public Void run(){
                            c.validate();
                            return null;
                        }
                    },stack,acc);
        }
    }

    private void prePaintDirtyRegions(){
        Map<Component,Rectangle> dirtyComponents;
        java.util.List<Runnable> runnableList;
        synchronized(this){
            dirtyComponents=this.dirtyComponents;
            runnableList=this.runnableList;
            this.runnableList=null;
        }
        if(runnableList!=null){
            for(Runnable runnable : runnableList){
                runnable.run();
            }
        }
        paintDirtyRegions();
        if(dirtyComponents.size()>0){
            // This'll only happen if a subclass isn't correctly dealing
            // with toplevels.
            paintDirtyRegions(dirtyComponents);
        }
    }

    private void updateWindows(Map<Component,Rectangle> dirtyComponents){
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        if(!(toolkit instanceof SunToolkit&&
                ((SunToolkit)toolkit).needUpdateWindow())){
            return;
        }
        Set<Window> windows=new HashSet<Window>();
        Set<Component> dirtyComps=dirtyComponents.keySet();
        for(Iterator<Component> it=dirtyComps.iterator();it.hasNext();){
            Component dirty=it.next();
            Window window=dirty instanceof Window?
                    (Window)dirty:
                    SwingUtilities.getWindowAncestor(dirty);
            if(window!=null&&
                    !window.isOpaque()){
                windows.add(window);
            }
        }
        for(Window window : windows){
            AWTAccessor.getWindowAccessor().updateWindow(window);
        }
    }

    boolean isPainting(){
        return painting;
    }

    public void paintDirtyRegions(){
        synchronized(this){  // swap for thread safety
            Map<Component,Rectangle> tmp=tmpDirtyComponents;
            tmpDirtyComponents=dirtyComponents;
            dirtyComponents=tmp;
            dirtyComponents.clear();
        }
        paintDirtyRegions(tmpDirtyComponents);
    }

    private void paintDirtyRegions(
            final Map<Component,Rectangle> tmpDirtyComponents){
        if(tmpDirtyComponents.isEmpty()){
            return;
        }
        final java.util.List<Component> roots=
                new ArrayList<Component>(tmpDirtyComponents.size());
        for(Component dirty : tmpDirtyComponents.keySet()){
            collectDirtyComponents(tmpDirtyComponents,dirty,roots);
        }
        final AtomicInteger count=new AtomicInteger(roots.size());
        painting=true;
        try{
            for(int j=0;j<count.get();j++){
                final int i=j;
                final Component dirtyComponent=roots.get(j);
                AccessControlContext stack=AccessController.getContext();
                AccessControlContext acc=
                        AWTAccessor.getComponentAccessor().getAccessControlContext(dirtyComponent);
                javaSecurityAccess.doIntersectionPrivilege(new PrivilegedAction<Void>(){
                    public Void run(){
                        Rectangle rect=tmpDirtyComponents.get(dirtyComponent);
                        // Sometimes when RepaintManager is changed during the painting
                        // we may get null here, see #6995769 for details
                        if(rect==null){
                            return null;
                        }
                        int localBoundsH=dirtyComponent.getHeight();
                        int localBoundsW=dirtyComponent.getWidth();
                        SwingUtilities.computeIntersection(0,
                                0,
                                localBoundsW,
                                localBoundsH,
                                rect);
                        if(dirtyComponent instanceof JComponent){
                            ((JComponent)dirtyComponent).paintImmediately(
                                    rect.x,rect.y,rect.width,rect.height);
                        }else if(dirtyComponent.isShowing()){
                            Graphics g=JComponent.safelyGetGraphics(
                                    dirtyComponent,dirtyComponent);
                            // If the Graphics goes away, it means someone disposed of
                            // the window, don't do anything.
                            if(g!=null){
                                g.setClip(rect.x,rect.y,rect.width,rect.height);
                                try{
                                    dirtyComponent.paint(g);
                                }finally{
                                    g.dispose();
                                }
                            }
                        }
                        // If the repaintRoot has been set, service it now and
                        // remove any components that are children of repaintRoot.
                        if(repaintRoot!=null){
                            adjustRoots(repaintRoot,roots,i+1);
                            count.set(roots.size());
                            paintManager.isRepaintingRoot=true;
                            repaintRoot.paintImmediately(0,0,repaintRoot.getWidth(),
                                    repaintRoot.getHeight());
                            paintManager.isRepaintingRoot=false;
                            // Only service repaintRoot once.
                            repaintRoot=null;
                        }
                        return null;
                    }
                },stack,acc);
            }
        }finally{
            painting=false;
        }
        updateWindows(tmpDirtyComponents);
        tmpDirtyComponents.clear();
    }

    private void adjustRoots(JComponent root,
                             java.util.List<Component> roots,int index){
        for(int i=roots.size()-1;i>=index;i--){
            Component c=roots.get(i);
            for(;;){
                if(c==root||c==null||!(c instanceof JComponent)){
                    break;
                }
                c=c.getParent();
            }
            if(c==root){
                roots.remove(i);
            }
        }
    }

    void collectDirtyComponents(Map<Component,Rectangle> dirtyComponents,
                                Component dirtyComponent,
                                java.util.List<Component> roots){
        int dx, dy, rootDx, rootDy;
        Component component, rootDirtyComponent, parent;
        Rectangle cBounds;
        // Find the highest parent which is dirty.  When we get out of this
        // rootDx and rootDy will contain the translation from the
        // rootDirtyComponent's coordinate system to the coordinates of the
        // original dirty component.  The tmp Rect is also used to compute the
        // visible portion of the dirtyRect.
        component=rootDirtyComponent=dirtyComponent;
        int x=dirtyComponent.getX();
        int y=dirtyComponent.getY();
        int w=dirtyComponent.getWidth();
        int h=dirtyComponent.getHeight();
        dx=rootDx=0;
        dy=rootDy=0;
        tmp.setBounds(dirtyComponents.get(dirtyComponent));
        // System.out.println("Collect dirty component for bound " + tmp +
        //                                   "component bounds is " + cBounds);;
        SwingUtilities.computeIntersection(0,0,w,h,tmp);
        if(tmp.isEmpty()){
            // System.out.println("Empty 1");
            return;
        }
        for(;;){
            if(!(component instanceof JComponent))
                break;
            parent=component.getParent();
            if(parent==null)
                break;
            component=parent;
            dx+=x;
            dy+=y;
            tmp.setLocation(tmp.x+x,tmp.y+y);
            x=component.getX();
            y=component.getY();
            w=component.getWidth();
            h=component.getHeight();
            tmp=SwingUtilities.computeIntersection(0,0,w,h,tmp);
            if(tmp.isEmpty()){
                // System.out.println("Empty 2");
                return;
            }
            if(dirtyComponents.get(component)!=null){
                rootDirtyComponent=component;
                rootDx=dx;
                rootDy=dy;
            }
        }
        if(dirtyComponent!=rootDirtyComponent){
            Rectangle r;
            tmp.setLocation(tmp.x+rootDx-dx,
                    tmp.y+rootDy-dy);
            r=dirtyComponents.get(rootDirtyComponent);
            SwingUtilities.computeUnion(tmp.x,tmp.y,tmp.width,tmp.height,r);
        }
        // If we haven't seen this root before, then we need to add it to the
        // list of root dirty Views.
        if(!roots.contains(rootDirtyComponent))
            roots.add(rootDirtyComponent);
    }

    public synchronized String toString(){
        StringBuffer sb=new StringBuffer();
        if(dirtyComponents!=null)
            sb.append(""+dirtyComponents);
        return sb.toString();
    }

    public Image getOffscreenBuffer(Component c,int proposedWidth,int proposedHeight){
        RepaintManager delegate=getDelegate(c);
        if(delegate!=null){
            return delegate.getOffscreenBuffer(c,proposedWidth,proposedHeight);
        }
        return _getOffscreenBuffer(c,proposedWidth,proposedHeight);
    }

    private Image _getOffscreenBuffer(Component c,int proposedWidth,int proposedHeight){
        Dimension maxSize=getDoubleBufferMaximumSize();
        DoubleBufferInfo doubleBuffer;
        int width, height;
        // If the window is non-opaque, it's double-buffered at peer's level
        Window w=(c instanceof Window)?(Window)c:SwingUtilities.getWindowAncestor(c);
        if(!w.isOpaque()){
            Toolkit tk=Toolkit.getDefaultToolkit();
            if((tk instanceof SunToolkit)&&(((SunToolkit)tk).needUpdateWindow())){
                return null;
            }
        }
        if(standardDoubleBuffer==null){
            standardDoubleBuffer=new DoubleBufferInfo();
        }
        doubleBuffer=standardDoubleBuffer;
        width=proposedWidth<1?1:
                (proposedWidth>maxSize.width?maxSize.width:proposedWidth);
        height=proposedHeight<1?1:
                (proposedHeight>maxSize.height?maxSize.height:proposedHeight);
        if(doubleBuffer.needsReset||(doubleBuffer.image!=null&&
                (doubleBuffer.size.width<width||
                        doubleBuffer.size.height<height))){
            doubleBuffer.needsReset=false;
            if(doubleBuffer.image!=null){
                doubleBuffer.image.flush();
                doubleBuffer.image=null;
            }
            width=Math.max(doubleBuffer.size.width,width);
            height=Math.max(doubleBuffer.size.height,height);
        }
        Image result=doubleBuffer.image;
        if(doubleBuffer.image==null){
            result=c.createImage(width,height);
            doubleBuffer.size=new Dimension(width,height);
            if(c instanceof JComponent){
                ((JComponent)c).setCreatedDoubleBuffer(true);
                doubleBuffer.image=result;
            }
            // JComponent will inform us when it is no longer valid
            // (via removeNotify) we have no such hook to other components,
            // therefore we don't keep a ref to the Component
            // (indirectly through the Image) by stashing the image.
        }
        return result;
    }

    public Dimension getDoubleBufferMaximumSize(){
        if(doubleBufferMaxSize==null){
            try{
                Rectangle virtualBounds=new Rectangle();
                GraphicsEnvironment ge=GraphicsEnvironment.
                        getLocalGraphicsEnvironment();
                for(GraphicsDevice gd : ge.getScreenDevices()){
                    GraphicsConfiguration gc=gd.getDefaultConfiguration();
                    virtualBounds=virtualBounds.union(gc.getBounds());
                }
                doubleBufferMaxSize=new Dimension(virtualBounds.width,
                        virtualBounds.height);
            }catch(HeadlessException e){
                doubleBufferMaxSize=new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
            }
        }
        return doubleBufferMaxSize;
    }

    public void setDoubleBufferMaximumSize(Dimension d){
        doubleBufferMaxSize=d;
        if(doubleBufferMaxSize==null){
            clearImages();
        }else{
            clearImages(d.width,d.height);
        }
    }

    public Image getVolatileOffscreenBuffer(Component c,
                                            int proposedWidth,int proposedHeight){
        RepaintManager delegate=getDelegate(c);
        if(delegate!=null){
            return delegate.getVolatileOffscreenBuffer(c,proposedWidth,
                    proposedHeight);
        }
        // If the window is non-opaque, it's double-buffered at peer's level
        Window w=(c instanceof Window)?(Window)c:SwingUtilities.getWindowAncestor(c);
        if(!w.isOpaque()){
            Toolkit tk=Toolkit.getDefaultToolkit();
            if((tk instanceof SunToolkit)&&(((SunToolkit)tk).needUpdateWindow())){
                return null;
            }
        }
        GraphicsConfiguration config=c.getGraphicsConfiguration();
        if(config==null){
            config=GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();
        }
        Dimension maxSize=getDoubleBufferMaximumSize();
        int width=proposedWidth<1?1:
                (proposedWidth>maxSize.width?maxSize.width:proposedWidth);
        int height=proposedHeight<1?1:
                (proposedHeight>maxSize.height?maxSize.height:proposedHeight);
        VolatileImage image=volatileMap.get(config);
        if(image==null||image.getWidth()<width||
                image.getHeight()<height){
            if(image!=null){
                image.flush();
            }
            image=config.createCompatibleVolatileImage(width,height,
                    volatileBufferType);
            volatileMap.put(config,image);
        }
        return image;
    }

    public boolean isDoubleBufferingEnabled(){
        return doubleBufferingEnabled;
    }

    public void setDoubleBufferingEnabled(boolean aFlag){
        doubleBufferingEnabled=aFlag;
        PaintManager paintManager=getPaintManager();
        if(!aFlag&&paintManager.getClass()!=PaintManager.class){
            setPaintManager(new PaintManager());
        }
    }

    private synchronized PaintManager getPaintManager(){
        if(paintManager==null){
            PaintManager paintManager=null;
            if(doubleBufferingEnabled&&!nativeDoubleBuffering){
                switch(bufferStrategyType){
                    case BUFFER_STRATEGY_NOT_SPECIFIED:
                        Toolkit tk=Toolkit.getDefaultToolkit();
                        if(tk instanceof SunToolkit){
                            SunToolkit stk=(SunToolkit)tk;
                            if(stk.useBufferPerWindow()){
                                paintManager=new BufferStrategyPaintManager();
                            }
                        }
                        break;
                    case BUFFER_STRATEGY_SPECIFIED_ON:
                        paintManager=new BufferStrategyPaintManager();
                        break;
                    default:
                        break;
                }
            }
            // null case handled in setPaintManager
            setPaintManager(paintManager);
        }
        return paintManager;
    }
    //
    // Paint methods.  You very, VERY rarely need to invoke these.
    // They are invoked directly from JComponent's painting code and
    // when painting happens outside the normal flow: DefaultDesktopManager
    // and JViewport.  If you end up needing these methods in other places be
    // careful that you don't get stuck in a paint loop.
    //

    void setPaintManager(PaintManager paintManager){
        if(paintManager==null){
            paintManager=new PaintManager();
        }
        PaintManager oldPaintManager;
        synchronized(this){
            oldPaintManager=this.paintManager;
            this.paintManager=paintManager;
            paintManager.repaintManager=this;
        }
        if(oldPaintManager!=null){
            oldPaintManager.dispose();
        }
    }

    void resetDoubleBuffer(){
        if(standardDoubleBuffer!=null){
            standardDoubleBuffer.needsReset=true;
        }
    }

    void resetVolatileDoubleBuffer(GraphicsConfiguration gc){
        Image image=volatileMap.remove(gc);
        if(image!=null){
            image.flush();
        }
    }

    boolean useVolatileDoubleBuffer(){
        return volatileImageBufferEnabled;
    }

    void paint(JComponent paintingComponent,
               JComponent bufferComponent,Graphics g,
               int x,int y,int w,int h){
        PaintManager paintManager=getPaintManager();
        if(!isPaintingThread()){
            // We're painting to two threads at once.  PaintManager deals
            // with this a bit better than BufferStrategyPaintManager, use
            // it to avoid possible exceptions/corruption.
            if(paintManager.getClass()!=PaintManager.class){
                paintManager=new PaintManager();
                paintManager.repaintManager=this;
            }
        }
        if(!paintManager.paint(paintingComponent,bufferComponent,g,
                x,y,w,h)){
            g.setClip(x,y,w,h);
            paintingComponent.paintToOffscreen(g,x,y,w,h,x+w,y+h);
        }
    }

    private synchronized boolean isPaintingThread(){
        return (Thread.currentThread()==paintThread);
    }

    void copyArea(JComponent c,Graphics g,int x,int y,int w,int h,
                  int deltaX,int deltaY,boolean clip){
        getPaintManager().copyArea(c,g,x,y,w,h,deltaX,deltaY,clip);
    }

    private void addRepaintListener(RepaintListener l){
        repaintListeners.add(l);
    }

    private void removeRepaintListener(RepaintListener l){
        repaintListeners.remove(l);
    }

    void notifyRepaintPerformed(JComponent c,int x,int y,int w,int h){
        for(RepaintListener l : repaintListeners){
            l.repaintPerformed(c,x,y,w,h);
        }
    }

    void beginPaint(){
        boolean multiThreadedPaint=false;
        int paintDepth;
        Thread currentThread=Thread.currentThread();
        synchronized(this){
            paintDepth=this.paintDepth;
            if(paintThread==null||currentThread==paintThread){
                paintThread=currentThread;
                this.paintDepth++;
            }else{
                multiThreadedPaint=true;
            }
        }
        if(!multiThreadedPaint&&paintDepth==0){
            getPaintManager().beginPaint();
        }
    }

    void endPaint(){
        if(isPaintingThread()){
            PaintManager paintManager=null;
            synchronized(this){
                if(--paintDepth==0){
                    paintManager=getPaintManager();
                }
            }
            if(paintManager!=null){
                paintManager.endPaint();
                synchronized(this){
                    paintThread=null;
                }
            }
        }
    }

    boolean show(Container c,int x,int y,int w,int h){
        return getPaintManager().show(c,x,y,w,h);
    }

    void doubleBufferingChanged(JRootPane rootPane){
        getPaintManager().doubleBufferingChanged(rootPane);
    }

    static class PaintManager{
        protected RepaintManager repaintManager;
        boolean isRepaintingRoot;

        public boolean paint(JComponent paintingComponent,
                             JComponent bufferComponent,Graphics g,
                             int x,int y,int w,int h){
            // First attempt to use VolatileImage buffer for performance.
            // If this fails (which should rarely occur), fallback to a
            // standard Image buffer.
            boolean paintCompleted=false;
            Image offscreen;
            if(repaintManager.useVolatileDoubleBuffer()&&
                    (offscreen=getValidImage(repaintManager.
                            getVolatileOffscreenBuffer(bufferComponent,w,h)))!=null){
                VolatileImage vImage=(VolatileImage)offscreen;
                GraphicsConfiguration gc=bufferComponent.
                        getGraphicsConfiguration();
                for(int i=0;!paintCompleted&&
                        i<RepaintManager.VOLATILE_LOOP_MAX;i++){
                    if(vImage.validate(gc)==
                            VolatileImage.IMAGE_INCOMPATIBLE){
                        repaintManager.resetVolatileDoubleBuffer(gc);
                        offscreen=repaintManager.getVolatileOffscreenBuffer(
                                bufferComponent,w,h);
                        vImage=(VolatileImage)offscreen;
                    }
                    paintDoubleBuffered(paintingComponent,vImage,g,x,y,
                            w,h);
                    paintCompleted=!vImage.contentsLost();
                }
            }
            // VolatileImage painting loop failed, fallback to regular
            // offscreen buffer
            if(!paintCompleted&&(offscreen=getValidImage(
                    repaintManager.getOffscreenBuffer(
                            bufferComponent,w,h)))!=null){
                paintDoubleBuffered(paintingComponent,offscreen,g,x,y,w,
                        h);
                paintCompleted=true;
            }
            return paintCompleted;
        }

        protected void paintDoubleBuffered(JComponent c,Image image,
                                           Graphics g,int clipX,int clipY,
                                           int clipW,int clipH){
            Graphics osg=image.getGraphics();
            int bw=Math.min(clipW,image.getWidth(null));
            int bh=Math.min(clipH,image.getHeight(null));
            int x, y, maxx, maxy;
            try{
                for(x=clipX,maxx=clipX+clipW;x<maxx;x+=bw){
                    for(y=clipY,maxy=clipY+clipH;y<maxy;y+=bh){
                        osg.translate(-x,-y);
                        osg.setClip(x,y,bw,bh);
                        if(volatileBufferType!=Transparency.OPAQUE
                                &&osg instanceof Graphics2D){
                            final Graphics2D g2d=(Graphics2D)osg;
                            final Color oldBg=g2d.getBackground();
                            g2d.setBackground(c.getBackground());
                            g2d.clearRect(x,y,bw,bh);
                            g2d.setBackground(oldBg);
                        }
                        c.paintToOffscreen(osg,x,y,bw,bh,maxx,maxy);
                        g.setClip(x,y,bw,bh);
                        if(volatileBufferType!=Transparency.OPAQUE
                                &&g instanceof Graphics2D){
                            final Graphics2D g2d=(Graphics2D)g;
                            final Composite oldComposite=g2d.getComposite();
                            g2d.setComposite(AlphaComposite.Src);
                            g2d.drawImage(image,x,y,c);
                            g2d.setComposite(oldComposite);
                        }else{
                            g.drawImage(image,x,y,c);
                        }
                        osg.translate(x,y);
                    }
                }
            }finally{
                osg.dispose();
            }
        }

        private Image getValidImage(Image image){
            if(image!=null&&image.getWidth(null)>0&&
                    image.getHeight(null)>0){
                return image;
            }
            return null;
        }

        public void copyArea(JComponent c,Graphics g,int x,int y,int w,
                             int h,int deltaX,int deltaY,boolean clip){
            g.copyArea(x,y,w,h,deltaX,deltaY);
        }

        public void beginPaint(){
        }

        public void endPaint(){
        }

        public boolean show(Container c,int x,int y,int w,int h){
            return false;
        }

        public void doubleBufferingChanged(JRootPane rootPane){
        }

        protected void repaintRoot(JComponent root){
            assert (repaintManager.repaintRoot==null);
            if(repaintManager.painting){
                repaintManager.repaintRoot=root;
            }else{
                root.repaint();
            }
        }

        protected boolean isRepaintingRoot(){
            return isRepaintingRoot;
        }

        protected void dispose(){
        }
    }

    private static final class DisplayChangedHandler implements
            DisplayChangedListener{
        // Empty non private constructor was added because access to this
        // class shouldn't be generated by the compiler using synthetic
        // accessor method
        DisplayChangedHandler(){
        }

        public void displayChanged(){
            scheduleDisplayChanges();
        }

        public void paletteChanged(){
        }

        private static void scheduleDisplayChanges(){
            // To avoid threading problems, we notify each RepaintManager
            // on the thread it was created on.
            for(AppContext context : AppContext.getAppContexts()){
                synchronized(context){
                    if(!context.isDisposed()){
                        EventQueue eventQueue=(EventQueue)context.get(
                                AppContext.EVENT_QUEUE_KEY);
                        if(eventQueue!=null){
                            eventQueue.postEvent(new InvocationEvent(
                                    Toolkit.getDefaultToolkit(),
                                    new DisplayChangedRunnable()));
                        }
                    }
                }
            }
        }
    }

    private static final class DisplayChangedRunnable implements Runnable{
        public void run(){
            RepaintManager.currentManager((JComponent)null).displayChanged();
        }
    }

    private class DoubleBufferInfo{
        public Image image;
        public Dimension size;
        public boolean needsReset=false;
    }

    private final class ProcessingRunnable implements Runnable{
        // If true, we're wainting on the EventQueue.
        private boolean pending;

        public synchronized boolean markPending(){
            if(!pending){
                pending=true;
                return true;
            }
            return false;
        }

        public void run(){
            synchronized(this){
                pending=false;
            }
            // First pass, flush any heavy paint events into real paint
            // events.  If there are pending heavy weight requests this will
            // result in q'ing this request up one more time.  As
            // long as no other requests come in between now and the time
            // the second one is processed nothing will happen.  This is not
            // ideal, but the logic needed to suppress the second request is
            // more headache than it's worth.
            scheduleHeavyWeightPaints();
            // Do the actual validation and painting.
            validateInvalidComponents();
            prePaintDirtyRegions();
        }
    }
}
