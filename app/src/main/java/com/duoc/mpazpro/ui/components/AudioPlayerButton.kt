package com.duoc.mpazpro.ui.components

import android.media.MediaPlayer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioPlayerButton(audioResName: String?) {
    if (audioResName.isNullOrEmpty()) return
    val context = LocalContext.current

    Button(
        onClick = {
            val resId = context.resources.getIdentifier(audioResName, "raw", context.packageName)
            if (resId != 0) {
                val mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { it.release() }
            }
        },
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text("Escuchar Instruccion", fontSize = 18.sp)
    }
}