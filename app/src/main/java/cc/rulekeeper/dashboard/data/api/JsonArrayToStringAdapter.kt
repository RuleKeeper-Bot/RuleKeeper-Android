package cc.rulekeeper.dashboard.data.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Gson TypeAdapter that handles conversion between JSON arrays and strings.
 * When the API returns a JSON array, it converts it to a string representation.
 * When the API returns a string, it passes it through unchanged.
 */
class JsonArrayToStringAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(input: JsonReader): String {
        return when (input.peek()) {
            JsonToken.STRING -> input.nextString()
            JsonToken.BEGIN_ARRAY -> {
                val list = mutableListOf<String>()
                input.beginArray()
                while (input.hasNext()) {
                    when (input.peek()) {
                        JsonToken.STRING -> list.add(input.nextString())
                        JsonToken.NUMBER -> list.add(input.nextLong().toString())
                        JsonToken.BOOLEAN -> list.add(input.nextBoolean().toString())
                        else -> input.skipValue()
                    }
                }
                input.endArray()
                // Convert list to JSON array string format
                "[${list.joinToString(",") { "\"$it\"" }}]"
            }
            JsonToken.NULL -> {
                input.nextNull()
                "[]"
            }
            else -> {
                input.skipValue()
                "[]"
            }
        }
    }
}
