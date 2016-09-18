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
package org.floens.chan.ui.settings;

import android.view.Gravity;
import android.view.View;

import org.floens.chan.R;
import org.floens.chan.core.settings.Setting;
import org.floens.chan.ui.view.FloatingMenu;
import org.floens.chan.ui.view.FloatingMenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.floens.chan.utils.AndroidUtils.dp;

public class ListSettingView<T> extends SettingView implements FloatingMenu.FloatingMenuCallback, View.OnClickListener {
    public final List<Item> items;

    public int selected;

    private Setting<T> setting;

    public ListSettingView(SettingsController settingsController, Setting<T> setting, int name, String[] itemNames, String[] keys) {
        this(settingsController, setting, getString(name), itemNames, keys);
    }

    public ListSettingView(SettingsController settingsController, Setting<T> setting, String name, String[] itemNames, String[] keys) {
        super(settingsController, name);

        this.setting = setting;

        items = new ArrayList<>(itemNames.length);
        for (int i = 0; i < itemNames.length; i++) {
            items.add(i, new Item<>(itemNames[i], keys[i]));
        }

        updateSelection();
    }

    public ListSettingView(SettingsController settingsController, Setting<T> setting, int name, Item[] items) {
        this(settingsController, setting, getString(name), items);
    }

    public ListSettingView(SettingsController settingsController, Setting<T> setting, int name, List<Item> items) {
        this(settingsController, setting, getString(name), items);
    }

    public ListSettingView(SettingsController settingsController, Setting<T> setting, String name, Item[] items) {
        this(settingsController, setting, name, Arrays.asList(items));
    }

    public ListSettingView(SettingsController settingsController, Setting<T> setting, String name, List<Item> items) {
        super(settingsController, name);
        this.setting = setting;
        this.items = items;

        updateSelection();
    }

    public String getBottomDescription() {
        return items.get(selected).name;
    }

    public Setting<T> getSetting() {
        return setting;
    }

    @Override
    public void setView(View view) {
        super.setView(view);
        view.setOnClickListener(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        view.setEnabled(enabled);
        view.findViewById(R.id.top).setEnabled(enabled);
        View bottom = view.findViewById(R.id.bottom);
        if (bottom != null) {
            bottom.setEnabled(enabled);
        }
    }

    @Override
    public void onClick(View v) {
        List<FloatingMenuItem> menuItems = new ArrayList<>(items.size());
        for (Item item : items) {
            menuItems.add(new FloatingMenuItem(item.key, item.name, item.enabled));
        }

        FloatingMenu menu = new FloatingMenu(v.getContext());
        menu.setAnchor(v, Gravity.LEFT, dp(5), dp(5));
        menu.setPopupWidth(FloatingMenu.POPUP_WIDTH_ANCHOR);
        menu.setCallback(this);
        menu.setItems(menuItems);
        menu.show();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onFloatingMenuItemClicked(FloatingMenu menu, FloatingMenuItem item) {
        T selectedKey = (T) item.getId();
        setting.set(selectedKey);
        updateSelection();
        settingsController.onPreferenceChange(this);
    }

    @Override
    public void onFloatingMenuDismissed(FloatingMenu menu) {
    }

    public void updateSelection() {
        T selectedKey = setting.get();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).key.equals(selectedKey)) {
                selected = i;
                break;
            }
        }
    }

    public static class Item<T> {
        public final String name;
        public final T key;
        public boolean enabled;

        public Item(String name, T key) {
            this.name = name;
            this.key = key;
            enabled = true;
        }

        public Item(String name, T key, boolean enabled) {
            this.name = name;
            this.key = key;
            this.enabled = enabled;
        }
    }
}
