/*
 * Copyright (c) 2013 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2013 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Contributions by: Filip Schramka, Samuel von Stachelski
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */package ch.fhnw.ether.audio.fx;

 import ch.fhnw.ether.audio.AudioFrame;
 import ch.fhnw.ether.audio.AudioUtilities;
 import ch.fhnw.ether.audio.ButterworthFilter;
 import ch.fhnw.ether.audio.IAudioRenderTarget;
 import ch.fhnw.ether.audio.Smooth;
 import ch.fhnw.ether.media.AbstractRenderCommand;
 import ch.fhnw.ether.media.RenderCommandException;
 import ch.fhnw.ether.ui.IPlotable;
 import ch.fhnw.util.color.RGB;
 import ch.fhnw.util.math.MathUtilities;

 public class BandsButterworth extends AbstractRenderCommand<IAudioRenderTarget> implements IPlotable {
	 private final int     size;
	 private final double  lowers[];
	 private final double  uppers[];
	 private final double  centers[];
	 private final boolean center[];
	 private final int     strength;

	 private ButterworthFilter filters[][];
	 private Smooth            smooth;
	 private float[]           power;

	 public BandsButterworth(int strength, double bandWidthPercent, float ... freqs) {
		 this(strength, -bandWidthPercent, true, freqs);
	 }

	 public BandsButterworth(int strength, double bandWidth, boolean centered, float ... freqs) {
		 this.strength = strength;
		 if(centered) {
			 size = freqs.length;
			 centers = new double[size];
			 center  = new boolean[size];  
			 lowers  = new double[size];
			 uppers  = new double[size];
			 for(int i = 0; i < size; i++) {
				 double range = bandWidth < 0 ? freqs[i] * (bandWidth/-100) : bandWidth / 2;
				 lowers[i]  = freqs[i] - range;
				 uppers[i]  = freqs[i] + range;
				 centers[i] = freqs[i];
				 center[i] = true;
			 }
		 } else {
			 size = freqs.length - 1;
			 centers = new double[size];
			 center  = new boolean[size];  
			 lowers  = new double[size];
			 uppers  = new double[size];
			 for(int i = 0; i < size; i++) {
				 lowers[i]  = freqs[i];
				 uppers[i]  = freqs[i+1];
				 centers[i] = Math.exp((Math.log(lowers[i]) + Math.log(uppers[i])) / 2.0);
				 double bw = uppers[i] - lowers[i]; 
				 if(bw < bandWidth) {
					 center[i] = true;
					 lowers[i] = centers[i] - bw / 2;
					 uppers[i] = centers[i] + bw / 2;
				 }
			 }
		 }
	 }

	 public BandsButterworth(float lower, float upper, double minBandWidth, int size, int strength) {
		 if(lower >= upper)
			 throw new IllegalArgumentException();
		 this.strength = strength;
		 this.size = size;
		 centers = new double[size];
		 center  = new boolean[size];  
		 lowers = new double[size];
		 uppers = new double[size];
		 double logLower = Math.log(lower);
		 double logUpper = Math.log(upper);
		 double logLast = logLower;
		 for(int i = 0; i < size; i++)
		 {
			 double logStart = logLast;
			 double logStop  = logLast + (logUpper - logLast) / (size - i);
			 double start    = Math.exp(logStart);
			 double stop     = Math.exp(logStop);
			 if(stop - start < minBandWidth) {
				 stop = start + minBandWidth;
				 logStop = Math.log(stop);
			 }
			 centers[i] = Math.exp((logStart + logStop) / 2.0);
			 lowers[i] = start;
			 uppers[i] = stop;

			 logLast = logStop;
		 }
	 }


	 @Override
	 public void init(IAudioRenderTarget target) {
		 smooth = new Smooth(centers.length, 0.01f);
		 power  = new float[centers.length];
		 filters = new ButterworthFilter[size][strength];

		 for(int i = 0; i < size; i++)
			 for(int j = 0; j < strength; j++)
				 if(center[i])
					 filters[i][j] = ButterworthFilter.getBandpassFilter0(target.getSampleRate(), centers[i], uppers[i] - lowers[i]);
				 else
					 filters[i][j] = ButterworthFilter.getBandpassFilter(target.getSampleRate(), lowers[i], uppers[i]);
	 }

	 public float power(int i) {
		 return smooth.get(i);
	 }

	 public float[] power(float[] values) {
		 System.arraycopy(power, 0, values, 0, Math.min(values.length, power.length));
		 return values;
	 }

	 public float[] powerSmooth(float[] values) {
		 return smooth.get(values);
	 }

	 public int numBands() {
		 return centers.length;
	 }

	 @Override
	 protected void run(final IAudioRenderTarget target) throws RenderCommandException {
		 final AudioFrame frame = target.getFrame();
		 for(int band = 0; band < centers.length; band++) {
			 float[] samples = frame.getMonoSamples().clone();
			 for(int i = 0; i < filters[band].length; i++)
				 filters[band][i].processBand(samples);
			 power[band] = MathUtilities.clamp(AudioUtilities.energy(samples) * centers.length, 0, 1);
		 }
		 smooth.update(frame.playOutTime, power);
		 clear();
		 column(power, 0, 1, RGB.WHITE);
	 }	

	 @Override
	 public int getPlotHeight() {
		 return numBands();
	 }
 }
