package hutech.com.example.projecthoctienganh;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import hutech.com.example.projecthoctienganh.adapter.TopicAdapter;
import hutech.com.example.projecthoctienganh.db.DatabaseHelper;
import hutech.com.example.projecthoctienganh.model.Topic;

public class Main_ChuDe extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private ImageButton btnBackTopic;
    private EditText etSearchTopic;
    private RecyclerView rvTopics;
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
        rvTopics = findViewById(R.id.rvTopics);
        fabAddTopic = findViewById(R.id.fabAddTopic);

        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TopicAdapter(this, filteredTopics);
        rvTopics.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBackTopic.setOnClickListener(v -> finish());

        fabAddTopic.setOnClickListener(v -> showAddTopicDialog());

        adapter.setOnTopicClickListener(topic -> {
            Intent intent = new Intent(Main_ChuDe.this, Main_TuVung.class);
            intent.putExtra(Main_TuVung.EXTRA_TOPIC_ID, topic.getId());
            startActivity(intent);
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
        if (query.trim().isEmpty()) {
            filteredTopics.addAll(allTopics);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Topic t : allTopics) {
                if (t.getName().toLowerCase().contains(lowerQuery) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(lowerQuery))) {
                    filteredTopics.add(t);
                }
            }
        }
        adapter.updateList(filteredTopics);
    }

    private void showAddTopicDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_edit_topic);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextInputEditText etTopicName = dialog.findViewById(R.id.etTopicName);
        TextInputEditText etTopicDesc = dialog.findViewById(R.id.etTopicDesc);
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