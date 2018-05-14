package unicam.nettezzasmart.Request;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import unicam.nettezzasmart.MainActivity;
import unicam.nettezzasmart.R;
import unicam.nettezzasmart.RWStoredData;
import unicam.nettezzasmart.SyncToServerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestDetail.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestDetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestDetail extends Fragment {
    private static final String ARG_PARAM2 = "param2";
    private View myFragmentView;
    private String mParam1;
    private String mParam2;
    public static Request request;
    private RequestDetail.OnFragmentInteractionListener mListener;

    public RequestDetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestDetail.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestDetail newInstance(Request param1, String param2) {
        RequestDetail fragment = new RequestDetail();
        Bundle args = new Bundle();
        request=param1;
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_request_detail, container, false);
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
        syncDetailsToView(view);
        //TODO: aggiungere modifica dati server
        //TODO: aggiungere eliminazione dati server
        //Entrambi le operazioni sono consentite se lo status della request è quella iniziale.
        FloatingActionButton modifyButton = (FloatingActionButton) view.findViewById(R.id.modify_button);
        FloatingActionButton deleteButton= (FloatingActionButton) view.findViewById(R.id.delete_button);
        if(request.getStatus()<2) {
            modifyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity(), "La funzione è in fase di implementazione", Toast.LENGTH_SHORT).show();
                    /*
                    FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.replace(R.id.content_frame, RequestMaking.newInstance("modify", null));
                    transaction.addToBackStack(null);
                    transaction.commit();
                    */
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (request.getStatus() == 1) {
                        try {
                            String output = new SyncToServerTask.DeleteServerTask(request.getCode(), true).execute().get();
                            if (output != null) {
                                RequestCollection.request_list.remove(request);
                                RWStoredData.updateRequestsToFile(getActivity());
                                MainActivity.manager.popBackStackImmediate();
                                Toast.makeText(getActivity(), R.string.request_deleted, Toast.LENGTH_SHORT).show();
                            } else Toast.makeText(getActivity(), R.string.problem_deleting_request,
                                    Toast.LENGTH_SHORT).show();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), R.string.problem_deleting_request,
                                    Toast.LENGTH_SHORT).show();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), R.string.problem_deleting_request,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        RequestCollection.request_list.remove(request);
                        RWStoredData.updateRequestsToFile(getActivity());
                        MainActivity.manager.popBackStackImmediate();
                        Toast.makeText(getActivity(), R.string.request_deleted, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            modifyButton.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
        }

    }

    public static void syncDetailsToView(View view){
        TextView detailCode= (TextView) view.findViewById(R.id.detail_code_text);
        detailCode.setText(view.getContext().getString(R.string.request_code)+" "+String.valueOf(request.getCode()));
        TextView detailDate= (TextView) view.findViewById(R.id.detail_choosen_data);
        detailDate.setText(request.getWhen());
        TextView detailPlace= (TextView) view.findViewById(R.id.detail_choosen_place);
        detailPlace.setText(request.getWhere());
        TextView detailStatus= (TextView) view.findViewById(R.id.detail_status);
        switch (request.getStatus()){
            case -1: detailStatus.setText(R.string.richiesta_rifiutata);
                break;
            case 1: detailStatus.setText(R.string.richiesta_ricevuta);
                break;
            case 2: detailStatus.setText(R.string.richiesta_accolta);
                break;
            case 3: detailStatus.setText(R.string.operatore_richiesta);
                break;
            case 4: detailStatus.setText(R.string.completato);
                break;
        }
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


}
