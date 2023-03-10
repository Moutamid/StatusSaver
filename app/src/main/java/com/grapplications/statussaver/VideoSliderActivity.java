package com.grapplications.statussaver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grapplications.statussaver.adapter.VideoSliderAdapter;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;
import com.grapplications.statussaver.utils.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoSliderActivity extends AppCompatActivity {

    private FloatingActionButton download, delete;
    private ViewPager2 viewPager;
    private PreferenceManager preferenceManager;

    private String container = null;
    private List<StatusItem> statusItems;
    private VideoSliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_slider);

        Toolbar toolbar = findViewById(R.id.toolbar_video);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        preferenceManager = new PreferenceManager(this);

        if (getIntent().getExtras() != null)
            container = getIntent().getExtras().getString("container");

        setUpButtons();
        setUpSlider();
    }

    private void setUpButtons() {
        //private ImageButton play;
        FloatingActionButton share = findViewById(R.id.fab_share);
        FloatingActionButton waShare = findViewById(R.id.fab_wa_share);
        download = findViewById(R.id.fab_download);
        delete = findViewById(R.id.fab_delete);

        share.setOnClickListener(v -> sliderAdapter.share(viewPager.getCurrentItem()));

        waShare.setOnClickListener(v -> sliderAdapter.wa_share(viewPager.getCurrentItem()));

        download.setOnClickListener(v -> sliderAdapter.download(viewPager.getCurrentItem()));

        delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(VideoSliderActivity.this);
            builder.setTitle("Delete")
                    .setMessage("Do you sure want to delete this status?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        sliderAdapter.delete(viewPager.getCurrentItem());
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void setUpSlider() {
        statusItems = new ArrayList<>();
        sliderAdapter = new VideoSliderAdapter(this, statusItems);
        int itemPos = getDataWithPos();

        viewPager = findViewById(R.id.viewpager_video_slider);
        viewPager.setAdapter(sliderAdapter);
        viewPager.setCurrentItem(itemPos, false);
    }

    private int getDataWithPos() {
        int pos = 0;

        if (container != null && container.equalsIgnoreCase("recent")) {
            download.setVisibility(View.VISIBLE);
            delete.setVisibility(View.GONE);

            if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp)))
                getItems(MyConstants.allWAVideoItems);

            else if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp_business)))
                getItems(MyConstants.allW4bVideoItems);

            if (getIntent().getExtras() != null) pos = getIntent().getExtras().getInt("pos");

        } else if (container != null && container.equalsIgnoreCase("saved")) {
            delete.setVisibility(View.VISIBLE);
            download.setVisibility(View.GONE);

            String filePath = null;
            if (getIntent().getExtras() != null)
                filePath = getIntent().getExtras().getString("filePath");

            for (int i = 0; i < MyConstants.allSavedItems.size(); i++) {
                StatusItem item = MyConstants.allSavedItems.get(i);
                if (item.getFilePath().endsWith(".mp4") || item.getFilePath().endsWith(".3gp")) statusItems.add(item);
            }

            for (int i = 0; i < MyConstants.allSavedItems.size(); i++) {
                StatusItem item = MyConstants.allSavedItems.get(i);
                if (item.getFilePath().equals(filePath)) break;
                else if (item.getFilePath().endsWith(".mp4") || item.getFilePath().endsWith(".3gp")) pos += 1;
            }
        }

        return pos;
    }

    private void getItems(List<StatusItem> allVideoItems) {
        for (StatusItem item : allVideoItems) {
            if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                item.setDownloaded(true);

            statusItems.add(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
