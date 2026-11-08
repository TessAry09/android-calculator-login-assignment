package com.example.logincalculator;

public final class AppContract {
    public static final String ACTION_AUTH = "com.example.logincalculator.ACTION_AUTH";
    public static final String ACTION_CALC_FINISHED = "com.example.logincalculator.ACTION_CALC_FINISHED";

    public static final String EXTRA_STUDENT_ID = "student_id";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_RESULT_M = "result_m";
    public static final String EXTRA_HISTORY = "history";
    public static final String EXTRA_MESSAGE = "message";

    public static final int AUTH_SUCCESS = 1;
    public static final int AUTH_FAILURE = 0;

    public static final String DEMO_STUDENT_ID = "2010813299";
    public static final String DEMO_PASSWORD = "123456";

    private AppContract() {
    }
}
