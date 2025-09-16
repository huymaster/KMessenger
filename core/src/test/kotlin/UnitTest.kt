import java.sql.DriverManager
import kotlin.test.Test

class UnitTest {
    @Test
    fun testConvertable() {
        val conn = DriverManager.getConnection("jdbc:cassandra://localhost:9042")
        println(conn)
    }
}