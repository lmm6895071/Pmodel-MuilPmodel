package compare;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import models.PInterfaces;
import utils.ToolUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

//import com.sun.tools.javac.code.Attribute;

/**
 * 一般基于情感词典的方法
 * Created by liuwei on 2017/1/5.
 */
public class Model implements PInterfaces{

    public static final int TYPE_POS = 1;
    public static final int TYPE_NEG = 2;
    public static final int TYPE_STOP = 3;

    private HashSet<String> posDicts;
    private HashSet<String> negDicts;
    private HashSet<String> stopwords;

    private JiebaSegmenter segmenter;

    public Model(){
        posDicts = new HashSet<>();
        negDicts = new HashSet<>();
        stopwords = new HashSet<>();
        init();
        segmenter = new JiebaSegmenter();
    }

    private void loadDict(String path,int type) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String line = null;
        if(type == TYPE_POS){
            while((line = br.readLine()) != null){
                posDicts.add(line);
            }
        }
        else if(type == TYPE_NEG){
            while((line = br.readLine()) != null){
                negDicts.add(line);
            }
        }
        else{
            while((line = br.readLine()) != null){
                stopwords.add(line);
            }
        }
        br.close();
    }

    private void init() {
        try{
            loadNegDict("./src/main/resources/dicts/hownet_neg.txt");
            loadNegDict("./src/main/resources/dicts/ntusd-negative.txt");
            loadPosDict("./src/main/resources/dicts/hownet_pos.txt");
            loadPosDict("./src/main/resources/dicts/ntusd-positive.txt");
            loadStopWords("./src/main/resources/dicts/stopword.txt");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadStopWords(String path) throws IOException {
        loadDict(path,TYPE_STOP);
    }

    private void loadNegDict(String path) throws IOException {
        loadDict(path,TYPE_NEG);
    }

    private void loadPosDict(String path) throws IOException {
        loadDict(path,TYPE_POS);
    }

    private List<String> spilt(String document){
        return segmenter.sentenceProcess(document);
    }

    private void checkDict(){
        for(String word : negDicts){
            if(posDicts.contains(word)){
                System.out.println(word);
            }
        }
    }

    @Override
    public List<Integer> predict(List<String> documents) {
        List<Integer> result = new ArrayList<>(documents.size());
        for(String document : documents){
            List<String> words = spilt(document);
            int score = 0;
            for(String word : words){
                if(negDicts.contains(word) && !posDicts.contains(word)){
                    score -= 1;
                }
                else if(!negDicts.contains(word) && posDicts.contains(word)){
                    score += 1;
                }
                else{
                }
            }
            if(score >= 0){
                result.add(1);
            }
            else{
                result.add(-1);
            }
        }
        return result;
    }

    @Override
    public void fit(List<String> documents, List<Integer> labels) {

    }

    public static void main(String[] args) throws IOException {
        Model model = new Model();
        String path = "./src/main/resources/datas/";
 //       String fname = "ch_hotel_corpus.txt";
        String fname = "ch_waimai2_corpus.txt";

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + fname)));
        List<String> datas = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.substring(0, 3).equals("neg")) {
                labels.add(-1);
            } else {
                labels.add(1);
            }
            datas.add(line.substring(4));
        }
        br.close();

        List<Integer> predicts = model.predict(datas);
        ToolUtils.score(labels,predicts);
    }

    /**
     * Created by liuwei on 2017/3/20.
     * 情感词典模型
     */
    public static class SModel {
        public static final String MERGE_DICT = "MERGE_DICT";

        private String[] POS_DICT_PATHS = {"",""};
        private String[] NEG_DICT_PATHS = {"",""};

        private HashMap<String,Integer> posDicts = new HashMap();
        private HashMap<String,Integer> negDicts = new HashMap();
        private HashSet<String> stopwords = new HashSet<>();

        private JiebaSegmenter segmenter = new JiebaSegmenter();

        public SModel(){
            defaultLoad();
            merge();
            WordDictionary.getInstance().loadUserDict(Paths.get(MERGE_DICT));
        }

        public void loadNegDict(String path) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = null;
            while ((line = br.readLine()) != null) {
                Integer fre = negDicts.get(line);
                if(fre == null){
                    fre = 0;
                }
                negDicts.put(line,fre+1);
            }
            br.close();
        }

        public void loadPosDict(String path) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = null;
            while ((line = br.readLine()) != null) {
                Integer fre = posDicts.get(line);
                if(fre == null){
                    fre = 0;
                }
                posDicts.put(line,fre+1);
            }
            br.close();
        }

        public void loadStopWords(String path) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = null;
            while ((line = br.readLine()) != null) {
                stopwords.add(line.trim());
            }
            br.close();
        }

        private void merge(){
            HashSet<String> common = new HashSet<>();
            for(String s: negDicts.keySet()){
                if(posDicts.containsKey(s)){
                    common.add(s);
                }
            }
            for(String s: common){
                negDicts.remove(s);
            }
            for(String s: common){
                posDicts.remove(s);
            }

            try {
                FileOutputStream outputStream = new FileOutputStream(MERGE_DICT);
                Iterator<Map.Entry<String,Integer>> iter = negDicts.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,Integer> entry = iter.next();
                    outputStream.write((entry.getKey()+"\t "+entry.getValue()+"\n").getBytes());
                }
                outputStream.close();

                outputStream = new FileOutputStream(MERGE_DICT,true);
                iter = negDicts.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,Integer> entry = iter.next();
                    outputStream.write((entry.getKey()+"\t "+entry.getValue()+"\n").getBytes());
                }
                outputStream.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }

        public void defaultLoad()  {
            try {
                loadNegDict("./src/main/resources/dicts/hownet_neg.txt");
                loadNegDict("./src/main/resources/dicts/ntusd-negative.txt");
                loadPosDict("./src/main/resources/dicts/hownet_pos.txt");
                loadPosDict("./src/main/resources/dicts/ntusd-positive.txt");
                loadStopWords("./src/main/resources/dicts/stopword.txt");
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        public List<String> spilt(String document){
            List<String> raw = segmenter.sentenceProcess(document);
            List<String> result = new ArrayList<>();
            for(String s:raw){
                if(!stopwords.contains(s)&& s.length() >=2){
                    result.add(s);
                }
            }
            return result;
        }

        private int judgle(List<String> tokens) {
            int score = 0;
            for(String s: tokens){
                if(posDicts.containsKey(s)){
                    score += 1;
                }
                else if (negDicts.containsKey(s)){
                    score -= 1;
                }
            }
            return score;
        }

        public List<Integer> predict(List<String> documents){
            List<Integer> result = new ArrayList<>(documents.size());
            for (String document : documents) {
                List<String> tokens = spilt(document);
                int score = judgle(tokens);
                if(score >=0){
                    result.add(1);
                }
                else{
                    result.add(-1);
                }
            }
            return result;
        }

        public void fit(List<String> documents, List<Integer> labels) {

        }

        public static void main(String[] args) throws IOException {
    //        new SModel();
        }



    }
}
