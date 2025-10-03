package com.github.huymaster.textguardian.core.utils

import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

fun BaseTable<*>.bytesarray(name: String): Column<Array<ByteArray>> {
    return registerColumn(name, ByteArraySqlType)
}

object ByteArraySqlType : SqlType<Array<ByteArray>>(Types.ARRAY, "_bytea") {
    override fun doSetParameter(
        ps: PreparedStatement,
        index: Int,
        parameter: Array<ByteArray>
    ) {
        val conn = ps.connection
        val array = conn.createArrayOf("bytea", parameter)
        ps.setArray(index, array)
    }

    @Suppress("UNCHECKED_CAST")
    override fun doGetResult(rs: ResultSet, index: Int): Array<ByteArray>? {
        val array = rs.getArray(index) ?: return null

        try {
            if (array.baseTypeName != "bytea") return null
            val anys = array.array as Array<Any>
            val bytesArray = anys
                .map { it as? ByteArray }
                .map { it ?: byteArrayOf() }
            return bytesArray.toTypedArray()
        } catch (ignore: Exception) {
        } finally {
            array.free()
        }
        return null
    }
}