/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.floens.chan.ui.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import org.floens.chan.R;
import org.floens.chan.controller.Controller;
import org.floens.chan.core.model.Loadable;
import org.floens.chan.core.model.PostImage;
import org.floens.chan.core.saver.ImageSaveTask;
import org.floens.chan.core.saver.ImageSaver;
import org.floens.chan.ui.theme.ThemeHelper;
import org.floens.chan.ui.toolbar.ToolbarMenu;
import org.floens.chan.ui.toolbar.ToolbarMenuItem;
import org.floens.chan.ui.view.FloatingMenuItem;
import org.floens.chan.ui.view.GridRecyclerView;
import org.floens.chan.ui.view.PostImageThumbnailView;
import org.floens.chan.utils.RecyclerUtils;

import java.util.ArrayList;
import java.util.List;

import static org.floens.chan.ui.theme.ThemeHelper.theme;
import static org.floens.chan.utils.AndroidUtils.dp;

public class AlbumDownloadController extends Controller implements ToolbarMenuItem.ToolbarMenuItemCallback, View.OnClickListener {
    private static final int CHECK_ALL = 1;

    private GridRecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private FloatingActionButton download;

    private List<AlbumDownloadItem> items = new ArrayList<>();
    private Loadable loadable;

    private boolean allChecked = true;
    private AlbumAdapter adapter;
    private ImageSaver imageSaver;

    public AlbumDownloadController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        imageSaver = ImageSaver.getInstance();

        view = inflateRes(R.layout.controller_album_download);

        updateTitle();

        navigationItem.menu = new ToolbarMenu(context);
        navigationItem.menu.addItem(new ToolbarMenuItem(context, this, CHECK_ALL, R.drawable.ic_select_all_white_24dp));

        download = (FloatingActionButton) view.findViewById(R.id.download);
        download.setOnClickListener(this);
        theme().applyFabColor(download);
        recyclerView = (GridRecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        gridLayoutManager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setSpanWidth(dp(90));

        adapter = new AlbumAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        if (v == download) {
            int checkCount = getCheckCount();
            if (checkCount == 0) {
                new AlertDialog.Builder(context)
                        .setMessage(R.string.album_download_none_checked)
                        .setPositiveButton(R.string.ok, null)
                        .show();
            } else {
                final String folderForAlbum = imageSaver.getSubFolder(loadable.title);

                String message = context.getString(R.string.album_download_confirm,
                        context.getResources().getQuantityString(R.plurals.image, checkCount, checkCount),
                        folderForAlbum);

                new AlertDialog.Builder(context)
                        .setMessage(message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                List<ImageSaveTask> tasks = new ArrayList<>(items.size());
                                for (AlbumDownloadItem item : items) {
                                    if (item.checked) {
                                        tasks.add(new ImageSaveTask(item.postImage));
                                    }
                                }

                                if (imageSaver.startBundledTask(context, folderForAlbum, tasks)) {
                                    navigationController.popController();
                                }
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    public void onMenuItemClicked(ToolbarMenuItem menuItem) {
        if ((Integer) menuItem.getId() == CHECK_ALL) {
            RecyclerUtils.clearRecyclerCache(recyclerView);

            for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
                AlbumDownloadItem item = items.get(i);
                if (item.checked == allChecked) {
                    item.checked = !allChecked;
                    AlbumDownloadCell cell = (AlbumDownloadCell) recyclerView.findViewHolderForAdapterPosition(i);
                    if (cell != null) {
                        setItemChecked(cell, item.checked, true);
                    }
                }
            }
            updateAllChecked();
            updateTitle();
        }
    }

    @Override
    public void onSubMenuItemClicked(ToolbarMenuItem parent, FloatingMenuItem item) {
    }

    public void setPostImages(Loadable loadable, List<PostImage> postImages) {
        this.loadable = loadable;
        for (int i = 0, postImagesSize = postImages.size(); i < postImagesSize; i++) {
            PostImage postImage = postImages.get(i);
            items.add(new AlbumDownloadItem(postImage, true, i));
        }
    }

    private void updateTitle() {
        navigationItem.title = context.getString(R.string.album_download_screen, getCheckCount(), items.size());
        ((ToolbarNavigationController) navigationController).toolbar.updateTitle(navigationItem);
    }

    private void updateAllChecked() {
        allChecked = getCheckCount() == items.size();
    }

    private int getCheckCount() {
        int checkCount = 0;
        for (AlbumDownloadItem item : items) {
            if (item.checked) {
                checkCount++;
            }
        }
        return checkCount;
    }

    private static class AlbumDownloadItem {
        public PostImage postImage;
        public boolean checked;
        public int id;

        public AlbumDownloadItem(PostImage postImage, boolean checked, int id) {
            this.postImage = postImage;
            this.checked = checked;
            this.id = id;
        }
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumDownloadCell> {
        public AlbumAdapter() {
            setHasStableIds(true);
        }

        @Override
        public AlbumDownloadCell onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AlbumDownloadCell(LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_album_download, parent, false));
        }

        @Override
        public void onBindViewHolder(AlbumDownloadCell holder, int position) {
            AlbumDownloadItem item = items.get(position);

            holder.thumbnailView.setPostImage(item.postImage, dp(100), dp(100));
            setItemChecked(holder, item.checked, false);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }
    }

    private class AlbumDownloadCell extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView checkbox;
        private PostImageThumbnailView thumbnailView;

        public AlbumDownloadCell(View itemView) {
            super(itemView);
            itemView.getLayoutParams().height = recyclerView.getRealSpanWidth();
            checkbox = (ImageView) itemView.findViewById(R.id.checkbox);
            thumbnailView = (PostImageThumbnailView) itemView.findViewById(R.id.thumbnail_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            AlbumDownloadItem item = items.get(adapterPosition);
            item.checked = !item.checked;
            updateAllChecked();
            updateTitle();
            setItemChecked(this, item.checked, true);
        }
    }

    @SuppressWarnings("deprecation")
    private void setItemChecked(AlbumDownloadCell cell, boolean checked, boolean animated) {
        float scale = checked ? 0.75f : 1f;
        if (animated) {
            cell.thumbnailView.animate().scaleX(scale).scaleY(scale)
                    .setInterpolator(new DecelerateInterpolator(3f)).setDuration(500).start();
        } else {
            cell.thumbnailView.setScaleX(scale);
            cell.thumbnailView.setScaleY(scale);
        }

        Drawable drawable = context.getResources().getDrawable(checked ? R.drawable.ic_check_circle_white_24dp :
                R.drawable.ic_radio_button_unchecked_white_24dp);

        if (checked) {
            Drawable wrapped = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(wrapped, ThemeHelper.PrimaryColor.BLUE.color);
            cell.checkbox.setImageDrawable(wrapped);
        } else {
            cell.checkbox.setImageDrawable(drawable);
        }
    }
}
