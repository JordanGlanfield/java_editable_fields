package jg.editables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Contains some utilities for working with reflection.
 * @author Jordan Glanfield
 */
public class ReflectionUtils {

  /**
   * Returns a list of all fields in the class and its superclasses. 
   */
  public static List<Field> getAllDeclaredFields(Class<?> clazz) {
    List<Field> fields = new LinkedList<>();
    Class<?> current = clazz;
    
    while (current != null) {
      Field[] classFields = current.getDeclaredFields();
      
      for (int i = 0; i < classFields.length; i++) {
        fields.add(classFields[i]);
      }
      
      current = current.getSuperclass();
    }
    
    return fields;
  }

  /**
   * Returns the first method matching the given signature starting at clazz and working upwards
   * through the superclasses. Returns null if no matching method is found.
   */
  public static Method findMethodInHierarchy(Class<?> clazz, String methodName,
      Class<?> ... types) {
    try {
      return clazz.getDeclaredMethod(methodName, types);
    } catch (NoSuchMethodException e) {
      Class<?> superClass = clazz.getSuperclass();
      
      if (superClass == null) {
        return null;
      } else {
        return findMethodInHierarchy(superClass, methodName, types);
      }
    }
  }

}
