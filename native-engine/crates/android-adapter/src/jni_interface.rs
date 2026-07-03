use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use once_cell::sync::Lazy;
use std::sync::Mutex;
use crate::adapter::AndroidAdapter;
use crate::mapper::{AndroidPacketQueryInput, AndroidFlowQueryInput};
use kotlin_bridge_contracts as kotlin;

static ADAPTER: Lazy<Mutex<AndroidAdapter>> = Lazy::new(|| {
    Mutex::new(AndroidAdapter::new())
});

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_pingNative(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let adapter = ADAPTER.lock().unwrap();
    let result = adapter.ping();
    env.new_string(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_openCaptureNative(
    mut env: JNIEnv,
    _class: JClass,
    path: JString,
    now_epoch_micros: i64,
) -> jstring {
    let path_str: String = env.get_string(&path).unwrap().into();
    let mut adapter = ADAPTER.lock().unwrap();

    let result = adapter.open_capture(&path_str, now_epoch_micros);
    let output = match result {
        Ok(snapshot) => kotlin::encode_session_snapshot_text(&snapshot.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };

    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_snapshotActiveNative(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let adapter = ADAPTER.lock().unwrap();
    let result = adapter.snapshot_active();
    let output = match result {
        Ok(snapshot) => kotlin::encode_session_snapshot_text(&snapshot.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_queryPacketsNative(
    mut env: JNIEnv,
    _class: JClass,
    protocol: JString,
    host: JString,
    text: JString,
    packet_number: i64,
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
    };

    let adapter = ADAPTER.lock().unwrap();
    let result = adapter.query_packets(query);
    let output = match result {
        Ok(res) => kotlin::encode_packet_result_text(&res.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_queryFlowsNative(
    mut env: JNIEnv,
    _class: JClass,
    protocol: JString,
    host: JString,
    port: i32,
    text: JString,
) -> jstring {
    let protocol_opt = opt_string(&mut env, protocol);
    let host_opt = opt_string(&mut env, host);
    let port_opt = if port >= 0 { Some(port as u32) } else { None };
    let text_opt = opt_string(&mut env, text);

    let query = AndroidFlowQueryInput {
        protocol: protocol_opt,
        host: host_opt,
        port: port_opt,
        text: text_opt,
    };

    let adapter = ADAPTER.lock().unwrap();
    let result = adapter.query_flows(query);
    let output = match result {
        Ok(res) => kotlin::encode_flow_result_text(&res.into()),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_persistActiveNative(
    mut env: JNIEnv,
    _class: JClass,
    tags_csv: JString,
    notes: JString,
) -> jstring {
    let tags_str: String = env.get_string(&tags_csv).unwrap().into();
    let notes_opt = opt_string(&mut env, notes);

    let mut adapter = ADAPTER.lock().unwrap();
    let result = adapter.persist_active(&tags_str, notes_opt);
    let output = match result {
        Ok(_) => "OK".to_string(),
        Err(e) => format!("ERROR: {:?}", e),
    };
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_integration_Native_NativeBridgeClient_00024Native_listStoredSessionsNative(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let adapter = ADAPTER.lock().unwrap();
    let result = adapter.list_stored_sessions();
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
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_NativeBridgeClient_00024Native_getNeuralSnapshotNative(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let adapter = ADAPTER.lock().unwrap();
    let snapshot = adapter.get_neural_snapshot();
    let output = serde_json::to_string(&snapshot).unwrap_or_default();
    env.new_string(output).unwrap().into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_intersec_androidapp_core_bridge_NativeBridgeClient_00024Native_pushNeuralEventNative(
    mut env: JNIEnv,
    _class: JClass,
    ip: JString,
    proto: JString,
    lat: f64,
    lon: f64,
    volume: i64,
) {
    let ip_str: String = env.get_string(&ip).unwrap().into();
    let proto_str: String = env.get_string(&proto).unwrap().into();
    let mut adapter = ADAPTER.lock().unwrap();
    adapter.push_neural_event(&ip_str, &proto_str, lat, lon, volume as u64);
}

fn opt_string(env: &mut JNIEnv, s: JString) -> Option<String> {
    if s.is_null() {
        None
    } else {
        let val: String = env.get_string(&s).ok()?.into();
        if val.is_empty() { None } else { Some(val) }
    }
}

