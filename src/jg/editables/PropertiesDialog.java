package jg.editables;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.List;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides a rudimentary dialog for modifying editable fields on an object using a list of
 * field categories, a list of fields for each category and a text field for value entry.
 * @author Jordan Glanfield
 */
public class PropertiesDialog extends Dialog {

  private static final int WIDTH = 500;
  private static final int HEIGHT = 300;
  private static final int CATEGORY_LIST_WIDTH = 80;
  private static final int PROPERTIES_LIST_WIDTH = 220;
  private static final int LIST_PADDING = 35;
  private static final int FIELD_WIDTH = 80;
  private static final int FIELD_HEIGHT = 20;
  
  private List categories = new List(1, false);
  private Map<String, java.util.List<EditableField>> categoryProperties;
  private Map<String, List> categoryLists;
  private List activeList;
  private TextField valueField = new TextField(50);

  private EditableField selectedProperty;
  private Object object;
  private ParsingFunctionsMap parsingFunctions;
  private boolean bComplexProperty = false;

  /**
   * @param owner the frame owning this dialog.
   * @param title title for this dialog.
   * @param modal whether the dialog is modal, see Java dialog documentation.
   * @param object the object whose fields should be modified by this dialog.
   * @param parsingFunctions The functions that should be used for parsing text field input into
   * values for the fields.
   * @param categoryProperties A map from categories to a list of editable fields that will be
   * translated into the UI elements.
   */
  public PropertiesDialog(Frame owner, String title, boolean modal, Object object,
      ParsingFunctionsMap parsingFunctions,
      Map<String, java.util.List<EditableField>> categoryProperties) {
    super(owner, title, modal);
    
    initialise(object, parsingFunctions, categoryProperties);
  }

  public PropertiesDialog(Frame owner, String title, boolean modal, Object object,
      ParsingFunctionsMap parsingFunctions) {
    this(owner, title, modal, object, parsingFunctions,
        EditablePropertyUtils.getPropertyGroups(object));
  }

  public PropertiesDialog(Dialog owner, String title, boolean modal, Object object,
      Map<String, java.util.List<EditableField>> categoryProperties,
      ParsingFunctionsMap parsingFunctions) {
    super(owner, title, modal);
    
    initialise(object, parsingFunctions, categoryProperties);
  }

  public PropertiesDialog(Dialog owner, String title, boolean modal,
      Map<String, java.util.List<EditableField>> categoryProperties, Object object,
      ParsingFunctionsMap parsingFunctions) {
    super(owner, title, modal);

    initialise(object, parsingFunctions, categoryProperties);
  }

