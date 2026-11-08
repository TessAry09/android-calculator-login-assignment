package com.example.logincalculator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/** Receives an explicit, app-private ordered broadcast and returns the auth result. */
public class AuthReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!AppContract.ACTION_AUTH.equals(intent.getAction())) {
            return;
        }

        String studentId = safe(intent.getStringExtra(AppContract.EXTRA_STUDENT_ID));
        String password = safe(intent.getStringExtra(AppContract.EXTRA_PASSWORD));
        String message;
        boolean success = false;

        if (studentId.isEmpty() || password.isEmpty()) {
            message = "学号和密码不能为空。";
        } else if (!studentId.matches("\\d{10}")) {
            message = "学号格式错误：请输入 10 位数字。";
        } else if (!AppContract.DEMO_STUDENT_ID.equals(studentId)
                || !AppContract.DEMO_PASSWORD.equals(password)) {
            message = "账号或密码错误，请使用本地演示账号。";
        } else {
            success = true;
            message = "鉴权成功";
        }

        Bundle result = new Bundle();
        result.putString(AppContract.EXTRA_MESSAGE, message);
        setResultExtras(result);
        setResultCode(success ? AppContract.AUTH_SUCCESS : AppContract.AUTH_FAILURE);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
