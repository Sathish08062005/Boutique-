package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Design::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun designDao(): DesignDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ziva_boutique_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.designDao()
                    populateDatabase(dao)
                }
            }
        }

        suspend fun populateDatabase(dao: DesignDao) {
            // Initial Preset Luxury Designs to showcase boutique features immediately!
            val presets = listOf(
                Design(
                    title = "Royal Banarasi Zari Silk Saree",
                    category = "Sarees",
                    description = "Pure katan silk with intricate handwoven gold brocade zari work across the custom border and pallu. Crafted by master artisans in Varanasi. A timeless heritage piece for grand celebrations.",
                    imageUrls = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?auto=format&fit=crop&w=800&q=80,https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3, // 3 days ago
                    isFeatured = true
                ),
                Design(
                    title = "Emerald Green Organza Saree",
                    category = "Sarees",
                    description = "Delicate sheer organza saree in emerald green featuring hand-painted floral motifs and an exquisite scalloped hand-embroidered border. Includes a matching raw silk blouse piece.",
                    imageUrls = "https://images.unsplash.com/photo-1617627143750-d86bc21e42bb?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 1, // 1 day ago
                    isFeatured = true
                ),
                Design(
                    title = "Crimson Heritage Bridal Lehenga",
                    category = "Bridal Wear",
                    description = "Vibrant crimson red velvet bridal lehenga heavily adorned with intricate antique dori, fine metal sequins, and traditional gold zardozi embroidery. Accompanied by a designer sweetheart choli and double sheer net dupattas.",
                    imageUrls = "https://images.unsplash.com/photo-1595777457583-95e059d581b8?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5, // 5 days ago
                    isFeatured = true
                ),
                Design(
                    title = "Grand Ivory Fusion Wedding Gown",
                    category = "Bridal Wear",
                    description = "Exquisite pearl-white premium georgette bridal fusion gown with semi-transparent net neckline. Highlighted with luxury glass beads, handpicked pearls, and dainty silver gota work for a contemporary modern bride.",
                    imageUrls = "https://images.unsplash.com/photo-1605001011156-cbf0b0f67a51?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 2, // 2 hours ago
                    isFeatured = false
                ),
                Design(
                    title = "Blossom Pink Festive Lehenga",
                    category = "Lehengas",
                    description = "Light pastel blossom pink silk lehenga featuring circular flares with delicate silver foil prints, handcrafted mirrors, and exquisite dori embroidery. Elegant, airy, and perfect for your sangeet night.",
                    imageUrls = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 10, // 10 days ago
                    isFeatured = true
                ),
                Design(
                    title = "Mirage Blue Satin Silk Lehenga",
                    category = "Lehengas",
                    description = "Premium double-layer satin silk lehenga in deep sapphire blue. Outfitted with masterfully crafted gota border elements and an matching gold brocade designer choli.",
                    imageUrls = "https://images.unsplash.com/photo-1612459284970-e8f027596582?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 18, // 18 hours ago
                    isFeatured = false
                ),
                Design(
                    title = "Traditional Patiala Salwar Suit",
                    category = "Salwars",
                    description = "Traditional silk-georgette neck salwar kameez outfit. Highlighted with vibrant hand-stitched floral Phulkari threadwork on the high-quality chiffon dupatta. Breathable, comfortable luxury.",
                    imageUrls = "https://images.unsplash.com/photo-1609357518652-6cf0416f0cbe?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 4, // 4 days ago
                    isFeatured = false
                ),
                Design(
                    title = "Maggam Back-Neck Designer Blouse",
                    category = "Blouse Designs",
                    description = "Custom couture bridal blouse in pure dupion silk. Boasts an elegant keyhole back neck opening adorned with master-crafted heavy maggam, colorful stones, fine beads, and signature gold thread linings.",
                    imageUrls = "https://images.unsplash.com/photo-1621184455862-c163dfb30e0f?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 12, // 12 hours ago
                    isFeatured = true
                ),
                Design(
                    title = "Mini Heritage Silk Sherwani",
                    category = "Kids Collection",
                    description = "Charming children's ethnic set with a lightweight jacquard silk sherwani and cotton-silk breezy pajama trousers. Features child-safety snap button fasteners and soft velvet collars for itch-free play.",
                    imageUrls = "https://images.unsplash.com/photo-1519689680058-324335c77eba?auto=format&fit=crop&w=800&q=80",
                    uploadDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7, // 7 days ago
                    isFeatured = false
                )
            )
            for (design in presets) {
                dao.insertDesign(design)
            }
        }
    }
}
