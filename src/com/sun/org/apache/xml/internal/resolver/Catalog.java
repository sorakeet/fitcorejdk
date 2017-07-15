/**
 * Copyright (c) 2005, 2015, Oracle and/or its affiliates. All rights reserved.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Catalog.java - Represents OASIS Open Catalog files.
package com.sun.org.apache.xml.internal.resolver;

import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;
import com.sun.org.apache.xerces.internal.utils.SecuritySupport;
import com.sun.org.apache.xml.internal.resolver.helpers.FileURL;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import com.sun.org.apache.xml.internal.resolver.readers.CatalogReader;
import com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader;
import com.sun.org.apache.xml.internal.resolver.readers.SAXCatalogReader;
import com.sun.org.apache.xml.internal.resolver.readers.TR9401CatalogReader;

import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Catalog{
    public static final int BASE=CatalogEntry.addEntryType("BASE",1);
    public static final int CATALOG=CatalogEntry.addEntryType("CATALOG",1);
    public static final int DOCUMENT=CatalogEntry.addEntryType("DOCUMENT",1);
    public static final int OVERRIDE=CatalogEntry.addEntryType("OVERRIDE",1);
    public static final int SGMLDECL=CatalogEntry.addEntryType("SGMLDECL",1);
    public static final int DELEGATE_PUBLIC=CatalogEntry.addEntryType("DELEGATE_PUBLIC",2);
    public static final int DELEGATE_SYSTEM=CatalogEntry.addEntryType("DELEGATE_SYSTEM",2);
    public static final int DELEGATE_URI=CatalogEntry.addEntryType("DELEGATE_URI",2);
    public static final int DOCTYPE=CatalogEntry.addEntryType("DOCTYPE",2);
    public static final int DTDDECL=CatalogEntry.addEntryType("DTDDECL",2);
    public static final int ENTITY=CatalogEntry.addEntryType("ENTITY",2);
    public static final int LINKTYPE=CatalogEntry.addEntryType("LINKTYPE",2);
    public static final int NOTATION=CatalogEntry.addEntryType("NOTATION",2);
    public static final int PUBLIC=CatalogEntry.addEntryType("PUBLIC",2);
    public static final int SYSTEM=CatalogEntry.addEntryType("SYSTEM",2);
    public static final int URI=CatalogEntry.addEntryType("URI",2);
    public static final int REWRITE_SYSTEM=CatalogEntry.addEntryType("REWRITE_SYSTEM",2);
    public static final int REWRITE_URI=CatalogEntry.addEntryType("REWRITE_URI",2);
    public static final int SYSTEM_SUFFIX=CatalogEntry.addEntryType("SYSTEM_SUFFIX",2);
    public static final int URI_SUFFIX=CatalogEntry.addEntryType("URI_SUFFIX",2);
    protected URL base;
    protected URL catalogCwd;
    protected Vector catalogEntries=new Vector();
    protected boolean default_override=true;
    protected CatalogManager catalogManager=CatalogManager.getStaticManager();
    protected Vector catalogFiles=new Vector();
    protected Vector localCatalogFiles=new Vector();
    protected Vector catalogs=new Vector();
    protected Vector localDelegate=new Vector();
    protected Map<String,Integer> readerMap=new HashMap<>();
    protected Vector readerArr=new Vector();

    public Catalog(){
        // nop;
    }

    public Catalog(CatalogManager manager){
        catalogManager=manager;
    }

    public CatalogManager getCatalogManager(){
        return catalogManager;
    }

    public void setCatalogManager(CatalogManager manager){
        catalogManager=manager;
    }

    public void setupReaders(){
        SAXParserFactory spf=catalogManager.useServicesMechanism()?
                SAXParserFactory.newInstance():new SAXParserFactoryImpl();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        SAXCatalogReader saxReader=new SAXCatalogReader(spf);
        saxReader.setCatalogParser(null,"XMLCatalog",
                "com.sun.org.apache.xml.internal.resolver.readers.XCatalogReader");
        saxReader.setCatalogParser(OASISXMLCatalogReader.namespaceName,
                "catalog",
                "com.sun.org.apache.xml.internal.resolver.readers.OASISXMLCatalogReader");
        addReader("application/xml",saxReader);
        TR9401CatalogReader textReader=new TR9401CatalogReader();
        addReader("text/plain",textReader);
    }

    public void addReader(String mimeType,CatalogReader reader){
        if(readerMap.containsKey(mimeType)){
            Integer pos=readerMap.get(mimeType);
            readerArr.set(pos,reader);
        }else{
            readerArr.add(reader);
            Integer pos=readerArr.size()-1;
            readerMap.put(mimeType,pos);
        }
    }

    public String getCurrentBase(){
        return base.toString();
    }

    public String getDefaultOverride(){
        if(default_override){
            return "yes";
        }else{
            return "no";
        }
    }

    public void loadSystemCatalogs()
            throws MalformedURLException, IOException{
        Vector catalogs=catalogManager.getCatalogFiles();
        if(catalogs!=null){
            for(int count=0;count<catalogs.size();count++){
                catalogFiles.addElement(catalogs.elementAt(count));
            }
        }
        if(catalogFiles.size()>0){
            // This is a little odd. The parseCatalog() method expects
            // a filename, but it adds that name to the end of the
            // catalogFiles vector, and then processes that vector.
            // This allows the system to handle CATALOG entries
            // correctly.
            //
            // In this init case, we take the last element off the
            // catalogFiles vector and pass it to parseCatalog. This
            // will "do the right thing" in the init case, and allow
            // parseCatalog() to do the right thing in the non-init
            // case. Honest.
            //
            String catfile=(String)catalogFiles.lastElement();
            catalogFiles.removeElement(catfile);
            parseCatalog(catfile);
        }
    }

    public synchronized void parseCatalog(String fileName)
            throws MalformedURLException, IOException{
        default_override=catalogManager.getPreferPublic();
        catalogManager.debug.message(4,"Parse catalog: "+fileName);
        // Put the file into the list of catalogs to process...
        // In all cases except the case when initCatalog() is the
        // caller, this will be the only catalog initially in the list...
        catalogFiles.addElement(fileName);
        // Now process all the pending catalogs...
        parsePendingCatalogs();
    }

    protected synchronized void parsePendingCatalogs()
            throws MalformedURLException, IOException{
        if(!localCatalogFiles.isEmpty()){
            // Move all the localCatalogFiles into the front of
            // the catalogFiles queue
            Vector newQueue=new Vector();
            Enumeration q=localCatalogFiles.elements();
            while(q.hasMoreElements()){
                newQueue.addElement(q.nextElement());
            }
            // Put the rest of the catalogs on the end of the new list
            for(int curCat=0;curCat<catalogFiles.size();curCat++){
                String catfile=(String)catalogFiles.elementAt(curCat);
                newQueue.addElement(catfile);
            }
            catalogFiles=newQueue;
            localCatalogFiles.clear();
        }
        // Suppose there are no catalog files to process, but the
        // single catalog already parsed included some delegate
        // entries? Make sure they don't get lost.
        if(catalogFiles.isEmpty()&&!localDelegate.isEmpty()){
            Enumeration e=localDelegate.elements();
            while(e.hasMoreElements()){
                catalogEntries.addElement(e.nextElement());
            }
            localDelegate.clear();
        }
        // Now process all the files on the catalogFiles vector. This
        // vector can grow during processing if CATALOG entries are
        // encountered in the catalog
        while(!catalogFiles.isEmpty()){
            String catfile=(String)catalogFiles.elementAt(0);
            try{
                catalogFiles.remove(0);
            }catch(ArrayIndexOutOfBoundsException e){
                // can't happen
            }
            if(catalogEntries.size()==0&&catalogs.size()==0){
                // We haven't parsed any catalogs yet, let this
                // catalog be the first...
                try{
                    parseCatalogFile(catfile);
                }catch(CatalogException ce){
                    System.out.println("FIXME: "+ce.toString());
                }
            }else{
                // This is a subordinate catalog. We save its name,
                // but don't bother to load it unless it's necessary.
                catalogs.addElement(catfile);
            }
            if(!localCatalogFiles.isEmpty()){
                // Move all the localCatalogFiles into the front of
                // the catalogFiles queue
                Vector newQueue=new Vector();
                Enumeration q=localCatalogFiles.elements();
                while(q.hasMoreElements()){
                    newQueue.addElement(q.nextElement());
                }
                // Put the rest of the catalogs on the end of the new list
                for(int curCat=0;curCat<catalogFiles.size();curCat++){
                    catfile=(String)catalogFiles.elementAt(curCat);
                    newQueue.addElement(catfile);
                }
                catalogFiles=newQueue;
                localCatalogFiles.clear();
            }
            if(!localDelegate.isEmpty()){
                Enumeration e=localDelegate.elements();
                while(e.hasMoreElements()){
                    catalogEntries.addElement(e.nextElement());
                }
                localDelegate.clear();
            }
        }
        // We've parsed them all, reinit the vector...
        catalogFiles.clear();
    }

    protected synchronized void parseCatalogFile(String fileName)
            throws MalformedURLException, IOException, CatalogException{
        CatalogEntry entry;
        // The base-base is the cwd. If the catalog file is specified
        // with a relative path, this assures that it gets resolved
        // properly...
        try{
            // tack on a basename because URLs point to files not dirs
            catalogCwd=FileURL.makeURL("basename");
        }catch(MalformedURLException e){
            catalogManager.debug.message(1,"Malformed URL on cwd","user.dir");
            catalogCwd=null;
        }
        // The initial base URI is the location of the catalog file
        try{
            base=new URL(catalogCwd,fixSlashes(fileName));
        }catch(MalformedURLException e){
            try{
                base=new URL("file:"+fixSlashes(fileName));
            }catch(MalformedURLException e2){
                catalogManager.debug.message(1,"Malformed URL on catalog filename",
                        fixSlashes(fileName));
                base=null;
            }
        }
        catalogManager.debug.message(2,"Loading catalog",fileName);
        catalogManager.debug.message(4,"Default BASE",base.toString());
        fileName=base.toString();
        DataInputStream inStream=null;
        boolean parsed=false;
        boolean notFound=false;
        for(int count=0;!parsed&&count<readerArr.size();count++){
            CatalogReader reader=(CatalogReader)readerArr.get(count);
            try{
                notFound=false;
                inStream=new DataInputStream(base.openStream());
            }catch(FileNotFoundException fnfe){
                // No catalog; give up!
                notFound=true;
                break;
            }
            try{
                reader.readCatalog(this,inStream);
                parsed=true;
            }catch(CatalogException ce){
                if(ce.getExceptionType()==CatalogException.PARSE_FAILED){
                    // give up!
                    break;
                }else{
                    // try again!
                }
            }
            try{
                inStream.close();
            }catch(IOException e){
                //nop
            }
        }
        if(!parsed){
            if(notFound){
                catalogManager.debug.message(3,"Catalog does not exist",fileName);
            }else{
                catalogManager.debug.message(1,"Failed to parse catalog",fileName);
            }
        }
    }

    protected String fixSlashes(String sysid){
        return sysid.replace('\\','/');
    }

    public synchronized void parseCatalog(String mimeType,InputStream is)
            throws IOException, CatalogException{
        default_override=catalogManager.getPreferPublic();
        catalogManager.debug.message(4,"Parse "+mimeType+" catalog on input stream");
        CatalogReader reader=null;
        if(readerMap.containsKey(mimeType)){
            int arrayPos=((Integer)readerMap.get(mimeType)).intValue();
            reader=(CatalogReader)readerArr.get(arrayPos);
        }
        if(reader==null){
            String msg="No CatalogReader for MIME type: "+mimeType;
            catalogManager.debug.message(2,msg);
            throw new CatalogException(CatalogException.UNPARSEABLE,msg);
        }
        reader.readCatalog(this,is);
        // Now process all the pending catalogs...
        parsePendingCatalogs();
    }

    public synchronized void parseCatalog(URL aUrl) throws IOException{
        catalogCwd=aUrl;
        base=aUrl;
        default_override=catalogManager.getPreferPublic();
        catalogManager.debug.message(4,"Parse catalog: "+aUrl.toString());
        DataInputStream inStream=null;
        boolean parsed=false;
        for(int count=0;!parsed&&count<readerArr.size();count++){
            CatalogReader reader=(CatalogReader)readerArr.get(count);
            try{
                inStream=new DataInputStream(aUrl.openStream());
            }catch(FileNotFoundException fnfe){
                // No catalog; give up!
                break;
            }
            try{
                reader.readCatalog(this,inStream);
                parsed=true;
            }catch(CatalogException ce){
                if(ce.getExceptionType()==CatalogException.PARSE_FAILED){
                    // give up!
                    break;
                }else{
                    // try again!
                }
            }
            try{
                inStream.close();
            }catch(IOException e){
                //nop
            }
        }
        if(parsed) parsePendingCatalogs();
    }

    public void addEntry(CatalogEntry entry){
        int type=entry.getEntryType();
        if(type==BASE){
            String value=entry.getEntryArg(0);
            URL newbase=null;
            if(base==null){
                catalogManager.debug.message(5,"BASE CUR","null");
            }else{
                catalogManager.debug.message(5,"BASE CUR",base.toString());
            }
            catalogManager.debug.message(4,"BASE STR",value);
            try{
                value=fixSlashes(value);
                newbase=new URL(base,value);
            }catch(MalformedURLException e){
                try{
                    newbase=new URL("file:"+value);
                }catch(MalformedURLException e2){
                    catalogManager.debug.message(1,"Malformed URL on base",value);
                    newbase=null;
                }
            }
            if(newbase!=null){
                base=newbase;
            }
            catalogManager.debug.message(5,"BASE NEW",base.toString());
        }else if(type==CATALOG){
            String fsi=makeAbsolute(entry.getEntryArg(0));
            catalogManager.debug.message(4,"CATALOG",fsi);
            localCatalogFiles.addElement(fsi);
        }else if(type==PUBLIC){
            String publicid=PublicId.normalize(entry.getEntryArg(0));
            String systemid=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,publicid);
            entry.setEntryArg(1,systemid);
            catalogManager.debug.message(4,"PUBLIC",publicid,systemid);
            catalogEntries.addElement(entry);
        }else if(type==SYSTEM){
            String systemid=normalizeURI(entry.getEntryArg(0));
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"SYSTEM",systemid,fsi);
            catalogEntries.addElement(entry);
        }else if(type==URI){
            String uri=normalizeURI(entry.getEntryArg(0));
            String altURI=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,altURI);
            catalogManager.debug.message(4,"URI",uri,altURI);
            catalogEntries.addElement(entry);
        }else if(type==DOCUMENT){
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(0)));
            entry.setEntryArg(0,fsi);
            catalogManager.debug.message(4,"DOCUMENT",fsi);
            catalogEntries.addElement(entry);
        }else if(type==OVERRIDE){
            catalogManager.debug.message(4,"OVERRIDE",entry.getEntryArg(0));
            catalogEntries.addElement(entry);
        }else if(type==SGMLDECL){
            // meaningless in XML
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(0)));
            entry.setEntryArg(0,fsi);
            catalogManager.debug.message(4,"SGMLDECL",fsi);
            catalogEntries.addElement(entry);
        }else if(type==DELEGATE_PUBLIC){
            String ppi=PublicId.normalize(entry.getEntryArg(0));
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,ppi);
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"DELEGATE_PUBLIC",ppi,fsi);
            addDelegate(entry);
        }else if(type==DELEGATE_SYSTEM){
            String psi=normalizeURI(entry.getEntryArg(0));
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,psi);
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"DELEGATE_SYSTEM",psi,fsi);
            addDelegate(entry);
        }else if(type==DELEGATE_URI){
            String pui=normalizeURI(entry.getEntryArg(0));
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,pui);
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"DELEGATE_URI",pui,fsi);
            addDelegate(entry);
        }else if(type==REWRITE_SYSTEM){
            String psi=normalizeURI(entry.getEntryArg(0));
            String rpx=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,psi);
            entry.setEntryArg(1,rpx);
            catalogManager.debug.message(4,"REWRITE_SYSTEM",psi,rpx);
            catalogEntries.addElement(entry);
        }else if(type==REWRITE_URI){
            String pui=normalizeURI(entry.getEntryArg(0));
            String upx=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,pui);
            entry.setEntryArg(1,upx);
            catalogManager.debug.message(4,"REWRITE_URI",pui,upx);
            catalogEntries.addElement(entry);
        }else if(type==SYSTEM_SUFFIX){
            String pui=normalizeURI(entry.getEntryArg(0));
            String upx=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,pui);
            entry.setEntryArg(1,upx);
            catalogManager.debug.message(4,"SYSTEM_SUFFIX",pui,upx);
            catalogEntries.addElement(entry);
        }else if(type==URI_SUFFIX){
            String pui=normalizeURI(entry.getEntryArg(0));
            String upx=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(0,pui);
            entry.setEntryArg(1,upx);
            catalogManager.debug.message(4,"URI_SUFFIX",pui,upx);
            catalogEntries.addElement(entry);
        }else if(type==DOCTYPE){
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"DOCTYPE",entry.getEntryArg(0),fsi);
            catalogEntries.addElement(entry);
        }else if(type==DTDDECL){
            // meaningless in XML
            String fpi=PublicId.normalize(entry.getEntryArg(0));
            entry.setEntryArg(0,fpi);
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"DTDDECL",fpi,fsi);
            catalogEntries.addElement(entry);
        }else if(type==ENTITY){
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"ENTITY",entry.getEntryArg(0),fsi);
            catalogEntries.addElement(entry);
        }else if(type==LINKTYPE){
            // meaningless in XML
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"LINKTYPE",entry.getEntryArg(0),fsi);
            catalogEntries.addElement(entry);
        }else if(type==NOTATION){
            String fsi=makeAbsolute(normalizeURI(entry.getEntryArg(1)));
            entry.setEntryArg(1,fsi);
            catalogManager.debug.message(4,"NOTATION",entry.getEntryArg(0),fsi);
            catalogEntries.addElement(entry);
        }else{
            catalogEntries.addElement(entry);
        }
    }

    protected String makeAbsolute(String sysid){
        URL local=null;
        sysid=fixSlashes(sysid);
        try{
            local=new URL(base,sysid);
        }catch(MalformedURLException e){
            catalogManager.debug.message(1,"Malformed URL on system identifier",sysid);
        }
        if(local!=null){
            return local.toString();
        }else{
            return sysid;
        }
    }

    protected String normalizeURI(String uriref){
        if(uriref==null){
            return null;
        }
        byte[] bytes;
        try{
            bytes=uriref.getBytes("UTF-8");
        }catch(UnsupportedEncodingException uee){
            // this can't happen
            catalogManager.debug.message(1,"UTF-8 is an unsupported encoding!?");
            return uriref;
        }
        StringBuilder newRef=new StringBuilder(bytes.length);
        for(int count=0;count<bytes.length;count++){
            int ch=bytes[count]&0xFF;
            if((ch<=0x20)    // ctrl
                    ||(ch>0x7F)  // high ascii
                    ||(ch==0x22) // "
                    ||(ch==0x3C) // <
                    ||(ch==0x3E) // >
                    ||(ch==0x5C) // \
                    ||(ch==0x5E) // ^
                    ||(ch==0x60) // `
                    ||(ch==0x7B) // {
                    ||(ch==0x7C) // |
                    ||(ch==0x7D) // }
                    ||(ch==0x7F)){
                newRef.append(encodedByte(ch));
            }else{
                newRef.append((char)bytes[count]);
            }
        }
        return newRef.toString();
    }

    protected String encodedByte(int b){
        String hex=Integer.toHexString(b).toUpperCase();
        if(hex.length()<2){
            return "%0"+hex;
        }else{
            return "%"+hex;
        }
    }

    protected void addDelegate(CatalogEntry entry){
        int pos=0;
        String partial=entry.getEntryArg(0);
        Enumeration local=localDelegate.elements();
        while(local.hasMoreElements()){
            CatalogEntry dpe=(CatalogEntry)local.nextElement();
            String dp=dpe.getEntryArg(0);
            if(dp.equals(partial)){
                // we already have this prefix
                return;
            }
            if(dp.length()>partial.length()){
                pos++;
            }
            if(dp.length()<partial.length()){
                break;
            }
        }
        // now insert partial into the vector at [pos]
        if(localDelegate.size()==0){
            localDelegate.addElement(entry);
        }else{
            localDelegate.insertElementAt(entry,pos);
        }
    }

    public void unknownEntry(Vector strings){
        if(strings!=null&&strings.size()>0){
            String keyword=(String)strings.elementAt(0);
            catalogManager.debug.message(2,"Unrecognized token parsing catalog",keyword);
        }
    }

    public void parseAllCatalogs()
            throws MalformedURLException, IOException{
        // Parse all the subordinate catalogs
        for(int catPos=0;catPos<catalogs.size();catPos++){
            Catalog c=null;
            try{
                c=(Catalog)catalogs.elementAt(catPos);
            }catch(ClassCastException e){
                String catfile=(String)catalogs.elementAt(catPos);
                c=newCatalog();
                c.parseCatalog(catfile);
                catalogs.setElementAt(c,catPos);
                c.parseAllCatalogs();
            }
        }
        // Parse all the DELEGATE catalogs
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==DELEGATE_PUBLIC
                    ||e.getEntryType()==DELEGATE_SYSTEM
                    ||e.getEntryType()==DELEGATE_URI){
                Catalog dcat=newCatalog();
                dcat.parseCatalog(e.getEntryArg(1));
            }
        }
    }

    protected Catalog newCatalog(){
        String catalogClass=this.getClass().getName();
        try{
            Catalog c=(Catalog)(Class.forName(catalogClass).newInstance());
            c.setCatalogManager(catalogManager);
            copyReaders(c);
            return c;
        }catch(ClassNotFoundException cnfe){
            catalogManager.debug.message(1,"Class Not Found Exception: "+catalogClass);
        }catch(IllegalAccessException iae){
            catalogManager.debug.message(1,"Illegal Access Exception: "+catalogClass);
        }catch(InstantiationException ie){
            catalogManager.debug.message(1,"Instantiation Exception: "+catalogClass);
        }catch(ClassCastException cce){
            catalogManager.debug.message(1,"Class Cast Exception: "+catalogClass);
        }catch(Exception e){
            catalogManager.debug.message(1,"Other Exception: "+catalogClass);
        }
        Catalog c=new Catalog();
        c.setCatalogManager(catalogManager);
        copyReaders(c);
        return c;
    }

    protected void copyReaders(Catalog newCatalog){
        // Have to copy the readers in the right order...convert hash to arr
        Vector mapArr=new Vector(readerMap.size());
        // Pad the mapArr out to the right length
        for(int count=0;count<readerMap.size();count++){
            mapArr.add(null);
        }
        for(Map.Entry<String,Integer> entry : readerMap.entrySet()){
            mapArr.set(entry.getValue(),entry.getKey());
        }
        for(int count=0;count<mapArr.size();count++){
            String mimeType=(String)mapArr.get(count);
            Integer pos=readerMap.get(mimeType);
            newCatalog.addReader(mimeType,
                    (CatalogReader)
                            readerArr.get(pos));
        }
    }

    public String resolveDoctype(String entityName,
                                 String publicId,
                                 String systemId)
            throws MalformedURLException, IOException{
        String resolved=null;
        catalogManager.debug.message(3,"resolveDoctype("
                +entityName+","+publicId+","+systemId+")");
        systemId=normalizeURI(systemId);
        if(publicId!=null&&publicId.startsWith("urn:publicid:")){
            publicId=PublicId.decodeURN(publicId);
        }
        if(systemId!=null&&systemId.startsWith("urn:publicid:")){
            systemId=PublicId.decodeURN(systemId);
            if(publicId!=null&&!publicId.equals(systemId)){
                catalogManager.debug.message(1,"urn:publicid: system identifier differs from public identifier; using public identifier");
                systemId=null;
            }else{
                publicId=systemId;
                systemId=null;
            }
        }
        if(systemId!=null){
            // If there's a SYSTEM entry in this catalog, use it
            resolved=resolveLocalSystem(systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        if(publicId!=null){
            // If there's a PUBLIC entry in this catalog, use it
            resolved=resolveLocalPublic(DOCTYPE,
                    entityName,
                    publicId,
                    systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        // If there's a DOCTYPE entry in this catalog, use it
        boolean over=default_override;
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==OVERRIDE){
                over=e.getEntryArg(0).equalsIgnoreCase("YES");
                continue;
            }
            if(e.getEntryType()==DOCTYPE
                    &&e.getEntryArg(0).equals(entityName)){
                if(over||systemId==null){
                    return e.getEntryArg(1);
                }
            }
        }
        // Otherwise, look in the subordinate catalogs
        return resolveSubordinateCatalogs(DOCTYPE,
                entityName,
                publicId,
                systemId);
    }

    public String resolveDocument()
            throws MalformedURLException, IOException{
        // If there's a DOCUMENT entry, return it
        catalogManager.debug.message(3,"resolveDocument");
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==DOCUMENT){
                return e.getEntryArg(0);
            }
        }
        return resolveSubordinateCatalogs(DOCUMENT,
                null,null,null);
    }

    protected synchronized String resolveSubordinateCatalogs(int entityType,
                                                             String entityName,
                                                             String publicId,
                                                             String systemId)
            throws MalformedURLException, IOException{
        for(int catPos=0;catPos<catalogs.size();catPos++){
            Catalog c=null;
            try{
                c=(Catalog)catalogs.elementAt(catPos);
            }catch(ClassCastException e){
                String catfile=(String)catalogs.elementAt(catPos);
                c=newCatalog();
                try{
                    c.parseCatalog(catfile);
                }catch(MalformedURLException mue){
                    catalogManager.debug.message(1,"Malformed Catalog URL",catfile);
                }catch(FileNotFoundException fnfe){
                    catalogManager.debug.message(1,"Failed to load catalog, file not found",
                            catfile);
                }catch(IOException ioe){
                    catalogManager.debug.message(1,"Failed to load catalog, I/O error",catfile);
                }
                catalogs.setElementAt(c,catPos);
            }
            String resolved=null;
            // Ok, now what are we supposed to call here?
            if(entityType==DOCTYPE){
                resolved=c.resolveDoctype(entityName,
                        publicId,
                        systemId);
            }else if(entityType==DOCUMENT){
                resolved=c.resolveDocument();
            }else if(entityType==ENTITY){
                resolved=c.resolveEntity(entityName,
                        publicId,
                        systemId);
            }else if(entityType==NOTATION){
                resolved=c.resolveNotation(entityName,
                        publicId,
                        systemId);
            }else if(entityType==PUBLIC){
                resolved=c.resolvePublic(publicId,systemId);
            }else if(entityType==SYSTEM){
                resolved=c.resolveSystem(systemId);
            }else if(entityType==URI){
                resolved=c.resolveURI(systemId);
            }
            if(resolved!=null){
                return resolved;
            }
        }
        return null;
    }

    public String resolveEntity(String entityName,
                                String publicId,
                                String systemId)
            throws MalformedURLException, IOException{
        String resolved=null;
        catalogManager.debug.message(3,"resolveEntity("
                +entityName+","+publicId+","+systemId+")");
        systemId=normalizeURI(systemId);
        if(publicId!=null&&publicId.startsWith("urn:publicid:")){
            publicId=PublicId.decodeURN(publicId);
        }
        if(systemId!=null&&systemId.startsWith("urn:publicid:")){
            systemId=PublicId.decodeURN(systemId);
            if(publicId!=null&&!publicId.equals(systemId)){
                catalogManager.debug.message(1,"urn:publicid: system identifier differs from public identifier; using public identifier");
                systemId=null;
            }else{
                publicId=systemId;
                systemId=null;
            }
        }
        if(systemId!=null){
            // If there's a SYSTEM entry in this catalog, use it
            resolved=resolveLocalSystem(systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        if(publicId!=null){
            // If there's a PUBLIC entry in this catalog, use it
            resolved=resolveLocalPublic(ENTITY,
                    entityName,
                    publicId,
                    systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        // If there's a ENTITY entry in this catalog, use it
        boolean over=default_override;
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==OVERRIDE){
                over=e.getEntryArg(0).equalsIgnoreCase("YES");
                continue;
            }
            if(e.getEntryType()==ENTITY
                    &&e.getEntryArg(0).equals(entityName)){
                if(over||systemId==null){
                    return e.getEntryArg(1);
                }
            }
        }
        // Otherwise, look in the subordinate catalogs
        return resolveSubordinateCatalogs(ENTITY,
                entityName,
                publicId,
                systemId);
    }

    public String resolveNotation(String notationName,
                                  String publicId,
                                  String systemId)
            throws MalformedURLException, IOException{
        String resolved=null;
        catalogManager.debug.message(3,"resolveNotation("
                +notationName+","+publicId+","+systemId+")");
        systemId=normalizeURI(systemId);
        if(publicId!=null&&publicId.startsWith("urn:publicid:")){
            publicId=PublicId.decodeURN(publicId);
        }
        if(systemId!=null&&systemId.startsWith("urn:publicid:")){
            systemId=PublicId.decodeURN(systemId);
            if(publicId!=null&&!publicId.equals(systemId)){
                catalogManager.debug.message(1,"urn:publicid: system identifier differs from public identifier; using public identifier");
                systemId=null;
            }else{
                publicId=systemId;
                systemId=null;
            }
        }
        if(systemId!=null){
            // If there's a SYSTEM entry in this catalog, use it
            resolved=resolveLocalSystem(systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        if(publicId!=null){
            // If there's a PUBLIC entry in this catalog, use it
            resolved=resolveLocalPublic(NOTATION,
                    notationName,
                    publicId,
                    systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        // If there's a NOTATION entry in this catalog, use it
        boolean over=default_override;
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==OVERRIDE){
                over=e.getEntryArg(0).equalsIgnoreCase("YES");
                continue;
            }
            if(e.getEntryType()==NOTATION
                    &&e.getEntryArg(0).equals(notationName)){
                if(over||systemId==null){
                    return e.getEntryArg(1);
                }
            }
        }
        // Otherwise, look in the subordinate catalogs
        return resolveSubordinateCatalogs(NOTATION,
                notationName,
                publicId,
                systemId);
    }

    public String resolvePublic(String publicId,String systemId)
            throws MalformedURLException, IOException{
        catalogManager.debug.message(3,"resolvePublic("+publicId+","+systemId+")");
        systemId=normalizeURI(systemId);
        if(publicId!=null&&publicId.startsWith("urn:publicid:")){
            publicId=PublicId.decodeURN(publicId);
        }
        if(systemId!=null&&systemId.startsWith("urn:publicid:")){
            systemId=PublicId.decodeURN(systemId);
            if(publicId!=null&&!publicId.equals(systemId)){
                catalogManager.debug.message(1,"urn:publicid: system identifier differs from public identifier; using public identifier");
                systemId=null;
            }else{
                publicId=systemId;
                systemId=null;
            }
        }
        // If there's a SYSTEM entry in this catalog, use it
        if(systemId!=null){
            String resolved=resolveLocalSystem(systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        // If there's a PUBLIC entry in this catalog, use it
        String resolved=resolveLocalPublic(PUBLIC,
                null,
                publicId,
                systemId);
        if(resolved!=null){
            return resolved;
        }
        // Otherwise, look in the subordinate catalogs
        return resolveSubordinateCatalogs(PUBLIC,
                null,
                publicId,
                systemId);
    }
    // -----------------------------------------------------------------

    protected synchronized String resolveLocalPublic(int entityType,
                                                     String entityName,
                                                     String publicId,
                                                     String systemId)
            throws MalformedURLException, IOException{
        // Always normalize the public identifier before attempting a match
        publicId=PublicId.normalize(publicId);
        // If there's a SYSTEM entry in this catalog, use it
        if(systemId!=null){
            String resolved=resolveLocalSystem(systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        // If there's a PUBLIC entry in this catalog, use it
        boolean over=default_override;
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==OVERRIDE){
                over=e.getEntryArg(0).equalsIgnoreCase("YES");
                continue;
            }
            if(e.getEntryType()==PUBLIC
                    &&e.getEntryArg(0).equals(publicId)){
                if(over||systemId==null){
                    return e.getEntryArg(1);
                }
            }
        }
        // If there's a DELEGATE_PUBLIC entry in this catalog, use it
        over=default_override;
        en=catalogEntries.elements();
        Vector delCats=new Vector();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==OVERRIDE){
                over=e.getEntryArg(0).equalsIgnoreCase("YES");
                continue;
            }
            if(e.getEntryType()==DELEGATE_PUBLIC
                    &&(over||systemId==null)){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=publicId.length()
                        &&p.equals(publicId.substring(0,p.length()))){
                    // delegate this match to the other catalog
                    delCats.addElement(e.getEntryArg(1));
                }
            }
        }
        if(delCats.size()>0){
            Enumeration enCats=delCats.elements();
            if(catalogManager.debug.getDebug()>1){
                catalogManager.debug.message(2,"Switching to delegated catalog(s):");
                while(enCats.hasMoreElements()){
                    String delegatedCatalog=(String)enCats.nextElement();
                    catalogManager.debug.message(2,"\t"+delegatedCatalog);
                }
            }
            Catalog dcat=newCatalog();
            enCats=delCats.elements();
            while(enCats.hasMoreElements()){
                String delegatedCatalog=(String)enCats.nextElement();
                dcat.parseCatalog(delegatedCatalog);
            }
            return dcat.resolvePublic(publicId,null);
        }
        // Nada!
        return null;
    }

    public String resolveSystem(String systemId)
            throws MalformedURLException, IOException{
        catalogManager.debug.message(3,"resolveSystem("+systemId+")");
        systemId=normalizeURI(systemId);
        if(systemId!=null&&systemId.startsWith("urn:publicid:")){
            systemId=PublicId.decodeURN(systemId);
            return resolvePublic(systemId,null);
        }
        // If there's a SYSTEM entry in this catalog, use it
        if(systemId!=null){
            String resolved=resolveLocalSystem(systemId);
            if(resolved!=null){
                return resolved;
            }
        }
        // Otherwise, look in the subordinate catalogs
        return resolveSubordinateCatalogs(SYSTEM,
                null,
                null,
                systemId);
    }

    protected String resolveLocalSystem(String systemId)
            throws MalformedURLException, IOException{
        String osname=SecuritySupport.getSystemProperty("os.name");
        boolean windows=(osname.indexOf("Windows")>=0);
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==SYSTEM
                    &&(e.getEntryArg(0).equals(systemId)
                    ||(windows
                    &&e.getEntryArg(0).equalsIgnoreCase(systemId)))){
                return e.getEntryArg(1);
            }
        }
        // If there's a REWRITE_SYSTEM entry in this catalog, use it
        en=catalogEntries.elements();
        String startString=null;
        String prefix=null;
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==REWRITE_SYSTEM){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=systemId.length()
                        &&p.equals(systemId.substring(0,p.length()))){
                    // Is this the longest prefix?
                    if(startString==null
                            ||p.length()>startString.length()){
                        startString=p;
                        prefix=e.getEntryArg(1);
                    }
                }
            }
        }
        if(prefix!=null){
            // return the systemId with the new prefix
            return prefix+systemId.substring(startString.length());
        }
        // If there's a SYSTEM_SUFFIX entry in this catalog, use it
        en=catalogEntries.elements();
        String suffixString=null;
        String suffixURI=null;
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==SYSTEM_SUFFIX){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=systemId.length()
                        &&systemId.endsWith(p)){
                    // Is this the longest prefix?
                    if(suffixString==null
                            ||p.length()>suffixString.length()){
                        suffixString=p;
                        suffixURI=e.getEntryArg(1);
                    }
                }
            }
        }
        if(suffixURI!=null){
            // return the systemId for the suffix
            return suffixURI;
        }
        // If there's a DELEGATE_SYSTEM entry in this catalog, use it
        en=catalogEntries.elements();
        Vector delCats=new Vector();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==DELEGATE_SYSTEM){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=systemId.length()
                        &&p.equals(systemId.substring(0,p.length()))){
                    // delegate this match to the other catalog
                    delCats.addElement(e.getEntryArg(1));
                }
            }
        }
        if(delCats.size()>0){
            Enumeration enCats=delCats.elements();
            if(catalogManager.debug.getDebug()>1){
                catalogManager.debug.message(2,"Switching to delegated catalog(s):");
                while(enCats.hasMoreElements()){
                    String delegatedCatalog=(String)enCats.nextElement();
                    catalogManager.debug.message(2,"\t"+delegatedCatalog);
                }
            }
            Catalog dcat=newCatalog();
            enCats=delCats.elements();
            while(enCats.hasMoreElements()){
                String delegatedCatalog=(String)enCats.nextElement();
                dcat.parseCatalog(delegatedCatalog);
            }
            return dcat.resolveSystem(systemId);
        }
        return null;
    }

    public String resolveURI(String uri)
            throws MalformedURLException, IOException{
        catalogManager.debug.message(3,"resolveURI("+uri+")");
        uri=normalizeURI(uri);
        if(uri!=null&&uri.startsWith("urn:publicid:")){
            uri=PublicId.decodeURN(uri);
            return resolvePublic(uri,null);
        }
        // If there's a URI entry in this catalog, use it
        if(uri!=null){
            String resolved=resolveLocalURI(uri);
            if(resolved!=null){
                return resolved;
            }
        }
        // Otherwise, look in the subordinate catalogs
        return resolveSubordinateCatalogs(URI,
                null,
                null,
                uri);
    }
    // -----------------------------------------------------------------

    protected String resolveLocalURI(String uri)
            throws MalformedURLException, IOException{
        Enumeration en=catalogEntries.elements();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==URI
                    &&(e.getEntryArg(0).equals(uri))){
                return e.getEntryArg(1);
            }
        }
        // If there's a REWRITE_URI entry in this catalog, use it
        en=catalogEntries.elements();
        String startString=null;
        String prefix=null;
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==REWRITE_URI){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=uri.length()
                        &&p.equals(uri.substring(0,p.length()))){
                    // Is this the longest prefix?
                    if(startString==null
                            ||p.length()>startString.length()){
                        startString=p;
                        prefix=e.getEntryArg(1);
                    }
                }
            }
        }
        if(prefix!=null){
            // return the uri with the new prefix
            return prefix+uri.substring(startString.length());
        }
        // If there's a URI_SUFFIX entry in this catalog, use it
        en=catalogEntries.elements();
        String suffixString=null;
        String suffixURI=null;
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==URI_SUFFIX){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=uri.length()
                        &&uri.endsWith(p)){
                    // Is this the longest prefix?
                    if(suffixString==null
                            ||p.length()>suffixString.length()){
                        suffixString=p;
                        suffixURI=e.getEntryArg(1);
                    }
                }
            }
        }
        if(suffixURI!=null){
            // return the uri for the suffix
            return suffixURI;
        }
        // If there's a DELEGATE_URI entry in this catalog, use it
        en=catalogEntries.elements();
        Vector delCats=new Vector();
        while(en.hasMoreElements()){
            CatalogEntry e=(CatalogEntry)en.nextElement();
            if(e.getEntryType()==DELEGATE_URI){
                String p=(String)e.getEntryArg(0);
                if(p.length()<=uri.length()
                        &&p.equals(uri.substring(0,p.length()))){
                    // delegate this match to the other catalog
                    delCats.addElement(e.getEntryArg(1));
                }
            }
        }
        if(delCats.size()>0){
            Enumeration enCats=delCats.elements();
            if(catalogManager.debug.getDebug()>1){
                catalogManager.debug.message(2,"Switching to delegated catalog(s):");
                while(enCats.hasMoreElements()){
                    String delegatedCatalog=(String)enCats.nextElement();
                    catalogManager.debug.message(2,"\t"+delegatedCatalog);
                }
            }
            Catalog dcat=newCatalog();
            enCats=delCats.elements();
            while(enCats.hasMoreElements()){
                String delegatedCatalog=(String)enCats.nextElement();
                dcat.parseCatalog(delegatedCatalog);
            }
            return dcat.resolveURI(uri);
        }
        return null;
    }
}
