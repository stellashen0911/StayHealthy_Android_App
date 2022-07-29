package com.example.stayhealthy_android_app.Journey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.stayhealthy_android_app.R;

public class JourneyPostAdapter  extends RecyclerView.Adapter<JourneyPostViewHolder> {

    private final List<JourneyPost> posts;
    private final Context context;
    public JourneyPostAdapter(List<JourneyPost> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }
    @NonNull
    @Override
    public JourneyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new JourneyPostViewHolder(layoutInflater.inflate(R.layout.post_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull JourneyPostViewHolder holder, int position) {
        holder.bindJourneyPost(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
