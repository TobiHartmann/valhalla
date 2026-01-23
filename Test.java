
// ../build/fastdebug/jdk/bin/javac -J-Xint -source 27 --enable-preview --add-exports java.base/jdk.internal.value=ALL-UNNAMED --add-exports java.base/jdk.internal.value=ALL-UNNAMED Test.java && ../build/fastdebug/jdk/bin/java --enable-preview --add-exports java.base/jdk.internal.value=ALL-UNNAMED -XX:CompileCommand=compileonly,Test::testHash -Xbatch -XX:CompileCommand=quiet -XX:-TieredCompilation -XX:CompileCommand=print,Test::testHashd -XX:+UseAltSubstitutabilityMethod Test

// With release builds
// time ../build/release/images/jdk/bin/java --enable-preview --add-exports java.base/jdk.internal.value=ALL-UNNAMED -XX:CompileCommand=compileonly,Test::testHash -Xbatch -XX:CompileCommand=quiet -XX:-TieredCompilation -XX:CompileCommand=print,Test::testHashd -XX:+UseAltSubstitutabilityMethod Test

// Baseline No Preview
// ??? Need to get rid of the flat array dependency first

// Baseline Preview
// real	0m28.848s

// Fix
// real	0m10.478s

import jdk.internal.value.ValueClass;

public class Test {

    public static int testHash(Object obj) {
        return System.identityHashCode(obj);
    }

    // TODO cache the arrays
    public static Object clearHash(Object obj) {
        // Store in a flat array to make sure buffer and therefore cached hash code is lost
        Object[] array = (Object[])ValueClass.newNullRestrictedNonAtomicArray(obj.getClass(), 1, obj);
        return array[0];
    }

    // Load values from flat fields such that null markers in the buffer payload will be set
    Byte byteField1 = Byte.MIN_VALUE;
    Byte byteField2 = Byte.MAX_VALUE;
    Boolean boolField1 = false;
    Boolean boolField2 = true;
    Character charField1 = Character.MIN_VALUE;
    Character charField2 = Character.MAX_VALUE;
    Short shortField1 = Short.MIN_VALUE;
    Short shortField2 = Short.MAX_VALUE;
    Float floatField1 = Float.MIN_VALUE;
    Float floatField2 = Float.MAX_VALUE;
    Integer integerField1 = Integer.MIN_VALUE;
    Integer integerField2 = Integer.MAX_VALUE;
    Double doubleField1 = Double.MIN_VALUE;
    Double doubleField2 = Double.MAX_VALUE;
    Long longField1 = Long.MIN_VALUE;
    Long longField2 = Long.MAX_VALUE;

    public void run() {
        Object[] values = {byteField1, byteField2, Byte.MIN_VALUE, Byte.MAX_VALUE, boolField1, boolField2, false, true, charField1, charField2, Character.MIN_VALUE, Character.MAX_VALUE, shortField1, shortField2, Short.MIN_VALUE, Short.MAX_VALUE, floatField1, floatField2, Float.MIN_VALUE, Float.MAX_VALUE, integerField1, integerField2, Integer.MIN_VALUE, Integer.MAX_VALUE, doubleField1, doubleField2, Double.MIN_VALUE, Double.MAX_VALUE, longField1, longField2, Long.MIN_VALUE, Long.MAX_VALUE};
        int[] expectedResults = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            expectedResults[i] = System.identityHashCode(values[i]);
        }
        for (int i = 0; i < 1_000_000; ++i) {
            for (int j = 0; j < values.length; ++j) {
                Object value = values[j];
                value = clearHash(value);
                int res = testHash(value);
                if (res != expectedResults[j]) {
                    throw new RuntimeException("Fail for case " + j + ": " + res + " != " + expectedResults[j]);
                }
            }
        }
    }

    public static void main(String[] args) {
        Test t = new Test();
        t.run();
    }
}
