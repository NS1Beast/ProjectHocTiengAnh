package hutech.com.example.projecthoctienganh;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import hutech.com.example.projecthoctienganh.db.DatabaseHelper;
import hutech.com.example.projecthoctienganh.model.Topic;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class Main_KiemTra extends AppCompatActivity {

    private static final int TYPE_MEANING_TO_WORD = 1; // Cho Nghĩa -> Đoán Từ
    private static final int TYPE_WORD_TO_MEANING = 2; // Cho Từ -> Đoán Nghĩa
    private static final int TYPE_MIXED = 3;           // Hỗn hợp 2 loại

    private DatabaseHelper dbHelper;

    // Top Bar
    private ImageButton btnBackKiemTra;

    // Setup Views
    private ScrollView layoutSetupQuiz;
    private Spinner spTopicQuiz, spQuestionCountQuiz;
    private RadioGroup rgQuizType;
    private RadioButton rbMeaningToWord, rbWordToMeaning, rbMixedQuiz;
    private MaterialButton btnStartQuiz;

    // Playing Views
    private LinearLayout layoutPlayQuiz;
    private TextView tvQuizProgress, tvQuizScore;
    private ProgressBar pbQuizProgress;
    private TextView tvQuestionTypeBadge, tvQuestionPrompt, tvQuestionText, tvQuestionSub;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4;

    // Result Views
    private LinearLayout layoutResultQuiz;
    private TextView tvResultScore, tvResultPercentage, tvResultMessage;
    private MaterialButton btnRetryQuiz, btnNewSetupQuiz;

    // Data State
    private List<Topic> topicList = new ArrayList<>();
    private List<Vocabulary> poolVocabularies = new ArrayList<>();
    private List<QuestionItem> questionList = new ArrayList<>();

    private int currentQuestionIndex = 0;
    private int score = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    private static class QuestionItem {
        Vocabulary targetVocab;
        int questionType; // 1: Meaning -> Word, 2: Word -> Meaning
        List<String> options = new ArrayList<>();
        int correctOptionIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_kiem_tra);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        loadTopicsToSpinner();
        setupQuestionCountSpinner();
    }

    private void initViews() {
        btnBackKiemTra = findViewById(R.id.btnBackKiemTra);

        // Setup Layout
        layoutSetupQuiz = findViewById(R.id.layoutSetupQuiz);
        spTopicQuiz = findViewById(R.id.spTopicQuiz);
        spQuestionCountQuiz = findViewById(R.id.spQuestionCountQuiz);
        rgQuizType = findViewById(R.id.rgQuizType);
        rbMeaningToWord = findViewById(R.id.rbMeaningToWord);
        rbWordToMeaning = findViewById(R.id.rbWordToMeaning);
        rbMixedQuiz = findViewById(R.id.rbMixedQuiz);
        btnStartQuiz = findViewById(R.id.btnStartQuiz);

        // Playing Layout
        layoutPlayQuiz = findViewById(R.id.layoutPlayQuiz);
        tvQuizProgress = findViewById(R.id.tvQuizProgress);
        tvQuizScore = findViewById(R.id.tvQuizScore);
        pbQuizProgress = findViewById(R.id.pbQuizProgress);

        tvQuestionTypeBadge = findViewById(R.id.tvQuestionTypeBadge);
        tvQuestionPrompt = findViewById(R.id.tvQuestionPrompt);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        tvQuestionSub = findViewById(R.id.tvQuestionSub);

        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);

        // Result Layout
        layoutResultQuiz = findViewById(R.id.layoutResultQuiz);
        tvResultScore = findViewById(R.id.tvResultScore);
        tvResultPercentage = findViewById(R.id.tvResultPercentage);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        btnRetryQuiz = findViewById(R.id.btnRetryQuiz);
        btnNewSetupQuiz = findViewById(R.id.btnNewSetupQuiz);
    }

    private void setupListeners() {
        btnBackKiemTra.setOnClickListener(v -> finish());

        btnStartQuiz.setOnClickListener(v -> startQuizGame());

        View.OnClickListener optionClickListener = v -> {
            int selectedIndex = -1;
            if (v.getId() == R.id.btnOption1) selectedIndex = 0;
            else if (v.getId() == R.id.btnOption2) selectedIndex = 1;
            else if (v.getId() == R.id.btnOption3) selectedIndex = 2;
            else if (v.getId() == R.id.btnOption4) selectedIndex = 3;

            handleAnswerSelection(selectedIndex);
        };

        btnOption1.setOnClickListener(optionClickListener);
        btnOption2.setOnClickListener(optionClickListener);
        btnOption3.setOnClickListener(optionClickListener);
        btnOption4.setOnClickListener(optionClickListener);

        btnRetryQuiz.setOnClickListener(v -> startQuizGame());

        btnNewSetupQuiz.setOnClickListener(v -> {
            layoutResultQuiz.setVisibility(View.GONE);
            layoutPlayQuiz.setVisibility(View.GONE);
            layoutSetupQuiz.setVisibility(View.VISIBLE);
        });

        spTopicQuiz.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePoolVocabularies(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadTopicsToSpinner() {
        topicList = dbHelper.getAllTopics();
        List<String> spinnerOptions = new ArrayList<>();

        int totalAllWords = dbHelper.getAllVocabularies().size();
        spinnerOptions.add("Tất cả chủ đề (" + totalAllWords + " từ)");

        for (Topic topic : topicList) {
            int count = dbHelper.getVocabulariesByTopic(topic.getId(), DatabaseHelper.FILTER_ALL).size();
            spinnerOptions.add(topic.getName() + " (" + count + " từ)");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTopicQuiz.setAdapter(spinnerAdapter);
    }

    private void updatePoolVocabularies(int topicSpinnerPosition) {
        if (topicSpinnerPosition == 0) {
            poolVocabularies = dbHelper.getAllVocabularies();
        } else {
            Topic selectedTopic = topicList.get(topicSpinnerPosition - 1);
            poolVocabularies = dbHelper.getVocabulariesByTopic(selectedTopic.getId(), DatabaseHelper.FILTER_ALL);
        }
    }

    private void setupQuestionCountSpinner() {
        List<String> countOptions = new ArrayList<>();
        countOptions.add("5 câu");
        countOptions.add("10 câu");
        countOptions.add("15 câu");
        countOptions.add("Tất cả từ vựng");

        ArrayAdapter<String> countAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, countOptions);
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spQuestionCountQuiz.setAdapter(countAdapter);
    }

    private void startQuizGame() {
        if (poolVocabularies.size() < 4) {
            Toast.makeText(this, "Chủ đề này cần ít nhất 4 từ vựng để tạo bài kiểm tra!", Toast.LENGTH_LONG).show();
            return;
        }

        // Determine number of questions
        int countPosition = spQuestionCountQuiz.getSelectedItemPosition();
        int targetCount = poolVocabularies.size();
        if (countPosition == 0) targetCount = Math.min(5, poolVocabularies.size());
        else if (countPosition == 1) targetCount = Math.min(10, poolVocabularies.size());
        else if (countPosition == 2) targetCount = Math.min(15, poolVocabularies.size());

        // Determine quiz mode
        int selectedQuizMode = TYPE_MEANING_TO_WORD;
        int checkedId = rgQuizType.getCheckedRadioButtonId();
        if (checkedId == R.id.rbWordToMeaning) {
            selectedQuizMode = TYPE_WORD_TO_MEANING;
        } else if (checkedId == R.id.rbMixedQuiz) {
            selectedQuizMode = TYPE_MIXED;
        }

        // Generate questions
        generateQuestions(targetCount, selectedQuizMode);

        if (questionList.isEmpty()) {
            Toast.makeText(this, "Không thể khởi tạo câu hỏi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset Quiz State
        currentQuestionIndex = 0;
        score = 0;

        // UI transitions
        layoutSetupQuiz.setVisibility(View.GONE);
        layoutResultQuiz.setVisibility(View.GONE);
        layoutPlayQuiz.setVisibility(View.VISIBLE);

        displayCurrentQuestion();
    }

    private void generateQuestions(int count, int quizMode) {
        questionList.clear();

        List<Vocabulary> shuffledPool = new ArrayList<>(poolVocabularies);
        Collections.shuffle(shuffledPool);

        List<Vocabulary> selectedVocabList = shuffledPool.subList(0, count);
        Random random = new Random();

        for (Vocabulary vocab : selectedVocabList) {
            QuestionItem q = new QuestionItem();
            q.targetVocab = vocab;

            // Set question type for this question
            if (quizMode == TYPE_MIXED) {
                q.questionType = random.nextBoolean() ? TYPE_MEANING_TO_WORD : TYPE_WORD_TO_MEANING;
            } else {
                q.questionType = quizMode;
            }

            // Build correct & wrong options
            String correctAnswer = (q.questionType == TYPE_MEANING_TO_WORD) ? vocab.getWord() : vocab.getMeaning();

            List<String> wrongOptions = new ArrayList<>();
            List<Vocabulary> distractorPool = new ArrayList<>(poolVocabularies);
            distractorPool.remove(vocab);
            Collections.shuffle(distractorPool);

            for (Vocabulary distractor : distractorPool) {
                String optionVal = (q.questionType == TYPE_MEANING_TO_WORD) ? distractor.getWord() : distractor.getMeaning();
                if (!wrongOptions.contains(optionVal) && !optionVal.equals(correctAnswer)) {
                    wrongOptions.add(optionVal);
                }
                if (wrongOptions.size() == 3) break;
            }

            // Combine correct & wrong options
            List<String> allOptions = new ArrayList<>(wrongOptions);
            allOptions.add(correctAnswer);
            Collections.shuffle(allOptions);

            q.options = allOptions;
            q.correctOptionIndex = allOptions.indexOf(correctAnswer);

            questionList.add(q);
        }
    }

    private void displayCurrentQuestion() {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) return;

        QuestionItem q = questionList.get(currentQuestionIndex);

        // Update progress & score
        tvQuizProgress.setText("Câu " + (currentQuestionIndex + 1) + " / " + questionList.size());
        tvQuizScore.setText("Đúng: " + score);
        pbQuizProgress.setMax(questionList.size());
        pbQuizProgress.setProgress(currentQuestionIndex + 1);

        // Render question text & prompt
        if (q.questionType == TYPE_MEANING_TO_WORD) {
            tvQuestionTypeBadge.setText("NGHĨA ➔ TỪ TIẾNG ANH");
            tvQuestionPrompt.setText("Từ tiếng Anh nào tương ứng với nghĩa:");
            tvQuestionText.setText(q.targetVocab.getMeaning());
            tvQuestionSub.setVisibility(View.GONE);
        } else {
            tvQuestionTypeBadge.setText("TỪ TIẾNG ANH ➔ NGHĨA");
            tvQuestionPrompt.setText("Nghĩa tiếng Việt của từ này là gì:");
            tvQuestionText.setText(q.targetVocab.getWord());
            if (q.targetVocab.getPhonetic() != null && !q.targetVocab.getPhonetic().isEmpty()) {
                tvQuestionSub.setText(q.targetVocab.getPhonetic());
                tvQuestionSub.setVisibility(View.VISIBLE);
            } else {
                tvQuestionSub.setVisibility(View.GONE);
            }
        }

        // Render Option Buttons
        resetOptionButtonsState();

        btnOption1.setText("A. " + q.options.get(0));
        btnOption2.setText("B. " + q.options.get(1));
        btnOption3.setText("C. " + q.options.get(2));
        btnOption4.setText("D. " + q.options.get(3));
    }

    private void resetOptionButtonsState() {
        MaterialButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};
        int defaultTextColor = ContextCompat.getColor(this, R.color.text_primary);
        int defaultBorderColor = ContextCompat.getColor(this, R.color.border_color);

        for (MaterialButton btn : buttons) {
            btn.setEnabled(true);
            btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.card_bg)));
            btn.setTextColor(defaultTextColor);
            btn.setStrokeColor(ColorStateList.valueOf(defaultBorderColor));
            btn.setStrokeWidth(3);
        }
    }

    private void handleAnswerSelection(int selectedIndex) {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) return;

        QuestionItem q = questionList.get(currentQuestionIndex);
        MaterialButton[] buttons = {btnOption1, btnOption2, btnOption3, btnOption4};

        // Disable all buttons to prevent double tap
        for (MaterialButton btn : buttons) {
            btn.setEnabled(false);
        }

        int correctColor = ContextCompat.getColor(this, R.color.quiz_correct);
        int wrongColor = ContextCompat.getColor(this, R.color.quiz_wrong);
        int whiteColor = ContextCompat.getColor(this, R.color.white);

        if (selectedIndex == q.correctOptionIndex) {
            // Correct Answer
            score++;
            tvQuizScore.setText("Đúng: " + score);

            buttons[selectedIndex].setBackgroundTintList(ColorStateList.valueOf(correctColor));
            buttons[selectedIndex].setStrokeColor(ColorStateList.valueOf(correctColor));
            buttons[selectedIndex].setTextColor(whiteColor);
        } else {
            // Incorrect Answer
            buttons[selectedIndex].setBackgroundTintList(ColorStateList.valueOf(wrongColor));
            buttons[selectedIndex].setStrokeColor(ColorStateList.valueOf(wrongColor));
            buttons[selectedIndex].setTextColor(whiteColor);

            // Highlight Correct Option
            buttons[q.correctOptionIndex].setBackgroundTintList(ColorStateList.valueOf(correctColor));
            buttons[q.correctOptionIndex].setStrokeColor(ColorStateList.valueOf(correctColor));
            buttons[q.correctOptionIndex].setTextColor(whiteColor);
        }

        // Delay 1 second then load next question
        handler.postDelayed(() -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                displayCurrentQuestion();
            } else {
                showQuizResults();
            }
        }, 1000);
    }

    private void showQuizResults() {
        layoutPlayQuiz.setVisibility(View.GONE);
        layoutResultQuiz.setVisibility(View.VISIBLE);

        int totalQuestions = questionList.size();
        tvResultScore.setText(score + " / " + totalQuestions);

        int percentage = Math.round(((float) score / totalQuestions) * 100);
        tvResultPercentage.setText("Đạt " + percentage + "%");

        if (percentage >= 80) {
            tvResultMessage.setText("Xuất sắc! Bạn có khả năng ghi nhớ từ vựng rất tốt!");
        } else if (percentage >= 50) {
            tvResultMessage.setText("Khá tốt! Hãy tiếp tục rèn luyện thêm nhé!");
        } else {
            tvResultMessage.setText("Cần cố gắng thêm! Hãy dùng Flashcard ôn tập lại và thử lại nhé!");
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}