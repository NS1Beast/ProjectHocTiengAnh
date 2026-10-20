package hutech.com.example.projecthoctienganh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import hutech.com.example.projecthoctienganh.R;
import hutech.com.example.projecthoctienganh.model.Vocabulary;

public class VocabularyAdapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(Vocabulary vocabulary, int position);
    }

    public interface OnStatusClickListener {
        void OnStatusClick(Vocabulary vocabulary, int position);
    }

    private final Context context;
    private List<Vocabulary> vocabularyList;
    private OnItemClickListener itemClickListener;
    private OnStatusClickListener statusClickListener;

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

    public void updateList(List<Vocabulary> newList) {
        this.vocabularyList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return vocabularyList != null ? vocabularyList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return vocabularyList != null ? vocabularyList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tu_vung, parent, false);
            holder = new ViewHolder();
            holder.tvEnglishWord = convertView.findViewById(R.id.tvEnglishWord);
            holder.tvPhonetic = convertView.findViewById(R.id.tvPhonetic);
            holder.tvVietnameseMeaning = convertView.findViewById(R.id.tvVietnameseMeaning);
            holder.btnStatusToggle = convertView.findViewById(R.id.btnStatusToggle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Vocabulary vocab = vocabularyList.get(position);
        if (vocab != null) {
            holder.tvEnglishWord.setText(vocab.getWord());
            holder.tvPhonetic.setText(vocab.getPhonetic());
            holder.tvVietnameseMeaning.setText(vocab.getMeaning());

            if (vocab.isLearned()) {
                holder.btnStatusToggle.setImageResource(R.drawable.ic_check_circle_green);
            } else {
                holder.btnStatusToggle.setImageResource(R.drawable.ic_circle_outline_gray);
            }

            holder.btnStatusToggle.setOnClickListener(v -> {
                if (statusClickListener != null) {
                    statusClickListener.OnStatusClick(vocab, position);
                }
            });

            convertView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(vocab, position);
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvEnglishWord;
        TextView tvPhonetic;
        TextView tvVietnameseMeaning;
        ImageButton btnStatusToggle;
    }
}