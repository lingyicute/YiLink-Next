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

package io.nekohasekai.sagernet.fmt.internal;

import androidx.annotation.NonNull;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import io.nekohasekai.sagernet.fmt.KryoConverters;

public class ConfigBean extends InternalBean {

    public String type;
    public String content;
    public String serverAddresses;

    @Override
    public String displayName() {
        if (!name.isEmpty()) {
            return name;
        } else {
            return "Config " + Math.abs(hashCode());
        }
    }

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();
        if (name == null) name = "";
        if (type == null) type = "v2ray";
        if (content == null) content = "{}";
        if (serverAddresses == null) serverAddresses = "";
    }

    @Override
    public void serialize(ByteBufferOutput output) {
        output.writeInt(1);

        output.writeString(type);
        output.writeString(content);
        output.writeString(serverAddresses);
    }

    @Override
    public void deserialize(ByteBufferInput input) {
        int version = input.readInt();

        type = input.readString();
        content = input.readString();
        if (version >= 1) {
            serverAddresses = input.readString();
        }
    }

    @NonNull
    @Override
    public ConfigBean clone() {
        return KryoConverters.deserialize(new ConfigBean(), KryoConverters.serialize(this));
    }

    public static final Creator<ConfigBean> CREATOR = new CREATOR<ConfigBean>() {
        @NonNull
        @Override
        public ConfigBean newInstance() {
            return new ConfigBean();
        }

        @Override
        public ConfigBean[] newArray(int size) {
            return new ConfigBean[size];
        }
    };

}
