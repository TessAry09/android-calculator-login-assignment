package com.example.logincalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Pure Java calculator logic so arithmetic can be tested without Android UI. */
public final class CalculatorEngine {
    private static final MathContext DIVISION_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);

    private CalculatorEngine() {
    }

    public static String calculate(String left, String operator, String right) {
        BigDecimal a = parse(left);
        BigDecimal b = parse(right);
        BigDecimal result;
        switch (operator) {
            case "+":
                result = a.add(b);
                break;
            case "−":
            case "-":
                result = a.subtract(b);
                break;
            case "×":
            case "*":
                result = a.multiply(b);
                break;
            case "÷":
            case "/":
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("除数不能为 0");
                }
                result = a.divide(b, DIVISION_CONTEXT);
                break;
            default:
                throw new IllegalArgumentException("不支持的运算符: " + operator);
        }
        return format(result);
    }

    public static String negate(String value) {
        return format(parse(value).negate());
    }

    public static String percent(String value) {
        return format(parse(value).movePointLeft(2));
    }

    public static String format(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        return normalized.toPlainString();
    }

    private static BigDecimal parse(String value) {
        if (value == null || value.isEmpty() || "-".equals(value) || ".".equals(value)) {
            throw new NumberFormatException("数字格式错误");
        }
        return new BigDecimal(value);
    }
}
