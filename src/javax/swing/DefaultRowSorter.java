/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

import java.text.Collator;
import java.util.*;

public abstract class DefaultRowSorter<M,I> extends RowSorter<M>{
    private boolean sortsOnUpdates;
    private Row[] viewToModel;
    private int[] modelToView;
    private Comparator[] comparators;
    private boolean[] isSortable;
    private SortKey[] cachedSortKeys;
    private Comparator[] sortComparators;
    private RowFilter<? super M,? super I> filter;
    private FilterEntry filterEntry;
    private List<SortKey> sortKeys;
    private boolean[] useToString;
    private boolean sorted;
    private int maxSortKeys;
    private ModelWrapper<M,I> modelWrapper;
    private int modelRowCount;

    public DefaultRowSorter(){
        sortKeys=Collections.emptyList();
        maxSortKeys=3;
    }

    public final M getModel(){
        return getModelWrapper().getModel();
    }

    protected final ModelWrapper<M,I> getModelWrapper(){
        return modelWrapper;
    }

    protected final void setModelWrapper(ModelWrapper<M,I> modelWrapper){
        if(modelWrapper==null){
            throw new IllegalArgumentException(
                    "modelWrapper most be non-null");
        }
        ModelWrapper<M,I> last=this.modelWrapper;
        this.modelWrapper=modelWrapper;
        if(last!=null){
            modelStructureChanged();
        }else{
            // If last is null, we're in the constructor. If we're in
            // the constructor we don't want to call to overridable methods.
            modelRowCount=getModelWrapper().getRowCount();
        }
    }

    public void toggleSortOrder(int column){
        checkColumn(column);
        if(isSortable(column)){
            List<SortKey> keys=new ArrayList<SortKey>(getSortKeys());
            SortKey sortKey;
            int sortIndex;
            for(sortIndex=keys.size()-1;sortIndex>=0;sortIndex--){
                if(keys.get(sortIndex).getColumn()==column){
                    break;
                }
            }
            if(sortIndex==-1){
                // Key doesn't exist
                sortKey=new SortKey(column,SortOrder.ASCENDING);
                keys.add(0,sortKey);
            }else if(sortIndex==0){
                // It's the primary sorting key, toggle it
                keys.set(0,toggle(keys.get(0)));
            }else{
                // It's not the first, but was sorted on, remove old
                // entry, insert as first with ascending.
                keys.remove(sortIndex);
                keys.add(0,new SortKey(column,SortOrder.ASCENDING));
            }
            if(keys.size()>getMaxSortKeys()){
                keys=keys.subList(0,getMaxSortKeys());
            }
            setSortKeys(keys);
        }
    }

    public int convertRowIndexToModel(int index){
        if(viewToModel==null){
            if(index<0||index>=getModelWrapper().getRowCount()){
                throw new IndexOutOfBoundsException("Invalid index");
            }
            return index;
        }
        return viewToModel[index].modelIndex;
    }

    public int convertRowIndexToView(int index){
        if(modelToView==null){
            if(index<0||index>=getModelWrapper().getRowCount()){
                throw new IndexOutOfBoundsException("Invalid index");
            }
            return index;
        }
        return modelToView[index];
    }    public void setSortKeys(List<? extends SortKey> sortKeys){
        List<SortKey> old=this.sortKeys;
        if(sortKeys!=null&&sortKeys.size()>0){
            int max=getModelWrapper().getColumnCount();
            for(SortKey key : sortKeys){
                if(key==null||key.getColumn()<0||
                        key.getColumn()>=max){
                    throw new IllegalArgumentException("Invalid SortKey");
                }
            }
            this.sortKeys=Collections.unmodifiableList(
                    new ArrayList<SortKey>(sortKeys));
        }else{
            this.sortKeys=Collections.emptyList();
        }
        if(!this.sortKeys.equals(old)){
            fireSortOrderChanged();
            if(viewToModel==null){
                // Currently unsorted, use sort so that internal fields
                // are correctly set.
                sort();
            }else{
                sortExistingData();
            }
        }
    }

