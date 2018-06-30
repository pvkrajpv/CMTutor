package smtlab.cse.varnamtutor;

import android.content.Context;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class WavSynth {

    static RandomAccessFile raw;

    static int byteCount = 0;
    static int bitDepth = 16;
    static int nChannels = 1;
    static int dur = 1;


    final float MAX = (float) 0.8;
    int iFilterLength=  128;
    double dblFilterCoeff[][] = {
        {0.00013894, 0.00013027,	0.00018665,	0.00025466,	0.00033434,	0.00042494,	0.000525,	0.0006323,	0.00074376,	0.00085504,	0.00096141,	0.0010567,	0.0011343,	0.0011868,	0.001206,	0.0011839,	0.001112,	0.00098254,	0.00078808,	0.00052228,	0.00018028,	-0.00024104,	-0.00074262,	-0.0013228,	-0.0019773,	-0.0026985,	-0.0034757,	-0.0042947,	-0.0051383,	-0.0059857,	-0.0068135,	-0.0075952,	-0.0083028,	-0.0089061,	-0.0093744,	-0.0096766,	-0.0097824,	-0.009663,	-0.0092921,	-0.0086467,	-0.0077078,	-0.0064618,	-0.0049004,	-0.0030218,	-0.00083093,	0.0016602,	0.0044323,	0.0074591,	0.010707,	0.014137,	0.017703,	0.021354,	0.025036,	0.028692,	0.032261,	0.035685,	0.038905,	0.041863,	0.044508,	0.046792,	0.048672,	0.050113,	0.05109,	0.051583,	0.051583,	0.05109,	0.050113,	0.048672,	0.046792,	0.044508,	0.041863,	0.038905,	0.035685,	0.032261,	0.028692,	0.025036,	0.021354,	0.017703,	0.014137,	0.010707,	0.0074591,	0.0044323,	0.0016602,	-0.00083093,	-0.0030218,	-0.0049004,	-0.0064618,	-0.0077078,	-0.0086467,	-0.0092921,	-0.009663,	-0.0097824,	-0.0096766,	-0.0093744,	-0.0089061,	-0.0083028,	-0.0075952,	-0.0068135,	-0.0059857,	-0.0051383,	-0.0042947,	-0.0034757,	-0.0026985,	-0.0019773,	-0.0013228,	-0.00074262,	-0.00024104,	0.00018028,	0.00052228,	0.00078808,	0.00098254,	0.001112,	0.0011839,	0.001206,	0.0011868,	0.0011343,	0.0010567,	0.00096141,	0.00085504,	0.00074376,	0.0006323,	0.000525,	0.00042494,	0.00033434,	0.00025466,	0.00018665,	0.00013027,	0.00013894}, //Cutoff = 2 Hz//
        {-0.00012998,	-0.00016042,	-0.0002507,	-0.00036552,	-0.00050537,	-0.00066884,	-0.00085215,	-0.0010494,	-0.0012517,	-0.0014479,	-0.0016242,	-0.0017647,	-0.0018522,	-0.0018689,	-0.0017976,	-0.0016223,	-0.0013302,	-0.00091259,	-0.00036631,	0.00030534,	0.0010916,	0.0019736,	0.0029243,	0.0039088,	0.0048851,	0.0058055,	0.0066181,	0.0072699,	0.0077087,	0.0078864,	0.0077626,	0.0073068,	0.0065022,	0.0053477,	0.0038601,	0.0020753,	0.000048811,	-0.0021448,	-0.0044137,	-0.006651,	-0.0087386,	-0.010552,	-0.011963,	-0.01285,	-0.013099,	-0.012613,	-0.011314,	-0.0091529,	-0.0061079,	-0.0021915,	0.00255,	0.0080353,	0.01415,	0.020751,	0.027667,	0.034708,	0.041672,	0.048349,	0.054534,	0.060031,	0.064663,	0.068278,	0.070757,	0.072018,	0.072018,	0.070757,	0.068278,	0.064663,	0.060031,	0.054534,	0.048349,	0.041672,	0.034708,	0.027667,	0.020751,	0.01415,	0.0080353,	0.00255,	-0.0021915,	-0.0061079,	-0.0091529,	-0.011314,	-0.012613,	-0.013099,	-0.01285,	-0.011963,	-0.010552,	-0.0087386,	-0.006651,	-0.0044137,	-0.0021448,	0.000048811,	0.0020753,	0.0038601,	0.0053477,	0.0065022,	0.0073068,	0.0077626,	0.0078864,	0.0077087,	0.0072699,	0.0066181,	0.0058055,	0.0048851,	0.0039088,	0.0029243,	0.0019736,	0.0010916,	0.00030534,	-0.00036631,	-0.00091259,	-0.0013302,	-0.0016223,	-0.0017976,	-0.0018689,	-0.0018522,	-0.0017647,	-0.0016242,	-0.0014479,	-0.0012517,	-0.0010494,	-0.00085215,	-0.00066884,	-0.00050537,	-0.00036552,	-0.0002507,	-0.00016042,	-0.00012998},// Cutoff = 3 Hz
        {0.00013208,	0.00021065,	0.00036318,	0.0005715,	0.00084032,	0.0011698,	0.0015547,	0.001983,	0.0024356,	0.0028863,	0.0033028	,0.0036477	,0.0038816	,0.0039659	,0.0038662	,0.0035566	,0.003023	,0.0022669	,0.0013075	,0.00018317	,-0.0010491	,-0.0023153	,-0.0035287	,-0.004595	,-0.0054198	,-0.005916	,-0.0060125	,-0.0056623	,-0.0048494	,-0.0035942	,-0.0019559	,-0.000032179	,0.0020442	,0.0041128	,0.0059959	,0.0075112	,0.008487	,0.0087774	,0.0082779	,0.0069383	,0.0047735	,0.0018693	,-0.001616	,-0.0054561	,-0.0093671	,-0.013022	,-0.016072	,-0.018168,	-0.018986,	-0.018256,	-0.015778,	-0.011447,	-0.0052657,	0.0026485,	0.012064	,0.022644	,0.033959	,0.045514,	0.056772,	0.067192,	0.076254,	0.083497	,0.088547,	0.09114	,0.09114	,0.088547,	0.083497,	0.076254,	0.067192,	0.056772,	0.045514,	0.033959,	0.022644	,0.012064,	0.0026485,	-0.0052657,	-0.011447,	-0.015778,	-0.018256,	-0.018986,	-0.018168,	-0.016072,	-0.013022,	-0.0093671,	-0.0054561,	-0.001616,	0.0018693,	0.0047735,	0.0069383,	0.0082779,	0.0087774,	0.008487,	0.0075112,	0.0059959,	0.0041128,	0.0020442,	-0.000032179,	-0.0019559,	-0.0035942,	-0.0048494,	-0.0056623,	-0.0060125,	-0.005916,	-0.0054198,	-0.004595,	-0.0035287,	-0.0023153,	-0.0010491,	0.00018317,	0.0013075	,0.0022669,	0.003023,	0.0035566	,0.0038662,	0.0039659,	0.0038816,	0.0036477,	0.0033028,	0.0028863,	0.0024356,	0.001983	,0.0015547,	0.0011698,	0.00084032,	0.0005715	,0.00036318,	0.00021065,	0.00013208},// Cutoff = 4 Hz
        {0.000020586,	-0.00011675,	-0.00023635,	-0.0004472,	-0.00075277	,-0.00116	,-0.0016671	,-0.0022596	,-0.0029079,	-0.0035673,	-0.0041797,	-0.0046774	,-0.0049899	,-0.0050511,	-0.0048093,	-0.0042357	,-0.0033325	,-0.0021381,	-0.0007291	,0.00078324,	0.0022604	,0.003549	,0.0044974	,0.0049739	,0.0048854	,0.0041935	,0.0029261	,0.0011826,	-0.00087012,	-0.00301	,-0.0049804	,-0.0065179	,-0.0073844	,-0.0073992	,-0.0064681	,-0.0046049	,-0.0019422	,0.001272,	0.0046915,	0.007904	,0.010474	,0.011995	,0.01214	,0.010715	,0.0076948	,0.0032482	,-0.0022576	,-0.0082756,	-0.014119,	-0.01902	,-0.022203	,-0.022966	,-0.020758	,-0.015248	,-0.0063799	,0.0056051	,0.020166	,0.036495,	0.053576,	0.070266	,0.085391	,0.097853	,0.10672	,0.11134	,0.11134	,0.10672	,0.097853	,0.085391,	0.070266,	0.053576	,0.036495	,0.020166	,0.0056051	,-0.0063799	,-0.015248	,-0.020758	,-0.022966	,-0.022203,	-0.01902,	-0.014119,	-0.0082756	,-0.0022576	,0.0032482	,0.0076948	,0.010715	,0.01214	,0.011995	,0.010474,	0.007904,	0.0046915, 0.001272	,-0.0019422	,-0.0046049	,-0.0064681	,-0.0073992	,-0.0073844	,-0.0065179	,-0.0049804,	-0.00301,	-0.00087012,	0.0011826	,0.0029261	,0.0041935	,0.0048854	,0.0049739	,0.0044974	,0.003549	,0.0022604,	0.00078324,	-0.0007291,	-0.0021381	,-0.0033325	,-0.0042357	,-0.0048093	,-0.0050511	,-0.0049899	,-0.0046774	,-0.0041797,	-0.0035673,	-0.0029079,	-0.0022596	,-0.0016671	,-0.00116	,-0.00075277,	-0.0004472,	-0.00023635	,-0.00011675,	0.000020586},// Cutoff = 5 Hz
        {-0.000084523	,-0.000028783	,0.000035623	,0.0001857	,0.00045316	,0.0008639	,0.0014303	,0.0021434	,0.002967	,0.003835	,0.0046547	,0.0053139	,0.0056955	,0.0056949	,0.0052399	,0.0043093	,0.002946	,0.0012627	,-0.00056452	,-0.0023133	,-0.0037435	,-0.0046325	,-0.0048141	,-0.0042119	,-0.0028642	,-0.00093123	,0.0013165	,0.0035297	,0.0053305	,0.0063716	,0.0063994	,0.0053071	,0.0031692	,0.00024876	,-0.0030268	,-0.0061212	,-0.0084701	,-0.0095752	,-0.0090984	,-0.0069387	,-0.0032774	,0.0014201	,0.0064503	,0.010963	,0.01409	,0.015089	,0.013482	,0.0091769	,0.0025276	,-0.0056604	,-0.014197	,-0.021639	,-0.026473	,-0.027329	,-0.023183	,-0.013542	,0.0014477	,0.020959	,0.043541	,0.067262	,0.089922	,0.10931	,0.12346	,0.13093	,0.13093	,0.12346	,0.10931	,0.089922	,0.067262	,0.043541	,0.020959	,0.0014477	,-0.013542	,-0.023183	,-0.027329	,-0.026473	,-0.021639	,-0.014197	,-0.0056604	,0.0025276	,0.0091769	,0.013482	,0.015089	,0.01409	,0.010963	,0.0064503	,0.0014201	,-0.0032774	,-0.0069387	,-0.0090984	,-0.0095752	,-0.0084701	,-0.0061212	,-0.0030268	,0.00024876	,0.0031692	,0.0053071	,0.0063994	,0.0063716	,0.0053305	,0.0035297	,0.0013165	,-0.00093123,	-0.0028642	,-0.0042119	,-0.0048141	,-0.0046325	,-0.0037435	,-0.0023133	,-0.00056452	,0.0012627	,0.002946	,0.0043093	,0.0052399	,0.0056949	,0.0056955	,0.0053139	,0.0046547	,0.003835	,0.002967	,0.0021434	,0.0014303	,0.0008639	,0.00045316	,0.0001857	,0.000035623	,-0.000028783	,-0.000084523},// Cutoff = 6 Hz
        {0.00013115	,0.00017608	,0.00023262	,0.00022629	,0.00009955	,-0.00020778	,-0.00074308	,-0.0015242	,-0.0025237	,-0.0036588	,-0.0047923	,-0.0057458	,-0.0063268	,-0.0063653	,-0.0057541,	-0.0044843	,-0.0026665	,-0.0005303	,0.0016028	,0.0033704	,0.0044375	,0.0045698	,0.0036956	,0.0019403	,-0.00037655	,-0.002788	,-0.0047648	,-0.0058243	,-0.0056422	,-0.0041412	,-0.0015352	,0.0016886	,0.0048504	,0.0072128	,0.0081422	,0.0072667	,0.0045899	,0.00053289	,-0.0041152	,-0.0083363	,-0.011091	,-0.011555	,-0.0093327	,-0.0046037	,0.00185	,0.0087442	,0.014518	,0.017648	,0.016999	,0.012122	,0.0034615	,-0.0076131	,-0.018954	,-0.027958	,-0.032008	,-0.028972	,-0.017648	,0.0019173	,0.028315	,0.058899	,0.090132	,0.11811	,0.13917	,0.15047	,0.15047	,0.13917	,0.11811	,0.090132	,0.058899	,0.028315	,0.0019173	,-0.017648	,-0.028972	,-0.032008	,-0.027958	,-0.018954	,-0.0076131	,0.0034615	,0.012122	,0.016999	,0.017648	,0.014518	,0.0087442	,0.00185	,-0.0046037	,-0.0093327	,-0.011555	,-0.011091	,-0.0083363	,-0.0041152	,0.00053289	,0.0045899	,0.0072667	,0.0081422	,0.0072128	,0.0048504	,0.0016886	,-0.0015352	,-0.0041412	,-0.0056422	,-0.0058243	,-0.0047648	,-0.002788	,-0.00037655	,0.0019403,	0.0036956	,0.0045698	,0.0044375	,0.0033704	,0.0016028	,-0.0005303	,-0.0026665	,-0.0044843	,-0.0057541	,-0.0063653	,-0.0063268	,-0.0057458	,-0.0047923	,-0.0036588	,-0.0025237	,-0.0015242	,-0.00074308,	-0.00020778	,0.00009955,	0.00022629	,0.00023262	,0.00017608	,0.00013115},// Cutoff = 7 Hz
        {-0.00017348	,-0.00034779,	-0.00060723,	-0.00088361	,-0.0010893	,-0.0011081	,-0.00081827	,-0.00012774	,0.00098803	,0.0024555	,0.0040913	,0.0056199	,0.0067204	,0.0070987	,0.0065684	,0.005119	,0.0029505	,0.0004564	,-0.0018474	,-0.0034389	,-0.003925	,-0.0031592	,-0.0013088	,0.0011575	,0.0035668	,0.0052074	,0.0055247	,0.0042937	,0.0017167	,-0.0015912	,-0.0047379	,-0.0067847	,-0.0070159	,-0.0051732	,-0.0015813	,0.002883	,0.0069874	,0.0094672	,0.0093947	,0.0064916	,0.0012875	,-0.0049407	,-0.010454	,-0.013508	,-0.012862	,-0.0082092	,-0.00039013	,0.0087001	,0.016513	,0.020489	,0.018788	,0.010922	,-0.0018915	,-0.016781	,-0.029709	,-0.036308	,-0.032905	,-0.017475	,0.0096934	,0.045814	,0.085974	,0.124	,0.15366	,0.1699	,0.1699	,0.15366	,0.124	,0.085974	,0.045814	,0.0096934	,-0.017475	,-0.032905	,-0.036308	,-0.029709	,-0.016781	,-0.0018915	,0.010922	,0.018788	,0.020489	,0.016513	,0.0087001	,-0.00039013,	-0.0082092,	-0.012862	,-0.013508,	-0.010454	,-0.0049407	,0.0012875	,0.0064916	,0.0093947	,0.0094672	,0.0069874	,0.002883	,-0.0015813	,-0.0051732	,-0.0070159	,-0.0067847	,-0.0047379	,-0.0015912	,0.0017167	,0.0042937	,0.0055247	,0.0052074	,0.0035668	,0.0011575	,-0.0013088	,-0.0031592	,-0.003925	,-0.0034389	,-0.0018474	,0.0004564	,0.0029505	,0.005119	,0.0065684	,0.0070987	,0.0067204	,0.0056199	,0.0040913	,0.0024555	,0.00098803	,-0.00012774,	-0.00081827,	-0.0011081	,-0.0010893,	-0.00088361	,-0.00060723	,-0.00034779,	-0.00017348},// Cutoff = 8 Hz
        {0.00022166	,0.00058003	,0.0011949	,0.0020433	,0.0030288	,0.0039624	,0.0045874	,0.0046393	,0.0039282	,0.0024206	,0.00029267	,-0.0020743	,-0.0041657	,-0.0054653	,-0.0056123	,-0.0045359	,-0.0025169	,-0.00014154	,0.0018508	,0.0027923	,0.0023141	,0.00049959	,-0.0021011	,-0.0046162	,-0.0061261	,-0.0059839	,-0.0040723	,-0.00090043	,0.0025157	,0.004967	,0.0054702	,0.0036488	,-000063615	,-0.0044942	,-0.0080789	,-0.0093923	,-0.0076858	,-0.0032385	,0.002627	,0.0079106	,0.010589	,0.0093559	,0.0041893	,-0.0034828	,-0.011123	,-0.015868	,-0.015535	,-0.0095069	,0.0008215	,0.012259	,0.020694	,0.022429	,0.015552	,0.00092498	,-0.017624	,-0.034025	,-0.041507	,-0.034544	,-0.010653	,0.0285	,0.07709	,0.12627	,0.16624	,0.18862	,0.18862	,0.16624	,0.12627	,0.07709	,0.0285	,-0.010653	,-0.034544	,-0.041507	,-0.034025	,-0.017624	,0.00092498	,0.015552	,0.022429	,0.020694	,0.012259	,0.0008215	,-0.0095069	,-0.015535	,-0.015868	,-0.011123	,-0.0034828	,0.0041893	,0.0093559	,0.010589	,0.0079106	,0.002627	,-0.0032385	,-0.0076858	,-0.0093923	,-0.0080789	,-0.0044942	,-0.000063615,	0.0036488	,0.0054702	,0.004967	,0.0025157	,-0.00090043	,-0.0040723	,-0.0059839	,-0.0061261	,-0.0046162	,-0.0021011	,0.00049959	,0.0023141	,0.0027923	,0.0018508	,-0.00014154	,-0.0025169	,-0.0045359	,-0.0056123	,-0.0054653	,-0.0041657	,-0.0020743	,0.00029267	,0.0024206	,0.0039282	,0.0046393	,0.0045874	,0.0039624	,0.0030288	,0.0020433	,0.0011949	,0.00058003	,0.00022166},// Cutoff = 9 Hz
        {-0.000023212	,-0.00033482	,-0.00089187	,-0.0018491	,-0.0031596	,-0.0046453	,-0.0059769	,-0.0067421	,-0.0065713	,-0.0052883	,-0.003028	,-0.00026344	,0.0022917	,0.0038961	,0.0040444	,0.0026851	,0.00029591	,-0.0022384			,-0.0039254	,-0.0040439	,-0.0024567	,0.00028136	,0.0030914	,0.004775	,0.0045117	,0.0022458	,-0.0012151		,-0.0044647	,-0.0060513	,-0.0051088	,-0.0017976	,0.0026575	,0.0063957	,0.0076751	,0.0056582	,0.00088331	,-0.0048216	,-0.0090069	,-0.0096207	,-0.0059691	,0.00081548	,0.0080611	,0.01256	,0.011931	,0.0057872	,-0.003838	,-0.013069	,-0.017659	,-0.014821	,-0.0046719	,0.0093737	,0.02158	,0.026009	,0.019056	,0.001428	,-0.021309	,-0.040012	,-0.044802	,-0.028567	,0.010095	,0.065621	,0.1265	,0.17834	,0.20812	,0.20812	,0.17834	,0.1265	,0.065621	,0.010095	,-0.028567	,-0.044802	,-0.040012	,-0.021309	,0.001428	,0.019056	,0.026009	,0.02158	,0.0093737	,-0.0046719	,-0.014821	,-0.017659	,-0.013069	,-0.003838	,0.0057872	,0.011931	,0.01256	,0.0080611	,0.00081548	,-0.0059691	,-0.0096207	,-0.0090069	,-0.0048216	,0.00088331	,0.0056582	,0.0076751	,0.0063957	,0.0026575	,-0.0017976,	-0.0051088	,-0.0060513	,-0.0044647	,-0.0012151	,0.0022458	,0.0045117	,0.004775	,0.0030914	,0.00028136	,-0.0024567	,-0.0040439	,-0.0039254	,-0.0022384	,0.00029591	,0.0026851	,0.0040444	,0.0038961	,0.0022917	,-0.00026344	,-0.003028	,-0.0052883		,-0.0065713,	-0.0067421	,-0.0059769	,-0.0046453	,-0.0031596	,-0.0018491	,-0.00089187	,-0.00033482	,-0.00002},// Cutoff = 10 Hz
    };

    public void writeWavFile(Context myContext,String resource,File wavFile,int orgTonic,int tonic){
        ArrayList<String> allFileData = getFileData(myContext, resource);
        int totalLength = allFileData.size();
        float[] amplitude= new float[totalLength];
        float[] timeList= new float[totalLength];
        float[] pitchList= new float[totalLength];
        for(int i=0;i<allFileData.size();i++) {
            String tmp[] = allFileData.get(i).split(" ");
            timeList[i] = Float.parseFloat(tmp[0]);
            pitchList[i] = Float.parseFloat(tmp[1]);
            amplitude[i] = Float.parseFloat(tmp[2]);
        }
        pitchList = tonicNormalize(pitchList, orgTonic, tonic);
        writeWavFile(allFileData, wavFile,timeList,pitchList,amplitude);
    }

    public void writeWavFile( ArrayList<String> allFileData, File wavFile, float[] timeList, float[] pitchList,float[] amplitude){

        File fptr_read, debug;
        long size;
        float pi =(float) 3.1415926535;
        int i=0,j=0, length=0, phase_len=0, srate=16000, written, iLPChoice;
        float pitch, tim, max_amp=0, t=0, temp=0, phase1=0, phase2 =0, phase3 =0, dummy1, prev_time=0;
        float[] scale1, scale2, window;
        float prev_mean, next_mean;
        double[] dblAmplitude;
        long k1, tot_samples=0;
        int index,no_samples=0;
        int  M, L;
        short x[];
        iLPChoice = 3;
        RandomAccessFile fwavwrite = null;
        ArrayList<Byte> wavData = new ArrayList<Byte>();


        try
        {
            raw = new RandomAccessFile(wavFile.getAbsolutePath(), "rw");

            raw.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
            raw.writeBytes("RIFF");
            raw.writeInt(0); // Final file size not known yet, write 0. This is = sample count + 36 bytes from header.
            raw.writeBytes("WAVE");
            raw.writeBytes("fmt ");
            raw.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
            raw.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
            raw.writeShort(Short.reverseBytes((short)nChannels));// Number of channels, 1 for mono, 2 for stereo
            raw.writeInt(Integer.reverseBytes(srate)); // Sample rate
            raw.writeInt(Integer.reverseBytes(srate*bitDepth*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*bitDepth/8
            raw.writeShort(Short.reverseBytes((short)(nChannels*bitDepth/8))); // Block align, NumberOfChannels*bitDepth/8
            raw.writeShort(Short.reverseBytes((short)bitDepth)); // Bit Depth
            raw.writeBytes("data");
            raw.writeInt(0); // Data chunk size not known yet, write 0. This is = sample count.
        }
        catch(IOException e)
        {
            System.out.println("I/O exception occured while writing data");
        }




        dblAmplitude = new double[allFileData.size()];

        for(i=0; i<length; i++)
        {
            amplitude[i] = (float) 0.8 * (amplitude[i])/ max_amp;
            dblAmplitude[i] = (double) amplitude[i];

        }

        //if ((iLPChoice>0) && (length>iFilterLength*2))
        //   vSmoothAmplitudeEnvelope(iLPChoice, dblAmplitude, length);
        for (i=0;i<length ;i++ )
            amplitude[i]=(float) dblAmplitude[i];

        j=0;
        ArrayList<Short> allSamples = new ArrayList<Short>();
        while (j<allFileData.size()-1)
        {

            tim =timeList[j];
            pitch =pitchList[j];

            if (j==0)
                //phase_len = (int) (tim * (float)srate);
                phase_len = (int) Math.round(tim * (float)srate);
            else
                //phase_len = (int) ((tim - prev_time) * srate);
                phase_len = ((int) Math.round(tim*srate)) - (int) tot_samples;

            prev_time = tim;
            //25% overlap

            M = (int) ((float) phase_len/2.5); //25% overlap
            L = phase_len; //length of the buffer
            //printf("\ntot_len = %d", tot_len);

            window = new float[L+M+1];

            x = new short[L+M+1];


            scale1 = new float[phase_len+1];
            scale2 = new float[phase_len+1];

            no_samples=0;


            if(j==0) prev_mean = 0;
            else
            {
                if(amplitude[j-1] == 0)
                    prev_mean = 0;
                else
                    prev_mean = (amplitude[j]+amplitude[j-1])*MAX/2;
            }
            if(j == length-1) next_mean = (0+amplitude[j])*MAX/2;
            else
            {
                if(amplitude[j+1] == 0)
                    next_mean = 0;
                else
                    next_mean = (amplitude[j]+amplitude[j+1])*MAX/2;
            }


            //printf("\nj= %d", j);
            //printf("\nphase_len = %d", phase_len);
            for ( index = 0; index < L ; index++ )
            {
                if(index < M)
                {
                    if(amplitude[j]!=0)
                        window[index] = MAX+ (( MAX- (prev_mean/amplitude[j]) )/M ) * (index-M);
                    else
                        window[index] = MAX+ (( MAX - prev_mean )/M ) * (index-M);
                }
                else if ( index > (L-M))
                {
                    if(amplitude[j]!=0)
                        window[index] = MAX+ (((next_mean/amplitude[j])-MAX)/M) *(index-(L-M));
                    else
                        window[index] = MAX + (( MAX - next_mean )/M ) * (index-M);
                }else window[index] = MAX;
            }
            //getchar();
            //generate the sine wave for each frame  change <=
            for (index=0,t=0;index<phase_len;index++,t=t+(float)(1.0/srate))
            {
                temp=(float) (((amplitude[j])) * (Math.sin(2*pi*(pitch)*t + phase1) + Math.sin(2*pi*(2*pitch)*t + phase2) + Math.sin(2*pi*(3*pitch)*t + phase3))/3.0)  ;
			x[no_samples] = (short) (temp * 32767/*pow(2,15)*/);
                no_samples++;
            }

            //Update phase at the end of every frame
            if ((amplitude[j])!=0){
                phase1 = (2*pi*t*pitch) + phase1;
                phase2 = (2*pi*t*2*pitch) + phase2;
                phase3 = (2*pi*t*3*pitch) + phase3;
            }

            /****************************x contains new tpe frame here*****************************/
            for(index=0; index<L; index++){
                    ByteBuffer buffer = ByteBuffer.allocate(2);
                    buffer.putShort((short) (x[index]*window[index]));
                    wavData.add(buffer.array()[1]);
                    wavData.add(buffer.array()[0]);
                    byteCount += 2;
            }


            tot_samples += L;
            j++;
        }

        byte[] data = new byte[wavData.size()];
        for(int k=0;k<wavData.size();k++){
            data[k]=wavData.get(k);
        }

        try
        {
           raw.write(data);
        }
         catch(IOException e)
        {
          System.out.println("I/O exception occured while writing data");
        }
        closeFile();



    }



    static void closeFile()
    {
        try
        {
            raw.seek(4); // Write size to RIFF header
            raw.writeInt(Integer.reverseBytes(byteCount + 36));
            raw.seek(40); // Write size to Subchunk2Size field
            raw.writeInt(Integer.reverseBytes(byteCount));
            raw.close();
        }
        catch(IOException e)
        {
            System.out.println("I/O exception occured while closing output file");
        }
    }



    void vSmoothAmplitudeEnvelope(int iLPChoice, double[] amplitude, int iNumFrames)
    {

        int iCounter, iFrameCounter;
        double[] dblTempFilter, dblFilterOutput;

        //Initialize filter coefficients
        dblTempFilter = new double[iFilterLength];
        for (iCounter=0; iCounter<iFilterLength; iCounter++)
            dblTempFilter[iCounter]=dblFilterCoeff[iLPChoice-2][iCounter];

        dblFilterOutput = new double[iNumFrames+iFilterLength-1];
        convolve (dblFilterOutput, amplitude, iNumFrames, dblTempFilter, iFilterLength);

        for (iCounter=iFilterLength/2, iFrameCounter=0;iFrameCounter<iNumFrames; iFrameCounter++, iCounter++)
        {
            amplitude[iFrameCounter]=(double) Math.abs(dblFilterOutput[iCounter]);
            if (amplitude[iFrameCounter]<0.0)
                amplitude[iFrameCounter]=0.0;
        }


    }


    void convolve (double[] y, double[] x, int L, double[] h, int M)
    {
        int n,i,k;
        double[] temp;
        double sum=0;

        temp = new double[L+2*M-2];


        for(i=0; i<L+2*M-2; i++) //zero pad the signal at the start and end.
        {
            if (i < M-1 )	temp[i] = 0;
            else if (i >= (M-1) && i < (L + M) ) temp[i] = x[i - (M-1)];
            else temp[i] = 0;
            //printf("\ntemp[%d] = %f", i, temp[i]);
        }

        for(n = M-1 ; n < L + 2*M - 2 ; n++) {
            sum = 0;
            for (k = 0; k < M; k++)
                sum += temp[n - k] * h[k];
            y[n - (M - 1)] = sum;
            //printf("\ny[%d] = %f", n-(M-1), y[n-(M-1)]);
        }
    }

    public ArrayList<String> getFileData(Context myContext, String resource){

        ArrayList<String> allFileData = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(myContext.getAssets().open(resource)));
            String mLine;

            while ((mLine = reader.readLine()) != null) {
                mLine = mLine.trim();
                if(mLine.length()>0){
                    mLine = mLine.trim();
                    if(mLine.length()>0)
                        allFileData.add(mLine);

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return allFileData;
    }

    public  DataPoint[] getPitchCurve( ArrayList<String> allFileData,int orgTonic,int newTonic){
        String[] tmp;
        DataPoint[] data = new DataPoint[allFileData.size()];
        String dataLine;
        int totalLength = allFileData.size();
        float[] amplitude= new float[totalLength];
        float[] timeList= new float[totalLength];
        float[] pitchList= new float[totalLength];
        for(int i=0;i<allFileData.size();i++) {
            tmp = allFileData.get(i).split(" ");
            timeList[i] = Float.parseFloat(tmp[0]);
            pitchList[i] = Float.parseFloat(tmp[1]);
        }
        pitchList = tonicNormalize(pitchList,orgTonic,newTonic);
        for(int i=0; i<allFileData.size();i++){
            DataPoint newData = new DataPoint(timeList[i], pitchList[i]);
            data[i] = newData;
        }
        return data;
    }

    public static float[] tonicNormalize(float[] reference_pitch,int orgTonic, int newTonic) {
        float[] newPitch = new float[reference_pitch.length];
        float tmp;
        for(int i=0;i<reference_pitch.length;i++) {
            tmp =(float) (newTonic * (float) Math.pow(2, log2(reference_pitch[i]/orgTonic)));
            newPitch[i] = tmp;
        }
        return newPitch;
    }
    public static double log2(double num)
    {
        return ((double)Math.log(num)/Math.log(2));
    }

    public  float[] getMinMaxPitch( ArrayList<String> allFileData){
        String[] tmp;
        float[] minMax = new float[2];
        int totalLength = allFileData.size();
        minMax[0]=0;
        minMax[1]=0;
        float pitch;
        for(int i=0;i<allFileData.size();i++) {
            tmp = allFileData.get(i).split(" ");
            pitch = Float.parseFloat(tmp[1]);
            if(minMax[0]>pitch){
                minMax[0] = pitch;
            }else if(minMax[1]<pitch){
                minMax[1] = pitch;
            }
        }
       return minMax;
    }


}
