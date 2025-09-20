import com.github.huymaster.textguardian.core.converter.EntityConverterFactory
import com.github.huymaster.textguardian.core.converter.UserConverter
import com.github.huymaster.textguardian.core.type.User
import com.google.gson.JsonParser
import java.util.*
import kotlin.test.Test

class UnitTest {
    @Test
    fun testConvertable() {
        val user = EntityConverterFactory.create<User>(UserConverter())
        val template = """
            {
                "${User.ID_FIELD}": "${UUID.randomUUID()}",
                "${User.PHONE_NUMBER_FIELD}": "0123456789"
            }
        """.trimIndent()
        val json = JsonParser.parseString(template)
        user.read(json)
        println(user.write())
    }
}