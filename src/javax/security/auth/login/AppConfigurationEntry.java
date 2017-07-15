/**
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.security.auth.login;

import java.util.Collections;
import java.util.Map;

public class AppConfigurationEntry{
    private String loginModuleName;
    private LoginModuleControlFlag controlFlag;
    private Map<String,?> options;

    public AppConfigurationEntry(String loginModuleName,
                                 LoginModuleControlFlag controlFlag,
                                 Map<String,?> options){
        if(loginModuleName==null||loginModuleName.length()==0||
                (controlFlag!=LoginModuleControlFlag.REQUIRED&&
                        controlFlag!=LoginModuleControlFlag.REQUISITE&&
                        controlFlag!=LoginModuleControlFlag.SUFFICIENT&&
                        controlFlag!=LoginModuleControlFlag.OPTIONAL)||
                options==null)
            throw new IllegalArgumentException();
        this.loginModuleName=loginModuleName;
        this.controlFlag=controlFlag;
        this.options=Collections.unmodifiableMap(options);
    }

    public String getLoginModuleName(){
        return loginModuleName;
    }

    public LoginModuleControlFlag getControlFlag(){
        return controlFlag;
    }

    public Map<String,?> getOptions(){
        return options;
    }

    public static class LoginModuleControlFlag{
        public static final LoginModuleControlFlag REQUIRED=
                new LoginModuleControlFlag("required");
        public static final LoginModuleControlFlag REQUISITE=
                new LoginModuleControlFlag("requisite");
        public static final LoginModuleControlFlag SUFFICIENT=
                new LoginModuleControlFlag("sufficient");
        public static final LoginModuleControlFlag OPTIONAL=
                new LoginModuleControlFlag("optional");
        private String controlFlag;

        private LoginModuleControlFlag(String controlFlag){
            this.controlFlag=controlFlag;
        }

        public String toString(){
            return (sun.security.util.ResourcesMgr.getString
                    ("LoginModuleControlFlag.")+controlFlag);
        }
    }
}
