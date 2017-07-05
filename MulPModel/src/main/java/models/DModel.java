package models;

import com.huaban.analysis.jieba.JiebaSegmenter;
import utils.ToolUtils;
import java.io.*;
import java.util.*;

/**
 * Created by liuwei on 2017/1/13.
 */
public class DModel extends TreeModel{

    public static final int DEFAULT_SHUFFLE_COUNT = 20;
    public static final int DEFAULT_ITER_COUNT = 20;
    public static  int DEFAULT_TREE_COUNT = 60;
    public static final double DEFAULT_THRESHOLD = 0.4;
    public static final double DEFAULT_LEARNING_RATE =0.05;
    public static final double DEFAULT_WORD_WEIGHT = 0.005;
    public static final  int DEFAULT_STRANGS = 10;


    public class TreeNode{
        double score;
        char ch;
        ArrayList<TreeNode> children;
        TreeNode parent;

        TreeNode(char ch,double score){
            this.ch = ch;
            this.score = score;
        }
    }

    private TreeNode[] model;
    private JiebaSegmenter segmenter;

    private int shuffle_count;
    private int iter_count;
    private int trees;
    private double threshold;
    private double rate;
    private double word_weight;

    private OutputStream outputStream;
    public   HashMap<String,Double> IDFMap =new HashMap<>();


    public DModel(){
        this(DEFAULT_SHUFFLE_COUNT,
                DEFAULT_ITER_COUNT,
                DEFAULT_TREE_COUNT,
                DEFAULT_THRESHOLD,
                DEFAULT_LEARNING_RATE,
                DEFAULT_WORD_WEIGHT);
    }

    public DModel(int shuffle,int iter,int trees,double threshold,double rate,double weight) {
        this.shuffle_count = shuffle;
        this.iter_count = iter;
        this.trees = trees;
        this.threshold = threshold;
        this.rate = rate;
        this.word_weight = weight;
        model = new TreeNode[trees];
        for(int i=0; i<trees;i++){
            model[i] = new TreeNode('/',0);
            model[i].parent = null;
        }
        segmenter = new JiebaSegmenter();
    }

    private TreeNode getTree(char ch){
        int index = ch % trees;
        return model[index];
    }

    private TreeNode getNode(TreeNode parent,char ch){
        if(parent == null || parent.children == null || parent.children.size() == 0) return null;
        for(TreeNode node : parent.children){
            if(node.ch == ch){
                return node;
            }
        }
        return null;
    }
    public void initidf() throws  Exception{
        String path = "./src/main/resources/datas/";
        String fname = "hotel_idf.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path + fname)));
        String line = null;
        while ((line = br.readLine()) != null) {
            String []results=line.split(":");
            this.IDFMap.put(results[0],Double.parseDouble(results[1]));
            //System.out.println(results[0]+" "+results[1]);
        }
        br.close();

    }
    @Override
    public List<Integer> predict(List<String> documents) {
        if(documents == null) return null;
        List<Integer> result = new ArrayList<>(documents.size());
        for(String document : documents){
            List<String> words = spilt(document);
            double score = formatScore(sumBranchs(words));
            if(score >=0){
                result.add(1);
            }
            else{
                result.add(-1);
            }
        }
        return result;
    }

    @Override
    public void fit(List<String> documents, List<Integer> labels) throws Exception {
        if(documents == null || labels == null || documents.size() != labels.size()){
            throw new Exception("Please check the input train data dimension");
        }
        //initidf();
        initBranchs(documents,labels);
        train(documents,labels);
    }
    public static LinkedList<Double> myresults = new LinkedList<Double>();
    private void train(List<String> documents,List<Integer> labels){
        List<Integer> array = ToolUtils.range(documents.size());
        for(int i=0; i<shuffle_count;i++){
            Collections.shuffle(array);
            for(int j=0; j<documents.size();j++){

                String document = documents.get(array.get(j));
                int label = labels.get(array.get(j));
                int iter_cur = 1;
                List<String> words = spilt(document);
                double score = formatScore(sumBranchs(words));

                while(iter_cur < iter_count && isNotOk(score,label)){
                    double error = (label - score) * rate;
                    double sum_weight=0;

                    for(String w :words){
                        double temp = sumBranch(w);
                        if (temp <0)
                            temp=-1*temp;
                        sum_weight+=temp;
                    }

                    for(String word:words){
                        double wscore=formatScore(sumBranch(word));
                        if (wscore <0)
                           wscore=wscore*(-1);
                        updateBranch(word,error*wscore/sum_weight);
                    }
                    score = formatScore(sumBranchs(words));
                    iter_cur = iter_cur + 1;
                }
            }
            System.out.println("Finish shuffle:"+(i+1)+"......");
            List<Integer> result = predict(documents);
            double resultss=ToolUtils.score(labels,result);
            //myresults.add(resultss);
        }
    }

