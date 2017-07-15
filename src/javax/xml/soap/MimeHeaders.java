/**
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.xml.soap;

import java.util.Iterator;
import java.util.Vector;

public class MimeHeaders{
    private Vector headers;

    public MimeHeaders(){
        headers=new Vector();
    }

    public String[] getHeader(String name){
        Vector values=new Vector();
        for(int i=0;i<headers.size();i++){
            MimeHeader hdr=(MimeHeader)headers.elementAt(i);
            if(hdr.getName().equalsIgnoreCase(name)
                    &&hdr.getValue()!=null)
                values.addElement(hdr.getValue());
        }
        if(values.size()==0)
            return null;
        String r[]=new String[values.size()];
        values.copyInto(r);
        return r;
    }

    public void setHeader(String name,String value){
        boolean found=false;
        if((name==null)||name.equals(""))
            throw new IllegalArgumentException("Illegal MimeHeader name");
        for(int i=0;i<headers.size();i++){
            MimeHeader hdr=(MimeHeader)headers.elementAt(i);
            if(hdr.getName().equalsIgnoreCase(name)){
                if(!found){
                    headers.setElementAt(new MimeHeader(hdr.getName(),
                            value),i);
                    found=true;
                }else
                    headers.removeElementAt(i--);
            }
        }
        if(!found)
            addHeader(name,value);
    }

    public void addHeader(String name,String value){
        if((name==null)||name.equals(""))
            throw new IllegalArgumentException("Illegal MimeHeader name");
        int pos=headers.size();
        for(int i=pos-1;i>=0;i--){
            MimeHeader hdr=(MimeHeader)headers.elementAt(i);
            if(hdr.getName().equalsIgnoreCase(name)){
                headers.insertElementAt(new MimeHeader(name,value),
                        i+1);
                return;
            }
        }
        headers.addElement(new MimeHeader(name,value));
    }

    public void removeHeader(String name){
        for(int i=0;i<headers.size();i++){
            MimeHeader hdr=(MimeHeader)headers.elementAt(i);
            if(hdr.getName().equalsIgnoreCase(name))
                headers.removeElementAt(i--);
        }
    }

    public void removeAllHeaders(){
        headers.removeAllElements();
    }

    public Iterator getAllHeaders(){
        return headers.iterator();
    }

    public Iterator getMatchingHeaders(String[] names){
        return new MatchingIterator(names,true);
    }

    public Iterator getNonMatchingHeaders(String[] names){
        return new MatchingIterator(names,false);
    }

    class MatchingIterator implements Iterator{
        private boolean match;
        private Iterator iterator;
        private String[] names;
        private Object nextHeader;

        MatchingIterator(String[] names,boolean match){
            this.match=match;
            this.names=names;
            this.iterator=headers.iterator();
        }

        private Object nextMatch(){
            next:
            while(iterator.hasNext()){
                MimeHeader hdr=(MimeHeader)iterator.next();
                if(names==null)
                    return match?null:hdr;
                for(int i=0;i<names.length;i++)
                    if(hdr.getName().equalsIgnoreCase(names[i]))
                        if(match)
                            return hdr;
                        else
                            continue next;
                if(!match)
                    return hdr;
            }
            return null;
        }

        public boolean hasNext(){
            if(nextHeader==null)
                nextHeader=nextMatch();
            return nextHeader!=null;
        }

        public Object next(){
            // hasNext should've prefetched the header for us,
            // return it.
            if(nextHeader!=null){
                Object ret=nextHeader;
                nextHeader=null;
                return ret;
            }
            if(hasNext())
                return nextHeader;
            return null;
        }

        public void remove(){
            iterator.remove();
        }
    }
}
