package com.ejf2015.i49;

import java.util.LinkedList;
import java.util.List;

/**
 * Copyright 9/28/15 by HW Tech Services, Inc., LLC
 * <p/>
 * Package com.hwtechservices.effectivejava
 * Project i49
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

public abstract class GoodWorkQueue {

    private final List queue = new LinkedList();
    private boolean stopped = false;

    //
    public final void enqueue(Object workItem) {
        synchronized (queue) {
            queue.add(workItem);
            queue.notify();
        }
    }

    //
    public final void stop() {
        synchronized (queue) {
            stopped = true;
            queue.notify();
        }
    }

    // user must override this with concrete implementation
    protected abstract void processItem(Object workItem) throws InterruptedException;



    // Corrected / fixed - invokes alien method outside synchronized block
    private class GoodWorkerThread extends Thread {
        public void run() {
            Object workItem = null;
            // main loop
            while (true) {
                synchronized (queue) {
                    // nothing to process, or stop processing is requested
                    try {
                        while (queue.isEmpty() && !stopped) {
                            queue.wait();
                        }
                    } catch (InterruptedException e) {
                        // todo: write error msg
                        return;
                    }
                    // stop is requested, so we are done.
                    if (stopped) {
                        // todo: write info msg
                        return;
                    }

                    // we have data to process. save for processing outside synchronized block
                    workItem = queue.remove(0);
                }

                // processItem is now outside the synchronized block.
                try {
                    processItem(workItem);  // Lock not held!, success!
                } catch (InterruptedException e) {
                    // todo: write error msg
                    return;
                }
            }
        }
    }

}