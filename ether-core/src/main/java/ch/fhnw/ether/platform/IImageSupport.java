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
 */

package ch.fhnw.ether.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.image.IHostImage;
import ch.fhnw.ether.image.IImage;
import ch.fhnw.ether.image.IImage.AlphaMode;
import ch.fhnw.ether.image.IImage.ComponentFormat;
import ch.fhnw.ether.image.IImage.ComponentType;
import ch.fhnw.util.TextUtilities;

public interface IImageSupport {
	enum FileFormat {
		BMP, TIFF, PNG, JPEG, BIN;
		
		public static FileFormat get(File file) {
			return get(TextUtilities.getFileExtensionWithoutDot(file.getName()).toLowerCase());
		}
		
		public static FileFormat get(String ext) {
			switch (ext) {
			case "jpg":
			case "jpeg":
				return JPEG;
			case "png":
				return PNG;
			case "bin":
				return BIN;
			case "bmp":
				return BMP;
			case "tif":
			case "tiff":
				return TIFF;
			}
			throw new IllegalArgumentException("invalid image extension: " + ext);
		}
	}

	/**
	 * Read a host image from input stream.
	 * 
	 * @param in
	 *            the stream to read from.
	 * @param componentType
	 *            the requested component type for the image or null for using.
	 *            the best matching type
	 * @param componentFormat
	 *            the requested component format for the image or null for using
	 *            the best matching format.
	 * @param alphaMode
	 *            the requested alpha format or null for post multiplied.
	 * @param flipVertical
	 * 			flip the image vertically on load.
	 * @return the loaded image
	 * @throws IOException
	 *             if image cannot be read.
	 */
	IHostImage readHost(InputStream in, ComponentType componentType, ComponentFormat componentFormat, AlphaMode alphaMode) throws IOException;

	/**
	 * Read a GPU image from input stream.
	 * 
	 * @see #readHost(InputStream, ComponentType, ComponentFormat, AlphaMode)
	 */
	default IGPUImage readGPU(InputStream in, ComponentType componentType, ComponentFormat componentFormat, AlphaMode alphaMode) throws IOException {
		return readHost(in, componentType, componentFormat, alphaMode).createGPUImage();
	}

	/**
	 * Write an image to output stream.
	 * 
	 * @param image
	 *            the image to be written
	 * @param out
	 *            the stream to write to
	 * @param format
	 *            the requested file format or null for using default format
	 *            (jpg)
	 * @throws IOException
	 *             if image cannot be written
	 */
	void write(IImage image, OutputStream out, FileFormat format) throws IOException;

	/**
	 * Resize host image.
	 * 
	 * @param image
	 *            the image to be resized
	 * @param width
	 *            target width
	 * @param height
	 *            target height
	 * @return new image, resized to width*height
	 */
	IHostImage scale(IHostImage image, int width, int height);

	/**
	 * Returns true if this image support can read files of the given mime type.
	 * 
	 * @param mimeType
	 * @return
	 */
	boolean canRead(String mimeType);
	
	/**
	 * Returns true if this image support can write files of the given mime type.
	 * 
	 * @param mimeType
	 * @return
	 */
	boolean canWrite(String mimeType);
}
