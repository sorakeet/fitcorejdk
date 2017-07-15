/**
 * Copyright (c) 1997, 2007, Oracle and/or its affiliates. All rights reserved.
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
 * Generated By:JJTree: Do not edit this line. SimpleNode.java
 */
/** Generated By:JJTree: Do not edit this line. SimpleNode.java */
package com.sun.jmx.snmp.IPAcl;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Vector;

class SimpleNode implements Node{
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Parser parser;

    public SimpleNode(Parser p,int i){
        this(i);
        parser=p;
    }

    public SimpleNode(int i){
        id=i;
    }

    public static Node jjtCreate(int id){
        return new SimpleNode(id);
    }

    public static Node jjtCreate(Parser p,int id){
        return new SimpleNode(p,id);
    }

    public void jjtOpen(){
    }

    public void jjtClose(){
    }

    public void jjtSetParent(Node n){
        parent=n;
    }

    public Node jjtGetParent(){
        return parent;
    }

    public void jjtAddChild(Node n,int i){
        if(children==null){
            children=new Node[i+1];
        }else if(i>=children.length){
            Node c[]=new Node[i+1];
            System.arraycopy(children,0,c,0,children.length);
            children=c;
        }
        children[i]=n;
    }

    public Node jjtGetChild(int i){
        return children[i];
    }

    public int jjtGetNumChildren(){
        return (children==null)?0:children.length;
    }

    public void buildTrapEntries(Hashtable<InetAddress,Vector<String>> dest){
        if(children!=null){
            for(int i=0;i<children.length;++i){
                SimpleNode n=(SimpleNode)children[i];
                if(n!=null){
                    n.buildTrapEntries(dest);
                }
            } /** end of loop */
        }
    }

    public void buildInformEntries(Hashtable<InetAddress,Vector<String>> dest){
        if(children!=null){
            for(int i=0;i<children.length;++i){
                SimpleNode n=(SimpleNode)children[i];
                if(n!=null){
                    n.buildInformEntries(dest);
                }
            } /** end of loop */
        }
    }

    public void buildAclEntries(PrincipalImpl owner,AclImpl acl){
        if(children!=null){
            for(int i=0;i<children.length;++i){
                SimpleNode n=(SimpleNode)children[i];
                if(n!=null){
                    n.buildAclEntries(owner,acl);
                }
            } /** end of loop */
        }
    }

    public String toString(String prefix){
        return prefix+toString();
    }

    /** END SR */
    public String toString(){
        return ParserTreeConstants.jjtNodeName[id];
    }

    public void dump(String prefix){
        if(children!=null){
            for(int i=0;i<children.length;++i){
                SimpleNode n=(SimpleNode)children[i];
                if(n!=null){
                    n.dump(prefix+" ");
                }
            }
        }
    }
}