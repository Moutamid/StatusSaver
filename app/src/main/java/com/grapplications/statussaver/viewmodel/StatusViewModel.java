package com.grapplications.statussaver.viewmodel;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.grapplications.statussaver.model.StatusItem;
import com.grapplications.statussaver.utils.MyConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatusViewModel extends ViewModel {
    public LiveData<List<StatusItem>> getMutableLiveData(DocumentFile[] documentFiles) {
        MutableLiveData<List<StatusItem>> data = new MutableLiveData<>();
        List<StatusItem> itemList = new ArrayList<>();

        if (documentFiles != null) {
            Arrays.sort(documentFiles, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));

            for (DocumentFile documentFile : documentFiles) {
                if (!documentFile.getUri().toString().contains(".nomedia")) {
                    StatusItem item = new StatusItem();
                    item.setFileUri(documentFile.getUri());
                    item.setFileName(documentFile.getName());

                    if (new File(MyConstants.SAVED_FOLDER, item.getFileName()).exists())
                        item.setDownloaded(true);

                    itemList.add(item);
                }
            }

            data.setValue(itemList);
        }

        return data;
    }
}
