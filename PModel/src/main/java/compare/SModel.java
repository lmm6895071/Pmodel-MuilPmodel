package compare;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by liuwei on 2017/3/20.
 * 情感词典模型
 */
public class SModel {
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
