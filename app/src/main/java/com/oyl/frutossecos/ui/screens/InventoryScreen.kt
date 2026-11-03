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


@Composable fun InventoryPage(products:List<ProductRow>,movements:List<StockMovementRow>,onChange:(ProductRow,Int)->Unit){
    Column(Modifier.padding(16.dp)){
        Heading("Gestión de inventario","Los movimientos muestran si se registraron en el servidor")
        LazyColumn{
            items(products){p->
                Panel{
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Column(Modifier.weight(1f)){
                            Text(p.name,fontWeight=FontWeight.Bold)
                            Text("Stock actual: ${p.stock}"+(if(p.stock<10)" · BAJO" else ""))
                        }
                        FilledTonalButton(onClick={onChange(p,-1)}){Text("−1")}
                        Spacer(Modifier.width(6.dp))
                        FilledTonalButton(onClick={onChange(p,1)}){Text("+1")}
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                Heading("Últimos movimientos")
            }
            items(movements){m->
                Text("${m.productName}: ${if(m.delta>0) "+" else ""}${m.delta} · ${if(m.pendingSync) "local" else "enviado"}",modifier=Modifier.padding(8.dp))
            }
        }
    }
}
