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
package org.floens.chan.ui.helper;

import android.content.Context;

import org.floens.chan.controller.Controller;
import org.floens.chan.core.model.Post;
import org.floens.chan.core.model.PostImage;
import org.floens.chan.core.presenter.ThreadPresenter;
import org.floens.chan.ui.controller.PostRepliesController;
import org.floens.chan.ui.view.ThumbnailView;

import java.util.ArrayList;
import java.util.List;

public class PostPopupHelper {
    private Context context;
    private ThreadPresenter presenter;
    private final PostPopupHelperCallback callback;

    private final List<RepliesData> dataQueue = new ArrayList<>();
    private PostRepliesController presentingController;

    public PostPopupHelper(Context context, ThreadPresenter presenter, PostPopupHelperCallback callback) {
        this.context = context;
        this.presenter = presenter;
        this.callback = callback;
    }

    public void showPosts(Post forPost, List<Post> posts) {
        RepliesData data = new RepliesData(forPost, posts);

        dataQueue.add(data);

        if (dataQueue.size() == 1) {
            present();
        }
        presentingController.setPostRepliesData(data);
    }

    public void pop() {
        if (dataQueue.size() > 0) {
            dataQueue.remove(dataQueue.size() - 1);
        }

        if (dataQueue.size() > 0) {
            presentingController.setPostRepliesData(dataQueue.get(dataQueue.size() - 1));
        } else {
            dismiss();
        }
    }

    public void popAll() {
        dataQueue.clear();
        dismiss();
    }

    public boolean isOpen() {
        return presentingController != null && presentingController.alive;
    }

    public List<Post> getDisplayingPosts() {
        return presentingController.getPostRepliesData();
    }

    public void scrollTo(int displayPosition, boolean smooth) {
        presentingController.scrollTo(displayPosition, smooth);
    }

    public ThumbnailView getThumbnail(PostImage postImage) {
        return presentingController.getThumbnail(postImage);
    }

    public void postClicked(Post p) {
        popAll();
        presenter.highlightPost(p);
        presenter.scrollToPost(p, true);
    }

    private void dismiss() {
        if (presentingController != null) {
            presentingController.stopPresenting();
            presentingController = null;
        }
    }

    private void present() {
        if (presentingController == null) {
            presentingController = new PostRepliesController(context, this, presenter);
            callback.presentRepliesController(presentingController);
        }
    }

    public static class RepliesData {
        public List<Post> posts;
        public Post forPost;
        public int listViewIndex;
        public int listViewTop;

        public RepliesData(Post forPost, List<Post> posts) {
            this.forPost = forPost;
            this.posts = posts;
        }
    }

    public interface PostPopupHelperCallback {
        void presentRepliesController(Controller controller);
    }
}
