/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * <p>
 * $Id: DTMConfigurationException.java,v 1.2.4.1 2005/09/15 08:14:52 suresh_emailid Exp $
 */
/**
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * $Id: DTMConfigurationException.java,v 1.2.4.1 2005/09/15 08:14:52 suresh_emailid Exp $
 */
package com.sun.org.apache.xml.internal.dtm;

import javax.xml.transform.SourceLocator;

public class DTMConfigurationException extends DTMException{
    static final long serialVersionUID=-4607874078818418046L;

    public DTMConfigurationException(){
        super("Configuration Error");
    }

    public DTMConfigurationException(String msg){
        super(msg);
    }

    public DTMConfigurationException(Throwable e){
        super(e);
    }

    public DTMConfigurationException(String msg,Throwable e){
        super(msg,e);
    }

    public DTMConfigurationException(String message,
                                     SourceLocator locator){
        super(message,locator);
    }

    public DTMConfigurationException(String message,
                                     SourceLocator locator,
                                     Throwable e){
        super(message,locator,e);
    }
}
