package com.github.mikeandv.pingwatch.listener

import com.github.mikeandv.pingwatch.entity.RequestTimings
import okhttp3.Call
import okhttp3.EventListener

class TimingEventListenerFactory(private val onFinished: (RequestTimings) -> Unit) : EventListener.Factory {
    override fun create(call: Call): EventListener = TimingEventListener(onFinished)
}