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
package org.floens.chan.ui.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import static org.floens.chan.utils.AndroidUtils.dp;

public class PopupControllerContainer extends FrameLayout {
    public PopupControllerContainer(Context context) {
        super(context);
    }

    public PopupControllerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopupControllerContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        FrameLayout.LayoutParams child = (LayoutParams) getChildAt(0).getLayoutParams();

        if (widthMode == MeasureSpec.EXACTLY && widthSize < dp(600)) {
            child.width = widthSize;
        } else {
            child.width = dp(600);
        }

        if (heightMode == MeasureSpec.EXACTLY && heightSize < dp(600)) {
            child.height = heightSize;
        } else {
            child.height = dp(600);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
