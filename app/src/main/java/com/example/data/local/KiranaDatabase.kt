package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProductEntity::class, BillEntity::class, BillItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KiranaDatabase : RoomDatabase() {

    abstract fun kiranaDao(): KiranaDao

    companion object {
        @Volatile
        private var INSTANCE: KiranaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KiranaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KiranaDatabase::class.java,
                    "kirana_store.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database starts completely clean. Items are added from the Suggested Menu or manually with custom prices.
            }
        }

        suspend fun populateInitialGroceryItems(dao: KiranaDao) {
            val sampleProducts = listOf(
                ProductEntity(
                    id = "prod-rice-01",
                    nameEnglish = "Sona Masuri Rice",
                    nameTelugu = "సోనా మసూరి బియ్యం",
                    aliases = "rice, biyyam, chawal, sonamasuri, akshathalu",
                    category = "Grains",
                    unit = "kg",
                    pricePerUnit = 58.0,
                    stockQuantity = 45.0,
                    minStockThreshold = 10.0
                ),
                ProductEntity(
                    id = "prod-sugar-02",
                    nameEnglish = "Sugar",
                    nameTelugu = "పంచదార",
                    aliases = "sugar, panchadara, cheeni, sakhara",
                    category = "Essentials",
                    unit = "kg",
                    pricePerUnit = 42.0,
                    stockQuantity = 30.0,
                    minStockThreshold = 8.0
                ),
                ProductEntity(
                    id = "prod-toordal-03",
                    nameEnglish = "Toor Dal",
                    nameTelugu = "కందిపప్పు",
                    aliases = "toor dal, kandi pappu, dal, kandulu, arhar",
                    category = "Pulses",
                    unit = "kg",
                    pricePerUnit = 145.0,
                    stockQuantity = 22.0,
                    minStockThreshold = 5.0
                ),
                ProductEntity(
                    id = "prod-oil-04",
                    nameEnglish = "Sunflower Oil",
                    nameTelugu = "సన్‌ఫ్లవర్ నూనె",
                    aliases = "oil, sunflower oil, noone, cooking oil, tel",
                    category = "Oils",
                    unit = "l",
                    pricePerUnit = 135.0,
                    stockQuantity = 18.0,
                    minStockThreshold = 5.0
                ),
                ProductEntity(
                    id = "prod-milk-05",
                    nameEnglish = "Whole Milk",
                    nameTelugu = "పాలు",
                    aliases = "milk, paalu, packet milk, doodh",
                    category = "Dairy",
                    unit = "pkt",
                    pricePerUnit = 32.0,
                    stockQuantity = 15.0,
                    minStockThreshold = 4.0
                ),
                ProductEntity(
                    id = "prod-tea-06",
                    nameEnglish = "Red Label Tea",
                    nameTelugu = "టీ పొడి",
                    aliases = "tea, tea podi, chai, red label, tea powder",
                    category = "Beverages",
                    unit = "pkt",
                    pricePerUnit = 95.0,
                    stockQuantity = 20.0,
                    minStockThreshold = 5.0
                ),
                ProductEntity(
                    id = "prod-atta-07",
                    nameEnglish = "Aashirvaad Atta",
                    nameTelugu = "గోధుమ పిండి",
                    aliases = "atta, wheat, wheat flour, godhuma pindi, aashirvaad",
                    category = "Grains",
                    unit = "kg",
                    pricePerUnit = 52.0,
                    stockQuantity = 28.0,
                    minStockThreshold = 6.0
                ),
                ProductEntity(
                    id = "prod-salt-08",
                    nameEnglish = "Tata Salt",
                    nameTelugu = "టాటా ఉప్పు",
                    aliases = "salt, uppu, tata salt, namak",
                    category = "Essentials",
                    unit = "pkt",
                    pricePerUnit = 25.0,
                    stockQuantity = 35.0,
                    minStockThreshold = 10.0
                ),
                ProductEntity(
                    id = "prod-chilli-09",
                    nameEnglish = "Guntur Chilli Powder",
                    nameTelugu = "కారం పొడి",
                    aliases = "chilli powder, karam, mirchi powder, mirapakaya podi",
                    category = "Spices",
                    unit = "kg",
                    pricePerUnit = 220.0,
                    stockQuantity = 12.0,
                    minStockThreshold = 3.0
                ),
                ProductEntity(
                    id = "prod-turmeric-10",
                    nameEnglish = "Turmeric Powder",
                    nameTelugu = "పసుపు",
                    aliases = "turmeric, pasupu, haldi, turmeric powder",
                    category = "Spices",
                    unit = "kg",
                    pricePerUnit = 160.0,
                    stockQuantity = 9.0,
                    minStockThreshold = 2.0
                ),
                ProductEntity(
                    id = "prod-onions-11",
                    nameEnglish = "Fresh Onions",
                    nameTelugu = "ఉల్లిపాయలు",
                    aliases = "onions, ullipayalu, ulligadda, kanda, pyaz",
                    category = "Vegetables",
                    unit = "kg",
                    pricePerUnit = 35.0,
                    stockQuantity = 40.0,
                    minStockThreshold = 10.0
                ),
                ProductEntity(
                    id = "prod-potatoes-12",
                    nameEnglish = "Potatoes",
                    nameTelugu = "బంగాళాదుంపలు",
                    aliases = "potatoes, potato, alu, aloo, bangaladumpalu",
                    category = "Vegetables",
                    unit = "kg",
                    pricePerUnit = 30.0,
                    stockQuantity = 32.0,
                    minStockThreshold = 8.0
                ),
                ProductEntity(
                    id = "prod-moongdal-13",
                    nameEnglish = "Moong Dal",
                    nameTelugu = "పెసరపప్పు",
                    aliases = "moong dal, pesara pappu, pesarapappu, green gram",
                    category = "Pulses",
                    unit = "kg",
                    pricePerUnit = 125.0,
                    stockQuantity = 14.0,
                    minStockThreshold = 4.0
                ),
                ProductEntity(
                    id = "prod-soap-14",
                    nameEnglish = "Rin Soap Bar",
                    nameTelugu = "రిన్ సబ్బు",
                    aliases = "rin, rin soap, sabbu, laundry soap",
                    category = "Household",
                    unit = "piece",
                    pricePerUnit = 10.0,
                    stockQuantity = 45.0,
                    minStockThreshold = 12.0
                )
            )
            dao.insertProducts(sampleProducts)
        }
    }
}
