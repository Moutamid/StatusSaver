package com.grapplications.statussaver.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grapplications.statussaver.ImageSliderActivity;
import com.grapplications.statussaver.R;
import com.grapplications.statussaver.SavedActivity;
import com.grapplications.statussaver.VideoSliderActivity;
import com.grapplications.statussaver.interfaces.AdClick;
import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;

import java.io.File;
import java.util.List;

public class SavedAdapter extends RecyclerView.Adapter<SavedAdapter.SavedViewHolder> {

    private final Context context;
    private final List<StatusItem> items;
    private final SavedActivity savedActivity;
    private final AdClick adClick;

    public SavedAdapter(Context context, List<StatusItem> items, AdClick adClick) {
        this.context = context;
        this.items = items;
        this.adClick = adClick;
        savedActivity = (SavedActivity) context;
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_thumbnail, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SavedViewHolder holder, final int position) {
        final StatusItem statusItem = items.get(position);
        holder.share.setVisibility(View.VISIBLE);
        if (statusItem.getFilePath().endsWith(".jpg")) {
            Glide.with(context)
                    .load(statusItem.getFileUri())
                    .centerCrop()
                    .into(holder.imageView);
            holder.videoIcon.setVisibility(View.GONE);

        } else if (statusItem.getFilePath().endsWith(".mp4") || statusItem.getFilePath().endsWith(".3gp")) {
            Glide.with(context)
                    .load(statusItem.getFileUri())
                    .centerCrop()
                    .into(holder.imageView);
            holder.videoIcon.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            /*if (savedActivity.is_in_action_mode) {
                if (holder.is_selected) {
                    holder.select_view.setVisibility(View.GONE);
                    holder.is_selected = false;

                } else {
                    holder.select_view.setVisibility(View.VISIBLE);
                    holder.is_selected = true;
                }
                //savedActivity.addItem(position);

            } else {*/

            if (statusItem.getFilePath().endsWith(".mp4") || statusItem.getFilePath().endsWith(".3gp")) {
                Intent intent = new Intent(context, VideoSliderActivity.class);
                intent.putExtra("container", "saved");
                intent.putExtra("filePath", statusItem.getFilePath());
                intent.putExtra("pos", position);
                nextPage(intent, "video");

            } else if (statusItem.getFilePath().endsWith(".jpg") || statusItem.getFilePath().endsWith(".jpeg") || statusItem.getFilePath().endsWith(".png")) {
                Intent intent = new Intent(context, ImageSliderActivity.class);
                intent.putExtra("container", "saved");
                intent.putExtra("filePath", statusItem.getFilePath());
                intent.putExtra("pos", 0);
                nextPage(intent, "image");
            }
            //}
        });

        /*holder.itemView.setOnLongClickListener(v -> {
            if (savedActivity.is_in_action_mode) {
                if (holder.is_selected) {
                    holder.select_view.setVisibility(View.GONE);
                    holder.is_selected = false;

                } else {
                    holder.select_view.setVisibility(View.VISIBLE);
                    holder.is_selected = true;
                }
                savedActivity.addItem(position);

            } else {
                savedActivity.actionModeEnabled();
            }
            return true;
        });

        if (savedActivity.is_in_action_mode) {
            holder.layout_button.setVisibility(View.GONE);
            holder.share.setVisibility(View.GONE);

        } else {
            holder.layout_button.setVisibility(View.VISIBLE);
            holder.select_view.setVisibility(View.GONE);
            holder.is_selected = false;
        }

        if (savedActivity.check_all) {
            holder.select_view.setVisibility(View.VISIBLE);
            holder.is_selected = true;

        } else {
            holder.select_view.setVisibility(View.GONE);
            holder.is_selected = false;
        }*/

        holder.share.setOnClickListener(v -> share(statusItem));
    }

    private void nextPage(Intent intent, String type) {
        adClick.onAdClick(intent, type);

    }

    /*public void deleteItems(List<StatusItem> selected_items) {
        boolean is_deleted = false;
        for (int i =0; i<selected_items.size(); i++) {
            StatusItem item = selected_items.get(i);
            File file = new File(item.getFilePath());
            if (file.delete()) {
                items.remove(item);
                notifyItemRemoved(i);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                context.sendBroadcast(intent);
                is_deleted = true;
            }
        }

        if (is_deleted) {
            customToast("Selected items deleted!");
            if (items.size() == 0) savedActivity.no_status_layout.setVisibility(View.VISIBLE);
        }
    }*/

    private void share(StatusItem statusItem) {
        File file = new File(statusItem.getFilePath());
        Uri fileUri = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider", file);

        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (statusItem.getFilePath().endsWith(".jpg")) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);

            } else if (statusItem.getFilePath().endsWith(".mp4")) {
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            }

            if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                context.startActivity(Intent.createChooser(intent, "Share with"));

            } else {
                try {
                    customToast("Download WhatsApp first!");
                    Intent storeIntent = new Intent(Intent.ACTION_VIEW);
                    storeIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + MyConstants.WhatsApp_PACKAGE));
                    context.startActivity(storeIntent);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            customToast(ex.toString());
        }
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

    static class SavedViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView, videoIcon;
        private final ImageButton share;
        private final LinearLayout layout_button;
        private final View select_view;
        private boolean is_selected = false;

        SavedViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.rv_image);
            videoIcon = itemView.findViewById(R.id.rv_videoIcon);
            share = itemView.findViewById(R.id.rv_share);
            layout_button = itemView.findViewById(R.id.layout_button);
            select_view = itemView.findViewById(R.id.rv_select_view);
        }
    }
}
