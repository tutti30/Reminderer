package com.frankandrobot.reminderer.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.frankandrobot.reminderer.alarm.AlarmHelper;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.LinkedList;

/**
 * The app-specific interface to the database
 *
 */
public class TaskDatabaseFacade
{
    static private String TAG = "R:TaskFacade";

    static public int ADD_TASK_LOADER_ID = 0;
    static public int LOAD_ALL_TASKS_LOADER_ID = 1;
    static public int LOAD_TASKS_LOADER_ID = 2;

    public TaskDatabaseFacade() {}

    public AddTask getAddTaskLoader(Context context, Task task)
    {
        return new AddTask(context, task);
    }

    public LoadAllTasks getLoadAllTasksLoader(Context context)
    {
        return new LoadAllTasks(context);
    }

    public LoadTasks getLoadTasksLoader(Context context, long dueTime)
    {
        return new LoadTasks(context, dueTime);
    }

    static private class AddTask extends AsyncTaskLoader<Void>
    {
        private Task task;
        private AlarmHelper alarmHelper = new AlarmHelper();

        public AddTask(Context context, Task task) {
            super(context);

            this.task = task;

        }

        @Override
        public Void loadInBackground()
        {
            if (Logger.LOGV)
            {
                Log.v(TAG, "Saving task:\n" + task);
            }

            if (task != null)
            {
                ContentResolver resolver = getContext().getContentResolver();
                resolver.insert(TaskProvider.CONTENT_URI, task.toContentValues());
                alarmHelper.findAndEnableNextTasksDue(getContext(), System.currentTimeMillis());
            }
            return null;
        }
    }

    static private class LoadAllTasks extends AsyncTaskLoader<String[]>
    {
        public LoadAllTasks(Context context)
        {
            super(context);
        }

        @Override
        public String[] loadInBackground()
        {
            if (Logger.LOGV)
            {
                Log.v(TAG, "Loading tasks");
            }

            Cursor cursor = getContext().getContentResolver().query(TaskProvider.CONTENT_URI,
                                                                    new String[]{TaskCol.TASK_DESC.toString()},
                                                                    null,
                                                                    null,
                                                                    null);

            if (cursor != null)
            {
                LinkedList<String> llTasks = new LinkedList<String>();

                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    llTasks.add(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())));
                    cursor.moveToNext();
                }
                return llTasks.toArray(new String[llTasks.size()]);
            }

            return new String[0];
        }
    }

    static private class LoadTasks extends AsyncTaskLoader<String[]>
    {
        private long dueTime;

        public LoadTasks(Context context, long dueTime)
        {
            super(context);
            this.dueTime = dueTime;
        }

        @Override
        public String[] loadInBackground()
        {
            if (Logger.LOGV)
            {
                Log.v(TAG, "Loading tasks");
            }

            Cursor cursor = getContext().getContentResolver().query(TaskProvider.CONTENT_URI,
                                                                    new String[]{TaskCol.TASK_DESC.toString()},
                                                                    TaskCol.TASK_DUE_DATE+"=?",
                                                                    new String[]{String.valueOf(dueTime)},
                                                                    null);

            if (cursor != null)
            {
                LinkedList<String> llTasks = new LinkedList<String>();

                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    llTasks.add(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())));
                    cursor.moveToNext();
                }
                return llTasks.toArray(new String[llTasks.size()]);
            }

            return new String[0];
        }
    }

}
