package com.example.logincalculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class CalculatorEngineTest {
    @Test
    public void additionProducesIntegerWithoutDecimalSuffix() {
        assertEquals("100", CalculatorEngine.calculate("50", "+", "50"));
    }

    @Test
    public void supportsFourArithmeticOperations() {
        assertEquals("7", CalculatorEngine.calculate("10", "−", "3"));
        assertEquals("42", CalculatorEngine.calculate("6", "×", "7"));
        assertEquals("4", CalculatorEngine.calculate("20", "÷", "5"));
    }

    @Test
    public void divisionKeepsUsefulDecimalPrecision() {
        assertEquals("0.5", CalculatorEngine.calculate("1", "÷", "2"));
        assertEquals("0.3333333333333333", CalculatorEngine.calculate("1", "÷", "3"));
    }

    @Test
    public void signAndPercentAreSupported() {
        assertEquals("-50", CalculatorEngine.negate("50"));
        assertEquals("50", CalculatorEngine.negate("-50"));
        assertEquals("0.5", CalculatorEngine.percent("50"));
    }

    @Test
    public void divisionByZeroIsRejected() {
        ArithmeticException error = assertThrows(ArithmeticException.class,
                () -> CalculatorEngine.calculate("9", "÷", "0"));
        assertEquals("除数不能为 0", error.getMessage());
    }

    @Test
    public void malformedNumberIsRejected() {
        assertThrows(NumberFormatException.class,
                () -> CalculatorEngine.calculate(".", "+", "1"));
    }
}
