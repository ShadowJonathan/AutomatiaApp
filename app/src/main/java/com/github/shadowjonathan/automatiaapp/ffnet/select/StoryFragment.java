package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Archive;
import com.github.shadowjonathan.automatiaapp.ffnet.Registry;

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
            ARA = new ArchiveRecyclerAdapter(archive.reg.getList(), mListener);
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

        public ArchiveRecyclerAdapter(List<Registry.RegistryEntry> items, OnStoryTapListener listener) {
            mValues = items;
            allValues = new ArrayList<Registry.RegistryEntry>(items);
            mListener = listener;
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
            Registry.RegistryEntry ar = mValues.get(position);
            // TODO MAKE REGISTRY
            //holder.mContentView.setText(ar.name);
            //holder.mAmountView.setText(Helper.formatNumber(ar.len));

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

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            /*
            public final TextView mContentView;
            public final TextView mAmountView;
            */
            public Registry.RegistryEntry mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                //mContentView = view.findViewById(R.id.content);
                //mAmountView = view.findViewById(R.id.len);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + /*mContentView.getText() + */ "'";
            }
        }
    }
}
