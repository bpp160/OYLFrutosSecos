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


@OptIn(ExperimentalCoroutinesApi::class)
class OylViewModel(private val dao:StoreDao,private val prefs:android.content.SharedPreferences):ViewModel(){
 var serverUrl by mutableStateOf(prefs.getString("serverUrl", "http://10.0.2.2:8080/") ?: "http://10.0.2.2:8080/"); private set
 var online by mutableStateOf(prefs.getBoolean("online",false)); private set
 var serverMessage by mutableStateOf("Modo local"); private set
 private var api:BackendApi? = null
 private fun remote():BackendApi {
  val url=serverUrl.trim().let { if(it.endsWith("/"))it else "$it/" }
  return RetrofitFactory.create(url)
 }
 fun configureServer(url:String,enabled:Boolean,done:(String)->Unit){
  val normalized=url.trim().let { if(it.endsWith("/"))it else "$it/" }
  if(!normalized.startsWith("http://") && !normalized.startsWith("https://")){done("URL inválida: comienza con http:// o https://");return}
  serverUrl=normalized;online=enabled;api=null
  prefs.edit { putString("serverUrl",normalized);putBoolean("online",enabled) }
  serverMessage=if(enabled)"Conexión activada: comprueba el servidor" else "Modo local"
  done(serverMessage)
 }
 fun testServer(done:(String)->Unit){viewModelScope.launch {
  val result=runCatching { withContext(Dispatchers.IO) { remote().products() } }
  val msg=result.fold({"Servidor disponible: ${it.size} productos"},{"Sin conexión: ${it.message ?: "error de red"}"})
  serverMessage=msg;done(msg)
 }}
 fun syncProducts(done:(String)->Unit){viewModelScope.launch {
  if(!online){done("Activa conexión en Perfil");return@launch}
  val result=runCatching { withContext(Dispatchers.IO) { remote().products() } }
  result.onSuccess { rows ->
   val local=products.value.associateBy{it.id}
   rows.forEach { item ->
    val existing=local[item.id]
    dao.saveProduct(ProductRow(item.id,item.name,item.category,existing?.description?:"Producto del servidor",item.price,item.stock,existing?.grams?:250,existing?.active?:true,existing?.photo?:""))
   }
   serverMessage="Catálogo sincronizado";done("${rows.size} productos sincronizados")
  }.onFailure {serverMessage="Sin conexión";done("No se sincronizó: ${it.message}")}
 }}

