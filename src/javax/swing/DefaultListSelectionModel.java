/**
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.beans.Transient;
import java.io.Serializable;
import java.util.BitSet;
import java.util.EventListener;

public class DefaultListSelectionModel implements ListSelectionModel, Cloneable, Serializable{
    private static final int MIN=-1;
    private static final int MAX=Integer.MAX_VALUE;
    protected EventListenerList listenerList=new EventListenerList();
    protected boolean leadAnchorNotificationEnabled=true;
    private int selectionMode=MULTIPLE_INTERVAL_SELECTION;
    private int minIndex=MAX;
    private int maxIndex=MIN;
    private int anchorIndex=-1;
    private int leadIndex=-1;
    private int firstAdjustedIndex=MAX;
    private int lastAdjustedIndex=MIN;
    private boolean isAdjusting=false;
    private int firstChangedIndex=MAX;
    private int lastChangedIndex=MIN;
    private BitSet value=new BitSet(32);

    public ListSelectionListener[] getListSelectionListeners(){
        return listenerList.getListeners(ListSelectionListener.class);
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }

    public boolean isLeadAnchorNotificationEnabled(){
        return leadAnchorNotificationEnabled;
    }    public boolean getValueIsAdjusting(){
        return isAdjusting;
    }

    public void setLeadAnchorNotificationEnabled(boolean flag){
        leadAnchorNotificationEnabled=flag;
    }    public int getSelectionMode(){
        return selectionMode;
    }

    public void setSelectionInterval(int index0,int index1){
        if(index0==-1||index1==-1){
            return;
        }
        if(getSelectionMode()==SINGLE_SELECTION){
            index0=index1;
        }
        updateLeadAnchorIndices(index0,index1);
        int clearMin=minIndex;
        int clearMax=maxIndex;
        int setMin=Math.min(index0,index1);
        int setMax=Math.max(index0,index1);
        changeSelection(clearMin,clearMax,setMin,setMax);
    }    public void setSelectionMode(int selectionMode){
        switch(selectionMode){
            case SINGLE_SELECTION:
            case SINGLE_INTERVAL_SELECTION:
            case MULTIPLE_INTERVAL_SELECTION:
                this.selectionMode=selectionMode;
                break;
            default:
                throw new IllegalArgumentException("invalid selectionMode");
        }
    }

    public void addSelectionInterval(int index0,int index1){
        if(index0==-1||index1==-1){
            return;
        }
        // If we only allow a single selection, channel through
        // setSelectionInterval() to enforce the rule.
        if(getSelectionMode()==SINGLE_SELECTION){
            setSelectionInterval(index0,index1);
            return;
        }
        updateLeadAnchorIndices(index0,index1);
        int clearMin=MAX;
        int clearMax=MIN;
        int setMin=Math.min(index0,index1);
        int setMax=Math.max(index0,index1);
        // If we only allow a single interval and this would result
        // in multiple intervals, then set the selection to be just
        // the new range.
        if(getSelectionMode()==SINGLE_INTERVAL_SELECTION&&
                (setMax<minIndex-1||setMin>maxIndex+1)){
            setSelectionInterval(index0,index1);
            return;
        }
        changeSelection(clearMin,clearMax,setMin,setMax);
    }

    public void removeSelectionInterval(int index0,int index1){
        removeSelectionIntervalImpl(index0,index1,true);
    }

    public int getMinSelectionIndex(){
        return isSelectionEmpty()?-1:minIndex;
    }    public void addListSelectionListener(ListSelectionListener l){
        listenerList.add(ListSelectionListener.class,l);
    }

    public int getMaxSelectionIndex(){
        return maxIndex;
    }    public void removeListSelectionListener(ListSelectionListener l){
        listenerList.remove(ListSelectionListener.class,l);
    }

    public boolean isSelectedIndex(int index){
        return ((index<minIndex)||(index>maxIndex))?false:value.get(index);
    }

    @Transient
    public int getAnchorSelectionIndex(){
        return anchorIndex;
    }    protected void fireValueChanged(boolean isAdjusting){
        if(lastChangedIndex==MIN){
            return;
        }
        /** Change the values before sending the event to the
         * listeners in case the event causes a listener to make
         * another change to the selection.
         */
        int oldFirstChangedIndex=firstChangedIndex;
        int oldLastChangedIndex=lastChangedIndex;
        firstChangedIndex=MAX;
        lastChangedIndex=MIN;
        fireValueChanged(oldFirstChangedIndex,oldLastChangedIndex,isAdjusting);
    }

    public void setAnchorSelectionIndex(int anchorIndex){
        updateLeadAnchorIndices(anchorIndex,this.leadIndex);
        fireValueChanged();
    }

    @Transient
    public int getLeadSelectionIndex(){
        return leadIndex;
    }

    public void setLeadSelectionIndex(int leadIndex){
        int anchorIndex=this.anchorIndex;
        // only allow a -1 lead if the anchor is already -1
        if(leadIndex==-1){
            if(anchorIndex==-1){
                updateLeadAnchorIndices(anchorIndex,leadIndex);
                fireValueChanged();
            }
            return;
            // otherwise, don't do anything if the anchor is -1
        }else if(anchorIndex==-1){
            return;
        }
        if(this.leadIndex==-1){
            this.leadIndex=leadIndex;
        }
        boolean shouldSelect=value.get(this.anchorIndex);
        if(getSelectionMode()==SINGLE_SELECTION){
            anchorIndex=leadIndex;
            shouldSelect=true;
        }
        int oldMin=Math.min(this.anchorIndex,this.leadIndex);
        int oldMax=Math.max(this.anchorIndex,this.leadIndex);
        int newMin=Math.min(anchorIndex,leadIndex);
        int newMax=Math.max(anchorIndex,leadIndex);
        updateLeadAnchorIndices(anchorIndex,leadIndex);
        if(shouldSelect){
            changeSelection(oldMin,oldMax,newMin,newMax);
        }else{
            changeSelection(newMin,newMax,oldMin,oldMax,false);
        }
    }

    public void clearSelection(){
        removeSelectionIntervalImpl(minIndex,maxIndex,false);
    }

    public boolean isSelectionEmpty(){
        return (minIndex>maxIndex);
    }

    public void insertIndexInterval(int index,int length,boolean before){
        /** The first new index will appear at insMinIndex and the last
         * one will appear at insMaxIndex
         */
        int insMinIndex=(before)?index:index+1;
        int insMaxIndex=(insMinIndex+length)-1;
        /** Right shift the entire bitset by length, beginning with
         * index-1 if before is true, index+1 if it's false (i.e. with
         * insMinIndex).
         */
        for(int i=maxIndex;i>=insMinIndex;i--){
            setState(i+length,value.get(i));
        }
        /** Initialize the newly inserted indices.
         */
        boolean setInsertedValues=((getSelectionMode()==SINGLE_SELECTION)?
                false:value.get(index));
        for(int i=insMinIndex;i<=insMaxIndex;i++){
            setState(i,setInsertedValues);
        }
        int leadIndex=this.leadIndex;
        if(leadIndex>index||(before&&leadIndex==index)){
            leadIndex=this.leadIndex+length;
        }
        int anchorIndex=this.anchorIndex;
        if(anchorIndex>index||(before&&anchorIndex==index)){
            anchorIndex=this.anchorIndex+length;
        }
        if(leadIndex!=this.leadIndex||anchorIndex!=this.anchorIndex){
            updateLeadAnchorIndices(anchorIndex,leadIndex);
        }
        fireValueChanged();
    }

    private void setState(int index,boolean state){
        if(state){
            set(index);
        }else{
            clear(index);
        }
    }

    public void removeIndexInterval(int index0,int index1){
        int rmMinIndex=Math.min(index0,index1);
        int rmMaxIndex=Math.max(index0,index1);
        int gapLength=(rmMaxIndex-rmMinIndex)+1;
        /** Shift the entire bitset to the left to close the index0, index1
         * gap.
         */
        for(int i=rmMinIndex;i<=maxIndex;i++){
            setState(i,value.get(i+gapLength));
        }
        int leadIndex=this.leadIndex;
        if(leadIndex==0&&rmMinIndex==0){
            // do nothing
        }else if(leadIndex>rmMaxIndex){
            leadIndex=this.leadIndex-gapLength;
        }else if(leadIndex>=rmMinIndex){
            leadIndex=rmMinIndex-1;
        }
        int anchorIndex=this.anchorIndex;
        if(anchorIndex==0&&rmMinIndex==0){
            // do nothing
        }else if(anchorIndex>rmMaxIndex){
            anchorIndex=this.anchorIndex-gapLength;
        }else if(anchorIndex>=rmMinIndex){
            anchorIndex=rmMinIndex-1;
        }
        if(leadIndex!=this.leadIndex||anchorIndex!=this.anchorIndex){
            updateLeadAnchorIndices(anchorIndex,leadIndex);
        }
        fireValueChanged();
    }

    // private implementation allowing the selection interval
    // to be removed without affecting the lead and anchor
    private void removeSelectionIntervalImpl(int index0,int index1,
                                             boolean changeLeadAnchor){
        if(index0==-1||index1==-1){
            return;
        }
        if(changeLeadAnchor){
            updateLeadAnchorIndices(index0,index1);
        }
        int clearMin=Math.min(index0,index1);
        int clearMax=Math.max(index0,index1);
        int setMin=MAX;
        int setMax=MIN;
        // If the removal would produce to two disjoint selections in a mode
        // that only allows one, extend the removal to the end of the selection.
        if(getSelectionMode()!=MULTIPLE_INTERVAL_SELECTION&&
                clearMin>minIndex&&clearMax<maxIndex){
            clearMax=maxIndex;
        }
        changeSelection(clearMin,clearMax,setMin,setMax);
    }

    private void updateLeadAnchorIndices(int anchorIndex,int leadIndex){
        if(leadAnchorNotificationEnabled){
            if(this.anchorIndex!=anchorIndex){
                markAsDirty(this.anchorIndex);
                markAsDirty(anchorIndex);
            }
            if(this.leadIndex!=leadIndex){
                markAsDirty(this.leadIndex);
                markAsDirty(leadIndex);
            }
        }
        this.anchorIndex=anchorIndex;
        this.leadIndex=leadIndex;
    }

    // Updates first and last change indices
    private void markAsDirty(int r){
        if(r==-1){
            return;
        }
        firstAdjustedIndex=Math.min(firstAdjustedIndex,r);
        lastAdjustedIndex=Math.max(lastAdjustedIndex,r);
    }

    private void changeSelection(int clearMin,int clearMax,int setMin,int setMax){
        changeSelection(clearMin,clearMax,setMin,setMax,true);
    }

    private void changeSelection(int clearMin,int clearMax,
                                 int setMin,int setMax,boolean clearFirst){
        for(int i=Math.min(setMin,clearMin);i<=Math.max(setMax,clearMax);i++){
            boolean shouldClear=contains(clearMin,clearMax,i);
            boolean shouldSet=contains(setMin,setMax,i);
            if(shouldSet&&shouldClear){
                if(clearFirst){
                    shouldClear=false;
                }else{
                    shouldSet=false;
                }
            }
            if(shouldSet){
                set(i);
            }
            if(shouldClear){
                clear(i);
            }
        }
        fireValueChanged();
    }

    private void fireValueChanged(){
        if(lastAdjustedIndex==MIN){
            return;
        }
        /** If getValueAdjusting() is true, (eg. during a drag opereration)
         * record the bounds of the changes so that, when the drag finishes (and
         * setValueAdjusting(false) is called) we can post a single event
         * with bounds covering all of these individual adjustments.
         */
        if(getValueIsAdjusting()){
            firstChangedIndex=Math.min(firstChangedIndex,firstAdjustedIndex);
            lastChangedIndex=Math.max(lastChangedIndex,lastAdjustedIndex);
        }
        /** Change the values before sending the event to the
         * listeners in case the event causes a listener to make
         * another change to the selection.
         */
        int oldFirstAdjustedIndex=firstAdjustedIndex;
        int oldLastAdjustedIndex=lastAdjustedIndex;
        firstAdjustedIndex=MAX;
        lastAdjustedIndex=MIN;
        fireValueChanged(oldFirstAdjustedIndex,oldLastAdjustedIndex);
    }

    protected void fireValueChanged(int firstIndex,int lastIndex){
        fireValueChanged(firstIndex,lastIndex,getValueIsAdjusting());
    }

    protected void fireValueChanged(int firstIndex,int lastIndex,boolean isAdjusting){
        Object[] listeners=listenerList.getListenerList();
        ListSelectionEvent e=null;
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==ListSelectionListener.class){
                if(e==null){
                    e=new ListSelectionEvent(this,firstIndex,lastIndex,isAdjusting);
                }
                ((ListSelectionListener)listeners[i+1]).valueChanged(e);
            }
        }
    }

    // Sets the state at this index and update all relevant state.
    private void set(int r){
        if(value.get(r)){
            return;
        }
        value.set(r);
        markAsDirty(r);
        // Update minimum and maximum indices
        minIndex=Math.min(minIndex,r);
        maxIndex=Math.max(maxIndex,r);
    }

    // Clears the state at this index and update all relevant state.
    private void clear(int r){
        if(!value.get(r)){
            return;
        }
        value.clear(r);
        markAsDirty(r);
        // Update minimum and maximum indices
        /**
         If (r > minIndex) the minimum has not changed.
         The case (r < minIndex) is not possible because r'th value was set.
         We only need to check for the case when lowest entry has been cleared,
         and in this case we need to search for the first value set above it.
         */
        if(r==minIndex){
            for(minIndex=minIndex+1;minIndex<=maxIndex;minIndex++){
                if(value.get(minIndex)){
                    break;
                }
            }
        }
        /**
         If (r < maxIndex) the maximum has not changed.
         The case (r > maxIndex) is not possible because r'th value was set.
         We only need to check for the case when highest entry has been cleared,
         and in this case we need to search for the first value set below it.
         */
        if(r==maxIndex){
            for(maxIndex=maxIndex-1;minIndex<=maxIndex;maxIndex--){
                if(value.get(maxIndex)){
                    break;
                }
            }
        }
        /** Performance note: This method is called from inside a loop in
         changeSelection() but we will only iterate in the loops
         above on the basis of one iteration per deselected cell - in total.
         Ie. the next time this method is called the work of the previous
         deselection will not be repeated.

         We also don't need to worry about the case when the min and max
         values are in their unassigned states. This cannot happen because
         this method's initial check ensures that the selection was not empty
         and therefore that the minIndex and maxIndex had 'real' values.

         If we have cleared the whole selection, set the minIndex and maxIndex
         to their cannonical values so that the next set command always works
         just by using Math.min and Math.max.
         */
        if(isSelectionEmpty()){
            minIndex=MAX;
            maxIndex=MIN;
        }
    }

    private boolean contains(int a,int b,int i){
        return (i>=a)&&(i<=b);
    }

    public Object clone() throws CloneNotSupportedException{
        DefaultListSelectionModel clone=(DefaultListSelectionModel)super.clone();
        clone.value=(BitSet)value.clone();
        clone.listenerList=new EventListenerList();
        return clone;
    }

    public String toString(){
        String s=((getValueIsAdjusting())?"~":"=")+value.toString();
        return getClass().getName()+" "+Integer.toString(hashCode())+" "+s;
    }

    public void moveLeadSelectionIndex(int leadIndex){
        // disallow a -1 lead unless the anchor is already -1
        if(leadIndex==-1){
            if(this.anchorIndex!=-1){
                return;
            }
/** PENDING(shannonh) - The following check is nice, to be consistent with
 setLeadSelectionIndex. However, it is not absolutely
 necessary: One could work around it by setting the anchor
 to something valid, modifying the lead, and then moving
 the anchor back to -1. For this reason, there's no sense
 in adding it at this time, as that would require
 updating the spec and officially committing to it.

 // otherwise, don't do anything if the anchor is -1
 } else if (this.anchorIndex == -1) {
 return;
 */
        }
        updateLeadAnchorIndices(this.anchorIndex,leadIndex);
        fireValueChanged();
    }    public void setValueIsAdjusting(boolean isAdjusting){
        if(isAdjusting!=this.isAdjusting){
            this.isAdjusting=isAdjusting;
            this.fireValueChanged(isAdjusting);
        }
    }














}
