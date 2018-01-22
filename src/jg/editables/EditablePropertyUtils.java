package jg.editables;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contains static methods for finding editable fields in a class.
 * @author Jordan Glanfield
 */
public class EditablePropertyUtils {

  /**
   * Generates a map from string categories to lists of editable fields, in the class and any
   * superclasses, belonging to those categories.
   */
  public static Map<String, List<EditableField>> getPropertyGroups(Class clazz) {
    Map<String, List<EditableField>> groups = new LinkedHashMap<>();
    List<Field> fields = ReflectionUtils.getAllDeclaredFields(clazz);

    for (Field field : fields) {
      if (field.isAnnotationPresent(Editable.class)) {
        Editable editable = field.getAnnotation(Editable.class);
        String category = editable.category();
        List<EditableField> list;
        EditableField property = new EditableField(clazz, field, editable);

        if (!groups.containsKey(category)) {
          list = new LinkedList<>();
          list.add(property);
          groups.put(category, list);
        } else {
          groups.get(category).add(property);
        }

        field.setAccessible(true);
      }
    }

    return groups;
  }

  /**
   * Generates a map from string categories to lists of editable fields, in the object's class and
   * any superclasses, belonging to those categories. Equivalent to calling
   * getPropertyGroups(object.getClass()).
   */
  public static Map<String, List<EditableField>> getPropertyGroups(Object object) {
    return getPropertyGroups(object.getClass());
  }

  /**
   * Prints out the editable fields in each category along with the concrete value for each field
   * based on the given object prototype. Assumes that the prototype has the same type or
   * inherits from the same type that the editable fields apply to. Present for debugging.
   */
  public static void printGroups(Map<String, List<EditableField>> groups, Object object) {
    for (Map.Entry<String, List<EditableField>> entry : groups.entrySet()) {
      System.out.println(entry.getKey());

      for (EditableField field : entry.getValue()) {
        System.out.println("\t" + field.getField().getName() + " " + field.getFieldValueChecked(object));
      }
    }
  }
}