    public void setSortable(int column,boolean sortable){
        checkColumn(column);
        if(isSortable==null){
            isSortable=new boolean[getModelWrapper().getColumnCount()];
            for(int i=isSortable.length-1;i>=0;i--){
                isSortable[i]=true;
            }
        }
        isSortable[column]=sortable;
    }    public List<? extends SortKey> getSortKeys(){
        return sortKeys;
    }

    private void checkColumn(int column){
        if(column<0||column>=getModelWrapper().getColumnCount()){
            throw new IndexOutOfBoundsException(
                    "column beyond range of TableModel");
        }
    }

    public boolean isSortable(int column){
        checkColumn(column);
        return (isSortable==null)?true:isSortable[column];
    }

    public int getMaxSortKeys(){
        return maxSortKeys;
    }

    public void setMaxSortKeys(int max){
        if(max<1){
            throw new IllegalArgumentException("Invalid max");
        }
        maxSortKeys=max;
    }

    public boolean getSortsOnUpdates(){
        return sortsOnUpdates;
    }

    public void setSortsOnUpdates(boolean sortsOnUpdates){
        this.sortsOnUpdates=sortsOnUpdates;
    }

    public RowFilter<? super M,? super I> getRowFilter(){
        return filter;
    }

    public void setRowFilter(RowFilter<? super M,? super I> filter){
        this.filter=filter;
        sort();
    }

    private SortKey toggle(SortKey key){
        if(key.getSortOrder()==SortOrder.ASCENDING){
            return new SortKey(key.getColumn(),SortOrder.DESCENDING);
        }
        return new SortKey(key.getColumn(),SortOrder.ASCENDING);
    }

    private boolean isUnsorted(){
        List<? extends SortKey> keys=getSortKeys();
        int keySize=keys.size();
        return (keySize==0||keys.get(0).getSortOrder()==
                SortOrder.UNSORTED);
    }

    private void sortExistingData(){
        int[] lastViewToModel=getViewToModelAsInts(viewToModel);
        updateUseToString();
        cacheSortKeys(getSortKeys());
        if(isUnsorted()){
            if(getRowFilter()==null){
                viewToModel=null;
                modelToView=null;
            }else{
                int included=0;
                for(int i=0;i<modelToView.length;i++){
                    if(modelToView[i]!=-1){
                        viewToModel[included].modelIndex=i;
                        modelToView[i]=included++;
                    }
                }
            }
        }else{
            // sort the data
            Arrays.sort(viewToModel);
            // Update the modelToView array
            setModelToViewFromViewToModel(false);
        }
        fireRowSorterChanged(lastViewToModel);
    }

    public void sort(){
        sorted=true;
        int[] lastViewToModel=getViewToModelAsInts(viewToModel);
        updateUseToString();
        if(isUnsorted()){
            // Unsorted
            cachedSortKeys=new SortKey[0];
            if(getRowFilter()==null){
                // No filter & unsorted
                if(viewToModel!=null){
                    // sorted -> unsorted
                    viewToModel=null;
                    modelToView=null;
                }else{
                    // unsorted -> unsorted
                    // No need to do anything.
                    return;
                }
            }else{
                // There is filter, reset mappings
                initializeFilteredMapping();
            }
        }else{
            cacheSortKeys(getSortKeys());
            if(getRowFilter()!=null){
                initializeFilteredMapping();
            }else{
                createModelToView(getModelWrapper().getRowCount());
                createViewToModel(getModelWrapper().getRowCount());
            }
            // sort them
            Arrays.sort(viewToModel);
            // Update the modelToView array
            setModelToViewFromViewToModel(false);
        }
        fireRowSorterChanged(lastViewToModel);
    }

    private void updateUseToString(){
        int i=getModelWrapper().getColumnCount();
        if(useToString==null||useToString.length!=i){
            useToString=new boolean[i];
        }
        for(--i;i>=0;i--){
            useToString[i]=useToString(i);
        }
    }

