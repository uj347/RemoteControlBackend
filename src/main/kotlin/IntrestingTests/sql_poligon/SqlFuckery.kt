package IntrestingTests.sql_poligon

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariDataSource
import org.h2.security.auth.H2AuthConfig
import remotecontrolbackend.command_invoker_part.command_hierarchy.mocks.MockCommand
import remotecontrolbackend.command_invoker_part.command_repo.createReference
import remotecontrolbackend.database.H2Database
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.DriverManager
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists

fun main(){

//    val source=HikariDataSource()
//    source.clos
//    val h2testpath = Paths.get("J:\\UjTrash\\h2test")
//    val testDbPswd="azaza"
//    val testUserPswd="uzaza"
//    if(!h2testpath.exists()){
//        Files.createDirectories(h2testpath)
//    }
//
//    Files.newDirectoryStream(h2testpath).filter{it.fileName.toString().contains("uj_db")}.forEach{it.deleteExisting()}
//
//    source.jdbcUrl="jdbc:h2:file:$h2testpath/uj_db;USER=uj;PASSWORD=$testDbPswd $testUserPswd;MODE=MYSQL;DATABASE_TO_LOWER=TRUE"
//    val db=H2Database.invoke(source.asJdbcDriver())
//    db.h2Queries.
//    db.h2Queries.insertOrUpdate("cooocooo","zozozozo")
//    db.h2Queries.selectAll().executeAsList().forEach({it-> println(it)})
//    H2Database.
}
//fun mainMySql(){
//    val dataSource=HikariDataSource()
//    dataSource.jdbcUrl="jdbc:mysql://localhost:3306/remote-control"
//    dataSource.username="root"
//    dataSource.password="root"
//   val driver= dataSource.asJdbcDriver()
//    val db= MySqlDatabase.invoke(driver)
//    val testBooba=Booba().also{it.boobaName="bo"}
//    db.my_sql_testQueries.createBoobaTaleIfNotExists()
//    db.my_sql_testQueries.insertBooba(testBooba.boobaName!!)
//    db.my_sql_testQueries.selectAll().executeAsList().forEach{ println(it.name)}
//}
//
//fun main(){
//    val dataSource=HikariDataSource()
//val h2testpath = Paths.get("J:\\UjTrash\\h2test")
//    val testDbPswd="azaza"
//    val testUserPswd="uzaza"
//    if(!h2testpath.exists()){
//        Files.createDirectories(h2testpath)
//    }
//
//    dataSource.jdbcUrl="jdbc:h2:file:$h2testpath/uj_db;CIPHER=AES;USER=uj;PASSWORD=$testDbPswd $testUserPswd;MODE=MYSQL;DATABASE_TO_LOWER=TRUE"
////    dataSource.username="root"
////    dataSource.password="root"
//    val db=H2Database.invoke(dataSource.asJdbcDriver())
//    val testBooba=Booba().also{it.boobaName="bo"}
//    db.h2_testQueries.createBoobaTaleIfNotExists()
//    db.h2_testQueries.insertBooba(testBooba.boobaName!!)
//    db.h2_testQueries.selectAll().executeAsList().forEach{ println(it.name)}
//
//}
//class SqlFuckery