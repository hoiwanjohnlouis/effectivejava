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

public abstract class BadWorkQueue {

    private final List queue = new LinkedList();
    private boolean stopped = false;

    protected BadWorkQueue() { new BadWorkerThread().start(); };

    //
    public final void enqueue(final Object workItem) {
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

    // Broken - invokes alien method from synchronized block
    private class BadWorkerThread extends Thread {
        public void run() {
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
                    // we have data to process.
                    Object workItem = queue.remove(0);
                    try {
                        processItem(workItem);  // Lock held while calling external function!, broken logic
                    } catch (InterruptedException e) {
                        // todo: write error msg
                        return;
                    }
                }
            }
        }
    }
}
