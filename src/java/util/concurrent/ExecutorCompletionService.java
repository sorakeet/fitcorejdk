/**
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
/**
 *
 *
 *
 *
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */
package java.util.concurrent;

public class ExecutorCompletionService<V> implements CompletionService<V>{
    private final Executor executor;
    private final AbstractExecutorService aes;
    private final BlockingQueue<Future<V>> completionQueue;

    public ExecutorCompletionService(Executor executor){
        if(executor==null)
            throw new NullPointerException();
        this.executor=executor;
        this.aes=(executor instanceof AbstractExecutorService)?
                (AbstractExecutorService)executor:null;
        this.completionQueue=new LinkedBlockingQueue<Future<V>>();
    }

    public ExecutorCompletionService(Executor executor,
                                     BlockingQueue<Future<V>> completionQueue){
        if(executor==null||completionQueue==null)
            throw new NullPointerException();
        this.executor=executor;
        this.aes=(executor instanceof AbstractExecutorService)?
                (AbstractExecutorService)executor:null;
        this.completionQueue=completionQueue;
    }

    public Future<V> submit(Callable<V> task){
        if(task==null) throw new NullPointerException();
        RunnableFuture<V> f=newTaskFor(task);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    private RunnableFuture<V> newTaskFor(Callable<V> task){
        if(aes==null)
            return new FutureTask<V>(task);
        else
            return aes.newTaskFor(task);
    }

    public Future<V> submit(Runnable task,V result){
        if(task==null) throw new NullPointerException();
        RunnableFuture<V> f=newTaskFor(task,result);
        executor.execute(new QueueingFuture(f));
        return f;
    }

    private RunnableFuture<V> newTaskFor(Runnable task,V result){
        if(aes==null)
            return new FutureTask<V>(task,result);
        else
            return aes.newTaskFor(task,result);
    }

    public Future<V> take() throws InterruptedException{
        return completionQueue.take();
    }

    public Future<V> poll(){
        return completionQueue.poll();
    }

    public Future<V> poll(long timeout,TimeUnit unit)
            throws InterruptedException{
        return completionQueue.poll(timeout,unit);
    }

    private class QueueingFuture extends FutureTask<Void>{
        private final Future<V> task;

        QueueingFuture(RunnableFuture<V> task){
            super(task,null);
            this.task=task;
        }

        protected void done(){
            completionQueue.add(task);
        }
    }
}
