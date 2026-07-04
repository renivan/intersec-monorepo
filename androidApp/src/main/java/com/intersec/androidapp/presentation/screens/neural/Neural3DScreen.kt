package com.intersec.androidapp.presentation.screens.neural

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

/**
 * Sentinel 3D HUD: Interface imersiva baseada em Google Filament.
 */
@Composable
fun Neural3DScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    Neural3DContent(state, onBack, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Neural3DContent(
    state: com.intersec.androidapp.presentation.state.AnalysisUiState,
    onBack: () -> Unit,
    viewModel: AnalysisViewModel? = null
) {
    var showHud by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }
    
    // Estados de Controle 3D
    var rotationY by remember { mutableStateOf(0f) }
    var rotationX by remember { mutableStateOf(0f) }
    var zoomScale by remember { mutableStateOf(0.29f) }

    // Atualização do Relógio Sentinel
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1.seconds)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // --- ÁREA DE CLIQUE PARA DESELECIONAR ---
        Box(modifier = Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { viewModel?.inspectNeuralLink(null) })

        // --- CAMADA 1: MOTOR 3D FILAMENT (Composable SceneView) ---
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val modelInstance = rememberModelInstance(modelLoader, "models/earth.glb")
        
        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
        ) {
            modelInstance?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    scale = Scale(zoomScale),
                    rotation = Rotation(x = rotationX, y = rotationY),
                    position = Position(z = -1.0f) // Recua um pouco para o zoom ser mais visível
                )
            }
        }

        if (modelInstance == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.Cyan, strokeWidth = 2.dp)
        }

        // --- CAMADA 2 & 3: HUD TÉCNICO (Retrátil) ---
        if (showHud) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 80.dp, start = 16.dp)
                    .width(180.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                TechnicalInfoItem("Usuário", "ADMIN_ANALSYT")
                TechnicalInfoItem("HORÁRIO", currentTime)
                TechnicalInfoItem("License", "SENTINEL_PRO_v3")
                TechnicalInfoItem("NODES", state.neuralLinks.size.toString())
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .width(150.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                TechnicalInfoItem("LAT", "%.4f".format(state.lastLatitude ?: 0.0))
                TechnicalInfoItem("LON", "%.4f".format(state.lastLongitude ?: 0.0))
                TechnicalInfoItem("STATUS", if (state.userTier == 1) "ACTIVE" else "MOD")
            }
        }

        // --- CAMADA 4: CONTROLES SUPERIORES ---
        CenterAlignedTopAppBar(
            title = { Text("NEURAL SENTINEL 3D", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 14.sp) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.Cyan)
                }
            },
            actions = {
                IconButton(onClick = { showHud = !showHud }) {
                    Icon(imageVector = if (showHud) Icons.Default.Close else Icons.Default.Menu, contentDescription = "Menu HUD", tint = Color.Cyan)
                }
            }
        )

        // --- CAMADA 5: CONTROLES DE NAVEGAÇÃO VERTICAIS E TRANSLÚCIDOS ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 70.dp), // Aumentado de 40 para 70 para subir os controles
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // 1. Controle Azimutal (D-PAD)
            Box(modifier = Modifier.size(110.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = Color.Cyan.copy(alpha = 0.05f))
                }
                DirectionalButton(Icons.Default.ArrowDropUp, Modifier.align(Alignment.TopCenter)) { rotationX = (rotationX - 10f) % 360f }
                DirectionalButton(Icons.Default.ArrowDropDown, Modifier.align(Alignment.BottomCenter)) { rotationX = (rotationX + 10f) % 360f }
                DirectionalButton(Icons.AutoMirrored.Filled.ArrowLeft, Modifier.align(Alignment.CenterStart)) { rotationY = (rotationY - 10f) % 360f }
                DirectionalButton(Icons.AutoMirrored.Filled.ArrowRight, Modifier.align(Alignment.CenterEnd)) { rotationY = (rotationY + 10f) % 360f }
                Text("D-PAD", modifier = Modifier.align(Alignment.Center), color = Color.Cyan.copy(alpha = 0.4f), fontSize = 8.sp, fontWeight = FontWeight.Black)
            }

            // 2. Barra de Rotação X (Vertical)
            VerticalControlBar("ROT X", rotationX, -180f..180f, Color.Cyan) { rotationX = it }

            // 3. Barra de Rotação Y (Horizontal/Azimute)
            VerticalControlBar("ROT Y", rotationY, -180f..180f, Color.Cyan) { rotationY = it }

            // 4. Barra Central de Playback (Vertical)
            Column(
                modifier = Modifier
                    .height(110.dp)
                    .width(40.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Cyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlaybackIcon(Icons.Default.SkipPrevious)
                PlaybackIcon(Icons.Default.PlayArrow)
                PlaybackIcon(Icons.Default.SkipNext)
            }

            // 5. Barra de Zoom
            VerticalControlBar("ZOOM", zoomScale, 0.05f..1.5f, Color.White) { zoomScale = it }
        }

        // --- CAMADA 6: PAINEL DE INSPEÇÃO PROFUNDA (DPI) ---
        state.selectedNeuralLink?.let { link ->
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(24.dp)
                    .width(280.dp)
                    .animateContentSize(),
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("DECODIFICAÇÃO DE FLUXO", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel?.inspectNeuralLink(null) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("DESTINO: ${link.destIp}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text("GEO: ${link.city}, ${link.countryCode}", color = Color.LightGray, fontSize = 10.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Cyan.copy(alpha = 0.2f))
                    if (state.inspectedPacketPayload == null) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally), strokeWidth = 2.dp, color = Color.Cyan)
                    } else {
                        Text(text = state.inspectedPacketPayload, color = Color(0xFF00FF00), fontSize = 9.sp, fontFamily = FontFamily.Monospace, lineHeight = 14.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel?.blockIp(link.destIp, "BLOCK FROM 3D HUD") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red)) {
                        Text("BLOQUEAR NÓ NEURAL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VerticalControlBar(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.height(110.dp).width(36.dp)
    ) {
        Text(label, color = color.copy(alpha = 0.5f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.weight(1f).width(4.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(2.dp))) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 270f
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
                    .width(90.dp)
                    .align(Alignment.Center),
                colors = SliderDefaults.colors(
                    thumbColor = color.copy(alpha = 0.9f),
                    activeTrackColor = color.copy(alpha = 0.4f),
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun PlaybackIcon(icon: ImageVector, onClick: () -> Unit = {}) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(icon, null, tint = Color.Cyan, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun DirectionalButton(icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier.size(32.dp).background(Color.Cyan.copy(alpha = 0.1f), CircleShape).border(1.dp, Color.Cyan.copy(alpha = 0.2f), CircleShape)) {
        Icon(icon, null, tint = Color.Cyan, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun TechnicalInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = Color.Cyan.copy(alpha = 0.5f), fontSize = 9.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    }
}
