package com.oyl.frutossecos

import android.Manifest
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.core.content.edit

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale


@Composable fun ProfilePage(vm:OylViewModel,onGo:(Screen)->Unit,onLogout:()->Unit){
 val context=LocalContext.current
 var name by remember(vm.userId){mutableStateOf(vm.userName)}
 var phone by remember(vm.userId){mutableStateOf("")}
 var address by remember(vm.userId){mutableStateOf("")}
 LaunchedEffect(vm.userId){if(vm.userId>0){val row=OylDatabase.get(context).dao().clientById(vm.userId);if(row!=null){name=row.name;phone=row.phone;address=row.address}}}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),horizontalAlignment=Alignment.CenterHorizontally){
  Text("👤",style=MaterialTheme.typography.displayLarge)
  Heading(vm.userName,if(vm.isAdmin)"Administrador" else "Cliente")
  if(!vm.isAdmin){Input(name,{name=it},"Nombre");Input(phone,{phone=it},"Teléfono");Input(address,{address=it},"Dirección");Pill("Guardar perfil"){vm.saveProfile(name,phone,address){Toast.makeText(context,it,Toast.LENGTH_SHORT).show()}}}
  Panel{Text("Accesos");listOf("Catálogo" to Screen.CATALOG,"Carrito" to Screen.CART,"Notificaciones" to Screen.NOTICES,"Pedidos" to Screen.ORDERS).forEach{(t,s)->TextButton(onClick={onGo(s)}){Text("→ $t")}};if(vm.isAdmin){TextButton(onClick={onGo(Screen.MANAGE)}){Text("→ Gestión de productos")};TextButton(onClick={onGo(Screen.INVENTORY)}){Text("→ Inventario")}}}
  HorizontalDivider()
  Heading("Conexión Spring Boot","Configura la IP de tu computador para el teléfono físico")
  var url by remember { mutableStateOf(vm.serverUrl) }
  var enabled by remember { mutableStateOf(vm.online) }
  var feedback by remember { mutableStateOf("") }
  Input(url,{url=it},"URL del servidor (ej. http://192.168.1.20:8080/)")
  Row(verticalAlignment=Alignment.CenterVertically){Switch(checked=enabled,onCheckedChange={enabled=it});Text(" Usar servidor para pedidos e inventario")}
  Pill("Guardar conexión"){vm.configureServer(url,enabled){feedback=it}}
  OutlinedButton(onClick={vm.testServer{feedback=it}},modifier=Modifier.fillMaxWidth()){Text("Probar conexión")}
  OutlinedButton(onClick={vm.syncProducts{feedback=it}},modifier=Modifier.fillMaxWidth()){Text("Sincronizar catálogo")}
  OutlinedButton(onClick={vm.syncOrders{feedback=it}},modifier=Modifier.fillMaxWidth()){Text("Consultar pedidos del servidor")}
  Text(if(feedback.isNotBlank())feedback else vm.serverMessage,color=Color.Gray)
  Text("Importante: el modo local no envía datos al servidor. No actives el modo conectado sin probar la URL.",color=Color.Gray)
  Pill("Cerrar sesión",onLogout)
 }
}
