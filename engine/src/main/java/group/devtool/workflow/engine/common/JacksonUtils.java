/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.devtool.workflow.engine.exception.DeserializeException;
import group.devtool.workflow.engine.exception.SerializeException;

public final class JacksonUtils {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private JacksonUtils() {

	}

	public static String serialize(Object value) throws SerializeException {
		try {
			return MAPPER.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			throw new SerializeException("参数序列化异常：" + e.getMessage());
		}
	}

	public static <T> T deserialize(String value, Class<T> clazz) throws DeserializeException {
		try {
			return MAPPER.readValue(value, clazz);
		} catch (JsonProcessingException e) {
			throw new DeserializeException("参数反序列化异常：" + e.getMessage());
		}
	}

	public static <T> T deserialize(String value) throws DeserializeException {
		try {
			return MAPPER.readValue(value, new TypeReference<T>() {});
		} catch (JsonProcessingException e) {
			throw new DeserializeException("参数反序列化异常：" + e.getMessage());
		}
	}
}
