package models;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.IOException;
import java.util.List;

/**
 * Created by liuwei on 2017/1/13.
 */
public abstract class TreeModel {

    public abstract List<Integer> predict(List<String> documents);

    public abstract void fit(List<String> documents, List<Integer> labels) throws Exception;

    protected abstract void addBranch(String word,double initValue);

    protected abstract void updateBranch(String word,double error);

    protected abstract double sumBranch(String word);

    protected abstract double sumBranchs(List<String> words);

    protected abstract List<String> spilt(String document);

    protected abstract double formatScore(double score);

    protected abstract boolean isNotOk(double score,int label);

    public abstract void dump(String path) throws IOException;

}
