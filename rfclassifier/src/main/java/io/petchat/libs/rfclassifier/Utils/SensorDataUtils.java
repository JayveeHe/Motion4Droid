package io.petchat.libs.rfclassifier.Utils;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

/**
 * Created by jayvee on 15/10/30.
 */
public class SensorDataUtils {
    /**
     * trans raw sensor data into RF classifier input data
     *
     * @param xyzlist   raw data
     * @param dimOffset offset of handle raw data dim.(when handling xyz data, dimOffset=0; magnetic data, dimOffset=3)
     * @return dim=12 input data
     * @throws IOException
     */
    public static float[] transXYZ2InputData(float[][] xyzlist, int dimOffset) throws IOException, IndexOutOfBoundsException {
        // windows = 50 stride=50
//        if (xyzlist.length >= 50) {
        // only pick 50 point of data
        final int LISTSIZE = xyzlist.length;
        float meanMod = 0;
        float meanAbsMax = 0;
        float meanAbsMin = 0;
        float stdMod = 0;
        float stdAbsMax = 0;
        float stdAbsMin = 0;
        float minMod = Float.MAX_VALUE;
        float minAbsMax = Float.MAX_VALUE;
        float minAbsMin = Float.MAX_VALUE;
        float maxMod = 0;
        float maxAbsMax = 0;
        float maxAbsMin = 0;
        float[] modList = new float[LISTSIZE];
        float[] absMaxList = new float[LISTSIZE];
        float[] absMinList = new float[LISTSIZE];
        for (int i = 0; i < LISTSIZE; i++) {
            // get first level features
            float[] xyzdata = {xyzlist[i][dimOffset], xyzlist[i][1 + dimOffset], xyzlist[i][2 + dimOffset]};
            float mod = 0;
            float absMin = 0;
            float absMax = 0;
            mod = calMod(xyzdata);
            float absX = Math.abs(xyzdata[0]);
            float absY = Math.abs(xyzdata[1]);
            float absZ = Math.abs(xyzdata[2]);
            absMax = absX > absY ? absX : absY;
            absMax = absZ > absMax ? absZ : absMax;
            absMin = absX < absY ? absX : absY;
            absMin = absZ < absMin ? absZ : absMin;
            modList[i] = mod;
            absMaxList[i] = absMax;
            absMinList[i] = absMin;
            // get second level features
            meanMod += mod;
            meanAbsMax += absMax;
            meanAbsMin += absMin;
            maxMod = maxMod > mod ? maxMod : mod;
            maxAbsMax = maxAbsMax > absMax ? maxAbsMax : absMax;
            maxAbsMin = maxAbsMin > absMin ? maxAbsMin : absMin;
            minMod = minMod < mod ? minMod : mod;
            minAbsMax = minAbsMax < absMax ? minAbsMax : absMax;
            minAbsMin = minAbsMin < absMin ? minAbsMin : absMin;
        }
        meanMod /= LISTSIZE;
        meanAbsMax /= LISTSIZE;
        meanAbsMin /= LISTSIZE;
        for (int i = 0; i < LISTSIZE; i++) {
            stdMod += Math.pow((modList[i] - meanMod), 2);
            stdAbsMax += Math.pow((absMaxList[i] - meanAbsMax), 2);
            stdAbsMin += Math.pow((absMinList[i] - meanAbsMin), 2);
        }
        stdMod = (float) Math.sqrt(stdMod / (LISTSIZE - 1));
        stdAbsMax = (float) Math.sqrt(stdAbsMax / (LISTSIZE - 1));
        stdAbsMin = (float) Math.sqrt(stdAbsMin / (LISTSIZE - 1));
        return new float[]{meanAbsMax, stdAbsMax, minAbsMax, maxAbsMax,
                meanAbsMin, stdAbsMin, minAbsMin, maxAbsMin,
                meanMod, stdMod, minMod, maxMod};
    }


