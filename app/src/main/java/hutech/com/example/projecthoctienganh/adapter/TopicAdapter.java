package hutech.com.example.projecthoctienganh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hutech.com.example.projecthoctienganh.R;
import hutech.com.example.projecthoctienganh.model.Topic;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    public interface OnTopicClickListener {
        void onTopicClick(Topic topic);
    }

    private final Context context;
    private List<Topic> topicList;
    private OnTopicClickListener clickListener;

    public TopicAdapter(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    public void setOnTopicClickListener(OnTopicClickListener listener) {
        this.clickListener = listener;
    }

    public void updateList(List<Topic> newList) {
        this.topicList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topic topic = topicList.get(position);

        holder.tvTopicName.setText(topic.getName());
        holder.tvTopicDesc.setText(topic.getDescription());

        int iconResId = getIconResId(topic.getIconName());
        holder.imgTopicIcon.setImageResource(iconResId);

        int total = topic.getTotalWords();
        int learned = topic.getLearnedWords();
        int percent = total > 0 ? (learned * 100 / total) : 0;

        holder.progressBarTopic.setProgress(percent);
        holder.tvTopicStats.setText(learned + "/" + total + " từ (" + percent + "%)");

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTopicClick(topic);
            }
        });
    }

    @Override
    public int getItemCount() {
        return topicList != null ? topicList.size() : 0;
    }

    private int getIconResId(String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return R.drawable.ic_flight;
        }
        int resId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        return resId != 0 ? resId : R.drawable.ic_flight;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTopicIcon;
        TextView tvTopicName;
        TextView tvTopicDesc;
        ProgressBar progressBarTopic;
        TextView tvTopicStats;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTopicIcon = itemView.findViewById(R.id.imgTopicIcon);
            tvTopicName = itemView.findViewById(R.id.tvTopicName);
            tvTopicDesc = itemView.findViewById(R.id.tvTopicDesc);
            progressBarTopic = itemView.findViewById(R.id.progressBarTopic);
            tvTopicStats = itemView.findViewById(R.id.tvTopicStats);
        }
    }
}