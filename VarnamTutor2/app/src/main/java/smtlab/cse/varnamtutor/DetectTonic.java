package smtlab.cse.varnamtutor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.writer.WriterProcessor;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetectTonic.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetectTonic#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetectTonic extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private ArrayList<Float> tonicList;
    private ArrayList<String> ttonicList;
    private String mParam1;
    private String mParam2;
    StringBuilder strBuilder;
    private Button buttonTonic;
    private Button buttonApply;
    private TextView tonicValueField;
    private TextView tonicTextShow;
    private String tonicFileName;
    private double lastPitch;
    private int tonic;
    private PointsGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    AudioDispatcher dispatcher;
    PitchProcessor  pitchProcessor;
    WriterProcessor writerProcessor;
    GraphView graph;

    File mFolder;

    private OnFragmentInteractionListener mListener;

    public DetectTonic() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetectTonic.
     */
    // TODO: Rename and change types and number of parameters
    public static DetectTonic newInstance(String param1, String param2) {
        DetectTonic fragment = new DetectTonic();
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
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_detect_tonic, container, false);


        mFolder = new File(Environment.getExternalStorageDirectory(), "VarnamTutor");

        buttonTonic = view.findViewById(R.id.buttonTonic);


        buttonTonic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startPitch(true,view);
            }
        });


        buttonApply= view.findViewById(R.id.buttonApply);

        tonicValueField = view.findViewById(R.id.tonicVal);
        buttonApply.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(tonicValueField.getText().toString().length()==0){
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                    alertDialog.setTitle("Tonic");
                    alertDialog.setMessage("Please detect tonic or enter manually");
                    alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                        }
                    });

                    alertDialog.show();
                }else {
                    ((LivePitch) getActivity()).setGlobalTonic(tonicValueField.getText().toString(), true);
                }
            }
        });


        return view;
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

    public void startPitch(final boolean detectTonic,View view){
        tonicList = new ArrayList<Float>();
        ttonicList = new ArrayList<String>();
        tonicValueField=  view.findViewById(R.id.tonicVal);
        graph = (GraphView) view.findViewById(R.id.graph);
        graph.removeAllSeries();
        mSeries1 = new PointsGraphSeries<>();
        mSeries2 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);
        graph.addSeries(mSeries2);
        graph.getViewport().setXAxisBoundsManual(true);


        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(700);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(1600);

        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);


        mSeries1.setShape(PointsGraphSeries.Shape.POINT);
        mSeries1.setSize(new Float(4));

        mSeries2.setColor(Color.GRAY);
        mSeries2.setThickness(2);
        int sampleRate = 44100;
        int windowLength = 4410;
        int windowShift = 441;
        int algoWL=7168;
        int overLap=algoWL - windowShift;
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate,algoWL,overLap);
        pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate, algoWL,windowLength,windowShift, new PitchDetectionHandler() {

            @Override
            public void handlePitch(final PitchDetectionResult pitchDetectionResult,
                                    final AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();

                final double currentTime = audioEvent.getTimeStamp();



                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView text = (TextView) findViewById(R.id.textview1);
                        //text.setText("" +  " " + currentTime);


                        mSeries2.resetData(new DataPoint[] {
                                new DataPoint(currentTime, lastPitch-100),
                                new DataPoint(currentTime, lastPitch+100)
                        });
                        buttonApply.setEnabled(false);
                        buttonTonic.setEnabled(false);
                        ttonicList.add(Double.toString(currentTime) +" " + Float.toString(pitchInHz) );
                        if(pitchInHz!=-1) {
                            lastPitch = pitchInHz;
                            DataPoint newData = new DataPoint(audioEvent.getTimeStamp(), pitchInHz);
                            mSeries1.appendData(newData, true, 1600);
                            graph.getViewport().setMinX(audioEvent.getTimeStamp()-10);
                            graph.getViewport().setMaxX(audioEvent.getTimeStamp()+10);
                            graph.getViewport().setMinY(lastPitch-100);
                            graph.getViewport().setMaxY(lastPitch+100);

                            graph.getViewport().setYAxisBoundsManual(true);
                            graph.getViewport().setXAxisBoundsManual(true);
                            if(detectTonic){
                                tonicList.add(pitchInHz);
                            }
                        }

                        if (currentTime > 10) {
                            stopPitch();
                            buttonTonic.setEnabled(true);
                            buttonApply.setEnabled(true);

                        }

                    }
                });

            }
        });

        dispatcher.addAudioProcessor(pitchProcessor);


        TarsosDSPAudioFormat format=new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                2 * 8,
                1,
                2 * 1,
                sampleRate,
                false);
        long curTime = System.currentTimeMillis();
        File wavfile = new File(mFolder, String.format("T%s.wav",curTime ));
        tonicFileName =String.format("T%s.txt", curTime);
        try {
            RandomAccessFile wfile=new RandomAccessFile(wavfile,"rw");
            writerProcessor = new WriterProcessor(format,wfile,windowShift);
            dispatcher.addAudioProcessor(writerProcessor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(dispatcher,"Audio Dispatcher").start();
    }


    public void stopPitch(){


        if (dispatcher!=null) {
            dispatcher.removeAudioProcessor(pitchProcessor);
            dispatcher.removeAudioProcessor(writerProcessor);
            dispatcher.stop();
            dispatcher= null;
            graph.removeSeries(mSeries2);
            if(tonicFileName.length()==0)
                ((LivePitch)getActivity()).writeToFile("Tonic.txt" ,ttonicList );
            else
                ((LivePitch)getActivity()).writeToFile(tonicFileName ,ttonicList );
            int [] tmp = getTonic(tonicList, 90,  300);
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Tonic Detected");
            strBuilder = new StringBuilder();
            strBuilder.append("Tonic detected is ");
            strBuilder.append(tmp[0]);
            strBuilder.append("Hz with a confidence of ");
            strBuilder.append(tmp[1]);
            strBuilder.append("%");
            alert.setMessage(strBuilder.toString());
            alert.setPositiveButton("OK",null);
            alert.show();

            tonic = Math.round(tmp[0]);
            tonicValueField.setText(Integer.toString(tonic));

        }
    }

    public static int[] getTonic(ArrayList<Float> signal, int minTonic, int maxTonic){
        int[] bins = new int[maxTonic-minTonic+1];
        for(int i=0;i<bins.length;i++)
            bins[i]=0;

        int lowLobe=0, highLobe=0;
        int totalCount =0;
        for(Float pitch:signal){
            if(pitch==-1.0)
                continue;
            totalCount++;
            if(pitch<minTonic)
                lowLobe++;
            else if(pitch>maxTonic)
                highLobe++;
            else
                bins[Math.round(pitch)-minTonic]++;

        }
        int max = bins[0];
        int maxIdx = 0;

        for(int i=1;i<bins.length;i++){
            if(bins[i]>max){
                max = bins[i];
                maxIdx = i;
            }
        }


        int tonic = minTonic + maxIdx;
        int tonicLevy =(int) Math.round(tonic * .03);
        int startP=0, endP=maxTonic-minTonic;
        int windowSum=0;
        if(maxIdx-tonicLevy>0)
            startP=maxIdx-tonicLevy;

        if(maxIdx+tonicLevy<maxTonic-minTonic)
            endP=maxIdx+tonicLevy;
        for(int i=startP;i<=endP;i++)
            windowSum = windowSum + bins[i];

        int[] retVal = new int[2];
        retVal[0]=tonic;
        retVal[1]=Math.round((float)windowSum/totalCount*100);
        return retVal;

    }

}
