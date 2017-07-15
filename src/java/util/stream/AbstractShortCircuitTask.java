/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util.stream;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("serial")
abstract class AbstractShortCircuitTask<P_IN,P_OUT,R,
        K extends AbstractShortCircuitTask<P_IN,P_OUT,R,K>>
        extends AbstractTask<P_IN,P_OUT,R,K>{
    protected final AtomicReference<R> sharedResult;
    protected volatile boolean canceled;

    protected AbstractShortCircuitTask(PipelineHelper<P_OUT> helper,
                                       Spliterator<P_IN> spliterator){
        super(helper,spliterator);
        sharedResult=new AtomicReference<>(null);
    }

    protected AbstractShortCircuitTask(K parent,
                                       Spliterator<P_IN> spliterator){
        super(parent,spliterator);
        sharedResult=parent.sharedResult;
    }

    protected void shortCircuit(R result){
        if(result!=null)
            sharedResult.compareAndSet(null,result);
    }

    @Override
    public R getRawResult(){
        return getLocalResult();
    }

    @Override
    public R getLocalResult(){
        if(isRoot()){
            R answer=sharedResult.get();
            return (answer==null)?getEmptyResult():answer;
        }else
            return super.getLocalResult();
    }

    protected abstract R getEmptyResult();

    @Override
    protected void setLocalResult(R localResult){
        if(isRoot()){
            if(localResult!=null)
                sharedResult.compareAndSet(null,localResult);
        }else
            super.setLocalResult(localResult);
    }

    @Override
    public void compute(){
        Spliterator<P_IN> rs=spliterator, ls;
        long sizeEstimate=rs.estimateSize();
        long sizeThreshold=getTargetSize(sizeEstimate);
        boolean forkRight=false;
        @SuppressWarnings("unchecked") K task=(K)this;
        AtomicReference<R> sr=sharedResult;
        R result;
        while((result=sr.get())==null){
            if(task.taskCanceled()){
                result=task.getEmptyResult();
                break;
            }
            if(sizeEstimate<=sizeThreshold||(ls=rs.trySplit())==null){
                result=task.doLeaf();
                break;
            }
            K leftChild, rightChild, taskToFork;
            task.leftChild=leftChild=task.makeChild(ls);
            task.rightChild=rightChild=task.makeChild(rs);
            task.setPendingCount(1);
            if(forkRight){
                forkRight=false;
                rs=ls;
                task=leftChild;
                taskToFork=rightChild;
            }else{
                forkRight=true;
                task=rightChild;
                taskToFork=leftChild;
            }
            taskToFork.fork();
            sizeEstimate=rs.estimateSize();
        }
        task.setLocalResult(result);
        task.tryComplete();
    }

    protected void cancel(){
        canceled=true;
    }

    protected boolean taskCanceled(){
        boolean cancel=canceled;
        if(!cancel){
            for(K parent=getParent();!cancel&&parent!=null;parent=parent.getParent())
                cancel=parent.canceled;
        }
        return cancel;
    }

    protected void cancelLaterNodes(){
        // Go up the tree, cancel right siblings of this node and all parents
        for(@SuppressWarnings("unchecked") K parent=getParent(), node=(K)this;
            parent!=null;
            node=parent,parent=parent.getParent()){
            // If node is a left child of parent, then has a right sibling
            if(parent.leftChild==node){
                K rightSibling=parent.rightChild;
                if(!rightSibling.canceled)
                    rightSibling.cancel();
            }
        }
    }
}
