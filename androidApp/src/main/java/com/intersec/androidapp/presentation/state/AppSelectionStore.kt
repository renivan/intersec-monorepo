package com.intersec.androidapp.presentation.state

import com.intersec.androidapp.core.bridge.RustFlowItem
import com.intersec.androidapp.core.bridge.RustPacketItem

/**
 * Singleton responsável por armazenar o estado global de seleção da UI.
 */
object AppSelectionStore {
    var selectedPacket: RustPacketItem? = null
    var selectedFlow: RustFlowItem? = null

}
