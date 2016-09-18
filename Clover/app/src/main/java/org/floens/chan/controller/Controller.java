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
package org.floens.chan.controller;

import android.content.Context;
import android.content.res.Configuration;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.floens.chan.controller.transition.FadeInTransition;
import org.floens.chan.controller.transition.FadeOutTransition;
import org.floens.chan.ui.activity.StartActivity;
import org.floens.chan.ui.controller.DoubleNavigationController;
import org.floens.chan.ui.toolbar.NavigationItem;
import org.floens.chan.utils.AndroidUtils;
import org.floens.chan.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class Controller {
    private static final boolean LOG_STATES = false;

    public Context context;
    public ViewGroup view;

    public NavigationItem navigationItem = new NavigationItem();

    public Controller parentController;

    public List<Controller> childControllers = new ArrayList<>();

    // NavigationControllers members
    public Controller previousSiblingController;
    public NavigationController navigationController;

    public DoubleNavigationController doubleNavigationController;

    /**
     * Controller that this controller is presented by.
     */
    public Controller presentingByController;

    /**
     * Controller that this controller is presenting.
     */
    public Controller presentingThisController;

    public boolean alive = false;
    public boolean shown = false;

    public Controller(Context context) {
        this.context = context;
    }

    public void onCreate() {
        alive = true;
        if (LOG_STATES) {
            Logger.test(getClass().getSimpleName() + " onCreate");
        }
    }

    public void onShow() {
        shown = true;
        if (LOG_STATES) {
            Logger.test(getClass().getSimpleName() + " onShow");
        }

        view.setVisibility(View.VISIBLE);

        for (Controller controller : childControllers) {
            if (!controller.shown) {
                controller.onShow();
            }
        }
    }

    public void onHide() {
        shown = false;
        if (LOG_STATES) {
            Logger.test(getClass().getSimpleName() + " onHide");
        }

        view.setVisibility(View.GONE);

        for (Controller controller : childControllers) {
            if (controller.shown) {
                controller.onHide();
            }
        }
    }

    public void onDestroy() {
        alive = false;
        if (LOG_STATES) {
            Logger.test(getClass().getSimpleName() + " onDestroy");
        }

        while (childControllers.size() > 0) {
            removeChildController(childControllers.get(0));
        }

        if (AndroidUtils.removeFromParentView(view)) {
            if (LOG_STATES) {
                Logger.test(getClass().getSimpleName() + " view removed onDestroy");
            }
        }
    }

    public void addChildController(Controller controller) {
        childControllers.add(controller);
        controller.parentController = this;
        if (doubleNavigationController != null) {
            controller.doubleNavigationController = doubleNavigationController;
        }
        if (navigationController != null) {
            controller.navigationController = navigationController;
        }
        controller.onCreate();
    }

    public boolean removeChildController(Controller controller) {
        controller.onDestroy();
        return childControllers.remove(controller);
    }

    public void attachToParentView(ViewGroup parentView) {
        if (view.getParent() != null) {
            if (LOG_STATES) {
                Logger.test(getClass().getSimpleName() + " view removed");
            }
            AndroidUtils.removeFromParentView(view);
        }

        if (parentView != null) {
            if (LOG_STATES) {
                Logger.test(getClass().getSimpleName() + " view attached");
            }
            attachToView(parentView, true);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        for (Controller controller : childControllers) {
            controller.onConfigurationChanged(newConfig);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        for (int i = childControllers.size() - 1; i >= 0; i--) {
            Controller controller = childControllers.get(i);
            if (controller.dispatchKeyEvent(event)) {
                return true;
            }
        }

        return false;
    }

    public boolean onBack() {
        for (int i = childControllers.size() - 1; i >= 0; i--) {
            Controller controller = childControllers.get(i);
            if (controller.onBack()) {
                return true;
            }
        }

        return false;
    }

    public void presentController(Controller controller) {
        presentController(controller, true);
    }

    public void presentController(Controller controller, boolean animated) {
        ViewGroup contentView = ((StartActivity) context).getContentView();
        presentingThisController = controller;
        controller.presentingByController = this;

        controller.onCreate();
        controller.attachToView(contentView, true);
        controller.onShow();

        if (animated) {
            ControllerTransition transition = new FadeInTransition();
            transition.to = controller;
            transition.perform();
        }

        ((StartActivity) context).addController(controller);
    }

    public void stopPresenting() {
        stopPresenting(true);
    }

    public void stopPresenting(boolean animated) {
        if (animated) {
            ControllerTransition transition = new FadeOutTransition();
            transition.from = this;
            transition.perform();
            transition.setCallback(new ControllerTransition.Callback() {
                @Override
                public void onControllerTransitionCompleted(ControllerTransition transition) {
                    finishPresenting();
                }
            });
        } else {
            finishPresenting();
        }

        ((StartActivity) context).removeController(this);
        presentingByController.presentingThisController = null;
    }

    private void finishPresenting() {
        onHide();
        onDestroy();
    }

    public Controller getTop() {
        if (childControllers.size() > 0) {
            return childControllers.get(childControllers.size() - 1);
        } else {
            return null;
        }
    }

    public ViewGroup inflateRes(int resId) {
        return (ViewGroup) LayoutInflater.from(context).inflate(resId, null);
    }

    private void attachToView(ViewGroup parentView, boolean over) {
        ViewGroup.LayoutParams params = view.getLayoutParams();

        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }

        view.setLayoutParams(params);

        if (over) {
            parentView.addView(view, view.getLayoutParams());
        } else {
            parentView.addView(view, 0, view.getLayoutParams());
        }
    }
}
