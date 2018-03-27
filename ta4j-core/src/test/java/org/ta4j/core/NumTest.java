/*******************************************************************************
 *   The MIT License (MIT)
 *
 *   Copyright (c) 2014-2017 Marc de Verdelhan, 2017-2018 Ta4j Organization 
 *   & respective authors (see AUTHORS)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy of
 *   this software and associated documentation files (the "Software"), to deal in
 *   the Software without restriction, including without limitation the rights to
 *   use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *   the Software, and to permit persons to whom the Software is furnished to do so,
 *   subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *   FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *   COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *   IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.ta4j.core;

import org.junit.Test;
import org.ta4j.core.indicators.AbstractIndicatorTest;
import org.ta4j.core.num.BigDecimalNum;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.function.Function;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.ta4j.core.TestUtils.assertNumEquals;
import static org.ta4j.core.num.NaN.NaN;


public class NumTest extends AbstractIndicatorTest {

    public NumTest(Function<Number, Num> numFunction) {
        super(numFunction);
    }

    @Test(expected = AssertionError.class)
    public void testStringNumFail() {
        assertNumEquals("1.234", numOf(4.321));
    }

    @Test
    public void testStringNumPass() {
        assertNumEquals("1.234", numOf(1.234));
    }

    @Test
    public void testValueOf() {
        assertNumEquals(0.33333333333333333332, numOf(0.33333333333333333332));
        assertNumEquals(1, numOf(1d));
        assertNumEquals(2.54, numOf(new BigDecimal("2.54")));

        assertNumEquals(0.33, numOf(0.33));
        assertNumEquals(1, numOf(1));
        assertNumEquals(2.54, numOf(new BigDecimal(2.54)));
    }

    @Test
    public void testMultiplicationSymmetrically() {
        Num decimalFromString = numOf(new BigDecimal("0.33"));
        Num decimalFromDouble = numOf(45.33);
        assertEquals(decimalFromString.multipliedBy(decimalFromDouble), decimalFromDouble.multipliedBy(decimalFromString));

        Num doubleNumFromString = numOf(new BigDecimal("0.33"));
        Num doubleNumFromDouble = numOf(10.33);
        assertNumEquals(doubleNumFromString.multipliedBy(doubleNumFromDouble), doubleNumFromDouble.multipliedBy(doubleNumFromString));
    }

    @Test(expected = java.lang.ClassCastException.class)
    public void testFailDifferentNumsAdd() {
        Num a = BigDecimalNum.valueOf(12);
        Num b = DoubleNum.valueOf(12);
        a.plus(b);
    }

    @Test(expected = java.lang.ClassCastException.class)
    public void testFailDifferentNumsCompare() {
        Num a = BigDecimalNum.valueOf(12);
        Num b = DoubleNum.valueOf(13);
        a.isEqual(b);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFailNaNtoInt() {
        NaN.intValue();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testFailNaNtoLong() {
        NaN.longValue();
    }


    @Test
    public void testNaN() {
        Num a = NaN;
        Num eleven = BigDecimalNum.valueOf(11);

        Num mustBeNaN = a.plus(eleven);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.minus(eleven);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.dividedBy(a);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.multipliedBy(NaN);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.max(eleven);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = eleven.min(a);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.pow(12);
        assertNumEquals(mustBeNaN, NaN);

        mustBeNaN = a.pow(a);
        assertNumEquals(mustBeNaN, NaN);

        Double nanDouble = a.doubleValue();
        assertEquals(Double.NaN, nanDouble);

        Float nanFloat = a.floatValue();
        assertEquals(Float.NaN, nanFloat);

        assertTrue(NaN.equals(a)); // NaN == NaN -> true

    }

    @Test
    public void testArithmetic() {
        Num ten = numOf(10);
        Num million = numOf(1000000);
        assertNumEquals(10, ten);
        assertNumEquals("1000000.0", million);

        Num zero = ten.minus(ten);
        assertNumEquals(0, zero);

        Num hundred = ten.multipliedBy(ten);
        assertNumEquals(100, hundred);

        Num hundredMillion = hundred.multipliedBy(million);
        assertNumEquals(100000000, hundredMillion);

        assertNumEquals(hundredMillion.dividedBy(hundred), million);
        assertNumEquals(0, hundredMillion.remainder(hundred));

        Num five = ten.numOf(5); // generate new value with NumFunction
        Num zeroDotTwo = ten.numOf(0.2); // generate new value with NumFunction
        Num fiveHundred54 = ten.numOf(554); // generate new value with NumFunction
        assertNumEquals(0, hundredMillion.remainder(five));

        assertNumEquals(0.00032, zeroDotTwo.pow(5));
        assertNumEquals(0.7247796636776955, zeroDotTwo.pow(zeroDotTwo));
        assertNumEquals(1.37972966146, zeroDotTwo.pow(numOf(-0.2)));
        assertNumEquals(554, fiveHundred54.max(five));
        assertNumEquals(5, fiveHundred54.min(five));
        assertTrue(fiveHundred54.isGreaterThan(five));
        assertFalse(five.isGreaterThan(five.function().apply(5)));
        assertFalse(five.isGreaterThanOrEqual(fiveHundred54));
        assertFalse(five.isGreaterThanOrEqual(five.function().apply(6)));
        assertTrue(five.isGreaterThanOrEqual(five.function().apply(5)));

        assertTrue(five.equals(five.function().apply(5)));
        assertTrue(five.equals(five.function().apply(5.0)));
        assertTrue(five.equals(five.function().apply((float) 5)));
        assertTrue(five.equals(five.function().apply((short) 5)));

        assertFalse(five.equals(five.function().apply(4.9)));
        assertFalse(five.equals(five.function().apply(6)));
        assertFalse(five.equals(five.function().apply((float) 15)));
        assertFalse(five.equals(five.function().apply((short) 45)));
    }

    @Test
    public void sqrtOfBigInteger() {
        String sqrtOfTwo = "1.4142135623730950488016887242096980785696718753769480731" +
            "766797379907324784621070388503875343276415727350138462309122970249248360" +
            "558507372126441214970999358314132226659275055927557999505011527820605715";

        int precision = 200;
        assertNumEquals(sqrtOfTwo, numOf(2).sqrt(precision));
    }

    @Test
    public void sqrtOfBigDouble() {
        String sqrtOfOnePointTwo = "1.095445115010332226913939565601604267905489389995966508453788899464986554245445467601716872327741252";

        int precision = 100;
        assertNumEquals(sqrtOfOnePointTwo, numOf(1.2).sqrt(precision));
    }

    @Test
    public void sqrtOfNegativeDouble() {
        assertTrue(numOf(-1.2).sqrt(12).isNaN());
        assertTrue(numOf(-1.2).sqrt().isNaN());
    }

    @Test
    public void sqrtOfZero() {
        assertNumEquals(0, numOf(0).sqrt(12));
        assertNumEquals(0, numOf(0).sqrt());
    }

    @Test
    public void sqrtOverflow() {
        assertNumEquals(1d,
            BigDecimalNum.valueOf(Double.MAX_VALUE).
                multipliedBy(BigDecimalNum.valueOf(Double.MAX_VALUE).
                    plus(BigDecimalNum.valueOf(1))).

                sqrt(100));
    }
}
