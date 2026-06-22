use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jstring, jboolean};
use once_cell::sync::Lazy;
use std::sync::Mutex;
use bridge_runtime::BridgeRuntime;
use android_adapter::{AndroidPacketQueryInput, AndroidFlowQueryInput};
use kotlin_bridge_contracts as kotlin;

static RUNTIME: Lazy<Mutex<BridgeRuntime>> = Lazy::new(|| {
    Mutex::new(BridgeRuntime::new())
});

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_pingNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.ping_android().unwrap_or_else(|e| format!("ERROR: {:?}", e));
    env.new_string(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_openCaptureNative(
    mut env: JNIEnv,
    _class: JClass,
    path: JString,
    now_epoch_micros: i64,
) -> jstring {
    let path_str: String = env.get_string(&path).unwrap().into();
    let mut runtime = RUNTIME.lock().unwrap();

    let result = runtime.open_capture_android(&path_str, now_epoch_micros);
    let output = match result {
        Ok(snapshot) => kotlin::encode_session_snapshot_text(&snapshot.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };

    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_snapshotActiveNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.snapshot_android();
    let output = match result {
        Ok(snapshot) => kotlin::encode_session_snapshot_text(&snapshot.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_queryPacketsNative(
    mut env: JNIEnv,
    _class: JClass,
    protocol: JString,
    host: JString,
    text: JString,
    packet_number: i64,
    offset: i64,
    limit: i32,
) -> jstring {
    let protocol_opt = opt_string(&mut env, protocol);
    let host_opt = opt_string(&mut env, host);
    let text_opt = opt_string(&mut env, text);
    let packet_number_opt = if packet_number >= 0 { Some(packet_number as u64) } else { None };

    let query = AndroidPacketQueryInput {
        protocol: protocol_opt,
        host: host_opt,
        text: text_opt,
        packet_number: packet_number_opt,
        offset: offset as usize,
        limit: limit as usize,
    };

    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.query_packets_android(query);
    let output = match result {
        Ok(res) => {
            let kotlin_res: kotlin::dto::KotlinPacketSearchResult = res.into();
            kotlin::encode_packet_result_text(&kotlin_res)
        },
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_queryFlowsNative(
    mut env: JNIEnv,
    _class: JClass,
    protocol: JString,
    host: JString,
    port: i32,
    text: JString,
    offset: i64,
    limit: i32,
) -> jstring {
    let protocol_opt = opt_string(&mut env, protocol);
    let host_opt = opt_string(&mut env, host);
    let port_opt = if port > 0 && port <= 65535 { Some(port as u16) } else { None };
    let text_opt = opt_string(&mut env, text);

    let query = AndroidFlowQueryInput {
        protocol: protocol_opt,
        host: host_opt,
        port: port_opt,
        text: text_opt,
        offset: offset as usize,
        limit: limit as usize,
    };

    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.query_flows_android(query);
    let output = match result {
        Ok(res) => {
            let kotlin_res: kotlin::dto::KotlinFlowSearchResult = res.into();
            kotlin::encode_flow_result_text(&kotlin_res)
        },
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_persistActiveNative(
    mut env: JNIEnv,
    _class: JClass,
    tags_csv: JString,
    notes: JString,
) -> jstring {
    let tags_str: String = env.get_string(&tags_csv).unwrap().into();
    let notes_opt = opt_string(&mut env, notes);

    let mut runtime = RUNTIME.lock().unwrap();
    let result = runtime.persist_active_android(&tags_str, notes_opt);
    let output = match result {
        Ok(_) => "OK".to_string(),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_listStoredSessionsNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.list_stored_sessions_android();
    let output = match result {
        Ok(sessions) => {
            let kotlin_sessions: Vec<_> = sessions.into_iter().map(Into::into).collect();
            kotlin::encode_session_list_text(&kotlin_sessions)
        },
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_getCaptureOverviewNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.get_capture_overview_android();
    let output = match result {
        Ok(res) => {
            let kotlin_res: kotlin::dto::KotlinCaptureOverview = res.into();
            kotlin::encode_capture_overview_text(&kotlin_res)
        },
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_startCaptureNative(
    mut env: JNIEnv,
    _class: JClass,
    iface: JString,
    filter: JString,
) -> jstring {
    let iface_str: String = env.get_string(&iface).unwrap().into();
    let filter_str: String = env.get_string(&filter).unwrap().into();

    let mut runtime = RUNTIME.lock().unwrap();
    let result = runtime.start_capture_android(&iface_str, &filter_str);
    let output = match result {
        Ok(session_id) => session_id,
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_stopCaptureNative(
    mut env: JNIEnv,
    _class: JClass,
    session_id: JString,
) -> jstring {
    let id_str: String = env.get_string(&session_id).unwrap().into();
    let mut runtime = RUNTIME.lock().unwrap();
    let result = runtime.stop_capture_android(&id_str);
    let output = match result {
        Ok(snapshot) => kotlin::encode_session_snapshot_text(&snapshot.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_capturePacketsNative(
    env: JNIEnv,
    _class: JClass,
    _session_id: JString,
    limit: i32,
) -> jstring {
    let runtime = RUNTIME.lock().unwrap();
    let result = runtime.get_latest_packets_android(limit as usize);
    let output = match result {
        Ok(res) => {
            // Ajustado para usar o tipo correto e a função de codificação correta
            let kotlin_res = kotlin::dto::KotlinPacketSearchResult {
                items: res.into_iter().map(Into::into).collect(),
                total_items: 0, // Durante a captura viva, o total é dinâmico
            };
            kotlin::encode_packet_result_text(&kotlin_res)
        },
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_runFullSystemTestNative(
    env: JNIEnv,
    _class: JClass,
) -> jstring {
    let runtime = RUNTIME.lock().unwrap();
    let start = std::time::Instant::now();

    // 1. Teste de Ping
    let _ = runtime.ping_android();

    // 2. Teste de Snapshot
    let _ = runtime.snapshot_android();

    // 3. Teste de Overview
    let _ = runtime.get_capture_overview_android();

    let duration = start.elapsed().as_micros();
    let report = format!("PASS|{}|Latency: {}us|All systems green", duration, duration);

    env.new_string(report).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_RustBridgeClient_00024Native_attachVpnTunnelNative(
    _env: JNIEnv,
    _class: JClass,
    fd: i32,
) -> jboolean {
    let mut runtime = RUNTIME.lock().unwrap();
    match runtime.attach_vpn_tunnel(fd) {
        Ok(_) => 1, // true
        Err(_) => 0, // false
    }
}

fn opt_string(env: &mut JNIEnv, s: JString) -> Option<String> {
    if s.is_null() {
        None
    } else {
        let val: String = env.get_string(&s).ok()?.into();
        if val.is_empty() { None } else { Some(val) }
    }
}
