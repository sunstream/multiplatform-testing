package utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StringUtils {
	public static String getTimestampSuffix(){
		LocalDateTime time = LocalDateTime.now();
		return time.format(java.time.format.DateTimeFormatter.ofPattern("_hh_mm_ss"));
	}

	public static String getMapAsString(Map<String, String> map) {
		return map.keySet().stream().map(k -> k + " = " + map.get(k)).collect(joinByNewLine());
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static Collector<CharSequence, ?, String> joinByNewLine() {
		return joinBy("\n");
	}

	public static Collector<CharSequence, ?, String> joinByComma() {
		return joinBy(", ");
	}

	public static Collector<CharSequence, ?, String> joinBy(String delimiter) {
		return Collectors.joining(delimiter);
	}

}
