package hutech.com.example.projecthoctienganh;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hutech.com.example.projecthoctienganh.adapter.TopicAdapter;
import hutech.com.example.projecthoctienganh.db.DatabaseHelper;
import hutech.com.example.projecthoctienganh.model.Topic;

public class Main_ChuDe extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ImageButton btnBackTopic;
    private EditText etSearchTopic;
    private ListView lvTopics;
    private FloatingActionButton fabAddTopic;

    private TopicAdapter adapter;
    private List<Topic> allTopics = new ArrayList<>();
    private List<Topic> filteredTopics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_chu_de);

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
        btnBackTopic = findViewById(R.id.btnBackTopic);
        etSearchTopic = findViewById(R.id.etSearchTopic);
        lvTopics = findViewById(R.id.lvTopics);
        fabAddTopic = findViewById(R.id.fabAddTopic);

        adapter = new TopicAdapter(this, filteredTopics);
        lvTopics.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackTopic.setOnClickListener(v -> finish());

        fabAddTopic.setOnClickListener(v -> showAddTopicDialog());

        lvTopics.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < filteredTopics.size()) {
                Topic topic = filteredTopics.get(position);
                Intent intent = new Intent(Main_ChuDe.this, Main_TuVung.class);
                intent.putExtra(Main_TuVung.EXTRA_TOPIC_ID, topic.getId());
                startActivity(intent);
            }
        });

        etSearchTopic.addTextChangedListener(new TextWatcher() {
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
        filterTopics(etSearchTopic.getText() != null ? etSearchTopic.getText().toString() : "");
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

    private void showAddTopicDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_sua_chu_de);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etTopicName = dialog.findViewById(R.id.etTopicName);
        EditText etTopicDesc = dialog.findViewById(R.id.etTopicDesc);
        MaterialButton btnCancelTopic = dialog.findViewById(R.id.btnCancelTopic);
        MaterialButton btnSaveTopic = dialog.findViewById(R.id.btnSaveTopic);

        btnCancelTopic.setOnClickListener(v -> dialog.dismiss());

        btnSaveTopic.setOnClickListener(v -> {
            String name = etTopicName.getText() != null ? etTopicName.getText().toString().trim() : "";
            String desc = etTopicDesc.getText() != null ? etTopicDesc.getText().toString().trim() : "";

            if (name.isEmpty()) {
                Toast.makeText(Main_ChuDe.this, "Vui lòng nhập tên chủ đề!", Toast.LENGTH_SHORT).show();
                return;
            }

            Topic newTopic = new Topic();
            newTopic.setName(name);
            newTopic.setDescription(desc);
            newTopic.setIconName("ic_flight");

            dbHelper.addTopic(newTopic);
            Toast.makeText(Main_ChuDe.this, "Thêm chủ đề mới thành công!", Toast.LENGTH_SHORT).show();

            loadTopics();
            dialog.dismiss();
        });

        dialog.show();
    }
}