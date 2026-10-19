package hutech.com.example.projecthoctienganh;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hutech.com.example.projecthoctienganh.adapter.TopicAdapter;
import hutech.com.example.projecthoctienganh.db.DatabaseHelper;
import hutech.com.example.projecthoctienganh.model.Topic;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class Main_ChonTuVung extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ImageButton btnBackTuVung;
    private EditText etSearchTopicTuVung;
    private ListView lvTopicsTuVung;
    private FloatingActionButton fabAddWordGlobal;

    private TopicAdapter adapter;
    private List<Topic> allTopics = new ArrayList<>();
    private List<Topic> filteredTopics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_chon_tu_vung);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTopics();
    }

    private void initViews() {
        btnBackTuVung = findViewById(R.id.btnBackTuVung);
        etSearchTopicTuVung = findViewById(R.id.etSearchTopicTuVung);
        lvTopicsTuVung = findViewById(R.id.lvTopicsTuVung);
        fabAddWordGlobal = findViewById(R.id.fabAddWordGlobal);

        adapter = new TopicAdapter(this, filteredTopics);
        lvTopicsTuVung.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackTuVung.setOnClickListener(v -> finish());

        fabAddWordGlobal.setOnClickListener(v -> showAddWordWithTopicDialog());

        lvTopicsTuVung.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < filteredTopics.size()) {
                Topic topic = filteredTopics.get(position);
                Intent intent = new Intent(Main_ChonTuVung.this, Main_TuVung.class);
                intent.putExtra(Main_TuVung.EXTRA_TOPIC_ID, topic.getId());
                startActivity(intent);
            }
        });

        etSearchTopicTuVung.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTopics(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadTopics() {
        allTopics = dbHelper.getAllTopics();
        filterTopics(etSearchTopicTuVung.getText() != null ? etSearchTopicTuVung.getText().toString() : "");
    }

    private void filterTopics(String query) {
        filteredTopics.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredTopics.addAll(allTopics);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();
            for (Topic t : allTopics) {
                String name = t.getName() != null ? t.getName().toLowerCase(Locale.getDefault()) : "";
                String desc = t.getDescription() != null ? t.getDescription().toLowerCase(Locale.getDefault()) : "";
                if (name.contains(lowerQuery) || desc.contains(lowerQuery)) {
                    filteredTopics.add(t);
                }
            }
        }
        adapter.updateList(filteredTopics);
    }

    private void showAddWordWithTopicDialog() {
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

        tvDialogTitle.setText("Thêm từ vựng mới");

        // Load topics for Dropdown
        List<Topic> topicsList = dbHelper.getAllTopics();
        if (topicsList.isEmpty()) {
            Toast.makeText(this, "Chưa có chủ đề nào, vui lòng tạo chủ đề trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> topicNames = new ArrayList<>();
        for (Topic t : topicsList) {
            topicNames.add(t.getName());
        }

        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, topicNames);
        actvSelectTopic.setAdapter(topicAdapter);
        actvSelectTopic.setText(topicNames.get(0), false);

        final int[] chosenTopicId = {topicsList.get(0).getId()};
        actvSelectTopic.setOnItemClickListener((parent, view, position, id) -> {
            chosenTopicId[0] = topicsList.get(position).getId();
        });

        btnCancelWord.setOnClickListener(v -> dialog.dismiss());

        btnSaveWord.setOnClickListener(v -> {
            String word = etWord.getText() != null ? etWord.getText().toString().trim() : "";
            String phonetic = etPhonetic.getText() != null ? etPhonetic.getText().toString().trim() : "";
            String meaning = etMeaning.getText() != null ? etMeaning.getText().toString().trim() : "";
            String example = etExample.getText() != null ? etExample.getText().toString().trim() : "";

            if (word.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(Main_ChonTuVung.this, "Vui lòng nhập đầy đủ Từ vựng và Nghĩa!", Toast.LENGTH_SHORT).show();
                return;
            }

            Vocabulary newVocab = new Vocabulary();
            newVocab.setTopicId(chosenTopicId[0]);
            newVocab.setWord(word);
            newVocab.setPhonetic(phonetic);
            newVocab.setMeaning(meaning);
            newVocab.setExample(example);
            newVocab.setIconName("ic_word_default");
            newVocab.setLearned(false);

            dbHelper.addVocabulary(newVocab);
            Toast.makeText(Main_ChonTuVung.this, "Thêm từ vựng mới vào chủ đề thành công!", Toast.LENGTH_SHORT).show();

            loadTopics();
            dialog.dismiss();
        });

        dialog.show();
    }
}