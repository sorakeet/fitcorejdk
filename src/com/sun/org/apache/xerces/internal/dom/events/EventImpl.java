/**
 * Copyright (c) 2007, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * <p>
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
/**
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package com.sun.org.apache.xerces.internal.dom.events;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

public class EventImpl implements Event{
    public String type=null;
    public EventTarget target;
    public EventTarget currentTarget;
    public short eventPhase;
    public boolean initialized=false, bubbles=true, cancelable=false;
    public boolean stopPropagation=false, preventDefault=false;
    protected long timeStamp=System.currentTimeMillis();

    public String getType(){
        return type;
    }

    public EventTarget getTarget(){
        return target;
    }

    public EventTarget getCurrentTarget(){
        return currentTarget;
    }

    public short getEventPhase(){
        return eventPhase;
    }

    public boolean getBubbles(){
        return bubbles;
    }

    public boolean getCancelable(){
        return cancelable;
    }

    public long getTimeStamp(){
        return timeStamp;
    }

    public void stopPropagation(){
        stopPropagation=true;
    }

    public void preventDefault(){
        preventDefault=true;
    }

    public void initEvent(String eventTypeArg,boolean canBubbleArg,
                          boolean cancelableArg){
        type=eventTypeArg;
        bubbles=canBubbleArg;
        cancelable=cancelableArg;
        initialized=true;
    }
}
