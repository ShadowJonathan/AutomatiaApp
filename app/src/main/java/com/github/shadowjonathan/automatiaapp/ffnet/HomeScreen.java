package com.github.shadowjonathan.automatiaapp.ffnet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.shadowjonathan.automatiaapp.R;

public class HomeScreen extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Override
    public int getItemViewType(int position) {
        return position % 2 * 2;
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ffnet_fragment_story,
                                parent, false);
                return new ViewHolder0(view);
            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ffnet_fragment_story,
                                parent, false);
                return new ViewHolder2(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 0:
                ViewHolder0 viewHolder0 = (ViewHolder0) holder;
                //...
                break;

            case 2:
                ViewHolder2 viewHolder2 = (ViewHolder2) holder;
                //...
                break;
        }
    }

    class ViewHolder0 extends RecyclerView.ViewHolder {
        //...
        public ViewHolder0(View itemView) {
            super(itemView);
            //...
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        //...
        public ViewHolder2(View itemView) {
            super(itemView);
            //...
        }
    }
}