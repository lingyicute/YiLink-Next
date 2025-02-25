/******************************************************************************
 * Copyright (C) 2025 by lingyicute <contact-git@sekai.icu>                  *
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

package io.nekohasekai.sagernet.bg.test

import io.nekohasekai.sagernet.bg.AbstractInstance
import io.nekohasekai.sagernet.database.DataStore
import libcore.DebugInstance
import libcore.Libcore

class DebugInstance : AbstractInstance {

    lateinit var instance: DebugInstance

    override fun launch() {
        instance = Libcore.newDebugInstance(DataStore.pprofServer)
    }

    override fun close() {
        if (::instance.isInitialized) instance.close()
    }
}