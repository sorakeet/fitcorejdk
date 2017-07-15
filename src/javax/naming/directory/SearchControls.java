/**
 * Copyright (c) 1999, 2000, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.naming.directory;

public class SearchControls implements java.io.Serializable{
    public final static int OBJECT_SCOPE=0;
    public final static int ONELEVEL_SCOPE=1;
    public final static int SUBTREE_SCOPE=2;
    private static final long serialVersionUID=-2480540967773454797L;
    private int searchScope;
    private int timeLimit;
    private boolean derefLink;
    private boolean returnObj;
    private long countLimit;
    private String[] attributesToReturn;

    public SearchControls(){
        searchScope=ONELEVEL_SCOPE;
        timeLimit=0; // no limit
        countLimit=0; // no limit
        derefLink=false;
        returnObj=false;
        attributesToReturn=null; // return all
    }

    public SearchControls(int scope,
                          long countlim,
                          int timelim,
                          String[] attrs,
                          boolean retobj,
                          boolean deref){
        searchScope=scope;
        timeLimit=timelim; // no limit
        derefLink=deref;
        returnObj=retobj;
        countLimit=countlim; // no limit
        attributesToReturn=attrs; // return all
    }

    public int getSearchScope(){
        return searchScope;
    }

    public void setSearchScope(int scope){
        searchScope=scope;
    }

    public int getTimeLimit(){
        return timeLimit;
    }

    public void setTimeLimit(int ms){
        timeLimit=ms;
    }

    public boolean getDerefLinkFlag(){
        return derefLink;
    }

    public void setDerefLinkFlag(boolean on){
        derefLink=on;
    }

    public boolean getReturningObjFlag(){
        return returnObj;
    }

    public void setReturningObjFlag(boolean on){
        returnObj=on;
    }

    public long getCountLimit(){
        return countLimit;
    }

    public void setCountLimit(long limit){
        countLimit=limit;
    }

    public String[] getReturningAttributes(){
        return attributesToReturn;
    }

    public void setReturningAttributes(String[] attrs){
        attributesToReturn=attrs;
    }
}
