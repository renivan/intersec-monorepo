use core_types::ProtocolNode;

pub fn build_summary(nodes: &[ProtocolNode]) -> String {
    if nodes.is_empty() {
        return "Empty packet".into();
    }

    let mut labels = Vec::<String>::new();
    for node in nodes {
        labels.push(node.label.clone());
    }
    labels.join(" -> ")
}
