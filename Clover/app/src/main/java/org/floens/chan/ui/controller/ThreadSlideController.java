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
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import android.view.ViewGroup;

import org.floens.chan.R;
import org.floens.chan.controller.Controller;
import org.floens.chan.controller.ControllerTransition;
import org.floens.chan.ui.layout.ThreadSlidingPaneLayout;
import org.floens.chan.ui.toolbar.NavigationItem;
import org.floens.chan.ui.toolbar.Toolbar;
import org.floens.chan.utils.Logger;

import java.lang.reflect.Field;

import static org.floens.chan.utils.AndroidUtils.dp;
import static org.floens.chan.utils.AndroidUtils.getAttrColor;

public class ThreadSlideController extends Controller implements DoubleNavigationController, SlidingPaneLayout.PanelSlideListener, ToolbarNavigationController.ToolbarSearchCallback {
    private static final String TAG = "ThreadSlideController";

    public Controller leftController;
    public Controller rightController;

    private boolean leftOpen = true;
    private ViewGroup emptyView;
    private ThreadSlidingPaneLayout slidingPaneLayout;

    public ThreadSlideController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        doubleNavigationController = this;

        navigationItem.swipeable = false;
        navigationItem.handlesToolbarInset = true;
        navigationItem.hasDrawer = true;

        view = inflateRes(R.layout.controller_thread_slide);

        slidingPaneLayout = (ThreadSlidingPaneLayout) view.findViewById(R.id.sliding_pane_layout);
        slidingPaneLayout.setThreadSlideController(this);
        slidingPaneLayout.setPanelSlideListener(this);
        slidingPaneLayout.setParallaxDistance(dp(100));
        slidingPaneLayout.setShadowResourceLeft(R.drawable.panel_shadow);
        int fadeColor = (getAttrColor(context, R.attr.backcolor) & 0xffffff) + 0xCC000000;
        slidingPaneLayout.setSliderFadeColor(fadeColor);
        slidingPaneLayout.openPane();

        setLeftController(null);
        setRightController(null);
    }

    public void onSlidingPaneLayoutStateRestored() {
        // SlidingPaneLayout does some annoying things for state restoring and incorrectly
        // tells us if the restored state was open or closed
        // We need to use reflection to get the private field that stores this correct state
        boolean restoredOpen = false;
        try {
            Field field = SlidingPaneLayout.class.getDeclaredField("mPreservedOpenState");
            field.setAccessible(true);
            restoredOpen = field.getBoolean(slidingPaneLayout);
        } catch (Exception e) {
            Logger.e(TAG, "Error getting restored open state with reflection", e);
        }
        if (restoredOpen != leftOpen) {
            leftOpen = restoredOpen;
            slideStateChanged(leftOpen);
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelOpened(View panel) {
        if (this.leftOpen != leftOpen()) {
            this.leftOpen = leftOpen();
            slideStateChanged(leftOpen());
        }
    }

    @Override
    public void onPanelClosed(View panel) {
        if (this.leftOpen != leftOpen()) {
            this.leftOpen = leftOpen();
            slideStateChanged(leftOpen());
        }
    }

    @Override
    public void switchToController(boolean leftController) {
        if (leftController != leftOpen()) {
            if (leftController) {
                slidingPaneLayout.openPane();
            } else {
                slidingPaneLayout.closePane();
            }
            Toolbar toolbar = ((ToolbarNavigationController) navigationController).toolbar;
            toolbar.processScrollCollapse(Toolbar.TOOLBAR_COLLAPSE_SHOW, true);
        }
    }

    @Override
    public void setEmptyView(ViewGroup emptyView) {
        this.emptyView = emptyView;
    }

    public void setLeftController(Controller leftController) {
        if (this.leftController != null) {
            this.leftController.onHide();
            removeChildController(this.leftController);
        }

        this.leftController = leftController;

        if (leftController != null) {
            addChildController(leftController);
            leftController.attachToParentView(slidingPaneLayout.leftPane);
            leftController.onShow();
            if (leftOpen()) {
                setParentNavigationItem(true);
            }
        }
    }

    public void setRightController(Controller rightController) {
        if (this.rightController != null) {
            this.rightController.onHide();
            removeChildController(this.rightController);
        } else {
            this.slidingPaneLayout.rightPane.removeAllViews();
        }

        this.rightController = rightController;

        if (rightController != null) {
            addChildController(rightController);
            rightController.attachToParentView(slidingPaneLayout.rightPane);
            rightController.onShow();
            if (!leftOpen()) {
                setParentNavigationItem(false);
            }
        } else {
            slidingPaneLayout.rightPane.addView(emptyView);
        }
    }

    @Override
    public Controller getLeftController() {
        return leftController;
    }

    @Override
    public Controller getRightController() {
        return rightController;
    }

    @Override
    public boolean pushController(Controller to) {
        return navigationController.pushController(to);
    }

    @Override
    public boolean pushController(Controller to, boolean animated) {
        return navigationController.pushController(to, animated);
    }

    @Override
    public boolean pushController(Controller to, ControllerTransition controllerTransition) {
        return navigationController.pushController(to, controllerTransition);
    }

    @Override
    public boolean popController() {
        return navigationController.popController();
    }

    @Override
    public boolean popController(boolean animated) {
        return navigationController.popController(animated);
    }

    @Override
    public boolean popController(ControllerTransition controllerTransition) {
        return navigationController.popController(controllerTransition);
    }

    @Override
    public boolean onBack() {
        if (!leftOpen()) {
            if (rightController != null && rightController.onBack()) {
                return true;
            } else {
                switchToController(true);
                return true;
            }
        } else {
            if (leftController != null && leftController.onBack()) {
                return true;
            }
        }

        return super.onBack();
    }

    @Override
    public void onSearchVisibilityChanged(boolean visible) {
        if (leftOpen() && leftController != null && leftController instanceof ToolbarNavigationController.ToolbarSearchCallback) {
            ((ToolbarNavigationController.ToolbarSearchCallback) leftController).onSearchVisibilityChanged(visible);
        }
        if (!leftOpen() && rightController != null && rightController instanceof ToolbarNavigationController.ToolbarSearchCallback) {
            ((ToolbarNavigationController.ToolbarSearchCallback) rightController).onSearchVisibilityChanged(visible);
        }
    }

    @Override
    public void onSearchEntered(String entered) {
        if (leftOpen() && leftController != null && leftController instanceof ToolbarNavigationController.ToolbarSearchCallback) {
            ((ToolbarNavigationController.ToolbarSearchCallback) leftController).onSearchEntered(entered);
        }
        if (!leftOpen() && rightController != null && rightController instanceof ToolbarNavigationController.ToolbarSearchCallback) {
            ((ToolbarNavigationController.ToolbarSearchCallback) rightController).onSearchEntered(entered);
        }
    }

    private boolean leftOpen() {
        return slidingPaneLayout.isOpen();
    }

    private void slideStateChanged(boolean leftOpen) {
        setParentNavigationItem(leftOpen);
    }

    private void setParentNavigationItem(boolean left) {
        Toolbar toolbar = ((ToolbarNavigationController) navigationController).toolbar;

        NavigationItem item = null;
        if (left) {
            if (leftController != null) {
                item = leftController.navigationItem;
            }
        } else {
            if (rightController != null) {
                item = rightController.navigationItem;
            }
        }

        if (item != null) {
            navigationItem = item;
            navigationItem.swipeable = false;
            navigationItem.handlesToolbarInset = true;
            navigationItem.hasDrawer = true;
            toolbar.setNavigationItem(false, true, navigationItem);
        }
    }
}
