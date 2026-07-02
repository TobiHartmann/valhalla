/*
 * Copyright (c) 2026, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8386499
 * @summary Test C2 Unsafe.getFlatValue with a nullable non-atomic flat layout
 * @requires vm.compiler2.enabled
 * @enablePreview
 * @modules java.base/jdk.internal.misc
 * @run main/othervm -XX:+UnlockDiagnosticVMOptions
 *                   -XX:+UseFieldFlattening -XX:+UseNullableNonAtomicValueFlattening
 *                   -XX:-TieredCompilation -Xcomp
 *                   -XX:CompileCommand=compileonly,${test.main.class}::test
 *                   ${test.main.class}
 */

package compiler.valhalla.inlinetypes;

import java.lang.reflect.Field;

import jdk.internal.misc.Unsafe;

public class TestGetFlatValueNullableNonAtomic {
    static final Unsafe U = Unsafe.getUnsafe();
    static final Container CONTAINER = new Container(new Value(1, 2));
    static final long OFFSET;
    static final int LAYOUT;

    static {
        try {
            Field f = Container.class.getDeclaredField("value");
            OFFSET = U.objectFieldOffset(f);
            LAYOUT = U.fieldLayout(f);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static value class Value {
        int x;
        int y;

        Value(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int sum() {
            return x + y;
        }
    }

    static value class Container {
        Value value;

        Container(Value value) {
            this.value = value;
        }
    }

    static int test() {
        Value v = U.getFlatValue(CONTAINER, OFFSET, LAYOUT, Value.class);
        return v.sum();
    }

    public static void main(String[] args) {
        if (LAYOUT != 5) {
            throw new AssertionError("expected nullable non-atomic flat layout 5, got " + LAYOUT);
        }
        int result = test();
        if (result != 3) {
            throw new AssertionError(result);
        }
    }
}
