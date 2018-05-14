package unicam.nettezzasmart.Request;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import unicam.nettezzasmart.MainActivity;
import unicam.nettezzasmart.R;
import unicam.nettezzasmart.RWStoredData;
import unicam.nettezzasmart.SyncToServerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RequestMaking.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RequestMaking#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestMaking extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View myFragmentView;
    private static String choosenData="";
    private String choosenPlace="";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private RequestMaking.OnFragmentInteractionListener mListener;

    public RequestMaking() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestMaking.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestMaking newInstance(String param1, String param2) {
        RequestMaking fragment = new RequestMaking();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myFragmentView = inflater.inflate(R.layout.fragment_request_making, container, false);
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
        //Setting up listeners
        final Button openCalendar= (Button) getActivity().findViewById(R.id.set_data);
        final EditText place= (EditText) getActivity().findViewById(R.id.where_input);
        final FABProgressCircle sendButton = (FABProgressCircle) getActivity().findViewById(R.id.requestFabSend);
        //Calendar
        openCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(),"Date Picker");
            }
        });
        //Place
        place.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                choosenPlace=place.getText().toString();
            }
        });
        //Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(choosenPlace.length()<5 || choosenData.length()<1){
                    Toast.makeText(getActivity(), "Compila tutti i campi", Toast.LENGTH_SHORT).show();
                    /* TODO: funzione modify temporaneamente commentata
                } else if (mParam1.equals("modify")) {
                    RequestDetail.request.setWhen(choosenData);
                    RequestDetail.request.setWhere(choosenPlace);
                    Toast.makeText(getActivity(), "La tua richiesta è stata modificata", Toast.LENGTH_SHORT).show();
                    MainActivity.manager.popBackStackImmediate();
                    */
                } else if(SyncToServerTask.checkConnection(getActivity())){
                    sendButton.show();
                    final Activity myActivity= getActivity();
                    MainActivity.tpe.execute(new Runnable() {
                        @Override
                        public void run() {
                            String output;
                            int code;
                            try {
                                output= new SyncToServerTask.PostRequestServerTask(choosenData, choosenPlace).execute().get();
                                if(output==null){
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendButton.hide();
                                            Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    code= Integer.valueOf(output);
                                    new Request(code, choosenData, choosenPlace).setStatus(1);
                                    RWStoredData.addRequestToFile("__"+String.valueOf(code)+"__"+choosenData, getActivity());
                                    myActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.manager.popBackStackImmediate();
                                            sendButton.hide();
                                            Toast.makeText(getActivity(), R.string.richiesta_ricevuta, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendButton.hide();
                                        Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendButton.hide();
                                        Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                                myActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendButton.hide();
                                        Toast.makeText(getActivity(), R.string.problem, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                } else Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
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



    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            //Use the current date as the default date in the date picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            c.add(Calendar.DAY_OF_YEAR, 2); //2 giorni minimo dalla richiesta
            DatePickerDialog dpd = new DatePickerDialog(getActivity(),this,year, month, day);
            DatePicker dp = dpd.getDatePicker();
            dp.setMinDate(c.getTimeInMillis());
            return dpd;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            month+=1; //Non so manco io perchè
            TextView pickedData= (TextView) getActivity().findViewById(R.id.choosen_data);
            choosenData=day+"/"+month+"/"+year;
            pickedData.setText(choosenData);
        }
    }

}
