package com.david.game.davidnotifyme.edupage;

import android.content.Context;
import android.util.Log;

import com.david.game.davidnotifyme.utils.InternalFiles;
import com.david.game.davidnotifyme.utils.InternalStorageFile;

public class ClassReader {
    Context context;
    StudentsClass[] studentsClasses;
    public ClassReader(Context context){
        this.context = context;

        InternalStorageFile classes = new InternalStorageFile(context, InternalFiles.CLASSES);
        final String[] classArray = classes.read("/");

        studentsClasses = new StudentsClass[classArray.length];
        int error_count = 0;
        for (int i = 0; i < classArray.length - error_count; i++) {
            String[] vals = classArray[i].split(":");
            if(vals.length < 2){
                error_count++;
                continue;
            }

            StudentsClass student = new StudentsClass(vals[0], vals[1]);

            Log.d("STUDENTCLASS", classArray[i] + "   " +  student);
            studentsClasses[i - error_count] = student;
        }
    }

    public StudentsClass[] getStudentsClasses() {
        return studentsClasses;
    }

    public String[] getIDs(){
        String[] output = new String[studentsClasses.length];
        for (int i = 0; i < output.length;i++){
            output[i] = studentsClasses[i].getId();
        }
        return output;
    }


    public String[] getClassNames(){
        String[] output = new String[studentsClasses.length];
        for (int i = 0; i < output.length;i++){
            output[i] = studentsClasses[i].getLabel();
        }
        return output;
    }
}
