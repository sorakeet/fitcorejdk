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

public class DoubleAdder extends Striped64 implements Serializable{
    private static final long serialVersionUID=7249069246863182397L;

    public DoubleAdder(){
    }

    public void add(double x){
        Cell[] as;
        long b, v;
        int m;
        Cell a;
        if((as=cells)!=null||
                !casBase(b=base,
                        Double.doubleToRawLongBits
                                (Double.longBitsToDouble(b)+x))){
            boolean uncontended=true;
            if(as==null||(m=as.length-1)<0||
                    (a=as[getProbe()&m])==null||
                    !(uncontended=a.cas(v=a.value,
                            Double.doubleToRawLongBits
                                    (Double.longBitsToDouble(v)+x))))
                doubleAccumulate(x,null,uncontended);
        }
    }

    public void reset(){
        Cell[] as=cells;
        Cell a;
        base=0L; // relies on fact that double 0 must have same rep as long
        if(as!=null){
            for(int i=0;i<as.length;++i){
                if((a=as[i])!=null)
                    a.value=0L;
            }
        }
    }

    public double sumThenReset(){
        Cell[] as=cells;
        Cell a;
        double sum=Double.longBitsToDouble(base);
        base=0L;
        if(as!=null){
            for(int i=0;i<as.length;++i){
                if((a=as[i])!=null){
                    long v=a.value;
                    a.value=0L;
                    sum+=Double.longBitsToDouble(v);
                }
            }
        }
        return sum;
    }

    public String toString(){
        return Double.toString(sum());
    }

    public double sum(){
        Cell[] as=cells;
        Cell a;
        double sum=Double.longBitsToDouble(base);
        if(as!=null){
            for(int i=0;i<as.length;++i){
                if((a=as[i])!=null)
                    sum+=Double.longBitsToDouble(a.value);
            }
        }
        return sum;
    }

    public int intValue(){
        return (int)sum();
    }

    public long longValue(){
        return (long)sum();
    }

    public float floatValue(){
        return (float)sum();
    }

    public double doubleValue(){
        return sum();
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
        private final double value;

        SerializationProxy(DoubleAdder a){
            value=a.sum();
        }

        private Object readResolve(){
            DoubleAdder a=new DoubleAdder();
            a.base=Double.doubleToRawLongBits(value);
            return a;
        }
    }
}
