/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/
 
package org.opengroup.osdu.legal.ibm.tags;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Optional;

import jakarta.inject.Inject;

import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper=true)

public class CloudantBackedLegalTag extends LegalTag {

	@EqualsAndHashCode.Exclude
	private String _rev = null;
	 @Inject
	 private static JaxRsDpsLog logger;

	public static final JsonSerializer<LegalTag> serializer = new JsonSerializer<LegalTag>() {  
		@Override
		public JsonElement serialize(LegalTag src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject json = new JsonObject();

			if (src instanceof CloudantBackedLegalTag) {
				json.addProperty("_rev", ((CloudantBackedLegalTag)src).get_rev());	
			}

			json.addProperty("_id", src.getId().toString());
			json.addProperty("name", src.getName());
			json.addProperty("description", src.getDescription());
			json.addProperty("is_valid", src.getIsValid());

			json.add("properties", context.serialize(src.getProperties(), Properties.class));

			return json;
		}
	};

	public static final JsonSerializer<Date> sqlDateSerializer = new JsonSerializer<Date>() {  
		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.getTime());
		}
	};
	
	public static final JsonDeserializer<CloudantBackedLegalTag> deserializer = new JsonDeserializer<CloudantBackedLegalTag>() {

		@Override
		public CloudantBackedLegalTag deserialize(JsonElement json, Type arg1, JsonDeserializationContext context)
				throws JsonParseException {

			final CloudantBackedLegalTag result = new CloudantBackedLegalTag();
			JsonObject src = json.getAsJsonObject();

			try {
				// Only the _rev object may be null
				result.set_rev(Optional.ofNullable(src.get("_rev")).map(JsonElement::getAsString).orElse(null));

				result.setId(src.get("_id").getAsLong());
				result.setName(src.get("name").getAsString());
				result.setDescription(src.get("description").getAsString());
				result.setIsValid(src.get("is_valid").getAsBoolean());
				result.setProperties(context.deserialize(src.get("properties"), Properties.class));

			} catch (NullPointerException ex) {
				logger.error("Serialized Legal Tag is missing one or more required fields");
				throw new IllegalStateException("Serialized Legal Tag is missing one or more required fields");
			}
			return result;
		}

	};
	
	public static final JsonDeserializer<Date> sqlDateDeserializer = new JsonDeserializer<Date>() {

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			return new Date(json.getAsLong());
		}
		
	};

}
