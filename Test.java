import jdk.internal.value.ValueClass;
import jdk.internal.vm.annotation.ImplicitlyConstructible;
import jdk.internal.vm.annotation.LooselyConsistentValue;
import jdk.internal.vm.annotation.NullRestricted;

import java.util.Arrays;

import java.util.Random;

class Test {

    final static Random RAND = new Random();

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue1 {
        int x;
        int y;
        
        public MyValue1(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return x + " " + y;
        }
    }

    @NullRestricted
    MyValue1 field1;
    
    public void testWrite1(int x, int y) {
        field1 = new MyValue1(x, y);
    }

    public MyValue1 testRead1() {
        return field1;
    }

    public MyValue1 testWriteRead1(int x, int y) {
        testWrite1(x, y);
        return testRead1();
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
    
    public void testWrite2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        field2 = new MyValue2(b1, b2, b3, b4, b5, b6, b7, b8);
    }

    public MyValue2 testRead2() {
        return field2;
    }

    public MyValue2 testWriteRead2(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        testWrite2(b1, b2, b3, b4, b5, b6, b7, b8);
        return testRead2();
    }

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue3 {
        byte b;
        short s;
        boolean bol;
        char c;

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
    
    public void testWrite3(byte b, short s, boolean bol, char c) {
        field3 = new MyValue3(b, s, bol, c);
    }

    public MyValue3 testRead3() {
        return field3;
    }

    public MyValue3 testWriteRead3(byte b, short s, boolean bol, char c) {
        testWrite3(b, s, bol, c);
        return testRead3();
    }

    @ImplicitlyConstructible
    @LooselyConsistentValue
    static value class MyValue4 {
        byte b1;

        @NullRestricted
        MyValue3 val;

        byte b2;

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
    
    public void testWrite4(byte b1, byte b2, short s, boolean bol, char c, byte b3) {
        field4 = new MyValue4(b1, b2, s, bol, c, b3);
    }

    public MyValue4 testRead4() {
        return field4;
    }

    public MyValue4 testWriteRead4(byte b1, byte b2, short s, boolean bol, char c, byte b3) {
        testWrite4(b1, b2, s, bol, c, b3);
        return testRead4();
    }

// TODO test with arrays
    public static void main(String[] args) {
        Test t = new Test();
        for (int i = -50_000; i < 50_000; ++i) {
            int x = RAND.nextInt();
            int y = RAND.nextInt();
            MyValue1 result1 = t.testWriteRead1(x, y);
            MyValue1 expected1 = new MyValue1(x, y);
            if (result1 != expected1) throw new RuntimeException("Incorrect result: " + expected1 + " vs. " + result1);

            byte b1 = (byte)RAND.nextInt();
            byte b2 = (byte)RAND.nextInt();
            byte b3 = (byte)RAND.nextInt();
            byte b4 = (byte)RAND.nextInt();
            byte b5 = (byte)RAND.nextInt();
            byte b6 = (byte)RAND.nextInt();
            byte b7 = (byte)RAND.nextInt();
            byte b8 = (byte)RAND.nextInt();
            MyValue2 result2 = t.testWriteRead2(b1, b2, b3, b4, b5, b6, b7, b8);
            MyValue2 expected2 = new MyValue2(b1, b2, b3, b4, b5, b6, b7, b8);
            if (result2 != expected2) throw new RuntimeException("Incorrect result: " + expected2 + " vs. " + result2);

            byte b = (byte)RAND.nextInt();
            short s = (short)RAND.nextInt();
            boolean bol = RAND.nextBoolean();
            char c = (char)RAND.nextInt();
            MyValue3 result3 = t.testWriteRead3(b, s, bol, c);
            MyValue3 expected3 = new MyValue3(b, s, bol, c);
            if (result3 != expected3) throw new RuntimeException("Incorrect result: " + expected3 + " vs. " + result3);

            MyValue4 result4 = t.testWriteRead4(b1, b2, s, bol, c, b3);
            MyValue4 expected4 = new MyValue4(b1, b2, s, bol, c, b3);
            if (result4 != expected4) throw new RuntimeException("Incorrect result: " + expected4 + " vs. " + result4);
        }
    }
}
