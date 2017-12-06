package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Registry;
import com.github.shadowjonathan.automatiaapp.ffnet.Story;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.HomeScreenHelp;

import java.util.ArrayList;
import java.util.List;

public class StoryFragment extends Fragment {

    private static String TAG = "ST_FRAG";

    /*
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
            ARA = new StoryRecyclerAdapter(archive.reg.getList(), mListener, getContext());
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
    }*/

    interface OnStoryTapListener {
        void onSTap(Registry.RegistryEntry item);
    }

    public static class StoryRecyclerAdapter extends RecyclerView.Adapter<StoryRecyclerAdapter.ViewHolder> {

        private final List<Registry.RegistryEntry> allValues;
        private final OnStoryTapListener mListener;
        private List<Registry.RegistryEntry> mValues;
        private Context context;

        StoryRecyclerAdapter(List<Registry.RegistryEntry> items, OnStoryTapListener listener, Context context) {
            mValues = items;
            allValues = new ArrayList<>(items);
            mListener = listener;
            this.context = context;
        }

        public static String makeCharactersAndShips(ArrayList<String> characters, ArrayList<String> ships) {
            if (ships.isEmpty())
                return TextUtils.join(", ", characters);
            else {
                ArrayList<String> unShippedCharacters = new ArrayList<>(characters);
                String shipString = "";
                for (String ship : ships) {
                    for (String s : ship.split("/"))
                        unShippedCharacters.remove(s);
                    shipString += "[" + ship + "]";
                }

                if (!unShippedCharacters.isEmpty())
                    return TextUtils.join(", ", unShippedCharacters) + ", " + shipString;
                else
                    return shipString;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ffnet_fragment_story, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            final Registry.RegistryEntry re = mValues.get(position);
            /*
            Log.d(TAG, "onBindViewHolder: " + mValues + " " + mValues.size() + " " + position + " " + System.identityHashCode(holder));
            Log.d(TAG, "onBindViewHolder: " + holder.title + " " + holder.mView.getId() + "*" + R.id.ffnet_story_card);
            */
            holder.title.setText(re.title);
            holder.author.setText("By " + re.author);
            holder.summary.setText(re.summary);
            holder.favorites.setText(Helper.formatNumber(re.favs));
            holder.follows.setText(Helper.formatNumber(re.follows));
            holder.reviews.setText(Helper.formatNumber(re.reviews));
            holder.words.setText(Helper.formatNumber(re.words));
            holder.chapters.setText(Helper.formatNumber(re.chapters));
            if (re.completed) {
                holder.done_or_progress.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_done));
                holder.done_or_progress.setColorFilter(ContextCompat.getColor(context, R.color.story_done));
            } else {
                holder.done_or_progress.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pencil));
                holder.done_or_progress.setColorFilter(ContextCompat.getColor(context, R.color.story_card_grey));
            }
            if (re.updated != null)
                holder.updated.setText(Helper.TSUtils.makeDate(re.updated));
            else
                holder.updated_wrapper.setVisibility(View.GONE);

            holder.published.setText(Helper.TSUtils.makeDate(re.published));

            if (re.genre.isEmpty())
                holder.genre_wrapper.setVisibility(View.GONE);
            else
                holder.genre.setText(TextUtils.join(", ", re.genre));

            if (re.characters.isEmpty())
                holder.characters_row.setVisibility(View.GONE);
            else
                holder.characters.setText(makeCharactersAndShips(re.characters, re.ships));

            holder.actions.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Log.d(TAG, "onMenuItemClick: pressed " + item.toString());
                    switch (item.getItemId()) {
                        case R.id.action_copy_url:
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Url for " + re.title, re.fullURL());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(context, "Copied URL", Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.action_download:
                            Story s = Story.getStory(re.storyID, re);
                            if (s.putDownload()) {
                                Toast.makeText(context, "Downloading...", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Download already queried", Toast.LENGTH_SHORT).show();
                            }
                            Log.d(TAG, "onMenuItemClick: CALLED DOWNLOAD");
                            break;
                    }
                    return false;
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onSTap(holder.mItem);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        List<Registry.RegistryEntry> getItems() {
            return allValues;
        }

        public void sortBy(int type) {
            notifyDataSetChanged();
        }

        @SuppressWarnings("WeakerAccess")
        public static class ViewHolder extends HomeScreenHelp.CategorisedViewHolder {
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
            public final LinearLayout genre_wrapper;
            public final TextView published;
            public final TextView chapters;
            public final TextView updated;
            public final LinearLayout updated_wrapper;
            public final Toolbar actions;
            public final TableRow characters_row;
            public final TextView characters;

            public Registry.RegistryEntry mItem;

            public Context context;

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
                genre_wrapper = (LinearLayout) view.findViewById(R.id.genre_wrapper);
                published = (TextView) view.findViewById(R.id.pub_when);
                chapters = (TextView) view.findViewById(R.id.chap_len);
                updated = (TextView) view.findViewById(R.id.up_when);
                updated_wrapper = (LinearLayout) view.findViewById(R.id.up_wrap);
                actions = (Toolbar) view.findViewById(R.id.card_toolbar);
                characters_row = (TableRow) view.findViewById(R.id.person_row);
                characters = (TextView) view.findViewById(R.id.person_text);

                actions.inflateMenu(R.menu.ffnet_story_card);

                Log.d(TAG, "ViewHolder called " + System.identityHashCode(this));
            }

            public ViewHolder attachContext(Context context) {
                this.context = context;
                return this;
            }

            @Override
            public String toString() {
                return super.toString() + " '" + (title == null ? "" : title.getText()) + "'";
            }

            @Override
            public int getType(HomeScreenHelp.Palette fromPalette) {
                return fromPalette.getType(ViewHolder.class);
            }
        }
    }
}
