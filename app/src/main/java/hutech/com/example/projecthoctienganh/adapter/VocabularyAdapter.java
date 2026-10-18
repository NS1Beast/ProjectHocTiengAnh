package hutech.com.example.projecthoctienganh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hutech.com.example.projecthoctienganh.R;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Vocabulary vocabulary, int position);
    }

    public interface OnStatusClickListener {
        void OnStatusClick(Vocabulary vocabulary, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Vocabulary vocabulary, int position);
    }

    private final Context context;
    private List<Vocabulary> vocabularyList;
    private OnItemClickListener itemClickListener;
    private OnStatusClickListener statusClickListener;
    private OnItemLongClickListener itemLongClickListener;

    public VocabularyAdapter(Context context, List<Vocabulary> vocabularyList) {
        this.context = context;
        this.vocabularyList = vocabularyList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnStatusClickListener(OnStatusClickListener listener) {
        this.statusClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    public void updateList(List<Vocabulary> newList) {
        this.vocabularyList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tu_vung, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vocabulary vocab = vocabularyList.get(position);

        holder.tvEnglishWord.setText(vocab.getWord());
        holder.tvPhonetic.setText(vocab.getPhonetic());
        holder.tvVietnameseMeaning.setText(vocab.getMeaning());

        // Icon resolution
        int iconResId = getIconResId(vocab.getIconName());
        holder.imgLeadingIcon.setImageResource(iconResId);

        // Learned Status Checkmark Button
        if (vocab.isLearned()) {
            holder.btnStatusToggle.setImageResource(R.drawable.ic_check_circle_green);
        } else {
            holder.btnStatusToggle.setImageResource(R.drawable.ic_circle_outline_gray);
        }

        // Listeners
        holder.btnStatusToggle.setOnClickListener(v -> {
            if (statusClickListener != null) {
                statusClickListener.OnStatusClick(vocab, holder.getBindingAdapterPosition());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(vocab, holder.getBindingAdapterPosition());
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemLongClick(vocab, holder.getBindingAdapterPosition());
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList != null ? vocabularyList.size() : 0;
    }

    private int getIconResId(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_word_default;
        }
        int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.ic_word_default;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLeadingIcon;
        TextView tvEnglishWord;
        TextView tvPhonetic;
        TextView tvVietnameseMeaning;
        ImageButton btnStatusToggle;
        ImageView imgArrowRight;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLeadingIcon = itemView.findViewById(R.id.imgLeadingIcon);
            tvEnglishWord = itemView.findViewById(R.id.tvEnglishWord);
            tvPhonetic = itemView.findViewById(R.id.tvPhonetic);
            tvVietnameseMeaning = itemView.findViewById(R.id.tvVietnameseMeaning);
            btnStatusToggle = itemView.findViewById(R.id.btnStatusToggle);
            imgArrowRight = itemView.findViewById(R.id.imgArrowRight);
        }
    }
}