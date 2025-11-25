package com.example.comp3606project_group2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private TextView textHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        textHistory = findViewById(R.id.textHistory);
        findViewById(R.id.btnBackToMain).setOnClickListener(v -> goBackToMain());
        findViewById(R.id.btnExitHistory).setOnClickListener(v -> finishAffinity());

        List<QuizResult> results = QuizStorage.loadResults(this);

        if (results == null || results.isEmpty()) {
            textHistory.setText("No quiz attempts have been saved yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (QuizResult r : results) {
            sb.append(r.toDisplayString()).append("\n\n");
        }

        textHistory.setText(sb.toString());

    }

    private void goBackToMain() {
        Intent i = new Intent(HistoryActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
