/**
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.print.attribute.standard;

import javax.print.attribute.Attribute;
import javax.print.attribute.PrintJobAttribute;
import java.util.Collection;
import java.util.HashSet;

public final class JobStateReasons
        extends HashSet<JobStateReason> implements PrintJobAttribute{
    private static final long serialVersionUID=8849088261264331812L;

    public JobStateReasons(){
        super();
    }

    public JobStateReasons(int initialCapacity){
        super(initialCapacity);
    }

    public JobStateReasons(int initialCapacity,float loadFactor){
        super(initialCapacity,loadFactor);
    }

    public JobStateReasons(Collection<JobStateReason> collection){
        super(collection);
    }

    public boolean add(JobStateReason o){
        if(o==null){
            throw new NullPointerException();
        }
        return super.add((JobStateReason)o);
    }

    public final Class<? extends Attribute> getCategory(){
        return JobStateReasons.class;
    }

    public final String getName(){
        return "job-state-reasons";
    }
}
