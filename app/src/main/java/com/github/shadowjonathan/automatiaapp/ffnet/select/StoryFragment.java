package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Archive;
import com.github.shadowjonathan.automatiaapp.ffnet.Registry;
import com.github.shadowjonathan.automatiaapp.global.Helper;

import java.util.ArrayList;
import java.util.List;

public class StoryFragment extends Fragment {

    private static final String ARG_ARCHIVE = "archive";
    private static String TAG = "ST_FRAG";
    public Archive archive;
    private OnStoryTapListener mListener;
    private ArchiveRecyclerAdapter ARA;

    public StoryFragment() {
    }

    public static StoryFragment newInstance(Archive a) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARCHIVE, a.makeID());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            archive = Archive.getArchive(getArguments().getString(ARG_ARCHIVE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ffnet_fragment_archive_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            Log.d(TAG, "onCreateView reg: "+archive.reg.getList());
            ARA = new ArchiveRecyclerAdapter(archive.reg.getList(), mListener, getContext());
            recyclerView.setAdapter(ARA);
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStoryTapListener) {
            mListener = (OnStoryTapListener) context;
        } else {
            throw new RuntimeException(
                    context.toString()
                            + " must implement OnArchiveTapListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void filter(String s) {
        ARA.filter(s);
    }

    public interface OnStoryTapListener {
        void onSTap(Registry.RegistryEntry item);
    }

    public static class ArchiveRecyclerAdapter extends RecyclerView.Adapter<ArchiveRecyclerAdapter.ViewHolder> {

        private final List<Registry.RegistryEntry> allValues;
        private final OnStoryTapListener mListener;
        private List<Registry.RegistryEntry> mValues;
        private Context context;

        public ArchiveRecyclerAdapter(List<Registry.RegistryEntry> items, OnStoryTapListener listener, Context context) {
            mValues = items;
            allValues = new ArrayList<Registry.RegistryEntry>(items);
            mListener = listener;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ffnet_fragment_archive, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            Registry.RegistryEntry re = mValues.get(position);
            // TODO MAKE REGISTRY
            //holder.mContentView.setText(ar.name);
            //holder.mAmountView.setText(Helper.formatNumber(ar.len));

            holder.title.setText(re.title);
            holder.author.setText("By "+re.author);
            holder.summary.setText(re.summary);
            holder.favorites.setText(Helper.formatNumber(re.favs));
            holder.follows.setText(Helper.formatNumber(re.follows));
            holder.reviews.setText(Helper.formatNumber(re.reviews));
            holder.words.setText(Helper.formatNumber(re.words));
            holder.chapters.setText(Helper.formatNumber(re.chapters));
            if (re.completed) {
                holder.done_or_progress.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_done));
                holder.done_or_progress.setColorFilter(R.color.story_done);
            } else {
                holder.done_or_progress.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pencil));
                holder.done_or_progress.setColorFilter(R.color.story_card_grey);
            }
            if (re.updated != null) {
                holder.updated.setText(Helper.TSUtils.makeDate(re.updated));
            } else {
                holder.updated_wrapper.setVisibility(View.GONE);
            }
            holder.published.setText(Helper.TSUtils.makeDate(re.updated));
            holder.genre.setText(TextUtils.join(", ", re.genre));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onSTap(holder.mItem);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public void filter(String s) {
            s = s.toLowerCase();
            /*
            mValues.clear();
            for (Category.ArchiveRef a : allValues) {
                if (a.name.toLowerCase().contains(s)) {
                    mValues.add(a);
                }
            }*/
            notifyDataSetChanged();

            Log.d(TAG, "filtering '" + s + String.format("'... (%d/%d)", mValues.size(), allValues.size()));
        }

        @SuppressWarnings("WeakerAccess")
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;

            public final TextView title;
            public final TextView author;
            public final TextView summary;
            public final TextView favorites;
            public final TextView follows;
            public final TextView reviews;
            public final TextView words;
            public final ImageView done_or_progress;
            public final TextView genre;
            public final TextView published;
            public final TextView chapters;
            public final TextView updated;
            public final LinearLayout updated_wrapper;

            public Registry.RegistryEntry mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                title = (TextView) view.findViewById(R.id.story_title);
                author = (TextView) view.findViewById(R.id.story_author);
                summary = (TextView) view.findViewById(R.id.story_summary);
                favorites = (TextView) view.findViewById(R.id.fav_len);
                follows = (TextView) view.findViewById(R.id.fol_len);
                reviews = (TextView) view.findViewById(R.id.rev_len);
                words = (TextView) view.findViewById(R.id.words_len);
                done_or_progress = (ImageView) view.findViewById(R.id.done_or_progress);
                genre = (TextView) view.findViewById(R.id.genre);
                published = (TextView) view.findViewById(R.id.pub_when);
                chapters = (TextView) view.findViewById(R.id.chap_len);
                updated = (TextView) view.findViewById(R.id.up_when);
                updated_wrapper = (LinearLayout) view.findViewById(R.id.up_wrap);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + title.getText() +  "'";
            }
        }
    }
}
