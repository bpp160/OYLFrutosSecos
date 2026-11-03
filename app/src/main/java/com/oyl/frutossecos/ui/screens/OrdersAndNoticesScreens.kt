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


@Composable fun OrdersPage(orders:List<OrderRow>,admin:Boolean,onStatus:(OrderRow,String)->Unit){var filter by remember{mutableStateOf("Todos")};Column(Modifier.padding(16.dp)){Heading(if(admin)"Gestión de pedidos" else "Mis pedidos");Row(Modifier.horizontalScroll(rememberScrollState())){listOf("Todos","Pendiente","Confirmado","Preparado","Entregado").forEach{FilterChip(selected=filter==it,onClick={filter=it},label={Text(it)})}};LazyColumn{items(orders.filter{filter=="Todos"||it.status.startsWith(filter)}){o->Panel{Text("Pedido #${o.id}",fontWeight=FontWeight.Bold);Text("${money(o.total)} · ${o.delivery}");Text("Estado: ${o.status}",color=Green);if(admin){Row(Modifier.horizontalScroll(rememberScrollState())){listOf("Confirmado","Preparado","Entregado").forEach{s->TextButton(onClick={onStatus(o,s)}){Text(s)}}}}}}}}}
@Composable fun NoticesPage(notices:List<NoticeRow>){LazyColumn(Modifier.padding(16.dp)){item{Heading("Notificaciones")};items(notices){n->Panel{Text("🔔 ${n.title}",fontWeight=FontWeight.Bold);Text(n.message)}};if(notices.isEmpty())item{Text("No hay avisos todavía")}}}
