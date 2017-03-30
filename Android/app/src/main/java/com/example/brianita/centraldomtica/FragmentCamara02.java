package com.example.brianita.centraldomtica;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentCamara02.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class FragmentCamara02 extends Fragment {

    private OnFragmentInteractionListener mListener;

    public FragmentCamara02() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);
        View rstpView = inflater.inflate(R.layout.fragment_fragment_camara02, container, false);
        String rstpAddress = "rtsp://admin:admin@192.168.1.200:554/cam/realmonitor?channel=2&subtype=0";

        final VideoView camara02 = (VideoView)rstpView.findViewById(R.id.camara_02);
        Uri vidUri = Uri.parse(rstpAddress);
        camara02.setVideoURI(vidUri);


        MediaController vidControl = new MediaController(getActivity());
        vidControl.setAnchorView(camara02);
        camara02.setMediaController(vidControl);


        camara02.start();
        return rstpView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
