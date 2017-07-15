/**
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.lang.invoke;

public class SwitchPoint{
    private static final MethodHandle
            K_true=MethodHandles.constant(boolean.class,true),
            K_false=MethodHandles.constant(boolean.class,false);
    private final MutableCallSite mcs;
    private final MethodHandle mcsInvoker;

    public SwitchPoint(){
        this.mcs=new MutableCallSite(K_true);
        this.mcsInvoker=mcs.dynamicInvoker();
    }

    public static void invalidateAll(SwitchPoint[] switchPoints){
        if(switchPoints.length==0) return;
        MutableCallSite[] sites=new MutableCallSite[switchPoints.length];
        for(int i=0;i<switchPoints.length;i++){
            SwitchPoint spt=switchPoints[i];
            if(spt==null) break;  // MSC.syncAll will trigger a NPE
            sites[i]=spt.mcs;
            spt.mcs.setTarget(K_false);
        }
        MutableCallSite.syncAll(sites);
    }

    public boolean hasBeenInvalidated(){
        return (mcs.getTarget()!=K_true);
    }

    public MethodHandle guardWithTest(MethodHandle target,MethodHandle fallback){
        if(mcs.getTarget()==K_false)
            return fallback;  // already invalid
        return MethodHandles.guardWithTest(mcsInvoker,target,fallback);
    }
}
