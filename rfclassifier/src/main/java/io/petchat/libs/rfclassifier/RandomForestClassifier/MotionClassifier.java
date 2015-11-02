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
    public static final int SIT = 1993;
    public static final int DRIVE = 1994;

    private RandomForest rf_Rid_WalkRun;
    private RandomForest rf_Walk_Run;
    private RandomForest rf_Drive_Sit;
    private RandomForest rf_DriveSit_WalkRunRid;

    public MotionClassifier(Context context) throws IOException {
        String str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_drivesit_walkrunrid)), "utf-8");
        this.rf_DriveSit_WalkRunRid = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_rid_walkrun_forest_31)), "utf-8");
        this.rf_Rid_WalkRun = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_walkrun_forest_31)), "utf-8");
        this.rf_Walk_Run = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_drivesit)), "utf-8");
        this.rf_Drive_Sit = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
    }

    /**
     * classify user's recent motion by RandomForest, with input data of 50 acc sensor readings.
     *
     * @param rawXYZ raw data of acc sensor and magnetic sensor readings,
     *               float[50][6], (acc_x,acc_y,acc_z,mag_x,mag_y,mag_z)
     */
    public int classifyMotionByRF(float[][] rawXYZ) throws IOException {
//        try {
        float[] inputXYZData = SensorDataUtils.transXYZ2InputData(rawXYZ, 0);//TODO throw the exception to the caller?
        if (0 == rf_DriveSit_WalkRunRid.voteClassifyVec(inputXYZData)) {//0~walk or run or rid, 1~drive or sit
            if (0 == rf_Rid_WalkRun.voteClassifyVec(inputXYZData)) {//1~rid, 0~walk or run
                if (0 == rf_Walk_Run.voteClassifyVec(inputXYZData)) {//1~walk, 0~run
                    return RUN;
                } else {
                    return WALK;
                }
            } else {
                return RIDE;
            }
        } else {
            // to classify drive and sit, add magnetic data and normalize data first.
            rawXYZ = SensorDataUtils.normalizeMagnetXYZData(rawXYZ);
            float[] inputNormalXYZData = SensorDataUtils.transXYZ2InputData(rawXYZ, 0);
            float[] inputNormalMagData = SensorDataUtils.transXYZ2InputData(rawXYZ, 3);
            int normalDim = inputNormalXYZData.length + inputNormalMagData.length;
            float[] inputNormalData = new float[normalDim];
            // combine xyz and magnetic data
            for (int i = 0; i < 3; i++) {
                inputNormalData[(i * 8)] = inputNormalXYZData[0];
                inputNormalData[1 + i * 8] = inputNormalXYZData[1];
                inputNormalData[2 + i * 8] = inputNormalXYZData[2];
                inputNormalData[3 + i * 8] = inputNormalXYZData[3];
                inputNormalData[4 + i * 8] = inputNormalMagData[0];
                inputNormalData[5 + i * 8] = inputNormalMagData[1];
                inputNormalData[6 + i * 8] = inputNormalMagData[2];
                inputNormalData[7 + i * 8] = inputNormalMagData[3];
            }
            if (0 == rf_Drive_Sit.voteClassifyVec(inputNormalData)) {
                return DRIVE;
            } else {
                return SIT;
            }
        }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return 0;
    }

}
