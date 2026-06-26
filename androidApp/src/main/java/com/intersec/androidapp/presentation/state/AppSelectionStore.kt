package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.NativeFlowItem
import com.intersec.androidapp.core.bridge.NativePacketItem

/**
 * Singleton responsável por armazenar o estado global de seleção da UI.
 */
object AppSelectionStore {
    var selectedPacket: NativePacketItem? = null
    var selectedFlow: NativeFlowItem? = null

}

