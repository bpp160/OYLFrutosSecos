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


enum class Screen(val title:String){WELCOME("Bienvenida"),REGISTER("Crear cuenta"),LOGIN("Iniciar sesión"),HOME("Inicio"),MAP("Mapa de tiendas"),CATALOG("Catálogo"),DETAIL("Detalle del producto"),QR("Escáner QR"),CART("Mi carrito"),CHECKOUT("Confirmar pedido"),MANAGE("Gestión de productos"),INVENTORY("Inventario"),ORDERS("Mis pedidos"),NOTICES("Notificaciones"),PROFILE("Mi perfil")}

@Composable fun OylApp(vm:OylViewModel){
 var screen by remember { mutableStateOf(if(vm.userId>=0)Screen.HOME else Screen.WELCOME) }
 var selected by remember { mutableIntStateOf(1) }
 var orderConfirmed by remember { mutableLongStateOf(-1) }
 val products by vm.products.collectAsState()
 val cart by vm.cart.collectAsState()
 val orders by vm.orders.collectAsState()
 val notices by vm.notices.collectAsState()
 val context=LocalContext.current
 val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
 LaunchedEffect(vm.userId) {
     if (vm.userId >= 0 && Build.VERSION.SDK_INT >= 33 &&
         ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
         notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
 }
 LaunchedEffect(notices,vm.userId) {
     if(vm.userId < 0 || notices.isEmpty()) return@LaunchedEffect
     val prefs=context.getSharedPreferences("oyl_notifications",android.content.Context.MODE_PRIVATE)
     val key="last_${vm.userId}_${vm.isAdmin}"
     val last=prefs.getLong(key,-1L)
     if(last == -1L) { prefs.edit().putLong(key,notices.maxOf { it.id }).apply() }
     else {
         notices.filter { it.id>last }.sortedBy { it.id }.forEach { notifyOyl(context,it) }
         prefs.edit().putLong(key,notices.maxOf { it.id }).apply()
     }
 }
 fun go(s:Screen){screen=s}
 fun toast(msg:String){Toast.makeText(context,msg,Toast.LENGTH_SHORT).show()}
 val nav= listOf(Screen.HOME,Screen.MAP,Screen.QR,Screen.ORDERS,Screen.PROFILE)
 MaterialTheme(colorScheme=lightColorScheme(primary=Green,secondary=Orange,background=Light,surface=Color.White)){
 Scaffold(topBar={if(screen !in listOf(Screen.WELCOME,Screen.LOGIN,Screen.REGISTER)){
  Surface(color=Green){Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text("OYL  •  ${screen.title}",color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text(if(vm.isAdmin)"ADMIN" else "CLIENTE",color=Color.White)}}
 }},bottomBar={if(vm.userId>=0&&screen !in listOf(Screen.WELCOME,Screen.LOGIN,Screen.REGISTER)){
 NavigationBar {nav.forEachIndexed { i,s -> NavigationBarItem(selected=screen==s,onClick={go(s)},icon={Text(listOf("⌂","⌖","▣","▤","♙")[i])},label={Text(listOf("Inicio","Mapa","QR","Pedidos","Perfil")[i])}) }}
 }}) {pad-> Column(Modifier.fillMaxSize().padding(pad)){
 AnimatedContent(targetState=screen,label="screens") { target->
 when(target){
 Screen.WELCOME -> Welcome(onLogin={go(Screen.LOGIN)},onCatalog={go(Screen.CATALOG)},onMap={go(Screen.MAP)})
 Screen.REGISTER -> Register(onBack={go(Screen.LOGIN)},onSave={n,a,p,t,d->vm.register(n,a,p,t,d){toast(it);if(it.startsWith("Cuenta"))go(Screen.LOGIN)}})
 Screen.LOGIN -> Login(onRegister={go(Screen.REGISTER)},onLogin={a,p,admin->vm.login(a,p,admin){ok,msg->if(ok)go(Screen.HOME)else toast(msg)}})
 Screen.HOME -> Dashboard(vm,products,cart,orders,onGo=::go)
 Screen.MAP -> StoreMapPage()
 Screen.CATALOG -> Catalog(products,vm.isAdmin,onSelect={selected=it.id;go(Screen.DETAIL)},onAdd={product->vm.addCart(product){msg->toast(msg)}})
 Screen.DETAIL -> ProductDetail(products.find{it.id==selected},vm.isAdmin,onBack={go(Screen.CATALOG)},onAdd={p,q->vm.addCart(p,q){toast(it)}})
 Screen.QR -> QRPage(onFound={code->val id=code.substringAfterLast("/").toIntOrNull();if(id!=null&&products.any{it.id==id}){selected=id;go(Screen.DETAIL)}else toast("QR no asociado a un producto")})
 Screen.CART -> CartPage(products,cart,onQuantity={p,q->vm.setCart(p,q)},onNext={go(Screen.CHECKOUT)})
 Screen.CHECKOUT -> CheckoutPage(products,cart,orderConfirmed,onConfirm={mode->vm.placeOrder(mode){id->if(id>0)orderConfirmed=id else toast(if(id == -2L)"Stock insuficiente" else if(id == -3L)"No se pudo enviar el pedido al servidor. Revisa Perfil" else "Carrito vacío")}},onOrders={orderConfirmed=-1;go(Screen.ORDERS)})
 Screen.MANAGE -> ManagePage(products,onSave={vm.saveProduct(it);toast("Producto guardado")})
 Screen.INVENTORY -> InventoryPage(products,vm.movements.collectAsState().value,onChange={p,d->vm.stock(p,d)})
 Screen.ORDERS -> OrdersPage(orders,vm.isAdmin,onStatus={o,s->vm.changeStatus(o,s)})
 Screen.NOTICES -> NoticesPage(notices)
 Screen.PROFILE -> ProfilePage(vm,onGo=::go,onLogout={vm.logout();go(Screen.WELCOME)})
 }
 }
 }}
 }
}
