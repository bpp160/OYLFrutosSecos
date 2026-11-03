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


val Green=Color(0xFF17683D)
val Light=Color(0xFFF2F8F2)
val Orange=Color(0xFFE7A443)
fun money(n:Int)="$"+NumberFormat.getIntegerInstance(Locale("es","CL")).format(n)
@Composable fun Heading(title:String,sub:String="") {Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold,color=Green);if(sub.isNotBlank())Text(sub,color=Color.Gray)}
@Composable fun Pill(label:String,action:()->Unit){Button(onClick=action,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)){Text(label)}}
@Composable fun Input(value:String,on:(String)->Unit,label:String,secret:Boolean=false,error:String?=null){OutlinedTextField(value=value,onValueChange=on,label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=true,isError=error!=null,supportingText=if(error!=null){{Text("⚠ $error")}}else null,trailingIcon=if(error!=null){{Text("⚠",color=MaterialTheme.colorScheme.error)}}else null,visualTransformation=if(secret)PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None)}
@Composable fun Panel(content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth().padding(vertical=6.dp),shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)}}
