package com.grapplications.statussaver;

import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grapplications.statussaver.adapter.RecentAdapter;
import com.grapplications.statussaver.adapter.SavedAdapter;
import com.grapplications.statussaver.interfaces.AdClick;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;
import com.grapplications.statussaver.utils.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedActivity extends AppCompatActivity implements OnUserEarnedRewardListener {

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private Toolbar toolbar;
    public LinearLayout no_status_layout;

    private File savedDirectory;
    private File[] savedFiles;

    private List<StatusItem> statusItems;
    private SavedAdapter statusAdapter;

    private PreferenceManager preferenceManager;

    private boolean onPause;
    private int itemLength = 0, totalItem = 0;

    public boolean is_in_action_mode = false;
    public boolean check_all = false;
    public List<StatusItem> selected_items;
    public int counter;
    private AlertDialog dialog;

    private AdView adView;
    private RewardedInterstitialAd rewardedInterstitialAd;

    private Intent intentNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        no_status_layout = findViewById(R.id.no_status_layout);
        preferenceManager = new PreferenceManager(this);

        setUpToolbar();
        setUpAppName();
        setUpAds();
        setUpRecyclerView();
        setUpFab();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (onPause) {
            onPause = false;*/
        if (MyConstants.isPermissionGranted(this)) {
                /*savedFiles = savedDirectory.listFiles();
                if (savedFiles != null) {
                    for (File totalFile : savedFiles) {
                        if (totalFile.getPath().endsWith(".jpg") || totalFile.getPath().endsWith(".jpeg") || totalFile.getPath().endsWith(".mp4") || totalFile.getPath().endsWith(".3gp")) {
                            totalItem += 1;
                        }
                    }

                    if (totalItem == 0) no_status_layout.setVisibility(View.VISIBLE);

                    if (itemLength != totalItem) {
                        if (is_in_action_mode) {
                            actionModeDisabled();
                            if (dialog != null)
                                dialog.dismiss();
                        }
*/
            getStatus();
/*
                    }
                    totalItem = 0;
                } else no_status_layout.setVisibility(View.VISIBLE);*/

        } else {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
        }
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPause = true;
        itemLength = statusItems.size();
    }

    //Set up all components
    private void setUpToolbar() {
        toolbar = findViewById(R.id.save_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Saved Status");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.saved_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(SavedActivity.this, 3));

        statusItems = new ArrayList<>();
        statusAdapter = new SavedAdapter(SavedActivity.this, statusItems, (intent, type) -> {
            this.intentNext = intent;
            if (rewardedInterstitialAd != null && preferenceManager.getItemClick() == 1)
                rewardedInterstitialAd.show(SavedActivity.this, SavedActivity.this);

            else goToNextPage();
            preferenceManager.storeItemClick(1);
        });
        recyclerView.setAdapter(statusAdapter);
    }

    private void goToNextPage() {
        startActivity(intentNext);
    }

    private void setUpFab() {
        FloatingActionButton wa_fab = findViewById(R.id.wa_fab);
        wa_fab.setOnClickListener(v -> {
            Intent intent = getPackageManager().getLaunchIntentForPackage(MyConstants.WhatsApp_PACKAGE);
            if (intent != null) {
                if (getPackageManager().resolveActivity(intent, 0) != null) {
                    startActivity(intent);
                } else {
                    try {
                        customToast("Download WhatsApp first!");
                        Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW);
                        whatsAppIntent.setData(Uri.parse(MyConstants.GOOGLE_PLAY_DOWNLOAD_LINK + MyConstants.WhatsApp_PACKAGE));
                        startActivity(whatsAppIntent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void setUpAppName() {
        TextView appName = findViewById(R.id.tv_app_name);

        ObjectAnimator cardAnim = ObjectAnimator.ofPropertyValuesHolder(appName,
                PropertyValuesHolder.ofFloat("alpha", 0.1f));

        cardAnim.setDuration(1500);
        cardAnim.setRepeatCount(ValueAnimator.INFINITE);
        cardAnim.setRepeatMode(ValueAnimator.REVERSE);
        cardAnim.start();
    }

    private void getStatus() {
        savedDirectory = new File(MyConstants.SAVED_FOLDER);

        if (savedDirectory.exists()) {
            savedFiles = savedDirectory.listFiles();

            if (savedFiles == null) no_status_layout.setVisibility(View.VISIBLE);
            else getItems(savedFiles);

        } else no_status_layout.setVisibility(View.VISIBLE);
    }

    private void getItems(File[] savedFiles) {
        statusItems.clear();
        MyConstants.allSavedItems.clear();


        new Thread(new Runnable() {
            @Override
            public void run() {

                for (File sourceFile : savedFiles) {
                    if (sourceFile.getPath().endsWith(".jpg") || sourceFile.getPath().endsWith(".jpeg") || sourceFile.getPath().endsWith(".png") || sourceFile.getPath().endsWith(".mp4") || sourceFile.getPath().endsWith(".3gp")) {
                        StatusItem item = new StatusItem(sourceFile, sourceFile.getName(), sourceFile.getAbsolutePath(), Uri.fromFile(sourceFile), sourceFile.lastModified());
                        statusItems.add(item);
                        MyConstants.allSavedItems.add(item);

                    }
                }

                if (SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(statusItems, Comparator.comparing(StatusItem::getTimeStamp));
                }
                Collections.reverse(statusItems);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        statusAdapter.notifyItemChanged(statusItems.size() - 1);
                        no_status_layout.setVisibility(View.GONE);

                        if (preferenceManager.getSaveAdapterCount() > statusAdapter.getItemCount())
                            for (int i = preferenceManager.getSaveAdapterCount(); i >= statusAdapter.getItemCount(); i--)
                                statusAdapter.notifyItemRemoved(i);

                        preferenceManager.setSaveAdapterCount(statusAdapter.getItemCount());
                    }
                });

            }
        }).start();

        /*ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            Arrays.sort(savedFiles, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));

            for (File sourceFile : savedFiles) {
                if (sourceFile.getPath().endsWith(".jpg") || sourceFile.getPath().endsWith(".jpeg") || sourceFile.getPath().endsWith(".png") || sourceFile.getPath().endsWith(".mp4") || sourceFile.getPath().endsWith(".3gp")) {
                    StatusItem item = new StatusItem(sourceFile, sourceFile.getName(), sourceFile.getAbsolutePath(), Uri.fromFile(sourceFile), sourceFile.lastModified());
                    statusItems.add(item);
                    MyConstants.allSavedItems.add(item);
                    statusAdapter.notifyItemChanged(statusItems.size() - 1);
                    no_status_layout.setVisibility(View.GONE);
                }
            }

            if (preferenceManager.getSaveAdapterCount() > statusAdapter.getItemCount())
                for (int i = preferenceManager.getSaveAdapterCount(); i >= statusAdapter.getItemCount(); i--)
                    statusAdapter.notifyItemRemoved(i);

            preferenceManager.setSaveAdapterCount(statusAdapter.getItemCount());
        });
        service.shutdown();*/
    }

    /*public void actionModeEnabled() {
        selected_items = new ArrayList<>();
        is_in_action_mode = true;
        toolbar.inflateMenu(R.menu.menu_saved_contex);
        toolbar.setTitle("0 selected");
    }

    public void actionModeDisabled() {
        counter = 0;
        is_in_action_mode = false;
        check_all = false;
        toolbar.getMenu().clear();
        toolbar.setTitle("Saved Status");

        for (int pos = 0; pos < statusItems.size(); pos++)
            statusAdapter.notifyItemChanged(pos);
    }

    public void addItem(int pos) {
        if (selected_items.contains(statusItems.get(pos))) {
            selected_items.remove(statusItems.get(pos));
            counter = counter - 1;

        } else {
            selected_items.add(statusItems.get(pos));
            counter = counter + 1;
        }
        updateCounter(counter);
    }

    private void updateCounter(int counter) {
        if (counter == statusItems.size()) {
            toolbar.setTitle(counter + " selected");
            check_all = true;

        } else if (counter == 0) {
            toolbar.setTitle("0 selected");
            check_all = false;

        } else toolbar.setTitle(counter + " selected");
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            /*if (is_in_action_mode) actionModeDisabled();
            else*/
            finish();

        /*} else if (item.getItemId() == R.id.check_all) {
            if (check_all) {
                for (int pos = 0; pos < statusItems.size(); pos++)
                    statusAdapter.notifyItemChanged(pos);

                check_all = false;
                selected_items.clear();
                counter = 0;
                updateCounter(counter);

            } else {
                check_all = true;
                for (int pos = 0; pos < statusItems.size(); pos++) {
                    if (!selected_items.contains(statusItems.get(pos))) {
                        selected_items.add(statusItems.get(pos));
                        counter = counter + 1;
                        updateCounter(counter);
                    }
                    statusAdapter.notifyItemChanged(pos);
                }
            }

        } else if (item.getItemId() == R.id.share) {
            if (selected_items.size() == 0) {
                customToast("Please select item first");

            } else {
                ArrayList<Uri> fileUri = new ArrayList<>();
                for (int i = 0; i < selected_items.size(); i++) {
                    File path = new File(selected_items.get(i).getFilePath());
                    fileUri.add(FileProvider.getUriForFile(this,
                            getApplicationContext().getPackageName() + ".provider", path));
                }

                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("* /*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Share with"));
                actionModeDisabled();
            }

        } else if (item.getItemId() == R.id.delete) {
            if (selected_items.size() == 0) {
                customToast("Please select item first");

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete");
                builder.setMessage("Do you want to delete items that you have selected, remember you never get back these items after delete?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    statusAdapter.deleteItems(selected_items);
                    actionModeDisabled();
                    dialog.dismiss();
                });
                builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                dialog = builder.create();
                dialog.show();
            }*/
        }
        return true;
    }

    //Google Ads
    private void setUpAds() {
        MobileAds.initialize(this, initializationStatus -> loadRewardedInterstitialAd());
        loadBannerAd();
    }

    //Banner Ad
    private void loadBannerAd() {
        FrameLayout adViewContainer = findViewById(R.id.adViewContainer);
        adView = new AdView(this);
        adView.setAdUnitId(MyConstants.BANNER_AD_UNIT_ID);
        adViewContainer.addView(adView);
        adView.setAdSize(getAdSize());
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixel = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixel / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    //Rewarded Interstitial Ad
    private void loadRewardedInterstitialAd() {
        RewardedInterstitialAd.load(this, MyConstants.REWARDED_INTERSTITIAL_AD_UNIT_ID,
                new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedAd) {
                        rewardedInterstitialAd = rewardedAd;
                        rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent();
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                rewardedInterstitialAd = null;
                                loadRewardedInterstitialAd();
                                goToNextPage();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        rewardedInterstitialAd = null;
                    }
                });
    }

    private void customToast(String sub) {
        View toastView = LayoutInflater.from(this).inflate(R.layout.toast_layout, findViewById(R.id.ad_root_layout));
        TextView subject = toastView.findViewById(R.id.toast_text);
        subject.setText(sub);

        Toast toast = new Toast(this);
        toast.setView(toastView);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        /*if (is_in_action_mode) {
            actionModeDisabled();
        } else {*/
        super.onBackPressed();
        //}
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) adView.destroy();
    }
}