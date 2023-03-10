package com.grapplications.statussaver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.button.MaterialButton;
import com.grapplications.statussaver.utils.MyConstants;

public class TextRepeaterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_text_repeater);

        setUpAd();
        setUpRepeater();
    }

    private void setUpRepeater() {
        ImageButton back;
        final EditText count, text;
        final TextView repeatedText;
        MaterialButton generate, clear, copy, share;
        ToggleButton newLine, space;

        back = findViewById(R.id.btn_back);
        count = findViewById(R.id.et_repeated_time);
        text = findViewById(R.id.et_text);
        repeatedText = findViewById(R.id.tv_repeated_text);
        generate = findViewById(R.id.btn_generate);
        clear = findViewById(R.id.btn_clear);
        copy = findViewById(R.id.btn_copy);
        share = findViewById(R.id.btn_share);

        newLine = findViewById(R.id.btn_line);
        space = findViewById(R.id.btn_space);

        back.setOnClickListener(v -> finish());

        generate.setOnClickListener(v -> {
            final long repeatTime;
            if (TextUtils.isEmpty(count.getText().toString().trim()))
                repeatTime = 0;
            else
                repeatTime = Long.parseLong(count.getText().toString().trim());

            if (TextUtils.isEmpty(count.getText().toString().trim())) {
                customToast("How many times you want to repeat?");

            } else if (repeatTime > 10000 || repeatTime <= 0) {
                customToast("Minimum repeating limit is 1 and Maximum is 10000");

            } else if (TextUtils.isEmpty(text.getText().toString().trim())) {
                customToast("Please type something to repeat");

            } else {
                StringBuilder textAfterRepeat = new StringBuilder();
                String textForRepeat = text.getText().toString();

                if (newLine.isChecked()) {
                    if (space.isChecked()) {
                        for (int i = 0; i < repeatTime; i++) {
                            textAfterRepeat.append(textForRepeat).append(" ").append("\n");
                        }
                    } else {
                        for (int i = 0; i < repeatTime; i++) {
                            textAfterRepeat.append(textForRepeat).append("\n");
                        }
                    }

                } else if (space.isChecked()) {
                    for (int i = 0; i < repeatTime; i++) {
                        textAfterRepeat.append(textForRepeat).append(" ");
                    }

                } else {
                    for (int i = 0; i < repeatTime; i++) {
                        textAfterRepeat.append(textForRepeat);
                    }
                }
                repeatedText.setText(textAfterRepeat);
            }
        });

        clear.setOnClickListener(v -> {
            repeatedText.setText("");
            text.setText("");
            count.setText("");

        });

        share.setOnClickListener(v -> {
            if (TextUtils.isEmpty(repeatedText.getText())) {
                customToast("Generate text first...");

            } else {
                customToast("Please wait...");
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, repeatedText.getText().toString());
                startActivity(Intent.createChooser(intent, "Send to"));
            }
        });

        copy.setOnClickListener(v -> {
            if (TextUtils.isEmpty(repeatedText.getText())) {
                customToast("Generate text first...");

            } else {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Text copied", repeatedText.getText().toString());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    customToast("Text copied");
                }
            }
        });
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