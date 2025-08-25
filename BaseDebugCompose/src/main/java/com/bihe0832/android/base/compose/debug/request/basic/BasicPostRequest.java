package com.bihe0832.android.base.compose.debug.request.basic;


import com.bihe0832.android.base.compose.debug.request.Constants;
import com.bihe0832.android.lib.http.common.core.HttpBasicRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BasicPostRequest extends HttpBasicRequest {


    public BasicPostRequest(String para) throws JSONException {
//        String encodedParam = Constants.PARA_PARA + HTTP_REQ_ENTITY_MERGE + para;
//        try {
//            this.data = encodedParam.getBytes("UTF-8");
//        }catch (Exception e){
//            e.printStackTrace();
//        }

//        ArrayList<String> taskArr = new ArrayList<String>();
//        taskArr.add("111");
//        final JSONObject req = new JSONObject();
//        String data = JsonHelper.INSTANCE.toJson(taskArr);
//        req.put("taskIDList", data);
//        req.put(Constants.PARA_PARA, para);
//        req.put("sdfdsf", "dfd");
//        this.data = req.toString().getBytes();
//
//


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("key1", "value1");
        jsonObj.put("key2", "value2");

        // 创建一个JSON数组
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("value1");
        jsonArray.put("value1");
        jsonArray.put("value1");
        jsonArray.put("value1");

        // 将JSON数组添加到JSON对象中
        jsonObj.put("taskIDList", jsonArray);

        // 将JSON对象转换为字节数组
        this.data = jsonObj.toString().getBytes();



//        val taskArr = ArrayList<String>()
//        taskArr.add("111")
//
//        HashMap<String, String?>().apply {
//            put(Constants.PARA_PARA, result ?: "")
//            put("sdfdsf", "dfd")
//            put("ewewe", JsonHelper.toJson(taskArr).toString())
//        }.let {
//            this.data = getFormData(it)
//        }
    }

	@Override
	public String getUrl() {
        return Constants.HTTP_DOMAIN + Constants.PATH_POST;
	}

}
