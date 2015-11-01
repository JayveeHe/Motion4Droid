package io.petchat.libs.rfclassifier.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

/**
 * Created by jayvee on 15/10/30.
 */
public class SensorDataUtils {
    public static float[] transXYZ2InputData(float[][] xyzlist) throws IOException {
        // trans raw sensor data into RF classifier input data
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
            float mod = 0;
            float absMin = 0;
            float absMax = 0;
            mod = calMod(xyzlist[i]);
            float absX = Math.abs(xyzlist[i][0]);
            float absY = Math.abs(xyzlist[i][1]);
            float absZ = Math.abs(xyzlist[i][2]);
            if (absX > absY) {
                absMax = absX;
                absMin = absY;
            } else {
                absMax = absY;
                absMin = absY;
            }
            absMax = absZ > absMax ? absZ : absMax;
            absMin = absZ < absMin ? absZ : absMin;
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
        meanMod /= meanMod / LISTSIZE;
        meanAbsMax /= meanAbsMax / LISTSIZE;
        meanAbsMin /= meanAbsMin / LISTSIZE;
        for (int i = 0; i < LISTSIZE; i++) {
            stdMod += Math.pow((modList[i] - meanMod), 2);
            stdAbsMax += Math.pow((absMaxList[i] - meanAbsMax), 2);
            stdAbsMin += Math.pow((absMinList[i] - meanAbsMin), 2);
        }
        stdMod = (float) Math.sqrt(stdMod);
        stdAbsMax = (float) Math.sqrt(stdAbsMax);
        stdAbsMin = (float) Math.sqrt(stdAbsMin);
        return new float[]{meanAbsMax, stdAbsMax, minAbsMax, maxAbsMax,
                meanAbsMin, stdAbsMin, minAbsMin, maxAbsMin,
                meanMod, stdMod, minMod, maxMod};
    }

//    else
//
//    {
//        throw new IOException("input size is not correct");
//    }

//}

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

}