    private void initializeFilteredMapping(){
        int rowCount=getModelWrapper().getRowCount();
        int i, j;
        int excludedCount=0;
        // Update model -> view
        createModelToView(rowCount);
        for(i=0;i<rowCount;i++){
            if(include(i)){
                modelToView[i]=i-excludedCount;
            }else{
                modelToView[i]=-1;
                excludedCount++;
            }
        }
        // Update view -> model
        createViewToModel(rowCount-excludedCount);
        for(i=0,j=0;i<rowCount;i++){
            if(modelToView[i]!=-1){
                viewToModel[j++].modelIndex=i;
            }
        }
    }

    private void createModelToView(int rowCount){
        if(modelToView==null||modelToView.length!=rowCount){
            modelToView=new int[rowCount];
        }
    }

    private void createViewToModel(int rowCount){
        int recreateFrom=0;
        if(viewToModel!=null){
            recreateFrom=Math.min(rowCount,viewToModel.length);
            if(viewToModel.length!=rowCount){
                Row[] oldViewToModel=viewToModel;
                viewToModel=new Row[rowCount];
                System.arraycopy(oldViewToModel,0,viewToModel,
                        0,recreateFrom);
            }
        }else{
            viewToModel=new Row[rowCount];
        }
        int i;
        for(i=0;i<recreateFrom;i++){
            viewToModel[i].modelIndex=i;
        }
        for(i=recreateFrom;i<rowCount;i++){
            viewToModel[i]=new Row(this,i);
        }
    }

    private void cacheSortKeys(List<? extends SortKey> keys){
        int keySize=keys.size();
        sortComparators=new Comparator[keySize];
        for(int i=0;i<keySize;i++){
            sortComparators[i]=getComparator0(keys.get(i).getColumn());
        }
        cachedSortKeys=keys.toArray(new SortKey[keySize]);
    }

    protected boolean useToString(int column){
        return (getComparator(column)==null);
    }

    private void setModelToViewFromViewToModel(boolean unsetFirst){
        int i;
        if(unsetFirst){
            for(i=modelToView.length-1;i>=0;i--){
                modelToView[i]=-1;
            }
        }
        for(i=viewToModel.length-1;i>=0;i--){
            modelToView[viewToModel[i].modelIndex]=i;
        }
    }

    private int[] getViewToModelAsInts(Row[] viewToModel){
        if(viewToModel!=null){
            int[] viewToModelI=new int[viewToModel.length];
            for(int i=viewToModel.length-1;i>=0;i--){
                viewToModelI[i]=viewToModel[i].modelIndex;
            }
            return viewToModelI;
        }
        return new int[0];
    }

    public void setComparator(int column,Comparator<?> comparator){
        checkColumn(column);
        if(comparators==null){
            comparators=new Comparator[getModelWrapper().getColumnCount()];
        }
        comparators[column]=comparator;
    }

    public Comparator<?> getComparator(int column){
        checkColumn(column);
        if(comparators!=null){
            return comparators[column];
        }
        return null;
    }

    // Returns the Comparator to use during sorting.  Where as
    // getComparator() may return null, this will never return null.
    private Comparator getComparator0(int column){
        Comparator comparator=getComparator(column);
        if(comparator!=null){
            return comparator;
        }
        // This should be ok as useToString(column) should have returned
        // true in this case.
        return Collator.getInstance();
    }

    private RowFilter.Entry<M,I> getFilterEntry(int modelIndex){
        if(filterEntry==null){
            filterEntry=new FilterEntry();
        }
        filterEntry.modelIndex=modelIndex;
        return filterEntry;
    }

    private void allChanged(){
        modelToView=null;
        viewToModel=null;
        comparators=null;
        isSortable=null;
        if(isUnsorted()){
            // Keys are already empty, to force a resort we have to
            // call sort
            sort();
        }else{
            setSortKeys(null);
        }
    }

