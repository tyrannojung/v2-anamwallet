package com.anam145.wallet.core.security.data.util

import com.anam145.wallet.core.security.model.KdfParams
import com.anam145.wallet.core.security.model.Pbkdf2KdfParams
import com.anam145.wallet.core.security.model.ScryptKdfParams
import com.google.gson.*
import java.lang.reflect.Type

/**
 * KdfParams의 다형성을 처리하기 위한 Gson TypeAdapter
 */
class KdfParamsTypeAdapter : JsonDeserializer<KdfParams>, JsonSerializer<KdfParams> {
    
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): KdfParams {
        val jsonObject = json.asJsonObject
        
        // scrypt가 기본값이므로 n 파라미터가 있으면 ScryptKdfParams로 판단
        return if (jsonObject.has("n")) {
            context.deserialize(json, ScryptKdfParams::class.java)
        } else if (jsonObject.has("c")) {
            context.deserialize(json, Pbkdf2KdfParams::class.java)
        } else {
            // 기본값은 scrypt
            context.deserialize(json, ScryptKdfParams::class.java)
        }
    }
    
    override fun serialize(
        src: KdfParams,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return context.serialize(src, src::class.java)
    }
}