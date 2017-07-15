/**
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

import javax.naming.Binding;

public class SearchResult extends Binding{
    private static final long serialVersionUID=-9158063327699723172L;
    private Attributes attrs;

    public SearchResult(String name,Object obj,Attributes attrs){
        super(name,obj);
        this.attrs=attrs;
    }

    public SearchResult(String name,Object obj,Attributes attrs,
                        boolean isRelative){
        super(name,obj,isRelative);
        this.attrs=attrs;
    }

    public SearchResult(String name,String className,
                        Object obj,Attributes attrs){
        super(name,className,obj);
        this.attrs=attrs;
    }

    public SearchResult(String name,String className,Object obj,
                        Attributes attrs,boolean isRelative){
        super(name,className,obj,isRelative);
        this.attrs=attrs;
    }

    public String toString(){
        return super.toString()+":"+getAttributes();
    }

    public Attributes getAttributes(){
        return attrs;
    }

    public void setAttributes(Attributes attrs){
        this.attrs=attrs;
        // ??? check for null?
    }
}
