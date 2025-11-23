package com.example.comp3606project_group2;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class QuizStorage {

    private static final String FILE_NAME = "quiz_results.dat";
    private static final String TAG = "QuizStorage";

    @SuppressWarnings("unchecked")
    public static List<QuizResult> loadResults(Context context) {
        List<QuizResult> list = null;
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            list = (List<QuizResult>) ois.readObject();
        } catch (Exception e) {
            Log.d(TAG, "No existing results or error reading file: " + e.getMessage());
        }
        if (list == null) list = new ArrayList<>();
        return list;
    }

    public static void saveResult(Context context, QuizResult result) {
        List<QuizResult> list = loadResults(context);
        list.add(result);

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(list);
        } catch (Exception e) {
            Log.e(TAG, "Error writing results file: " + e.getMessage());
        }
    }
}
