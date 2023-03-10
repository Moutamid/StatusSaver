package com.grapplications.statussaver;

import static android.os.Build.VERSION.SDK_INT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;

import com.grapplications.statussaver.utils.MyConstants;

import java.util.List;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String ANDROID_DOC_ID_WA = "primary:Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
    private static final String EXTERNAL_STORAGE_AUTHORITY_PROVIDER = "com.android.externalstorage.documents";
    private Uri treeUriWA;

    //private RewardedInterstitialAd rewardedInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);

        treeUriWA = DocumentsContract.buildTreeDocumentUri(
                EXTERNAL_STORAGE_AUTHORITY_PROVIDER,
                ANDROID_DOC_ID_WA
        );

        new Handler().postDelayed(this::nextActivity, 1000);
    }

    private void nextActivity() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (checkIfGotAccess()) startActivity(new Intent(this, MainActivity.class));
            else startActivity(new Intent(this, PermissionActivity.class));

        } else {
            if (MyConstants.isPermissionGranted(this)) startActivity(new Intent(this, MainActivity.class));
            else startActivity(new Intent(this, PermissionActivity.class));
        }

        finish();
    }

    private Boolean checkIfGotAccess() {
        List<UriPermission> permissionList = getContentResolver().getPersistedUriPermissions();
        for (UriPermission it : permissionList) {
            if (it.getUri().equals(treeUriWA) && it.isReadPermission())
                return true;
        }
        return false;
    }
}
