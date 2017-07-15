/**
 * Copyright (c) 1999, 2004, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming;

public class LinkException extends NamingException{
    private static final long serialVersionUID=-7967662604076777712L;
    protected Name linkResolvedName;
    protected Object linkResolvedObj;
    protected Name linkRemainingName;
    protected String linkExplanation;

    public LinkException(String explanation){
        super(explanation);
        linkResolvedName=null;
        linkResolvedObj=null;
        linkRemainingName=null;
        linkExplanation=null;
    }

    public LinkException(){
        super();
        linkResolvedName=null;
        linkResolvedObj=null;
        linkRemainingName=null;
        linkExplanation=null;
    }

    public Name getLinkResolvedName(){
        return this.linkResolvedName;
    }

    public void setLinkResolvedName(Name name){
        if(name!=null){
            this.linkResolvedName=(Name)(name.clone());
        }else{
            this.linkResolvedName=null;
        }
    }

    public Name getLinkRemainingName(){
        return this.linkRemainingName;
    }

    public void setLinkRemainingName(Name name){
        if(name!=null)
            this.linkRemainingName=(Name)(name.clone());
        else
            this.linkRemainingName=null;
    }

    public Object getLinkResolvedObj(){
        return this.linkResolvedObj;
    }

    public void setLinkResolvedObj(Object obj){
        this.linkResolvedObj=obj;
    }

    public String getLinkExplanation(){
        return this.linkExplanation;
    }

    public void setLinkExplanation(String msg){
        this.linkExplanation=msg;
    }    public String toString(){
        return super.toString()+"; Link Remaining Name: '"+
                this.linkRemainingName+"'";
    }

    public String toString(boolean detail){
        if(!detail||this.linkResolvedObj==null)
            return this.toString();
        return this.toString()+"; Link Resolved Object: "+
                this.linkResolvedObj;
    }


};
