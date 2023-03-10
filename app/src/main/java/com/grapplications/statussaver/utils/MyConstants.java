package com.grapplications.statussaver.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.grapplications.statussaver.R;
import com.grapplications.statussaver.model.StatusItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MyConstants {
    public static final int PERMISSION_CODE = 1001;
    public static final String SOURCE_FOLDER_WA_OLD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/.Statuses";
    public static final String SOURCE_FOLDER_WA_NEW = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
    public static final String SOURCE_FOLDER_W4B_OLD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp Business/Media/.Statuses";
    public static final String SOURCE_FOLDER_W4B_NEW = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses";
    public static final String SAVED_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/GR Status Saver/WhatsApp/";
    public static final String WA_TREE_FULL = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses";
    public static final String W4B_TREE_FULL = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp Business%2FMedia%2F.Statuses/document/primary%3AAndroid%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp Business%2FMedia%2F.Statuses";

    public static final String WhatsApp_PACKAGE = "com.whatsapp";
    public static final String WhatsApp4B_PACKAGE = "com.whatsapp.w4b";
    public static final String GOOGLE_PLAY_DOWNLOAD_LINK = "https://play.google.com/store/apps/details?id=";
    public static final String GOOGLE_PLAY_DEVELOPER_LINK = "https://play.google.com/store/apps/developer?id=Rider+Apps";
    public static final String GOOGLE_DRIVE_DOWNLOAD_LINK = "https://drive.google.com/open?id=1-7pYW5_7jSs_X82FZsYEZsptg7ucuT52";
    public static final String DATA_FETCH_LINK = "https://drive.google.com/open?id=17HU9QjKpENjX3IMKyk5eReS_7YAB26yr";
    public static final String PRIVACY_POLICY = "https://riderapps39.blogspot.com/2020/05/privacy-policy.html";
    public static final String DEVELOPER_EMAIL = "gadgetrider2@gmail.com";

    public static final String APP_OPEN_AD_UNIT_ID = "ca-app-pub-3689467985437081/5850968578";
    public static final String BANNER_AD_UNIT_ID = "ca-app-pub-3689467985437081/8877660317";
    public static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3689467985437081/7564578644";
    public static final String REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3689467985437081/6544843384";
    public static final String NATIVE_AD_UNIT_ID = "ca-app-pub-3689467985437081/6059925283";
    public static final String REWARDED_VIDEO_AD_UNIT_ID = "ca-app-pub-3689467985437081/6251496970";

    public static List<StatusItem> allWAImageItems = new ArrayList<>();
    public static List<StatusItem> allWAVideoItems = new ArrayList<>();
    public static List<StatusItem> allW4bImageItems = new ArrayList<>();
    public static List<StatusItem> allW4bVideoItems = new ArrayList<>();
    public static List<StatusItem> allSavedItems = new ArrayList<>();

    public static String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_33 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
    };

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storage_permissions_33;
            Log.e("permission", "permissions: Passed");
        } else {
            p = storage_permissions;
        }
        return p;
    }

    public static boolean isPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int requestPhotoPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES);
            int requestVideoPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO);

            return requestPhotoPermission == PackageManager.PERMISSION_GRANTED
                    && requestVideoPermission == PackageManager.PERMISSION_GRANTED;

        } else {
            int requestExternalStoragePermission1 = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int requestExternalStoragePermission2 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            return requestExternalStoragePermission1 == PackageManager.PERMISSION_GRANTED
                    && requestExternalStoragePermission2 == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void getStoragePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, permissions(), MyConstants.PERMISSION_CODE);
    }

    public static Boolean checkIfGotAccess(Context context, PreferenceManager preferenceManager, Uri treeUriWA, Uri treeUriW4B) {
        List<UriPermission> permissionList = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission it : permissionList) {
            if (preferenceManager.getApp().equals(context.getString(R.string.whatsapp)))
                if (it.getUri().equals(treeUriWA) && it.isReadPermission())
                    return true;

            if (preferenceManager.getApp().equals(context.getString(R.string.whatsapp_business)))
                if (it.getUri().equals(treeUriW4B) && it.isReadPermission())
                    return true;
        }

        return false;
    }

    public static boolean copyFileInSavedDir(Context context, String path, String name) {
        String string = SAVED_FOLDER + name;
        Uri uri = Uri.fromFile(new File(string));
        try {
            Uri uri1 = Uri.parse(path);
            InputStream inputStream = context.getContentResolver().openInputStream(uri1);
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri, "w");
            try {
                byte[] arrayOfByte = new byte[1024];
                while (true) {
                    int i = inputStream.read(arrayOfByte);
                    if (i > 0) {
                        outputStream.write(arrayOfByte, 0, i);
                        continue;
                    }
                    inputStream.close();
                    outputStream.flush();
                    outputStream.close();

                    Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                    intent.setData(uri);
                    context.sendBroadcast(intent);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkFolder() {
        File dirs = new File(SAVED_FOLDER);
        if (dirs.exists()) return true;
        else if (!dirs.mkdirs()) {
            dirs.mkdirs();
            return true;

        } else return false;
    }


    public static void checkApp(Activity activity) {
        String appName = "StatusSaver"; //TODO: CHANGE APP NAME

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }


}
