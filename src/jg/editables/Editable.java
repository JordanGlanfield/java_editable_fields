package jg.editables;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an annotation placed before a field that specifies that the field may be editable
 * at run time.
 * @author Jordan Glanfield
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Editable {
  static final String NO_SETTER = "__noSetter";

  /**
   * Returns the category in which this field belongs. Can be modified in order to better
   * delineate properties.
   */
  String category() default "Object";

  /**
   * The name of the setter to use when modifying the field's value. The setter must accept a
   * single argument that the field is assignable from. By default the field's value will be
   * directly set. Only public setters may be used currently.
   */
  String setterName() default NO_SETTER;
}
