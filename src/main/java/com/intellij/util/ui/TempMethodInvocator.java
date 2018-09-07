/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Mimics com.intellij.util.MethodInvocator.
 */
@SuppressWarnings("WeakerAccess")
public class TempMethodInvocator<R> {
    private Method myMethod;
    private Constructor<?> myCtor;

    public TempMethodInvocator(String clsFQN, String method, Class<?>... parameterTypes) {
        try {
            Class<?> cls = Class.forName(clsFQN);
            myMethod = cls.getDeclaredMethod(method, parameterTypes);

            if (!myMethod.isAccessible()) {
                myMethod.setAccessible(true);
            }
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    @SuppressWarnings("unused")
    public TempMethodInvocator(String clsFQN, Class<?>... parameterTypes) {
        try {
            Class<?> cls = Class.forName(clsFQN);
            myCtor = cls.getDeclaredConstructor(parameterTypes);

            if (!myCtor.isAccessible()) {
                myCtor.setAccessible(true);
            }
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public R invoke(Object object, Object... arguments) {
        if (myMethod == null) {
            throw new IllegalStateException("Method is not available");
        }

        try {
            Object res = myMethod.invoke(object, arguments);
            //noinspection unchecked
            return res != null ? (R)res : null;
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings({"unused", "InfiniteRecursion"})
    public R staticInvoke(Object... arguments) {
        return invoke(null, arguments);
    }

    @SuppressWarnings("unused")
    public R create(Object... args) {
        if (myCtor == null) {
            throw new IllegalStateException("Ctor is not available");
        }

        try {
            //noinspection unchecked
            return (R)myCtor.newInstance(args);
        }
        catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
