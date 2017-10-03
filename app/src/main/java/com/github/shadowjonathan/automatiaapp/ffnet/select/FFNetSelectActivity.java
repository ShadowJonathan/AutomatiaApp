package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Category;

public class FFNetSelectActivity extends AppCompatActivity implements CategoryFragment.OnCategoryTapListener, ArchiveFragment.OnArchiveTapListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ffnet_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.root_layout, CategoryFragment.newInstance(), "categoryList")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                ((Toolbar) findViewById(R.id.toolbar)).setTitle(R.string.title_activity_ffnet_main);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCTap(Category item) {
        final ArchiveFragment aFrag =
                ArchiveFragment.newInstance(item);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root_layout, aFrag, "rageComicDetails")
                .addToBackStack(null)
                .commit();
        ((Toolbar) findViewById(R.id.toolbar)).setTitle(item.getViewableName());
    }

    @Override
    public void onATap(Category.ArchiveRef item) {
        Toast.makeText(this, "Selected " + item.name, Toast.LENGTH_SHORT).show();
    }
}
