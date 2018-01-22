package jg.editables;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Wraps a class field that is defined as editable through a jg.editables.Editable annotation and provides
 * an interface for modifying the field's value through reflection.
 * @author Jordan Glanfield
 */
public class EditableField {

  private Editable editable;
  private Class<?> editableClass;
  private Field field;
  private Method setter;
  
  // Initialisation
  
  /**
   * Creates an editable field that refers to the given class, wraps the provided field and uses
   * the field's editable annotation.
   */
  public EditableField(Class<?> editableClass, Field field, Editable editable) {
    this.editableClass = editableClass;
    this.field = field;
    this.editable = editable;
    setSetter();
  }

  private void setSetter() {
    String setterName = editable.setterName();

    if (setterName == Editable.NO_SETTER) {
      setter = null;
    } else {
      try {
        setter = editableClass.getMethod(setterName, field.getType());
        setter.setAccessible(true);
      } catch (NoSuchMethodException e) {
        setter = null;
      }
    }
  }
  
  // Getters

  /**
   * Returns the underlying field being wrapped.
   */
  public Field getField() {
    return field;
  }

  /**
   * Returns whether the underlying field represents an enum.
   */
  public boolean isEnumValue() {
    return field.getType().isEnum();
  }
  
  // Field value updating

  /**
   * Attempts to parse the given string into a value for the field using the given parsingFunctions.
   * May throw exceptions based on the parsingFunctions given.
   * @return true if successful and false if not.
   */
  public boolean setFieldValue(Object object, String string, ParsingFunctionsMap parsingFunctions) {
    Object result = parsingFunctions.parse(field.getType(), string);

    if (result != null) {
      return setValue(object, result);
    } else if (isEnumValue()) {
      try {
        Enum enu = (Enum) field.get(object);
        if (enu != null) {
          result = Enum.valueOf(enu.getClass(), string);
        }
      } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
        return false;
      }

      return setValue(object, result);
    } else {
      return false;
    }
  }

  /**
   * Attempts apply the given value to this field on the object.
   * @return true if successful and false if not.
   */
  public boolean setValue(Object object, Object value) {
    if (setter != null) {
      try {
        setter.invoke(object, value);
        return true;
      } catch (IllegalAccessException e) {
        e.printStackTrace();
        return setValueDirectly(object, value);
      } catch (InvocationTargetException e) {
        e.printStackTrace();
        return setValueDirectly(object, value);
      }
    } else {
      return setValueDirectly(object, value);
    }
  }

  private boolean setValueDirectly(Object object, Object value) {
    try {
      field.set(object, value);
      return true;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Returns the value of this field on the given object.
   * @throws IllegalAccessException
   */
  public Object getFieldValue(Object object) throws IllegalAccessException {
    return field.get(object);
  }

  /**
   * Returns the value of this field on the given object, or null if the value cannot be accessed.
   */
  public Object getFieldValueChecked(Object object) {
    try {
      return getFieldValue(object);
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  // Miscellaneous
  
  @Override
  public String toString() {
    return field.getName();
  }

  @Override
  public int hashCode() {
    return field.hashCode();
  }
}
