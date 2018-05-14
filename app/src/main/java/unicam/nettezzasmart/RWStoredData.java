package unicam.nettezzasmart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import unicam.nettezzasmart.Report.Report;
import unicam.nettezzasmart.Report.ReportCollection;
import unicam.nettezzasmart.Request.Request;
import unicam.nettezzasmart.Request.RequestCollection;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class RWStoredData {
    public static void readLocalData(Context context) {
        try {
            Scanner sc = new Scanner (context.openFileInput("Data.txt")).useDelimiter("__");
            MyData.name= sc.next();
            MyData.surname= sc.next();
            MyData.email= sc.next();
            MyData.number= sc.next();
            MainActivity.logged=true;
            View hView =  MainActivity.navigationView.getHeaderView(0);
            TextView name_user = (TextView)hView.findViewById(R.id.name_nav);
            TextView email_user = (TextView)hView.findViewById(R.id.email_nav);
            name_user.setText(MyData.name+" "+MyData.surname);
            email_user.setText(MyData.email);
        }
        catch (FileNotFoundException e) {
            Log.e("Login", "File not found: Registration Required");
            MainActivity.logged=false;
        }
        RequestCollection.request_list.clear();
        try {
            Scanner sc = new Scanner (context.openFileInput("Request.txt")).useDelimiter("__");
            RequestCollection.request_list.clear();
            while(sc.hasNext()){
                new Request(Integer.parseInt(sc.next()), sc.next(), "").setStatus(0);
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            Log.e("Request", "File not found: Never made");
        }
        ReportCollection.report_list.clear();
        try {
            Scanner sc = new Scanner (context.openFileInput("Report.txt")).useDelimiter("__");
            while(sc.hasNext()){
                int code= Integer.parseInt(sc.next());
                new Report(code, sc.next(), "", "", "", "", null).setStatus(0);
            }
            sc.close();
        }
        catch (FileNotFoundException e) {
            Log.e("Report", "File not found: Never made");
        }
    }

    public static void writeLoginDataToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("Data.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close(); }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void addRequestToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("Request.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close(); }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void updateRequestsToFile(Context context){
        String data="";
        for (int i=0; i<RequestCollection.request_list.size(); i++) {
            data += "__" + String.valueOf(RequestCollection.request_list.get(i).getCode())
            + "__" +RequestCollection.request_list.get(i).getWhen();
        }
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("Request.txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void addReportToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("Report.txt", Context.MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close(); }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static void updateReportsToFile(Context context){
        String data="";
        for (int i=0; i<ReportCollection.report_list.size(); i++) {
            data += "__" + String.valueOf(ReportCollection.report_list.get(i).getCode()) + "__"+ ReportCollection.report_list.get(i).getWhen();
        }
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("Report.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
