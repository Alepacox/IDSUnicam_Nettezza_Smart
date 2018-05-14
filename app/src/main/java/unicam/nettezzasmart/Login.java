package unicam.nettezzasmart;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import unicam.nettezzasmart.Report.ReportMaking;
import unicam.nettezzasmart.Request.RequestCollection;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Login.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Login#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Login extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String[] tempInfo= new String[]{"","","",""};
    private View myFragmentView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;


    public Login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Login.
     */
    // TODO: Rename and change types and number of parameters
    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
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
        myFragmentView = inflater.inflate(R.layout.fragment_login, container, false);
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
        final EditText nameText= (EditText) getActivity().findViewById(R.id.name_box);
        final EditText surnameText= (EditText) getActivity().findViewById(R.id.surname_box);
        final EditText emailText= (EditText) getActivity().findViewById(R.id.email_box);
        final EditText numberText= (EditText) getActivity().findViewById(R.id.phone_box);
        if(mParam1.equals("modify")){
            TextView modify_data= (TextView) getActivity().findViewById(R.id.login_text);
            modify_data.setText("Modifica solo i campi che vuoi modifiare");
            nameText.setHint(MyData.name);
            surnameText.setHint(MyData.surname);
            emailText.setHint(MyData.email);
            numberText.setHint(MyData.number);
            tempInfo[0]=MyData.name;
            tempInfo[1]=MyData.surname;
            tempInfo[2]=MyData.number;
            tempInfo[3]=MyData.email;
        }
        //Setup Name
        nameText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                tempInfo[0] =nameText.getText().toString();
            }
        });
        //Setup Surname


        surnameText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
               tempInfo[1]=surnameText.getText().toString();
            }
        });
        //Setup Number
        numberText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                tempInfo[2]=numberText.getText().toString();
            }
        });
        //Setup Email
        emailText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                tempInfo[3]=emailText.getText().toString();
            }
        });
        //Setup Button
        FloatingActionButton loginFab = (FloatingActionButton) getActivity().findViewById(R.id.login_fab);
        loginFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //TODO: Aggiungere check spazi
                    if (tempInfo[0].length()>2&&tempInfo[1].length()>2&&tempInfo[2].length()>7&&tempInfo[3].length()>5) {
                        MyData.name=tempInfo[0];
                        MyData.surname=tempInfo[1];
                        MyData.number=tempInfo[2];
                        MyData.email=tempInfo[3];
                        MainActivity.logged = true;
                        View hView = MainActivity.navigationView.getHeaderView(0);
                        TextView name_user = (TextView) hView.findViewById(R.id.name_nav);
                        TextView email_user = (TextView) hView.findViewById(R.id.email_nav);
                        name_user.setText(MyData.name + " " + MyData.surname);
                        email_user.setText(MyData.email);
                        RWStoredData.writeLoginDataToFile(MyData.name + "__" + MyData.surname + "__" + MyData.email + "__" + MyData.number, getActivity().getApplicationContext());
                        if (MainActivity.selected == 0) {
                            FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.replace(R.id.content_frame, ReportMaking.newInstance("param2", "param2"));
                            transaction.commit();
                        } else if (MainActivity.selected == 1) {
                            FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.replace(R.id.content_frame, RequestCollection.newInstance("param2", "param2"));
                            transaction.commit();
                        } else if (MainActivity.selected == 2) {
                            if (mParam1.equals("modify")) {
                                MainActivity.manager.popBackStackImmediate();
                            } else {
                                FragmentTransaction transaction = MainActivity.manager.beginTransaction();
                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                transaction.replace(R.id.content_frame, MyData.newInstance("param2", "param2"));
                                transaction.commit();
                            }
                        } else {
                            InfoFragment.syncProfileToView(InfoFragment.thisview);
                            MainActivity.manager.popBackStack();
                        }
                    } else
                        Toast.makeText(getActivity(), "Compila tutti i campi", Toast.LENGTH_SHORT).show();
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
}
