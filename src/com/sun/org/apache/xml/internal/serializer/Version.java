/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 2005 The Apache Software Foundation.
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
 * $Id: Version.java,v 1.2 2005/09/28 13:49:09 pvedula Exp $
 */
/**
 * Copyright 2005 The Apache Software Foundation.
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
 * $Id: Version.java,v 1.2 2005/09/28 13:49:09 pvedula Exp $
 */
package com.sun.org.apache.xml.internal.serializer;

public final class Version{
    public static void _main(String argv[]){
        System.out.println(getVersion());
    }

    public static String getVersion(){
        return getProduct()+" "+getImplementationLanguage()+" "
                +getMajorVersionNum()+"."+getReleaseVersionNum()+"."
                +((getDevelopmentVersionNum()>0)?
                ("D"+getDevelopmentVersionNum()):(""+getMaintenanceVersionNum()));
    }

    public static String getProduct(){
        return "Serializer";
    }

    public static String getImplementationLanguage(){
        return "Java";
    }

    public static int getMajorVersionNum(){
        return 2;
    }

    public static int getReleaseVersionNum(){
        return 7;
    }

    public static int getMaintenanceVersionNum(){
        return 0;
    }

    public static int getDevelopmentVersionNum(){
        try{
            if((new String("")).length()==0)
                return 0;
            else
                return Integer.parseInt("");
        }catch(NumberFormatException nfe){
            return 0;
        }
    }
}
