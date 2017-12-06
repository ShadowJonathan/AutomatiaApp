package com.github.shadowjonathan.automatiaapp.ffnet;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.background.Comms;
import com.github.shadowjonathan.automatiaapp.ffnet.select.ArchiveFragment;
import com.github.shadowjonathan.automatiaapp.ffnet.select.FFNetCategorySelectActivity;
import com.github.shadowjonathan.automatiaapp.ffnet.select.FFNetStorySelectActivity;
import com.github.shadowjonathan.automatiaapp.ffnet.select.StoryFragment;
import com.github.shadowjonathan.automatiaapp.global.DirListener;
import com.github.shadowjonathan.automatiaapp.global.GlobalViews;
import com.github.shadowjonathan.automatiaapp.global.Helper;
import com.github.shadowjonathan.automatiaapp.global.HomeScreenHelp;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.util.ArrayList;
import java.util.Collections;

import static com.github.shadowjonathan.automatiaapp.ffnet.select.StoryFragment.StoryRecyclerAdapter.makeCharactersAndShips;

public class HomeScreen extends RecyclerView.Adapter<HomeScreenHelp.CategorisedViewHolder> implements
        HomeScreenHelp.HasPalette {
    private static final String TAG = "HOMESCREEN.FFNET";

    private HomeScreenHelp.Palette palette;
    private HomeScreenHelp.GroupHolder groups;

    private PinnedArchives pinnedarchives;

    private StoryManager sm;
    private DirectoryChooserFragment chooserDialog;
    private onDirReturn currentReturn;
    public DirListener dl = new DirListener() {
        @Override
        public void dismiss() {
            chooserDialog.dismiss();
        }

        @Override
        public void select(String dir) {
            if (currentReturn != null)
                currentReturn.onReturn(dir);
            chooserDialog.dismiss();
        }
    };

    public HomeScreen() {
        palette = new HomeScreenHelp.Palette();
        groups = new HomeScreenHelp.GroupHolder();
        palette.put(GlobalViews.Header.class, 0);
        palette.put(GlobalViews.SubHeader.class, 1);
        palette.put(SimpleReferrer.class, 2);
        palette.put(StoryFragment.StoryRecyclerAdapter.ViewHolder.class, 3);
        palette.put(GlobalViews.SimpleDimmedCenteredText.class, 4);
        palette.put(ArchiveFragment.ArchiveRecyclerAdapter.ViewHolder.class, 5);
        palette.put(HomeScreenStory.class, 6);

        groups.add(new SingulairReferenceGroup(FFNetCategorySelectActivity.class, "Categories"));

        pinnedarchives = new PinnedArchives("Pinned Archives", "No archives pinned");
        groups.add(pinnedarchives);

        sm = new StoryManager();

        groups.add(sm.updatesGroup);
        groups.add(sm.downloadedingGroup);

        for (Archive a : Archive.Pinned.getAll())
            pinnedarchives.placeRef(a.getRef());
    }

    @Override
    public HomeScreenHelp.CategorisedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.global_header,
                                parent, false);
                return new GlobalViews.Header(view);
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.global_sub_header,
                                parent, false);
                return new GlobalViews.SubHeader(view);
            case 2:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.simple_referrer,
                                parent, false);
                return new SimpleReferrer(view);
            case 3:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ffnet_fragment_story,
                                parent, false);
                return new StoryFragment.StoryRecyclerAdapter.ViewHolder(view).attachContext(parent.getContext());
            case 4:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.simple_dimmed_centered_text,
                                parent, false);
                return new GlobalViews.SimpleDimmedCenteredText(view);
            case 5:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ffnet_fragment_archive,
                                parent, false);
                return new ArchiveFragment.ArchiveRecyclerAdapter.ViewHolder(view);
            case 6:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ffnet_homescreen_story,
                                parent, false);
                return new HomeScreenStory(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final HomeScreenHelp.CategorisedViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 0:
                GlobalViews.Header header = (GlobalViews.Header) holder;
                HeadedGroup headedGroup = (HeadedGroup) groups.getGroupAt(position);
                header.attachRef(headedGroup.headerRef);
                break;
            case 1:
                GlobalViews.SubHeader subheader = (GlobalViews.SubHeader) holder;
                SubHeadedGroup SubHeadedGroup = (SubHeadedGroup) groups.getGroupAt(position);
                subheader.attachRef(SubHeadedGroup.headerRef);
                break;
            case 2:
                final SimpleReferrer referrer = (SimpleReferrer) holder;
                if (groups.getGroupAt(position) instanceof SingulairReferenceGroup) {
                    SingulairReferenceGroup g = (SingulairReferenceGroup) groups.getGroupAt(position);
                    referrer.attachReference(g.ref, g.text);
                } else {
                    SimpleReferrer.SimpleReferrerBundle bundle = (SimpleReferrer.SimpleReferrerBundle) groups.getData(position);
                    referrer.attachReference(bundle.ref, bundle.text);
                }

                referrer.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        referrer.doReference(v.getContext());
                    }
                });
                break;
            case 3:
                StoryFragment.StoryRecyclerAdapter.ViewHolder story = (StoryFragment.StoryRecyclerAdapter.ViewHolder) holder;
                onBindStory(story, position);
                break;
            case 4:
                GlobalViews.SimpleDimmedCenteredText DimmedText = (GlobalViews.SimpleDimmedCenteredText) holder;
                String text = (String) groups.getData(position);
                DimmedText.attachText(text);
                break;
            case 5:
                Category.ArchiveRef ref = (Category.ArchiveRef) groups.getData(position);
                final ArchiveFragment.ArchiveRecyclerAdapter.ViewHolder viewHolder = (ArchiveFragment.ArchiveRecyclerAdapter.ViewHolder) holder;
                viewHolder.mItem = ref;
                viewHolder.mContentView.setText(ref.name);
                viewHolder.mAmountView.setText(Helper.formatNumber(ref.len));
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent ffnetI = new Intent(v.getContext(), FFNetStorySelectActivity.class);
                        ffnetI.putExtra("archive", viewHolder.mItem.getArchive().makeID());
                        v.getContext().startActivity(ffnetI);
                    }
                });
                break;
            case 6:
                HomeScreenStory HSS = (HomeScreenStory) holder;
                Story Hstory = (Story) groups.getData(position);
                HSS.bind(Hstory);
                break;
        }
    }

    public void onBindStory(final StoryFragment.StoryRecyclerAdapter.ViewHolder holder, final int position) {
        final Registry.RegistryEntry re = (Registry.RegistryEntry) groups.getData(position);
        holder.title.setText(re.title);
        holder.author.setText("By " + re.author);
        holder.summary.setText(re.summary);
        holder.favorites.setText(Helper.formatNumber(re.favs));
        holder.follows.setText(Helper.formatNumber(re.follows));
        holder.reviews.setText(Helper.formatNumber(re.reviews));
        holder.words.setText(Helper.formatNumber(re.words));
        holder.chapters.setText(Helper.formatNumber(re.chapters));
        if (re.completed) {
            holder.done_or_progress.setImageDrawable(ContextCompat.getDrawable(holder.context, R.drawable.ic_done));
            holder.done_or_progress.setColorFilter(ContextCompat.getColor(holder.context, R.color.story_done));
        } else {
            holder.done_or_progress.setImageDrawable(ContextCompat.getDrawable(holder.context, R.drawable.ic_pencil));
            holder.done_or_progress.setColorFilter(ContextCompat.getColor(holder.context, R.color.story_card_grey));
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
                Log.d("HOMEMENU", "onMenuItemClick: pressed " + item.toString());
                switch (item.getItemId()) {
                    case R.id.action_copy_url:
                        ClipboardManager clipboard = (ClipboardManager) holder.context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Url for " + re.title, re.fullURL());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(holder.context, "Copied URL", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "TAPPED ITEM: " + holder.title.getText(), Toast.LENGTH_SHORT).show();
                //if (null != mListener) {
                //    mListener.onSTap(holder.mItem);
                //}
            }
        });
    }

    @Override
    public HomeScreenHelp.Palette getPalette() {
        return palette;
    }

    @Override
    public int getItemViewType(int position) {
        return groups.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return groups.amount();
    }

    public boolean checkStoragePerms(Activity context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.READ_CONTACTS)) {

            } else {
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            return false;
        } else
            return true;
    }

    class SimpleReferrer extends HomeScreenHelp.CategorisedViewHolder {
        protected View view;
        private Class reference;
        private TextView text;

        public SimpleReferrer(View itemView) {
            super(itemView);
            view = itemView;
            text = (TextView) itemView.findViewById(R.id.content);
        }

        public void attachReference(Class ref, String text) {
            reference = ref;
            this.text.setText(text);
        }

        public void doReference(Context from) {
            Intent ffnetI = new Intent(from, reference);
            from.startActivity(ffnetI);
        }

        @Override
        public int getType(HomeScreenHelp.Palette fromPalette) {
            return fromPalette.getType(SimpleReferrer.class);
        }

        public class SimpleReferrerBundle {
            public Class ref;
            public String text;

            public SimpleReferrerBundle(Class ref, String text) {
                this.ref = ref;
                this.text = text;
            }
        }
    }

    class HomeScreenStory extends HomeScreenHelp.CategorisedViewHolder {
        protected View view;
        private Story story;

        private TextView story_title;
        private TextView story_archive;
        private TextView story_author;

        private LinearLayout progress_wrapper;
        private TextView progress_text;
        private ProgressBar progress;

        private LinearLayout downloaded_wrapper;
        private Button action_update;

        private Toolbar actions;

        private int state;

        public HomeScreenStory(View itemView) {
            super(itemView);
            view = itemView;
            story_title = (TextView) view.findViewById(R.id.story_title);
            story_archive = (TextView) view.findViewById(R.id.story_archive);
            story_author = (TextView) view.findViewById(R.id.story_author);

            progress_wrapper = (LinearLayout) view.findViewById(R.id.progress_wrapper);
            progress_text = (TextView) view.findViewById(R.id.progress_text);
            progress = (ProgressBar) view.findViewById(R.id.progress);

            downloaded_wrapper = (LinearLayout) view.findViewById(R.id.downloaded_wrapper);
            action_update = (Button) view.findViewById(R.id.action_update);

            actions = (Toolbar) view.findViewById(R.id.card_toolbar);

            actions.inflateMenu(R.menu.ffnet_home_story_card);
        }

        public void bind(final Story s) {
            final Activity a = (Activity) view.getContext();
            story = s;
            story_title.setText(s.info.title);
            story_archive.setText("in " + s.from().getViewableName());
            story_author.setText("by " + s.info.author);

            progress_wrapper.setVisibility(View.GONE);
            downloaded_wrapper.setVisibility(View.GONE);
            action_update.setVisibility(View.GONE);

            state = s.getState();

            switch (state) {
                case Story.UPDATE_READY:
                    action_update.setVisibility(View.VISIBLE);
                case Story.DOWNLOADED:
                    downloaded_wrapper.setVisibility(View.VISIBLE);
                    break;
                case Story.DOWNLOADING:
                    progress_wrapper.setVisibility(View.VISIBLE);
                    break;
            }

            progress.setIndeterminate(true);
            progress_text.setText("Downloading...");

            s.registerProgress(new Story.ProgressState() {
                @Override
                void showProgress(final int total, final int progress) {
                    ((Activity) view.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progress == 0 && total == 0) {
                                HomeScreenStory.this.progress.setIndeterminate(true);
                            } else {
                                progress_text.setText(progress + "/" + total + " chapter" + (total != 1 ? "s" : "") + "...");
                                HomeScreenStory.this.progress.setIndeterminate(false);
                                HomeScreenStory.this.progress.setMax(total);
                                HomeScreenStory.this.progress.setProgress(progress);
                            }
                        }
                    });
                }

                @Override
                void stateChange(final int newState) {
                    ((Activity) view.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress_wrapper.setVisibility(View.GONE);
                            downloaded_wrapper.setVisibility(View.GONE);
                            action_update.setVisibility(View.GONE);

                            switch (newState) {
                                case Story.UPDATE_READY:
                                    action_update.setVisibility(View.VISIBLE);
                                case Story.DOWNLOADED:
                                    downloaded_wrapper.setVisibility(View.VISIBLE);
                                    break;
                                case Story.DOWNLOADING:
                                    progress_wrapper.setVisibility(View.VISIBLE);
                                    break;
                            }
                            sm.listener.onChange(story, newState);
                            state = newState;
                        }
                    });
                }
            });

            action_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    s.putDownload();
                }
            });

            actions.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.export_story:
                            if (checkStoragePerms(a)) {
                                chooserDialog = DirectoryChooserFragment.newInstance(DirectoryChooserConfig.builder()
                                        .newDirectoryName("Stories")
                                        .allowReadOnlyDirectory(false)
                                        .allowNewDirectoryNameModification(true)
                                        .initialDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                                        .build());

                                Log.d(TAG, "onMenuItemClick: Opening on " + Environment.getExternalStorageDirectory().getAbsolutePath());

                                chooserDialog.show(((Activity) view.getContext()).getFragmentManager(), null);
                                currentReturn = new onDirReturn() {
                                    @Override
                                    void onReturn(String dir) {
                                        if (story.copyTo(dir)) {
                                            Toast.makeText(view.getContext(), "Moved to '" + dir + "'...", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                return true;
                            }
                        case R.id.redownload_story:
                            s.putDownload();
                            return true;
                    }
                    return false;
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Clicked " + s.info.title, Toast.LENGTH_SHORT).show();
                }
            });

            if (s.p_total > 0 && Comms.online) s.progress.showProgress(s.p_total, s.p_state);
        }

        @Override
        public int getType(HomeScreenHelp.Palette fromPalette) {
            return fromPalette.getType(HomeScreenStory.class);
        }
    }

    class SingulairReferenceGroup extends HomeScreenHelp.Group {
        protected Class ref;
        protected String text;

        public SingulairReferenceGroup(Class reference, String text) {
            super(HomeScreen.this.getPalette());
            ref = reference;
            this.text = text;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return palette.getType(SimpleReferrer.class);
            else
                throw new Error("ASKED FOR POSITION OUT OF RANGE: " + position);
        }

        @Override
        public int size() {
            return 1;
        }
    }

    class HeadedGroup extends HomeScreenHelp.Group {
        protected GlobalViews.HeaderRef headerRef;

        HeadedGroup(String text) {
            super(HomeScreen.this.getPalette());
            headerRef = new GlobalViews.HeaderRef(text);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return palette.getType(GlobalViews.Header.class);
            return super.getItemViewType(position) - 1;
        }

        @Override
        public int size() {
            return super.size() + 1;
        }
    }

    class SubHeadedGroup extends HeadedGroup {
        SubHeadedGroup(String text) {
            super(text);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return palette.getType(GlobalViews.SubHeader.class);
            return super.getItemViewType(position) - 1;
        }
    }

    class PlaceHoldedGroup extends HeadedGroup {
        protected String absense;

        PlaceHoldedGroup(String text, String absensetext) {
            super(text);
            absense = absensetext;
        }

        @Override
        public Object getData(int position) {
            if (position == 1 && super.size() == 1 && data.size() == 0)
                return absense;
            return super.getData(position);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 1 && super.size() == 1 && data.size() == 0)
                return palette.getType(GlobalViews.SimpleDimmedCenteredText.class);
            return super.getItemViewType(position);
        }

        @Override
        public int size() {
            if (super.size() == 1)
                return 2;
            else
                return super.size();
        }
    }

    abstract class onDirReturn {
        abstract void onReturn(String dir);
    }

    final class PinnedArchives extends PlaceHoldedGroup {
        PinnedArchives(String text, String absensetext) {
            super(text, absensetext);
        }

        @Override
        public int size() {
            if (data.size() < 1)
                return super.size();
            else
                return data.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (data.size() > 0 && position > 0)
                return palette.getType(ArchiveFragment.ArchiveRecyclerAdapter.ViewHolder.class);
            return super.getItemViewType(position);
        }

        public void placeRef(Category.ArchiveRef ref) {
            data.put(data.size() + 1, ref);
        }
    }

    final class StoryGroup extends PlaceHoldedGroup {
        private ArrayList<Story> data;

        StoryGroup(String text, String absensetext, ArrayList<Story> data) {
            super(text, absensetext);
            this.data = data;
        }

        @Override
        public int size() {
            if (data.size() < 1)
                return super.size();
            else
                return data.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (data.size() > 0 && position > 0)
                return palette.getType(HomeScreenStory.class);
            return super.getItemViewType(position);
        }

        @Override
        public Object getData(int position) {
            if (data.size() != 0)
                return data.get(position - 1);
            return super.getData(position);
        }
    }

    final class StoryManager {
        private static final String TAG = "STORYMANAGER";
        public StoryGroup downloadedingGroup;
        public StoryGroup updatesGroup;
        private ArrayList<Story> stories;
        private ArrayList<Story> downloadeding = new ArrayList<>();
        private ArrayList<Story> updates = new ArrayList<>();
        public onImportantStateChange listener = new onImportantStateChange() {
            @Override
            void onChange(Story s, int newstate) {
                switch (newstate) {
                    case Story.DOWNLOADED:
                    case Story.DOWNLOADING:
                        updates.remove(s);
                        if (!downloadeding.contains(s))
                            downloadeding.add(0, s);
                        HomeScreen.this.notifyDataSetChanged();
                        break;
                    case Story.UPDATE_READY:
                        downloadeding.remove(s);
                        if (!updates.contains(s))
                            updates.add(0, s);
                        HomeScreen.this.notifyDataSetChanged();
                        break;
                }
            }
        };

        StoryManager() {
            stories = Story.getList();
            for (Story s :
                    stories) {
                switch (s.getState()) {
                    case Story.DOWNLOADED:
                    case Story.DOWNLOADING:
                        Log.d(TAG, "StoryManager: update not ready for " + s);
                        downloadeding.add(s);
                        break;
                    case Story.UPDATE_READY:
                        Log.d(TAG, "StoryManager: update ready for " + s);
                        updates.add(s);
                        break;
                }
            }

            Collections.reverse(downloadeding);
            downloadedingGroup = new StoryGroup("Stories", "No stories downloaded", downloadeding);
            updatesGroup = new StoryGroup("Updates", "All updates downloaded!", updates);
            Log.d(TAG, "StoryManager: LEN " + stories.size());
        }

        public abstract class onImportantStateChange {
            abstract void onChange(Story s, int newstate);
        }
    }
}