package cz.zcu.kiv.GSOC_2018.structure;

import java.util.ArrayList;

public class Method {
    public String interface_name;
    public String class_name;
    public String method_name;
    public String method_output_type;
    public String method_output_description;
    public String method_description;

    public Method(String interface_name, String class_name, String method_name, String method_output_type, String method_output_description, String method_description) {
        this.interface_name = interface_name;
        this.class_name = class_name;
        this.method_name = method_name;
        this.method_output_type = method_output_type;
        this.method_output_description = method_output_description;
        this.method_description = method_description;
    }

    public ArrayList<Parameter> method_parameters = new ArrayList<>();
}