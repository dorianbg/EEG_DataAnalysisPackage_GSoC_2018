package cz.zcu.kiv.GSOC_2018.structure;

public class Parameter {
    public String parameter_name ;
    public String parameter_type;
    public String parameter_description;
    public String parameter_set_of_recommended_values;
    public String parameter_range_of_allowed_values;

    public Parameter(String parameter_name, String parameter_type, String parameter_description, String parameter_set_of_recommended_values, String parameter_range_of_allowed_values) {
        this.parameter_name = parameter_name;
        this.parameter_type = parameter_type;
        this.parameter_description = parameter_description;
        this.parameter_set_of_recommended_values = parameter_set_of_recommended_values;
        this.parameter_range_of_allowed_values = parameter_range_of_allowed_values;
    }
}
