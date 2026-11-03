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


@Composable fun QRPage(onFound:(String)->Unit){
 val context=LocalContext.current
 var scanned by remember { mutableStateOf("") }
 var manual by remember { mutableStateOf("") }
 var busy by remember { mutableStateOf(false) }
 val options=remember { GmsBarcodeScannerOptions.Builder()
   .setBarcodeFormats(Barcode.FORMAT_QR_CODE).enableAutoZoom().build() }
 val scanner=remember(context) { GmsBarcodeScanning.getClient(context,options) }
 Column(Modifier.fillMaxSize().padding(20.dp),
   verticalArrangement=Arrangement.spacedBy(12.dp),
   horizontalAlignment=Alignment.CenterHorizontally){
   Heading("Escáner QR de productos","Apunta la cámara al QR del artículo")
   Text("▣",style=MaterialTheme.typography.displayLarge,color=Green)
   Pill(if(busy)"Abriendo escáner…" else "Escanear QR"){
     if(!busy){
       busy=true
       scanner.startScan()
         .addOnSuccessListener{barcode->
           val value=barcode.rawValue
           if(!value.isNullOrBlank()){scanned=value;onFound(value)}
           else Toast.makeText(context,"QR vacío",Toast.LENGTH_SHORT).show()
         }
         .addOnFailureListener{
           Toast.makeText(context,"No se pudo escanear: ${it.localizedMessage}",Toast.LENGTH_LONG).show()
         }
         .addOnCompleteListener{busy=false}
     }
   }
   Text("El escáner utiliza Google Play Services. Requiere conexión inicial para descargar el módulo si aún no está instalado.",color=Color.Gray)
   Input(manual,{manual=it},"ID de producto (1 a 6)")
   OutlinedButton(onClick={onFound(manual)},modifier=Modifier.fillMaxWidth()){Text("Consultar código manual")}
   if(scanned.isNotBlank()) Text("Código leído: $scanned")
 }
}

