package com.grapplications.statussaver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.button.MaterialButton;
import com.grapplications.statussaver.utils.MyConstants;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WhatsDirectActivity extends AppCompatActivity {

    private static final String countryCode = "91";

    private CountryCodePicker ccp;
    private TextView numberHolder;
    private String input;

    private ClipboardManager clipboardManager;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_whats_direct);

        setUpAd();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        input = "";
        numberHolder = findViewById(R.id.et_number);
        ccp = findViewById(R.id.countryCodePicker);

        setUpButton();

        if (savedInstanceState != null) {
            input = savedInstanceState.getString("number");
            numberHolder.setText(input);
            ccp.setCountryForNameCode(savedInstanceState.getString("code"));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("number", numberHolder.getText().toString().trim());
        outState.putString("code", ccp.getSelectedCountryNameCode());
    }

    private void setUpButton() {
        MaterialButton one, two, three, four, five, six, seven, eight, nine, zero;
        ImageButton backspace, openChat, back;

        one = findViewById(R.id.btn_one);
        two = findViewById(R.id.btn_two);
        three = findViewById(R.id.btn_three);
        four = findViewById(R.id.btn_four);
        five = findViewById(R.id.btn_five);
        six = findViewById(R.id.btn_six);
        seven = findViewById(R.id.btn_seven);
        eight = findViewById(R.id.btn_eight);
        nine = findViewById(R.id.btn_nine);
        zero = findViewById(R.id.btn_zero);

        zero.setOnClickListener(this::handleNumberButtonClick);
        one.setOnClickListener(this::handleNumberButtonClick);
        two.setOnClickListener(this::handleNumberButtonClick);
        three.setOnClickListener(this::handleNumberButtonClick);
        four.setOnClickListener(this::handleNumberButtonClick);
        five.setOnClickListener(this::handleNumberButtonClick);
        six.setOnClickListener(this::handleNumberButtonClick);
        seven.setOnClickListener(this::handleNumberButtonClick);
        eight.setOnClickListener(this::handleNumberButtonClick);
        nine.setOnClickListener(this::handleNumberButtonClick);

        ImageButton paste = findViewById(R.id.btn_paste);
        backspace = findViewById(R.id.btn_backspace);
        openChat = findViewById(R.id.btn_open);

        //Paste button setup
        paste.setOnClickListener(v -> {
            if (clipboardManager.hasPrimaryClip()) {
                ClipData clipData = clipboardManager.getPrimaryClip();
                if (clipData != null) {
                    ClipData.Item clipItem = clipData.getItemAt(0);

                    String pasteData = clipItem.getText().toString();

                    StringBuilder data = new StringBuilder();
                    //Extracting only numeric values from clip data
                    for (Character c : pasteData.toCharArray()) {
                        if (Character.isDigit(c)) {
                            data.append(Character.getNumericValue(c));
                        }
                    }

                    //Checking extracted numeric data
                    if (data.toString().startsWith(ccp.getSelectedCountryCode())) {
                        if ((ccp.getSelectedCountryCode().equals(countryCode)) && data.toString().length() > 12) {
                            String value = data.substring(ccp.getSelectedCountryCode().length(), ccp.getSelectedCountryCode().length() + 10);
                            Log.e("msg", "Log 1: " + value);
                            if (checkNumber(value)) setNumber(value);
                            else customToast("Not a valid number");

                        } else {
                            String value = data.substring(ccp.getSelectedCountryCode().length());
                            Log.e("msg", "Log 2: " + value);
                            if ((ccp.getSelectedCountryCode().equals(countryCode)) && value.length() < 10) {
                                value = data.toString();
                                Log.e("msg", "Log 3: " + value);

                            } else {
                                Log.e("msg", "Log 4: " + value);

                            }
                            if (checkNumber(value)) setNumber(value);
                            else customToast("Not a valid number");

                        }

                    } else {
                        if (checkNumber(data.toString())) setNumber(data.toString());
                        else customToast("Not a valid number");
                    }
                }
            }

            vibrate();
        });

        //Backspace & Open Chat button setup
        backspace.setOnClickListener(v -> {
            if (input.length() > 0) {
                input = input.substring(0, (input.length() - 1));
                numberHolder.setText(input);
            }
            vibrate();
        });
        backspace.setOnLongClickListener(v -> {
            input = "";
            numberHolder.setText(input);
            vibrate();
            return true;
        });
        openChat.setOnClickListener(v -> {
            vibrate();
            setUpOpenChat();
        });

        //Back button setup
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(v -> {
            finish();
            vibrate();
        });
    }

    private boolean checkNumber(String number) {
        EditText editText = new EditText(this);
        editText.setText(number);
        ccp.registerCarrierNumberEditText(editText);
        return ccp.isValidFullNumber();
    }

    private void setNumber(String number) {
        numberHolder.setText(number);
        input = numberHolder.getText().toString().trim();
        vibrate();
    }

    private void setUpOpenChat() {
        String code = ccp.getSelectedCountryCode();
        String wa_number = numberHolder.getText().toString().trim();

        if (TextUtils.isEmpty(numberHolder.getText())) {
            customToast("Please enter any WhatsApp number");

        } else {
            AtomicBoolean availableApp = new AtomicBoolean(false);
            List<Intent> targetedShareIntents = new ArrayList<>();

            List<PackageInfo> packageInfoList = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);

            for (PackageInfo packageInfo : packageInfoList) {
                Intent targetedShare = new Intent(Intent.ACTION_VIEW);
                targetedShare.setData(Uri.parse("https://wa.me/" + code + wa_number));

                if (packageInfo.packageName.equalsIgnoreCase("com.whatsapp") || packageInfo.packageName.equalsIgnoreCase("com.whatsapp.w4b")) {
                    targetedShare.setPackage(packageInfo.packageName);

                    if (getPackageManager().resolveActivity(targetedShare, 0) != null) {
                        targetedShareIntents.add(targetedShare);
                        availableApp.set(true);
                    }
                }
            }

            if (availableApp.get()) {
                Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Choose app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
                Log.e("errorMsg", "setUpOpenChat: " + targetedShareIntents.get(0));

            } else customToast("No WhatsApp installed in your device!");
        }

    }

    private void handleNumberButtonClick(View v) {
        Button button = (Button) v;
        switch (button.getText().toString()) {
            case "0":
                if (input.length() < 13) {
                    input += "0";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "1":
                if (input.length() < 13) {
                    input += "1";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "2":
                if (input.length() < 13) {
                    input += "2";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "3":
                if (input.length() < 13) {
                    input += "3";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "4":
                if (input.length() < 13) {
                    input += "4";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "5":
                if (input.length() < 13) {
                    input += "5";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "6":
                if (input.length() < 13) {
                    input += "6";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "7":
                if (input.length() < 13) {
                    input += "7";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "8":
                if (input.length() < 13) {
                    input += "8";
                    numberHolder.setText(input);
                }
                vibrate();
                break;

            case "9":
                if (input.length() < 13) {
                    input += "9";
                    numberHolder.setText(input);
                }
                vibrate();
                break;
        }
    }

    private void vibrate() {
        if (vibrator != null) {
            vibrator.vibrate(50);
        }
    }

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

    private void setUpAd() {
        MobileAds.initialize(this, initializationStatus -> {

        });
        TemplateView template = findViewById(R.id.my_template);

        AdLoader adLoader = new AdLoader.Builder(this, MyConstants.NATIVE_AD_UNIT_ID)
                .forNativeAd(nativeAd -> {
                    if (isDestroyed()) nativeAd.destroy();

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
}