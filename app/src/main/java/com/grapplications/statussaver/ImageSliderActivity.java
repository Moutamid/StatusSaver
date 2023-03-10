package com.grapplications.statussaver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grapplications.statussaver.adapter.ImageSliderAdapter;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;
import com.grapplications.statussaver.utils.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageSliderActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager2 viewpager;
    private FloatingActionButton download, delete;
    private ImageButton expand;
    //private BottomSheetBehavior bottomSheetBehavior;
    private PreferenceManager preferenceManager;

    private String container = null;
    private List<StatusItem> statusItems;
    private ImageSliderAdapter imageSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            final WindowInsetsController controller = getWindow().getDecorView().getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_slider);

        Toolbar toolbar = findViewById(R.id.toolbar_image);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getIntent().getExtras() != null)
            container = getIntent().getExtras().getString("container");

        preferenceManager = new PreferenceManager(this);

        //bottomSheetBehaviour();
        setUpButtons();
        setUpSlider();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpSlider() {
        statusItems = new ArrayList<>();
        int item_pos = getData();

        imageSliderAdapter = new ImageSliderAdapter(ImageSliderActivity.this, statusItems);
        viewpager = findViewById(R.id.viewpager_image_slider);

        viewpager.setAdapter(imageSliderAdapter);
        viewpager.setCurrentItem(item_pos, false);
    }

    private int getData() {
        int pos = 0;

        if (container != null && container.equalsIgnoreCase("recent")) {
            download.setVisibility(View.VISIBLE);
            delete.setVisibility(View.GONE);

            if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp)))
                getItems(MyConstants.allWAImageItems);

            else if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp_business)))
                getItems(MyConstants.allW4bImageItems);

            if (getIntent().getExtras() != null) pos = getIntent().getExtras().getInt("pos");

        } else if (container != null && container.equalsIgnoreCase("saved")) {
            delete.setVisibility(View.VISIBLE);
            download.setVisibility(View.GONE);

            String filePath = null;
            if (getIntent().getExtras() != null)
                filePath = getIntent().getExtras().getString("filePath");

            for (int i = 0; i < MyConstants.allSavedItems.size(); i++) {
                StatusItem item = MyConstants.allSavedItems.get(i);
                if (item.getFilePath().endsWith(".jpg") || item.getFilePath().endsWith(".jpeg") || item.getFilePath().endsWith(".png")) statusItems.add(item);
            }

            for (int i = 0; i < MyConstants.allSavedItems.size(); i++) {
                StatusItem item = MyConstants.allSavedItems.get(i);
                if (item.getFilePath().equals(filePath)) break;
                else if (item.getFilePath().endsWith(".jpg") || item.getFilePath().endsWith(".jpeg") || item.getFilePath().endsWith(".png")) pos += 1;
            }
        }

        return pos;
    }

    private void getItems(List<StatusItem> allImageItems) {
        for (StatusItem item : allImageItems) {
            if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                item.setDownloaded(true);
            statusItems.add(item);
        }
    }

 /*   private void bottomSheetBehaviour() {
        LinearLayout bottomSheetLayout = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        expand = findViewById(R.id.btn_expand);
        expand.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                expand.setImageResource(ic_arrow_up);

            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                expand.setImageResource(ic_arrow_down);
            }
        });
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_DRAGGING) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                expand.setImageResource(ic_arrow_up);

            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                expand.setImageResource(ic_arrow_down);
            }
        }
    }*/

    private void setUpButtons() {
        FloatingActionButton share = findViewById(R.id.fab_share);
        share.setOnClickListener(v -> {
            imageSliderAdapter.share(viewpager.getCurrentItem());
            //collapseBottomSheet();
        });

        FloatingActionButton wa_share = findViewById(R.id.fab_wa_share);
        wa_share.setOnClickListener(v -> {
            imageSliderAdapter.wa_share(viewpager.getCurrentItem());
            //collapseBottomSheet();
        });

        download = findViewById(R.id.fab_download);
        download.setOnClickListener(v -> {
            imageSliderAdapter.download(viewpager.getCurrentItem());
            //collapseBottomSheet();
        });

        delete = findViewById(R.id.fab_delete);
        delete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ImageSliderActivity.this);
            builder.setTitle("Delete")
                    .setMessage("Do you sure want to delete this status?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        imageSliderAdapter.delete(viewpager.getCurrentItem());
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();

            //collapseBottomSheet();
        });

        /*Button wallpaper = findViewById(R.id.btn_wallpaper);
        wallpaper.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ImageSliderActivity.this);
            builder.setTitle("Wallpaper")
                    .setMessage("Do you sure want to set this image as wallpaper?")
                    .setPositiveButton("No", (dialog, which) -> dialog.dismiss())
                    .setNegativeButton("Yes", (dialog, which) -> {
                        sliderAdapter.setWallpaper(viewpager.getCurrentItem());
                        dialog.dismiss();
                        goToHome();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            collapseBottomSheet();
        });*/
    }

    private void goToHome() {
        Intent goToHome = new Intent(Intent.ACTION_MAIN);
        goToHome.addCategory(Intent.CATEGORY_HOME);
        goToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(goToHome);
    }

    /*private void collapseBottomSheet() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            expand.setImageResource(ic_arrow_up);

        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            expand.setImageResource(ic_arrow_up);
        }
    }*/

    private void customToast(String sub) {
        View layoutView = LayoutInflater.from(this).inflate(R.layout.toast_layout, findViewById(R.id.ad_root_layout));
        TextView subject = layoutView.findViewById(R.id.toast_text);
        subject.setText(sub);

        Toast toast = new Toast(this);
        toast.setView(layoutView);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //collapseBottomSheet();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        /*if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            expand.setImageResource(ic_arrow_up);

        } else {
            finish();
        }*/
        finish();
    }

    @Override
    public void onClick(View v) {
        //collapseBottomSheet();
    }
}
