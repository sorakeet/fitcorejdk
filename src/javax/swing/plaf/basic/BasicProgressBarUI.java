/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.plaf.basic;

import sun.swing.DefaultLookup;
import sun.swing.SwingUtilities2;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ProgressBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class BasicProgressBarUI extends ProgressBarUI{
    //performance stuff
    private static boolean ADJUSTTIMER=true; //makes a BIG difference;
    protected JProgressBar progressBar;
    protected ChangeListener changeListener;
    //make this false for
    //performance tests
    protected Rectangle boxRect;
    private int cachedPercent;
    private int cellLength, cellSpacing;
    // The "selectionForeground" is the color of the text when it is painted
    // over a filled area of the progress bar. The "selectionBackground"
    // is for the text over the unfilled progress bar area.
    private Color selectionForeground, selectionBackground;
    private Animator animator;
    private Handler handler;
    private int animationIndex=0;
    private int numFrames;   //0 1|numFrames-1 ... numFrames/2
    private int repaintInterval;
    private int cycleTime;  //must be repaintInterval*2*aPositiveInteger
    private Rectangle nextPaintRect;
    //cache
    private Rectangle componentInnards;    //the current painting area
    private Rectangle oldComponentInnards; //used to see if the size changed
    private double delta=0.0;
    private int maxPosition=0; //maximum X (horiz) or Y box location

    public static ComponentUI createUI(JComponent x){
        return new BasicProgressBarUI();
    }

    public void installUI(JComponent c){
        progressBar=(JProgressBar)c;
        installDefaults();
        installListeners();
        if(progressBar.isIndeterminate()){
            initIndeterminateValues();
        }
    }

    public void uninstallUI(JComponent c){
        if(progressBar.isIndeterminate()){
            cleanUpIndeterminateValues();
        }
        uninstallDefaults();
        uninstallListeners();
        progressBar=null;
    }

    protected void uninstallDefaults(){
        LookAndFeel.uninstallBorder(progressBar);
    }

    protected void uninstallListeners(){
        progressBar.removeChangeListener(changeListener);
        progressBar.removePropertyChangeListener(getHandler());
        handler=null;
    }

    public void paint(Graphics g,JComponent c){
        if(progressBar.isIndeterminate()){
            paintIndeterminate(g,c);
        }else{
            paintDeterminate(g,c);
        }
    }

    public Dimension getPreferredSize(JComponent c){
        Dimension size;
        Insets border=progressBar.getInsets();
        FontMetrics fontSizer=progressBar.getFontMetrics(
                progressBar.getFont());
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            size=new Dimension(getPreferredInnerHorizontal());
            // Ensure that the progress string will fit
            if(progressBar.isStringPainted()){
                // I'm doing this for completeness.
                String progString=progressBar.getString();
                int stringWidth=SwingUtilities2.stringWidth(
                        progressBar,fontSizer,progString);
                if(stringWidth>size.width){
                    size.width=stringWidth;
                }
                // This uses both Height and Descent to be sure that
                // there is more than enough room in the progress bar
                // for everything.
                // This does have a strange dependency on
                // getStringPlacememnt() in a funny way.
                int stringHeight=fontSizer.getHeight()+
                        fontSizer.getDescent();
                if(stringHeight>size.height){
                    size.height=stringHeight;
                }
            }
        }else{
            size=new Dimension(getPreferredInnerVertical());
            // Ensure that the progress string will fit.
            if(progressBar.isStringPainted()){
                String progString=progressBar.getString();
                int stringHeight=fontSizer.getHeight()+
                        fontSizer.getDescent();
                if(stringHeight>size.width){
                    size.width=stringHeight;
                }
                // This is also for completeness.
                int stringWidth=SwingUtilities2.stringWidth(
                        progressBar,fontSizer,progString);
                if(stringWidth>size.height){
                    size.height=stringWidth;
                }
            }
        }
        size.width+=border.left+border.right;
        size.height+=border.top+border.bottom;
        return size;
    }

    protected Dimension getPreferredInnerHorizontal(){
        Dimension horizDim=(Dimension)DefaultLookup.get(progressBar,this,
                "ProgressBar.horizontalSize");
        if(horizDim==null){
            horizDim=new Dimension(146,12);
        }
        return horizDim;
    }

    protected Dimension getPreferredInnerVertical(){
        Dimension vertDim=(Dimension)DefaultLookup.get(progressBar,this,
                "ProgressBar.verticalSize");
        if(vertDim==null){
            vertDim=new Dimension(12,146);
        }
        return vertDim;
    }

    public Dimension getMinimumSize(JComponent c){
        Dimension pref=getPreferredSize(progressBar);
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            pref.width=10;
        }else{
            pref.height=10;
        }
        return pref;
    }

    public Dimension getMaximumSize(JComponent c){
        Dimension pref=getPreferredSize(progressBar);
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            pref.width=Short.MAX_VALUE;
        }else{
            pref.height=Short.MAX_VALUE;
        }
        return pref;
    }

    public int getBaseline(JComponent c,int width,int height){
        super.getBaseline(c,width,height);
        if(progressBar.isStringPainted()&&
                progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            FontMetrics metrics=progressBar.
                    getFontMetrics(progressBar.getFont());
            Insets insets=progressBar.getInsets();
            int y=insets.top;
            height=height-insets.top-insets.bottom;
            return y+(height+metrics.getAscent()-
                    metrics.getLeading()-
                    metrics.getDescent())/2;
        }
        return -1;
    }
    // Many of the Basic*UI components have the following methods.
    // This component does not have these methods because *ProgressBarUI
    //  is not a compound component and does not accept input.
    //
    // protected void installComponents()
    // protected void uninstallComponents()
    // protected void installKeyboardActions()
    // protected void uninstallKeyboardActions()

    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c){
        super.getBaselineResizeBehavior(c);
        if(progressBar.isStringPainted()&&
                progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            return Component.BaselineResizeBehavior.CENTER_OFFSET;
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    private void cleanUpIndeterminateValues(){
        // stop the animation thread if necessary
        if(progressBar.isDisplayable()){
            stopAnimationTimer();
        }
        cycleTime=repaintInterval=0;
        numFrames=animationIndex=0;
        maxPosition=0;
        delta=0.0;
        boxRect=nextPaintRect=null;
        componentInnards=oldComponentInnards=null;
        progressBar.removeHierarchyListener(getHandler());
    }

    protected void stopAnimationTimer(){
        if(animator!=null){
            animator.stop();
        }
    }

    protected void installDefaults(){
        LookAndFeel.installProperty(progressBar,"opaque",Boolean.TRUE);
        LookAndFeel.installBorder(progressBar,"ProgressBar.border");
        LookAndFeel.installColorsAndFont(progressBar,
                "ProgressBar.background",
                "ProgressBar.foreground",
                "ProgressBar.font");
        cellLength=UIManager.getInt("ProgressBar.cellLength");
        if(cellLength==0) cellLength=1;
        cellSpacing=UIManager.getInt("ProgressBar.cellSpacing");
        selectionForeground=UIManager.getColor("ProgressBar.selectionForeground");
        selectionBackground=UIManager.getColor("ProgressBar.selectionBackground");
    }

    protected void installListeners(){
        //Listen for changes in the progress bar's data.
        changeListener=getHandler();
        progressBar.addChangeListener(changeListener);
        //Listen for changes between determinate and indeterminate state.
        progressBar.addPropertyChangeListener(getHandler());
    }

    private Handler getHandler(){
        if(handler==null){
            handler=new Handler();
        }
        return handler;
    }

    private void initIndeterminateValues(){
        initIndeterminateDefaults();
        //assert cycleTime/repaintInterval is a whole multiple of 2.
        numFrames=cycleTime/repaintInterval;
        initAnimationIndex();
        boxRect=new Rectangle();
        nextPaintRect=new Rectangle();
        componentInnards=new Rectangle();
        oldComponentInnards=new Rectangle();
        // we only bother installing the HierarchyChangeListener if we
        // are indeterminate
        progressBar.addHierarchyListener(getHandler());
        // start the animation thread if necessary
        if(progressBar.isDisplayable()){
            startAnimationTimer();
        }
    }

    protected void startAnimationTimer(){
        if(animator==null){
            animator=new Animator();
        }
        animator.start(getRepaintInterval());
    }

    private int getRepaintInterval(){
        return repaintInterval;
    }

    private void initIndeterminateDefaults(){
        initRepaintInterval(); //initialize repaint interval
        initCycleTime();       //initialize cycle length
        // Make sure repaintInterval is reasonable.
        if(repaintInterval<=0){
            repaintInterval=100;
        }
        // Make sure cycleTime is reasonable.
        if(repaintInterval>cycleTime){
            cycleTime=repaintInterval*20;
        }else{
            // Force cycleTime to be a even multiple of repaintInterval.
            int factor=(int)Math.ceil(
                    ((double)cycleTime)
                            /((double)repaintInterval*2));
            cycleTime=repaintInterval*factor*2;
        }
    }

    private int initRepaintInterval(){
        repaintInterval=DefaultLookup.getInt(progressBar,
                this,"ProgressBar.repaintInterval",50);
        return repaintInterval;
    }

    private int initCycleTime(){
        cycleTime=DefaultLookup.getInt(progressBar,this,
                "ProgressBar.cycleTime",3000);
        return cycleTime;
    }

    // Called from initIndeterminateValues to initialize the animation index.
    // This assumes that numFrames is set to a correct value.
    private void initAnimationIndex(){
        if((progressBar.getOrientation()==JProgressBar.HORIZONTAL)&&
                (BasicGraphicsUtils.isLeftToRight(progressBar))){
            // If this is a left-to-right progress bar,
            // start at the first frame.
            setAnimationIndex(0);
        }else{
            // If we go right-to-left or vertically, start at the right/bottom.
            setAnimationIndex(numFrames/2);
        }
    }    protected Rectangle getBox(Rectangle r){
        int currentFrame=getAnimationIndex();
        int middleFrame=numFrames/2;
        if(sizeChanged()||delta==0.0||maxPosition==0.0){
            updateSizes();
        }
        r=getGenericBox(r);
        if(r==null){
            return null;
        }
        if(middleFrame<=0){
            return null;
        }
        //assert currentFrame >= 0 && currentFrame < numFrames
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            if(currentFrame<middleFrame){
                r.x=componentInnards.x
                        +(int)Math.round(delta*(double)currentFrame);
            }else{
                r.x=maxPosition
                        -(int)Math.round(delta*
                        (currentFrame-middleFrame));
            }
        }else{ //VERTICAL indeterminate progress bar
            if(currentFrame<middleFrame){
                r.y=componentInnards.y
                        +(int)Math.round(delta*currentFrame);
            }else{
                r.y=maxPosition
                        -(int)Math.round(delta*
                        (currentFrame-middleFrame));
            }
        }
        return r;
    }

    protected Color getSelectionForeground(){
        return selectionForeground;
    }    private void updateSizes(){
        int length=0;
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            length=getBoxLength(componentInnards.width,
                    componentInnards.height);
            maxPosition=componentInnards.x+componentInnards.width
                    -length;
        }else{ //VERTICAL progress bar
            length=getBoxLength(componentInnards.height,
                    componentInnards.width);
            maxPosition=componentInnards.y+componentInnards.height
                    -length;
        }
        //If we're doing bouncing-box animation, update delta.
        delta=2.0*(double)maxPosition/(double)numFrames;
    }

    protected Color getSelectionBackground(){
        return selectionBackground;
    }    private Rectangle getGenericBox(Rectangle r){
        if(r==null){
            r=new Rectangle();
        }
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            r.width=getBoxLength(componentInnards.width,
                    componentInnards.height);
            if(r.width<0){
                r=null;
            }else{
                r.height=componentInnards.height;
                r.y=componentInnards.y;
            }
            // end of HORIZONTAL
        }else{ //VERTICAL progress bar
            r.height=getBoxLength(componentInnards.height,
                    componentInnards.width);
            if(r.height<0){
                r=null;
            }else{
                r.width=componentInnards.width;
                r.x=componentInnards.x;
            }
        } // end of VERTICAL
        return r;
    }

    private int getCachedPercent(){
        return cachedPercent;
    }    protected int getBoxLength(int availableLength,int otherDimension){
        return (int)Math.round(availableLength/6.0);
    }

    private void setCachedPercent(int cachedPercent){
        this.cachedPercent=cachedPercent;
    }

    protected int getCellLength(){
        if(progressBar.isStringPainted()){
            return 1;
        }else{
            return cellLength;
        }
    }

    protected void setCellLength(int cellLen){
        this.cellLength=cellLen;
    }

    protected int getCellSpacing(){
        if(progressBar.isStringPainted()){
            return 0;
        }else{
            return cellSpacing;
        }
    }

    protected void setCellSpacing(int cellSpace){
        this.cellSpacing=cellSpace;
    }

    protected int getAmountFull(Insets b,int width,int height){
        int amountFull=0;
        BoundedRangeModel model=progressBar.getModel();
        if((model.getMaximum()-model.getMinimum())!=0){
            if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
                amountFull=(int)Math.round(width*
                        progressBar.getPercentComplete());
            }else{
                amountFull=(int)Math.round(height*
                        progressBar.getPercentComplete());
            }
        }
        return amountFull;
    }

    protected void paintIndeterminate(Graphics g,JComponent c){
        if(!(g instanceof Graphics2D)){
            return;
        }
        Insets b=progressBar.getInsets(); // area for border
        int barRectWidth=progressBar.getWidth()-(b.right+b.left);
        int barRectHeight=progressBar.getHeight()-(b.top+b.bottom);
        if(barRectWidth<=0||barRectHeight<=0){
            return;
        }
        Graphics2D g2=(Graphics2D)g;
        // Paint the bouncing box.
        boxRect=getBox(boxRect);
        if(boxRect!=null){
            g2.setColor(progressBar.getForeground());
            g2.fillRect(boxRect.x,boxRect.y,
                    boxRect.width,boxRect.height);
        }
        // Deal with possible text painting
        if(progressBar.isStringPainted()){
            if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
                paintString(g2,b.left,b.top,
                        barRectWidth,barRectHeight,
                        boxRect.x,boxRect.width,b);
            }else{
                paintString(g2,b.left,b.top,
                        barRectWidth,barRectHeight,
                        boxRect.y,boxRect.height,b);
            }
        }
    }

    protected void paintDeterminate(Graphics g,JComponent c){
        if(!(g instanceof Graphics2D)){
            return;
        }
        Insets b=progressBar.getInsets(); // area for border
        int barRectWidth=progressBar.getWidth()-(b.right+b.left);
        int barRectHeight=progressBar.getHeight()-(b.top+b.bottom);
        if(barRectWidth<=0||barRectHeight<=0){
            return;
        }
        int cellLength=getCellLength();
        int cellSpacing=getCellSpacing();
        // amount of progress to draw
        int amountFull=getAmountFull(b,barRectWidth,barRectHeight);
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(progressBar.getForeground());
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            // draw the cells
            if(cellSpacing==0&&amountFull>0){
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float)barRectHeight,
                        BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
            }else{
                // draw each individual cell
                g2.setStroke(new BasicStroke((float)barRectHeight,
                        BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
                        0.f,new float[]{cellLength,cellSpacing},0.f));
            }
            if(BasicGraphicsUtils.isLeftToRight(c)){
                g2.drawLine(b.left,(barRectHeight/2)+b.top,
                        amountFull+b.left,(barRectHeight/2)+b.top);
            }else{
                g2.drawLine((barRectWidth+b.left),
                        (barRectHeight/2)+b.top,
                        barRectWidth+b.left-amountFull,
                        (barRectHeight/2)+b.top);
            }
        }else{ // VERTICAL
            // draw the cells
            if(cellSpacing==0&&amountFull>0){
                // draw one big Rect because there is no space between cells
                g2.setStroke(new BasicStroke((float)barRectWidth,
                        BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
            }else{
                // draw each individual cell
                g2.setStroke(new BasicStroke((float)barRectWidth,
                        BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,
                        0f,new float[]{cellLength,cellSpacing},0f));
            }
            g2.drawLine(barRectWidth/2+b.left,
                    b.top+barRectHeight,
                    barRectWidth/2+b.left,
                    b.top+barRectHeight-amountFull);
        }
        // Deal with possible text painting
        if(progressBar.isStringPainted()){
            paintString(g,b.left,b.top,
                    barRectWidth,barRectHeight,
                    amountFull,b);
        }
    }

    protected void paintString(Graphics g,int x,int y,
                               int width,int height,
                               int amountFull,Insets b){
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            if(BasicGraphicsUtils.isLeftToRight(progressBar)){
                if(progressBar.isIndeterminate()){
                    boxRect=getBox(boxRect);
                    paintString(g,x,y,width,height,
                            boxRect.x,boxRect.width,b);
                }else{
                    paintString(g,x,y,width,height,x,amountFull,b);
                }
            }else{
                paintString(g,x,y,width,height,x+width-amountFull,
                        amountFull,b);
            }
        }else{
            if(progressBar.isIndeterminate()){
                boxRect=getBox(boxRect);
                paintString(g,x,y,width,height,
                        boxRect.y,boxRect.height,b);
            }else{
                paintString(g,x,y,width,height,y+height-amountFull,
                        amountFull,b);
            }
        }
    }    protected int getAnimationIndex(){
        return animationIndex;
    }

    private void paintString(Graphics g,int x,int y,int width,int height,
                             int fillStart,int amountFull,Insets b){
        if(!(g instanceof Graphics2D)){
            return;
        }
        Graphics2D g2=(Graphics2D)g;
        String progressString=progressBar.getString();
        g2.setFont(progressBar.getFont());
        Point renderLocation=getStringPlacement(g2,progressString,
                x,y,width,height);
        Rectangle oldClip=g2.getClipBounds();
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            g2.setColor(getSelectionBackground());
            SwingUtilities2.drawString(progressBar,g2,progressString,
                    renderLocation.x,renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(fillStart,y,amountFull,height);
            SwingUtilities2.drawString(progressBar,g2,progressString,
                    renderLocation.x,renderLocation.y);
        }else{ // VERTICAL
            g2.setColor(getSelectionBackground());
            AffineTransform rotate=
                    AffineTransform.getRotateInstance(Math.PI/2);
            g2.setFont(progressBar.getFont().deriveFont(rotate));
            renderLocation=getStringPlacement(g2,progressString,
                    x,y,width,height);
            SwingUtilities2.drawString(progressBar,g2,progressString,
                    renderLocation.x,renderLocation.y);
            g2.setColor(getSelectionForeground());
            g2.clipRect(x,fillStart,width,amountFull);
            SwingUtilities2.drawString(progressBar,g2,progressString,
                    renderLocation.x,renderLocation.y);
        }
        g2.setClip(oldClip);
    }

    protected Point getStringPlacement(Graphics g,String progressString,
                                       int x,int y,int width,int height){
        FontMetrics fontSizer=SwingUtilities2.getFontMetrics(progressBar,g,
                progressBar.getFont());
        int stringWidth=SwingUtilities2.stringWidth(progressBar,fontSizer,
                progressString);
        if(progressBar.getOrientation()==JProgressBar.HORIZONTAL){
            return new Point(x+Math.round(width/2-stringWidth/2),
                    y+((height+
                            fontSizer.getAscent()-
                            fontSizer.getLeading()-
                            fontSizer.getDescent())/2));
        }else{ // VERTICAL
            return new Point(x+((width-fontSizer.getAscent()+
                    fontSizer.getLeading()+fontSizer.getDescent())/2),
                    y+Math.round(height/2-stringWidth/2));
        }
    }    protected void setAnimationIndex(int newValue){
        if(animationIndex!=newValue){
            if(sizeChanged()){
                animationIndex=newValue;
                maxPosition=0;  //needs to be recalculated
                delta=0.0;      //needs to be recalculated
                progressBar.repaint();
                return;
            }
            //Get the previous box drawn.
            nextPaintRect=getBox(nextPaintRect);
            //Update the frame number.
            animationIndex=newValue;
            //Get the next box to draw.
            if(nextPaintRect!=null){
                boxRect=getBox(boxRect);
                if(boxRect!=null){
                    nextPaintRect.add(boxRect);
                }
            }
        }else{ //animationIndex == newValue
            return;
        }
        if(nextPaintRect!=null){
            progressBar.repaint(nextPaintRect);
        }else{
            progressBar.repaint();
        }
    }

    protected final int getFrameCount(){
        return numFrames;
    }    private boolean sizeChanged(){
        if((oldComponentInnards==null)||(componentInnards==null)){
            return true;
        }
        oldComponentInnards.setRect(componentInnards);
        componentInnards=SwingUtilities.calculateInnerArea(progressBar,
                componentInnards);
        return !oldComponentInnards.equals(componentInnards);
    }

    protected void incrementAnimationIndex(){
        int newValue=getAnimationIndex()+1;
        if(newValue<numFrames){
            setAnimationIndex(newValue);
        }else{
            setAnimationIndex(0);
        }
    }

    private int getCycleTime(){
        return cycleTime;
    }

    //
    // Animation Thread
    //
    private class Animator implements ActionListener{
        private Timer timer;
        private long previousDelay; //used to tune the repaint interval
        private int interval; //the fixed repaint interval
        private long lastCall; //the last time actionPerformed was called
        private int MINIMUM_DELAY=5;

        private void start(int interval){
            previousDelay=interval;
            lastCall=0;
            if(timer==null){
                timer=new Timer(interval,this);
            }else{
                timer.setDelay(interval);
            }
            if(ADJUSTTIMER){
                timer.setRepeats(false);
                timer.setCoalesce(false);
            }
            timer.start();
        }

        private void stop(){
            timer.stop();
        }

        public void actionPerformed(ActionEvent e){
            if(ADJUSTTIMER){
                long time=System.currentTimeMillis();
                if(lastCall>0){ //adjust nextDelay
                    //XXX maybe should cache this after a while
                    //actual = time - lastCall
                    //difference = actual - interval
                    //nextDelay = previousDelay - difference
                    //          = previousDelay - (time - lastCall - interval)
                    int nextDelay=(int)(previousDelay
                            -time+lastCall
                            +getRepaintInterval());
                    if(nextDelay<MINIMUM_DELAY){
                        nextDelay=MINIMUM_DELAY;
                    }
                    timer.setInitialDelay(nextDelay);
                    previousDelay=nextDelay;
                }
                timer.start();
                lastCall=time;
            }
            incrementAnimationIndex(); //paint next frame
        }
    }

    public class ChangeHandler implements ChangeListener{
        // NOTE: This class exists only for backward compatibility. All
        // its functionality has been moved into Handler. If you need to add
        // new functionality add it to the Handler, but make sure this
        // class calls into the Handler.
        public void stateChanged(ChangeEvent e){
            getHandler().stateChanged(e);
        }
    }

    private class Handler implements ChangeListener, PropertyChangeListener, HierarchyListener{
        // PropertyChangeListener
        public void propertyChange(PropertyChangeEvent e){
            String prop=e.getPropertyName();
            if("indeterminate"==prop){
                if(progressBar.isIndeterminate()){
                    initIndeterminateValues();
                }else{
                    //clean up
                    cleanUpIndeterminateValues();
                }
                progressBar.repaint();
            }
        }        // ChangeListener
        public void stateChanged(ChangeEvent e){
            BoundedRangeModel model=progressBar.getModel();
            int newRange=model.getMaximum()-model.getMinimum();
            int newPercent;
            int oldPercent=getCachedPercent();
            if(newRange>0){
                newPercent=(int)((100*(long)model.getValue())/newRange);
            }else{
                newPercent=0;
            }
            if(newPercent!=oldPercent){
                setCachedPercent(newPercent);
                progressBar.repaint();
            }
        }

        // we don't want the animation to keep running if we're not displayable
        public void hierarchyChanged(HierarchyEvent he){
            if((he.getChangeFlags()&HierarchyEvent.DISPLAYABILITY_CHANGED)!=0){
                if(progressBar.isIndeterminate()){
                    if(progressBar.isDisplayable()){
                        startAnimationTimer();
                    }else{
                        stopAnimationTimer();
                    }
                }
            }
        }


    }














}
