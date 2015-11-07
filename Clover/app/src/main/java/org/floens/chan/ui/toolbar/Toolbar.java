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
package org.floens.chan.ui.toolbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.floens.chan.R;
import org.floens.chan.ui.drawable.ArrowMenuDrawable;
import org.floens.chan.ui.drawable.DropdownArrowDrawable;
import org.floens.chan.ui.layout.SearchLayout;
import org.floens.chan.ui.view.LoadView;
import org.floens.chan.utils.AndroidUtils;

import java.util.ArrayList;
import java.util.List;

import static org.floens.chan.utils.AndroidUtils.dp;
import static org.floens.chan.utils.AndroidUtils.getAttrColor;
import static org.floens.chan.utils.AndroidUtils.setRoundItemBackground;

public class Toolbar extends LinearLayout implements View.OnClickListener, LoadView.Listener {
    public static final int TOOLBAR_COLLAPSE_HIDE = 1000000;
    public static final int TOOLBAR_COLLAPSE_SHOW = -1000000;

    private final RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            processScrollCollapse(dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                View positionZero = recyclerView.getLayoutManager().findViewByPosition(0);
                boolean allowHide = positionZero == null || positionZero.getTop() < 0;
                if (allowHide || lastScrollDeltaOffset <= 0) {
                    setCollapse(lastScrollDeltaOffset <= 0 ? TOOLBAR_COLLAPSE_SHOW : TOOLBAR_COLLAPSE_HIDE, true);
                } else {
                    setCollapse(TOOLBAR_COLLAPSE_SHOW, true);
                }
            }
        }
    };

    private ImageView arrowMenuView;
    private ArrowMenuDrawable arrowMenuDrawable;

    private LoadView navigationItemContainer;

    private ToolbarCallback callback;
    private NavigationItem navigationItem;
    private boolean openKeyboardAfterSearchViewCreated = false;
    private int lastScrollDeltaOffset;
    private int scrollOffset;

    public Toolbar(Context context) {
        super(context);
        init();
    }

    public Toolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getToolbarHeight() {
        return getHeight() == 0 ? getLayoutParams().height : getHeight();
    }

    public void processScrollCollapse(int offset) {
        processScrollCollapse(offset, false);
    }

    public void processScrollCollapse(int offset, boolean animated) {
        lastScrollDeltaOffset = offset;
        setCollapse(offset, animated);
    }

    public void setCollapse(int offset, boolean animated) {
        scrollOffset += offset;
        scrollOffset = Math.max(0, Math.min(getHeight(), scrollOffset));

        if (animated) {
            animate().translationY(-scrollOffset).setDuration(300).setInterpolator(new DecelerateInterpolator(2f)).start();
        } else {
            animate().cancel();
            setTranslationY(-scrollOffset);
        }
    }

    public void attachRecyclerViewScrollStateListener(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
    }

    public void detachRecyclerViewScrollStateListener(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

    public void updateNavigation() {
        closeSearchInternal();
        setNavigationItem(false, false, navigationItem);
    }

    public NavigationItem getNavigationItem() {
        return navigationItem;
    }

    public boolean openSearch() {
        return openSearchInternal();
    }

    public boolean closeSearch() {
        return closeSearchInternal();
    }

    public void setNavigationItem(final boolean animate, final boolean pushing, final NavigationItem item) {
        setNavigationItemInternal(animate, pushing, item);
    }

    public void setCallback(ToolbarCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onClick(View v) {
        if (v == arrowMenuView) {
            callback.onMenuOrBackClicked(arrowMenuDrawable.getProgress() == 1f);
        }
    }

    public void setArrowMenuProgress(float progress) {
        arrowMenuDrawable.setProgress(progress);
    }

    public ArrowMenuDrawable getArrowMenuDrawable() {
        return arrowMenuDrawable;
    }

    @Override
    public void onLoadViewRemoved(View view) {
        // TODO: this is kinda a hack
        if (view instanceof ViewGroup) {
            ((ViewGroup) view).removeAllViews();
        }
    }

    void setTitle(NavigationItem navigationItem) {
        if (navigationItem.view != null) {
            TextView titleView = (TextView) navigationItem.view.findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(navigationItem.title);
            }

            if (!TextUtils.isEmpty(navigationItem.subtitle)) {
                TextView subtitleView = (TextView) navigationItem.view.findViewById(R.id.subtitle);
                if (subtitleView != null) {
                    subtitleView.setText(navigationItem.subtitle);
                }
            }
        }
    }

    private void init() {
        setOrientation(HORIZONTAL);

        FrameLayout leftButtonContainer = new FrameLayout(getContext());
        addView(leftButtonContainer, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

        arrowMenuView = new ImageView(getContext());
        arrowMenuView.setOnClickListener(this);
        arrowMenuView.setFocusable(true);
        arrowMenuView.setScaleType(ImageView.ScaleType.CENTER);
        arrowMenuDrawable = new ArrowMenuDrawable();
        arrowMenuView.setImageDrawable(arrowMenuDrawable);

        setRoundItemBackground(arrowMenuView);

        leftButtonContainer.addView(arrowMenuView, new FrameLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.toolbar_height), FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER_VERTICAL));

        navigationItemContainer = new LoadView(getContext());
        navigationItemContainer.setListener(this);
        addView(navigationItemContainer, new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(dp(4f));
        }
    }

    private boolean openSearchInternal() {
        if (navigationItem != null && !navigationItem.search) {
            navigationItem.search = true;
            openKeyboardAfterSearchViewCreated = true;
            setNavigationItemInternal(true, false, navigationItem);
            callback.onSearchVisibilityChanged(true);
            return true;
        } else {
            return false;
        }
    }

    private boolean closeSearchInternal() {
        if (navigationItem != null && navigationItem.search) {
            navigationItem.search = false;
            navigationItem.searchText = null;
            setNavigationItemInternal(true, false, navigationItem);
            AndroidUtils.hideKeyboard(navigationItemContainer);
            callback.onSearchVisibilityChanged(false);
            return true;
        } else {
            return false;
        }
    }

    private void setNavigationItemInternal(boolean animate, boolean pushing, NavigationItem toItem) {
        final NavigationItem fromItem = navigationItem;

        boolean same = toItem == navigationItem;

        if (!toItem.search) {
            AndroidUtils.hideKeyboard(navigationItemContainer);
        }

        if (fromItem != null) {
            fromItem.toolbar = null;
        }

        toItem.toolbar = this;

        if (!animate) {
            if (fromItem != null) {
                removeNavigationItem(fromItem);
            }
            setArrowMenuProgress(toItem.hasBack ? 1f : 0f);
        }

        toItem.view = createNavigationItemView(toItem);

        // use the LoadView animation when from a search
        if (same) {
            navigationItemContainer.setView(toItem.view, animate);
        } else {
            navigationItemContainer.addView(toItem.view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        final int duration = 300;
        final int offset = dp(16);
        final int delay = pushing ? 0 : 100;

        if (animate) {
            animateArrow(toItem.hasBack || toItem.search, toItem.search ? 0 : delay);
        }

        // Use the LoadView animation when from a search
        if (animate && !same) {
            toItem.view.setAlpha(0f);

            List<Animator> animations = new ArrayList<>(5);

            Animator toYAnimation = ObjectAnimator.ofFloat(toItem.view, View.TRANSLATION_Y, pushing ? offset : -offset, 0f);
            toYAnimation.setDuration(duration);
            toYAnimation.setInterpolator(new DecelerateInterpolator(2f));
            toYAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (fromItem != null) {
                        removeNavigationItem(fromItem);
                    }
                }
            });
            animations.add(toYAnimation);

            Animator toAlphaAnimation = ObjectAnimator.ofFloat(toItem.view, View.ALPHA, 0f, 1f);
            toAlphaAnimation.setDuration(duration);
            toAlphaAnimation.setInterpolator(new DecelerateInterpolator(2f));
            animations.add(toAlphaAnimation);

            if (fromItem != null) {
                Animator fromYAnimation = ObjectAnimator.ofFloat(fromItem.view, View.TRANSLATION_Y, 0f, pushing ? -offset : offset);
                fromYAnimation.setDuration(duration);
                fromYAnimation.setInterpolator(new DecelerateInterpolator(2f));
                animations.add(fromYAnimation);

                Animator fromAlphaAnimation = ObjectAnimator.ofFloat(fromItem.view, View.ALPHA, 1f, 0f);
                fromAlphaAnimation.setDuration(duration);
                fromAlphaAnimation.setInterpolator(new DecelerateInterpolator(2f));
                animations.add(fromAlphaAnimation);
            }

            AnimatorSet set = new AnimatorSet();
            set.setStartDelay(delay);
            set.playTogether(animations);
            set.start();
        }

        navigationItem = toItem;
    }

    private void removeNavigationItem(NavigationItem item) {
        item.view.removeAllViews();
        navigationItemContainer.removeView(item.view);
        item.view = null;
    }

    private LinearLayout createNavigationItemView(final NavigationItem item) {
        if (item.search) {
            SearchLayout searchLayout = new SearchLayout(getContext());

            searchLayout.setCallback(new SearchLayout.SearchLayoutCallback() {
                @Override
                public void onSearchEntered(String entered) {
                    item.searchText = entered;
                    callback.onSearchEntered(entered);
                }
            });

            if (item.searchText != null) {
                searchLayout.setText(item.searchText);
            }

            searchLayout.setHint(callback.getSearchHint());

            if (openKeyboardAfterSearchViewCreated) {
                openKeyboardAfterSearchViewCreated = false;
                searchLayout.openKeyboard();
            }

            searchLayout.setPadding(dp(16), searchLayout.getPaddingTop(), searchLayout.getPaddingRight(), searchLayout.getPaddingBottom());

            return searchLayout;
        } else {
            @SuppressLint("InflateParams")
            LinearLayout menu = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.toolbar_menu, null);
            menu.setGravity(Gravity.CENTER_VERTICAL);

            FrameLayout titleContainer = (FrameLayout) menu.findViewById(R.id.title_container);

            final TextView titleView = (TextView) menu.findViewById(R.id.title);
            titleView.setTypeface(AndroidUtils.ROBOTO_MEDIUM);
            titleView.setText(item.title);
            titleView.setTextColor(0xffffffff);

            if (item.middleMenu != null) {
                item.middleMenu.setAnchor(titleView, Gravity.LEFT, dp(5), dp(5));

                Drawable drawable = new DropdownArrowDrawable(dp(12), dp(12), true, getAttrColor(getContext(), R.attr.dropdown_light_color), getAttrColor(getContext(), R.attr.dropdown_light_pressed_color));
                titleView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);

                titleView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.middleMenu.show();
                    }
                });
            }

            TextView subtitleView = (TextView) menu.findViewById(R.id.subtitle);
            if (!TextUtils.isEmpty(item.subtitle)) {
                ViewGroup.LayoutParams titleParams = titleView.getLayoutParams();
                titleParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                titleView.setLayoutParams(titleParams);
                subtitleView.setText(item.subtitle);
                subtitleView.setTextColor(0xffffffff);
                titleView.setPadding(titleView.getPaddingLeft(), dp(5f), titleView.getPaddingRight(), titleView.getPaddingBottom());
            } else {
                titleContainer.removeView(subtitleView);
            }

            if (item.rightView != null) {
                item.rightView.setPadding(0, 0, dp(16), 0);
                menu.addView(item.rightView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            }

            if (item.menu != null) {
                menu.addView(item.menu, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            }

            AndroidUtils.waitForMeasure(titleView, new AndroidUtils.OnMeasuredCallback() {
                @Override
                public boolean onMeasured(View view) {
                    if (item.middleMenu != null) {
                        item.middleMenu.setPopupWidth(Math.max(dp(200), titleView.getWidth()));
                    }
                    return false;
                }
            });

            return menu;
        }
    }

    private void animateArrow(boolean toArrow, long delay) {
        float to = toArrow ? 1f : 0f;
        if (to != arrowMenuDrawable.getProgress()) {
            ValueAnimator arrowAnimation = ValueAnimator.ofFloat(arrowMenuDrawable.getProgress(), to);
            arrowAnimation.setDuration(300);
            arrowAnimation.setInterpolator(new DecelerateInterpolator(2f));
            arrowAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setArrowMenuProgress((float) animation.getAnimatedValue());
                }
            });
            arrowAnimation.setStartDelay(delay);
            arrowAnimation.start();
        }
    }

    public interface ToolbarCallback {
        void onMenuOrBackClicked(boolean isArrow);

        void onSearchVisibilityChanged(boolean visible);

        String getSearchHint();

        void onSearchEntered(String entered);
    }
}
