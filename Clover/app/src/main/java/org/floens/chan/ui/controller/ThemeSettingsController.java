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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ContextThemeWrapper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.floens.chan.R;
import org.floens.chan.chan.ChanParser;
import org.floens.chan.controller.Controller;
import org.floens.chan.core.model.Loadable;
import org.floens.chan.core.model.Post;
import org.floens.chan.core.model.PostLinkable;
import org.floens.chan.core.settings.ChanSettings;
import org.floens.chan.ui.activity.StartActivity;
import org.floens.chan.ui.cell.PostCell;
import org.floens.chan.ui.theme.Theme;
import org.floens.chan.ui.theme.ThemeHelper;
import org.floens.chan.ui.toolbar.NavigationItem;
import org.floens.chan.ui.toolbar.Toolbar;
import org.floens.chan.ui.view.FloatingMenu;
import org.floens.chan.ui.view.FloatingMenuItem;
import org.floens.chan.ui.view.ThumbnailView;
import org.floens.chan.ui.view.ViewPagerAdapter;
import org.floens.chan.utils.AndroidUtils;
import org.floens.chan.utils.Time;

import java.util.ArrayList;
import java.util.List;

import static org.floens.chan.utils.AndroidUtils.dp;
import static org.floens.chan.utils.AndroidUtils.getAttrColor;
import static org.floens.chan.utils.AndroidUtils.getString;

public class ThemeSettingsController extends Controller implements View.OnClickListener {
    private PostCell.PostCellCallback DUMMY_POST_CALLBACK = new PostCell.PostCellCallback() {
        private Loadable loadable = Loadable.forThread("g", 1234);

        @Override
        public Loadable getLoadable() {
            return loadable;
        }

        @Override
        public void onPostClicked(Post post) {
        }

        @Override
        public void onThumbnailClicked(Post post, ThumbnailView thumbnail) {
        }

        @Override
        public void onShowPostReplies(Post post) {
        }

        @Override
        public void onPopulatePostOptions(Post post, List<FloatingMenuItem> menu) {
            menu.add(new FloatingMenuItem(1, "Option"));
        }

        @Override
        public void onPostOptionClicked(Post post, Object id) {
        }

        @Override
        public void onPostLinkableClicked(PostLinkable linkable) {
        }

        @Override
        public void onPostNoClicked(Post post) {
        }
    };

    private ViewPager pager;
    private FloatingActionButton done;
    private TextView textView;

    private Adapter adapter;
    private ThemeHelper themeHelper;

    private List<Theme> themes;
    private List<ThemeHelper.PrimaryColor> selectedPrimaryColors = new ArrayList<>();
    private ThemeHelper.PrimaryColor selectedAccentColor;