    private void checkAgainstModel(int firstRow,int endRow){
        if(firstRow>endRow||firstRow<0||endRow<0||
                firstRow>modelRowCount){
            throw new IndexOutOfBoundsException("Invalid range");
        }
    }    public int getViewRowCount(){
        if(viewToModel!=null){
            // When filtering this may differ from getModelWrapper().getRowCount()
            return viewToModel.length;
        }
        return getModelWrapper().getRowCount();
    }

    private boolean include(int row){
        RowFilter<? super M,? super I> filter=getRowFilter();
        if(filter!=null){
            return filter.include(getFilterEntry(row));
        }
        // null filter, always include the row.
        return true;
    }    public int getModelRowCount(){
        return getModelWrapper().getRowCount();
    }

    @SuppressWarnings("unchecked")
    private int compare(int model1,int model2){
        int column;
        SortOrder sortOrder;
        Object v1, v2;
        int result;
        for(int counter=0;counter<cachedSortKeys.length;counter++){
            column=cachedSortKeys[counter].getColumn();
            sortOrder=cachedSortKeys[counter].getSortOrder();
            if(sortOrder==SortOrder.UNSORTED){
                result=model1-model2;
            }else{
                // v1 != null && v2 != null
                if(useToString[column]){
                    v1=getModelWrapper().getStringValueAt(model1,column);
                    v2=getModelWrapper().getStringValueAt(model2,column);
                }else{
                    v1=getModelWrapper().getValueAt(model1,column);
                    v2=getModelWrapper().getValueAt(model2,column);
                }
                // Treat nulls as < then non-null
                if(v1==null){
                    if(v2==null){
                        result=0;
                    }else{
                        result=-1;
                    }
                }else if(v2==null){
                    result=1;
                }else{
                    result=sortComparators[counter].compare(v1,v2);
                }
                if(sortOrder==SortOrder.DESCENDING){
                    result*=-1;
                }
            }
            if(result!=0){
                return result;
            }
        }
        // If we get here, they're equal. Fallback to model order.
        return model1-model2;
    }

    private boolean isTransformed(){
        return (viewToModel!=null);
    }    public void modelStructureChanged(){
        allChanged();
        modelRowCount=getModelWrapper().getRowCount();
    }

    private void insertInOrder(List<Row> toAdd,Row[] current){
        int last=0;
        int index;
        int max=toAdd.size();
        for(int i=0;i<max;i++){
            index=Arrays.binarySearch(current,toAdd.get(i));
            if(index<0){
                index=-1-index;
            }
            System.arraycopy(current,last,
                    viewToModel,last+i,index-last);
            viewToModel[index+i]=toAdd.get(i);
            last=index;
        }
        System.arraycopy(current,last,viewToModel,last+max,
                current.length-last);
    }    public void allRowsChanged(){
        modelRowCount=getModelWrapper().getRowCount();
        sort();
    }

    private boolean shouldOptimizeChange(int firstRow,int lastRow){
        if(!isTransformed()){
            // Not transformed, nothing to do.
            return false;
        }
        if(!sorted||(lastRow-firstRow)>viewToModel.length/10){
            // We either weren't sorted, or to much changed, sort it all
            sort();
            return false;
        }
        return true;
    }    public void rowsInserted(int firstRow,int endRow){
        checkAgainstModel(firstRow,endRow);
        int newModelRowCount=getModelWrapper().getRowCount();
        if(endRow>=newModelRowCount){
            throw new IndexOutOfBoundsException("Invalid range");
        }
        modelRowCount=newModelRowCount;
        if(shouldOptimizeChange(firstRow,endRow)){
            rowsInserted0(firstRow,endRow);
        }
    }

