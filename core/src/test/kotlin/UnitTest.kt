import com.github.huymaster.textguardian.core.dto.BaseDTO
import com.github.huymaster.textguardian.core.dto.BaseDTOImpl
import com.github.huymaster.textguardian.core.dto.UserDTO
import com.github.huymaster.textguardian.core.utils.DEFAULT_OBJECT_MAPPER
import java.util.*
import kotlin.test.Test

class UnitTest {
    @Test
    fun testConvertable() {
        val o = BaseDTOImpl.getInstance(UserDTO::class)
        o.userId = UUID.randomUUID()
        o.phoneNumber = "123"
        val str = DEFAULT_OBJECT_MAPPER.writeValueAsString(o)
        println(str)
        println(DEFAULT_OBJECT_MAPPER.readValue(str, BaseDTO::class.java).toEntity())
    }
}