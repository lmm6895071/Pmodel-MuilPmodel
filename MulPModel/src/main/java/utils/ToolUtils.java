package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuwei on 2017/1/3.
 */
public class ToolUtils {

    public static final int MODEL_POS = 1;
    public static final int MODEL_NEG = -1;

    private static final double fScore(double precise,double recall){
        return 2*precise*recall/(precise+recall);
    }

    public static final double score(List<Integer> trueLabels,List<Integer> predictLabels){
        if(trueLabels == null || predictLabels == null || trueLabels.size() != predictLabels.size()){
            System.err.println("Error,Please check the true labels and predict labels");
            return -1;
        }
        int neg_true = 0,neg_predict = 0,neg_accurancy = 0;
        int pos_true = 0,pos_predict = 0,pos_accurancy = 0;
        for(Integer integer : trueLabels){
            if(integer == MODEL_POS){
                pos_true += 1;
            }
            else{
                neg_true += 1;
            }
        }

        for(Integer integer : predictLabels){
            if(integer == MODEL_POS){
                pos_predict += 1;
            }
            else{
                neg_predict += 1;
            }
        }

        for(int i=0; i<trueLabels.size();i++){
            if(predictLabels.get(i) == trueLabels.get(i)){
                if(predictLabels.get(i) == MODEL_POS){
                    pos_accurancy += 1;
                }
                else{
                    neg_accurancy += 1;
                }
            }
        }

        double pos_precise = pos_accurancy * 1.0 / pos_predict;
        double pos_recall = pos_accurancy * 1.0 / pos_true;
        double pos_F = fScore(pos_precise,pos_recall);

        double neg_precise = neg_accurancy * 1.0 / neg_predict;
        double neg_recall = neg_accurancy * 1.0 / neg_true;
        double neg_F = fScore(neg_precise,neg_recall);

        double mean_precise = (pos_precise * pos_true + neg_precise * neg_true)/(pos_true+neg_true);
        double mean_recall = (pos_recall * pos_true + neg_recall * neg_true)/(pos_true+neg_true);
//        double mean_F = (pos_F * pos_true + neg_F * neg_true)/(pos_true+neg_true);
        double mean_F = fScore(pos_F,neg_F);

        System.out.println("\t\tprecise\trecall\tF-score\tsupport");
        System.out.printf("pos\t\t%.3f\t%.3f\t%.3f\t%d",pos_precise,pos_recall,pos_F,pos_true);
        System.out.println();
        System.out.printf("neg\t\t%.3f\t%.3f\t%.3f\t%d",neg_precise,neg_recall,neg_F,neg_true);
        System.out.println();
        System.out.printf("mean\t%.3f\t%.3f\t%.3f\t%d",mean_precise,mean_recall,mean_F,(pos_true+neg_true));
        System.out.println();

        return mean_F;

    }

    public static List<Integer> posLen(int len){
        if(len < 0 ) return null;
        List<Integer> result = new ArrayList<>(len);
        for(int i=0; i<len;i++){
            result.add(MODEL_POS);
        }
        return result;
    }

    public static List<Integer> negLen(int len){
        if(len < 0 ) return null;
        List<Integer> result = new ArrayList<>(len);
        for(int i=0; i<len;i++){
            result.add(MODEL_NEG);
        }
        return result;
    }

    public static List<Integer> range(int size){
        List<Integer> result = new ArrayList<>(size);
        for(int i=0; i<size;i++){
            result.add(i);
        }
        return result;
    }

    public static List<String> randomSelect(List<String> raw_data,int size){
        List<Integer> shuffleList = range(raw_data.size());
        Collections.shuffle(shuffleList);
        List<String> result = new LinkedList<String>();
        if(size <=0){
            return raw_data;
        }
        else{
            for(int i=0; i<size;i++){
                result.add(raw_data.get(shuffleList.get(i)));
            }
            return result;
        }
    }


    public static void main(String[] args) {
        List<Integer> trueLabels = new ArrayList<>();
        List<Integer> predictLabels = new ArrayList<>();
        int[] trueA = {1,-1,1,-1,-1};
        int[] preA = {1,-1,1,-1,1};
        for(Integer integer : trueA){
            trueLabels.add(integer);
        }
        for(Integer integer : preA){
            predictLabels.add(integer);
        }
        score(trueLabels,predictLabels);

    }
}
