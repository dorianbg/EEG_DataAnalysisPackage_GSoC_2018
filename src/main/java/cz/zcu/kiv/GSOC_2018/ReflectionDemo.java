package cz.zcu.kiv.GSOC_2018;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.ParameterNamesNotFoundException;
import com.thoughtworks.paranamer.Paranamer;
import cz.zcu.kiv.Classification.IClassifier;
import cz.zcu.kiv.DataTransformation.IDataProvider;
import cz.zcu.kiv.FeatureExtraction.IFeatureExtraction;
import cz.zcu.kiv.GSOC_2018.structure.Container;
import cz.zcu.kiv.GSOC_2018.structure.Parameter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ReflectionDemo {
    public static void main(String[] args) throws IOException {
        // the main object we use to store all of the information
        Container container = new Container();

        // Reflection of all objects in the package
        Reflections reflections = new Reflections("cz.zcu.kiv");

        // Extract information from 3 main interfaces
        List<Class> targetInterfaces = new ArrayList<>();
        targetInterfaces.add(IClassifier.class);
        targetInterfaces.add(IDataProvider.class);
        targetInterfaces.add(IFeatureExtraction.class);

        // ignore method names that aren't the following
        List<String> desiredMethodNames = new ArrayList<>();
        desiredMethodNames.add("loadData");
        desiredMethodNames.add("extractFeatures");
        desiredMethodNames.add("filter");
        desiredMethodNames.add("train");
        desiredMethodNames.add("test");

        // Loop over the 3 main interfaces
        for (Class aInterface : targetInterfaces) {

            // Find all classes that implement the interface
            List<Class> classes = new ArrayList<>();
            classes.addAll(reflections.getSubTypesOf(aInterface));

            // Loop over the found classes
            for (Class aClass : classes) {

                // Find all the methods implemented by the class
                Method[] methods = aClass.getDeclaredMethods();

                // Loop over the methods
                for (Method method : methods) {

                    // ignore private methods
                    if (method.getName().contains("$")) {
                        continue;
                    }
                    // ignore methods that aren't desired
                    if (!desiredMethodNames.contains(method.getName())) {
                        continue;
                    }

                    // construct the Method object
                    String interfaceName = aInterface.getName();
                    interfaceName = interfaceName.substring(interfaceName.lastIndexOf(".") + 1,
                            interfaceName.length());

                    String className = aClass.getName();
                    className = className.substring(className.lastIndexOf(".") + 1,
                            className.length());

                    String methodName = method.getName();
                    String methodOutputType = method.getReturnType().getName();

                    String methodOutputDescription = "<empty>";

                    String methodDescription = "<empty>";

                    cz.zcu.kiv.GSOC_2018.structure.Method method1 = new cz.zcu.kiv.GSOC_2018.structure.Method(
                            interfaceName, className, methodName, methodOutputType, methodOutputDescription, methodDescription
                    );

                    // find all parameters of a method
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
                            String parameterName = parameterNamesList.get(i);
                            String parameterType = parameterTypesList.get(i);
                            String parameterDescription = "<empty>";
                            String parameterSetOfRecommendedValues = "<empty>";
                            String parameterRangeOfAllowedValues = "<empty>";
                            Parameter parameter = new Parameter(parameterName, parameterType, parameterDescription,
                                    parameterSetOfRecommendedValues, parameterRangeOfAllowedValues
                            );
                            // add the parameters to the method parameter array of a Method
                            method1.method_parameters.add(parameter);
                        }
                    } catch (ParameterNamesNotFoundException ex) {
                        System.out.println("-> Method has no parameters or they can't be accessed");
                    }
                    // add the current method to array of all methods
                    container.main_methods.add(method1);
                }
            }
        }

        // Convert the Container Java object to JSON, and save into a file
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String json = gson.toJson(container);

        String executionPath = null;
        try{
            executionPath = System.getProperty("user.dir");
            System.out.print("Executing at =>"+executionPath.replace("\\", "/"));
        }catch (Exception e){
            System.out.println("Exception caught ="+e.getMessage());
        }

        final String jsonLocation = executionPath + "/src/main/java/cz/zcu/kiv/GSOC_2018/json_output.txt";

        try (PrintWriter out = new PrintWriter(jsonLocation)) {
            out.println(json);
        }
    }
}
