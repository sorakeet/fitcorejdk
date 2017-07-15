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
package java.util.concurrent.atomic;

import java.io.Serializable;

public class LongAdder extends Striped64 implements Serializable{
    private static final long serialVersionUID=7249069246863182397L;

    public LongAdder(){
    }

    public void increment(){
        add(1L);
    }

    public void add(long x){
        Cell[] as;
        long b, v;
        int m;
        Cell a;
        if((as=cells)!=null||!casBase(b=base,b+x)){
            boolean uncontended=true;
            if(as==null||(m=as.length-1)<0||
                    (a=as[getProbe()&m])==null||
                    !(uncontended=a.cas(v=a.value,v+x)))
                longAccumulate(x,null,uncontended);
        }
    }

    public void decrement(){
        add(-1L);
    }

    public void reset(){
        Cell[] as=cells;
        Cell a;
        base=0L;
        if(as!=null){
            for(int i=0;i<as.length;++i){
                if((a=as[i])!=null)
                    a.value=0L;
            }
        }
    }

    public long sumThenReset(){
        Cell[] as=cells;
        Cell a;
        long sum=base;
        base=0L;
        if(as!=null){
            for(int i=0;i<as.length;++i){
                if((a=as[i])!=null){
                    sum+=a.value;
                    a.value=0L;
                }
            }
        }
        return sum;
    }

    public String toString(){
        return Long.toString(sum());
    }

    public long sum(){
        Cell[] as=cells;
        Cell a;
        long sum=base;
        if(as!=null){
            for(int i=0;i<as.length;++i){
                if((a=as[i])!=null)
                    sum+=a.value;
            }
        }
        return sum;
    }

    public int intValue(){
        return (int)sum();
    }

    public long longValue(){
        return sum();
    }

    public float floatValue(){
        return (float)sum();
    }

    public double doubleValue(){
        return (double)sum();
    }

    private Object writeReplace(){
        return new SerializationProxy(this);
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.InvalidObjectException{
        throw new java.io.InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable{
        private static final long serialVersionUID=7249069246863182397L;
        private final long value;

        SerializationProxy(LongAdder a){
            value=a.sum();
        }

        private Object readResolve(){
            LongAdder a=new LongAdder();
            a.base=value;
            return a;
        }
    }
}
