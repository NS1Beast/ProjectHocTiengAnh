package hutech.com.example.projecthoctienganh;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hutech.com.example.projecthoctienganh.db.DatabaseHelper;
import hutech.com.example.projecthoctienganh.model.Topic;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class Main_FlashCard extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    // Views
    private ImageButton btnBackFlashcard;
    private MaterialButton btnShuffleFlashcard;
    private Spinner spTopicFlashcard;
    private TextView tvFlashcardProgress, tvLearnedBadge, tvEmptyFlashcard;
    private ProgressBar pbFlashcardProgress;

    private MaterialCardView cardFlashcard;
    private LinearLayout layoutCardFront, layoutCardBack;
    private TextView tvWordFront, tvPhoneticFront;
    private TextView tvWordBackHeader, tvMeaningBack, tvExampleBack;

    private MaterialButton btnPrevCard, btnMarkLearned, btnNextCard;

    // Data
    private List<Topic> topicList = new ArrayList<>();
    private List<Vocabulary> vocabularyList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShowingFront = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_flash_card);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
        loadTopicsToSpinner();
    }

    private void initViews() {
        btnBackFlashcard = findViewById(R.id.btnBackFlashcard);
        btnShuffleFlashcard = findViewById(R.id.btnShuffleFlashcard);
        spTopicFlashcard = findViewById(R.id.spTopicFlashcard);

        tvFlashcardProgress = findViewById(R.id.tvFlashcardProgress);
        tvLearnedBadge = findViewById(R.id.tvLearnedBadge);
        tvEmptyFlashcard = findViewById(R.id.tvEmptyFlashcard);
        pbFlashcardProgress = findViewById(R.id.pbFlashcardProgress);

        cardFlashcard = findViewById(R.id.cardFlashcard);
        layoutCardFront = findViewById(R.id.layoutCardFront);
        layoutCardBack = findViewById(R.id.layoutCardBack);

        tvWordFront = findViewById(R.id.tvWordFront);
        tvPhoneticFront = findViewById(R.id.tvPhoneticFront);

        tvWordBackHeader = findViewById(R.id.tvWordBackHeader);
        tvMeaningBack = findViewById(R.id.tvMeaningBack);
        tvExampleBack = findViewById(R.id.tvExampleBack);

        btnPrevCard = findViewById(R.id.btnPrevCard);
        btnMarkLearned = findViewById(R.id.btnMarkLearned);
        btnNextCard = findViewById(R.id.btnNextCard);
    }

    private void setupListeners() {
        btnBackFlashcard.setOnClickListener(v -> finish());

        // Card Click to Flip
        cardFlashcard.setOnClickListener(v -> flipCard());

        // Navigation
        btnPrevCard.setOnClickListener(v -> showPreviousCard());
        btnNextCard.setOnClickListener(v -> showNextCard());

        // Mark Learned / Unlearned
        btnMarkLearned.setOnClickListener(v -> toggleLearnedStatus());

        // Shuffle Cards
        btnShuffleFlashcard.setOnClickListener(v -> {
            if (vocabularyList.size() > 1) {
                Collections.shuffle(vocabularyList);
                currentIndex = 0;
                resetToFrontView();
                displayCurrentCard();
                Toast.makeText(this, "Đã xáo trộn danh sách thẻ!", Toast.LENGTH_SHORT).show();
            }
        });

        // Spinner topic selection listener
        spTopicFlashcard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadVocabularyBySelectedTopic(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadTopicsToSpinner() {
        topicList = dbHelper.getAllTopics();
        List<String> spinnerOptions = new ArrayList<>();

        // Add "All topics"
        int totalAllWords = dbHelper.getAllVocabularies().size();
        spinnerOptions.add("Tất cả chủ đề (" + totalAllWords + " từ)");

        for (Topic topic : topicList) {
            int count = dbHelper.getVocabulariesByTopic(topic.getId(), DatabaseHelper.FILTER_ALL).size();
            spinnerOptions.add(topic.getName() + " (" + count + " từ)");
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, spinnerOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTopicFlashcard.setAdapter(spinnerAdapter);
    }

    private void loadVocabularyBySelectedTopic(int position) {
        if (position == 0) {
            // All topics
            vocabularyList = dbHelper.getAllVocabularies();
        } else {
            if (topicList != null && position - 1 >= 0 && position - 1 < topicList.size()) {
                Topic selectedTopic = topicList.get(position - 1);
                vocabularyList = dbHelper.getVocabulariesByTopic(selectedTopic.getId(), DatabaseHelper.FILTER_ALL);
            } else {
                vocabularyList = new ArrayList<>();
            }
        }

        if (vocabularyList == null) {
            vocabularyList = new ArrayList<>();
        }

        currentIndex = 0;
        resetToFrontView();

        if (vocabularyList.isEmpty()) {
            cardFlashcard.setVisibility(View.GONE);
            tvEmptyFlashcard.setVisibility(View.VISIBLE);
            tvFlashcardProgress.setText("Thẻ 0 / 0");
            pbFlashcardProgress.setMax(1);
            pbFlashcardProgress.setProgress(0);
            btnPrevCard.setEnabled(false);
            btnNextCard.setEnabled(false);
            btnMarkLearned.setEnabled(false);
        } else {
            cardFlashcard.setVisibility(View.VISIBLE);
            tvEmptyFlashcard.setVisibility(View.GONE);
            btnPrevCard.setEnabled(true);
            btnNextCard.setEnabled(true);
            btnMarkLearned.setEnabled(true);
            displayCurrentCard();
        }
    }

    private void displayCurrentCard() {
        if (vocabularyList == null || vocabularyList.isEmpty() || currentIndex < 0 || currentIndex >= vocabularyList.size()) {
            if (cardFlashcard != null) cardFlashcard.setVisibility(View.GONE);
            if (tvEmptyFlashcard != null) tvEmptyFlashcard.setVisibility(View.VISIBLE);
            if (tvFlashcardProgress != null) tvFlashcardProgress.setText("Thẻ 0 / 0");
            if (pbFlashcardProgress != null) {
                pbFlashcardProgress.setMax(1);
                pbFlashcardProgress.setProgress(0);
            }
            if (btnPrevCard != null) btnPrevCard.setEnabled(false);
            if (btnNextCard != null) btnNextCard.setEnabled(false);
            if (btnMarkLearned != null) btnMarkLearned.setEnabled(false);
            return;
        }

        Vocabulary current = vocabularyList.get(currentIndex);

        // Set Front Side
        tvWordFront.setText(current.getWord());
        tvPhoneticFront.setText(current.getPhonetic() != null && !current.getPhonetic().isEmpty()
                ? current.getPhonetic() : "");

        // Set Back Side
        tvWordBackHeader.setText(current.getWord() + " " + (current.getPhonetic() != null ? current.getPhonetic() : ""));
        tvMeaningBack.setText(current.getMeaning());
        tvExampleBack.setText(current.getExample() != null && !current.getExample().isEmpty()
                ? current.getExample() : "Chưa có ví dụ.");

        // Progress text & bar
        tvFlashcardProgress.setText("Thẻ " + (currentIndex + 1) + " / " + vocabularyList.size());
        pbFlashcardProgress.setMax(vocabularyList.size());
        pbFlashcardProgress.setProgress(currentIndex + 1);

        // Update Learned UI Button & Badge
        updateLearnedUI(current.isLearned());
    }

    private void updateLearnedUI(boolean isLearned) {
        if (isLearned) {
            tvLearnedBadge.setVisibility(View.VISIBLE);
            btnMarkLearned.setText("Đã thuộc");
            btnMarkLearned.setIconResource(R.drawable.ic_check_circle_green);
            btnMarkLearned.setTextColor(ContextCompat.getColor(this, R.color.learned_green));
            btnMarkLearned.setStrokeColorResource(R.color.learned_green_border);
        } else {
            tvLearnedBadge.setVisibility(View.GONE);
            btnMarkLearned.setText("Chưa thuộc");
            btnMarkLearned.setIconResource(R.drawable.ic_circle_outline_gray);
            btnMarkLearned.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            btnMarkLearned.setStrokeColorResource(R.color.border_color);
        }
    }

    private void toggleLearnedStatus() {
        if (vocabularyList.isEmpty() || currentIndex < 0 || currentIndex >= vocabularyList.size()) return;

        Vocabulary current = vocabularyList.get(currentIndex);
        boolean newLearnedState = !current.isLearned();
        current.setLearned(newLearnedState);

        // Update SQLite DB
        dbHelper.updateLearnedStatus(current.getId(), newLearnedState);

        updateLearnedUI(newLearnedState);
        Toast.makeText(this, newLearnedState ? "Đã đánh dấu thuộc từ này!" : "Đã bỏ thuộc từ này!", Toast.LENGTH_SHORT).show();
    }

    private void flipCard() {
        isShowingFront = !isShowingFront;
        cardFlashcard.animate().rotationY(90f).setDuration(150).withEndAction(() -> {
            if (isShowingFront) {
                layoutCardFront.setVisibility(View.VISIBLE);
                layoutCardBack.setVisibility(View.GONE);
                cardFlashcard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_bg));
                cardFlashcard.setStrokeColor(ContextCompat.getColor(this, R.color.border_color));
            } else {
                layoutCardFront.setVisibility(View.GONE);
                layoutCardBack.setVisibility(View.VISIBLE);
                cardFlashcard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.flashcard_back_bg));
                cardFlashcard.setStrokeColor(ContextCompat.getColor(this, R.color.flashcard_back_border));
            }
            cardFlashcard.setRotationY(-90f);
            cardFlashcard.animate().rotationY(0f).setDuration(150).start();
        }).start();
    }

    private void resetToFrontView() {
        isShowingFront = true;
        layoutCardFront.setVisibility(View.VISIBLE);
        layoutCardBack.setVisibility(View.GONE);
        cardFlashcard.setRotationY(0f);
        cardFlashcard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_bg));
        cardFlashcard.setStrokeColor(ContextCompat.getColor(this, R.color.border_color));
    }

    private void showPreviousCard() {
        if (vocabularyList.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            resetToFrontView();
            displayCurrentCard();
        } else {
            Toast.makeText(this, "Đây là thẻ đầu tiên!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNextCard() {
        if (vocabularyList.isEmpty()) return;
        if (currentIndex < vocabularyList.size() - 1) {
            currentIndex++;
            resetToFrontView();
            displayCurrentCard();
        } else {
            Toast.makeText(this, "Đã học hết danh sách thẻ!", Toast.LENGTH_SHORT).show();
        }
    }
}