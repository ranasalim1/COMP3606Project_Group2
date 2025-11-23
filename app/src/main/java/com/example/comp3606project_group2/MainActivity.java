package com.example.comp3606project_group2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStartQuiz;
    private Button btnViewHistory;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartQuiz = findViewById(R.id.btnStartQuiz);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnExit = findViewById(R.id.btnExit);

        btnStartQuiz.setOnClickListener(v -> {
            Intent quizIntent = new Intent(MainActivity.this, QuizActivity.class);
            startActivity(quizIntent);
        });

        btnViewHistory.setOnClickListener(v -> {
            Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(historyIntent);
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
