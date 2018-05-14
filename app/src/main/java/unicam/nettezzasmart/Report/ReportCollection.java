package unicam.nettezzasmart.Report;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.kofigyan.stateprogressbar.StateProgressBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import unicam.nettezzasmart.MainActivity;
import unicam.nettezzasmart.R;
import unicam.nettezzasmart.RWStoredData;
import unicam.nettezzasmart.Request.Request;
import unicam.nettezzasmart.Request.RequestCollection;
import unicam.nettezzasmart.SyncToServerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportCollection.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportCollection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportCollection extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static LayoutInflater vi;
    private boolean syncing=false;
    private View myFragmentView;
    public static LinearLayout viewableReportList;
    public static ArrayList<Report> report_list = new ArrayList<>();

    // TODO: Rename and chang
    private ReportCollection.OnFragmentInteractionListener mListener;

    public ReportCollection() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportCollection.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportCollection newInstance(String param1, String param2) {
        ReportCollection fragment = new ReportCollection();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_report_collection, container, false);
        return myFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewableReportList = (LinearLayout) getActivity().findViewById(R.id.report_list);
        syncCollectionToView();
        if(MainActivity.firstSyncReport){
            syncServerToCollection(getActivity());
            MainActivity.firstSyncReport=false;
        }
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_report);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!syncing) {
                    syncServerToCollection(getActivity());
                } else Toast.makeText(getActivity(), R.string.wait_for_previous_sync, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void syncServerToCollection(final Activity activity){
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) activity.findViewById(R.id.swipe_report);
        mSwipeRefreshLayout.setRefreshing(true);
        final boolean[] errors = new boolean[1];
        errors[0]=false;
        if(SyncToServerTask.checkConnection(getActivity())) {
            MainActivity.tpe.execute(new Runnable() {
                public void run() {
                    syncing=true;
                    for (int i = 0; i < ReportCollection.report_list.size(); i++) {
                        Report report = ReportCollection.report_list.get(i);
                        try {
                            String output = new SyncToServerTask.GetServerTask(report.getCode(), false).execute().get();
                            if (output != null) {
                                if (!output.equals("delete")) {
                                    JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                                    JSONObject data = object.getJSONObject("data");
                                    try {
                                        URL url = new URL(SyncToServerTask.URL+data.getString("pathToPhoto"));
                                        Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                                        report.setPhotoFile(image);
                                    } catch(IOException e) {
                                        e.printStackTrace();
                                    }
                                    report.setComment(data.getString("description"));
                                    report.setTrashType(data.getString("category"));
                                    switch (data.getString("status")) {
                                        case "Rifiutata":
                                            report.setStatus(-1);
                                            break;
                                        case "Da_Analizzare":
                                            report.setStatus(1);
                                            break;
                                        case "Accettata":
                                            report.setStatus(2);
                                            break;
                                        case "In_Corso":
                                            report.setStatus(3);
                                            break;
                                        case "Completata":
                                            report.setStatus(4);
                                            break;
                                    }
                                /*
                                JSONObject location = data.getJSONObject("location");
                                report.setLat(String.valueOf(location.getInt("lat")));
                                report.setLon(String.valueOf(location.getInt("long")));
                                */
                                } else {
                                    ReportCollection.report_list.remove(report);
                                    RWStoredData.updateReportsToFile(activity);
                                }
                            } else errors[0] = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            errors[0] = true;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            errors[0] = true;
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            errors[0] = true;
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (errors[0] == true) {
                                Toast.makeText(activity, R.string.problem_retrieving_elements, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(activity, R.string.updated, Toast.LENGTH_SHORT).show();
                            }
                            syncCollectionToView();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                    syncing=false;
                }
            });
        } else {
        Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(false);
    }
    }

    public void syncCollectionToView(){
        viewableReportList.removeAllViews();
        for(int i = 0; i< report_list.size(); i++){
            final Report temp= report_list.get(i);
            final View tempView= vi.inflate(R.layout.sample_report, null);
            TextView sample_code= (TextView)tempView.findViewById(R.id.sample_report_code);
            sample_code.setText(String.valueOf(temp.getCode()));
            TextView sample_when= (TextView) tempView.findViewById((R.id.sample_report_when));
            sample_when.setText(temp.getWhen());
            CardView report= (CardView) tempView.findViewById(R.id.base_report);
            StateProgressBar stateProgressBar = (StateProgressBar) tempView.findViewById(R.id.progress_bar);
            TextView status= (TextView) tempView.findViewById(R.id.status);
            switch (temp.getStatus()){
                case -1: status.setText(R.string.segnalazione_rifiutata);
                    //TODO: trovare un colore per la card di una richiesta rifiutata
                    stateProgressBar.setVisibility(View.INVISIBLE);
                    break;
                case 0: status.setText(R.string.syncing);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                    break;
                case 1: status.setText(R.string.segnalazione_ricevuta);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                    break;
                case 2: status.setText(R.string.segnalazione_accolta);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                    break;
                case 3: status.setText(R.string.operatore_segnalazione);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                    break;
                case 4: status.setText(R.string.completato);
                    report.setCardBackgroundColor(Color.parseColor("#A1887F"));
                    stateProgressBar.setStateNumberForegroundColor(Color.parseColor("#795548"));
                    stateProgressBar.setStateNumberBackgroundColor(Color.parseColor("#A1887F"));
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FIVE);
                    break;
            }
            report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (temp.getStatus()==0) {
                        Toast.makeText(tempView.getContext(), R.string.wait_details, Toast.LENGTH_SHORT).show();
                    } else {
                        FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.replace(R.id.content_frame, ReportDetail.newInstance(temp, null));
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
            viewableReportList.addView(tempView);
        }
    }


}
