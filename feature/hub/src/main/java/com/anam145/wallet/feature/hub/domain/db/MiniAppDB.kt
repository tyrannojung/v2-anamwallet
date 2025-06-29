package com.anam145.wallet.feature.hub.domain.db

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.anam145.wallet.core.common.model.MiniApp
import com.anam145.wallet.core.common.model.MiniAppType
import com.anam145.wallet.feature.hub.domain.dao.MiniAppDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [MiniApp::class], version = 1)
abstract class MiniAppDB : RoomDatabase() {
    abstract fun miniAppDao(): MiniAppDao

//
//    companion object {
//        @Volatile
//        private var INSTANCE: MiniAppDB? = null
//
//        fun getDatabase(context: Context, scope: CoroutineScope): MiniAppDB { // CoroutineScope를 인자로 받도록 수정
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    MiniAppDB::class.java,
//                    "mini_app"
//                )
//                    .fallbackToDestructiveMigration() // 앱 재시작때마다 reset
//                    .addCallback(MiniAppDatabaseCallback(scope)) // ★★★ 콜백 추가 ★★★
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//
//
//    private class MiniAppDatabaseCallback(
//        private val scope: CoroutineScope
//    ) : RoomDatabase.Callback() {
//        override fun onCreate(db: SupportSQLiteDatabase) { // 데이터베이스가 처음 생성될 때 호출됨
//            super.onCreate(db)
//            INSTANCE?.let { database ->
//                scope.launch(Dispatchers.IO) {
//                    val miniAppDao = database.miniAppDao()
//                    miniAppDao.insertMiniApp(
//                        MiniApp(
//                            appId = "app_id_1",
//                            name = "app_id_1_name",
//                            type = MiniAppType.BLOCKCHAIN,
//                            iconPath = "/path/to/app1_icon",
//                            balance = "app_id_1_balance",
//                        )
//                    )
//                }
//            }
//        }
//    }
}