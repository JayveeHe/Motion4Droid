package io.petchat.libs.rfclassifier.RandomForestClassifier;

import android.content.Context;
import io.petchat.libs.rfclassifier.R;
import io.petchat.libs.rfclassifier.Utils.FileUtils;
import io.petchat.libs.rfclassifier.Utils.SensorDataUtils;

import java.io.IOException;

/**
 * Created by jayvee on 15/11/1.
 */
public class MotionClassifier {
    public static final int WALK = 1990;
    public static final int RUN = 1991;
    public static final int RIDE = 1992;

    private RandomForest rf_Rid_WalkRun;
    private RandomForest rf_Walk_Run;

    public MotionClassifier(Context context) throws IOException {
        String str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_rid_walkrun_forest_31)), "utf-8");
        this.rf_Rid_WalkRun = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_walkrun_forest_31)), "utf-8");
        this.rf_Walk_Run = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));

    }

    /**
     * classify user's recent motion by RandomForest, with input data of 50 acc sensor readings.
     *
     * @param rawXYZ raw data of acc sensor readings, float[50][12]
     */
    public int classifyMotionByRF(float[][] rawXYZ) throws IOException {
//        try {
        float[] inputData = SensorDataUtils.transXYZ2InputData(rawXYZ);//TODO throw the exception to the caller?
        if (1 != rf_Rid_WalkRun.voteClassifyVec(inputData)) {//1~rid, 0~walk or run
            if (1 == rf_Walk_Run.voteClassifyVec(inputData)) {//1~walk, 0~run
                return WALK;
            } else {
                return RUN;
            }
        } else {
            return RIDE;
        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return 0;
    }

}