    public ThemeSettingsController(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        navigationItem.setTitle(R.string.settings_screen_theme);
        navigationItem.swipeable = false;
        view = inflateRes(R.layout.controller_theme);

        themeHelper = ThemeHelper.getInstance();
        themes = themeHelper.getThemes();

        pager = (ViewPager) view.findViewById(R.id.pager);
        done = (FloatingActionButton) view.findViewById(R.id.add);
        done.setOnClickListener(this);

        textView = (TextView) view.findViewById(R.id.text);

        SpannableString changeAccentColor = new SpannableString(getString(R.string.setting_theme_accent));
        changeAccentColor.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                showAccentColorPicker();
            }
        }, 0, changeAccentColor.length(), 0);

        textView.setText(TextUtils.concat(getString(R.string.setting_theme_explanation), changeAccentColor));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        adapter = new Adapter();
        pager.setAdapter(adapter);

        ChanSettings.ThemeColor currentSettingsTheme = ChanSettings.getThemeAndColor();
        for (int i = 0; i < themeHelper.getThemes().size(); i++) {
            Theme theme = themeHelper.getThemes().get(i);
            ThemeHelper.PrimaryColor primaryColor = theme.primaryColor;

            if (theme.name.equals(currentSettingsTheme.theme)) {
                // Current theme
                pager.setCurrentItem(i, false);
            }
            selectedPrimaryColors.add(primaryColor);
        }
        selectedAccentColor = themeHelper.getTheme().accentColor;
        done.setBackgroundTintList(ColorStateList.valueOf(selectedAccentColor.color));
    }

    @Override
    public void onClick(View v) {
        if (v == done) {
            saveTheme();
        }
    }

    private void saveTheme() {
        int currentItem = pager.getCurrentItem();
        Theme selectedTheme = themeHelper.getThemes().get(currentItem);
        ThemeHelper.PrimaryColor selectedColor = selectedPrimaryColors.get(currentItem);
        themeHelper.changeTheme(selectedTheme, selectedColor, selectedAccentColor);
        ((StartActivity) context).restart();
    }

    private void showAccentColorPicker() {
        List<FloatingMenuItem> items = new ArrayList<>();
        FloatingMenuItem selected = null;
        for (ThemeHelper.PrimaryColor color : themeHelper.getColors()) {
            FloatingMenuItem floatingMenuItem = new FloatingMenuItem(new ColorsAdapterItem(color, color.color), color.displayName);
            items.add(floatingMenuItem);
            if (color == selectedAccentColor) {
                selected = floatingMenuItem;
            }
        }

        FloatingMenu menu = getColorsMenu(items, selected, textView);
        menu.setCallback(new FloatingMenu.FloatingMenuCallback() {
            @Override
            public void onFloatingMenuItemClicked(FloatingMenu menu, FloatingMenuItem item) {
                ColorsAdapterItem colorItem = (ColorsAdapterItem) item.getId();
                selectedAccentColor = colorItem.color;
                done.setBackgroundTintList(ColorStateList.valueOf(selectedAccentColor.color));
            }

            @Override
            public void onFloatingMenuDismissed(FloatingMenu menu) {

            }
        });
        menu.setPopupWidth(dp(200));
        menu.setPopupHeight(dp(300));
        menu.show();
    }

    private FloatingMenu getColorsMenu(List<FloatingMenuItem> items, FloatingMenuItem selected, View anchor) {
        FloatingMenu menu = new FloatingMenu(context);

        menu.setItems(items);
        menu.setAdapter(new ColorsAdapter(items));
        menu.setSelectedItem(selected);
        menu.setAnchor(anchor, Gravity.CENTER, 0, dp(5));
        menu.setPopupWidth(anchor.getWidth());
        return menu;
    }

    private class Adapter extends ViewPagerAdapter {
        public Adapter() {
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public View getView(final int position, ViewGroup parent) {
            final Theme theme = themes.get(position);

            Context themeContext = new ContextThemeWrapper(context, theme.resValue);

            Post post = new Post();
            post.no = 123456789;
            post.time = (Time.get() - (30 * 60 * 1000)) / 1000;
            // No synchronization needed, this is a dummy
            post.repliesFrom.add(1);
            post.repliesFrom.add(2);
            post.repliesFrom.add(3);
            post.subject = "Lorem ipsum";
            post.rawComment = "<a href=\"#p123456789\" class=\"quotelink\">&gt;&gt;123456789</a><br>" +
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.<br>" +
                    "<br>" +
                    "<a href=\"#p123456789\" class=\"quotelink\">&gt;&gt;123456789</a><br>" +
                    "http://example.com/" +
                    "<br>" +
                    "Phasellus consequat semper sodales. Donec dolor lectus, aliquet nec mollis vel, rutrum vel enim.";
            ChanParser.getInstance().parse(theme, post);

            LinearLayout linearLayout = new LinearLayout(themeContext);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setBackgroundColor(getAttrColor(themeContext, R.attr.backcolor));

            final Toolbar toolbar = new Toolbar(themeContext);
            final View.OnClickListener colorClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<FloatingMenuItem> items = new ArrayList<>();
                    FloatingMenuItem selected = null;
                    for (ThemeHelper.PrimaryColor color : themeHelper.getColors()) {
                        FloatingMenuItem floatingMenuItem = new FloatingMenuItem(new ColorsAdapterItem(color, color.color500), color.displayName);
                        items.add(floatingMenuItem);
                        if (color == selectedPrimaryColors.get(position)) {
                            selected = floatingMenuItem;
                        }
                    }

                    FloatingMenu menu = getColorsMenu(items, selected, toolbar);
                    menu.setCallback(new FloatingMenu.FloatingMenuCallback() {
                        @Override
                        public void onFloatingMenuItemClicked(FloatingMenu menu, FloatingMenuItem item) {
                            ColorsAdapterItem colorItem = (ColorsAdapterItem) item.getId();
                            selectedPrimaryColors.set(position, colorItem.color);
                            toolbar.setBackgroundColor(colorItem.color.color);
                        }

                        @Override
                        public void onFloatingMenuDismissed(FloatingMenu menu) {
                        }
                    });
                    menu.show();
                }
            };
            toolbar.setCallback(new Toolbar.ToolbarCallback() {
                @Override
                public void onMenuOrBackClicked(boolean isArrow) {
                    colorClick.onClick(toolbar);
                }

                @Override
                public void onSearchVisibilityChanged(NavigationItem item, boolean visible) {
                }

                @Override
                public String getSearchHint(NavigationItem item) {
                    return null;
                }

                @Override
                public void onSearchEntered(NavigationItem item, String entered) {
                }
            });
            toolbar.setBackgroundColor(theme.primaryColor.color);
            final NavigationItem item = new NavigationItem();
            item.title = theme.displayName;
            item.hasBack = false;
            toolbar.setNavigationItem(false, true, item);
            toolbar.setOnClickListener(colorClick);

            linearLayout.addView(toolbar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    themeContext.getResources().getDimensionPixelSize(R.dimen.toolbar_height)));

            PostCell postCell = (PostCell) LayoutInflater.from(themeContext).inflate(R.layout.cell_post, null);
            postCell.setPost(theme, post, DUMMY_POST_CALLBACK, false, false, -1, true, ChanSettings.PostViewMode.LIST);
            linearLayout.addView(postCell, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            return linearLayout;
        }

        @Override
        public int getCount() {
            return themes.size();
        }
    }

    private class ColorsAdapter extends BaseAdapter {
        private List<FloatingMenuItem> items;

        public ColorsAdapter(List<FloatingMenuItem> items) {
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder")
            TextView textView = (TextView) LayoutInflater.from(context).inflate(R.layout.toolbar_menu_item, parent, false);
            textView.setText(getItem(position));
            textView.setTypeface(AndroidUtils.ROBOTO_MEDIUM);

            ColorsAdapterItem color = (ColorsAdapterItem) items.get(position).getId();

            textView.setBackgroundColor(color.bg);
            boolean lightColor = (Color.red(color.bg) * 0.299f) + (Color.green(color.bg) * 0.587f) + (Color.blue(color.bg) * 0.114f) > 125f;
            textView.setTextColor(lightColor ? 0xff000000 : 0xffffffff);

            return textView;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int position) {
            return items.get(position).getText();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    private static class ColorsAdapterItem {
        public ThemeHelper.PrimaryColor color;
        public int bg;

        public ColorsAdapterItem(ThemeHelper.PrimaryColor color, int bg) {
            this.color = color;
            this.bg = bg;
        }
    }
}
