/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.util.function.IntConsumer;

public class IntSummaryStatistics implements IntConsumer{
    private long count;
    private long sum;
    private int min=Integer.MAX_VALUE;
    private int max=Integer.MIN_VALUE;

    public IntSummaryStatistics(){
    }

    @Override
    public void accept(int value){
        ++count;
        sum+=value;
        min=Math.min(min,value);
        max=Math.max(max,value);
    }

    public void combine(IntSummaryStatistics other){
        count+=other.count;
        sum+=other.sum;
        min=Math.min(min,other.min);
        max=Math.max(max,other.max);
    }

    @Override
    /**
     * {@inheritDoc}
     *
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    public String toString(){
        return String.format(
                "%s{count=%d, sum=%d, min=%d, average=%f, max=%d}",
                this.getClass().getSimpleName(),
                getCount(),
                getSum(),
                getMin(),
                getAverage(),
                getMax());
    }

    public final long getCount(){
        return count;
    }

    public final long getSum(){
        return sum;
    }

    public final int getMin(){
        return min;
    }

    public final int getMax(){
        return max;
    }

    public final double getAverage(){
        return getCount()>0?(double)getSum()/getCount():0.0d;
    }
}
