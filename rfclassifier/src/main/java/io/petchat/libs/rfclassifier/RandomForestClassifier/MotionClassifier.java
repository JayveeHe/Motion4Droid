package io.petchat.libs.rfclassifier.RandomForestClassifier;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import io.petchat.libs.rfclassifier.R;
import io.petchat.libs.rfclassifier.Utils.FileUtils;
import io.petchat.libs.rfclassifier.Utils.SensorDataUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jayvee on 15/11/1.
 */
public class MotionClassifier {
    public static final int WALK = 1990;
    public static final int RUN = 1991;
    public static final int RIDE = 1992;
    public static final int SIT = 1993;
    public static final int DRIVE = 1994;
    public static final int DRIVE_OR_SIT = 1995;

    private RandomForest rf_Rid_WalkRun;
    private RandomForest rf_Walk_Run;
    private RandomForest rf_Drive_Sit;
    private RandomForest rf_DriveSit_WalkRunRid;
    private RandomForest rf_WatchPhone;

    public MotionClassifier(Context context) throws IOException {
        // loading assets resources
        AssetManager assetManager = context.getAssets();
        String str_params = new String(FileUtils.InputStreamTOByte(assetManager.open("rf_para_drivesit_walkrunrid.json")), "utf-8");
        this.rf_DriveSit_WalkRunRid = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(assetManager.open("rf_para_rid_walkrun.json")), "utf-8");
        this.rf_Rid_WalkRun = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(assetManager.open("rf_para_walkrun.json")), "utf-8");
        this.rf_Walk_Run = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(assetManager.open("rf_para_drivesit_non_norm.json")), "utf-8");
        this.rf_Drive_Sit = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
        str_params = new String(FileUtils.InputStreamTOByte(assetManager.open("rf_para_watchphone.json")), "utf-8");
        this.rf_WatchPhone = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
//        String str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_rf_para_drivesit_walkrunrid)), "utf-8");
//        this.rf_DriveSit_WalkRunRid = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
//        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_rf_para_rid_walkrun)), "utf-8");
//        this.rf_Rid_WalkRun = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
//        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_rf_para_walkrun)), "utf-8");
//        this.rf_Walk_Run = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
//        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.rf_para_drivesit_non_norm)), "utf-8");
//        this.rf_Drive_Sit = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
//        str_params = new String(FileUtils.InputStreamTOByte(context.getResources().openRawResource(R.raw.para_watchphone)), "utf-8");
//        this.rf_WatchPhone = new RandomForest(RandomForest.loadRandomForestByJSON(str_params));
    }

    /**
     * classify user's recent motion by RandomForest, with input data of 50 acc sensor readings.
     *
     * @param rawXYZ raw data of acc sensor and magnetic sensor readings,
     *               float[50][6], (acc_x,acc_y,acc_z,mag_x,mag_y,mag_z)
     */
    public int classifyMotionByRF(float[][] rawXYZ) throws IOException, IndexOutOfBoundsException {
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
        } else if (rawXYZ[0].length == 6) {
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
        } else {
            // without magnetic data or input data dim !=6
            return DRIVE_OR_SIT;
        }
    }

    /**
     * classify user's recent motion by RandomForest, with input data of 50 acc sensor and magnetic sensor data readings.
     *
     * @param accXYZ raw data of acc sensor and magnetic sensor readings,
     *               float[50][3], (acc_x,acc_y,acc_z)
     * @param magXYZ magnetic data,XYZ,flaot[50][3],(mag_x,mag_y,mag_z)
     */
    public int classifyMotionByRF(float[][] accXYZ, float[][] magXYZ) throws IOException, IndexOutOfBoundsException {
//        try {
        float[] inputXYZData = SensorDataUtils.transXYZ2InputData(accXYZ, 0);//TODO throw the exception to the caller?
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
            accXYZ = SensorDataUtils.normalizeMagnetXYZData(accXYZ);
            float[] inputNormalXYZData = SensorDataUtils.transXYZ2InputData(accXYZ, 0);
            float[] inputNormalMagData = SensorDataUtils.transXYZ2InputData(magXYZ, 0);
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
    }

    /**
     * classify user's recent motion by RandomForest, with input data of 50 acc sensor readings.
     *
     * @param rawXYZ raw data of acc sensor and magnetic sensor readings,
     *               float[50][3], (acc_x,acc_y,acc_z)
     */
    public int simpleClassifyMotionByRF(float[][] rawXYZ) throws IOException, IndexOutOfBoundsException {
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
            if (0 == rf_Drive_Sit.voteClassifyVec(inputXYZData)) {//0~drive,1~sit
                return DRIVE;
            } else {
                return SIT;
            }
        }
    }

    public Map<String, Float> probClassifyMotionByRF(float[][] rawXYZ) throws IOException, IndexOutOfBoundsException {
        float[] inputXYZData = SensorDataUtils.transXYZ2InputData(rawXYZ, 0);//TODO throw the exception to the caller?
        float[][] drivesit_walkrunrid_result = rf_DriveSit_WalkRunRid.getClassifyProb(inputXYZData);
        float[][] rid_walkrun_result = rf_Rid_WalkRun.getClassifyProb(inputXYZData);
        float[][] walk_run_result = rf_Walk_Run.getClassifyProb(inputXYZData);
        float[][] drive_sit_result = rf_Drive_Sit.getClassifyProb(inputXYZData);
        Map<String, Float> mapResult = new HashMap<>();
        float driveProb = drivesit_walkrunrid_result[1][1] * drive_sit_result[0][1];
        float sitProb = drivesit_walkrunrid_result[1][1] * drive_sit_result[1][1];
        float rideProb = drivesit_walkrunrid_result[0][1] * rid_walkrun_result[1][1];
        float walkProb = drivesit_walkrunrid_result[0][1] * rid_walkrun_result[0][1] * walk_run_result[1][1];
        float runProb = drivesit_walkrunrid_result[0][1] * rid_walkrun_result[0][1] * walk_run_result[0][1];
        mapResult.put("drive", driveProb);
        mapResult.put("sit", sitProb);
        mapResult.put("ride", rideProb);
        mapResult.put("walk", walkProb);
        mapResult.put("run", runProb);
        return mapResult;
    }

    /**
     * check if the user is watching phone
     *
     * @param accXYZ acc reading of 50.
     */
    public boolean isWatchPhone(float[][] accXYZ) {
        Log.d("isWatchPhone", "==>into func");
        float[] watchPhoneData = SensorDataUtils.transXYZ2WatchPhoneData(accXYZ);
        Log.d("isWatchPhone", "==>start classify");
        return 0 == rf_WatchPhone.voteClassifyVec(watchPhoneData);
    }
}
