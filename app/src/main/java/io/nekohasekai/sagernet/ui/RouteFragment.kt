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

package io.nekohasekai.sagernet.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.RuleEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.databinding.LayoutEmptyRouteBinding
import io.nekohasekai.sagernet.databinding.LayoutRouteItemBinding
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.widget.UndoSnackbarManager

class RouteFragment : ToolbarFragment(R.layout.layout_route), Toolbar.OnMenuItemClickListener {

    lateinit var activity: MainActivity
    lateinit var ruleListView: RecyclerView
    lateinit var ruleAdapter: RuleAdapter
    lateinit var undoManager: UndoSnackbarManager<RuleEntity>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = requireActivity() as MainActivity

        toolbar.setTitle(R.string.menu_route)
        toolbar.inflateMenu(R.menu.add_route_menu)
        toolbar.setOnMenuItemClickListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                top = bars.top,
                left = bars.left,
                right = bars.right,
            )
            WindowInsetsCompat.CONSUMED
        }

        ruleListView = view.findViewById(R.id.route_list)
        ruleListView.layoutManager = FixedLinearLayoutManager(ruleListView)
        ViewCompat.setOnApplyWindowInsetsListener(ruleListView) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left + dp2px(4),
                right = bars.right + dp2px(4),
                bottom = bars.bottom + dp2px(4),
            )
            WindowInsetsCompat.CONSUMED
        }

        ruleAdapter = RuleAdapter()
        ProfileManager.addListener(ruleAdapter)
        ruleListView.adapter = ruleAdapter
        undoManager = UndoSnackbarManager(activity, ruleAdapter)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.START) {

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ) = if (viewHolder is RuleAdapter.DocumentHolder) {
                0
            } else {
                super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun getDragDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ) = if (viewHolder is RuleAdapter.DocumentHolder) {
                0
            } else {
                super.getDragDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val index = viewHolder.adapterPosition
                ruleAdapter.remove(index)
                undoManager.remove(index to (viewHolder as RuleAdapter.RuleHolder).rule)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder,
            ): Boolean {
                return if (target is RuleAdapter.DocumentHolder) {
                    false
                } else {
                    ruleAdapter.move(viewHolder.adapterPosition, target.adapterPosition)
                    true
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ) {
                super.clearView(recyclerView, viewHolder)
                ruleAdapter.commitMove()
            }
        }).attachToRecyclerView(ruleListView)
    }

    override fun onDestroy() {
        if (::ruleAdapter.isInitialized) {
            ProfileManager.removeListener(ruleAdapter)
        }
        super.onDestroy()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_route -> {
                startActivity(Intent(context, RouteSettingsActivity::class.java))
            }
            R.id.action_reset_route -> {
                runOnDefaultDispatcher {
                    SagerDatabase.rulesDao.reset()
                    DataStore.rulesFirstCreate = false
                    ruleAdapter.reload()
                }
            }
            R.id.action_manage_assets -> {
                startActivity(Intent(requireContext(), AssetsActivity::class.java))
            }
        }
        return true
    }

    inner class RuleAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ProfileManager.RuleListener, UndoSnackbarManager.Interface<RuleEntity> {

        val ruleList = ArrayList<RuleEntity>()
        suspend fun reload() {
            val rules = ProfileManager.getRules()
            ruleListView.post {
                ruleList.clear()
                ruleList.addAll(rules)
                ruleAdapter.notifyDataSetChanged()
            }
        }

        init {
            runOnDefaultDispatcher {
                reload()
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                DocumentHolder(LayoutEmptyRouteBinding.inflate(layoutInflater, parent, false))
            } else {
                RuleHolder(LayoutRouteItemBinding.inflate(layoutInflater, parent, false))
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0) return 0
            return 1
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is DocumentHolder) {
                holder.bind()
            } else if (holder is RuleHolder) {
                holder.bind(ruleList[position - 1])
            }
        }

        override fun getItemCount(): Int {
            return ruleList.size + 1
        }

        override fun getItemId(position: Int): Long {
            if (position == 0) return 0L
            return ruleList[position - 1].id
        }

        private val updated = HashSet<RuleEntity>()
        fun move(from: Int, to: Int) {
            val first = ruleList[from - 1]
            var previousOrder = first.userOrder
            val (step, range) = if (from < to) Pair(1, from - 1 until to - 1) else Pair(-1, to downTo from - 1)
            for (i in range) {
                val next = ruleList[i + step]
                val order = next.userOrder
                next.userOrder = previousOrder
                previousOrder = order
                ruleList[i] = next
                updated.add(next)
            }
            first.userOrder = previousOrder
            ruleList[to - 1] = first
            updated.add(first)
            notifyItemMoved(from, to)
        }

        fun commitMove() = runOnDefaultDispatcher {
            if (updated.isNotEmpty()) {
                SagerDatabase.rulesDao.updateRules(updated.toList())
                updated.clear()
                needReload()
            }
        }

        fun remove(index: Int) {
            ruleList.removeAt(index - 1)
            notifyItemRemoved(index)
        }

        override fun undo(actions: List<Pair<Int, RuleEntity>>) {
            for ((index, item) in actions) {
                ruleList.add(index - 1, item)
                notifyItemInserted(index)
            }
        }

        override fun commit(actions: List<Pair<Int, RuleEntity>>) {
            val rules = actions.map { it.second }
            runOnDefaultDispatcher {
                ProfileManager.deleteRules(rules)
            }
        }

        override suspend fun onAdd(rule: RuleEntity) {
            ruleListView.post {
                ruleList.add(rule)
                ruleAdapter.notifyItemInserted(ruleList.size)
                needReload()
            }
        }

        override suspend fun onUpdated(rule: RuleEntity) {
            val index = ruleList.indexOfFirst { it.id == rule.id }
            if (index == -1) return
            ruleListView.post {
                ruleList[index] = rule
                ruleAdapter.notifyItemChanged(index + 1)
                needReload()
            }
        }

        override suspend fun onRemoved(ruleId: Long) {
            val index = ruleList.indexOfFirst { it.id == ruleId }
            if (index == -1) {
                onMainDispatcher {
                    needReload()
                }
            } else ruleListView.post {
                ruleList.removeAt(index)
                ruleAdapter.notifyItemRemoved(index + 1)
                needReload()
            }
        }

        override suspend fun onCleared() {
            ruleListView.post {
                ruleList.clear()
                ruleAdapter.notifyDataSetChanged()
                needReload()
            }
        }

        inner class DocumentHolder(binding: LayoutEmptyRouteBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind() {
                itemView.setOnClickListener {
                    it.context.launchCustomTab("https://www.v2fly.org/config/routing.html#ruleobject")
                }
            }
        }

        inner class RuleHolder(binding: LayoutRouteItemBinding) : RecyclerView.ViewHolder(binding.root) {

            lateinit var rule: RuleEntity
            val profileName = binding.profileName
            val profileType = binding.profileType
            val routeOutbound = binding.routeOutbound
            val editButton = binding.edit
            val shareLayout = binding.share
            val enableSwitch = binding.enable

            fun bind(ruleEntity: RuleEntity) {
                rule = ruleEntity
                profileName.text = rule.displayName()
                profileType.text = rule.mkSummary()
                routeOutbound.text = rule.displayOutbound()
                itemView.setOnClickListener {
                    enableSwitch.performClick()
                }
                enableSwitch.isChecked = rule.enabled
                enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                    runOnDefaultDispatcher {
                        rule.enabled = isChecked
                        SagerDatabase.rulesDao.updateRule(rule)
                        onMainDispatcher {
                            needReload()
                        }
                    }
                }
                editButton.setOnClickListener {
                    startActivity(Intent(it.context, RouteSettingsActivity::class.java).apply {
                        putExtra(RouteSettingsActivity.EXTRA_ROUTE_ID, rule.id)
                    })
                }
            }
        }

    }

}