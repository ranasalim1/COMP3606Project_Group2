package com.example.comp3606project_group2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private static final int REQ_SMS_PERMISSIONS = 20001;
    public static final int TIME_LIMIT_MS = 45000;

    private EditText editPhone;
    private TextView textCurrentQuestion;
    private TextView textTimer;
    private TextView textStatus;
    private TextView textIncomingMessages;

    private CountDownTimer countDownTimer;

    private static List<SpellingQuestion> questionBank;
    private static SpellingQuestion currentQuestion;
    private static int currentIndex = 0;
    private static long currentExpiryTime = 0;

    private static QuizActivity currentActivityInstance;

    private static int totalAsked = 0;
    private static int totalCorrect = 0;

    // RECEIVER THAT GETS DATA FROM SmsReceiver
    private final BroadcastReceiver incomingSmsUiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String from = intent.getStringExtra("from");
            String body = intent.getStringExtra("body");

            if (from != null && body != null) {
                String old = textIncomingMessages.getText().toString();
                String entry = "From: " + from + "\n" + body + "\n\n";
                textIncomingMessages.setText(entry + old);
            }

            if (intent.hasExtra("correct")) {
                boolean correct = intent.getBooleanExtra("correct", false);
                boolean timedOut = intent.getBooleanExtra("timedOut", false);
                char correctAnswer = intent.getCharExtra("correctAnswer", ' ');

                if (timedOut) {
                    textStatus.setText("Answer received too late. Correct answer: " + correctAnswer);
                } else if (correct) {
                    textStatus.setText("Correct answer received!");
                } else {
                    textStatus.setText("Incorrect. Correct answer: " + correctAnswer);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        editPhone = findViewById(R.id.editPhoneNumber);
        textCurrentQuestion = findViewById(R.id.textCurrentQuestion);
        textTimer = findViewById(R.id.textTimer);
        textStatus = findViewById(R.id.textStatus);
        textIncomingMessages = findViewById(R.id.textIncomingMessages);

        ensureQuestionBank();
        updateQuestionPreview();

        findViewById(R.id.btnSendQuestion).setOnClickListener(v -> sendCurrentQuestion());
        findViewById(R.id.btnNextQuestion).setOnClickListener(v -> moveToNextQuestion());

        findViewById(R.id.btnBackToMain).setOnClickListener(v -> {
            Intent i = new Intent(QuizActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btnExitQuiz).setOnClickListener(v -> finishAffinity());
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentActivityInstance = this;

        IntentFilter filter = new IntentFilter("QUIZ_INCOMING_SMS");
        registerReceiver(incomingSmsUiReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(incomingSmsUiReceiver);
        } catch (Exception ignore) {
        }
        if (currentActivityInstance == this) currentActivityInstance = null;
    }

    private void ensureQuestionBank() {
        if (questionBank != null && !questionBank.isEmpty()) return;

        questionBank = new ArrayList<>();

        questionBank.add(new SpellingQuestion("beautiful", "Meaning: very attractive",
                "beautifull", "beautiful", "beutiful", "beutifull", 'B'));

        questionBank.add(new SpellingQuestion("chocolate", "Sweet treat made from cocoa",
                "chocalate", "chocolate", "choclate", "choclatee", 'B'));

        questionBank.add(new SpellingQuestion("environment", "The world around us",
                "enviroment", "environment", "enviroement", "enviromentt", 'B'));

        questionBank.add(new SpellingQuestion("accommodate", "To make room",
                "acommodate", "accomodate", "accommodate", "acommodete", 'C'));

        questionBank.add(new SpellingQuestion("separate", "To divide",
                "seperate", "separate", "seperete", "separatte", 'B'));

        questionBank.add(new SpellingQuestion("mutant", "A being or creature that has changed from the usual form.",
                "mutent", "mutant", "mutan", "mutantt", 'B'));

        questionBank.add(new SpellingQuestion("urgent", "Something that needs to be done right away.",
                "urgint", "urjent", "urgent", "urgant", 'C'));

        questionBank.add(new SpellingQuestion("resolve", "To fix a problem or find an answer.",
                "reolve", "resolve", "reslove", "reesolve", 'B'));

        questionBank.add(new SpellingQuestion("inferior", "Not as good as something else.",
                "inferiour", "inferor", "inferier", "inferior", 'D'));

        questionBank.add(new SpellingQuestion("recount", "To tell a story or explain something that happened.",
                "rekount", "reccount", "recounce", "recount", 'D'));

        questionBank.add(new SpellingQuestion("viewpoint", "The way someone sees or thinks about something.",
                "viewpoint", "viewpont", "viewpiont", "viewpint", 'A'));

        questionBank.add(new SpellingQuestion("sparse", "Not many of something; spread out.",
                "sparce", "sparse", "spars", "sporse", 'B'));

        questionBank.add(new SpellingQuestion("peculiar", "Strange or unusual.",
                "peculor", "peculer", "peculiar", "peculair", 'C'));

        questionBank.add(new SpellingQuestion("overcome", "To defeat or get past a challenge.",
                "overkome", "overcome", "overcom", "overccome", 'B'));

        questionBank.add(new SpellingQuestion("bestow", "To give someone an honor or gift.",
                "bestoww", "bestow", "bistow", "bestowe", 'B'));

        questionBank.add(new SpellingQuestion("decrease", "To make smaller or less.",
                "decrese", "decrase", "decrease", "decresse", 'C'));

        questionBank.add(new SpellingQuestion("thrive", "To grow strong and do very well.",
                "thive", "thrive", "thrieve", "thrave", 'B'));

        questionBank.add(new SpellingQuestion("frequent", "Happening often.",
                "frequint", "frequent", "friquent", "frequant", 'B'));

        questionBank.add(new SpellingQuestion("hesitate", "To pause because you’re unsure.",
                "hesistate", "hesitate", "hesetate", "hesatate", 'B'));

        questionBank.add(new SpellingQuestion("realistic", "Something that seems real or possible.",
                "realistick", "realistic", "realstic", "realisitc", 'B'));

        questionBank.add(new SpellingQuestion("distract", "To take someone’s attention away.",
                "distact", "disstract", "distrack", "distract", 'D'));

        questionBank.add(new SpellingQuestion("ignorant", "Not knowing something important.",
                "ignorent", "ignorant", "ignerant", "ignorrant", 'B'));

        questionBank.add(new SpellingQuestion("tarmac", "The hard surface roads and runways are made of.",
                "tarmack", "tarmec", "tarmac", "tarmak", 'C'));

        questionBank.add(new SpellingQuestion("whirlpool", "Water spinning around in a circle.",
                "wirlpool", "whirlpool", "whirpool", "whirpol", 'B'));

        questionBank.add(new SpellingQuestion("recycle", "To use something again instead of throwing it away.",
                "recyle", "recycle", "recicle", "resycle", 'B'));

        questionBank.add(new SpellingQuestion("duplicate", "To make an exact copy of something.",
                "duplicat", "dublicate", "duplicate", "duplocate", 'C'));

        questionBank.add(new SpellingQuestion("crucial", "Very important.",
                "crusial", "crucial", "cruciel", "crucal", 'B'));

        questionBank.add(new SpellingQuestion("gigantic", "Very, very big.",
                "gigantik", "gigantic", "jigantic", "giganntic", 'B'));

        questionBank.add(new SpellingQuestion("population", "The number of people in a place.",
                "populatoin", "popullation", "populasion", "population", 'D'));

        questionBank.add(new SpellingQuestion("combination", "Two or more things joined together.",
                "combonation", "combinashun", "combination", "combanation", 'C'));

        questionBank.add(new SpellingQuestion("sincerity", "Being honest and truthful.",
                "sincerety", "sincerity", "sincirity", "sinceraty", 'B'));

        questionBank.add(new SpellingQuestion("primarily", "Mostly; mainly.",
                "primerily", "primarily", "primarilly", "primarely", 'B'));

        questionBank.add(new SpellingQuestion("glimpse", "A very quick look.",
                "glimse", "glimps", "glimpse", "glimpes", 'C'));

        questionBank.add(new SpellingQuestion("prohibit", "To not allow something.",
                "prohabit", "prohibit", "prohibet", "prhibt", 'B'));

        questionBank.add(new SpellingQuestion("research", "To study or learn about something carefully.",
                "researsh", "research", "reseurch", "reserch", 'B'));

        questionBank.add(new SpellingQuestion("photograph", "A picture taken with a camera.",
                "photogaph", "photagraph", "fotograph", "photograph", 'D'));

        questionBank.add(new SpellingQuestion("radiance", "A bright, glowing light.",
                "radaince", "radience", "radiance", "radence", 'C'));

        questionBank.add(new SpellingQuestion("formulate", "To create or plan something.",
                "formullate", "formulate", "formulait", "formulatte", 'B'));

        questionBank.add(new SpellingQuestion("determine", "To figure something out.",
                "determine", "determind", "determene", "determane", 'A'));

        questionBank.add(new SpellingQuestion("substance", "A material or matter.",
                "substanse", "substance", "substince", "substence", 'B'));

        questionBank.add(new SpellingQuestion("appendix", "A small section at the end of a book or inside the body.",
                "appendex", "appenix", "appendix", "apendix", 'C'));

        questionBank.add(new SpellingQuestion("revenue", "Money earned by a company or group.",
                "revenew", "revinue", "revenue", "reveneu", 'C'));

        questionBank.add(new SpellingQuestion("percentage", "A part of a whole, out of 100.",
                "persentage", "percentage", "percentace", "percentige", 'B'));

        questionBank.add(new SpellingQuestion("education", "Learning in school or from experience.",
                "eductaion", "educatoin", "education", "educashion", 'C'));

        questionBank.add(new SpellingQuestion("relation", "A connection between things or people.",
                "realtion", "relaiton", "relation", "relashon", 'C'));

        questionBank.add(new SpellingQuestion("structure", "The way something is built or arranged.",
                "structer", "struture", "structure", "structcher", 'C'));

        questionBank.add(new SpellingQuestion("evidence", "Proof that something is true.",
                "evidance", "evadence", "evidence", "evidense", 'C'));

        questionBank.add(new SpellingQuestion("fanatic", "Someone who is extremely excited about something.",
                "fanattic", "fanatic", "fanatik", "fanactic", 'B'));

        questionBank.add(new SpellingQuestion("janitorial", "Having to do with cleaning or maintenance work.",
                "janitorrial", "janitoriel", "janitoral", "janitorial", 'D'));

        questionBank.add(new SpellingQuestion("lavish", "Fancy, rich, or expensive-looking.",
                "lavash", "lavish", "lavis", "lavich", 'B'));

        questionBank.add(new SpellingQuestion("panicky", "Feeling very worried or scared suddenly.",
                "panikey", "panicky", "paniky", "panikee", 'B'));

        questionBank.add(new SpellingQuestion("recruit", "To get someone to join a group or team.",
                "recriut", "recroot", "recruite", "recruit", 'D'));

        questionBank.add(new SpellingQuestion("nuclear", "Related to the energy inside atoms.",
                "nuklear", "nuclear", "nucleer", "nucular", 'B'));

        questionBank.add(new SpellingQuestion("appropriate", "Right or suitable for the situation.",
                "appropiate", "appropriate", "approriate", "apropriatt", 'B'));

        questionBank.add(new SpellingQuestion("satisfaction", "A happy feeling you get when something goes well.",
                "satisfication", "satisfaction", "satisfactoin", "sattisfaction", 'B'));

        questionBank.add(new SpellingQuestion("benefactor", "Someone who donates or helps others.",
                "beneffactor", "benefacter", "benifactor", "benefactor", 'D'));

        questionBank.add(new SpellingQuestion("adamant", "Not changing your mind; very firm.",
                "adimant", "adamant", "addamant", "adament", 'B'));

        questionBank.add(new SpellingQuestion("eruption", "A sudden bursting out, like a volcano.",
                "eruption", "erpution", "erupshon", "erruption", 'A'));

        questionBank.add(new SpellingQuestion("unify", "To join things together.",
                "unifiy", "unify", "unifie", "younify", 'B'));

        questionBank.add(new SpellingQuestion("illusion", "Something that looks real but isn’t.",
                "illuson", "illusion", "illiusion", "iluson", 'B'));

        questionBank.add(new SpellingQuestion("unison", "Doing something at the same time as others.",
                "unison", "unicen", "unisone", "unizon", 'A'));

        questionBank.add(new SpellingQuestion("confide", "To tell someone a secret you trust them with.",
                "confide", "confied", "confyde", "confidee", 'A'));

        questionBank.add(new SpellingQuestion("argument", "A disagreement or reason you give for something.",
                "arguement", "argment", "argument", "argumant", 'C'));

        questionBank.add(new SpellingQuestion("multiple", "More than one; many.",
                "multiple", "mutiple", "multible", "multipul", 'A'));

        questionBank.add(new SpellingQuestion("illiterate", "Not able to read or write.",
                "illittrate", "illiterate", "iliterate", "illiterete", 'B'));

        questionBank.add(new SpellingQuestion("investigate", "To look closely into something to find answers.",
                "investagate", "investigate", "investigat", "investegate", 'B'));

        questionBank.add(new SpellingQuestion("objection", "A reason for disagreeing with something.",
                "objection", "objetion", "obbjection", "objecshun", 'A'));

        questionBank.add(new SpellingQuestion("abnormal", "Not normal; unusual.",
                "abnoramal", "abnormal", "abnornal", "abmornal", 'B'));

        questionBank.add(new SpellingQuestion("devastation", "Great destruction or damage.",
                "devistation", "devestation", "devastation", "dehvastation", 'C'));

        questionBank.add(new SpellingQuestion("washable", "Able to be cleaned with water.",
                "washabul", "washable", "washible", "washeble", 'B'));

        questionBank.add(new SpellingQuestion("composition", "A piece of writing or how something is made.",
                "composision", "compasition", "composition", "compositon", 'C'));

        questionBank.add(new SpellingQuestion("hedge", "A line of bushes or shrubs.",
                "hedje", "hegde", "hedge", "hidge", 'C'));

        questionBank.add(new SpellingQuestion("contaminate", "To make something dirty or unsafe.",
                "contaminate", "contiminate", "contamanate", "contaminat", 'A'));

        questionBank.add(new SpellingQuestion("fundamental", "Very important; the basic part of something.",
                "fundamantal", "fundimental", "fundamental", "fundemental", 'C'));

        questionBank.add(new SpellingQuestion("attention", "Focusing your mind on something.",
                "attantion", "atention", "attention", "attentoin", 'C'));

        questionBank.add(new SpellingQuestion("flaunt", "To show off proudly.",
                "flaont", "flaunt", "flont", "flauntt", 'B'));

        questionBank.add(new SpellingQuestion("robust", "Strong and healthy.",
                "robost", "robust", "roabust", "robbust", 'B'));

        questionBank.add(new SpellingQuestion("permission", "Being allowed to do something.",
                "permisson", "permission", "permmision", "permision", 'B'));

        questionBank.add(new SpellingQuestion("retrieve", "To get something back.",
                "retrieve", "retrive", "retreivee", "reteive", 'A'));

        questionBank.add(new SpellingQuestion("agriculture", "Farming; growing crops and raising animals.",
                "agricuture", "agriculture", "aggriculture", "agriculter", 'B'));

        questionBank.add(new SpellingQuestion("thorax", "The chest area of a body or insect.",
                "thorax", "thoracks", "thorac", "thoraxxe", 'A'));

        questionBank.add(new SpellingQuestion("probable", "Likely to happen.",
                "probible", "probable", "probabel", "propable", 'B'));

        questionBank.add(new SpellingQuestion("reinforce", "To make something stronger.",
                "reenforce", "reinforse", "reinforce", "reienforce", 'C'));

        questionBank.add(new SpellingQuestion("detention", "Being kept after school as punishment.",
                "detintion", "detension", "detention", "detensionn", 'C'));

        questionBank.add(new SpellingQuestion("transparent", "Easy to see through.",
                "transparant", "transperent", "tranparent", "transparent", 'D'));

        Random rand = new Random();
        int randomIndex = rand.nextInt(questionBank.size());
        currentQuestion = questionBank.get(randomIndex);
    }

    private void updateQuestionPreview() {
        SpellingQuestion q = currentQuestion;

        textCurrentQuestion.setText(
                "Choose the correct spelling of the word that matches the definition below:" + "\n\n" +
                "Definition: "+ q.getHint() + "\n\n" +
                "A) " + q.getOptionA() + "\n" +
                "B) " + q.getOptionB() + "\n" +
                "C) " + q.getOptionC() + "\n" +
                "D) " + q.getOptionD() + "\n"
        );

        textStatus.setText("Tap Send Question via SMS.");
        textTimer.setText("");
    }

    private void sendCurrentQuestion() {
        String phone = editPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Enter number", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean sendGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
        boolean receiveGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (!sendGranted || !receiveGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS},
                    REQ_SMS_PERMISSIONS
            );
            return;
        }

        SpellingQuestion q = currentQuestion;

        String sms = "Spelling Bee Practice\n" +
                "Word: " + q.getWord() + "\n\n" +
                "A) " + q.getOptionA() + "\n" +
                "B) " + q.getOptionB() + "\n" +
                "C) " + q.getOptionC() + "\n" +
                "D) " + q.getOptionD() + "\n\n" +
                "Reply with A, B, C or D.";

        SmsManager.getDefault().sendTextMessage(phone, null, sms, null, null);

        Toast.makeText(this, "Question sent", Toast.LENGTH_SHORT).show();

        totalAsked++;
        startTimer();
        textStatus.setText("Waiting for reply...");
    }

    private void startTimer() {
        currentExpiryTime = System.currentTimeMillis() + TIME_LIMIT_MS;

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(TIME_LIMIT_MS, 1000) {
            @Override
            public void onTick(long ms) {
                textTimer.setText("Time left: " + (ms / 1000) + " seconds");
            }

            @Override
            public void onFinish() {
                textTimer.setText("Time is up!");

                SpellingQuestion q = currentQuestion;
                if (q != null) {
                    String phone = editPhone.getText().toString().trim();
                    if (!phone.isEmpty()) {
                        SmsManager.getDefault().sendTextMessage(
                                phone,
                                null,
                                "Time’s up! The correct answer was: " + q.getCorrectOption(),
                                null,
                                null
                        );
                    }
                }
            }

        }.start();
    }

    public static void stopTimerExternally() {
        QuizActivity a = currentActivityInstance;
        if (a == null) return;

        a.runOnUiThread(() -> {
            if (a.countDownTimer != null) {
                a.countDownTimer.cancel();
                a.countDownTimer = null;
                a.textTimer.setText("Answer received.");
            }
        });
    }

    private void moveToNextQuestion() {
        Random rand = new Random();
        int randomIndex = rand.nextInt(questionBank.size());
        currentQuestion = questionBank.get(randomIndex);
        updateQuestionPreview();
    }

    public static SpellingQuestion getCurrentQuestion() {
        return currentQuestion;
    }

    public static long getCurrentExpiryTime() {
        return currentExpiryTime;
    }

    public static void recordResult(boolean correct) {
        if (correct) totalCorrect++;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_SMS_PERMISSIONS) {
            boolean allGranted = true;
            if (grantResults.length == 0) {
                allGranted = false;
            } else {
                for (int res : grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            }

            if (allGranted) {
                sendCurrentQuestion();
            } else {
                Toast.makeText(
                        this,
                        "SMS permissions are required to send questions and receive answers.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
