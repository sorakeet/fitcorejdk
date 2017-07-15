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
// FileURL.java - Construct a file: scheme URL
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
package com.sun.org.apache.xml.internal.resolver.helpers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class FileURL{
    protected FileURL(){
    }

    public static URL makeURL(String pathname) throws MalformedURLException{
        /**if (pathname.startsWith("/")) {
         return new URL("file://" + pathname);
         }

         String userdir = System.getProperty("user.dir");
         userdir.replace('\\', '/');

         if (userdir.endsWith("/")) {
         return new URL("file:///" + userdir + pathname);
         } else {
         return new URL("file:///" + userdir + "/" + pathname);
         }
         */
        File file=new File(pathname);
        return file.toURI().toURL();
    }
}
