/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.util.Objects;

public class Tuple4<_1, _2, _3, _4> {
    _1 _1;
    _2 _2;
    _3 _3;
    _4 _4;


    public Tuple4(_1 _1, _2 _2, _3 _3, _4 _4) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
    }

    public void set(_1 _1, _2 _2, _3 _3, _4 _4) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
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

    public _3 get_3() {
        return _3;
    }

    public void set_3(_3 _3) {
        this._3 = _3;
    }

    public _4 get_4() {
        return _4;
    }

    public void set_4(_4 _4) {
        this._4 = _4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple4)) return false;
        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
        return Objects.equals(_1, tuple4._1) &&
               Objects.equals(_2, tuple4._2) &&
               Objects.equals(_3, tuple4._3) &&
               Objects.equals(_4, tuple4._4);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_1, _2, _3, _4);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Tuple4{");
        sb.append("_1=").append(_1);
        sb.append(", _2=").append(_2);
        sb.append(", _3=").append(_3);
        sb.append(", _4=").append(_4);
        sb.append('}');
        return sb.toString();
    }
}
