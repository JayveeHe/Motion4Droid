package io.petchat.libs.rfclassifier.RandomForestClassifier;

import com.alibaba.fastjson.JSON;
import io.petchat.libs.rfclassifier.Utils.FileUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jayvee on 15/10/29.
 */
public class RandomForest {
    ArrayList<Map> forest;

    public RandomForest(ArrayList<Map> forest) {
        this.forest = forest;
    }

    public RandomForest(String paraPath) {
        this.forest = loadRandomForestByJSON(paraPath);
    }

    public int voteClassifyVec(float[] inputVec) {
        int[] votes = new int[2];
        for (Map singleDecisionTree : forest) {
            float weight = (float) singleDecisionTree.get("weight");
            DecisionTree tree = (DecisionTree) singleDecisionTree.get("tree");
            float label = tree.calLabel(inputVec);
            votes[label > 0 ? 1 : 0] += 1;
        }

//        return result;
        return votes[0] > votes[1] ? 0 : 1;
    }

    public float[][] getClassifyProb(float[] inputVec) {
        int[] votes = new int[2];
        float[][] result = new float[2][2];
        for (Map singleDecisionTree : forest) {
            float weight = (float) singleDecisionTree.get("weight");
            DecisionTree tree = (DecisionTree) singleDecisionTree.get("tree");
            float label = tree.calLabel(inputVec);
            result[label > 0 ? 1 : 0][0] += 1.0f;
        }
        result[0][1] = result[0][0] / forest.size();
        result[1][1] = result[1][0] / forest.size();

        return result;
//        return votes[0] > votes[1] ? 0 : 1;
    }

    public static ArrayList<Map> loadRandomForestByFile(String jsonPath) {
        String strjson = FileUtils.File2str(jsonPath, "utf-8");
        List<Map<String, Map>> forest = (List<Map<String, Map>>) JSON.parse(strjson);
        System.out.println(forest);
        ArrayList<Map> randomForest = new ArrayList<>(0);
        for (Map singleTree : forest) {
            float tree_weight = ((BigDecimal) singleTree.get("weight")).floatValue();
            Map tree = (Map) singleTree.get("tree");
            DecisionTree decisionTree = DecisionTree.buildNodeByMap(tree);
            Map singleDecisionTree = new HashMap();
            singleDecisionTree.put("weight", tree_weight);
            singleDecisionTree.put("tree", decisionTree);
            randomForest.add(singleDecisionTree);
        }
        return randomForest;
    }

    public static ArrayList<Map> loadRandomForestByJSON(String jsonStr) {
        List<Map<String, Map>> forest = (List<Map<String, Map>>) JSON.parse(jsonStr);
        System.out.println(forest);
        ArrayList<Map> randomForest = new ArrayList<>(0);
        for (Map singleTree : forest) {
            float tree_weight = ((BigDecimal) singleTree.get("weight")).floatValue();
            Map tree = (Map) singleTree.get("tree");
            DecisionTree decisionTree = DecisionTree.buildNodeByMap(tree);
            Map singleDecisionTree = new HashMap();
            singleDecisionTree.put("weight", tree_weight);
            singleDecisionTree.put("tree", decisionTree);
            randomForest.add(singleDecisionTree);
        }
        return randomForest;
    }

}
