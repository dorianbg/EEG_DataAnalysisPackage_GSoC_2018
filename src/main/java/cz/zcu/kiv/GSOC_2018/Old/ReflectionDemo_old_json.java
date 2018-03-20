package cz.zcu.kiv.GSOC_2018.Old;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import com.thoughtworks.paranamer.Paranamer;
import cz.zcu.kiv.Classification.IClassifier;
import cz.zcu.kiv.DataTransformation.IDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ReflectionDemo_old_json {
    public static void main(String[] args) {
        Reflections reflections = new Reflections("cz.zcu.kiv");


        // Extract information from 3 main interfaces
        List<Class> targetInterfaces = new ArrayList<>();
        targetInterfaces.add(IClassifier.class);
        targetInterfaces.add(IDataProvider.class);
        targetInterfaces.add(IFeatureExtraction.class);


        // the main json object we use to store all of the information
        JSONObject totalJsonObject = getJsonObject();

        JSONArray methodsJsonArray = new JSONArray();
        // loop over the interfaces
        for (Class aInterface : targetInterfaces) {
            // create an object for each interface
            // find all classes that implement the interface
            List<Class> classes = new ArrayList<>();
            classes.addAll(reflections.getSubTypesOf(aInterface));

            // loop over the classes
            for (Class aClass : classes) {
                //classObject.put("class_description", "<empty>");
                // find all the methods implemented by the class
                Method[] methods = aClass.getDeclaredMethods();
                // loop over the methods
                for (Method method : methods) {
                    // create a method object
                    JSONObject methodObject = getJsonObject();
                    if(method.getName().contains("$")){
                        continue;
                    }

                    List<String> desiredMethodNames = new ArrayList<>();
                    desiredMethodNames.add("loadData");
                    desiredMethodNames.add("extractFeatures");
                    desiredMethodNames.add("filter");
                    desiredMethodNames.add("train");
                    desiredMethodNames.add("test");

                    if (!desiredMethodNames.contains(method.getName())){
                        continue;
                    }

                    String interfaceName = aInterface.getName();
                    String className = aClass.getName();
                    methodObject.put("interface_name", interfaceName.substring(interfaceName.lastIndexOf(".")+1,
                                                        interfaceName.length()));
                    methodObject.put("class_name", className.substring(className.lastIndexOf(".")+1,
                                                    className.length()));
                    methodObject.put("method_name", method.getName());
                    methodObject.put("method_output_type", method.getReturnType().getName());
                    methodObject.put("method_output_description", "<empty>");
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
            }
        }
        // add the array of interfaces to the main document
        totalJsonObject.put("main_methods", methodsJsonArray);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("/Users/dbg/gsoc_projects/EEG_DataAnalysisPackage/src/main/java/cz/zcu/kiv/GSOC_2018/json_output.txt"), "utf-8"))) {
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
