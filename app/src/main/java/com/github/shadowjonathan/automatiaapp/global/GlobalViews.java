package com.github.shadowjonathan.automatiaapp.global;

import android.view.View;
import android.widget.TextView;

import com.github.shadowjonathan.automatiaapp.R;

public class GlobalViews {
    private GlobalViews() {
    }

    public static class Header extends HomeScreenHelp.CategorisedViewHolder {
        public int layout = R.layout.global_header;
        public TextView text;

        public Header(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.content);
        }

        @Override
        public int getType(HomeScreenHelp.Palette fromPalette) {
            return fromPalette.getType(Header.class);
        }

        public void attachRef(HeaderRef ref) {
            text.setText(ref.text);
        }
    }

    public static class SubHeader extends HomeScreenHelp.CategorisedViewHolder {
        public int layout = R.layout.global_sub_header;
        public TextView text;

        public SubHeader(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.content);
        }

        @Override
        public int getType(HomeScreenHelp.Palette fromPalette) {
            return fromPalette.getType(SubHeader.class);
        }

        public void attachRef(HeaderRef ref) {
            text.setText(ref.text);
        }
    }

    public static class SimpleDimmedCenteredText extends HomeScreenHelp.CategorisedViewHolder {
        public TextView text;

        public SimpleDimmedCenteredText(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);

        }

        @Override
        public int getType(HomeScreenHelp.Palette fromPalette) {
            return fromPalette.getType(SimpleDimmedCenteredText.class);
        }

        public void attachText(String text) {
            this.text.setText(text);
        }
    }

    public static class HeaderRef {
        protected String text;

        public HeaderRef(String text) {
            this.text = text;
        }
    }
}
