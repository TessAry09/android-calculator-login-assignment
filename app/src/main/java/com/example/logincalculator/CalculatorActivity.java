package com.example.logincalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class CalculatorActivity extends Activity {
    private static final String TAG = "ASSIGNMENT_FLOW";
    private static final int MAX_INPUT_LENGTH = 24;

    private TextView displayView;
    private TextView expressionView;
    private String studentId;
    private String current = "0";
    private String leftOperand;
    private String pendingOperator;
    private String lastRightOperand;
    private boolean enteringNewNumber = true;
    private boolean showingInitialStudent = true;
    private final ArrayList<String> history = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        displayView = findViewById(R.id.display_view);
        expressionView = findViewById(R.id.expression_view);
        studentId = getIntent().getStringExtra(AppContract.EXTRA_STUDENT_ID);
        if (studentId == null || studentId.isEmpty()) {
            studentId = AppContract.DEMO_STUDENT_ID;
        }

        displayView.setText(studentId);
        expressionView.setText("鉴权通过 · 首次数字输入会开始新计算");
        Log.i(TAG, "Calculator opened with initial student display=" + studentId);
        bindKeys();
    }

    private void bindKeys() {
        int[] digits = {R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
                R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9};
        for (int id : digits) {
            findViewById(id).setOnClickListener(v -> inputDigit(((Button) v).getText().toString()));
        }

        findViewById(R.id.key_add).setOnClickListener(v -> selectOperator("+"));
        findViewById(R.id.key_subtract).setOnClickListener(v -> selectOperator("−"));
        findViewById(R.id.key_multiply).setOnClickListener(v -> selectOperator("×"));
        findViewById(R.id.key_divide).setOnClickListener(v -> selectOperator("÷"));
        findViewById(R.id.key_equals).setOnClickListener(v -> equalsAndShowHistory());
        findViewById(R.id.key_ac).setOnClickListener(v -> clearAll());
        findViewById(R.id.key_sign).setOnClickListener(v -> toggleSign());
        findViewById(R.id.key_percent).setOnClickListener(v -> applyPercent());
        findViewById(R.id.key_decimal).setOnClickListener(v -> inputDecimal());
    }

    private void inputDigit(String digit) {
        if (showingInitialStudent || enteringNewNumber) {
            current = digit;
            showingInitialStudent = false;
            enteringNewNumber = false;
        } else if ("0".equals(current)) {
            current = digit;
        } else if ("-0".equals(current)) {
            current = "-" + digit;
        } else {
            if (current.replace("-", "").replace(".", "").length() >= MAX_INPUT_LENGTH) {
                Toast.makeText(this, "数字过长，最多输入 24 位", Toast.LENGTH_SHORT).show();
                return;
            }
            current += digit;
        }
        updateDisplay();
    }

    private void inputDecimal() {
        if (showingInitialStudent || enteringNewNumber) {
            current = "0.";
            showingInitialStudent = false;
            enteringNewNumber = false;
        } else if (!current.contains(".")) {
            current += ".";
        }
        updateDisplay();
    }

    private void selectOperator(String operator) {
        if (showingInitialStudent) {
            current = studentId;
            showingInitialStudent = false;
        }

        if (pendingOperator != null && !enteringNewNumber) {
            try {
                String result = CalculatorEngine.calculate(leftOperand, pendingOperator, current);
                history.add(0, formatRecord(leftOperand, pendingOperator, current, result));
                current = result;
                leftOperand = result;
            } catch (RuntimeException error) {
                showCalculationError(error.getMessage());
                return;
            }
        } else if (pendingOperator == null) {
            leftOperand = current;
        }

        pendingOperator = operator;
        enteringNewNumber = true;
        expressionView.setText(leftOperand + " " + operator);
        displayView.setText(current);
    }

    private void equalsAndShowHistory() {
        if (pendingOperator == null) {
            Toast.makeText(this, "请先选择运算符", Toast.LENGTH_SHORT).show();
            return;
        }
        String rightOperand;
        if (enteringNewNumber) {
            if (lastRightOperand == null) {
                Toast.makeText(this, "请输入第二个数", Toast.LENGTH_SHORT).show();
                return;
            }
            rightOperand = lastRightOperand;
        } else {
            rightOperand = current;
            lastRightOperand = rightOperand;
        }

        try {
            String resultM = CalculatorEngine.calculate(leftOperand, pendingOperator, rightOperand);
            String record = formatRecord(leftOperand, pendingOperator, rightOperand, resultM);
            history.add(0, record);
            expressionView.setText(leftOperand + " " + pendingOperator + " " + rightOperand + " =");
            current = resultM;
            displayView.setText(resultM);
            enteringNewNumber = true;
            Log.i(TAG, "Calculation completed: " + record + "; opening HistoryActivity");

            Intent historyIntent = new Intent(this, HistoryActivity.class);
            historyIntent.putExtra(AppContract.EXTRA_STUDENT_ID, studentId);
            historyIntent.putExtra(AppContract.EXTRA_RESULT_M, resultM);
            historyIntent.putStringArrayListExtra(AppContract.EXTRA_HISTORY, new ArrayList<>(history));
            startActivity(historyIntent);
        } catch (RuntimeException error) {
            showCalculationError(error.getMessage());
        }
    }

    private String formatRecord(String left, String operator, String right, String result) {
        return String.format(Locale.ROOT, "%s %s %s = %s", left, operator, right, result);
    }

    private void toggleSign() {
        if (showingInitialStudent) {
            current = studentId;
            showingInitialStudent = false;
        }
        try {
            current = CalculatorEngine.negate(current);
            enteringNewNumber = false;
            updateDisplay();
        } catch (RuntimeException error) {
            showCalculationError(error.getMessage());
        }
    }

    private void applyPercent() {
        if (showingInitialStudent) {
            current = studentId;
            showingInitialStudent = false;
        }
        try {
            current = CalculatorEngine.percent(current);
            enteringNewNumber = false;
            updateDisplay();
        } catch (RuntimeException error) {
            showCalculationError(error.getMessage());
        }
    }

    private void clearAll() {
        current = "0";
        leftOperand = null;
        pendingOperator = null;
        lastRightOperand = null;
        enteringNewNumber = true;
        showingInitialStudent = false;
        expressionView.setText("已清零");
        updateDisplay();
        Log.i(TAG, "Calculator AC pressed");
    }

    private void updateDisplay() {
        displayView.setText(current);
        if (pendingOperator != null) {
            expressionView.setText(leftOperand + " " + pendingOperator + " " + current);
        }
    }

    private void showCalculationError(String message) {
        String readable = message == null ? "计算错误" : message;
        Toast.makeText(this, readable, Toast.LENGTH_LONG).show();
        Log.i(TAG, "Calculation rejected: " + readable);
    }
}
