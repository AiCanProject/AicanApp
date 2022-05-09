package com.aican.aicanappnoncfr.utils

import kotlinx.coroutines.*

class PlotGraphNotifier(delayMs: Long = 2000, onNewEntryCallback: OnNewEntryCallback) {
    var job: Job? = null
    var running = true

    init {
        running = true
        job = GlobalScope.launch(Dispatchers.Default) {
            while (running) {
                //onNewEntryCallback.onNewEntry()
                delay(delayMs)
            }
        }
    }

    fun stop(){
        running = false
        job?.cancel(CancellationException())
    }

    interface OnNewEntryCallback{
        fun onNewEntry()
    }
}