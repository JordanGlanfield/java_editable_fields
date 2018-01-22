package jg.editables;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Maps classes to functions that will parse a string into an instance of that class.
 * @author Jordan Glanfield
 */
public class ParsingFunctionsMap {
  
  private Map<Class<?>, Function<String, Object>> parsingFunctions;

  /**
   * Creates a parsing functions map out of the given map of classes to functions for converting a
   * string representation to an object that is assignable to that type.
   */
  public ParsingFunctionsMap(Map<Class<?>, Function<String, Object>> parsingFunctions) {
    this.parsingFunctions = parsingFunctions;
  }

  /**
   * Adds the given parsing function to the map.
   */
  public void addParsingFunction(Class<?> targetClass, Function<String, Object> conversion) {
    parsingFunctions.put(targetClass, conversion);
  }

  /**
   * Returns true if the given class can be parsed using the available functions and false if not.
   */
  public boolean canParse(Class<?> targetClass) {
    return getParseFunction(targetClass) != null;
  }

  /**
   * Returns the result of applying a parsing function for the given class to the given string, or
   * null if no function was present or the parsing failed.
   */
  public Object parse(Class<?> targetClass, String string) {
    Function<String, Object> parseFunction = getParseFunction(targetClass);

    if (parseFunction != null) {
      return parseFunction.apply(string);
    } else {
      return null;
    }
  }

  /**
   * Returns the parsing function available for the given class or null if none is present. Will
   * first search for a direct parsing function for the targetClass and then for a parsing function
   * for a class from which targetClass is assignable.
   */
  public Function<String, Object> getParseFunction(Class<?> targetClass) {
    Function<String, Object> parseFunction = parsingFunctions.get(targetClass);
    if (parseFunction == null) {
      for (Map.Entry<Class<?>, Function<String, Object>> entry : parsingFunctions.entrySet()) {
        if (targetClass.isAssignableFrom(entry.getKey())) {
          parseFunction = entry.getValue();
          break;
        }
      }
    }
    
    return parseFunction;
  }

  /**
   * Returns a map from classes to parsing functions containing parsing functions for booleans,
   * chars, bytes, shorts, ints, longs, floats, doubles, strings and the boxed variants of all
   * the mentioned primitive types.
   */
  public static Map<Class<?>, Function<String, Object>> getPrimitiveParsingFunctions() {
    Map<Class<?>, Function<String, Object>> parsingFunctions = new LinkedHashMap<>();

    parsingFunctions.put(String.class, (string) -> string);
    parsingFunctions.put(Double.TYPE, Double::parseDouble);
    parsingFunctions.put(Double.class, Double::parseDouble);
    parsingFunctions.put(Float.TYPE, Float::parseFloat);
    parsingFunctions.put(Float.class, Float::parseFloat);
    parsingFunctions.put(Long.TYPE, Long::parseLong);
    parsingFunctions.put(Long.class, Long::parseLong);

    Function<String, Object> charParsingFunction = (string) -> {
      if (string.length() == 1) {
        return string.charAt(0);
      } else {
        return null;
      }
    };
    
    parsingFunctions.put(Character.TYPE, charParsingFunction);
    parsingFunctions.put(Character.class, charParsingFunction);

    parsingFunctions.put(Integer.TYPE, Integer::parseInt);
    parsingFunctions.put(Integer.class, Integer::parseInt);
    parsingFunctions.put(Short.TYPE, Short::parseShort);
    parsingFunctions.put(Short.class, Short::parseShort);
    parsingFunctions.put(Byte.TYPE, Byte::parseByte);
    parsingFunctions.put(Byte.class, Byte::parseByte);
    parsingFunctions.put(Boolean.TYPE, Boolean::parseBoolean);
    parsingFunctions.put(Boolean.class, Boolean::parseBoolean);

    return parsingFunctions;
  }
}
