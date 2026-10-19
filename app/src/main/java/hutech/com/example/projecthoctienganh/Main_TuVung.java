package hutech.com.example.projecthoctienganh;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import hutech.com.example.projecthoctienganh.adapter.VocabularyAdapter;
import hutech.com.example.projecthoctienganh.db.DatabaseHelper;
import hutech.com.example.projecthoctienganh.model.Topic;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class Main_TuVung extends AppCompatActivity {

    public static final String EXTRA_TOPIC_ID = "extra_topic_id";

    private DatabaseHelper dbHelper;
    private int currentTopicId = 1;
    private int currentFilter = DatabaseHelper.FILTER_ALL;

    // UI Components
    private ImageButton btnBack, btnMoreOptions;
    private TextView tvHeaderTitle, tvBannerTopicName, tvBannerSubStats;
    private ImageView imgBannerTopicIcon;
    private ProgressBar bannerProgressBar;

    private TextView btnFilterAll, btnFilterUnlearned, btnFilterLearned;
    private ListView lvVocabulary;
    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;
    private FloatingActionButton fabAddWord;

    private VocabularyAdapter adapter;
    private List<Vocabulary> currentVocabularyList = new ArrayList<>();
    private Topic currentTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_tu_vung);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent() != null && getIntent().hasExtra(EXTRA_TOPIC_ID)) {
            currentTopicId = getIntent().getIntExtra(EXTRA_TOPIC_ID, 1);
        }

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        loadTopicData();
        loadVocabularyList();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMoreOptions = findViewById(R.id.btnMoreOptions);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        tvBannerTopicName = findViewById(R.id.tvBannerTopicName);
        tvBannerSubStats = findViewById(R.id.tvBannerSubStats);
        imgBannerTopicIcon = findViewById(R.id.imgBannerTopicIcon);
        bannerProgressBar = findViewById(R.id.bannerProgressBar);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUnlearned = findViewById(R.id.btnFilterUnlearned);
        btnFilterLearned = findViewById(R.id.btnFilterLearned);

        lvVocabulary = findViewById(R.id.lvVocabulary);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        fabAddWord = findViewById(R.id.fabAddWord);

        adapter = new VocabularyAdapter(this, currentVocabularyList);
        lvVocabulary.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnMoreOptions.setOnClickListener(this::showMoreOptionsMenu);

        btnFilterAll.setOnClickListener(v -> setFilter(DatabaseHelper.FILTER_ALL));
        btnFilterUnlearned.setOnClickListener(v -> setFilter(DatabaseHelper.FILTER_UNLEARNED));
        btnFilterLearned.setOnClickListener(v -> setFilter(DatabaseHelper.FILTER_LEARNED));

        fabAddWord.setOnClickListener(v -> showAddEditWordDialog(null));

        lvVocabulary.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < currentVocabularyList.size()) {
                Vocabulary vocabulary = currentVocabularyList.get(position);
                showWordOptionsDialog(vocabulary);
            }
        });

        adapter.setOnStatusClickListener((vocabulary, position) -> {
            boolean newStatus = !vocabulary.isLearned();
            dbHelper.updateLearnedStatus(vocabulary.getId(), newStatus);
            vocabulary.setLearned(newStatus);

            loadTopicData();
            loadVocabularyList();

            String statusText = newStatus ? "Đã đánh dấu thuộc từ" : "Đã chuyển về chưa thuộc";
            Toast.makeText(Main_TuVung.this, statusText + ": " + vocabulary.getWord(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setFilter(int filterType) {
        this.currentFilter = filterType;

        // Reset Styles
        btnFilterAll.setBackgroundResource(R.drawable.bg_pill_unselected);
        btnFilterAll.setTextColor(getResources().getColor(R.color.pill_unselected_text, getTheme()));

        btnFilterUnlearned.setBackgroundResource(R.drawable.bg_pill_unselected);
        btnFilterUnlearned.setTextColor(getResources().getColor(R.color.pill_unselected_text, getTheme()));

        btnFilterLearned.setBackgroundResource(R.drawable.bg_pill_unselected);
        btnFilterLearned.setTextColor(getResources().getColor(R.color.pill_unselected_text, getTheme()));

        // Highlight Active
        if (filterType == DatabaseHelper.FILTER_ALL) {
            btnFilterAll.setBackgroundResource(R.drawable.bg_pill_selected);
            btnFilterAll.setTextColor(getResources().getColor(R.color.pill_selected_text, getTheme()));
        } else if (filterType == DatabaseHelper.FILTER_UNLEARNED) {
            btnFilterUnlearned.setBackgroundResource(R.drawable.bg_pill_selected);
            btnFilterUnlearned.setTextColor(getResources().getColor(R.color.pill_selected_text, getTheme()));
        } else if (filterType == DatabaseHelper.FILTER_LEARNED) {
            btnFilterLearned.setBackgroundResource(R.drawable.bg_pill_selected);
            btnFilterLearned.setTextColor(getResources().getColor(R.color.pill_selected_text, getTheme()));
        }

        loadVocabularyList();
    }

    private void loadTopicData() {
        currentTopic = dbHelper.getTopicById(currentTopicId);
        if (currentTopic != null) {
            tvHeaderTitle.setText(currentTopic.getName());
            tvBannerTopicName.setText(currentTopic.getName());

            int iconResId = getResources().getIdentifier(currentTopic.getIconName(), "drawable", getPackageName());
            if (iconResId != 0) {
                imgBannerTopicIcon.setImageResource(iconResId);
            } else {
                imgBannerTopicIcon.setImageResource(R.drawable.ic_flight);
            }

            int total = currentTopic.getTotalWords();
            int learned = currentTopic.getLearnedWords();
            int percent = total > 0 ? (learned * 100 / total) : 0;

            tvBannerSubStats.setText(total + " từ • " + learned + " đã thuộc");
            bannerProgressBar.setProgress(percent);
        }
    }

    private void loadVocabularyList() {
        currentVocabularyList = dbHelper.getVocabulariesByTopic(currentTopicId, currentFilter);
        adapter.updateList(currentVocabularyList);

        if (currentVocabularyList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            lvVocabulary.setVisibility(View.GONE);

            if (currentFilter == DatabaseHelper.FILTER_UNLEARNED) {
                tvEmptyMessage.setText("Bạn đã thuộc tất cả từ trong chủ đề này!");
            } else if (currentFilter == DatabaseHelper.FILTER_LEARNED) {
                tvEmptyMessage.setText("Chưa có từ nào được đánh dấu đã học.");
            } else {
                tvEmptyMessage.setText("Chưa có từ vựng nào trong chủ đề này.");
            }
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            lvVocabulary.setVisibility(View.VISIBLE);
        }
    }

    private void showMoreOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Chỉnh sửa chủ đề");
        popup.getMenu().add(0, 2, 1, "Xóa chủ đề");

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                showEditTopicDialog();
                return true;
            } else if (id == 2) {
                confirmDeleteTopic();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showWordDetailDialog(Vocabulary vocab) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_chi_tiet_tu_vung);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView imgDetailIcon = dialog.findViewById(R.id.imgDetailIcon);
        TextView tvDetailWord = dialog.findViewById(R.id.tvDetailWord);
        TextView tvDetailPhonetic = dialog.findViewById(R.id.tvDetailPhonetic);
        TextView tvDetailMeaning = dialog.findViewById(R.id.tvDetailMeaning);
        TextView tvDetailExample = dialog.findViewById(R.id.tvDetailExample);
        SwitchMaterial switchLearned = dialog.findViewById(R.id.switchLearned);
        MaterialButton btnCloseDetail = dialog.findViewById(R.id.btnCloseDetail);

        tvDetailWord.setText(vocab.getWord());
        tvDetailPhonetic.setText(vocab.getPhonetic());
        tvDetailMeaning.setText(vocab.getMeaning());
        tvDetailExample.setText(vocab.getExample() != null && !vocab.getExample().isEmpty() ? vocab.getExample() : "Chưa có ví dụ câu.");

        int iconResId = getResources().getIdentifier(vocab.getIconName(), "drawable", getPackageName());
        if (iconResId != 0) {
            imgDetailIcon.setImageResource(iconResId);
        }

        switchLearned.setChecked(vocab.isLearned());

        switchLearned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            vocab.setLearned(isChecked);
            dbHelper.updateLearnedStatus(vocab.getId(), isChecked);
            loadTopicData();
            loadVocabularyList();
        });

        btnCloseDetail.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showWordOptionsDialog(Vocabulary vocab) {
        String[] options = {"Xem chi tiết", "Sửa từ vựng", "Xóa từ vựng"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(vocab.getWord() + " (" + vocab.getMeaning() + ")");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showWordDetailDialog(vocab);
            } else if (which == 1) {
                showAddEditWordDialog(vocab);
            } else if (which == 2) {
                dbHelper.deleteVocabulary(vocab.getId());
                loadTopicData();
                loadVocabularyList();
                Toast.makeText(Main_TuVung.this, "Đã xóa từ: " + vocab.getWord(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showAddEditWordDialog(Vocabulary existingVocab) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_them_tu_vung);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        AutoCompleteTextView actvSelectTopic = dialog.findViewById(R.id.actvSelectTopic);
        TextInputEditText etWord = dialog.findViewById(R.id.etWord);
        TextInputEditText etPhonetic = dialog.findViewById(R.id.etPhonetic);
        TextInputEditText etMeaning = dialog.findViewById(R.id.etMeaning);
        TextInputEditText etExample = dialog.findViewById(R.id.etExample);
        MaterialButton btnCancelWord = dialog.findViewById(R.id.btnCancelWord);
        MaterialButton btnSaveWord = dialog.findViewById(R.id.btnSaveWord);

        // Load all topics for topic selector
        List<Topic> topicsList = dbHelper.getAllTopics();
        List<String> topicNames = new ArrayList<>();
        int selectedTopicIndex = 0;
        for (int i = 0; i < topicsList.size(); i++) {
            Topic t = topicsList.get(i);
            topicNames.add(t.getName());
            if (t.getId() == currentTopicId) {
                selectedTopicIndex = i;
            }
        }

        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, topicNames);
        actvSelectTopic.setAdapter(topicAdapter);

        if (!topicNames.isEmpty()) {
            actvSelectTopic.setText(topicNames.get(selectedTopicIndex), false);
        }

        final int[] chosenTopicId = {currentTopicId};
        actvSelectTopic.setOnItemClickListener((parent, view, position, id) -> {
            chosenTopicId[0] = topicsList.get(position).getId();
        });

        if (existingVocab != null) {
            tvDialogTitle.setText("Chỉnh sửa từ vựng");
            etWord.setText(existingVocab.getWord());
            etPhonetic.setText(existingVocab.getPhonetic());
            etMeaning.setText(existingVocab.getMeaning());
            etExample.setText(existingVocab.getExample());
        } else {
            tvDialogTitle.setText("Thêm từ vựng mới");
        }

        btnCancelWord.setOnClickListener(v -> dialog.dismiss());

        btnSaveWord.setOnClickListener(v -> {
            String word = etWord.getText() != null ? etWord.getText().toString().trim() : "";
            String phonetic = etPhonetic.getText() != null ? etPhonetic.getText().toString().trim() : "";
            String meaning = etMeaning.getText() != null ? etMeaning.getText().toString().trim() : "";
            String example = etExample.getText() != null ? etExample.getText().toString().trim() : "";

            if (word.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(Main_TuVung.this, "Vui lòng nhập đầy đủ Từ vựng và Nghĩa!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingVocab != null) {
                existingVocab.setTopicId(chosenTopicId[0]);
                existingVocab.setWord(word);
                existingVocab.setPhonetic(phonetic);
                existingVocab.setMeaning(meaning);
                existingVocab.setExample(example);
                dbHelper.updateVocabulary(existingVocab);
                Toast.makeText(Main_TuVung.this, "Cập nhật từ thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Vocabulary newVocab = new Vocabulary();
                newVocab.setTopicId(chosenTopicId[0]);
                newVocab.setWord(word);
                newVocab.setPhonetic(phonetic);
                newVocab.setMeaning(meaning);
                newVocab.setExample(example);
                newVocab.setIconName("ic_word_default");
                newVocab.setLearned(false);

                dbHelper.addVocabulary(newVocab);
                Toast.makeText(Main_TuVung.this, "Thêm từ thành công!", Toast.LENGTH_SHORT).show();
            }

            loadTopicData();
            loadVocabularyList();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditTopicDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_sua_chu_de);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputEditText etTopicName = dialog.findViewById(R.id.etTopicName);
        TextInputEditText etTopicDesc = dialog.findViewById(R.id.etTopicDesc);
        MaterialButton btnCancelTopic = dialog.findViewById(R.id.btnCancelTopic);
        MaterialButton btnSaveTopic = dialog.findViewById(R.id.btnSaveTopic);

        if (currentTopic != null) {
            etTopicName.setText(currentTopic.getName());
            etTopicDesc.setText(currentTopic.getDescription());
        }

        btnCancelTopic.setOnClickListener(v -> dialog.dismiss());

        btnSaveTopic.setOnClickListener(v -> {
            String name = etTopicName.getText() != null ? etTopicName.getText().toString().trim() : "";
            String desc = etTopicDesc.getText() != null ? etTopicDesc.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(Main_TuVung.this, "Vui lòng nhập tên chủ đề!", Toast.LENGTH_SHORT).show();
                return;
            }

            currentTopic.setName(name);
            currentTopic.setDescription(desc);
            dbHelper.updateTopic(currentTopic);

            loadTopicData();
            Toast.makeText(Main_TuVung.this, "Đã cập nhật chủ đề!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void confirmDeleteTopic() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa chủ đề")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ chủ đề '" + (currentTopic != null ? currentTopic.getName() : "") + "' cùng tất cả từ vựng không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    dbHelper.deleteTopic(currentTopicId);
                    Toast.makeText(Main_TuVung.this, "Đã xóa chủ đề!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}