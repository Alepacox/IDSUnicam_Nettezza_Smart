package unicam.nettezzasmart.Report;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import unicam.nettezzasmart.MainActivity;
import unicam.nettezzasmart.R;
import unicam.nettezzasmart.RWStoredData;
import unicam.nettezzasmart.SyncToServerTask;
import unicam.nettezzasmart.TakeAndSeePhoto;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportMaking.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportMaking#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportMaking extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View myFragmentView;
    public static ImageView imageReport;
    static final int REQUEST_LOCATION = 1654;
    LocationManager locationManager;
    private final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    //Elementi da verificare
    public static boolean addedPhoto = false;
    public static boolean addedSpinner = false;
    public static boolean addedComment = false;
    //Elementi da inviare
    public static Bitmap choosenImage;
    public static String choosenTrashType="Vario";
    public static String choosenComment = "";
    private double lat;
    private double lon;
    //Parametri interni
    public static String fileName = "";
    public static boolean usedURI= false;
    public static Uri tempURI;
    public static File tempFile;
    public static int selecteSpinnerItem=0;



    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public ReportMaking() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportMaking.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportMaking newInstance(String param1, String param2) {
        ReportMaking fragment = new ReportMaking();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_report_making, container, false);
        return myFragmentView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final CardView myReports = (CardView) getActivity().findViewById(R.id.my_report);
        //TODO: Qui avverrà il sync con il server e tutti i relativi check
        myReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.replace(R.id.content_frame, ReportCollection.newInstance("", null));
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        //Setup Spinner
        final Spinner spinner = (Spinner) myFragmentView.findViewById(R.id.reportSpinnerRifiuti);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(myFragmentView.getContext(),
                R.array.spinner_Elements, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (addedSpinner)
            spinner.setSelection(selecteSpinnerItem);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selecteSpinnerItem= adapterView.getSelectedItemPosition();
                switch (selecteSpinnerItem){
                    case 0: choosenTrashType= "Vario";
                    break;
                    case 1: choosenTrashType= "Umido";
                    break;
                    case 2: choosenTrashType= "Carta";
                    break;
                    case 3: choosenTrashType= "Plastica";
                    break;
                    case 4: choosenTrashType= "Indifferenziato";
                    break;
                }
                addedSpinner = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Setup Immagine
        imageReport = (ImageView) myFragmentView.findViewById(R.id.reportImmagine);
        if (addedPhoto) {
            imageReport.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageReport.setImageBitmap(choosenImage);
        }
        imageReport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (arePermissionsEnabled()) {
                        launchTakePhotoFragment();
                    } else
                        requestMultiplePermissions();
                } else {
                    launchTakePhotoFragment();
                }
            }
        });
        //Setup Commento
        final EditText comment = (EditText) getActivity().findViewById(R.id.reportCommento);
        if (addedComment) {
            comment.setText(choosenComment);
        }
        comment.setMovementMethod(new ScrollingMovementMethod());
        comment.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                choosenComment = comment.getText().toString();
                addedComment = true;
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });
        //Setup Send Button
        final FABProgressCircle sendButton = (FABProgressCircle) getActivity().findViewById(R.id.reportFabSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Fix parameters and Launch Fragment Report Detail
                if (addedPhoto) {
                    if(SyncToServerTask.checkConnection(getActivity())){
                    if (getLocation()) {
                        final String date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
                        if (choosenComment.length() < 1) {
                            choosenComment = getString(R.string.No_comment);
                        }
                        sendButton.show();
                        final Activity myActivity = getActivity();
                        MainActivity.tpe.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String output = new SyncToServerTask.PostReportServerTask(choosenTrashType,
                                            choosenComment, lat, lon, choosenImage).execute().get();
                                    if (output == null) {
                                        myActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sendButton.hide();
                                                Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        int code = Integer.valueOf(output);
                                        final Report temp = new Report(code, date, String.valueOf(lat), String.valueOf(lon),
                                                choosenTrashType, choosenComment, choosenImage);
                                        temp.setStatus(1);
                                        RWStoredData.addReportToFile("__" + String.valueOf(code) + "__" + fileName + "__" + date, getActivity());
                                        addedComment = false;
                                        addedPhoto = false;
                                        addedSpinner = false;
                                        usedURI = false;
                                        ReportMaking.choosenImage = null;
                                        myActivity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                sendButton.hide();
                                                Toast.makeText(getActivity(), R.string.segnalazione_ricevuta, Toast.LENGTH_SHORT).show();
                                                comment.setText("");
                                                spinner.setSelection(0);
                                                sendButton.hide();
                                                FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                transaction.replace(R.id.content_frame, ReportDetail.newInstance(temp, "param2"));
                                                transaction.addToBackStack(null);
                                                transaction.commit();
                                            }
                                        });
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                            sendButton.hide();
                                        }
                                    });
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                            sendButton.hide();
                                        }
                                    });
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                            sendButton.hide();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    } else Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
                } else Toast.makeText(getActivity(), R.string.please_insert_image, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void launchTakePhotoFragment(){
        FragmentTransaction transaction = MainActivity.manager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.content_frame, TakeAndSeePhoto.newInstance("param2", null));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @SuppressLint("MissingPermission")
    public boolean getLocation() {
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
        long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
        Location location = null;
        locationManager = (LocationManager)getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Toast.makeText(getActivity(), R.string.please_enable_GPS, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });
                Log.d("activity", "LOC Network Enabled");
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        Log.d("activity", "LOC by Network");
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                        Log.d("LatLong", String.valueOf(lat)+" "+String.valueOf(lon));
                        return true;
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.problem_gps, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {

                                }

                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) {

                                }

                                @Override
                                public void onProviderEnabled(String s) {

                                }

                                @Override
                                public void onProviderDisabled(String s) {

                                }
                            });
                    Log.d("activity", "RLOC: GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            Log.d("activity", "RLOC: loc by GPS");
                            lat = location.getLatitude();
                            Log.d("LatLong", String.valueOf(lat)+" "+String.valueOf(lon));
                            lon = location.getLongitude();
                            return true;
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.problem_gps, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
                        new AlertDialog.Builder(getActivity())
                                .setMessage("Negando le autorizzazioni Find My Trash non potrà compiere miracoli!")
                                .setPositiveButton("Richiedi",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestMultiplePermissions();
                                    }
                                })
                                .setNegativeButton("Non mi interessa", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                    }
                    return;
                }
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}