package com.biohazard786.quickupi.quicktile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.biohazard786.quickupi.MainActivity
import com.biohazard786.quickupi.R

class QuickTileService : TileService() {
    @Suppress("DEPRECATION")
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        } else {
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        qsTile.apply {
            state = Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(R.string.quick_tile_subtitle)
            }
            updateTile()
        }
    }
}