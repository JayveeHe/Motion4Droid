package io.petchat.libs.rfclassifier.RandomForestClassifier;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by jayvee on 15/10/28.
 */
public class DecisionTree {
    //    boolean isLeaf = false;
    DecisionTree leftNode = null;
    DecisionTree rightNode = null;
    Float leftLabel = null;
    Float rightLabel = null;
    float splitVal;
    int dimIndex;

    public DecisionTree(int dimIndex, float splitVal, Float leftLabel, Float rightLabel,
                        DecisionTree leftNode, DecisionTree rightNode) {
        this.dimIndex = dimIndex;
        this.splitVal = splitVal;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public static DecisionTree buildNodeByMap(Map inputMap) {
        float split_val = ((BigDecimal) inputMap.get("split_val")).floatValue();
        int dim_index = (int) inputMap.get("dim_index");
        DecisionTree leftNode = null;
        DecisionTree rightNode = null;
        Float leftLabel = null;
        Float rightLabel = null;
        Map leftMap = (Map) inputMap.get("left_node");
        if (null != leftMap) {
            //包含了左子树
            leftNode = buildNodeByMap(leftMap);
        } else {
            //达到了左叶子节点
            leftLabel = ((BigDecimal) inputMap.get("left_label")).floatValue();
        }
        Map rightMap = (Map) inputMap.get("right_node");
        if (null != rightMap) {
            //包含了左子树
            rightNode = buildNodeByMap(rightMap);
        } else {
            //达到了左叶子节点
            rightLabel = ((BigDecimal) inputMap.get("right_label")).floatValue();
        }
        return new DecisionTree(dim_index, split_val, leftLabel, rightLabel, leftNode, rightNode);
    }

    /**
     * classify input vector
     *
     * @param inputVec 12 feature
     * @return 0 or 1 label
     */
    public Float calLabel(float[] inputVec) {
        float dim_value = inputVec[this.dimIndex];
        float classifiedLabel;
        if (dim_value <= this.splitVal) {
            //go left
            if (null != this.leftNode) {
                classifiedLabel = this.leftNode.calLabel(inputVec);
            } else {
                classifiedLabel = this.leftLabel;
            }
        } else {
            //go right
            if (null != this.rightNode) {
                classifiedLabel = this.rightNode.calLabel(inputVec);
            } else {
                classifiedLabel = this.rightLabel;
            }
        }
        return classifiedLabel;
    }

    public DecisionTree getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(DecisionTree leftNode) {
        this.leftNode = leftNode;
    }


    public DecisionTree getRightNode() {
        return rightNode;
    }

    public void setRightNode(DecisionTree rightNode) {
        this.rightNode = rightNode;
    }

    public float getSplitVal() {
        return splitVal;
    }

    public void setSplitVal(float splitVal) {
        this.splitVal = splitVal;
    }

    public Float getRightLabel() {
        return rightLabel;
    }

    public Float getLeftLabel() {
        return leftLabel;
    }

    public int getDimIndex() {
        return dimIndex;
    }
}

