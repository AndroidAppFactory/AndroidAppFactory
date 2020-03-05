package com.bihe0832.lib.timer;


import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class TaskManager {	
	//定时任务检查的时间粒度
	private static final String LOG_TAG = "TASK";
    private final int PERIOD = 500;
    private Timer timer = null;
    private boolean started = false;
    private HashMap<String, BaseTask> mTaskList = new HashMap<String, BaseTask>();
    
    private static volatile TaskManager instance;

    public static TaskManager getInstance() {
		if (instance == null) {
			synchronized (TaskManager.class) {
				if (instance == null) {
					instance = new TaskManager();
				}
			}
		}
		return instance;
	}
    
    private TaskManager() {
        super();
    }
    
    private void startTimer() {
    	if (started == true) {
    		return;
    	}
    	timer = new Timer();
        timer.scheduleAtFixedRate(new TaskDispatcher(), 0, PERIOD);
        started = true;
    }

    private void stopTimer() {
        if (timer != null && started == true) {
            timer.cancel();
            timer = null;
            started = false;
        }
    }
    
    private class TaskDispatcher extends TimerTask {
        @Override
        public void run() {
            Log.d(LOG_TAG,"TaskDispatcher run");
            synchronized (mTaskList) {
            	Iterator<Entry<String, BaseTask>> iter = mTaskList.entrySet().iterator();
            	while (iter.hasNext()) {
            		Entry<String, BaseTask> entry = iter.next();
            		final BaseTask task = (BaseTask)entry.getValue();
            		if (task.getNotifiedTimes() > task.getMyInterval() - 1) {
                        task.resetNotifiedTimes();
            		    task.run();
                    }
                    task.increaseNotifiedTimes();
            	}
            }
        }
    }
    
    public int addTask(BaseTask task) {
        Log.d(LOG_TAG,"add task:"+ task.getTaskName());
        if(task.getMyInterval() < 1){
            Log.e(LOG_TAG,"add task: bad Interval"+ task.getTaskName());
            return -1;
        }
    	int result = -1;
        if (mTaskList.containsKey(task.getTaskName())) {
        	result = -2;
        } else {
        	synchronized (mTaskList) {
        		mTaskList.put(task.getTaskName(), task);
        	}
            result = 0;
        }
        
        if(!started){
			startTimer();
		}
        return result;
    }
    
    /**
     * 
     * 根据定时任务名称获取对应的定时任务
     * @param taskName 任务名称
     * @return 返回对应的定时任务
     * 
     */
    public BaseTask getTaskByName(String taskName) {
    	if(mTaskList.containsKey(taskName)){
    		return mTaskList.get(taskName);
    	}
		return null;
    }
    
    /**
     * 根据定时任务名称删除对应的任务
     * @param taskName 任务名称
     * @return true 为删除成功，false 为没有此任务删除失败
     */
    public Boolean removeTask(String taskName) {
        Log.d(LOG_TAG,"remove task:"+ taskName);
    	if (null != taskName) {
    		if(mTaskList.containsKey(taskName)){
    			 synchronized (mTaskList) {
    				 mTaskList.remove(taskName);
    				 if(started && mTaskList.isEmpty()){
                         stopTimer();
                     }
    				 return true;
	            }
    		}
        }
    	return false;
    }

    public void letTaskRunEarly(String taskName){
        Log.d(LOG_TAG,"letTaskRunEarly task:"+ taskName);
        BaseTask task = getTaskByName(taskName);
        if(null != task){
            task.letTaskRunEarly();
        }
    }
}
