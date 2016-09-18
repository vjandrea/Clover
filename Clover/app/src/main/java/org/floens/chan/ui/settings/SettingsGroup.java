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

import java.util.ArrayList;
import java.util.List;

public class SettingsGroup {
    public final String name;
    public final List<SettingView> settingViews = new ArrayList<>();

    public SettingsGroup(int name) {
        this(SettingView.getString(name));
    }

    public SettingsGroup(String name) {
        this.name = name;
    }

    public SettingView add(SettingView settingView) {
        settingViews.add(settingView);
        return settingView;
    }
}
