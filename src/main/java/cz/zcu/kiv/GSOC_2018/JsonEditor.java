package cz.zcu.kiv.GSOC_2018;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.zcu.kiv.GSOC_2018.structure.Container;
import cz.zcu.kiv.GSOC_2018.structure.Method;
import cz.zcu.kiv.GSOC_2018.structure.Parameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class JsonEditor {
    public static void main(String[] args) throws FileNotFoundException {

        String executionPath = null;
        try{
            executionPath = System.getProperty("user.dir");
            System.out.print("Executing at =>"+executionPath.replace("\\", "/"));
        }catch (Exception e){
            System.out.println("Exception caught ="+e.getMessage());
        }

        final String jsonLocation = executionPath + "/src/main/java/cz/zcu/kiv/GSOC_2018/json_output.txt";
        /*
                    Preparing the data
         */
        /*
        Initialize the GUI window
         */
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenSizeWidth = screenSize.getWidth();
        double screenSizeHeight = screenSize.getHeight();
        final JFrame mainScreen = new JFrame();
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to cross-platform
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // not worth my time
            }
        }

        /*
        Load the JSON representation into Java Objects
         */
        Gson gson = new Gson();

        final Container container = gson.fromJson(new FileReader(jsonLocation), Container.class);

        /*
        Keep track of current interface, class, method and parameter for the UI
         */
        Set<String> classNames = new HashSet<>();
        final Set<String> methodNames = new HashSet<>();
        final Set<String> interfaceNames = new HashSet<>();
        final Set<String> parameterNames = new HashSet<>();

        final String[] currentInterfaceName = new String[1];

        final String[] currentClassName = new String[1];

        final String[] currentMethodName = new String[1];
        final Method[] currentMethod = new Method[1];
        currentMethod[0] = container.main_methods.get(0);

        final String[] currentParameterName = new String[1];
        final Parameter[] currentParameter = new Parameter[1];
        currentParameter[0] = container.main_methods.get(0).method_parameters.get(0);

        for (Method method : container.main_methods){
            classNames.add(method.class_name);
            methodNames.add(method.method_name);
            interfaceNames.add(method.interface_name);
            for (Parameter parameter : method.method_parameters){
                parameterNames.add(parameter.parameter_name);
            }
        }



        /*
                    Building the UI
         */
        /*
        0. combo boxes that contain choices
         */
        final JComboBox interfaces = new JComboBox(interfaceNames.toArray());
        final JComboBox classes = new JComboBox(classNames.toArray());
        final JComboBox methods = new JComboBox(methodNames.toArray());
        final JComboBox parameters = new JComboBox(parameterNames.toArray());

        /*
        1. Add ActionListener to Interfaces JComboBox that filters the Classes that can be chosen in their JComboBox
         */
        JLabel label1 = new JLabel("Select the interface:");
        interfaces.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // set the selected interface a current
                String selectedInterface = String.valueOf(interfaces.getSelectedItem());
                currentInterfaceName[0] = selectedInterface;

                // change the selection of classes JComboBox
                DefaultComboBoxModel model = (DefaultComboBoxModel) classes.getModel();
                Set<String> classNames = new HashSet<>();  // use a set in order to only have distinct classes

                model.removeAllElements();
                for (Method method : container.main_methods){
                    if(method.interface_name.equals(selectedInterface)) {
                        classNames.add(method.class_name);
                    }
                };
                // using the Hash<Set> add elements
                for(String className : classNames){
                    model.addElement(className);
                }
                classes.setModel(model);
            }
        });

        /*
        2a) Initialize the JLabel and JTextField elements that represent the Method
         */
        JLabel label2 = new JLabel("Select the class:");
        JLabel label31 = new JLabel("         Method description:");
        final JTextField field31 = new JTextField(currentMethod[0].method_description);
        field31.setEditable(true);

        JLabel label32 = new JLabel("         Method output type:");
        final JTextField field32 = new JTextField(currentMethod[0].method_output_type);
        field32.setEditable(false);

        JLabel label33 = new JLabel("         Method output description:");
        final JTextField field33 = new JTextField(currentMethod[0].method_output_description);
        field33.setEditable(true);

        /*
        2b) Add ActionListener to Classes JComboBox that filters the Methods that can be chosen in their JComboBox
         */
        classes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedClass = String.valueOf(classes.getSelectedItem());
                currentClassName[0] = selectedClass;

                DefaultComboBoxModel model = (DefaultComboBoxModel) methods.getModel();

                model.removeAllElements();
                for (Method method : container.main_methods){
                    if(method.class_name.equals(selectedClass)) {
                        model.addElement(method.method_name);
                    }
                };
                methods.setModel(model);
            }
        });

        /*
        3) Add ActionListener to Methods JComboBox that filters the Parameters that can be chosen in their JComboBox and Method attributes
         */
        JLabel label3 = new JLabel("Select the method:");
        methods.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedMethod = String.valueOf(methods.getSelectedItem());
                currentMethodName[0] = selectedMethod;

                DefaultComboBoxModel model = (DefaultComboBoxModel) parameters.getModel();
                model.removeAllElements();

                for (Method method : container.main_methods){
                    if(method.class_name.equals(currentClassName[0]) && method.method_name.equals(currentMethodName[0])) {
                        currentMethod[0] = method;
                        for(Parameter param : method.method_parameters) {
                            model.addElement(param.parameter_name);
                        }
                    }
                };

                field31.setText(currentMethod[0].method_description);
                field33.setText(currentMethod[0].method_output_description);
                parameters.setModel(model);
            }
        });

        /*
        4a) Initialize the JLabel and JTextField elements that represent the Parameter
         */
        JLabel label41 = new JLabel("         Parameter type");
        final JTextField field41 = new JTextField(currentParameter[0].parameter_type);
        field41.setEditable(false);

        JLabel label42 = new JLabel("         Parameter description");
        final JTextField field42 = new JTextField(currentParameter[0].parameter_description);
        field42.setEditable(true);

        JLabel label43 = new JLabel("         Parameter set of recommended values");
        final JTextField field43 = new JTextField(currentParameter[0].parameter_set_of_recommended_values);
        field43.setEditable(true);

        JLabel label44 = new JLabel("         Parameter range of allowed values");
        final JTextField field44 = new JTextField(currentParameter[0].parameter_range_of_allowed_values);
        field44.setEditable(true);

        /*
        4b) Add ActionListener to Methods JComboBox that filters the Parameters that can be chosen in their JComboBox
         */
        JLabel label4 = new JLabel("Select the parameter:");
        parameters.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedParameter = String.valueOf(parameters.getSelectedItem());
                currentParameterName[0] = selectedParameter;

                for (Method method : container.main_methods){
                    if(method.class_name.equals(currentClassName[0]) && method.method_name.equals(currentMethodName[0])){
                        for(Parameter param : method.method_parameters) {
                            if(param.parameter_name.equals(currentParameterName[0])) {
                                currentParameter[0] = param;
                            }
                        }
                    }
                };
                field41.setText(currentParameter[0].parameter_type);
                field42.setText(currentParameter[0].parameter_description);
                field43.setText(currentParameter[0].parameter_set_of_recommended_values);
                field44.setText(currentParameter[0].parameter_range_of_allowed_values);
            }
        });

        /*
        5) Save button that saves the changes made to the data model
         */
        JButton saveButton = new JButton("SAVE");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Method method : container.main_methods){
                    if(method.class_name.equals(currentMethod[0].class_name) && method.interface_name.equals(currentMethod[0].interface_name)
                            && method.method_name.equals(currentMethod[0].method_name)){
                        method.method_description = field31.getText();
                        method.method_output_description = field33.getText();

                        for(Parameter parameter : method.method_parameters){
                            if(parameter.parameter_name.equals(currentParameter[0].parameter_name)){
                                parameter.parameter_type = field41.getText();
                                parameter.parameter_description = field42.getText();
                                parameter.parameter_set_of_recommended_values = field43.getText();
                                parameter.parameter_range_of_allowed_values = field44.getText();
                            }
                        }
                    }
                }
                Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
                String json = gson.toJson(container);
                try (PrintWriter out = new PrintWriter(jsonLocation)) {
                    out.println(json);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });

        /*
        6) Exit button to close the application
         */
        JButton exitButton = new JButton("EXIT");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainScreen.dispose();
            }
        });

        /*
        7) Build the total layout and everyting to the JFrame
         */
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(12,2,1,1));

        panel.add(label1);
        interfaces.setSelectedIndex(0);
        panel.add(interfaces);

        panel.add(label2);
        classes.setSelectedIndex(0);
        panel.add(classes);

        panel.add(label3);
        methods.setSelectedIndex(0);
        panel.add(methods);

        panel.add(label31);
        panel.add(field31);

        panel.add(label32);
        panel.add(field32);

        panel.add(label33);
        panel.add(field33);

        panel.add(label4);
        panel.add(parameters);

        panel.add(label41);
        panel.add(field41);

        panel.add(label42);
        panel.add(field42);

        panel.add(label43);
        panel.add(field43);

        panel.add(label44);
        panel.add(field44);

        panel.add(saveButton);
        panel.add(exitButton);

        mainScreen.add(panel);

        /*
        8) Display the actual JFrame with the JPanel we designed. Also add some key listeners
         */
        mainScreen.setSize((int) screenSizeWidth / 2, (int) (screenSizeHeight * 3 / 4));
        mainScreen.setResizable(true);
        mainScreen.setLocationByPlatform(true);
        mainScreen.setLocationRelativeTo(null); // positions the window to middle of screen
        mainScreen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainScreen.setVisible(true);
        // here we set the ESCAPE button to close the JFrame
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            // close the frame when the user presses escape
            public void actionPerformed(ActionEvent e) {
                mainScreen.dispose();
            }
        };
        mainScreen.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        mainScreen.getRootPane().getActionMap().put("ESCAPE", escapeAction);

    }
}
