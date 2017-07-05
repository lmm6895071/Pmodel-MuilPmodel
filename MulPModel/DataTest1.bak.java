package models;

import compare.Model;
import utils.ToolUtils;
import compare.SModel;
/*
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
 */


import java.io.*;
import java.util.*;
//import java.util.List;
import java.util.*;

/**
 * Created by liuwei on 2017/1/13.
 */
public class DataTest1 {

    public static void main(String[] args) throws Exception {

        String path = "./src/main/resources/datas/";
       //String fname = "ch_hotel_corpus.txt";
        String fname = "ch_waimai2_corpus.txt";
        System.out.println("this is the  first step of data load");
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + fname)));
        List<String> datas = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        String line = null;

        int ngcount=4000;
        int pscount=4000;
        while ((line = br.readLine()) != null) {
            if (line.substring(0, 3).equals("neg")) {
                labels.add(-1);
                ngcount--;
                if (ngcount==0)
                    continue;

            } else {
                labels.add(1);
                pscount--;
                if (pscount ==0)
                    continue;
            }
            datas.add(line.substring(4));

        }
        br.close();



        System.out.println("datas count");
        System.out.println(datas.size());

        float size = 0.2f;
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
       List<Double> myresults=new ArrayList<Double>();

        for(int index =21;index<=21;index++){
            DModel.DEFAULT_TREE_COUNT =200;
            DModel model = new DModel();

            //SModel model = new SModel();
            long start = System.currentTimeMillis();
            System.out.println(train_data.size());
            System.out.println(test_data.size());
            model.fit(train_data, train_y);
            long end = System.currentTimeMillis();
            System.out.println("fit time is:" + (end - start));
            List<Integer> pred_y = model.predict(test_data);
            start = System.currentTimeMillis();
            System.out.println("predict time is:" + (start - end));
            Double results = ToolUtils.score(test_y, pred_y);
           myresults.add(results);
        }
        for( Double x :myresults)        System.out.print( x+",");

    }
}
