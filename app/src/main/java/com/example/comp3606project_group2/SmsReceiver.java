package com.example.comp3606project_group2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) return;
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        SmsMessage[] parts = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (parts == null || parts.length == 0) return;

        String from = parts[0].getOriginatingAddress() == null
                ? "Unknown"
                : parts[0].getOriginatingAddress();

        StringBuilder body = new StringBuilder();
        for (SmsMessage m : parts) {
            if (m != null && m.getMessageBody() != null) {
                body.append(m.getMessageBody());
            }
        }

        String message = body.toString().trim();
        Log.d(TAG, "Incoming SMS from " + from + ": " + message);

        if (message.isEmpty()) return;

        String trimmed = message.trim().toUpperCase();

        if (trimmed.length() != 1) {
            Log.d(TAG, "Ignoring SMS because it's not a single-letter reply: " + trimmed);
            return;
        }

        char answerChar = trimmed.charAt(0);

        if (answerChar < 'A' || answerChar > 'D') {
            Log.d(TAG, "Ignoring SMS because letter is not A–D: " + answerChar);
            return;
        }

        Intent ui = new Intent("QUIZ_INCOMING_SMS");
        ui.putExtra("from", from);
        ui.putExtra("body", message);
        context.sendBroadcast(ui);

        SpellingQuestion q = QuizActivity.getCurrentQuestion();
        if (q == null) return;

        long expiry = QuizActivity.getCurrentExpiryTime();
        long now = System.currentTimeMillis();
        boolean timedOut = expiry > 0 && now > expiry;

        char correct = q.getCorrectOption();
        boolean correctAns = !timedOut && answerChar == correct;

        QuizResult result = new QuizResult(
                from, q.getWord(), answerChar, correct, correctAns, timedOut, now
        );
        QuizStorage.saveResult(context, result);
        QuizActivity.recordResult(correctAns);

        QuizActivity.stopTimerExternally();

        SmsManager sms = SmsManager.getDefault();
        if (timedOut) {
            sms.sendTextMessage(from, null,
                    "Time's up! The correct answer was: " + correct,
                    null, null);
        } else if (correctAns) {
            sms.sendTextMessage(from, null,
                    "Correct! Great job! (" + answerChar + ")",
                    null, null);
        } else {
            sms.sendTextMessage(from, null,
                    "Incorrect. You answered " + answerChar +
                            ". Correct: " + correct,
                    null, null);
        }

        Intent fullResult = new Intent("QUIZ_INCOMING_SMS");
        fullResult.putExtra("correct", correctAns);
        fullResult.putExtra("timedOut", timedOut);
        fullResult.putExtra("correctAnswer", correct);
        context.sendBroadcast(fullResult);
    }
}
