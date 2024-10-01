/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.apkeditor.utils;

import java.util.Iterator;

public class CyclicIterator<T> implements Iterator<T> {

    private final T[] elements;
    private int mIndex;
    private int cycleCount;
    private int count;

    public CyclicIterator(T[] elements) {
        if (elements == null || elements.length == 0) {
            throw new IllegalArgumentException("Elements can not be empty");
        }
        this.elements = elements;
    }
    @Override
    public boolean hasNext() {
        return elements.length != 0;
    }

    @Override
    public T next() {
        int i = mIndex;
        T item = elements[i];
        i ++;
        int length = elements.length;
        if (i >= length) {
            i = 0;
        }
        this.mIndex = i;
        this.count ++;
        if (this.count >= length) {
            this.count = 0;
            this.cycleCount ++;
        }
        return item;
    }
    public int length() {
        return elements.length;
    }

    public int getIndex() {
        return mIndex;
    }

    public int getCycleCount() {
        return cycleCount;
    }
    public void resetCycleCount() {
        this.cycleCount = 0;
        this.count = 0;
    }

    @Override
    public String toString() {
        return "[cycle = " + getCycleCount() + ": " + getIndex()
                + "/" + length() + "] "  + elements[mIndex];
    }
}
