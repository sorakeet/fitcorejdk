/**
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.tools;

import javax.tools.JavaFileManager.Location;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum StandardLocation implements Location{
    CLASS_OUTPUT,
    SOURCE_OUTPUT,
    CLASS_PATH,
    SOURCE_PATH,
    ANNOTATION_PROCESSOR_PATH,
    PLATFORM_CLASS_PATH,
    NATIVE_HEADER_OUTPUT;

    //where
    private static final ConcurrentMap<String,Location> locations
            =new ConcurrentHashMap<String,Location>();

    public static Location locationFor(final String name){
        if(locations.isEmpty()){
            // can't use valueOf which throws IllegalArgumentException
            for(Location location : values())
                locations.putIfAbsent(location.getName(),location);
        }
        locations.putIfAbsent(name.toString(/** null-check */),new Location(){
            public String getName(){
                return name;
            }

            public boolean isOutputLocation(){
                return name.endsWith("_OUTPUT");
            }
        });
        return locations.get(name);
    }

    public String getName(){
        return name();
    }

    public boolean isOutputLocation(){
        switch(this){
            case CLASS_OUTPUT:
            case SOURCE_OUTPUT:
            case NATIVE_HEADER_OUTPUT:
                return true;
            default:
                return false;
        }
    }
}
