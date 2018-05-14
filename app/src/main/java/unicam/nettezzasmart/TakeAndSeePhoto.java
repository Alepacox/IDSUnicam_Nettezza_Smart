package unicam.nettezzasmart;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import unicam.nettezzasmart.Report.ReportMaking;

import static android.os.Environment.getExternalStoragePublicDirectory;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReportMaking.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReportMaking#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TakeAndSeePhoto extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private View myFragmentView;
    public static Bitmap selectedImage;
    private boolean cameraShot=false;
    private String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 5467;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public TakeAndSeePhoto() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment ReportMaking.
     */
    // TODO: Rename and change types and number of parameters
    public static TakeAndSeePhoto newInstance(String param1, Bitmap bitmap) {
        TakeAndSeePhoto fragment = new TakeAndSeePhoto();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        if(param1.equals("notToRetry")) {
            selectedImage = bitmap;
        } else selectedImage=ReportMaking.choosenImage;
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
        myFragmentView = inflater.inflate(R.layout.fragment_see_taken_photo, container, false);
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
        if (!ReportMaking.addedPhoto && !mParam1.equals("notToRetry")) {
            newOrSeeOldPhoto();
            dispatchTakePictureIntent();
        }
        Button okButton = (Button) getActivity().findViewById(R.id.ok_button);
        Button retry = (Button) getActivity().findViewById(R.id.repeat_button);
        ImageView seeImage = (ImageView) getActivity().findViewById(R.id.seeImage);
        seeImage.setImageBitmap(selectedImage);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mParam1.equals("notToRetry")) {
                    MainActivity.manager.popBackStackImmediate();
                } else {
                    if (cameraShot) {
                        ReportMaking.choosenImage = selectedImage;
                        ReportMaking.addedPhoto = true;
                        ReportMaking.imageReport.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ReportMaking.imageReport.setImageBitmap(ReportMaking.choosenImage);
                    }
                    MainActivity.manager.popBackStackImmediate();
                }
            }
        });
        if (mParam1.equals("notToRetry")) {
            retry.setText("");
        } else {
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dispatchTakePictureIntent();
                }
            });
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, ReportMaking.tempURI);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == getActivity().RESULT_OK) {
            final InputStream imageStream;
            ImageView seeImage = (ImageView) getActivity().findViewById(R.id.seeImage);
            try {
                imageStream = getActivity().getContentResolver().openInputStream(ReportMaking.tempURI);
                selectedImage = BitmapFactory.decodeStream(imageStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileOutputStream out = null;
            try {
                    out = new FileOutputStream(ReportMaking.tempFile);
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 10, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                final InputStream imageStreamCompressed;
                try {
                    imageStreamCompressed = getActivity().getContentResolver().openInputStream(ReportMaking.tempURI);
                    selectedImage = BitmapFactory.decodeStream(imageStreamCompressed);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                seeImage.setImageBitmap(selectedImage);
                cameraShot = true;
                ReportMaking.usedURI = true;
            }
        }
    }

    private void newOrSeeOldPhoto() {
        if (!ReportMaking.addedPhoto && !ReportMaking.usedURI) {
            File photoFile = null;
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String name = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                ReportMaking.tempFile = File.createTempFile(
                        name,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
                ReportMaking.fileName = ReportMaking.tempFile.getName();
                // Save a file: path for use with ACTION_VIEW intents
                mCurrentPhotoPath = ReportMaking.tempFile.getAbsolutePath();
                photoFile= ReportMaking.tempFile;
                } catch (IOException e) {
                e.printStackTrace();}

            if (photoFile != null) {
                photoFile.getAbsolutePath();
                ReportMaking.tempURI = FileProvider.getUriForFile(getActivity(),
                        "unicam.nettezzasmart.fileprovider",
                        photoFile);
            }
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
