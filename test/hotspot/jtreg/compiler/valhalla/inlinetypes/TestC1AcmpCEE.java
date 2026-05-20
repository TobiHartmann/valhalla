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
 * @summary Test that C1's conditional folding preserves acmp substitutability.
 * @enablePreview
 * @library /test/lib
 * @run main ${test.main.class}
 * @run main/othervm -XX:TieredStopAtLevel=1 -Xbatch
 *                   -XX:CompileCommand=compileonly,${test.main.class}::test*
 *                   -XX:CompileCommand=inline,${test.main.class}::helper*
 *                   ${test.main.class}
 */

package compiler.valhalla.inlinetypes;

import jdk.test.lib.Asserts;

public class TestC1AcmpCEE {
    private static final Integer CONSTANT = Integer.MAX_VALUE;
    private static final Integer CONSTANT_EQUAL = 2147483647;

    // Helper to prevent javac from optimizing these shapes already
    public static int helper(boolean b) {
        if (b) {
            return 42;
        } else {
            return 0;
        }
    }

    // Test for fix (1)
    public static int testDirectIntegerConstantCompareEqual() {
        if (CONSTANT == CONSTANT_EQUAL) {
            return 42;
        } else {
            return 0;
        }
    }

    // Test for fix (2)
    public static int testDirectIntegerConstantCEEEqual() {
        return (CONSTANT == CONSTANT_EQUAL) ? 42 : 0;
    }

    // Test for fix (3)
    public static int testNestedIfOp(Integer left, Integer right) {
        // After inlining helper, the code looks like this:
        //
        //   b      = IfOp(left == right ? true : false) // needs substitutability
        //   result = IfOp(b == true ? 42 : 0)
        //
        // CEE collapses this into one conditional expression. The new IfOp
        // represents the original acmp and must keep its substitutability state.
        boolean b = (left == right);
        return helper(b);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 20_000; i++) {
            Asserts.assertEQ(42, testDirectIntegerConstantCompareEqual());
            Asserts.assertEQ(42, testDirectIntegerConstantCEEEqual());
            Asserts.assertEQ(42, testNestedIfOp(Integer.MAX_VALUE, Integer.MAX_VALUE));
            Asserts.assertEQ(0, testNestedIfOp(Integer.MAX_VALUE, Integer.MAX_VALUE + 1));
        }
    }
}

