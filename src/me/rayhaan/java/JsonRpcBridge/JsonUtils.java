package me.rayhaan.java.JsonRpcBridge;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.LinkedList;

import com.google.gson.*;

public class JsonUtils {

	/**
	 * Convert a JsonArray to an array of Java Objects. Note: we need to get the
	 * type of the array from the java class we know exists already, otherwise
	 * it is difficult to detect what type a primitive is.
	 * 
	 * @param jsa
	 *            the json array to convert to a java object array
	 */
	private static Object[] convertJsonArrayToJavaArray(JsonArray jsa,
			Class<?> type) throws Exception {

		Object[] result = new Object[jsa.size()];

		JsonObject obj;

		for (int i = 0; i < jsa.size(); i++) {
			obj = jsa.get(i).getAsJsonObject();
			result[i] = jsonObjectToObject(obj, type.toString());
		}

		return result;
	}

	private static LinkedList<Object> convertJsonArrayToJavaArray(JsonArray jsa)
			throws Exception {
		LinkedList<Object> result = new LinkedList<Object>();

		JsonElement obj;

		for (int i = 0; i < jsa.size(); i++) {
			obj = jsa.get(i);
			if (obj.isJsonNull()) {
				result.add(null);
			} else if (obj.isJsonArray()) {
				result.add(convertJsonArrayToJavaArray(obj.getAsJsonArray()));
			} else if (obj.isJsonPrimitive()) {

			} else if (obj.isJsonObject()) {
				
			} else {
				throw new Exception(
						"Error occured deserializing JsonElement in JsonArray");
			}
		}

	}

	private Object jsonPrimitiveToJavaPrimitive(JsonPrimitive jPrim) throws Exception {
		if (jPrim.isString()) {
			return jPrim.getAsString();
		} else if (jPrim.isNumber()) {
			// FIXME: we are assuming all numbers that have decimal points are
			// doubles, and all others are ints
			// Split by decimal point if it exists, and if we have more than one
			// part, the number had a decimal point
			if (jPrim.getAsString().split("\\.").length > 1) {
				return jPrim.getAsDouble();
			} else {
				return jPrim.getAsInt();
			}
		} 
		throw new Exception("Unable to convert JsonPrimitive.");
	}

	/**
	 * Convert a JsonObject to a Java object.
	 * 
	 */
	public static Object jsonObjectToObject(JsonObject JSON, String javaClass)
			throws Exception {
		Object instance = (Object) Class.forName(javaClass).newInstance();
		Class<?> clazz = instance.getClass();
		Field f;

		String fieldName;
		Object value;

		for (Map.Entry<String, JsonElement> entry : JSON.entrySet()) {
			fieldName = entry.getKey();

			f = resolveField(clazz, fieldName);
			f.setAccessible(true);

			Class<?> fieldType = f.getType();

			if (fieldType.isPrimitive()) {
				String type = f.getType().toString();

				switch (type) {
				case "int":
					f.set(instance, entry.getValue().getAsInt());
					break;
				case "double":
					f.set(instance, entry.getValue().getAsDouble());
					break;
				case "float":
					f.set(instance, entry.getValue().getAsFloat());
					break;
				case "byte":
					f.set(instance, entry.getValue().getAsByte());
					break;
				case "char":
					f.set(instance, entry.getValue().getAsCharacter());
					break;
				case "long":
					f.set(instance, entry.getValue().getAsLong());
					break;
				}
			} else if (fieldType.isArray()) {
				f.set(instance,
						convertJsonArrayToJavaArray(entry.getValue()
								.getAsJsonArray(), fieldType));
			} else {
				// must be an object that we have serialized
				f.set(instance,
						jsonObjectToObject(entry.getValue().getAsJsonObject(),
								fieldType.toString()));
			}
		}
		return instance;
	}

	/**
	 * Try and find a field in the class hierarchy starting from a given class
	 * and working up its supercalsses.
	 */
	private static Field resolveField(Class<?> clazz, String fieldName)
			throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null) {
				return resolveField(superClass, fieldName);
			} else {
				throw e;
			}

		}
	}

	/**
	 * Get the javaClass of an object that we have serialized
	 * 
	 * @param JSON
	 * @return the class name
	 */
	public static String javaClassFieldExtractor(JsonObject JSON) {
		for (Map.Entry<String, JsonElement> entrySet : JSON.entrySet()) {
			if (entrySet.getKey().equals("JavaClass")) {
				return entrySet.getValue().getAsString();
			}
		}
		return null;
	}

}
