package be.tarsos.dsp.writer;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * This class writes the ongoing sound to an output specified by the programmer
 *
 */
public class WriterProcessor implements AudioProcessor {
    RandomAccessFile output;
    TarsosDSPAudioFormat audioFormat;
    private int audioLen=0;
    private  static final int HEADER_LENGTH=44;//byte
    private int overLap=0;
    private byte[] lastBuffer;
    private byte[] tmp;
    /**
     *
     * @param audioFormat which this processor is attached to
     * @param output randomaccessfile of the output file
     */
    public WriterProcessor(TarsosDSPAudioFormat audioFormat,RandomAccessFile output, int overlap){
        this.output=output;
        this.audioFormat=audioFormat;
        this.overLap=overlap;
        try {
            output.write(new byte[HEADER_LENGTH]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean process(AudioEvent audioEvent) {
        try {
            //audioLen+=audioEvent.getByteBuffer().length;
        	audioLen+=overLap*2;
            //write audio to the output
        	lastBuffer = audioEvent.getByteBuffer();
        	
        	tmp = Arrays.copyOfRange(lastBuffer, 0, this.overLap*2);
        	
            output.write(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void processingFinished() {
        //write header and data to the result output
        WaveHeader waveHeader=new WaveHeader(WaveHeader.FORMAT_PCM,
                (short)audioFormat.getChannels(),
                (int)audioFormat.getSampleRate(),(short)16,audioLen);//16 is for pcm, Read WaveHeader class for more details
        ByteArrayOutputStream header=new ByteArrayOutputStream();
        try {
        	tmp = Arrays.copyOfRange(lastBuffer, this.overLap*2, lastBuffer.length);
        	output.write(tmp);
        	audioLen=audioLen + lastBuffer.length - (overLap*2) ;
            waveHeader.write(header);
            output.seek(0);
            output.write(header.toByteArray());
            output.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
