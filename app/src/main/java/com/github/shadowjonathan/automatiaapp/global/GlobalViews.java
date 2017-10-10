package com.github.shadowjonathan.automatiaapp.global;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.shadowjonathan.automatiaapp.R;

public class GlobalViews {
    private GlobalViews() {
    }

    public static class Header extends RecyclerView.ViewHolder {
        public int layout = R.layout.global_header;
        public TextView text;

        public Header(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.content);
        }
    }

    public static class SubHeader extends RecyclerView.ViewHolder {
        public int layout = R.layout.global_sub_header;
        public TextView text;

        public SubHeader(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
