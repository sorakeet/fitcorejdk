/**
 * Copyright (c) 1995, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang;

import sun.misc.VM;

import java.io.PrintStream;
import java.util.Arrays;

public class ThreadGroup implements Thread.UncaughtExceptionHandler{
    private final ThreadGroup parent;
    String name;
    int maxPriority;
    boolean destroyed;
    boolean daemon;
    boolean vmAllowSuspension;
    int nUnstartedThreads=0;
    int nthreads;
    Thread threads[];
    int ngroups;
    ThreadGroup groups[];

    private ThreadGroup(){     // called from C code
        this.name="system";
        this.maxPriority=Thread.MAX_PRIORITY;
        this.parent=null;
    }

    public ThreadGroup(String name){
        this(Thread.currentThread().getThreadGroup(),name);
    }

    public ThreadGroup(ThreadGroup parent,String name){
        this(checkParentAccess(parent),parent,name);
    }

    private ThreadGroup(Void unused,ThreadGroup parent,String name){
        this.name=name;
        this.maxPriority=parent.maxPriority;
        this.daemon=parent.daemon;
        this.vmAllowSuspension=parent.vmAllowSuspension;
        this.parent=parent;
        parent.add(this);
    }

    private static Void checkParentAccess(ThreadGroup parent){
        parent.checkAccess();
        return null;
    }

    public final ThreadGroup getParent(){
        if(parent!=null)
            parent.checkAccess();
        return parent;
    }

    public final int getMaxPriority(){
        return maxPriority;
    }

    public final void setMaxPriority(int pri){
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            checkAccess();
            if(pri<Thread.MIN_PRIORITY||pri>Thread.MAX_PRIORITY){
                return;
            }
            maxPriority=(parent!=null)?Math.min(pri,parent.maxPriority):pri;
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
        }
        for(int i=0;i<ngroupsSnapshot;i++){
            groupsSnapshot[i].setMaxPriority(pri);
        }
    }

    public final void checkAccess(){
        SecurityManager security=System.getSecurityManager();
        if(security!=null){
            security.checkAccess(this);
        }
    }

    public final boolean isDaemon(){
        return daemon;
    }

    public final void setDaemon(boolean daemon){
        checkAccess();
        this.daemon=daemon;
    }

    public synchronized boolean isDestroyed(){
        return destroyed;
    }

    public final boolean parentOf(ThreadGroup g){
        for(;g!=null;g=g.parent){
            if(g==this){
                return true;
            }
        }
        return false;
    }

    public int activeCount(){
        int result;
        // Snapshot sub-group data so we don't hold this lock
        // while our children are computing.
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            if(destroyed){
                return 0;
            }
            result=nthreads;
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
        }
        for(int i=0;i<ngroupsSnapshot;i++){
            result+=groupsSnapshot[i].activeCount();
        }
        return result;
    }

    public int enumerate(Thread list[]){
        checkAccess();
        return enumerate(list,0,true);
    }

    private int enumerate(Thread list[],int n,boolean recurse){
        int ngroupsSnapshot=0;
        ThreadGroup[] groupsSnapshot=null;
        synchronized(this){
            if(destroyed){
                return 0;
            }
            int nt=nthreads;
            if(nt>list.length-n){
                nt=list.length-n;
            }
            for(int i=0;i<nt;i++){
                if(threads[i].isAlive()){
                    list[n++]=threads[i];
                }
            }
            if(recurse){
                ngroupsSnapshot=ngroups;
                if(groups!=null){
                    groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
                }else{
                    groupsSnapshot=null;
                }
            }
        }
        if(recurse){
            for(int i=0;i<ngroupsSnapshot;i++){
                n=groupsSnapshot[i].enumerate(list,n,true);
            }
        }
        return n;
    }

    public int enumerate(Thread list[],boolean recurse){
        checkAccess();
        return enumerate(list,0,recurse);
    }

    public int activeGroupCount(){
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            if(destroyed){
                return 0;
            }
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
        }
        int n=ngroupsSnapshot;
        for(int i=0;i<ngroupsSnapshot;i++){
            n+=groupsSnapshot[i].activeGroupCount();
        }
        return n;
    }

    public int enumerate(ThreadGroup list[]){
        checkAccess();
        return enumerate(list,0,true);
    }

    private int enumerate(ThreadGroup list[],int n,boolean recurse){
        int ngroupsSnapshot=0;
        ThreadGroup[] groupsSnapshot=null;
        synchronized(this){
            if(destroyed){
                return 0;
            }
            int ng=ngroups;
            if(ng>list.length-n){
                ng=list.length-n;
            }
            if(ng>0){
                System.arraycopy(groups,0,list,n,ng);
                n+=ng;
            }
            if(recurse){
                ngroupsSnapshot=ngroups;
                if(groups!=null){
                    groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
                }else{
                    groupsSnapshot=null;
                }
            }
        }
        if(recurse){
            for(int i=0;i<ngroupsSnapshot;i++){
                n=groupsSnapshot[i].enumerate(list,n,true);
            }
        }
        return n;
    }

    public int enumerate(ThreadGroup list[],boolean recurse){
        checkAccess();
        return enumerate(list,0,recurse);
    }

    @Deprecated
    public final void stop(){
        if(stopOrSuspend(false))
            Thread.currentThread().stop();
    }

    @SuppressWarnings("deprecation")
    private boolean stopOrSuspend(boolean suspend){
        boolean suicide=false;
        Thread us=Thread.currentThread();
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot=null;
        synchronized(this){
            checkAccess();
            for(int i=0;i<nthreads;i++){
                if(threads[i]==us)
                    suicide=true;
                else if(suspend)
                    threads[i].suspend();
                else
                    threads[i].stop();
            }
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }
        }
        for(int i=0;i<ngroupsSnapshot;i++)
            suicide=groupsSnapshot[i].stopOrSuspend(suspend)||suicide;
        return suicide;
    }

    public final void interrupt(){
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            checkAccess();
            for(int i=0;i<nthreads;i++){
                threads[i].interrupt();
            }
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
        }
        for(int i=0;i<ngroupsSnapshot;i++){
            groupsSnapshot[i].interrupt();
        }
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public final void suspend(){
        if(stopOrSuspend(true))
            Thread.currentThread().suspend();
    }

    @Deprecated
    @SuppressWarnings("deprecation")
    public final void resume(){
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            checkAccess();
            for(int i=0;i<nthreads;i++){
                threads[i].resume();
            }
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
        }
        for(int i=0;i<ngroupsSnapshot;i++){
            groupsSnapshot[i].resume();
        }
    }

    private final void add(ThreadGroup g){
        synchronized(this){
            if(destroyed){
                throw new IllegalThreadStateException();
            }
            if(groups==null){
                groups=new ThreadGroup[4];
            }else if(ngroups==groups.length){
                groups=Arrays.copyOf(groups,ngroups*2);
            }
            groups[ngroups]=g;
            // This is done last so it doesn't matter in case the
            // thread is killed
            ngroups++;
        }
    }

    private void remove(ThreadGroup g){
        synchronized(this){
            if(destroyed){
                return;
            }
            for(int i=0;i<ngroups;i++){
                if(groups[i]==g){
                    ngroups-=1;
                    System.arraycopy(groups,i+1,groups,i,ngroups-i);
                    // Zap dangling reference to the dead group so that
                    // the garbage collector will collect it.
                    groups[ngroups]=null;
                    break;
                }
            }
            if(nthreads==0){
                notifyAll();
            }
            if(daemon&&(nthreads==0)&&
                    (nUnstartedThreads==0)&&(ngroups==0)){
                destroy();
            }
        }
    }

    public final void destroy(){
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            checkAccess();
            if(destroyed||(nthreads>0)){
                throw new IllegalThreadStateException();
            }
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
            if(parent!=null){
                destroyed=true;
                ngroups=0;
                groups=null;
                nthreads=0;
                threads=null;
            }
        }
        for(int i=0;i<ngroupsSnapshot;i+=1){
            groupsSnapshot[i].destroy();
        }
        if(parent!=null){
            parent.remove(this);
        }
    }

    void addUnstarted(){
        synchronized(this){
            if(destroyed){
                throw new IllegalThreadStateException();
            }
            nUnstartedThreads++;
        }
    }

    void add(Thread t){
        synchronized(this){
            if(destroyed){
                throw new IllegalThreadStateException();
            }
            if(threads==null){
                threads=new Thread[4];
            }else if(nthreads==threads.length){
                threads=Arrays.copyOf(threads,nthreads*2);
            }
            threads[nthreads]=t;
            // This is done last so it doesn't matter in case the
            // thread is killed
            nthreads++;
            // The thread is now a fully fledged member of the group, even
            // though it may, or may not, have been started yet. It will prevent
            // the group from being destroyed so the unstarted Threads count is
            // decremented.
            nUnstartedThreads--;
        }
    }

    void threadStartFailed(Thread t){
        synchronized(this){
            remove(t);
            nUnstartedThreads++;
        }
    }

    private void remove(Thread t){
        synchronized(this){
            if(destroyed){
                return;
            }
            for(int i=0;i<nthreads;i++){
                if(threads[i]==t){
                    System.arraycopy(threads,i+1,threads,i,--nthreads-i);
                    // Zap dangling reference to the dead thread so that
                    // the garbage collector will collect it.
                    threads[nthreads]=null;
                    break;
                }
            }
        }
    }

    void threadTerminated(Thread t){
        synchronized(this){
            remove(t);
            if(nthreads==0){
                notifyAll();
            }
            if(daemon&&(nthreads==0)&&
                    (nUnstartedThreads==0)&&(ngroups==0)){
                destroy();
            }
        }
    }

    public void list(){
        list(System.out,0);
    }

    void list(PrintStream out,int indent){
        int ngroupsSnapshot;
        ThreadGroup[] groupsSnapshot;
        synchronized(this){
            for(int j=0;j<indent;j++){
                out.print(" ");
            }
            out.println(this);
            indent+=4;
            for(int i=0;i<nthreads;i++){
                for(int j=0;j<indent;j++){
                    out.print(" ");
                }
                out.println(threads[i]);
            }
            ngroupsSnapshot=ngroups;
            if(groups!=null){
                groupsSnapshot=Arrays.copyOf(groups,ngroupsSnapshot);
            }else{
                groupsSnapshot=null;
            }
        }
        for(int i=0;i<ngroupsSnapshot;i++){
            groupsSnapshot[i].list(out,indent);
        }
    }

    public void uncaughtException(Thread t,Throwable e){
        if(parent!=null){
            parent.uncaughtException(t,e);
        }else{
            Thread.UncaughtExceptionHandler ueh=
                    Thread.getDefaultUncaughtExceptionHandler();
            if(ueh!=null){
                ueh.uncaughtException(t,e);
            }else if(!(e instanceof ThreadDeath)){
                System.err.print("Exception in thread \""
                        +t.getName()+"\" ");
                e.printStackTrace(System.err);
            }
        }
    }

    @Deprecated
    public boolean allowThreadSuspension(boolean b){
        this.vmAllowSuspension=b;
        if(!b){
            VM.unsuspendSomeThreads();
        }
        return true;
    }

    public String toString(){
        return getClass().getName()+"[name="+getName()+",maxpri="+maxPriority+"]";
    }

    public final String getName(){
        return name;
    }
}
