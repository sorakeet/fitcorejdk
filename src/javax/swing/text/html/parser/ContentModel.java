/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing.text.html.parser;

import java.io.Serializable;
import java.util.Vector;

public final class ContentModel implements Serializable{
    public int type;
    public Object content;
    public ContentModel next;
    private boolean valSet[];
    private boolean val[];

    public ContentModel(){
    }

    public ContentModel(Element content){
        this(0,content,null);
    }

    public ContentModel(int type,Object content,ContentModel next){
        this.type=type;
        this.content=content;
        this.next=next;
    }

    public ContentModel(int type,ContentModel content){
        this(type,content,null);
    }

    public boolean empty(){
        switch(type){
            case '*':
            case '?':
                return true;
            case '+':
            case '|':
                for(ContentModel m=(ContentModel)content;m!=null;m=m.next){
                    if(m.empty()){
                        return true;
                    }
                }
                return false;
            case ',':
            case '&':
                for(ContentModel m=(ContentModel)content;m!=null;m=m.next){
                    if(!m.empty()){
                        return false;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public void getElements(Vector<Element> elemVec){
        switch(type){
            case '*':
            case '?':
            case '+':
                ((ContentModel)content).getElements(elemVec);
                break;
            case ',':
            case '|':
            case '&':
                for(ContentModel m=(ContentModel)content;m!=null;m=m.next){
                    m.getElements(elemVec);
                }
                break;
            default:
                elemVec.addElement((Element)content);
        }
    }
    // A cache used by first().  This cache was found to speed parsing
    // by about 10% (based on measurements of the 4-12 code base after
    // buffering was fixed).

    public boolean first(Object token){
        switch(type){
            case '*':
            case '?':
            case '+':
                return ((ContentModel)content).first(token);
            case ',':
                for(ContentModel m=(ContentModel)content;m!=null;m=m.next){
                    if(m.first(token)){
                        return true;
                    }
                    if(!m.empty()){
                        return false;
                    }
                }
                return false;
            case '|':
            case '&':{
                Element e=(Element)token;
                if(valSet==null||valSet.length<=Element.getMaxIndex()){
                    valSet=new boolean[Element.getMaxIndex()+1];
                    val=new boolean[valSet.length];
                }
                if(valSet[e.index]){
                    return val[e.index];
                }
                for(ContentModel m=(ContentModel)content;m!=null;m=m.next){
                    if(m.first(token)){
                        val[e.index]=true;
                        break;
                    }
                }
                valSet[e.index]=true;
                return val[e.index];
            }
            default:
                return (content==token);
            // PENDING: refer to comment in ContentModelState
/**
 if (content == token) {
 return true;
 }
 Element e = (Element)content;
 if (e.omitStart() && e.content != null) {
 return e.content.first(token);
 }
 return false;
 */
        }
    }

    public Element first(){
        switch(type){
            case '&':
            case '|':
            case '*':
            case '?':
                return null;
            case '+':
            case ',':
                return ((ContentModel)content).first();
            default:
                return (Element)content;
        }
    }

    public String toString(){
        switch(type){
            case '*':
                return content+"*";
            case '?':
                return content+"?";
            case '+':
                return content+"+";
            case ',':
            case '|':
            case '&':
                char data[]={' ',(char)type,' '};
                String str="";
                for(ContentModel m=(ContentModel)content;m!=null;m=m.next){
                    str=str+m;
                    if(m.next!=null){
                        str+=new String(data);
                    }
                }
                return "("+str+")";
            default:
                return content.toString();
        }
    }
}
