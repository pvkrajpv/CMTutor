package smtlab.cse.varnamtutor;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

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
 * {@link SongPanel.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SongPanel#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongPanel extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int songId;
    private SongRecord songDet;
    private String selectedLine;
    private OnFragmentInteractionListener mListener;
    private  Spinner lineSpinner;
    private GraphView songGraph;
    private LineGraphSeries<DataPoint> refSeries;
    private int tonic;
    private PointsGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private float maxTime;
    private float minPitch;
    private float maxPitch;
    private Button buttonStart;
    private Button buttonStop;
    private Button buttonListen;
    AudioDispatcher dispatcher;
    PitchProcessor  pitchProcessor;
    WriterProcessor writerProcessor;
    File mFolder;

    CountDownTimer countDownTimer;
    boolean timehasstarted = false;
    long startTime = 3 * 500;
    long interval = 1 * 500;


    private MediaPlayer mp=new MediaPlayer();
    private Handler mHandler = new Handler();



    public SongPanel() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SongPanel.
     */
    // TODO: Rename and change types and number of parameters
    public static SongPanel newInstance(String param1, String param2) {
        SongPanel fragment = new SongPanel();
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
        View view =  inflater.inflate(R.layout.fragment_song_panel, container, false);
        mFolder = new File(Environment.getExternalStorageDirectory(), "VarnamTutor");
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            songId = bundle.getInt("songId");
            tonic =  bundle.getInt("tonic");
        }




        countDownTimer = new CountDownTimer(3*500, 500) {

            @Override
            public void onTick(long millisUntilFinished) {
                buttonStart.setText("" + millisUntilFinished / 500);
            }

            @Override
            public void onFinish() {
                buttonStart.setText("0");
                timehasstarted = false;
                buttonStart.setEnabled(false);
                startPitch();
            }
        };


        buttonStart = view.findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (!timehasstarted) {
                    countDownTimer.start();
                    timehasstarted = true;
                    buttonStart.setText("3");
                } else {
                    countDownTimer.cancel();
                    timehasstarted = false;
                    buttonStart.setText("Sing");
                }


            }
        });

        buttonStop = view.findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopPitch();
            }
        });

        buttonListen = view.findViewById(R.id.button_listen);
        buttonListen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(false);
                buttonListen.setEnabled(false);

                mSeries2 = new LineGraphSeries<>();
                songGraph.addSeries(mSeries2);
                mSeries2.setColor(Color.GRAY);
                mSeries2.setThickness(2);

                File wavfile = ((LivePitch)getActivity()).getGenWav(selectedLine, songDet);
                mp.reset();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        songGraph.removeSeries(mSeries2);
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        songGraph.getViewport().setMinX(0);
                        songGraph.getViewport().setMaxX(maxTime);
                        songGraph.refreshDrawableState();

                        buttonStart.setEnabled(true);
                        buttonStop.setEnabled(true);
                        buttonListen.setEnabled(true);

                    }
                });
                try {
                    FileInputStream is = new FileInputStream(wavfile);
                    mp.setDataSource(is.getFD());
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp.start();
                updateProgress();
            }
        });


        songDet = ((LivePitch)getActivity()).getSongDetails(songId);
        lineSpinner = view.findViewById(R.id.line_list);
        songGraph = view.findViewById(R.id.graph_song);
        songGraph.getViewport().setScalable(true);
        refSeries = new LineGraphSeries<>();
        songGraph.addSeries(refSeries);
        refSeries.setColor(Color.GRAY);
        refSeries.setThickness(5);
        songGraph.getViewport().setMinY(minPitch);
        songGraph.getViewport().setMaxY(maxPitch);

        songGraph.getViewport().setMinX(0);
        songGraph.getViewport().setMaxX(2);

        ArrayAdapter<String> adapter;


        adapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_spinner_item, songDet.getSegmentNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lineSpinner.setAdapter(adapter);
        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                setLine(lineSpinner.getSelectedItem().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        setLine(lineSpinner.getSelectedItem().toString());

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

    private void setLine(String lineName){
        selectedLine = lineName;
        DataPoint[] timeData =  ((LivePitch)getActivity()).getLineReference(lineName, songDet);
        String pitchResource =  ((LivePitch)getActivity()).getPitchResource(lineName, songDet);
        File waveFile =   ((LivePitch)getActivity()).getGenWav(lineName, songDet);
        refSeries.resetData(timeData);
        songGraph.removeSeries(mSeries1);
        songGraph.getViewport().setMinX(0);
        songGraph.getViewport().setMaxX(timeData[timeData.length-1].getX());
        maxTime = (float) timeData[timeData.length-1].getX() + 1;
        minPitch = 0;
        maxPitch = ( ((LivePitch)getActivity()).getMinMax(lineName, songDet)[1]) *(float) 1.2;
        WavSynth wavSynth = new WavSynth();
        wavSynth.writeWavFile(getActivity().getApplicationContext(),pitchResource,waveFile,songDet.getTonic(),tonic);

    }


    public void startPitch(){
        if(mSeries1!=null)
            songGraph.removeSeries(mSeries1);
        if(mSeries2!=null)
            songGraph.removeSeries(mSeries2);
        mSeries1 = new PointsGraphSeries<>();
        mSeries2 = new LineGraphSeries<>();
        songGraph.addSeries(mSeries1);
        songGraph.addSeries(mSeries2);
        songGraph.getViewport().setXAxisBoundsManual(true);
        songGraph.getViewport().setYAxisBoundsManual(true);
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
                                new DataPoint(currentTime, 0),
                                new DataPoint(currentTime, maxPitch)
                        });

                        if(pitchInHz!=-1) {
                            DataPoint newData = new DataPoint(audioEvent.getTimeStamp(), pitchInHz);
                            mSeries1.appendData(newData, true, 3000);
                            songGraph.getViewport().setMinX(audioEvent.getTimeStamp()-3);
                            songGraph.getViewport().setMaxX(audioEvent.getTimeStamp()+3);

                            songGraph.getViewport().setMinY(minPitch);
                            songGraph.getViewport().setMaxY(maxPitch);


                        }

                        if (currentTime > maxTime) {
                            stopPitch();
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
        File wavfile = new File(mFolder, String.format("S%s.wav", System.currentTimeMillis()));
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
        buttonStart.setEnabled(true);
        buttonStart.setText("Sing");
        songGraph.removeSeries(mSeries2);
        if (dispatcher!=null) {
            dispatcher.removeAudioProcessor(pitchProcessor);
            dispatcher.removeAudioProcessor(writerProcessor);
            dispatcher.stop();
            dispatcher= null;
        }
    }


    public void updateProgress() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long currentDuration = mp.getCurrentPosition();
            //buttonListen.setText(Long.toString(currentDuration));
            mSeries2.resetData(new DataPoint[] {
                    new DataPoint(currentDuration/1000, minPitch),
                    new DataPoint(currentDuration/1000, maxPitch)
            });

            songGraph.getViewport().setMinX(currentDuration/1000-1.5);
            songGraph.getViewport().setMaxX(currentDuration/1000+1.5);


            songGraph.getViewport().setMinY(minPitch);
            songGraph.getViewport().setMaxY(maxPitch);

            mHandler.postDelayed(this, 100);
        }
    };

}