 val products=dao.products().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 var userId by mutableLongStateOf(prefs.getLong("userId",-1)); private set
 var userName by mutableStateOf(prefs.getString("name","")?:""); private set
 var isAdmin by mutableStateOf(prefs.getBoolean("admin",false)); private set
 val cart:StateFlow<List<CartRow>> = snapshotFlow { userId }.flatMapLatest { if(it>0) dao.cart(it) else flowOf(emptyList()) }.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val orders:StateFlow<List<OrderRow>> = snapshotFlow { userId to isAdmin }.flatMapLatest { (id,admin) -> if(admin) dao.allOrders() else if(id>0) dao.clientOrders(id) else flowOf(emptyList()) }.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val movements=dao.movements().stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 val notices:StateFlow<List<NoticeRow>> = snapshotFlow { userId }.flatMapLatest { dao.notices(if(it>0) it else 0) }.stateIn(viewModelScope,SharingStarted.Eagerly,emptyList())
 init { viewModelScope.launch { if(dao.productCount()==0) initial.forEach {dao.saveProduct(it)} } }
 fun register(name:String,alias:String,pass:String,phone:String,address:String,done:(String)->Unit){
  if(RegistrationValidator.validate(name,alias,pass,phone,address).isNotEmpty()){done("Revisa los datos del formulario");return}
  viewModelScope.launch { if(dao.client(alias)!=null){done("El alias ya existe");return@launch}
   try{dao.addClient(ClientRow(name=name,alias=alias,password=PasswordHash.create(pass),phone=phone,address=address));done("Cuenta creada. Inicia sesión")}
   catch(_:Exception){done("No se pudo crear el usuario")}
  }
 }
 fun login(alias:String,pass:String,admin:Boolean,done:(Boolean,String)->Unit){viewModelScope.launch {
  if(admin){if(alias=="admin"&&pass=="admin123") { session(0,"Administrador",true);done(true,"") } else done(false,"Credenciales de demostración incorrectas")}
  else {val c=dao.client(alias);if(c!=null&&PasswordHash.matches(pass,c.password)){session(c.id,c.name,false);done(true,"")}else done(false,"Alias o contraseña incorrectos")}
 }}
 private fun session(id:Long,name:String,admin:Boolean){
  userId=id;userName=name;isAdmin=admin
  prefs.edit { putLong("userId",id); putString("name",name); putBoolean("admin",admin) }
 }
 fun logout(){session(-1,"",false)}
 fun addCart(p:ProductRow,qty:Int=1,done:(String)->Unit={}){ if(isAdmin){done("Inicia como cliente para comprar");return};viewModelScope.launch {
  val old=cart.value.find{it.productId==p.id}?.quantity?:0
  if(old+qty>p.stock){done("Stock insuficiente");return@launch}
  if(old+qty<=0)dao.removeCart(userId,p.id) else dao.saveCart(CartRow(userId,p.id,old+qty));done("Carrito actualizado")
 }}
 fun setCart(p:ProductRow,qty:Int){viewModelScope.launch { if(qty<=0)dao.removeCart(userId,p.id) else if(qty<=p.stock)dao.saveCart(CartRow(userId,p.id,qty)) }}
 fun placeOrder(delivery:String,done:(Long)->Unit){viewModelScope.launch {
  val lines=cart.value;val byId=products.value.associateBy{it.id}
  if(lines.isEmpty()){done(-1);return@launch}
  if(lines.any{(byId[it.productId]?.stock?:0)<it.quantity}){done(-2);return@launch}
  if(online){
   // En modo conectado, el servidor es la autoridad para precios y stock.
   val response=runCatching { withContext(Dispatchers.IO){remote().placeOrder(CreateOrderDto(userId,delivery,lines.map{OrderItemDto(it.productId,it.quantity)}))} }
   if(response.isFailure){serverMessage="Pedido no enviado: ${response.exceptionOrNull()?.message}";done(-3);return@launch}
   val order=response.getOrThrow()
   val id=dao.addOrder(OrderRow(clientId=userId,total=order.total,status=order.status,delivery=delivery,serverId=order.id))
   lines.forEach{l->val p=byId[l.productId]!!;dao.addOrderLine(OrderLineRow(orderId=id,productId=p.id,name=p.name,quantity=l.quantity,unitPrice=p.price))}
   dao.clearCart(userId)
   dao.addNotice(NoticeRow(clientId=userId,title="Pedido enviado",message="Pedido del servidor #${order.id}, total ${money(order.total)}"))
   syncProducts({})
   done(id)
  }else{
   val total=lines.sumOf{(byId[it.productId]?.price?:0)*it.quantity}
   val id=dao.addOrder(OrderRow(clientId=userId,total=total,delivery=delivery))
   lines.forEach{l->val p=byId[l.productId]!!;dao.addOrderLine(OrderLineRow(orderId=id,productId=p.id,name=p.name,quantity=l.quantity,unitPrice=p.price))}
   dao.clearCart(userId);dao.addNotice(NoticeRow(clientId=userId,title="Pedido local",message="Solicitud #$id guardada solo en el teléfono"));done(id)
  }
 }}
 fun syncOrders(done:(String)->Unit){viewModelScope.launch {
  if(!online){done("Activa el servidor en Perfil");return@launch}
  val result=runCatching{withContext(Dispatchers.IO){remote().orders()}}
  result.onSuccess{list->
   list.forEach { item ->
    val existing=dao.orderByServerId(item.id)
    if(existing==null)dao.addOrder(OrderRow(clientId=item.clientId,total=item.total,status=item.status,delivery=item.delivery,serverId=item.id))
    else dao.updateRemoteOrder(item.id,item.status,item.total)
   }
   done("${list.size} pedidos consultados en servidor")
  }.onFailure{done("Error al consultar pedidos: ${it.message}")}
 }}
 fun saveProfile(name:String,phone:String,address:String,done:(String)->Unit){
  if(name.isBlank()){done("Ingresa un nombre");return}
  viewModelScope.launch { if(userId>0){dao.updateProfile(userId,name.trim(),phone.trim(),address.trim());session(userId,name.trim(),false);done("Perfil actualizado") }else done("Perfil de administrador de demostración") }
 }
 fun saveProduct(p:ProductRow){viewModelScope.launch{
  if(online){
   val result=runCatching{withContext(Dispatchers.IO){remote().saveProduct(ProductDto(p.id,p.name,p.category,p.price,p.stock))}}
   result.onSuccess{dao.saveProduct(p.copy(price=it.price,stock=it.stock));serverMessage="Producto guardado en servidor"}
    .onFailure{serverMessage="No se guardó producto: ${it.message}"}
  }else dao.saveProduct(p)
 }}
 fun stock(p:ProductRow,delta:Int){viewModelScope.launch{
  if(delta==0)return@launch
  if(online){
   val result=runCatching{withContext(Dispatchers.IO){remote().changeStock(p.id,StockDto(delta))}}
   result.onSuccess{updated->
    dao.saveProduct(p.copy(stock=updated.stock))
    dao.addMovement(StockMovementRow(productId=p.id,productName=p.name,delta=delta,pendingSync=false))
    if(updated.stock<10)dao.addNotice(NoticeRow(clientId=0,title="Stock bajo",message="${p.name}: ${updated.stock} unidades"))
   }.onFailure{serverMessage="Stock no actualizado: ${it.message}"}
  }else if(dao.applyStockDelta(p.id,delta)>0){
   dao.addMovement(StockMovementRow(productId=p.id,productName=p.name,delta=delta))
   if(p.stock+delta<10)dao.addNotice(NoticeRow(clientId=0,title="Stock bajo",message="${p.name}: ${p.stock+delta} unidades"))
  }
 }}
 fun changeStatus(o:OrderRow,status:String){viewModelScope.launch{
  if(online){
   if(o.serverId==null){serverMessage="Este pedido es local; no existe en el servidor";return@launch}
   val result=runCatching {withContext(Dispatchers.IO){remote().changeStatus(o.serverId,StatusDto(status))}}
   result.onSuccess {dao.status(o.id,it.status);dao.addNotice(NoticeRow(clientId=o.clientId,title="Estado de pedido",message="Pedido servidor #${it.id}: ${it.status}"))}
    .onFailure{serverMessage="No se cambió el estado: ${it.message}"}
   return@launch
  }
  dao.status(o.id,status);dao.addNotice(NoticeRow(clientId=o.clientId,title="Estado de pedido",message="Pedido #${o.id}: $status"))
 }}
}
