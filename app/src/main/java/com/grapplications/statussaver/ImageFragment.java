package com.grapplications.statussaver;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.grapplications.statussaver.adapter.RecentAdapter;
import com.grapplications.statussaver.interfaces.AdClick;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;
import com.grapplications.statussaver.utils.PreferenceManager;
import com.grapplications.statussaver.viewmodel.StatusViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageFragment extends Fragment {

    private static final String TAG = "myMsg";
    private View view;
    private LinearLayout no_status_layout;
    public PreferenceManager preferenceManager;
    private ProgressBar progressBar;

    private static final String EXTERNAL_STORAGE_AUTHORITY_PROVIDER = "com.android.externalstorage.documents";
    private static final String ANDROID_DOC_ID_WA = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
    private static final String ANDROID_DOC_ID_WAB = "primary:Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses";
    private Uri uriWA, uriW4B;
    private List<StatusItem> statusItems;
    private RecentAdapter recentAdapter;
    private Intent intentNext;
    RecyclerView recyclerView;
    private RewardedInterstitialAd rewardedInterstitialAd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_image, container, false);

        preferenceManager = new PreferenceManager(requireContext());

        progressBar = view.findViewById(R.id.pb_progress);
        progressBar.setVisibility(View.GONE);

        no_status_layout = view.findViewById(R.id.no_status_layout);
        no_status_layout.setVisibility(View.VISIBLE);

        statusItems = new ArrayList<>();

        TextView appName = view.findViewById(R.id.tv_open_app);
        if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp)))
            appName.setText(getResources().getString(R.string.open_whatsapp));

        else if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp_business)))
            appName.setText(getResources().getString(R.string.open_wa_business));

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        uriWA = DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_AUTHORITY_PROVIDER, ANDROID_DOC_ID_WA);

        uriW4B = DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_AUTHORITY_PROVIDER, ANDROID_DOC_ID_WAB);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobileAds.initialize(requireContext(), initializationStatus -> loadRewardedInterstitialAd());
    }

    private void goToNextPage() {
        startActivity(intentNext);
    }

    //Rewarded Interstitial Ad
    private void loadRewardedInterstitialAd() {
        RewardedInterstitialAd.load(requireContext(), MyConstants.REWARDED_INTERSTITIAL_AD_UNIT_ID,
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

    @Override
    public void onResume() {
        super.onResume();
        if (SDK_INT >= Build.VERSION_CODES.R) {
            getStatus();
            //  new GetStatus().execute("");
        } else {
            getItems();
            // new GetItem().execute("");
        }
    }

    private void getItems() {
        statusItems.clear();
        List<StatusItem> allImageItems = new ArrayList<>();
        if (preferenceManager.getApp().equals(getString(R.string.whatsapp))) {
            allImageItems = MyConstants.allWAImageItems;
        } else if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business))) {
            allImageItems = MyConstants.allW4bImageItems;
        }
        List<StatusItem> finalAllImageItems = allImageItems;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (StatusItem item : finalAllImageItems) {
                    if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                        item.setDownloaded(true);
                    statusItems.add(item);
                    //recentAdapter.notifyItemChanged(statusItems.size() - 1);
                }

                requireActivity().runOnUiThread(() -> {
                    if (!statusItems.isEmpty()) no_status_layout.setVisibility(View.GONE);
                    else no_status_layout.setVisibility(View.VISIBLE);
                    recentAdapter = new RecentAdapter(getContext(), statusItems, new AdClick() {
                        @Override
                        public void onAdClick(Intent intent, String type) {
                            ImageFragment.this.intentNext = intent;
                            if (rewardedInterstitialAd != null && preferenceManager.getItemClick() == 1)
                                rewardedInterstitialAd.show(requireActivity(), new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

                                    }
                                });

                            else goToNextPage();
                            preferenceManager.storeItemClick(1);
                        }
                    });
                    recyclerView.setAdapter(recentAdapter);
                    recentAdapter.notifyDataSetChanged();
                    preferenceManager.setSaveAdapterCount(recentAdapter.getItemCount());
                });

            }
        }).start();
    }

    private void getStatus() {
        DocumentFile[] allDocumentFiles = getFromSdcard();
        List<StatusItem> tempItems = new ArrayList<>();

        no_status_layout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (allDocumentFiles != null) {

                    for (DocumentFile documentFile : allDocumentFiles) {
                        if (!documentFile.getUri().toString().contains(".nomedia")) {
                            if (documentFile.getUri().toString().endsWith(".jpg")
                                    || documentFile.getUri().toString().endsWith(".jpeg")
                                    || documentFile.getUri().toString().endsWith(".png")) {

                                StatusItem item = new StatusItem();
                                item.setFileUri(documentFile.getUri());
                                item.setFileName(documentFile.getName());
                                item.setTimeStamp(documentFile.lastModified());
                                Log.e(TAG, "Image Status: " + documentFile.lastModified());
                                //Log.e(TAG, "Get Status Called");

                                if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                                    item.setDownloaded(true);

                                tempItems.add(item);
                            }
                        }
                    }
                }

                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        statusItems.clear();
                        statusItems.addAll(tempItems);
                        if (SDK_INT >= Build.VERSION_CODES.N) {
                            Collections.sort(statusItems, Comparator.comparing(StatusItem::getTimeStamp));
                        }
                        Collections.reverse(statusItems);
                        if (preferenceManager.getApp().equals(getString(R.string.whatsapp))) {
                            MyConstants.allWAImageItems.clear();
                            MyConstants.allWAImageItems.addAll(statusItems);
                        }

                        if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business))) {
                            MyConstants.allW4bImageItems.clear();
                            MyConstants.allW4bImageItems.addAll(statusItems);
                        }

                        progressBar.setVisibility(View.GONE);
                        if (!statusItems.isEmpty()) {
                            no_status_layout.setVisibility(View.GONE);
                        } else {
                            no_status_layout.setVisibility(View.VISIBLE);
                        }

                        recentAdapter = new RecentAdapter(getContext(), statusItems, new AdClick() {
                            @Override
                            public void onAdClick(Intent intent, String type) {
                                ImageFragment.this.intentNext = intent;
                                if (rewardedInterstitialAd != null && preferenceManager.getItemClick() == 1)
                                    rewardedInterstitialAd.show(requireActivity(), new OnUserEarnedRewardListener() {
                                        @Override
                                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

                                        }
                                    });

                                else goToNextPage();
                                preferenceManager.storeItemClick(1);
                            }
                        });
                        recyclerView.setAdapter(recentAdapter);
                        recentAdapter.notifyDataSetChanged();
                    }
                });

            }
        }).start();


    }