    private void rowsInserted0(int firstRow,int lastRow){
        int[] oldViewToModel=getViewToModelAsInts(viewToModel);
        int i;
        int delta=(lastRow-firstRow)+1;
        List<Row> added=new ArrayList<Row>(delta);
        // Build the list of Rows to add into added
        for(i=firstRow;i<=lastRow;i++){
            if(include(i)){
                added.add(new Row(this,i));
            }
        }
        // Adjust the model index of rows after the effected region
        int viewIndex;
        for(i=modelToView.length-1;i>=firstRow;i--){
            viewIndex=modelToView[i];
            if(viewIndex!=-1){
                viewToModel[viewIndex].modelIndex+=delta;
            }
        }
        // Insert newly added rows into viewToModel
        if(added.size()>0){
            Collections.sort(added);
            Row[] lastViewToModel=viewToModel;
            viewToModel=new Row[viewToModel.length+added.size()];
            insertInOrder(added,lastViewToModel);
        }
        // Update modelToView
        createModelToView(getModelWrapper().getRowCount());
        setModelToViewFromViewToModel(true);
        // Notify of change
        fireRowSorterChanged(oldViewToModel);
    }    public void rowsDeleted(int firstRow,int endRow){
        checkAgainstModel(firstRow,endRow);
        if(firstRow>=modelRowCount||endRow>=modelRowCount){
            throw new IndexOutOfBoundsException("Invalid range");
        }
        modelRowCount=getModelWrapper().getRowCount();
        if(shouldOptimizeChange(firstRow,endRow)){
            rowsDeleted0(firstRow,endRow);
        }
    }

    private void rowsDeleted0(int firstRow,int lastRow){
        int[] oldViewToModel=getViewToModelAsInts(viewToModel);
        int removedFromView=0;
        int i;
        int viewIndex;
        // Figure out how many visible rows are going to be effected.
        for(i=firstRow;i<=lastRow;i++){
            viewIndex=modelToView[i];
            if(viewIndex!=-1){
                removedFromView++;
                viewToModel[viewIndex]=null;
            }
        }
        // Update the model index of rows after the effected region
        int delta=lastRow-firstRow+1;
        for(i=modelToView.length-1;i>lastRow;i--){
            viewIndex=modelToView[i];
            if(viewIndex!=-1){
                viewToModel[viewIndex].modelIndex-=delta;
            }
        }
        // Then patch up the viewToModel array
        if(removedFromView>0){
            Row[] newViewToModel=new Row[viewToModel.length-
                    removedFromView];
            int newIndex=0;
            int last=0;
            for(i=0;i<viewToModel.length;i++){
                if(viewToModel[i]==null){
                    System.arraycopy(viewToModel,last,
                            newViewToModel,newIndex,i-last);
                    newIndex+=(i-last);
                    last=i+1;
                }
            }
            System.arraycopy(viewToModel,last,
                    newViewToModel,newIndex,viewToModel.length-last);
            viewToModel=newViewToModel;
        }
        // Update the modelToView mapping
        createModelToView(getModelWrapper().getRowCount());
        setModelToViewFromViewToModel(true);
        // And notify of change
        fireRowSorterChanged(oldViewToModel);
    }    public void rowsUpdated(int firstRow,int endRow){
        checkAgainstModel(firstRow,endRow);
        if(firstRow>=modelRowCount||endRow>=modelRowCount){
            throw new IndexOutOfBoundsException("Invalid range");
        }
        if(getSortsOnUpdates()){
            if(shouldOptimizeChange(firstRow,endRow)){
                rowsUpdated0(firstRow,endRow);
            }
        }else{
            sorted=false;
        }
    }

