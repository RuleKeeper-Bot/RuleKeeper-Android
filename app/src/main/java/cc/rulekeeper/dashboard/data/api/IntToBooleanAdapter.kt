package cc.rulekeeper.dashboard.data.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Gson TypeAdapter that handles conversion between integers (0/1) and booleans.
 * SQLite stores booleans as integers, so this adapter handles the conversion.
 */
class IntToBooleanAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(input: JsonReader): Boolean {
        return when (input.peek()) {
            JsonToken.BOOLEAN -> input.nextBoolean()
            JsonToken.NUMBER -> input.nextInt() != 0
            JsonToken.STRING -> {
                val stringValue = input.nextString()
                stringValue.equals("true", ignoreCase = true) || stringValue == "1"
            }
            JsonToken.NULL -> {
                input.nextNull()
                false
            }
            else -> false
        }
    }
}