/*    // For Android version 29(Q) or below
    @SuppressLint("NotifyDataSetChanged")
    private void getItems(List<StatusItem> allImageItems) {
        statusItems.clear();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            for (StatusItem item : allImageItems) {
                if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                    item.setDownloaded(true);
                statusItems.add(item);

                //recentAdapter.notifyItemChanged(statusItems.size() - 1);
            }

            requireActivity().runOnUiThread(() -> {
                if (!statusItems.isEmpty()) no_status_layout.setVisibility(View.GONE);
                else no_status_layout.setVisibility(View.VISIBLE);
                recentAdapter = new RecentAdapter(getContext(), statusItems, this);
                recyclerView.setAdapter(recentAdapter);
                recentAdapter.notifyDataSetChanged();
            });

            preferenceManager.setSaveAdapterCount(recentAdapter.getItemCount());
        });
        service.shutdown();
    }*/

// For Android version 30(R) or above
  /*  @SuppressLint({"NotifyDataSetChanged", "SuspiciousIndentation"})
    private void getStatus() {
        List<StatusItem> tempItems = new ArrayList<>();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            DocumentFile[] allDocumentFiles = getFromSdcard();
            requireActivity().runOnUiThread(() -> {
                no_status_layout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            });

            if (allDocumentFiles != null) {
                for (DocumentFile documentFile : allDocumentFiles) {
                    if (!documentFile.getUri().toString().contains(".nomedia")) {
                        if (documentFile.getUri().toString().endsWith(".jpg")
                                || documentFile.getUri().toString().endsWith(".jpeg")
                                || documentFile.getUri().toString().endsWith(".png")) {

                            StatusItem item = new StatusItem();
                            item.setFileUri(documentFile.getUri());
                            item.setFileName(documentFile.getName());

                            Log.e(TAG, "Image Status: " + documentFile.lastModified());
                            //Log.e(TAG, "Get Status Called");

                            if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                                item.setDownloaded(true);

                            tempItems.add(item);
                        }
                    }
                }
            }

            requireActivity().runOnUiThread(() -> {
                statusItems.clear();
                statusItems.addAll(tempItems);

                if (preferenceManager.getApp().equals(getString(R.string.whatsapp))) {
                    MyConstants.allWAImageItems.clear();
                    MyConstants.allWAImageItems.addAll(statusItems);
                }

                if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business))) {
                    MyConstants.allW4bImageItems.clear();
                    MyConstants.allW4bImageItems.addAll(statusItems);
                }

                progressBar.setVisibility(View.GONE);
                if (!statusItems.isEmpty()) {
                    no_status_layout.setVisibility(View.GONE);
                } else {
                    no_status_layout.setVisibility(View.VISIBLE);
                }

                recentAdapter = new RecentAdapter(getContext(), statusItems, this);
                recyclerView.setAdapter(recentAdapter);
                recentAdapter.notifyDataSetChanged();
            });
        });
        service.shutdown();

    }*/

    class GetItem extends AsyncTask<String, String, String> {
        List<StatusItem> allImageItems;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusItems.clear();
            if (preferenceManager.getApp().equals(getString(R.string.whatsapp))) {
                allImageItems = MyConstants.allWAImageItems;
            } else if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business))) {
                allImageItems = MyConstants.allW4bImageItems;
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            for (StatusItem item : allImageItems) {
                if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                    item.setDownloaded(true);
                statusItems.add(item);

                //recentAdapter.notifyItemChanged(statusItems.size() - 1);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!statusItems.isEmpty()) no_status_layout.setVisibility(View.GONE);
            else no_status_layout.setVisibility(View.VISIBLE);
            recentAdapter = new RecentAdapter(getContext(), statusItems, new AdClick() {
                @Override
                public void onAdClick(Intent intent, String type) {
                    ImageFragment.this.intentNext = intent;
                    if (rewardedInterstitialAd != null && preferenceManager.getItemClick() == 1)
                        rewardedInterstitialAd.show(requireActivity(), new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

                            }
                        });

                    else goToNextPage();
                    preferenceManager.storeItemClick(1);
                }
            });
            recyclerView.setAdapter(recentAdapter);
            recentAdapter.notifyDataSetChanged();

            preferenceManager.setSaveAdapterCount(recentAdapter.getItemCount());
        }
    }

    class GetStatus extends AsyncTask<String, String, String> {

        DocumentFile[] allDocumentFiles = getFromSdcard();
        List<StatusItem> tempItems = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            no_status_layout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (allDocumentFiles != null) {
                if (SDK_INT >= Build.VERSION_CODES.R)
                    Arrays.sort(allDocumentFiles, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));

                for (DocumentFile documentFile : allDocumentFiles) {
                    if (!documentFile.getUri().toString().contains(".nomedia")) {
                        if (documentFile.getUri().toString().endsWith(".jpg")
                                || documentFile.getUri().toString().endsWith(".jpeg")
                                || documentFile.getUri().toString().endsWith(".png")) {

                            StatusItem item = new StatusItem();
                            item.setFileUri(documentFile.getUri());
                            item.setFileName(documentFile.getName());

                            Log.e(TAG, "Image Status: " + documentFile.lastModified());
                            //Log.e(TAG, "Get Status Called");

                            if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                                item.setDownloaded(true);

                            tempItems.add(item);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            statusItems.clear();
            statusItems.addAll(tempItems);

            if (preferenceManager.getApp().equals(getString(R.string.whatsapp))) {
                MyConstants.allWAImageItems.clear();
                MyConstants.allWAImageItems.addAll(statusItems);
            }

            if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business))) {
                MyConstants.allW4bImageItems.clear();
                MyConstants.allW4bImageItems.addAll(statusItems);
            }

            progressBar.setVisibility(View.GONE);
            if (!statusItems.isEmpty()) {
                no_status_layout.setVisibility(View.GONE);
            } else {
                no_status_layout.setVisibility(View.VISIBLE);
            }

            recentAdapter = new RecentAdapter(getContext(), statusItems, new AdClick() {
                @Override
                public void onAdClick(Intent intent, String type) {
                    ImageFragment.this.intentNext = intent;
                    if (rewardedInterstitialAd != null && preferenceManager.getItemClick() == 1)
                        rewardedInterstitialAd.show(requireActivity(), new OnUserEarnedRewardListener() {
                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

                            }
                        });

                    else goToNextPage();
                    preferenceManager.storeItemClick(1);
                }
            });
            recyclerView.setAdapter(recentAdapter);
            recentAdapter.notifyDataSetChanged();
        }

    }

    private DocumentFile[] getFromSdcard() {
        String str = null;
        if (preferenceManager.getApp().equals(getString(R.string.whatsapp)))
            str = preferenceManager.getWaSavedRoute();

        if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)))
            str = preferenceManager.getW4bSavedRoute();

        DocumentFile documentFile = DocumentFile.fromTreeUri(requireContext().getApplicationContext(), Uri.parse(str));

        return (documentFile != null && documentFile.exists()) ? documentFile.listFiles() : null;
    }
}