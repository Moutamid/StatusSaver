package com.grapplications.statussaver;

import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.grapplications.statussaver.adapter.TabAdapter;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.ConnectivityReceiver;
import com.grapplications.statussaver.utils.MyConstants;
import com.grapplications.statussaver.utils.StatusSaver;
import com.grapplications.statussaver.utils.PreferenceManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String TAG = "myMsg";

    private DrawerLayout drawerLayout;
    private ViewPager viewPager;
    private BottomSheetDialog use_bottom_sheet, feedback_bottom_sheet, exit_bottom_sheet;
    private ImageView exitImage;
    private PreferenceManager preferenceManager;
    private Dialog permissionDialog;

    private ConnectivityReceiver receiver;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private static final String APP_VERSION = "app_version";
    private static final String UPDATE_MESSAGE = "update_message";
    private static final String UPDATE_URL = "update_link";
    private static final String IS_AVAILABLE_IN_MARKET = "is_available";

    private static final String EXTERNAL_STORAGE_AUTHORITY_PROVIDER = "com.android.externalstorage.documents";
    private static final String ANDROID_DOC_ID_WA = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
    private static final String ANDROID_DOC_ID_W4B = "primary:Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses";
    private Uri uriWA, uriW4B;
    private Uri treeUriWA, treeUriW4B;

    private AdView adView;
    private TemplateView template;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyConstants.checkApp(this);
        MyConstants.checkFolder();
        setPreferences();
        setPermissionDialog();
        setIntentFilter();
        //checkConnection();
        setUpActionbar();
        setUpNavigationBar();
        setUpAppUseCard();
        setUpFab();
        setUpAppName();
        setUpAds();


        if (new File(MyConstants.SOURCE_FOLDER_WA_NEW).exists()) {
            uriWA = DocumentsContract.buildDocumentUri(
                    EXTERNAL_STORAGE_AUTHORITY_PROVIDER,
                    ANDROID_DOC_ID_WA
            );

            treeUriWA = DocumentsContract.buildTreeDocumentUri(
                    EXTERNAL_STORAGE_AUTHORITY_PROVIDER,
                    ANDROID_DOC_ID_WA
            );
        }

        if (new File(MyConstants.SOURCE_FOLDER_W4B_NEW).exists()) {
            uriW4B = DocumentsContract.buildDocumentUri(
                    EXTERNAL_STORAGE_AUTHORITY_PROVIDER,
                    ANDROID_DOC_ID_W4B
            );

            treeUriW4B = DocumentsContract.buildTreeDocumentUri(
                    EXTERNAL_STORAGE_AUTHORITY_PROVIDER,
                    ANDROID_DOC_ID_W4B
            );
        }

        if (SDK_INT >= Build.VERSION_CODES.R) {
            Toast.makeText(this, "Permission available : " + MyConstants.checkIfGotAccess(this, preferenceManager, treeUriWA, treeUriW4B), Toast.LENGTH_SHORT).show();
            if (MyConstants.checkIfGotAccess(this, preferenceManager, treeUriWA, treeUriW4B)) setUpSliderTabs();
            else {
                if (preferenceManager.getApp().equals(getString(R.string.whatsapp)) && (new File(MyConstants.SOURCE_FOLDER_WA_NEW).exists() || new File(MyConstants.SOURCE_FOLDER_WA_OLD).exists())) {
                    permissionDialog.show();
                }

                if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)) && (new File(MyConstants.SOURCE_FOLDER_W4B_NEW).exists() || new File(MyConstants.SOURCE_FOLDER_W4B_OLD).exists())) {
                    permissionDialog.show();
                }
            }

        } else setUpSliderTabs();
    }

    private void setPreferences() {
        preferenceManager = new PreferenceManager(this);
        if (!preferenceManager.isFirstEntry()) preferenceManager.createPreference();
        preferenceManager.storeItemClick(0);

        // Appear Rate dialog
        /* if (preferenceManager.setOpenCount() == 2) {
            if (ConnectivityReceiver.isConnected()) {
                if (preferenceManager.is_in_market()) {
                    new Handler().postDelayed(this::showRateDialog, 3000);
                }
            }
        } */
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void setPermissionDialog() {
        permissionDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popup_permission, findViewById(R.id.root_layout_permission));
        permissionDialog.setContentView(view);
        if (permissionDialog.getWindow() != null)
            permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        permissionDialog.setCancelable(false);

        MaterialButton allow = view.findViewById(R.id.mb_allow);
        allow.setOnClickListener(v -> {
            if (MyConstants.checkIfGotAccess(this, preferenceManager, treeUriWA, treeUriW4B)) setUpSliderTabs();
            else openDirectory();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void openDirectory() {
        if (MyConstants.checkIfGotAccess(this, preferenceManager, treeUriWA, treeUriW4B)) setUpSliderTabs();
        else {
            Intent intent = getPrimaryVolume().createOpenDocumentTreeIntent();
            if (preferenceManager.getApp().equals(getString(R.string.whatsapp)))
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriWA);

            if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)))
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriW4B);

            handleIntentActivityResult.launch(intent);
        }
    }

    ActivityResultLauncher<Intent> handleIntentActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        Uri directoryUri = result.getData().getData();
                        if (!directoryUri.toString().contains(".Statuses")) {
                            Log.d("myMsg", directoryUri.toString());
                            customToast("You didn't grant permission to the correct folder");
                            return;
                        }

                        getContentResolver().takePersistableUriPermission(directoryUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        if (MyConstants.checkIfGotAccess(this, preferenceManager, treeUriWA, treeUriW4B)) {
                            permissionDialog.dismiss();

                            if (preferenceManager.getApp().equals(getString(R.string.whatsapp)))
                                preferenceManager.setWaSavedRoute(directoryUri.toString());

                            if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)))
                                preferenceManager.setW4bSavedRoute(directoryUri.toString());

                            setUpSliderTabs();

                        } else customToast("You didn't grant permission to the correct folder");

                    }

                } else customToast("You didn't grant any permission");
            });

    @RequiresApi(api = Build.VERSION_CODES.R)
    private StorageVolume getPrimaryVolume() {
        StorageManager sm = (StorageManager) getSystemService(STORAGE_SERVICE);
        return sm.getPrimaryStorageVolume();
    }

    private void setIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new ConnectivityReceiver();
        registerReceiver(receiver, intentFilter);
        StatusSaver.getInstance().setConnectivityListener(this);
    }

    // Create Rate dialog
    /* private void showRateDialog() {
        TextView dismiss, not_now, never;
        Button rate;

        final Dialog rateDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popup_rating, findViewById(R.id.root_layout), false);
        rateDialog.setContentView(view);
        if (rateDialog.getWindow() != null) {
            rateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        rateDialog.setCancelable(true);
        rateDialog.show();

        dismiss = view.findViewById(R.id.tv_dismiss);
        dismiss.setOnClickListener(v -> rateDialog.dismiss());

        rate = view.findViewById(R.id.btn_rate);
        rate.setOnClickListener(v -> {
            openLink(preferenceManager.is_in_market(), MyConstants.GOOGLE_PLAY_DOWNLOAD_LINK + getPackageName());
            rateDialog.dismiss();
        });

        not_now = view.findViewById(R.id.tv_not_now);
        not_now.setOnClickListener(v -> rateDialog.dismiss());

        never = view.findViewById(R.id.tv_never);
        never.setOnClickListener(v -> {
            rateDialog.dismiss();
            preferenceManager.disableRateDialog();
        });
    }

    //Checking is there any network connection available or not
    private void checkConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        firebaseRemoteConfig(isConnected);
        showToast(isConnected);
    } */

    private void setUpActionbar() {
        Toolbar toolbar = findViewById(R.id.repeater_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

            if (preferenceManager.getApp().equals(getString(R.string.whatsapp)))
                getSupportActionBar().setTitle(getString(R.string.whatsapp_status));

            if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)))
                getSupportActionBar().setTitle(getString(R.string.whatsapp_business_status));
        }
    }

    //Set up all components
    private void setUpNavigationBar() {
        setUpHowToUseBottomSheet();
        setUpFeedbackBottomSheet();
        setUpExitBottomSheet();
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navBar = findViewById(R.id.nav);

        View navHeaderView = navBar.getHeaderView(0);
        TextView title = navHeaderView.findViewById(R.id.tv_title);
        title.setText(new StringBuilder().append("Status Saver ").append(currentAppVersion()));

        navBar.setItemIconTintList(null);
        navBar.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.whatsDirect) {
                Intent wd_intent = new Intent(MainActivity.this, WhatsDirectActivity.class);
                startActivity(wd_intent);
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.textRepeater) {
                Intent tr_intent = new Intent(MainActivity.this, TextRepeaterActivity.class);
                startActivity(tr_intent);
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.use) {
                use_bottom_sheet.show();
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.feedback) {
                feedback_bottom_sheet.show();
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.rate) {
                openLink(MyConstants.GOOGLE_PLAY_DOWNLOAD_LINK + getPackageName());
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.share) {
                shareApp();
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.moreApps) {
                openLink(MyConstants.GOOGLE_PLAY_DEVELOPER_LINK);
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.policy) {
                Intent openLink = new Intent(Intent.ACTION_VIEW);
                openLink.setData(Uri.parse(MyConstants.PRIVACY_POLICY));
                startActivity(openLink);
                drawerLayout.closeDrawer(GravityCompat.START);

            } else if (item.getItemId() == R.id.exit) {
                exit_bottom_sheet.show();
                drawerLayout.closeDrawer(GravityCompat.START);

            }
            return true;
        });
    }

    private void openLink(String link) {
        if (ConnectivityReceiver.isConnected()) {
            try {
                Intent openLink = new Intent(Intent.ACTION_VIEW);
                openLink.setData(Uri.parse(link));
                startActivity(openLink);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            customToast("Please connect to internet");
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String subject = getString(R.string.app_name) + " by GR Applications";
        String message = "Hi, I am using this amazing Status Saver app. " +
                "This app save all WhatsApp statuses which I have seen. " +
                "To download this app " +
                MyConstants.GOOGLE_PLAY_DOWNLOAD_LINK + getApplicationContext().getPackageName();

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(shareIntent, "Share with :"));
    }

    private void setUpFeedbackBottomSheet() {
        final LinearLayout client_layout;
        final EditText feedback;
        final Spinner emailHandlers;
        final RadioGroup radioGrp;
        final RadioButton[] radioBtn = new RadioButton[1];
        final FloatingActionButton sendBtn;

        String[] handlers = getResources().getStringArray(R.array.email_handlers);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, handlers);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        if (feedback_bottom_sheet == null) {
            final View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_feedback, findViewById(R.id.feedback_root_layout));
            feedback_bottom_sheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            feedback_bottom_sheet.setContentView(view);
            feedback_bottom_sheet.setCancelable(true);

            client_layout = view.findViewById(R.id.client_layout);
            emailHandlers = view.findViewById(R.id.email_client);
            emailHandlers.setAdapter(adapter);
            emailHandlers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (emailHandlers.getSelectedItem().toString()) {
                        case "Gmail":
                        case "Gmail Go":
                        case "Samsung Email":
                            client_layout.setBackgroundColor(Color.rgb(220, 60, 50));
                            break;

                        case "Yahoo Mail":
                            client_layout.setBackgroundColor(Color.rgb(110, 50, 235));
                            break;

                        case "Yahoo Mail Go":
                            client_layout.setBackgroundColor(Color.rgb(90, 20, 190));
                            break;

                        case "MS Outlook":
                            client_layout.setBackgroundColor(Color.rgb(40, 160, 230));
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            radioGrp = view.findViewById(R.id.radio_group);
            radioBtn[0] = view.findViewById(radioGrp.getCheckedRadioButtonId());
            radioGrp.setOnCheckedChangeListener((group, checkedId) -> radioBtn[0] = view.findViewById(checkedId));

            feedback = view.findViewById(R.id.et_feedback);
            sendBtn = view.findViewById(R.id.fab_send);
            sendBtn.setOnClickListener(v -> {
                sendFeedBack(emailHandlers.getSelectedItem().toString(),
                        String.valueOf(radioBtn[0].getText()), feedback.getText().toString().trim());
                feedback.setText("");
            });
        }
    }

    public void sendFeedBack(String client_app, String choice, String feedback) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        String subject = getResources().getString(R.string.app_name) + " " + currentAppVersion();
        String body = "Device name : " + getDeviceName()
                + "\nAndroid version : " + Build.VERSION.SDK_INT
                + "\nDisplay resolution : " + displayHeight + " x " + displayWidth + "px"
                + "\n\nFeedback :"
                + "\n\nI am " + choice + "."
                + "\n\n" + feedback;

        Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
        feedbackIntent.setType("message/rfc822");
        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{MyConstants.DEVELOPER_EMAIL});
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        feedbackIntent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            switch (client_app.toLowerCase()) {
                case "gmail":
                    feedbackIntent.setPackage("com.google.android.gm");
                    startActivity(feedbackIntent);
                    break;

                case "gmail go":
                    feedbackIntent.setPackage("com.google.android.gm.lite");
                    startActivity(feedbackIntent);
                    break;

                case "samsung email":
                    feedbackIntent.setPackage("com.samsung.android.email.provider");
                    startActivity(feedbackIntent);
                    break;

                case "yahoo mail":
                    feedbackIntent.setPackage("com.yahoo.mobile.client.android.mail");
                    startActivity(feedbackIntent);
                    break;

                case "yahoo mail go":
                    feedbackIntent.setPackage("com.yahoo.mobile.client.android.mail.lite");
                    startActivity(feedbackIntent);
                    break;

                case "ms outlook":
                    feedbackIntent.setPackage("com.microsoft.office.outlook");
                    startActivity(feedbackIntent);
                    break;
                default:
                    break;
            }
            feedback_bottom_sheet.dismiss();
        } catch (Exception ex) {
            customToast("Please change email handler!");
        }
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) return model.toUpperCase();
        else return manufacturer.toUpperCase() + " " + model;
    }

    private void setUpHowToUseBottomSheet() {
        if (use_bottom_sheet == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_how_to_use, findViewById(R.id.use_root_layout));
            use_bottom_sheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            use_bottom_sheet.setContentView(view);
            use_bottom_sheet.setCancelable(true);

            CardView openAppSettings = view.findViewById(R.id.layout_open_app_settings);
            openAppSettings.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse(String.format(Locale.getDefault(), "package:%s", getPackageName())));
                startActivity(intent);
                use_bottom_sheet.dismiss();
            });
        }
    }

    private void setUpExitBottomSheet() {
        TextView exit;
        ExtendedFloatingActionButton rate;
        RatingBar rateBar;

        if (exit_bottom_sheet == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_exit, findViewById(R.id.root_layout));
            exit_bottom_sheet = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            exit_bottom_sheet.setContentView(view);
            exit_bottom_sheet.setCancelable(true);

            rate = view.findViewById(R.id.fab_rate);
            rate.setOnClickListener(v -> openLink(MyConstants.GOOGLE_PLAY_DOWNLOAD_LINK + getPackageName()));

            rateBar = view.findViewById(R.id.rb_rating);
            rateBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                customToast(String.format(Locale.getDefault(), "Please rate us %s star here also", rating));
                openLink(MyConstants.GOOGLE_PLAY_DOWNLOAD_LINK + getPackageName());
                finish();
            });

            exit = view.findViewById(R.id.tv_exit);
            exit.setOnClickListener(v -> {
                exit_bottom_sheet.dismiss();
                finish();
            });

            exitImage = view.findViewById(R.id.exit_image);
            template = view.findViewById(R.id.my_temple);

            loadNativeAd();
        }

    }

    private void setUpSliderTabs() {
        TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager(), R.string.appbar_scrolling_view_behavior);
        tabAdapter.addFragment(this, new ImageFragment(), "", R.drawable.ic_picture);
        tabAdapter.addFragment(this, new VideoFragment(), "", R.drawable.ic_youtube_logo);

        viewPager = findViewById(R.id.viewpager_recent);
        viewPager.setAdapter(tabAdapter);
        viewPager.setCurrentItem(preferenceManager.getPagerCount());

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager, true);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {

                TextView tabTextView = new TextView(this);
                tab.setCustomView(tabTextView);

                tabTextView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                tabTextView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                tabTextView.setText(tab.getText());
                tabTextView.setTextSize(getResources().getDimension(R.dimen.tab_text_size));
                tabTextView.setTextColor(getResources().getColor(R.color.blackTransparent));

                if (i == preferenceManager.getPagerCount()) {
                    tabTextView.setTypeface(null, Typeface.BOLD);
                    tabTextView.setTextSize(getResources().getDimension(R.dimen.tab_selected_text_size));
                    tabTextView.setTextColor(getResources().getColor(R.color.black));
                }
            }

        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView text = (TextView) tab.getCustomView();
                if (text != null) {
                    text.setTypeface(null, Typeface.BOLD);
                    text.setTextSize(getResources().getDimension(R.dimen.tab_selected_text_size));
                    text.setTextColor(getResources().getColor(R.color.black));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView text = (TextView) tab.getCustomView();
                if (text != null) {
                    text.setTypeface(null, Typeface.NORMAL);
                    text.setTextSize(getResources().getDimension(R.dimen.tab_text_size));
                    text.setTextColor(getResources().getColor(R.color.blackTransparent));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        if (preferenceManager.getApp().equalsIgnoreCase(getResources().getString(R.string.whatsapp))) {
            menu.findItem(R.id.whatsapp).setChecked(true);
            menu.findItem(R.id.openApp).setIcon(R.drawable.ic_whatsapp_color);

        } else if (preferenceManager.getApp().equalsIgnoreCase(getResources().getString(R.string.whatsapp_business))) {
            menu.findItem(R.id.whatsapp4b).setChecked(true);
            menu.findItem(R.id.openApp).setIcon(R.drawable.ic_whatsapp_business);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            drawerLayout.openDrawer(GravityCompat.START);

        else if (item.getItemId() == R.id.openApp) {
            if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp)))
                openApp(getResources().getString(R.string.whatsapp), MyConstants.WhatsApp_PACKAGE);

            else if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp_business)))
                openApp(getResources().getString(R.string.whatsapp_business), MyConstants.WhatsApp4B_PACKAGE);

        } else if (item.getItemId() == R.id.whatsapp) {
            if (preferenceManager.getApp().equals(item.getTitle().toString()))
                customToast("Already selected!");

            else {
                if (getPackageManager().getLaunchIntentForPackage(MyConstants.WhatsApp_PACKAGE) != null)
                    changeApp(viewPager.getCurrentItem(), item);

                else customToast("Please download WhatsApp first!");
            }

        } else if (item.getItemId() == R.id.whatsapp4b) {
            if (preferenceManager.getApp().equals(item.getTitle().toString()))
                customToast("Already selected!");

            else {
                if (getPackageManager().getLaunchIntentForPackage(MyConstants.WhatsApp4B_PACKAGE) != null)
                    changeApp(viewPager.getCurrentItem(), item);

                else customToast("Please download WA Business first!");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void openApp(String app, String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            Toast.makeText(this, "Download " + app + " first!", Toast.LENGTH_SHORT).show();
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void changeApp(int pagerPos, MenuItem item) {
        if (viewPager != null) preferenceManager.setPagerCount(pagerPos);
        else preferenceManager.setPagerCount(0);
        preferenceManager.setApp(item.getTitle().toString());
        item.setChecked(true);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setUpAppUseCard() {
        CardView useAppCard = findViewById(R.id.layout_use_app);
        useAppCard.setOnClickListener(v -> use_bottom_sheet.show());

        ObjectAnimator cardAnim = ObjectAnimator.ofPropertyValuesHolder(useAppCard,
                PropertyValuesHolder.ofFloat("alpha", 0.5f));

        cardAnim.setDuration(2000);
        cardAnim.setRepeatCount(ValueAnimator.INFINITE);
        cardAnim.setRepeatMode(ValueAnimator.REVERSE);
        cardAnim.start();

        new CountDownTimer(18000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                cardAnim.cancel();
                useAppCard.animate().alpha(0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        useAppCard.setVisibility(View.GONE);
                    }
                });
            }
        }.start();
    }

    private void setUpFab() {
        FloatingActionButton save_fab = findViewById(R.id.save_fav);
        save_fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedActivity.class);
            startActivity(intent);
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

    private void customToast(String sub) {
        View layoutView = LayoutInflater.from(this).inflate(R.layout.toast_layout, findViewById(R.id.toast_root_layout));
        TextView subject = layoutView.findViewById(R.id.toast_text);
        subject.setText(sub);

        Toast toast = new Toast(this);
        toast.setView(layoutView);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (permissionDialog != null && !permissionDialog.isShowing() && !MyConstants.checkIfGotAccess(this, preferenceManager, treeUriWA, treeUriW4B)) {
                if (preferenceManager.getApp().equals(getString(R.string.whatsapp)) && new File(MyConstants.SOURCE_FOLDER_WA_NEW).exists())
                    permissionDialog.show();

                if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)) && new File(MyConstants.SOURCE_FOLDER_W4B_NEW).exists())
                    permissionDialog.show();
            }

        } else {
            if (!isPermissionGranted()) {
                Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
                startActivity(intent);
                finish();

            } else initFolders();
        }

    }

    private void initFolders() {
        if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp))) {
            File sourceDirectoryOld = new File(MyConstants.SOURCE_FOLDER_WA_OLD);
            getItems(sourceDirectoryOld);

        } else if (preferenceManager.getApp().equals(getResources().getString(R.string.whatsapp_business))) {
            File sourceDirectoryOld = new File(MyConstants.SOURCE_FOLDER_W4B_OLD);
            getItems(sourceDirectoryOld);
        }
    }

    private void getItems(File sourceFolder) {
        if (sourceFolder.exists()) {
            File[] sourceFiles = sourceFolder.listFiles();
            if (sourceFiles != null) getWAFiles(sourceFiles);
        }
    }

    private void getWAFiles(Object[] objects) {
        MyConstants.allWAImageItems.clear();
        MyConstants.allWAVideoItems.clear();

        MyConstants.allW4bImageItems.clear();
        MyConstants.allW4bVideoItems.clear();

        Arrays.sort(objects, (o1, o2) -> Long.compare(((File) o2).lastModified(), ((File) o1).lastModified()));

        for (File file : (File[]) objects) {
            if (!file.getPath().contains(".nomedia")) {
                if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png")) {
                    if (preferenceManager.getApp().equals(getString(R.string.whatsapp)))
                        MyConstants.allWAImageItems.add(new StatusItem(file, file.getName(), file.getAbsolutePath(), Uri.fromFile(file), file.lastModified()));

                    else if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)))
                        MyConstants.allW4bImageItems.add(new StatusItem(file, file.getName(), file.getAbsolutePath(), Uri.fromFile(file), file.lastModified()));

                } else if (file.getPath().endsWith(".mp4") || file.getPath().endsWith(".3gp")) {
                    if (preferenceManager.getApp().equals(getString(R.string.whatsapp)))
                        MyConstants.allWAVideoItems.add(new StatusItem(file, file.getName(), file.getAbsolutePath(), Uri.fromFile(file), file.lastModified()));

                    else if (preferenceManager.getApp().equals(getString(R.string.whatsapp_business)))
                        MyConstants.allW4bVideoItems.add(new StatusItem(file, file.getName(), file.getAbsolutePath(), Uri.fromFile(file), file.lastModified()));
                }
            }
        }
    }

    //Checking is there any permission available for access internal storage or not
    private boolean isPermissionGranted() {
        /*if (SDK_INT >= Build.VERSION_CODES.R)
            //Android 10 & above
            return Environment.isExternalStorageManager();

        else {
            //Below Android 10*/
        int requestExternalStoragePermission1 = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int requestExternalStoragePermission2 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);

        return requestExternalStoragePermission1 == PackageManager.PERMISSION_GRANTED
                && requestExternalStoragePermission2 == PackageManager.PERMISSION_GRANTED;
        //}
    }

    //Google Ads
    private void setUpAds() {
        MobileAds.initialize(this, initializationStatus -> {

        });
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

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                adView = null;
                loadBannerAd();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        });
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

    //Native Ad
    private void loadNativeAd() {
        AdLoader adLoader = new AdLoader.Builder(this, MyConstants.NATIVE_AD_UNIT_ID)
                .forNativeAd(nativeAd -> {
                    if (isDestroyed()) nativeAd.destroy();
                    if (exitImage != null) exitImage.setVisibility(View.GONE);

                    NativeTemplateStyle templateStyle = new NativeTemplateStyle.Builder().build();
                    template.setStyles(templateStyle);
                    template.setVisibility(View.VISIBLE);
                    template.setNativeAd(nativeAd);
                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                    }
                }).withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void onBackPressed() {
        if (permissionDialog.isShowing()) {
            permissionDialog.dismiss();
            finish();

        } else if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else if (viewPager != null && viewPager.getCurrentItem() == 1)
            viewPager.setCurrentItem(0);
        else if (feedback_bottom_sheet.isShowing()) feedback_bottom_sheet.dismiss();
        else if (use_bottom_sheet.isShowing()) use_bottom_sheet.dismiss();
        else exit_bottom_sheet.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected)
            firebaseRemoteConfig(isConnected);
        showToast(isConnected);
    }

    //Toast for connection status
    private void showToast(boolean connected) {
        if (!connected) customToast("Not connected to internet");
    }

    //FireBase remote config
    private void firebaseRemoteConfig(boolean connected) {
        if (connected) {
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600)
                    .build();
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

            Map<String, Object> firebaseParams = new HashMap<>();
            firebaseParams.put(UPDATE_MESSAGE, "");
            firebaseParams.put(APP_VERSION, "");
            firebaseParams.put(UPDATE_URL, "");
            firebaseParams.put(IS_AVAILABLE_IN_MARKET, "");

            firebaseRemoteConfig.setDefaultsAsync(firebaseParams);
            firebaseRemoteConfig.fetch(5).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    firebaseRemoteConfig.activate();
                    checkForUpdate();
                }
            });
        }
    }

    private void checkForUpdate() {
        double updateVersion = firebaseRemoteConfig.getDouble(APP_VERSION);
        String updateMessage = firebaseRemoteConfig.getString(UPDATE_MESSAGE);
        final String updateLink = firebaseRemoteConfig.getString(UPDATE_URL);

        if (currentAppVersion() < updateVersion) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update available");
            builder.setMessage(updateMessage
                    + "\n\n(Current : " + currentAppVersion() + " Latest : " + updateVersion + ")");
            builder.setPositiveButton("Update", (dialog, which) -> {
                try {
                    Intent moreApp = new Intent(Intent.ACTION_VIEW);
                    moreApp.setData(Uri.parse(updateLink));
                    startActivity(moreApp);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dialog.dismiss();
            });
            builder.setNegativeButton("Not now", (dialog, which) -> dialog.dismiss());
            AlertDialog updateDialog = builder.create();
            updateDialog.show();
        }
    }

    private double currentAppVersion() {
        try {
            return Double.parseDouble(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adView != null) adView.destroy();
       // unregisterReceiver(receiver);
        preferenceManager.clearPagerCount();
    }
}