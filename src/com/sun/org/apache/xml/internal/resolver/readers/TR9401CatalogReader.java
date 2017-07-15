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
// TR9401CatalogReader.java - Read OASIS Catalog files
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Vector;

public class TR9401CatalogReader extends TextCatalogReader{
    public void readCatalog(Catalog catalog,InputStream is)
            throws MalformedURLException, IOException{
        catfile=is;
        if(catfile==null){
            return;
        }
        Vector unknownEntry=null;
        try{
            while(true){
                String token=nextToken();
                if(token==null){
                    if(unknownEntry!=null){
                        catalog.unknownEntry(unknownEntry);
                        unknownEntry=null;
                    }
                    catfile.close();
                    catfile=null;
                    return;
                }
                String entryToken=null;
                if(caseSensitive){
                    entryToken=token;
                }else{
                    entryToken=token.toUpperCase();
                }
                if(entryToken.equals("DELEGATE")){
                    entryToken="DELEGATE_PUBLIC";
                }
                try{
                    int type=CatalogEntry.getEntryType(entryToken);
                    int numArgs=CatalogEntry.getEntryArgCount(type);
                    Vector args=new Vector();
                    if(unknownEntry!=null){
                        catalog.unknownEntry(unknownEntry);
                        unknownEntry=null;
                    }
                    for(int count=0;count<numArgs;count++){
                        args.addElement(nextToken());
                    }
                    catalog.addEntry(new CatalogEntry(entryToken,args));
                }catch(CatalogException cex){
                    if(cex.getExceptionType()==CatalogException.INVALID_ENTRY_TYPE){
                        if(unknownEntry==null){
                            unknownEntry=new Vector();
                        }
                        unknownEntry.addElement(token);
                    }else if(cex.getExceptionType()==CatalogException.INVALID_ENTRY){
                        catalog.getCatalogManager().debug.message(1,"Invalid catalog entry",token);
                        unknownEntry=null;
                    }else if(cex.getExceptionType()==CatalogException.UNENDED_COMMENT){
                        catalog.getCatalogManager().debug.message(1,cex.getMessage());
                    }
                }
            }
        }catch(CatalogException cex2){
            if(cex2.getExceptionType()==CatalogException.UNENDED_COMMENT){
                catalog.getCatalogManager().debug.message(1,cex2.getMessage());
            }
        }
    }
}