  private void initialise(Object object, ParsingFunctionsMap parsingFunctions, 
      Map<String, java.util.List<EditableField>> categoryProperties) {
    this.parsingFunctions = parsingFunctions;
    this.object = object;

    setSize(WIDTH, HEIGHT);
    setResizable(false);
    setLayout(null);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });

    EscapeKeyListener escapeKeyListener = new EscapeKeyListener();

    this.categoryProperties = categoryProperties;

    categories.setLocation(LIST_PADDING, LIST_PADDING);
    categories.setSize(CATEGORY_LIST_WIDTH, HEIGHT - 2 * LIST_PADDING);
    categories.addItemListener(new CategoriesListManager());
    categories.addKeyListener(escapeKeyListener);
    add(categories);

    categoryLists = new LinkedHashMap<>();

    for (Map.Entry<String, java.util.List<EditableField>> entry :
        categoryProperties.entrySet()) {
      String categoryName = entry.getKey();

      categories.add(categoryName);

      List list = new List(1, false);
      categoryLists.put(categoryName, list);

      for (EditableField property : entry.getValue()) {
        list.add(property.toString());
      }

      list.setLocation(2 * LIST_PADDING + CATEGORY_LIST_WIDTH, LIST_PADDING);
      list.setSize(PROPERTIES_LIST_WIDTH, HEIGHT - 2 * LIST_PADDING);
      list.setVisible(false);
      list.addItemListener(new PropertiesListManager(list));
      list.addKeyListener(escapeKeyListener);
      add(list);

      addKeyListener(escapeKeyListener);
    }

    if (categories.getItemCount() > 0) {
      categories.select(0);
      activeList = categoryLists.get(categories.getItem(0));
      activeList.setVisible(true);
    }

    valueField.setLocation(3 * LIST_PADDING + CATEGORY_LIST_WIDTH + PROPERTIES_LIST_WIDTH,
        LIST_PADDING);
    valueField.setSize(FIELD_WIDTH, FIELD_HEIGHT);
    valueField.addActionListener(new ValueFieldManager());
    valueField.addKeyListener(escapeKeyListener);
    add(valueField);
  }

  /**
   * Sets the current field value and disposes of the dialog.
   */
  public void close() {
    setPropertyValue();
    dispose();
  }
  
  private void setActiveList() {
    activeList.setVisible(false);
    activeList = categoryLists.get(categories.getSelectedItem());
    activeList.setVisible(true);
    valueField.setText("");
  }
  
  private void loadValueField() {
    if (selectedProperty != null) {
      if (bComplexProperty) {
        Object value = selectedProperty.getFieldValueChecked(object);
        
        if (value != null) {
          PropertiesDialog newDialog = createDialog((Frame) getParent(),
              selectedProperty.getField().getName(),
              isModal(),
              value,
              parsingFunctions);
          
          PropertiesDialog thisDialog = this;

          if (newDialog != null) {
            newDialog.addWindowListener(new WindowAdapter() {
              @Override
              public void windowClosed(WindowEvent e) {
                thisDialog.requestFocus();
              }
            });
            
            newDialog.setVisible(true);
          }
        }
      } else {
        valueField.setText(selectedProperty.getFieldValueChecked(object).toString());
      }
    } else {
      valueField.setText("");
    }
  }
  
  private void setPropertyValue() {
    if (selectedProperty != null && !bComplexProperty) {
      Object oldValue = selectedProperty.getFieldValueChecked(object);
      boolean bSuccess;
      try {
        bSuccess = selectedProperty.setFieldValue(object, valueField.getText(), parsingFunctions);
      } catch (RuntimeException e) {
        bSuccess = false;
      }

      if (!bSuccess) {
        valueField.setText(oldValue.toString());
      }
    }
  }
  
  public void setSelectedProperty() {
    selectedProperty =
        categoryProperties.get(categories.getSelectedItem()).get(activeList.getSelectedIndex());
    Field field = selectedProperty.getField();
    bComplexProperty = !parsingFunctions.canParse(field.getType()) && !selectedProperty.isEnumValue();
    loadValueField();
  }

  /**
   * Attempts to create a properties dialog out of the given parameters, returning the dialog
   * if there are valid categories for the object or null if not.
   */
  public static PropertiesDialog createDialog(Frame owner, String title, boolean modal,
      Object object, ParsingFunctionsMap parsingFunctions) {
    Map<String, java.util.List<EditableField>> categoryProperties
        = EditablePropertyUtils.getPropertyGroups(object);
    
    if (categoryProperties.isEmpty()) {
      return null;
    } else {
      return new PropertiesDialog(owner, title, modal, object, parsingFunctions, categoryProperties);
    }
  }

  private class EscapeKeyListener extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        close();
      }
    }
  }
  
  private class CategoriesListManager implements ItemListener {

    @Override
    public void itemStateChanged(ItemEvent e) {
      setActiveList();
    }
  }
  
  private class PropertiesListManager implements ItemListener {
    private List list;
    
    public PropertiesListManager(List list) {
      this.list = list;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      if (activeList.getSelectedItem() != null) {
        setPropertyValue();
      }
      setSelectedProperty();
    }
  }

  private class ValueFieldManager implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      setPropertyValue();
    }
  }
}
