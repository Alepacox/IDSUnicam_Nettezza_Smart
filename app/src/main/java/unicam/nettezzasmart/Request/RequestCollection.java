package unicam.nettezzasmart.Request;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import unicam.nettezzasmart.MainActivity;
import unicam.nettezzasmart.R;
import unicam.nettezzasmart.RWStoredData;
import unicam.nettezzasmart.SyncToServerTask;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestCollection.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestCollection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestCollection extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static LayoutInflater vi;
    private View myFragmentView;
    private boolean syncing=false;
    public static LinearLayout viewableRequestList;
    public static ArrayList<Request> request_list= new ArrayList<>();
    private RequestCollection.OnFragmentInteractionListener mListener;

    public RequestCollection() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestCollection.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestCollection newInstance(String param1, String param2) {
        RequestCollection fragment = new RequestCollection();
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
        myFragmentView = inflater.inflate(R.layout.fragment_request_collection, container, false);
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
        viewableRequestList = (LinearLayout) getActivity().findViewById(R.id.request_list);
        final CardView newRequest= (CardView) getActivity().findViewById(R.id.new_request);
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_request);

        syncCollectionToView();
        if(MainActivity.firstSyncRequest){
            syncServerToCollection(getActivity());
            MainActivity.firstSyncRequest=false;
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!syncing) {
                    syncServerToCollection(getActivity());
                } else Toast.makeText(getActivity(), R.string.wait_for_previous_sync, Toast.LENGTH_SHORT).show();
            }
        });
        newRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.replace(R.id.content_frame, RequestMaking.newInstance("", null));
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void syncServerToCollection(final Activity activity){
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_request);
        mSwipeRefreshLayout.setRefreshing(true);
        final boolean[] errors = new boolean[1];
        errors[0]=false;
        if(SyncToServerTask.checkConnection(getActivity())) {
            MainActivity.tpe.execute(new Runnable() {
                public void run() {
                    syncing=true;
                    for (int i = 0; i < RequestCollection.request_list.size(); i++) {
                        Request request = RequestCollection.request_list.get(i);
                        //TODO: da capire come comportarsi nel caso in cui qualche elemento non possa essere caricato a causa della connessione
                        try {
                            String output = new SyncToServerTask.GetServerTask(request.getCode(), true).execute().get();
                            if (output != null) {
                                if (!output.equals("delete")) {
                                    JSONObject object = (JSONObject) new JSONTokener(output).nextValue();
                                    JSONObject data = object.getJSONObject("data");
                                    JSONObject location = data.getJSONObject("location");
                                    request.setWhere(location.getString("address"));
                                    switch (data.getString("status")) {
                                        case "Rifiutata":
                                            request.setStatus(-1);
                                            break;
                                        case "Da_Analizzare":
                                            request.setStatus(1);
                                            break;
                                        case "Accettata":
                                            request.setStatus(2);
                                            break;
                                        case "In_Corso":
                                            request.setStatus(3);
                                            break;
                                        case "Completata":
                                            request.setStatus(4);
                                            break;
                                    }
                                } else {
                                    RequestCollection.request_list.remove(request);
                                    RWStoredData.updateRequestsToFile(activity);
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

    public void syncCollectionToView(){
        viewableRequestList.removeAllViews();
        for(int i=0; i<request_list.size(); i++){
            final Request temp= request_list.get(i);
            final View tempView= vi.inflate(R.layout.sample_request, null);
            TextView sample_code= (TextView)tempView.findViewById(R.id.sample_request_code);
            sample_code.setText(String.valueOf(temp.getCode()));
            TextView sample_data= (TextView)tempView.findViewById(R.id.sample_request_data);
            sample_data.setText(temp.getWhen());
            CardView request= (CardView) tempView.findViewById(R.id.base_request);
            // TODO: move element to last if completed or remove list.add(list.remove(0));
            StateProgressBar stateProgressBar = (StateProgressBar) tempView.findViewById(R.id.progress_bar);
            TextView status= (TextView) tempView.findViewById(R.id.status);
            switch (temp.getStatus()){
                case -1: status.setText(R.string.richiesta_rifiutata);
                //TODO: trovare un colore per la card di una richiesta cancellata
                stateProgressBar.setVisibility(View.INVISIBLE);
                    break;
                case 0: status.setText(R.string.syncing);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE);
                    break;
                case 1: status.setText(R.string.richiesta_ricevuta);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
                    break;
                case 2: status.setText(R.string.richiesta_accolta);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);
                    break;
                case 3: status.setText(R.string.operatore_richiesta);
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);
                    break;
                case 4: status.setText(R.string.completato);
                    request.setCardBackgroundColor(Color.parseColor("#A1887F"));
                    stateProgressBar.setStateNumberForegroundColor(Color.parseColor("#795548"));
                    stateProgressBar.setStateNumberBackgroundColor(Color.parseColor("#A1887F"));
                    stateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FIVE);
                    break;
            }
            request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (temp.getStatus()==0) {
                        Toast.makeText(tempView.getContext(), R.string.wait_details, Toast.LENGTH_SHORT).show();
                    } else {
                        FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        transaction.replace(R.id.content_frame, RequestDetail.newInstance(temp, null));
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
            viewableRequestList.addView(tempView);
        }
    }

}
