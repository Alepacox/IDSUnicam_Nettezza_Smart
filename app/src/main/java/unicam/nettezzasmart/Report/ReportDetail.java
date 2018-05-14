package unicam.nettezzasmart.Report;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import unicam.nettezzasmart.MainActivity;
import unicam.nettezzasmart.R;
import unicam.nettezzasmart.RWStoredData;
import unicam.nettezzasmart.Request.RequestCollection;
import unicam.nettezzasmart.Request.RequestDetail;
import unicam.nettezzasmart.SyncToServerTask;
import unicam.nettezzasmart.TakeAndSeePhoto;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestDetail.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestDetail#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReportDetail extends Fragment {
    private static final String ARG_PARAM2 = "param2";
    private View myFragmentView;
    private String mParam1;
    private String mParam2;
    private static Report report;
    private ReportDetail.OnFragmentInteractionListener mListener;

    public ReportDetail() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReportDetail.
     */
    // TODO: Rename and change types and number of parameters
    public static ReportDetail newInstance(Report param1, String param2) {
        ReportDetail fragment = new ReportDetail();
        Bundle args = new Bundle();
        report=param1;
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
        myFragmentView = inflater.inflate(R.layout.fragment_report_detail, container, false);
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
        FloatingActionButton deleteButton= (FloatingActionButton) view.findViewById(R.id.delete_button);
        if(report.getStatus()<2) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(report.getStatus()==1) {
                        try {
                            String output = new SyncToServerTask.DeleteServerTask(report.getCode(), false).execute().get();
                            if (output != null) {
                                ReportCollection.report_list.remove(report);
                                Log.d("Collection size", String.valueOf(ReportCollection.report_list.size()));
                                MainActivity.manager.popBackStackImmediate();
                                RWStoredData.updateReportsToFile(getActivity());
                                Toast.makeText(getActivity(), R.string.deleted_report, Toast.LENGTH_SHORT).show();
                            } else Toast.makeText(getActivity(), R.string.problem_deleting_report,
                                    Toast.LENGTH_SHORT).show();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), R.string.problem_deleting_report,
                                    Toast.LENGTH_SHORT).show();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), R.string.problem_deleting_report,
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        ReportCollection.report_list.remove(report);
                        Log.d("Collection size", String.valueOf(ReportCollection.report_list.size()));
                        RWStoredData.updateReportsToFile(getActivity());
                        MainActivity.manager.popBackStackImmediate();
                        Toast.makeText(getActivity(), R.string.deleted_report, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            deleteButton.setVisibility(View.INVISIBLE);
        }

    }

    public static void syncDetailsToView(View view){
        //Setting request code
        TextView detailCode= (TextView) view.findViewById(R.id.detail_code_text);
        detailCode.setText(view.getContext().getString(R.string.report_code)+" "+String.valueOf(report.getCode()));
        //Setting data
        TextView detailDate= (TextView) view.findViewById(R.id.detail_data_made);
        detailDate.setText(report.getWhen());
        //Setting comment
        TextView detailComment= (TextView) view.findViewById(R.id.detail_choosen_comment);
        detailComment.setText(report.getComment());
        detailComment.setMovementMethod(new ScrollingMovementMethod());
        //Setting Image
        ImageView detailImage= (ImageView) view.findViewById(R.id.detail_choosen_photo);
        detailImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        detailImage.setImageBitmap(report.getPhotoFile());
        detailImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.replace(R.id.content_frame, TakeAndSeePhoto.newInstance("notToRetry", report.getPhotoFile()));
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
        //Setting Trash Type
        TextView detailTrashType= (TextView) view.findViewById(R.id.detail_choosen_trashtype);
        detailTrashType.setText(report.getTrashType());
        //Setting Status
        TextView detailStatus= (TextView) view.findViewById(R.id.detail_status);
        switch (report.getStatus()){
            case -1: detailStatus.setText(R.string.segnalazione_rifiutata);
                break;
            case 1: detailStatus.setText(R.string.segnalazione_ricevuta);
                break;
            case 2: detailStatus.setText(R.string.segnalazione_accolta);
                break;
            case 3: detailStatus.setText(R.string.operatore_segnalazione);
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
