package com.oyl.frutossecos

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

/** Ubicaciones ficticias de demostración; reemplazar por sucursales verificadas. */
data class StoreLocation(
    val id: Int, val name: String, val address: String,
    val hours: String, val description: String,
    val latitude: Double, val longitude: Double
)

object StoreRepository {
    val stores = listOf(
        StoreLocation(1, "OYL Centro (demo)", "Sector Plaza de Armas, Santiago", "Horario de demostración: 09:00–18:00", "Punto referencial, no es una tienda verificada.", -33.4372, -70.6506),
        StoreLocation(2, "OYL Providencia (demo)", "Sector Metro Los Leones, Providencia", "Horario de demostración: 09:00–18:00", "Punto referencial, no es una tienda verificada.", -33.4208, -70.6083),
        StoreLocation(3, "OYL Ñuñoa (demo)", "Sector Plaza Ñuñoa, Ñuñoa", "Horario de demostración: 09:00–18:00", "Punto referencial, no es una tienda verificada.", -33.4556, -70.5977)
    )
}

/** Mapa Leaflet/OpenStreetMap con marcadores y ventanas informativas. Requiere Internet. */
@Composable
fun StoreMapPage() {
    val context = LocalContext.current
    var selected by remember { mutableIntStateOf(1) }
    val locations = remember { StoreRepository.stores }
    val current = locations.first { it.id == selected }
    val pointsJson = remember {
        JSONArray().apply {
            locations.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id); put("name", s.name); put("address", s.address)
                    put("hours", s.hours); put("description", s.description)
                    put("lat", s.latitude); put("lng", s.longitude)
                })
            }
        }.toString()
    }
    val html = remember(pointsJson) {
        """
        <!doctype html><html lang="es"><head><meta name="viewport" content="width=device-width,initial-scale=1">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
        <style>html,body,#map{height:100%;margin:0} .leaflet-popup-content{font:14px sans-serif;line-height:1.5}</style></head>
        <body><div id="map"></div><script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <script>
        const stores=$pointsJson;
        const map=L.map('map').setView([-33.439,-70.63],12);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
          maxZoom:19,attribution:'&copy; OpenStreetMap contributors'
        }).addTo(map);
        stores.forEach(s=>{
          const popup=document.createElement('div');
          const title=document.createElement('strong');title.textContent=s.name;popup.appendChild(title);
          for(const value of [s.address,s.hours,s.description]){
            const line=document.createElement('div');line.textContent=value;popup.appendChild(line);
          }
          const marker=L.marker([s.lat,s.lng]).addTo(map).bindPopup(popup);
          marker.on('click',()=>{window.location.href='oyl-store://select/'+s.id});
        });
        </script></body></html>
        """.trimIndent()
    }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Heading("Mapa de tiendas", "Ubicaciones de demostración en Santiago")
        Text("Selecciona un marcador para ver la tienda. Estos puntos no representan sucursales reales.")
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?, request: android.webkit.WebResourceRequest?
                        ): Boolean {
                            val uri = request?.url ?: return false
                            if (uri.scheme == "oyl-store") {
                                val id = uri.lastPathSegment?.toIntOrNull()
                                if (id != null && locations.any { it.id == id }) selected = id
                                return true
                            }
                            return false
                        }
                    }
                    loadDataWithBaseURL("https://www.openstreetmap.org/", html, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Text(current.name, style=MaterialTheme.typography.titleMedium)
        Text(current.address)
        Text(current.hours)
        Text(current.description, style=MaterialTheme.typography.bodySmall)
        Button(
            onClick = {
                val uri = Uri.parse("geo:${current.latitude},${current.longitude}?q=${current.latitude},${current.longitude}")
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            },
            modifier=Modifier.fillMaxWidth()
        ) { Text("Cómo llegar") }
        Text("También puedes seleccionar una ubicación de la lista:", style=MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            locations.forEach { store ->
                TextButton(onClick={selected=store.id}, modifier=Modifier.weight(1f)) {
                    Text(store.name.substringBefore(" (demo)"), maxLines=1)
                }
            }
        }
    }
}
