package com.example.logincalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class HistoryActivity extends Activity {
    private static final String TAG = "ASSIGNMENT_FLOW";
    private String studentId;
    private String resultM;
    private boolean broadcastSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        studentId = getIntent().getStringExtra(AppContract.EXTRA_STUDENT_ID);
        resultM = getIntent().getStringExtra(AppContract.EXTRA_RESULT_M);
        ArrayList<String> history = getIntent().getStringArrayListExtra(AppContract.EXTRA_HISTORY);
        if (history == null) {
            history = new ArrayList<>();
        }

        ListView historyList = findViewById(R.id.history_list);
        historyList.setAdapter(new HistoryAdapter(history));
        findViewById(R.id.finish_button).setOnClickListener(v -> finishWithBroadcast());
        Log.i(TAG, "HistoryActivity opened with real result count=" + history.size());
    }

    @Override
    public void onBackPressed() {
        finishWithBroadcast();
    }

    private void finishWithBroadcast() {
        if (broadcastSent) {
            return;
        }
        broadcastSent = true;
        Intent finished = new Intent(AppContract.ACTION_CALC_FINISHED);
        finished.setPackage(getPackageName());
        finished.putExtra(AppContract.EXTRA_STUDENT_ID, studentId);
        finished.putExtra(AppContract.EXTRA_RESULT_M, resultM);
        sendBroadcast(finished);
        Log.i(TAG, "History closed; ACTION_CALC_FINISHED sent once, M=" + resultM);
        finish();
    }

    private final class HistoryAdapter extends ArrayAdapter<String> {
        private HistoryAdapter(ArrayList<String> items) {
            super(HistoryActivity.this, R.layout.item_history, R.id.history_item_text, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.item_history, parent, false);
            }
            TextView text = row.findViewById(R.id.history_item_text);
            text.setText((position + 1) + ".  " + getItem(position));
            return row;
        }
    }
}
