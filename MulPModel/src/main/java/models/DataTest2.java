package models;

import utils.ToolUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuwei on 2017/1/7.
 */
public class DataTest2 {

    public static void main(String[] args) throws Exception {
        String path = "./src/main/resources/datas/";
        String fname = "ch_hotel_corpus.txt";
//        String fname = "ch_waimai2_corpus.txt";
        System.out.println("this is the  first step of data load");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + fname)));
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/liuwei/data/raw/fu/cut_pos.txt")));
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/liuwei/workspace/pycharm/graduation/Final/true_pos.txt")));
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./src/main/resources/datas/weibo0.txt")));
        List<String> datas = new ArrayList<>();
        List<String> pos_data = new ArrayList<>();
        List<String> neg_data = new ArrayList<>();

        List<Integer> labels = new ArrayList<>();
        String line = null;
        while ((line = br.readLine()) != null) {
            pos_data.add(line);
        }
        br.close();

        br = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/liuwei/data/raw/fu/cut_neg.txt")));
//        br = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/liuwei/workspace/pycharm/graduation/Final/true_neg.txt")));
//        br = new BufferedReader(new InputStreamReader(new FileInputStream("./src/main/resources/datas/weibo1.txt")));
        while ((line = br.readLine()) != null) {
            neg_data.add(line);
        }
        br.close();

//        int pos_count = 12000,neg_count =12000;
//        int pos_count = pos_data.size(),neg_count =neg_data.size();
        int pos_count = Math.min(pos_data.size(),neg_data.size());
        int neg_count = Math.min(pos_data.size(),neg_data.size());
        pos_data = ToolUtils.randomSelect(pos_data,pos_count);
        neg_data = ToolUtils.randomSelect(neg_data,neg_count);
        datas.addAll(pos_data);
        datas.addAll(neg_data);
        labels.addAll(ToolUtils.posLen(pos_data.size()));
        labels.addAll(ToolUtils.negLen(neg_data.size()));

        float size = 0.3f;
        int boundary = (int) (size * datas.size());
        LinkedList<String> train_data = new LinkedList<String>();
        LinkedList<Integer> train_y = new LinkedList<Integer>();
        LinkedList<String> test_data = new LinkedList<String>();
        LinkedList<Integer> test_y = new LinkedList<Integer>();

        List<Integer> shuffleList = ToolUtils.range(datas.size());
        Collections.shuffle(shuffleList);
        for (int i = 0; i < boundary; i++) {
            test_data.add(datas.get(shuffleList.get(i)));
            test_y.add(labels.get(shuffleList.get(i)));
        }
        for (int i = boundary; i < datas.size(); i++) {
            train_data.add(datas.get(shuffleList.get(i)));
            train_y.add(labels.get(shuffleList.get(i)));
        }

        DModel model = new DModel();
        long start = System.currentTimeMillis();
        model.fit(train_data, train_y);
        long end = System.currentTimeMillis();
        System.out.println("fit time is:" + (end - start));
        List<Integer> pred_y = model.predict(test_data);
        start = System.currentTimeMillis();
        System.out.println("predict time is:" + (start - end));

        ToolUtils.score(test_y, pred_y);
//        model.dump("true.model");
    }
}
