package com.activeminds.projectrepo.utils

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun View.gone() {
    if (this.visibility != View.GONE)
        this.visibility = View.GONE
}

fun View.visible() {
    if (this.visibility != View.VISIBLE)
        this.visibility = View.VISIBLE
}

fun View.invisible() {
    if (this.visibility != View.INVISIBLE)
        this.visibility = View.INVISIBLE
}

fun View.showSnack(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
        .show()
}