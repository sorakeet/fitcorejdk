/**
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.awt;

public class Insets implements Cloneable, java.io.Serializable{
    private static final long serialVersionUID=-2272572637695466749L;

    static{
        /** ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if(!GraphicsEnvironment.isHeadless()){
            initIDs();
        }
    }

    public int top;
    public int left;
    public int bottom;
    public int right;

    public Insets(int top,int left,int bottom,int right){
        this.top=top;
        this.left=left;
        this.bottom=bottom;
        this.right=right;
    }

    private static native void initIDs();

    public void set(int top,int left,int bottom,int right){
        this.top=top;
        this.left=left;
        this.bottom=bottom;
        this.right=right;
    }

    public int hashCode(){
        int sum1=left+bottom;
        int sum2=right+top;
        int val1=sum1*(sum1+1)/2+left;
        int val2=sum2*(sum2+1)/2+top;
        int sum3=val1+val2;
        return sum3*(sum3+1)/2+val2;
    }

    public boolean equals(Object obj){
        if(obj instanceof Insets){
            Insets insets=(Insets)obj;
            return ((top==insets.top)&&(left==insets.left)&&
                    (bottom==insets.bottom)&&(right==insets.right));
        }
        return false;
    }

    public Object clone(){
        try{
            return super.clone();
        }catch(CloneNotSupportedException e){
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    public String toString(){
        return getClass().getName()+"[top="+top+",left="+left+",bottom="+bottom+",right="+right+"]";
    }
}
