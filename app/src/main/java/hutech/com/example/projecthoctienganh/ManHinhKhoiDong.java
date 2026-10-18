package hutech.com.example.projecthoctienganh;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ManHinhKhoiDong extends AppCompatActivity {

    private ImageView imgTuVung, imgChuDe, imgFlashcard, imgKiemTra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_man_hinh_khoi_dong);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        imgTuVung = findViewById(R.id.img_tuvung);
        imgChuDe = findViewById(R.id.img_chude);
        imgFlashcard = findViewById(R.id.img_flashcard);
        imgKiemTra = findViewById(R.id.img_kiemtra);
    }

    private void setupListeners() {
        if (imgChuDe != null) {
            imgChuDe.setOnClickListener(v -> {
                Intent intent = new Intent(ManHinhKhoiDong.this, Main_ChuDe.class);
                startActivity(intent);
            });
        }

        if (imgTuVung != null) {
            imgTuVung.setOnClickListener(v -> {
                Intent intent = new Intent(ManHinhKhoiDong.this, Main_ChonTuVung.class);
                startActivity(intent);
            });
        }

        if (imgFlashcard != null) {
            imgFlashcard.setOnClickListener(v -> {
                Intent intent = new Intent(ManHinhKhoiDong.this, Main_FlashCard.class);
                startActivity(intent);
            });
        }

        if (imgKiemTra != null) {
            imgKiemTra.setOnClickListener(v -> {
                Intent intent = new Intent(ManHinhKhoiDong.this, Main_KiemTra.class);
                startActivity(intent);
            });
        }
    }
}