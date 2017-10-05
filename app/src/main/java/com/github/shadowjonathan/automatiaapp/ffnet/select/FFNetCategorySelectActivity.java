package com.github.shadowjonathan.automatiaapp.ffnet.select;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.github.shadowjonathan.automatiaapp.R;
import com.github.shadowjonathan.automatiaapp.ffnet.Category;

public class FFNetCategorySelectActivity extends AppCompatActivity implements CategoryFragment.OnCategoryTapListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ffnet_select_category);
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
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCTap(Category item) {
        openCategory(item);
    }

    public void openCategory(Category cat) {
        Intent ffnetI = new Intent(this, FFNetArchiveSelectActivity.class);
        ffnetI.putExtra("cat", cat.name);
        startActivity(ffnetI);
    }
}
