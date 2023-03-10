package com.grapplications.statussaver.adapter;

import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.grapplications.statussaver.ImageSliderActivity;
import com.grapplications.statussaver.R;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;

import java.io.File;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final Context context;
    private final List<StatusItem> items;
    private final ImageSliderActivity sliderActivity;

    public ImageSliderAdapter(Context context, List<StatusItem> items) {
        this.context = context;
        this.items = items;
        sliderActivity = (ImageSliderActivity) context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.slider_image, parent, false);
        return new ImageViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        StatusItem statusItem = items.get(position);
        Glide.with(context).load(statusItem.getFileUri()).into(holder.fullImage);
        holder.fullImage.setOnClickListener(sliderActivity);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private final PhotoView fullImage;

        private ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            fullImage = itemView.findViewById(R.id.pv_fullImage);
        }
    }

    public void share(int position) {
        /*try {
            StatusItem statusItem = items.get(position);
            File file = new File(statusItem.getFilePath());
            Uri imageUri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");

            Log.e("myMsg", "share: " + items.get(position).getFileUri());

            if (SDK_INT >= Build.VERSION_CODES.R) intent.putExtra(Intent.EXTRA_STREAM, items.get(position).getFileUri());
            else intent.putExtra(Intent.EXTRA_STREAM, imageUri);

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            context.startActivity(Intent.createChooser(intent, "Share with"));

        } catch (Exception ex) {
            Log.e("myMsg", ex.toString());
        }*/
        StatusItem statusItem = items.get(position);
        Uri imageUri = null;
        if (statusItem.getFilePath() != null) {
            imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", statusItem.getFile());
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (SDK_INT >= Build.VERSION_CODES.R)
            intent.putExtra(Intent.EXTRA_STREAM, statusItem.getFileUri());
        else intent.putExtra(Intent.EXTRA_STREAM, imageUri);

        context.startActivity(Intent.createChooser(intent, "Share with"));
    }

    public void wa_share(int position) {
        StatusItem statusItem = items.get(position);
        Uri imageUri = null;
        if (statusItem.getFilePath() != null) {
            imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", statusItem.getFile());
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(MyConstants.WhatsApp_PACKAGE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (SDK_INT >= Build.VERSION_CODES.R)
            intent.putExtra(Intent.EXTRA_STREAM, statusItem.getFileUri());
        else intent.putExtra(Intent.EXTRA_STREAM, imageUri);

        if (context.getPackageManager().resolveActivity(intent, 0) != null)
            context.startActivity(intent);

        else {
            try {
                customToast("Download WhatsApp first!");
                Intent storeIntent = new Intent(Intent.ACTION_VIEW);
                storeIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + MyConstants.WhatsApp_PACKAGE));
                context.startActivity(storeIntent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void download(int position) {
        if (MyConstants.checkFolder()) {
            StatusItem statusItem = items.get(position);
            //File source = new File(statusItem.getFilePath());
            File target = new File(MyConstants.SAVED_FOLDER + statusItem.getFileName());

            if (target.exists()) customToast("Already saved!");
            else {
                boolean isDownload = MyConstants.copyFileInSavedDir(context, statusItem.getFileUri().toString(), statusItem.getFileName());
                if (isDownload) customToast("File saved successfully!");
                else customToast("File not save please try again!");

                /*try {
                    FileInputStream in = new FileInputStream(source);
                    FileOutputStream out = new FileOutputStream(target + "");

                    if (SDK_INT >= Build.VERSION_CODES.Q) {
                        FileUtils.copy(in, out);
                        customToast("File saved successfully!");

                    } else {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        customToast("File saved successfully!");
                        in.close();
                        out.close();
                        out.flush();

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(target));
                        context.sendBroadcast(intent);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }

        } else customToast("File not save please try again!");

    }

    public void delete(final int position) {
        final File file = new File(items.get(position).getFilePath());

        if (file.delete()) {
            items.remove(position);
            notifyItemRemoved(position);
            customToast("Status has been deleted!");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            context.sendBroadcast(intent);

        } else customToast("Status is not deleted!\nTry again");

        if (items.size() == 0) sliderActivity.finish();
    }

    private void customToast(String sub) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        TextView subject = layoutView.findViewById(R.id.toast_text);
        subject.setText(sub);

        Toast toast = new Toast(context);
        toast.setView(layoutView);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
