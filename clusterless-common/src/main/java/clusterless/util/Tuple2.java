/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import java.util.Objects;

public class Tuple2<_1, _2> {
    _1 _1;
    _2 _2;

    public Tuple2(_1 _1, _2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public void set(_1 _1, _2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    public _1 get_1() {
        return _1;
    }

    public void set_1(_1 _1) {
        this._1 = _1;
    }

    public _2 get_2() {
        return _2;
    }

    public void set_2(_2 _2) {
        this._2 = _2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple2)) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1, tuple2._1) &&
               Objects.equals(_2, tuple2._2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1, _2);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tuple2{");
        sb.append("_1=").append(_1);
        sb.append(", _2=").append(_2);
        sb.append('}');
        return sb.toString();
    }
}
