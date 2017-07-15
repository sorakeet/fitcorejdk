/**
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.util;

import java.util.function.DoubleConsumer;

public class DoubleSummaryStatistics implements DoubleConsumer{
    private long count;
    private double sum;
    private double sumCompensation; // Low order bits of sum
    private double simpleSum; // Used to compute right sum for non-finite inputs
    private double min=Double.POSITIVE_INFINITY;
    private double max=Double.NEGATIVE_INFINITY;

    public DoubleSummaryStatistics(){
    }

    @Override
    public void accept(double value){
        ++count;
        simpleSum+=value;
        sumWithCompensation(value);
        min=Math.min(min,value);
        max=Math.max(max,value);
    }

    private void sumWithCompensation(double value){
        double tmp=value-sumCompensation;
        double velvel=sum+tmp; // Little wolf of rounding error
        sumCompensation=(velvel-sum)-tmp;
        sum=velvel;
    }

    public void combine(DoubleSummaryStatistics other){
        count+=other.count;
        simpleSum+=other.simpleSum;
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation);
        min=Math.min(min,other.min);
        max=Math.max(max,other.max);
    }

    @Override
    public String toString(){
        return String.format(
                "%s{count=%d, sum=%f, min=%f, average=%f, max=%f}",
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

    public final double getSum(){
        // Better error bounds to add both terms as the final sum
        double tmp=sum+sumCompensation;
        if(Double.isNaN(tmp)&&Double.isInfinite(simpleSum))
            // If the compensated sum is spuriously NaN from
            // accumulating one or more same-signed infinite values,
            // return the correctly-signed infinity stored in
            // simpleSum.
            return simpleSum;
        else
            return tmp;
    }

    public final double getMin(){
        return min;
    }

    public final double getMax(){
        return max;
    }

    public final double getAverage(){
        return getCount()>0?getSum()/getCount():0.0d;
    }
}
