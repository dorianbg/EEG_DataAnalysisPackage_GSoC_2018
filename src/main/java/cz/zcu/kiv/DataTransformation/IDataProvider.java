package cz.zcu.kiv.DataTransformation;

import java.util.List;

public interface IDataProvider {
    /**
     * @return epochs for training
     */
    public List<double[][]> getData();

    /**
     * @return data labels
     */
    public List<Double> getDataLabels();

}