    /**
     * Normalize raw input data when neccessary
     *
     * @param inputDataList
     * @return normalized input data list
     */
    public static float[][] normalizeMagnetXYZData(float[][] inputDataList) {
        // inputDataList shape=50*24
        int numDim = inputDataList[0].length;
        float[] minEachDim = new float[numDim];
        for (int i = 0; i < numDim; i++) {
            minEachDim[i] = Float.MAX_VALUE;
        }
        float[] maxEachDim = new float[numDim];
        // get min or max of each dim
        for (float[] inputData : inputDataList) {
            for (int i = 0; i < numDim; i++) {
                maxEachDim[i] = maxEachDim[i] > inputData[i] ? maxEachDim[i] : inputData[i];
                minEachDim[i] = minEachDim[i] < inputData[i] ? minEachDim[i] : inputData[i];
            }
        }
        // normalize each data
        for (float[] inputData : inputDataList) {
            for (int i = 0; i < numDim; i++) {
                inputData[i] = (inputData[i] - minEachDim[i]) / (maxEachDim[i] - minEachDim[i]);
            }
        }
        return inputDataList;
    }

    private static float calMod(float[] xyz) {
        float mod = 0;
        mod = (float) Math.sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2]);
        return mod;
    }

    public static JSONArray readSensorFile(final String filePath) {

        /*
        // for jayvee:
        // sensor files are located in senz_sensor_temp
        String FOLDER_SENSOR = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/senz_sensor_temp";
        String filePath = FOLDER_SENSOR + "/sensor.<timestamp>.txt";
        */

        try {
            FileReader fileReader = new FileReader(new File(filePath));
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            JSONArray sensors = new JSONArray();
//            JSONObject detectResults = new JSONObject();
            while ((line = br.readLine()) != null) {
                try {
                    JSONObject obj = JSON.parseObject(line);
                    sensors.add(obj);

                } catch (Exception e) {
                    continue;
                }
            }
            return sensors;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * trans XYZ data into watchphone input data
     *
     * @param rawXYZ
     * @return
     */
    public static float[] transXYZ2WatchPhoneData(float[][] rawXYZ) {
        Log.d("isWatchPhone", "==>trans data" + rawXYZ.length + "====" + rawXYZ[0].length);
        final int LISTSIZE = rawXYZ.length;
        float meanX = 0;
        float meanY = 0;
        float meanZ = 0;
        float stdX = 0;
        float stdY = 0;
        float stdZ = 0;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = 0;
        float maxY = 0;
        float maxZ = 0;
        float[] xList = new float[LISTSIZE];
        float[] yList = new float[LISTSIZE];
        float[] zList = new float[LISTSIZE];
        for (int i = 0; i < LISTSIZE; i++) {
//             get second level features
            float x = rawXYZ[i][0];
            float y = rawXYZ[i][1];
            float z = rawXYZ[i][2];
            xList[i] = x;
            yList[i] = y;
            zList[i] = z;
            meanX += x;
            meanY += y;
            meanZ += z;
            maxX = maxX > x ? maxX : x;
            maxY = maxY > y ? maxY : y;
            maxZ = maxZ > z ? maxZ : z;
            minX = minX < x ? minX : x;
            minY = minY < y ? minY : y;
            minZ = minZ < z ? minZ : z;
        }
        meanX /= LISTSIZE;
        meanY /= LISTSIZE;
        meanZ /= LISTSIZE;
        for (int i = 0; i < LISTSIZE; i++) {
            stdX += Math.pow((xList[i] - meanX), 2);
            stdY += Math.pow((yList[i] - meanY), 2);
            stdZ += Math.pow((zList[i] - meanZ), 2);
        }
        stdX = (float) Math.sqrt(stdX / (LISTSIZE - 1));
        stdY = (float) Math.sqrt(stdY / (LISTSIZE - 1));
        stdZ = (float) Math.sqrt(stdZ / (LISTSIZE - 1));
        return new float[]{meanY, stdY, minY, maxY,
                meanZ, stdZ, minZ, maxZ,
                meanX, stdX, minX, maxX};
    }

}