    private void rowsUpdated0(int firstRow,int lastRow){
        int[] oldViewToModel=getViewToModelAsInts(viewToModel);
        int i, j;
        int delta=lastRow-firstRow+1;
        int modelIndex;
        int last;
        int index;
        if(getRowFilter()==null){
            // Sorting only:
            // Remove the effected rows
            Row[] updated=new Row[delta];
            for(j=0,i=firstRow;i<=lastRow;i++,j++){
                updated[j]=viewToModel[modelToView[i]];
            }
            // Sort the update rows
            Arrays.sort(updated);
            // Build the intermediary array: the array of
            // viewToModel without the effected rows.
            Row[] intermediary=new Row[viewToModel.length-delta];
            for(i=0,j=0;i<viewToModel.length;i++){
                modelIndex=viewToModel[i].modelIndex;
                if(modelIndex<firstRow||modelIndex>lastRow){
                    intermediary[j++]=viewToModel[i];
                }
            }
            // Build the new viewToModel
            insertInOrder(Arrays.asList(updated),intermediary);
            // Update modelToView
            setModelToViewFromViewToModel(false);
        }else{
            // Sorting & filtering.
            // Remove the effected rows, adding them to updated and setting
            // modelToView to -2 for any rows that were not filtered out
            List<Row> updated=new ArrayList<Row>(delta);
            int newlyVisible=0;
            int newlyHidden=0;
            int effected=0;
            for(i=firstRow;i<=lastRow;i++){
                if(modelToView[i]==-1){
                    // This row was filtered out
                    if(include(i)){
                        // No longer filtered
                        updated.add(new Row(this,i));
                        newlyVisible++;
                    }
                }else{
                    // This row was visible, make sure it should still be
                    // visible.
                    if(!include(i)){
                        newlyHidden++;
                    }else{
                        updated.add(viewToModel[modelToView[i]]);
                    }
                    modelToView[i]=-2;
                    effected++;
                }
            }
            // Sort the updated rows
            Collections.sort(updated);
            // Build the intermediary array: the array of
            // viewToModel without the updated rows.
            Row[] intermediary=new Row[viewToModel.length-effected];
            for(i=0,j=0;i<viewToModel.length;i++){
                modelIndex=viewToModel[i].modelIndex;
                if(modelToView[modelIndex]!=-2){
                    intermediary[j++]=viewToModel[i];
                }
            }
            // Recreate viewToModel, if necessary
            if(newlyVisible!=newlyHidden){
                viewToModel=new Row[viewToModel.length+newlyVisible-
                        newlyHidden];
            }
            // Rebuild the new viewToModel array
            insertInOrder(updated,intermediary);
            // Update modelToView
            setModelToViewFromViewToModel(true);
        }
        // And finally fire a sort event.
        fireRowSorterChanged(oldViewToModel);
    }    public void rowsUpdated(int firstRow,int endRow,int column){
        checkColumn(column);
        rowsUpdated(firstRow,endRow);
    }

    protected abstract static class ModelWrapper<M,I>{
        protected ModelWrapper(){
        }

        public abstract M getModel();

        public abstract int getColumnCount();

        public abstract int getRowCount();

        public String getStringValueAt(int row,int column){
            Object o=getValueAt(row,column);
            if(o==null){
                return "";
            }
            String string=o.toString();
            if(string==null){
                return "";
            }
            return string;
        }

        public abstract Object getValueAt(int row,int column);

        public abstract I getIdentifier(int row);
    }

    // NOTE: this class is static so that it can be placed in an array
    private static class Row implements Comparable<Row>{
        int modelIndex;
        private DefaultRowSorter sorter;

        public Row(DefaultRowSorter sorter,int index){
            this.sorter=sorter;
            modelIndex=index;
        }

        public int compareTo(Row o){
            return sorter.compare(modelIndex,o.modelIndex);
        }
    }

    private class FilterEntry extends RowFilter.Entry<M,I>{
        int modelIndex;

        public M getModel(){
            return getModelWrapper().getModel();
        }

        public int getValueCount(){
            return getModelWrapper().getColumnCount();
        }

        public Object getValue(int index){
            return getModelWrapper().getValueAt(modelIndex,index);
        }

        public String getStringValue(int index){
            return getModelWrapper().getStringValueAt(modelIndex,index);
        }

        public I getIdentifier(){
            return getModelWrapper().getIdentifier(modelIndex);
        }
    }




















}
