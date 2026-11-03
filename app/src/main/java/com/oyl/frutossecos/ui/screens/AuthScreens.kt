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


@Composable fun Welcome(onLogin:()->Unit,onCatalog:()->Unit,onMap:()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally){Text("🌿",style=MaterialTheme.typography.displayLarge);Text("OYL",style=MaterialTheme.typography.displayLarge,color=Green,fontWeight=FontWeight.Black);Text("FRUTOS SECOS",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.height(20.dp));Text("Productos naturales más cerca de ti",textAlign=TextAlign.Center);Spacer(Modifier.height(28.dp));Panel{listOf("🥜 Catálogo actualizado","🛒 Pedidos online","📍 Retiros y despachos","🌱 Productos saludables").forEach{Text(it)}};Pill("Comenzar",onLogin);OutlinedButton(onClick=onLogin,modifier=Modifier.fillMaxWidth()){Text("Ya tengo una cuenta")};TextButton(onClick=onCatalog){Text("Ver catálogo de muestra")};TextButton(onClick=onMap){Text("Ubicación de tienda")}}}
@Composable fun Register(onBack:()->Unit,onSave:(String,String,String,String,String)->Unit){
 var name by remember{mutableStateOf("")};var alias by remember{mutableStateOf("")}
 var pass by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")}
 var address by remember{mutableStateOf("")};var submitted by remember{mutableStateOf(false)}
 val errors=if(submitted)RegistrationValidator.validate(name,alias,pass,phone,address) else emptyMap()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Heading("Crear cuenta","Registro de cliente")
  Input(name,{name=it},"Nombre completo",error=errors["name"])
  Input(alias,{alias=it},"Alias de acceso",error=errors["alias"])
  Input(pass,{pass=it},"Contraseña",secret=true,error=errors["password"])
  Input(phone,{phone=it},"Teléfono",error=errors["phone"])
  Input(address,{address=it},"Dirección",error=errors["address"])
  Pill("Crear cuenta"){submitted=true;if(RegistrationValidator.validate(name,alias,pass,phone,address).isEmpty())onSave(name.trim(),alias.trim(),pass,phone.trim(),address.trim())}
  TextButton(onClick=onBack){Text("Volver a iniciar sesión")}
 }
}
@Composable fun Login(onRegister:()->Unit,onLogin:(String,String,Boolean)->Unit){
 var admin by remember{mutableStateOf(false)};var alias by remember{mutableStateOf("")}
 var pass by remember{mutableStateOf("")};var submitted by remember{mutableStateOf(false)}
 val errors=if(submitted)RegistrationValidator.validateLogin(alias,pass) else emptyMap()
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally){
  Text("🌰",style=MaterialTheme.typography.displayLarge);Heading("Iniciar sesión")
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(!admin,onClick={admin=false},label={Text("Cliente")});FilterChip(admin,onClick={admin=true},label={Text("Administrador")})}
  Input(alias,{alias=it},"Alias",error=errors["alias"])
  Input(pass,{pass=it},"Contraseña",secret=true,error=errors["password"])
  Pill("Iniciar sesión"){submitted=true;if(RegistrationValidator.validateLogin(alias,pass).isEmpty())onLogin(alias.trim(),pass,admin)}
  if(admin)Text("Demo: alias admin / contraseña admin123",color=Color.Gray)
  TextButton(onClick=onRegister){Text("¿No tienes cuenta? Regístrate")}
 }
}
