package hutech.com.example.projecthoctienganh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import hutech.com.example.projecthoctienganh.R;
import hutech.com.example.projecthoctienganh.model.Topic;

public class TopicAdapter extends BaseAdapter {

    private final Context context;
    private List<Topic> topicList;

    public TopicAdapter(Context context, List<Topic> topicList) {
        this.context = context;
        this.topicList = topicList;
    }

    public void updateList(List<Topic> newList) {
        this.topicList = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return topicList != null ? topicList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return topicList != null ? topicList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chu_de, parent, false);
            holder = new ViewHolder();
            holder.tvTopicName = convertView.findViewById(R.id.tvTopicName);
            holder.tvTopicDesc = convertView.findViewById(R.id.tvTopicDesc);
            holder.progressBarTopic = convertView.findViewById(R.id.progressBarTopic);
            holder.tvTopicStats = convertView.findViewById(R.id.tvTopicStats);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Topic topic = topicList.get(position);
        if (topic != null) {
            holder.tvTopicName.setText(topic.getName());
            holder.tvTopicDesc.setText(topic.getDescription());

            int total = topic.getTotalWords();
            int learned = topic.getLearnedWords();
            int percent = total > 0 ? (learned * 100 / total) : 0;

            holder.progressBarTopic.setProgress(percent);
            holder.tvTopicStats.setText(learned + "/" + total + " từ (" + percent + "%)");
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvTopicName;
        TextView tvTopicDesc;
        ProgressBar progressBarTopic;
        TextView tvTopicStats;
    }
}