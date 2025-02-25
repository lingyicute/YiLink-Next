/******************************************************************************
 *                                                                            *
 * Copyright (C) 2025 by lingyicute <li@92li.us.kg>             *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.fmt.gson;

import androidx.annotation.NonNull;

import com.google.gson.stream.JsonToken;

public class JsonOr<X, Y> {

    public JsonToken tokenX;
    public JsonToken tokenY;

    public X valueX;
    public Y valueY;

    public JsonOr(JsonToken tokenX, JsonToken tokenY) {
        this.tokenX = tokenX;
        this.tokenY = tokenY;
    }

    protected JsonOr(X valueX, Y valueY) {
        this.valueX = valueX;
        this.valueY = valueY;
    }

    @NonNull
    @Override
    public String toString() {
        return valueX != null ? valueX.toString() : valueY != null ? valueY.toString() : "null";
    }
}
