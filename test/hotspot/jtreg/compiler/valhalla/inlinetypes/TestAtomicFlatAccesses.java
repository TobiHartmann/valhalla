/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
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

package compiler.valhalla.inlinetypes;

import compiler.lib.ir_framework.*;
import jdk.test.lib.Asserts;


import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import jdk.experimental.bytecode.TypeTag;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import jdk.internal.value.ValueClass;
import jdk.internal.vm.annotation.ImplicitlyConstructible;
import jdk.internal.vm.annotation.LooselyConsistentValue;
import jdk.internal.vm.annotation.NullRestricted;

import static compiler.valhalla.inlinetypes.InlineTypeIRNode.*;
import static compiler.valhalla.inlinetypes.InlineTypes.*;

/*
 * @test
 * @key randomness
 * @summary TODO
 * @library /test/lib /test/jdk/lib/testlibrary/bytecode /test/jdk/java/lang/invoke/common /
 * @requires (os.simpleArch == "x64" | os.simpleArch == "aarch64")
 * @modules java.base/jdk.internal.value
 * @build jdk.experimental.bytecode.BasicClassBuilder
 * @compile --add-exports java.base/jdk.internal.vm.annotation=ALL-UNNAMED
 *          --add-exports java.base/jdk.internal.value=ALL-UNNAMED TestAtomicFlatAccesses.java
 * @run main/othervm/timeout=450 -XX:+EnableValhalla compiler.valhalla.inlinetypes.TestAtomicFlatAccesses
 */

public class TestAtomicFlatAccesses {

    public static void main(String[] args) {
//        Scenario[] scenarios = InlineTypes.DEFAULT_SCENARIOS;
//        scenarios[3].addFlags("-XX:-MonomorphicArrayCheck", "-XX:FlatArrayElementMaxSize=-1");
//        scenarios[4].addFlags("-XX:-MonomorphicArrayCheck");

        InlineTypes.getFramework()
                 //  .addScenarios(scenarios)
                   .addFlags("--add-exports", "java.base/jdk.internal.vm.annotation=ALL-UNNAMED",
                             "--add-exports", "java.base/jdk.internal.value=ALL-UNNAMED")
                   .start();
    }

    final Random RAND = AbstractInfo.getRandom();

// TODO comment
    @DontInline
    static void preventFolding() { }
    @DontInline
    static void preventFolding(Object obj) { }

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue1 {
        int i1;
        int i2;

        @ForceInline
        public MyValue1(int i1, int i2) {
            this.i1 = i1;
            this.i2 = i2;
        }

        public String toString() {
            return i1 + " " + i2;
        }
    }

    @NullRestricted
    MyValue1 field1;

// TODO add tests that could be folded, folding does not work with atomic accesses (yet)

    @Test
    @IR(failOn = {IRNode.STORE_B, IRNode.STORE_I, IRNode.STORE_C, IRNode.LOAD_B, IRNode.LOAD_I, IRNode.LOAD_S},
        counts = {IRNode.STORE_L, "= 1", IRNode.LOAD_L, "= 1"})
    public MyValue1 test1Field(int i1, int i2) {
        field1 = new MyValue1(i1, i2);
        preventFolding();
        return field1;
    }

    @Run(test = "test1Field")
    public void test1Field_verifier() {
        int i1 = RAND.nextInt();
        int i2 = RAND.nextInt();
        MyValue1 result = test1Field(i1, i2);
        MyValue1 expected = new MyValue1(i1, i2);
        Asserts.assertEQ(result, expected);
    }

// TODO more array tests

    @Test
    @IR(failOn = {IRNode.STORE_B, IRNode.STORE_C, IRNode.LOAD_B, IRNode.LOAD_I, IRNode.LOAD_S},
        counts = {IRNode.STORE_I, "= 1", IRNode.STORE_L, "= 3", IRNode.LOAD_L, "= 2"})
    public MyValue1 test1Array(int i1, int i2) {
        MyValue1[] array = (MyValue1[])ValueClass.newNullRestrictedArray(MyValue1.class, 1);
        array[0] = new MyValue1(i1, i2);
        preventFolding(array);
        return array[0];
    }

    @Run(test = "test1Array")
    public void test1Array_verifier() {
        int i1 = RAND.nextInt();
        int i2 = RAND.nextInt();
        MyValue1 result = test1Array(i1, i2);
        MyValue1 expected = new MyValue1(i1, i2);
        Asserts.assertEQ(result, expected);
    }

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue2 {
        byte b1;
        byte b2;
        byte b3;
        byte b4;
        byte b5;
        byte b6;
        byte b7;
        byte b8;

        @ForceInline
        public MyValue2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
            this.b1 = b1;
            this.b2 = b2;
            this.b3 = b3;
            this.b4 = b4;
            this.b5 = b5;
            this.b6 = b6;
            this.b7 = b7;
            this.b8 = b8;
        }

