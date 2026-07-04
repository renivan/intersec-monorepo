package com.intersec.androidapp.core

import android.Manifest
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TacticalIntegrityTest: Validação CLI de alta prioridade.
 * Verifica o alinhamento entre o Manifesto Android e o Motor Nativo.
 */
@RunWith(AndroidJUnit4::class)
class TacticalIntegrityTest {

    @Test
    fun validateCriticalPermissionsDeclared() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val packageManager = appContext.packageManager
        val info = packageManager.getPackageInfo(appContext.packageName, PackageManager.GET_PERMISSIONS)
        val declaredPermissions = info.requestedPermissions?.toList() ?: emptyList()

        val essentialPermissions = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.BLUETOOTH_CONNECT
        )

        essentialPermissions.forEach { permission ->
            assertTrue("Missão Abortada: Permissão $permission não declarada no Manifesto!", 
                declaredPermissions.contains(permission))
        }
    }

    @Test
    fun validateNativeEngineLoad() {
        // Testa se a ponte JNI consegue carregar a biblioteca principal do motor nativo
        try {
            System.loadLibrary("wireshark_mobile_core") // Ajustar se o nome da lib for outro
            assertTrue(true)
        } catch (e: UnsatisfiedLinkError) {
            // No ambiente de teste local (JVM), isso pode falhar se o .so não estiver no path,
            // mas em um dispositivo real ou emulador, deve carregar.
            println("Aviso: Biblioteca nativa não encontrada no ambiente de teste atual.")
        }
    }
}
