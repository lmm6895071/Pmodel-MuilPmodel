package models;

import java.util.List;

/**
 * Created by liuwei on 2017/1/3.
 */
public interface PInterfaces {

    List<Integer> predict(List<String> documents);

    void fit(List<String> documents,List<Integer> labels);

}
