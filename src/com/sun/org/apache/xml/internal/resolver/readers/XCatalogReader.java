/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// XCatalogReader.java - Read XML Catalog files
/**
 * Copyright 2001-2004 The Apache Software Foundation or its licensors,
 * as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sun.org.apache.xml.internal.resolver.readers;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import com.sun.org.apache.xml.internal.resolver.CatalogException;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import java.util.Vector;

public class XCatalogReader extends SAXCatalogReader implements SAXCatalogParser{
    protected Catalog catalog=null;

    public XCatalogReader(SAXParserFactory parserFactory){
        super(parserFactory);
    }

    public Catalog getCatalog(){
        return catalog;
    }

    public void setCatalog(Catalog catalog){
        this.catalog=catalog;
    }
    // ----------------------------------------------------------------------
    // Implement the SAX DocumentHandler interface

    public void setDocumentLocator(Locator locator){
        return;
    }

    public void startDocument()
            throws SAXException{
        return;
    }

    public void endDocument()
            throws SAXException{
        return;
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException{
        int entryType=-1;
        Vector entryArgs=new Vector();
        if(localName.equals("Base")){
            entryType=catalog.BASE;
            entryArgs.add(atts.getValue("HRef"));
            catalog.getCatalogManager().debug.message(4,"Base",atts.getValue("HRef"));
        }else if(localName.equals("Delegate")){
            entryType=catalog.DELEGATE_PUBLIC;
            entryArgs.add(atts.getValue("PublicId"));
            entryArgs.add(atts.getValue("HRef"));
            catalog.getCatalogManager().debug.message(4,"Delegate",
                    PublicId.normalize(atts.getValue("PublicId")),
                    atts.getValue("HRef"));
        }else if(localName.equals("Extend")){
            entryType=catalog.CATALOG;
            entryArgs.add(atts.getValue("HRef"));
            catalog.getCatalogManager().debug.message(4,"Extend",atts.getValue("HRef"));
        }else if(localName.equals("Map")){
            entryType=catalog.PUBLIC;
            entryArgs.add(atts.getValue("PublicId"));
            entryArgs.add(atts.getValue("HRef"));
            catalog.getCatalogManager().debug.message(4,"Map",
                    PublicId.normalize(atts.getValue("PublicId")),
                    atts.getValue("HRef"));
        }else if(localName.equals("Remap")){
            entryType=catalog.SYSTEM;
            entryArgs.add(atts.getValue("SystemId"));
            entryArgs.add(atts.getValue("HRef"));
            catalog.getCatalogManager().debug.message(4,"Remap",
                    atts.getValue("SystemId"),
                    atts.getValue("HRef"));
        }else if(localName.equals("XMLCatalog")){
            // nop, start of catalog
        }else{
            // This is equivalent to an invalid catalog entry type
            catalog.getCatalogManager().debug.message(1,"Invalid catalog entry type",localName);
        }
        if(entryType>=0){
            try{
                CatalogEntry ce=new CatalogEntry(entryType,entryArgs);
                catalog.addEntry(ce);
            }catch(CatalogException cex){
                if(cex.getExceptionType()==CatalogException.INVALID_ENTRY_TYPE){
                    catalog.getCatalogManager().debug.message(1,"Invalid catalog entry type",localName);
                }else if(cex.getExceptionType()==CatalogException.INVALID_ENTRY){
                    catalog.getCatalogManager().debug.message(1,"Invalid catalog entry",localName);
                }
            }
        }
    }

    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
            throws SAXException{
        return;
    }

    public void characters(char ch[],int start,int length)
            throws SAXException{
        return;
    }

    public void ignorableWhitespace(char ch[],int start,int length)
            throws SAXException{
        return;
    }

    public void processingInstruction(String target,String data)
            throws SAXException{
        return;
    }
}
