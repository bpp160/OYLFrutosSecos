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


@Composable fun Dashboard(vm:OylViewModel,products:List<ProductRow>,cart:List<CartRow>,orders:List<OrderRow>,onGo:(Screen)->Unit){LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Heading("¡Hola, ${vm.userName}!","Qué bueno verte de nuevo")};item{Panel{Text("🌰 Compra saludable, vida mejor",fontWeight=FontWeight.Bold,color=Green);Text("Frutos secos, semillas y deshidratados para ti")}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick("🥜","Catálogo",Modifier.weight(1f)){onGo(Screen.CATALOG)};Quick("📍","Mapa",Modifier.weight(1f)){onGo(Screen.MAP)}}};item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Quick("📦","Pedidos",Modifier.weight(1f)){onGo(Screen.ORDERS)};Quick("🔔","Avisos",Modifier.weight(1f)){onGo(Screen.NOTICES)}}};if(vm.isAdmin){item{Panel{Text("Panel administrador",fontWeight=FontWeight.Bold);Text("${products.count{it.stock<10}} productos con stock bajo · ${orders.count{it.status.startsWith("Pendiente")}} pedidos pendientes");Pill("Gestionar productos"){onGo(Screen.MANAGE)};Pill("Gestionar inventario"){onGo(Screen.INVENTORY)}}}}else{item{Panel{Text("Carrito: ${cart.sumOf{it.quantity}} artículos");Pill("Ir a mi carrito"){onGo(Screen.CART)}}}};item{Heading("Productos destacados")};items(products.take(4)){ProductCard(it,onOpen={onGo(Screen.CATALOG)},onAdd={onGo(Screen.CATALOG)})}}}
@Composable fun Quick(icon:String,title:String,modifier:Modifier=Modifier,onClick:()->Unit){Card(onClick=onClick,modifier=modifier.height(90.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(icon,style=MaterialTheme.typography.headlineSmall);Text(title,fontWeight=FontWeight.SemiBold)}}}
@Composable fun ProductCard(p:ProductRow,onOpen:()->Unit,onAdd:()->Unit){Panel{Row(verticalAlignment=Alignment.CenterVertically){ProductPhoto(p,56);Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text("${p.grams} g · ${p.category}",style=MaterialTheme.typography.bodySmall);Text(money(p.price),color=Green,fontWeight=FontWeight.Bold);Text("Stock: ${p.stock}",style=MaterialTheme.typography.labelSmall)};TextButton(onClick=onOpen){Text("Ver")};FilledIconButton(onClick=onAdd){Text("+")}}}}
@Composable fun Catalog(products:List<ProductRow>,admin:Boolean,onSelect:(ProductRow)->Unit,onAdd:(ProductRow)->Unit){var search by remember{mutableStateOf("")};var cat by remember{mutableStateOf("Todos")};Column(Modifier.padding(14.dp)){Heading("Catálogo de productos");Input(search,{search=it},"Buscar productos");Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Todos","Frutos secos","Semillas","Deshidratados").forEach{FilterChip(selected=cat==it,onClick={cat=it},label={Text(it)})}};LazyColumn{items(products.filter{it.active&&(cat=="Todos"||it.category==cat)&&it.name.contains(search,true)}){p->ProductCard(p,{onSelect(p)},{if(admin)onSelect(p)else onAdd(p)})}}}}
@Composable fun ProductPhoto(p:ProductRow,size:Int){
 val bitmap=remember(p.photo){runCatching{if(p.photo.isNotBlank()&&File(p.photo).exists()) BitmapFactory.decodeFile(p.photo) else null}.getOrNull()}
 if(bitmap!=null) Image(bitmap.asImageBitmap(),contentDescription=p.name,modifier=Modifier.size(size.dp))
 else Text(when(p.category){"Semillas"->"🌱";"Deshidratados"->"🍇";else->"🥜"},style=MaterialTheme.typography.displaySmall)
}
@Composable fun ProductDetail(p:ProductRow?,admin:Boolean,onBack:()->Unit,onAdd:(ProductRow,Int)->Unit){if(p==null){Text("Producto no encontrado");return};var qty by remember(p.id){mutableIntStateOf(1)};Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){TextButton(onClick=onBack){Text("← Catálogo")};Box(Modifier.fillMaxWidth().height(180.dp).background(Light,RoundedCornerShape(22.dp)),contentAlignment=Alignment.Center){ProductPhoto(p,145)};Heading(p.name,p.category);Text(money(p.price),style=MaterialTheme.typography.headlineSmall,color=Green);Text("${p.grams} g · ${p.description}");Text(if(p.stock>0)"● En stock (${p.stock})" else "Agotado",color=if(p.stock>0)Green else Color.Red);if(!admin){Row(verticalAlignment=Alignment.CenterVertically){OutlinedButton(onClick={qty=(qty-1).coerceAtLeast(1)}){Text("−")};Text("  $qty  ");OutlinedButton(onClick={qty=(qty+1).coerceAtMost(p.stock.coerceAtLeast(1))}){Text("+")}};Pill("Agregar al carrito"){onAdd(p,qty)}}else Text("Administración: usa Gestión de productos para editar")}}
@Composable fun CartPage(products:List<ProductRow>,cart:List<CartRow>,onQuantity:(ProductRow,Int)->Unit,onNext:()->Unit){val byId=products.associateBy{it.id};val total=cart.sumOf{(byId[it.productId]?.price?:0)*it.quantity};LazyColumn(Modifier.padding(16.dp)){item{Heading("Mi carrito (${cart.sumOf{it.quantity}})")};items(cart){l->val p=byId[l.productId];if(p!=null)Panel{Row(verticalAlignment=Alignment.CenterVertically){Text("🥜",style=MaterialTheme.typography.headlineLarge);Column(Modifier.weight(1f)){Text(p.name,fontWeight=FontWeight.Bold);Text(money(p.price))};TextButton(onClick={onQuantity(p,l.quantity-1)}){Text("−")};Text("${l.quantity}");TextButton(onClick={onQuantity(p,l.quantity+1)}){Text("+")}}}};item{Panel{Text("Total: ${money(total)}",fontWeight=FontWeight.Bold);Pill("Continuar compra",onNext)}}}}
@Composable fun CheckoutPage(products:List<ProductRow>,cart:List<CartRow>,confirmed:Long,onConfirm:(String)->Unit,onOrders:()->Unit){var mode by remember{mutableStateOf("Retiro en tienda")};Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally){if(confirmed>0){Text("🎉",style=MaterialTheme.typography.displayLarge);Heading("¡Pedido registrado!");Text("N.º de solicitud: #$confirmed");Text("Estado: Pendiente de validación");Text("Revisa en Perfil si el pedido se envió al servidor o quedó guardado localmente.",color=Color.Gray);Pill("Ver mis pedidos",onOrders)}else{Heading("Confirmación del pedido");Text("Total: ${money(cart.sumOf{l->(products.find{it.id==l.productId}?.price?:0)*l.quantity})}");listOf("Retiro en tienda","Entrega a domicilio").forEach{Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=mode==it,onClick={mode=it});Text(it)}};Pill("Confirmar solicitud"){onConfirm(mode)};Text("Con servidor activado, Spring Boot validará precios y stock. Sin servidor, se guarda solo en el teléfono.",color=Color.Gray)}}}
@Composable fun ManagePage(products:List<ProductRow>,onSave:(ProductRow)->Unit){var editing by remember{mutableIntStateOf(0)};var name by remember{mutableStateOf("")};var cat by remember{mutableStateOf("Frutos secos")};var desc by remember{mutableStateOf("")};var price by remember{mutableStateOf("")};var stock by remember{mutableStateOf("")};var active by remember{mutableStateOf(true)};var photo by remember{mutableStateOf("")}
 val context=LocalContext.current
 val camera=rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()){bitmap->
 if(bitmap!=null){val file=File(context.filesDir,"product_${System.currentTimeMillis()}.jpg");file.outputStream().use{bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,85,it)};photo=file.absolutePath}
 }
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){if(it)camera.launch(null)}
 Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Heading("Gestión de productos","Administrador");products.forEach{p->Panel{Row(verticalAlignment=Alignment.CenterVertically){Text(p.name,modifier=Modifier.weight(1f));Text(money(p.price));TextButton(onClick={editing=p.id;name=p.name;cat=p.category;desc=p.description;price=p.price.toString();stock=p.stock.toString();active=p.active;photo=p.photo}){Text("Editar")}}}};HorizontalDivider();Heading(if(editing==0)"Nuevo producto" else "Editar producto");Input(name,{name=it},"Nombre");Input(cat,{cat=it},"Categoría");Input(desc,{desc=it},"Descripción");Input(price,{price=it},"Precio");Input(stock,{stock=it},"Stock");OutlinedButton(onClick={permission.launch(Manifest.permission.CAMERA)}){Text("📷 Fotografiar producto")};if(photo.isNotBlank())Text("Fotografía guardada localmente");Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=active,onCheckedChange={active=it});Text("Producto activo")};Pill("Guardar producto"){val pr=price.toIntOrNull();val st=stock.toIntOrNull();if(name.isNotBlank()&&pr!=null&&pr>=0&&st!=null&&st>=0){onSave(ProductRow(if(editing==0)(products.maxOfOrNull{it.id}?:0)+1 else editing,name,cat,desc,pr,st,active=active,photo=photo));editing=0;name="";desc="";price="";stock="";photo=""}}}}