    private void initBranchs(List<String> documents,List<Integer> labels){
        HashMap<String,Double> weights = new HashMap<>();
        String document = null;
        int label = 0;
        double averageW = 0;
        Set set = IDFMap.keySet();

        for(int i=0; i<documents.size();i++){
            document = documents.get(i);
            label = labels.get(i);
            List<String> words = spilt(document);
            averageW = label * 1.0 ;/// words.size();
            double sumWeight=0;
            for(String ws :words){
                if(IDFMap.containsKey(ws)) {
                    double temp = IDFMap.get(ws);
                    sumWeight+= temp ;
                }
            }
            for(String word: words){
                Double w = weights.get(word);
                if(w == null){
                    w = 0d;
                }
                //weights.put(word,w+averageW);
                if(set.contains("咨询")){
                    System.out.println("---------咨询");
                }
                if(IDFMap.containsKey(word)) {

                    double temp = IDFMap.get(word);
                    weights.put(word, w + temp * label);
                    System.out.println("---------------------");
                }
                else{
                    weights.put(word,w+averageW/words.size());
                }
            }
        }
        Iterator<Map.Entry<String,Double>> iterator = weights.entrySet().iterator();
        int count = 0;
        while(iterator.hasNext()){
            Map.Entry<String,Double> entry = iterator.next();
            if(entry.getValue() >= word_weight || entry.getValue() <= -1*word_weight){
                addBranch(entry.getKey(),entry.getValue());
                count ++;
            }
        }
        System.out.println("Add branch count:"+count);
    }

    @Override
    protected void addBranch(String word, double initValue) {
        if(word == null || word.length() == 0) return;
        TreeNode parent = getTree(word.charAt(0));
        TreeNode cur = null;
        for(int i=0; i<word.length(); i++){
            cur = getNode(parent,word.charAt(i));
            if(cur == null){
                TreeNode newNode = new TreeNode(word.charAt(i),initValue);
                newNode.parent = parent;
                if(parent.children == null) parent.children = new ArrayList<>();
                parent.children.add(newNode);
                parent = newNode;
            }
            else{
                cur.score = (cur.score + initValue)/2;
                parent = cur;
            }
        }
    }

    @Override
    protected void updateBranch(String word, double error) {
        if(word == null || word.length() == 0) return;
        TreeNode parent = getTree(word.charAt(0));
        TreeNode cur = null;

        for(int i=0; i<word.length();i++){
            double lError = (i+1) / word.length()  * error ;
            cur = getNode(parent,word.charAt(i));
            if(cur != null){
                cur.score += lError;
                parent = cur;
            }
            else{
                TreeNode newRoot = getTree(word.charAt(i));
                TreeNode newNode = getNode(newRoot,word.charAt(i));
                if(newNode == null){
                    String ws= word.substring(i,word.length());
//                    System.out.print(word+": "+ ws+"\n");
                    addBranch(ws,lError);
                    break;
                }
                else{
                    updateBranch(word.substring(i),lError);
                }
            }
        }
    }

    @Override
    protected double sumBranch(String word) {
        if(word == null || word.length() == 0) return 0;
        double score = 0;
        TreeNode parent = getTree(word.charAt(0));
        TreeNode cur = null;
        for(int i=0;i<word.length();i++){
            cur = getNode(parent,word.charAt(i));
            if(cur == null){
                TreeNode newRoot = getTree(word.charAt(i));
                TreeNode newNode = getNode(newRoot,word.charAt(i));
                if(newNode == null){
                    return score;
                }
                else{
                    return score + sumBranch(word.substring(i));
                }
            }
            else{
                score += cur.score;
                parent = cur;
            }
        }
        return score;
    }

    @Override
    protected double sumBranchs(List<String> words) {
        double score = 0;
        for(String word : words){
            score += sumBranch(word);
        }
        return score;
    }

    @Override
    protected List<String> spilt(String document) {
//        return segmenter.sentenceProcess(document);
        String[] strings = document.split("\t");
        List<String> result= new ArrayList<>();
        for(String word :strings){
           // if(word.length()>=2)
                result.add(word);
        }
        return result;
    }

    @Override
    protected double formatScore(double score) {
        return Math.tanh(score);
    }

    @Override
    protected boolean isNotOk(double score, int label) {
        if(score >= -1 * threshold && score <= threshold || score * label <= 0){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void dump(String path) throws IOException {
        outputStream = new FileOutputStream(path);
        for(int i=0; i<trees;i++){
            System.out.println("tree:"+(i+1));
            dumpTree(model[i],path);
            System.out.println("=========================");
        }
        outputStream.close();
    }

    private void dumpTree(TreeNode root,String path) throws IOException {
        if(root.children == null){
            String result = "";
            while (root != null){
                result = root.ch+"="+root.score+" "+result;
                root = root.parent;
            }
//            System.out.println(result);
            outputStream.write((result+"\n").getBytes());
        }
        else{
            for(TreeNode node:root.children){
                dumpTree(node,path);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        DModel model = new DModel();
        model.addBranch("斗鱼直播",1);
        model.addBranch("斗鱼tv",0);
        model.addBranch("斗鱼女主播",-1);
        model.addBranch("火箭",1);
        System.out.println(model.sumBranch("斗鱼火箭"));
        model.initidf();
    }

}
