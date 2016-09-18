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
import android.widget.FrameLayout;

import org.floens.chan.R;
import org.floens.chan.controller.Controller;
import org.floens.chan.controller.ControllerTransition;
import org.floens.chan.controller.NavigationController;
import org.floens.chan.ui.toolbar.NavigationItem;
import org.floens.chan.ui.toolbar.Toolbar;

public abstract class ToolbarNavigationController extends NavigationController implements Toolbar.ToolbarCallback {
    protected Toolbar toolbar;

    public ToolbarNavigationController(Context context) {
        super(context);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void showSearch() {
        toolbar.openSearch();
    }

    @Override
    public void transition(Controller from, Controller to, boolean pushing, ControllerTransition controllerTransition) {
        super.transition(from, to, pushing, controllerTransition);

        if (to != null) {
            toolbar.setNavigationItem(controllerTransition != null, pushing, to.navigationItem);
            updateToolbarCollapse(to, controllerTransition != null);
        }
    }

    @Override
    public boolean beginSwipeTransition(Controller from, Controller to) {
        if (!super.beginSwipeTransition(from, to)) {
            return false;
        }

        toolbar.processScrollCollapse(Toolbar.TOOLBAR_COLLAPSE_SHOW, true);
        toolbar.beginTransition(to.navigationItem);
        toolbar.transitionProgress(0f, false);

        return true;
    }

    @Override
    public void swipeTransitionProgress(float progress) {
        super.swipeTransitionProgress(progress);

        toolbar.transitionProgress(progress, false);
    }

    @Override
    public void endSwipeTransition(Controller from, Controller to, boolean finish) {
        super.endSwipeTransition(from, to, finish);

        toolbar.finishTransition(finish);
        updateToolbarCollapse(finish ? to : from, controllerTransition != null);
    }

    @Override
    public void onMenuOrBackClicked(boolean isArrow) {
        if (isArrow) {
            onBack();
        } else {
            onMenuClicked();
        }
    }

    public void onMenuClicked() {
    }

    @Override
    public boolean onBack() {
        return toolbar.closeSearch() || super.onBack();
    }

    @Override
    public String getSearchHint(NavigationItem item) {
        return context.getString(R.string.search_hint);
    }

    @Override
    public void onSearchVisibilityChanged(NavigationItem item, boolean visible) {
        for (Controller controller : childControllers) {
            if (controller.navigationItem == item) {
                ((ToolbarSearchCallback) controller).onSearchVisibilityChanged(visible);
                break;
            }
        }
    }

    @Override
    public void onSearchEntered(NavigationItem item, String entered) {
        for (Controller controller : childControllers) {
            if (controller.navigationItem == item) {
                ((ToolbarSearchCallback) controller).onSearchEntered(entered);
                break;
            }
        }
    }

    protected void updateToolbarCollapse(Controller controller, boolean animate) {
        if (!controller.navigationItem.handlesToolbarInset) {
            FrameLayout.LayoutParams toViewParams = (FrameLayout.LayoutParams) controller.view.getLayoutParams();
            toViewParams.topMargin = toolbar.getToolbarHeight();
            controller.view.setLayoutParams(toViewParams);
        }

        toolbar.processScrollCollapse(Toolbar.TOOLBAR_COLLAPSE_SHOW, animate);
    }

    public interface ToolbarSearchCallback {
        void onSearchVisibilityChanged(boolean visible);

        void onSearchEntered(String entered);
    }
}
