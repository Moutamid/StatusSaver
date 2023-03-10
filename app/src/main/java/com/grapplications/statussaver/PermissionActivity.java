package com.grapplications.statussaver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.grapplications.statussaver.utils.MyConstants;
import com.grapplications.statussaver.utils.PreferenceManager;

public class PermissionActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        preferenceManager = new PreferenceManager(this);

        if (MyConstants.isPermissionGranted(this)) nextActivity();

        ImageView close = findViewById(R.id.iv_close);
        close.setOnClickListener(v -> onBackPressed());

        MaterialButton permission = findViewById(R.id.mb_permission);
        permission.setOnClickListener(v -> {
            if (MyConstants.isPermissionGranted(this)) nextActivity();
            else MyConstants.getStoragePermission(this);
        });

        TextView privacy = findViewById(R.id.tv_privacy);
        privacy.setOnClickListener(v -> {
            Intent openLink = new Intent(Intent.ACTION_VIEW);
            openLink.setData(Uri.parse(MyConstants.PRIVACY_POLICY));
            startActivity(openLink);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MyConstants.PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                Log.e("myMsg", "onRequestPermissionsResult: " + READ_EXTERNAL_STORAGE + "\n" + WRITE_EXTERNAL_STORAGE);

                if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) firstOpen();
                else customToast("Please allow the permission");
            }
        }
    }

    private void firstOpen() {
        preferenceManager.createPreference();
        nextActivity();
        customToast("Welcome to Status Saver");
    }

    private void nextActivity() {
        Intent intent = new Intent(PermissionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
}