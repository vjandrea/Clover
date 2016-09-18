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

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import org.floens.chan.R;
import org.floens.chan.core.settings.Setting;

public class BooleanSettingView extends SettingView implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private SwitchCompat switcher;
    private Setting<Boolean> setting;
    private String description;
    private boolean building = true;

    public BooleanSettingView(SettingsController settingsController, Setting<Boolean> setting, int name, int description) {
        this(settingsController, setting, getString(name), getString(description));
    }

    public BooleanSettingView(SettingsController settingsController, Setting<Boolean> setting, String name, String description) {
        super(settingsController, name);
        this.setting = setting;
        this.description = description;
    }

    @Override
    public void setView(View view) {
        super.setView(view);

        view.setOnClickListener(this);

        switcher = (SwitchCompat) view.findViewById(R.id.switcher);
        switcher.setOnCheckedChangeListener(this);

        switcher.setChecked(setting.get());

        building = false;
    }

    @Override
    public String getBottomDescription() {
        return description;
    }

    @Override
    public void setEnabled(boolean enabled) {
        view.setEnabled(enabled);
        view.findViewById(R.id.top).setEnabled(enabled);
        View bottom = view.findViewById(R.id.bottom);
        if (bottom != null) {
            bottom.setEnabled(enabled);
        }
        switcher.setEnabled(enabled);
    }

    @Override
    public void onClick(View v) {
        switcher.toggle();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!building) {
            setting.set(isChecked);
            settingsController.onPreferenceChange(this);
        }
    }
}
