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

package io.nekohasekai.sagernet.fmt

object Alerts {

    const val ROUTE_ALERT_NOT_VPN = 0
    const val ROUTE_ALERT_NEED_BACKGROUND_LOCATION_ACCESS = 1
    const val ROUTE_ALERT_NEED_COARSE_LOCATION_ACCESS = 3
    const val ROUTE_ALERT_NEED_FINE_LOCATION_ACCESS = 4
    const val ROUTE_ALERT_LOCATION_DISABLED = 2

    class RouteAlertException(val alert: Int, val routeName: String) : Exception()
}


