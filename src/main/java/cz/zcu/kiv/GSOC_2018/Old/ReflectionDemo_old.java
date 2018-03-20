package cz.zcu.kiv.GSOC_2018.Old;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import com.thoughtworks.paranamer.Paranamer;
import cz.zcu.kiv.Classification.IClassifier;
import cz.zcu.kiv.DataTransformation.IDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import org.reflections.Reflections;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class ReflectionDemo_old {
    public static void main(String[] args) {
        Reflections reflections = new Reflections("cz.zcu.kiv");


        // Extract information from 3 main interfaces
        List<Class> targetInterfaces = new ArrayList<>();
        targetInterfaces.add(IClassifier.class);
        targetInterfaces.add(IDataProvider.class);
        targetInterfaces.add(IFeatureExtraction.class);


        // the main json object we use to store all of the information
        JSONObject totalJsonObject = getJsonObject();
        JSONArray interfacesJsonArray = new JSONArray();

        // loop over the interfaces
        for (Class aInterface : targetInterfaces) {
            // create an object for each interface
            JSONObject interfaceObject = getJsonObject();
            interfaceObject.put("interface_name", aInterface.getName());
            interfaceObject.put("interface_description", "<empty>");

            // find all classes that implement the interface
            List<Class> classes = new ArrayList<>();
            classes.addAll(reflections.getSubTypesOf(aInterface));
            JSONArray classesJsonArray = new JSONArray();

            // loop over the classes
            for (Class aClass : classes) {
                // create an object for each class
                JSONObject classObject = getJsonObject();
                classObject.put("class_name", aClass.getName());
                classObject.put("class_description", "<empty>");
                // find all the methods implemented by the class
                JSONArray methodsJsonArray = new JSONArray();
                Method[] methods = aClass.getDeclaredMethods();
                // loop over the methods
                for (Method method : methods) {
                    // create a method object
                    JSONObject methodObject = getJsonObject();
                    if(method.getName().contains("$")){
                        continue;
                    }
                    methodObject.put("method_name", method.getName());
                    methodObject.put("method_output_type", method.getReturnType().getName());
                    methodObject.put("method_description", "<empty>");

                    // find all parameters of a method
                    JSONArray parametersJsonArray = new JSONArray();
                    Paranamer paranamer = new AdaptiveParanamer();
                    String[] parameterNames;
                    try {
                        parameterNames = paranamer.lookupParameterNames(method);
                        Class<?>[] parameterTypes = method.getParameterTypes();

                        List<String> parameterTypesList = new ArrayList<>();
                        for (Class<?> parameterType : parameterTypes) {
                            parameterTypesList.add(parameterType.getName());
                        }
                        List<String> parameterNamesList = new ArrayList<>();
                        for (String parameterName : parameterNames) {
                            parameterNamesList.add(parameterName);
                        }
                        assert parameterTypesList.size() == parameterNamesList.size();
                        // parameters
                        for (int i = 0; i < parameterTypesList.size(); i++) {
                            // create a specific object for a parameter and fill it in with information
                            JSONObject parameterObject = getJsonObject();
                            parameterObject.put("parameter_name", parameterNamesList.get(i));
                            parameterObject.put("parameter_type", parameterTypesList.get(i));
                            parameterObject.put("parameter_description", "<empty>");
                            parameterObject.put("parameter_set_of_recommended_values", "<empty>");
                            parameterObject.put("parameter_range_of_allowed_values", "<empty>");
                            parametersJsonArray.put(parameterObject);
                        }
                    } catch (ParameterNamesNotFoundException ex) {
                        System.out.println("-> Method has no parameters or they can't be accessed");
                    }
                    // add the array of parameters to the method
                    methodObject.put("method_parameters", parametersJsonArray);
                    methodObject.put("method_output_type", method.getReturnType().getName());
                    // add the current method to array of all methods
                    methodsJsonArray.put(methodObject);
                }
                // add all the methods to the containing class
                classObject.put("contained_methods", methodsJsonArray);
                // add the current class to array of all classes
                classesJsonArray.put(classObject);
            }
            // add all the methods to the containing interface
            interfaceObject.put("implemented_classes", classesJsonArray);
            // add the current interface to array of all interface
            interfacesJsonArray.put(interfaceObject);
        }
        // add the array of interfaces to the main document
        totalJsonObject.put("main_interfaces", interfacesJsonArray);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("/Users/dbg/gsoc_projects/EEG_DataAnalysisPackage/src/main/java/cz/zcu/kiv/Reflection/json_output.txt"), "utf-8"))) {
            writer.write(totalJsonObject.toString(4));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(totalJsonObject.toString(4));
    }

    // a hack on the actual JSONObject class to preserve the order of keys
    public static JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();
        Field map = null;
        try {
            map = jsonObject.getClass().getDeclaredField("map");
            map.setAccessible(true);//because the field is private final...
            map.set(jsonObject, new LinkedHashMap<>());
            map.setAccessible(false);//return flag
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
