/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.ui;

import java.lang.reflect.Field;

/**
 * Thread unsafe field accessor (mimics com.intellij.util.FieldAccessor).
 *
 * @param <E> the type of the field's class
 * @param <T> the type of the field
 */
@SuppressWarnings("WeakerAccess")
public class TempFieldAccessor<E, T> {
    private MyRef<Field> myFieldRef;
    private final Class<E> myClass;
    private final String myName;

    public TempFieldAccessor(String clsFQN, String name) {
        Class<E> c = null;
        try {
            //noinspection unchecked
            c = (Class<E>)Class.forName(clsFQN);
        } catch (ClassNotFoundException ignored) {
        }
        myClass = c;
        myName = name;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isAvailable() {
        if (myFieldRef == null) {
            try {
                myFieldRef = new MyRef<>();
                myFieldRef.set(myClass == null ? null : myClass.getDeclaredField(myName));
                myFieldRef.get().setAccessible(true);
            }
            catch (NoSuchFieldException ignored) {
            }
        }
        return myFieldRef.get() != null;
    }

    public T get(E object) {
        if (!isAvailable()) return null;
        try {
            @SuppressWarnings("unchecked")
            T value = (T)myFieldRef.get().get(object);
            return value;
        }
        catch (IllegalAccessException ignored) {
        }
        return null;
    }

    @SuppressWarnings("unused")
    public void set(E object, T value) {
        if (!isAvailable()) return;
        try {
            myFieldRef.get().set(object, value);
        }
        catch (IllegalAccessException ignored) {
        }
    }

    private static class MyRef<T> {
        T myValue;

        public MyRef() {
        }

        @SuppressWarnings("unused")
        public MyRef(T value) {
            myValue = value;
        }

        public void set(T value) {
            myValue = value;
        }

        public T get() {
            return myValue;
        }
    }
}
