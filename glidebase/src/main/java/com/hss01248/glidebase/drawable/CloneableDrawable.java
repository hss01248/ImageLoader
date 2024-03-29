/*
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.hss01248.glidebase.drawable;

import android.graphics.drawable.Drawable;

/**
 * A drawable that is capable of cloning itself.
 */
public interface CloneableDrawable {

    /**
     * Creates a copy of the drawable.
     *
     * @return the drawable copy
     */
    Drawable cloneDrawable();
}
