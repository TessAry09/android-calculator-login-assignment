package com.example.logincalculator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginActivity extends Activity {
    private static final String TAG = "ASSIGNMENT_FLOW";
    private static final long BACKGROUND_DURATION_MS = 10_000L;

    private EditText studentIdInput;
    private EditText passwordInput;
    private Button loginButton;
    private LinearLayout progressPanel;
    private ProgressBar progressBar;
    private TextView progressText;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean loading = new AtomicBoolean(false);
    private Future<?> progressTask;
    private boolean returnHandled;
    private boolean receiverRegistered;

    private final BroadcastReceiver calculationFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!AppContract.ACTION_CALC_FINISHED.equals(intent.getAction()) || returnHandled) {
                return;
            }
            String originalId = intent.getStringExtra(AppContract.EXTRA_STUDENT_ID);
            String resultM = intent.getStringExtra(AppContract.EXTRA_RESULT_M);
            if (originalId == null || resultM == null) {
                return;
            }
            returnHandled = true;
            String preservedPassword = passwordInput.getText().toString();
            Log.i(TAG, "ACTION_CALC_FINISHED received; rebuilding login task, M=" + resultM);

            Intent restart = new Intent(LoginActivity.this, LoginActivity.class);
            restart.putExtra(AppContract.EXTRA_STUDENT_ID, originalId + "-" + resultM);
            restart.putExtra(AppContract.EXTRA_PASSWORD, preservedPassword);
            restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(restart);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        studentIdInput = findViewById(R.id.student_id_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        progressPanel = findViewById(R.id.progress_panel);
        progressBar = findViewById(R.id.auth_progress);
        progressText = findViewById(R.id.progress_text);

        String returnedStudent = getIntent().getStringExtra(AppContract.EXTRA_STUDENT_ID);
        String returnedPassword = getIntent().getStringExtra(AppContract.EXTRA_PASSWORD);
        if (returnedStudent != null) {
            studentIdInput.setText(returnedStudent);
        }
        if (returnedPassword != null) {
            passwordInput.setText(returnedPassword);
        }

        registerCalculationReceiver();
        loginButton.setOnClickListener(v -> requestAuthentication());
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag") // Flag overload is unavailable below API 33.
    private void registerCalculationReceiver() {
        IntentFilter filter = new IntentFilter(AppContract.ACTION_CALC_FINISHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(calculationFinishedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(calculationFinishedReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void requestAuthentication() {
        if (loading.get()) {
            return;
        }
        String studentId = studentIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        Intent authIntent = new Intent(AppContract.ACTION_AUTH);
        authIntent.setComponent(new ComponentName(this, AuthReceiver.class));
        authIntent.putExtra(AppContract.EXTRA_STUDENT_ID, studentId);
        authIntent.putExtra(AppContract.EXTRA_PASSWORD, password);

        BroadcastReceiver finalReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = getResultExtras(false);
                String message = extras == null ? "鉴权服务未返回结果。" :
                        extras.getString(AppContract.EXTRA_MESSAGE, "鉴权失败。");
                if (getResultCode() == AppContract.AUTH_SUCCESS) {
                    startBackgroundProgress(studentId);
                } else {
                    showAuthFailure(message);
                }
            }
        };

        Log.i(TAG, "Sending explicit ordered ACTION_AUTH for student=" + studentId);
        sendOrderedBroadcast(authIntent, null, finalReceiver, mainHandler,
                AppContract.AUTH_FAILURE, null, null);
    }

    private void showAuthFailure(String message) {
        Log.i(TAG, "Authentication failed: " + message);
        new AlertDialog.Builder(this)
                .setTitle("鉴权失败")
                .setMessage(message)
                .setPositiveButton("知道了", null)
                .show();
    }

    private void startBackgroundProgress(String studentId) {
        if (!loading.compareAndSet(false, true)) {
            return;
        }
        loginButton.setEnabled(false);
        studentIdInput.setEnabled(false);
        passwordInput.setEnabled(false);
        progressPanel.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressText.setText(getString(R.string.loading_progress, 0));

        final long startedAt = SystemClock.elapsedRealtime();
        Log.i(TAG, "10-second background task START elapsed=" + startedAt);
        progressTask = executor.submit(() -> {
            for (int progress = 1; progress <= 100; progress++) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                SystemClock.sleep(BACKGROUND_DURATION_MS / 100L);
                final int value = progress;
                mainHandler.post(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    progressBar.setProgress(value);
                    progressText.setText(getString(R.string.loading_progress, value));
                    if (value == 100 && loading.compareAndSet(true, false)) {
                        long elapsed = SystemClock.elapsedRealtime() - startedAt;
                        Log.i(TAG, "10-second background task END durationMs=" + elapsed);
                        Intent calculator = new Intent(LoginActivity.this, CalculatorActivity.class);
                        calculator.putExtra(AppContract.EXTRA_STUDENT_ID, studentId);
                        startActivity(calculator);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(calculationFinishedReceiver);
            receiverRegistered = false;
        }
        if (progressTask != null && !progressTask.isDone()) {
            progressTask.cancel(true);
        }
        executor.shutdownNow();
        super.onDestroy();
    }
}
