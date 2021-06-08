package com.mee.utils

import android.content.SharedPreferences
import android.view.Menu
import com.CodeBoy.MediaFacer.mediaHolders.videoContent
import com.mee.player.R
import com.mee.ui.main.MainActivityViewModel

fun SharedPreferences.saveInt(key: String, value: Int) {
    val edit: SharedPreferences.Editor = edit()
    edit.putInt(key, value)
    edit.apply()
    edit.commit()
}

fun MutableList<videoContent>.sort(order: Int): MutableList<videoContent> {
    when(order) {
        SortOrder.NEWEST_DATE_FIRST.order -> {
            sortByDescending { it.date_added }
        }
        SortOrder.OLDEST_DATE_FIRST.order -> {
            sortBy { it.date_added }
        }
        SortOrder.LARGEST_FIRST.order -> {
            sortByDescending { it.videoSize }
        }
        SortOrder.SMALLEST_FIRST.order -> {
            sortBy { it.videoSize }
        }
        SortOrder.NAME_A_TO_Z.order -> {
            sortBy { it.videoName }
        }
        SortOrder.NAME_Z_TO_A.order -> {
            sortByDescending { it.videoName }
        }
    }
    return this
}

fun Menu.updateCheckedStatus(order: Int) {
    when(order) {
        SortOrder.NEWEST_DATE_FIRST.order -> {
            findItem(R.id.sort_by_newest_date_first_menu_item).isChecked = true
        }
        SortOrder.OLDEST_DATE_FIRST.order -> {
            findItem(R.id.sort_by_oldest_date_first_menu_item).isChecked = true

        }
        SortOrder.LARGEST_FIRST.order -> {
            findItem(R.id.sort_by_largest_first_menu_item).isChecked = true
        }
        SortOrder.SMALLEST_FIRST.order -> {
            findItem(R.id.sort_by_smallest_first_menu_item).isChecked = true
        }
        SortOrder.NAME_A_TO_Z.order -> {
            findItem(R.id.sort_by_name_a_to_z_menu_item).isChecked = true
        }
        SortOrder.NAME_Z_TO_A.order -> {
            findItem(R.id.sort_by_name_z_to_a_menu_item).isChecked = true
        }
    }
}
