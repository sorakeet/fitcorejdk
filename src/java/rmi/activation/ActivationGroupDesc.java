/**
 * Copyright (c) 1997, 2008, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package java.rmi.activation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.util.Arrays;
import java.util.Properties;

public final class ActivationGroupDesc implements Serializable{
    private static final long serialVersionUID=-4936225423168276595L;
    private String className;
    private String location;
    private MarshalledObject<?> data;
    private CommandEnvironment env;
    private Properties props;

    public ActivationGroupDesc(Properties overrides,
                               CommandEnvironment cmd){
        this(null,null,null,overrides,cmd);
    }

    public ActivationGroupDesc(String className,
                               String location,
                               MarshalledObject<?> data,
                               Properties overrides,
                               CommandEnvironment cmd){
        this.props=overrides;
        this.env=cmd;
        this.data=data;
        this.location=location;
        this.className=className;
    }

    public String getClassName(){
        return className;
    }

    public String getLocation(){
        return location;
    }

    public MarshalledObject<?> getData(){
        return data;
    }

    public Properties getPropertyOverrides(){
        return (props!=null)?(Properties)props.clone():null;
    }

    public CommandEnvironment getCommandEnvironment(){
        return this.env;
    }

    public static class CommandEnvironment implements Serializable{
        private static final long serialVersionUID=6165754737887770191L;
        private String command;
        private String[] options;

        public CommandEnvironment(String cmdpath,
                                  String[] argv){
            this.command=cmdpath;     // might be null
            // Hold a safe copy of argv in this.options
            if(argv==null){
                this.options=new String[0];
            }else{
                this.options=new String[argv.length];
                System.arraycopy(argv,0,this.options,0,argv.length);
            }
        }

        public String getCommandPath(){
            return (this.command);
        }

        public String[] getCommandOptions(){
            return options.clone();
        }

        public int hashCode(){
            // hash command and ignore possibly expensive options
            return (command==null?0:command.hashCode());
        }

        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException{
            in.defaultReadObject();
            if(options==null){
                options=new String[0];
            }
        }        public boolean equals(Object obj){
            if(obj instanceof CommandEnvironment){
                CommandEnvironment env=(CommandEnvironment)obj;
                return
                        ((command==null?env.command==null:
                                command.equals(env.command))&&
                                Arrays.equals(options,env.options));
            }else{
                return false;
            }
        }


    }

    public boolean equals(Object obj){
        if(obj instanceof ActivationGroupDesc){
            ActivationGroupDesc desc=(ActivationGroupDesc)obj;
            return
                    ((className==null?desc.className==null:
                            className.equals(desc.className))&&
                            (location==null?desc.location==null:
                                    location.equals(desc.location))&&
                            (data==null?desc.data==null:data.equals(desc.data))&&
                            (env==null?desc.env==null:env.equals(desc.env))&&
                            (props==null?desc.props==null:
                                    props.equals(desc.props)));
        }else{
            return false;
        }
    }

    public int hashCode(){
        // hash location, className, data, and env
        // but omit props (may be expensive)
        return ((location==null
                ?0
                :location.hashCode()<<24)^
                (env==null
                        ?0
                        :env.hashCode()<<16)^
                (className==null
                        ?0
                        :className.hashCode()<<8)^
                (data==null
                        ?0
                        :data.hashCode()));
    }
}
