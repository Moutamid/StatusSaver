package com.grapplications.statussaver.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grapplications.statussaver.ImageSliderActivity;
import com.grapplications.statussaver.R;
import com.grapplications.statussaver.VideoSliderActivity;
import com.grapplications.statussaver.interfaces.AdClick;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;

import java.io.File;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {

    private final Context context;
    private final List<StatusItem> items;
    private final AdClick adClick;

    public RecentAdapter(Context context, List<StatusItem> items, AdClick adClick) {
        this.context = context;
        this.items = items;
        this.adClick = adClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_thumbnail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final StatusItem statusItem = items.get(position);

        holder.download.setVisibility(View.VISIBLE);
        holder.share.setVisibility(View.GONE);

        if (statusItem.getFileUri().toString().endsWith(".jpg")
                || statusItem.getFileUri().toString().endsWith(".jpeg")
                || statusItem.getFileUri().toString().endsWith(".png")) {
            Glide.with(context)
                    .load(statusItem.getFileUri())
                    .centerCrop()
                    .into(holder.rv_image);
            holder.rv_videoIcon.setVisibility(View.GONE);

        } else if (statusItem.getFileUri().toString().endsWith(".mp4") || statusItem.getFileUri().toString().endsWith(".3gp")) {
            Glide.with(context)
                    .load(statusItem.getFileUri())
                    .centerCrop()
                    .into(holder.rv_image);
            holder.rv_videoIcon.setVisibility(View.VISIBLE);
        }

        if (statusItem.isDownloaded()) {
            holder.rv_check.setVisibility(View.VISIBLE);
            holder.download.setVisibility(View.GONE);

        } else {
            holder.rv_check.setVisibility(View.GONE);
            holder.download.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            goToNextPage(statusItem, position);
        });

        holder.download.setOnClickListener(v -> {
            if (download(statusItem.getFileUri().toString(), statusItem.getFileName())) {
                holder.rv_check.setVisibility(View.VISIBLE);
                holder.download.setVisibility(View.GONE);
                customToast("File saved successfully!");

            } else {
                holder.rv_check.setVisibility(View.GONE);
                holder.download.setVisibility(View.VISIBLE);
                customToast("File not save please try again...");
            }
        });
    }

    private void goToNextPage(StatusItem statusItem, int position) {
        if (statusItem.getFileUri().toString().endsWith(".mp4") || statusItem.getFileUri().toString().endsWith(".3gp")) {
            Intent intent = new Intent(context, VideoSliderActivity.class);
            intent.putExtra("container", "recent");
            intent.putExtra("pos", position);
            nextPage(intent, "video");
        } else if (statusItem.getFileUri().toString().endsWith(".jpg") || statusItem.getFileUri().toString().endsWith(".jpeg")) {
            Intent intent = new Intent(context, ImageSliderActivity.class);
            intent.putExtra("container", "recent");
            intent.putExtra("filePath", statusItem.getFilePath());
            intent.putExtra("pos", position);
            nextPage(intent, "image");
        }
    }

    private void nextPage(Intent intent, String type) {
        adClick.onAdClick(intent, type);
    }

    /*private StringBuilder getDuration(Uri uri) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, uri);
        long dur = mediaPlayer.getDuration();
        long duration = TimeUnit.MILLISECONDS.toSeconds(dur);

        StringBuilder videoDuration = new StringBuilder();
        if (duration < 10) {
            videoDuration.append("00:").append("0").append(duration);

        } else if (duration > 60) {
            videoDuration.append(TimeUnit.MILLISECONDS.toMinutes(dur)).append("mins");

        } else {
            videoDuration.append("00:").append(duration);
        }

        return videoDuration;
    }*/

    private boolean download(String videoPath, String fileName) {
        boolean isDownload = false;

        if (MyConstants.checkFolder()) {
            File source = new File(videoPath);
            File target = new File(MyConstants.SAVED_FOLDER + fileName);

            if (target.exists()) customToast("Already saved!");
            else {
                /*try {
                    FileInputStream in = new FileInputStream(source);
                    FileOutputStream out = new FileOutputStream(target);

                    if (SDK_INT >= Build.VERSION_CODES.R) {
                        //MyConstants.copyFileInSavedDir(context, videoPath, fileName);
                        Log.e("myMsg", "download: " + videoPath + fileName);

                    } else if (SDK_INT >= Build.VERSION_CODES.Q) {
                        FileUtils.copy(in, out);
                        isDownload = true;

                    } else {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        in.close();
                        out.flush();
                        out.close();
                        isDownload = true;

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(target));
                        context.sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    isDownload = false;
                    Log.e("myMsg", "download (error message) : " + e.getMessage());
                }*/
                isDownload = MyConstants.copyFileInSavedDir(context, videoPath, fileName);
            }
        } else customToast("File not save please try again!");

        return isDownload;
    }

    private void customToast(String sub) {
        View toastView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        TextView subject = toastView.findViewById(R.id.toast_text);
        subject.setText(sub);

        Toast toast = new Toast(context);
        toast.setView(toastView);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView rv_image, rv_videoIcon, rv_check;
        private final ImageButton download, share;
        //private CheckBox rv_select;
        //private View rv_select_view;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            rv_image = itemView.findViewById(R.id.rv_image);
            rv_videoIcon = itemView.findViewById(R.id.rv_videoIcon);
            rv_check = itemView.findViewById(R.id.rv_check);
            download = itemView.findViewById(R.id.rv_download);
            share = itemView.findViewById(R.id.rv_share);
            //rv_select = itemView.findViewById(R.id.rv_select);
            //rv_select_view = itemView.findViewById(R.id.rv_select_view);
        }
    }
}
