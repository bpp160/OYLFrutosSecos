package com.oyl.frutossecos

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName="clients", indices=[Index(value=["alias"], unique=true)])
data class ClientRow(@PrimaryKey(autoGenerate=true) val id:Long=0, val name:String, val alias:String, val password:String, val phone:String, val address:String)
@Entity(tableName="products")
data class ProductRow(@PrimaryKey val id:Int, val name:String, val category:String, val description:String, val price:Int, val stock:Int, val grams:Int=250, val active:Boolean=true, val photo:String="")
@Entity(tableName="cart", primaryKeys=["clientId","productId"])
data class CartRow(val clientId:Long,val productId:Int,val quantity:Int)
@Entity(tableName="orders")
data class OrderRow(@PrimaryKey(autoGenerate=true) val id:Long=0,val clientId:Long,val total:Int,val status:String="Pendiente de validación",val delivery:String="Retiro en tienda",val createdAt:Long=System.currentTimeMillis(),val serverId:Long?=null)
@Entity(tableName="order_lines")
data class OrderLineRow(@PrimaryKey(autoGenerate=true) val id:Long=0,val orderId:Long,val productId:Int,val name:String,val quantity:Int,val unitPrice:Int)
@Entity(tableName="notices")
data class NoticeRow(@PrimaryKey(autoGenerate=true) val id:Long=0,val clientId:Long,val title:String,val message:String,val createdAt:Long=System.currentTimeMillis())

@Entity(tableName="stock_movements")
data class StockMovementRow(@PrimaryKey(autoGenerate=true) val id:Long=0, val productId:Int, val productName:String, val delta:Int, val createdAt:Long=System.currentTimeMillis(), val pendingSync:Boolean=true)

@Dao interface StoreDao {
 @Query("SELECT * FROM clients WHERE alias=:alias LIMIT 1") suspend fun client(alias:String):ClientRow?
 @Query("SELECT * FROM clients WHERE id=:id LIMIT 1") suspend fun clientById(id:Long):ClientRow?
 @Insert suspend fun addClient(row:ClientRow):Long
 @Query("UPDATE clients SET name=:name, phone=:phone, address=:address WHERE id=:id") suspend fun updateProfile(id:Long,name:String,phone:String,address:String)
 @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC LIMIT 100") fun movements():Flow<List<StockMovementRow>>
 @Insert suspend fun addMovement(row:StockMovementRow)
 @Query("UPDATE products SET stock=stock+:delta WHERE id=:id AND stock+:delta>=0") suspend fun applyStockDelta(id:Int,delta:Int):Int
 @Query("SELECT * FROM products ORDER BY id") fun products():Flow<List<ProductRow>>
 @Query("SELECT COUNT(*) FROM products") suspend fun productCount():Int
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun saveProduct(row:ProductRow)
 @Query("SELECT * FROM cart WHERE clientId=:id") fun cart(id:Long):Flow<List<CartRow>>
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun saveCart(row:CartRow)
 @Query("DELETE FROM cart WHERE clientId=:id AND productId=:pid") suspend fun removeCart(id:Long,pid:Int)
 @Query("DELETE FROM cart WHERE clientId=:id") suspend fun clearCart(id:Long)
 @Insert suspend fun addOrder(row:OrderRow):Long
 @Query("SELECT * FROM orders WHERE serverId=:serverId LIMIT 1") suspend fun orderByServerId(serverId:Long):OrderRow?
 @Query("UPDATE orders SET status=:status, total=:total WHERE serverId=:serverId") suspend fun updateRemoteOrder(serverId:Long,status:String,total:Int)
 @Insert suspend fun addOrderLine(row:OrderLineRow)
 @Query("SELECT * FROM orders WHERE clientId=:id ORDER BY id DESC") fun clientOrders(id:Long):Flow<List<OrderRow>>
 @Query("SELECT * FROM orders ORDER BY id DESC") fun allOrders():Flow<List<OrderRow>>
 @Query("SELECT * FROM order_lines WHERE orderId=:id") suspend fun orderLines(id:Long):List<OrderLineRow>
 @Query("UPDATE orders SET status=:status WHERE id=:id") suspend fun status(id:Long,status:String)
 @Insert suspend fun addNotice(row:NoticeRow)
 @Query("SELECT * FROM notices WHERE clientId=:id OR clientId=0 ORDER BY id DESC") fun notices(id:Long):Flow<List<NoticeRow>>
}
@Database(entities=[ClientRow::class,ProductRow::class,CartRow::class,OrderRow::class,OrderLineRow::class,NoticeRow::class,StockMovementRow::class],version=3,exportSchema=false)
abstract class OylDatabase:RoomDatabase() {
 abstract fun dao():StoreDao
 companion object {
  private val MIGRATION_1_2 = object:Migration(1,2) {
    override fun migrate(db:SupportSQLiteDatabase) {
      db.execSQL("CREATE TABLE IF NOT EXISTS `stock_movements` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `productId` INTEGER NOT NULL, `productName` TEXT NOT NULL, `delta` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `pendingSync` INTEGER NOT NULL)")
    }
  }
  private val MIGRATION_2_3 = object:Migration(2,3) {
   override fun migrate(db:SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE orders ADD COLUMN serverId INTEGER")
   }
  }
  @Volatile private var db:OylDatabase?=null
  fun get(context:Context):OylDatabase = db ?: synchronized(this) { db ?: Room.databaseBuilder(context.applicationContext,OylDatabase::class.java,"oyl_local.db").addMigrations(MIGRATION_1_2,MIGRATION_2_3).build().also{db=it} }
 }
}
