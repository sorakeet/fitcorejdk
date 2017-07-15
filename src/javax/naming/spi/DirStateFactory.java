/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.spi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.Hashtable;

public interface DirStateFactory extends StateFactory{
    public Result getStateToBind(Object obj,Name name,Context nameCtx,
                                 Hashtable<?,?> environment,
                                 Attributes inAttrs)
            throws NamingException;

    public static class Result{
        private Object obj;
        private Attributes attrs;

        public Result(Object obj,Attributes outAttrs){
            this.obj=obj;
            this.attrs=outAttrs;
        }

        public Object getObject(){
            return obj;
        }

        ;

        public Attributes getAttributes(){
            return attrs;
        }

        ;
    }
}