        public String toString() {
            return b1 + " " + b2 + " " + b3 + " " + b4 + " " + b5 + " " + b6 + " " + b7 + " " + b8;
        }
    }

    @NullRestricted
    MyValue2 field2;

    @ForceInline
    public void testWrite2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        field2 = new MyValue2(b1, b2, b3, b4, b5, b6, b7, b8);
    }

    @ForceInline
    public MyValue2 testRead2() {
        return field2;
    }

    @DontInline
    public MyValue2 test2Helper(MyValue2 val) {
        return val;
    }

    @Test
    @IR(failOn = {IRNode.STORE_B, IRNode.STORE_I, IRNode.STORE_C, IRNode.LOAD_B, IRNode.LOAD_I, IRNode.LOAD_S},
        counts = {IRNode.STORE_L, "= 1", IRNode.LOAD_L, "= 1"})
    public MyValue2 testWriteRead2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        testWrite2(b1, b2, b3, b4, b5, b6, b7, b8);
        preventFolding();
        MyValue2 res = testRead2();
        // Hide the buffering for the return value (too many registers) from the IR
        return test2Helper(res);
    }

    @Run(test = "testWriteRead2")
    public void testWriteRead2_verifier() {
        byte b1 = (byte)RAND.nextInt();
        byte b2 = (byte)RAND.nextInt();
        byte b3 = (byte)RAND.nextInt();
        byte b4 = (byte)RAND.nextInt();
        byte b5 = (byte)RAND.nextInt();
        byte b6 = (byte)RAND.nextInt();
        byte b7 = (byte)RAND.nextInt();
        byte b8 = (byte)RAND.nextInt();

        MyValue2 result = testWriteRead2(b1, b2, b3, b4, b5, b6, b7, b8);
        MyValue2 expected = new MyValue2(b1, b2, b3, b4, b5, b6, b7, b8);
        Asserts.assertEQ(result, expected);
    }

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue3 {
        byte b;
        short s;
        boolean bol;
        char c;

        @ForceInline
        public MyValue3(byte b, short s, boolean bol, char c) {
            this.b = b;
            this.s = s;
            this.bol = bol;
            this.c = c;
        }

        public String toString() {
            return b + " " + s + " " + bol + " " + c;
        }
    }

    @NullRestricted
    MyValue3 field3;

    @ForceInline
    public void testWrite3(byte b, short s, boolean bol, char c) {
        field3 = new MyValue3(b, s, bol, c);
    }

    @ForceInline
    public MyValue3 testRead3() {
        return field3;
    }

    @Test
    @IR(failOn = {IRNode.STORE_B, IRNode.STORE_I, IRNode.STORE_C, IRNode.LOAD_B, IRNode.LOAD_I, IRNode.LOAD_S},
        counts = {IRNode.STORE_L, "= 1", IRNode.LOAD_L, "= 1"})
    public MyValue3 testWriteRead3(byte b, short s, boolean bol, char c) {
        testWrite3(b, s, bol, c);
        preventFolding();
        return testRead3();
    }

    @Run(test = "testWriteRead3")
    public void testWriteRead3_verifier() {
        byte b = (byte)RAND.nextInt();
        short s = (short)RAND.nextInt();
        boolean bol = RAND.nextBoolean();
        char c = (char)RAND.nextInt();

        MyValue3 result = testWriteRead3(b, s, bol, c);
        MyValue3 expected = new MyValue3(b, s, bol, c);
        Asserts.assertEQ(result, expected);
    }

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue4 {
        byte b1;

        @NullRestricted
        MyValue3 val;

        byte b2;

        @ForceInline
        public MyValue4(byte b1, byte b2, short s, boolean bol, char c, byte b3) {
            this.b1 = b1;
            this.val = new MyValue3(b3, s, bol, c);
            this.b2 = b2;
        }

        public String toString() {
            return b1 + " " + val + " " + b2;
        }
    }

    @NullRestricted
    MyValue4 field4;

    @ForceInline
    public void testWrite4(byte b1, byte b2, short s, boolean bol, char c, byte b3) {
        field4 = new MyValue4(b1, b2, s, bol, c, b3);
    }

    @ForceInline
    public MyValue4 testRead4() {
        return field4;
    }

    @Test
    @IR(failOn = {IRNode.STORE_B, IRNode.STORE_I, IRNode.STORE_C, IRNode.LOAD_B, IRNode.LOAD_I, IRNode.LOAD_S},
        counts = {IRNode.STORE_L, "= 1", IRNode.LOAD_L, "= 1"})
    public MyValue4 testWriteRead4(byte b1, byte b2, short s, boolean bol, char c, byte b3) {
        testWrite4(b1, b2, s, bol, c, b3);
        preventFolding();
        return testRead4();
    }

    @Run(test = "testWriteRead4")
    public void testWriteRead4_verifier() {
        byte b1 = (byte)RAND.nextInt();
        byte b2 = (byte)RAND.nextInt();
        byte b3 = (byte)RAND.nextInt();
        short s = (short)RAND.nextInt();
        boolean bol = RAND.nextBoolean();
        char c = (char)RAND.nextInt();

        MyValue4 result = testWriteRead4(b1, b2, s, bol, c, b3);
        MyValue4 expected = new MyValue4(b1, b2, s, bol, c, b3);
        Asserts.assertEQ(result, expected);
    }

// TODO tests for flat arrays

}
