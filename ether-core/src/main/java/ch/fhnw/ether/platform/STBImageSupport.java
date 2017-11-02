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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.image.IHostImage;
import ch.fhnw.ether.image.IImage;
import ch.fhnw.ether.image.IImage.AlphaMode;
import ch.fhnw.ether.image.IImage.ComponentFormat;
import ch.fhnw.ether.image.IImage.ComponentType;
import ch.fhnw.util.ArrayUtilities;
import ch.fhnw.util.MIME;
import ch.fhnw.util.TextUtilities;

public class STBImageSupport implements IImageSupport {

	@Override
	public IHostImage readHost(InputStream in, ComponentType componentType, ComponentFormat componentFormat, AlphaMode alphaMode) throws IOException {
		return (IHostImage)read(in, componentType, componentFormat, alphaMode, true);
	}

	@Override
	public IGPUImage readGPU(InputStream in, ComponentType componentType, ComponentFormat componentFormat, AlphaMode alphaMode) throws IOException {
		return (IGPUImage)read(in, componentType, componentFormat, alphaMode, false);
	}

	private static boolean ImageIOPlugins;
	public IImage read(InputStream in, ComponentType componentType, ComponentFormat componentFormat, AlphaMode alphaMode, boolean host) throws IOException {
		// TODO: Pre-multiplied alpha support

		STBImage.stbi_set_flip_vertically_on_load(true);
		STBImage.stbi_set_unpremultiply_on_load(true);

		int numComponentsRequested = componentFormat != null ? componentFormat.getNumComponents() : 0;
		byte[] bytes = getBytesFromInputStream(in);


		// check for TIFF header and convert TIFF to PNG
		if((bytes[0]=='I' && bytes[1]=='I') ||
				(bytes[0]=='M' && bytes[1]=='M')) {
			if(!(ImageIOPlugins)) {
				ImageIO.scanForPlugins();
				ImageIOPlugins = true;
			}
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(img, "PNG", out);
			bytes = out.toByteArray();
			STBImage.stbi_set_flip_vertically_on_load(false);
		}
		ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
		buffer.put(bytes).flip();

		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer numComponents = BufferUtils.createIntBuffer(1);

		if (componentType == null)
			componentType = ComponentType.BYTE;

		Buffer pixels;
		if (componentType == ComponentType.BYTE) {
			pixels = STBImage.stbi_load_from_memory(buffer, width, height, numComponents, numComponentsRequested);
		} else if (componentType == ComponentType.FLOAT) {
			pixels = STBImage.stbi_loadf_from_memory(buffer, width, height, numComponents, numComponentsRequested);
		} else {
			throw new IllegalArgumentException("unsupported component type: " + componentType);
		}
		if (pixels == null)
			throw new IOException("can't load image: " + STBImage.stbi_failure_reason());
		pixels.rewind();

		if (componentFormat == null)
			componentFormat = ComponentFormat.get(numComponents.get(0));

		if (alphaMode == null)
			alphaMode = AlphaMode.POST_MULTIPLIED;
		else if (alphaMode != AlphaMode.POST_MULTIPLIED)
			throw new UnsupportedOperationException("premultiplied alpha unsupported");

		IImage image = null;
		if (componentType == ComponentType.BYTE) {
			ByteBuffer bytePixels = (ByteBuffer)pixels;
			if (host) {
				// note: we need to create a copy here in order to explicity release the STB-managed buffer.
				ByteBuffer copy = BufferUtils.createByteBuffer(bytePixels.capacity());
				copy.put(bytePixels);
				image = IHostImage.create(width.get(0), height.get(0), componentType, componentFormat, alphaMode, copy);
			} else {
				// no copy required for gpu-fast path
				image = IGPUImage.create(width.get(0), height.get(0), componentType, componentFormat, alphaMode, bytePixels);
			}
			bytePixels.rewind();
			STBImage.stbi_image_free(bytePixels);
		} else {
			// note: for float images, we need to obtain a copy anyway, since we need to pass on a byte buffer
			FloatBuffer floatPixels = (FloatBuffer)pixels;
			ByteBuffer copy = BufferUtils.createByteBuffer(floatPixels.capacity() * 4);
			copy.asFloatBuffer().put(floatPixels);
			if (host) {
				image = IHostImage.create(width.get(0), height.get(0), componentType, componentFormat, alphaMode, copy);
			} else {
				image = IGPUImage.create(width.get(0), height.get(0), componentType, componentFormat, alphaMode, copy);
			}
			floatPixels.rewind();
			STBImage.stbi_image_free(floatPixels);
		}

		return image;
	}

	@Override
	public void write(IImage frame, OutputStream out, FileFormat format) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IHostImage scale(IHostImage image, int width, int height) {
		if (image.getWidth() == width && image.getHeight() == height)
			return image;

		ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * image.getNumBytesPerPixel());

		switch (image.getComponentType()) {
		case BYTE:
			STBImageResize.stbir_resize_uint8(image.getPixels(), image.getWidth(), image.getHeight(), 0, pixels, width, height, 0, image.getComponentFormat().getNumComponents());
			break;
		case FLOAT:
			STBImageResize.stbir_resize_float(image.getPixels().asFloatBuffer(), image.getWidth(), image.getHeight(), 0, pixels.asFloatBuffer(), width, height, 0, image.getComponentFormat().getNumComponents());
			break;
		}
		return IHostImage.create(width, height, image.getComponentType(), image.getComponentFormat(), image.getAlphaMode(), pixels);
	}

	private static byte[] getBytesFromInputStream(InputStream in) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[0xffff];
			for (int len; (len = in.read(buffer)) != -1;)
				os.write(buffer, 0, len);
			os.flush();
			return os.toByteArray();
		}
	}

	private static final String[] TYPES = {
			MIME.MT_PNG, MIME.MT_JPEG, MIME.MT_BMP, MIME.MT_TGA, MIME.MT_PSD, MIME.MT_HDR, MIME.MT_PICT, MIME.MT_PNM,
	};

	@Override
	public boolean canRead(String mimeType) {
		return ArrayUtilities.containsEquals(TYPES, mimeType);
	}

	@Override
	public boolean canWrite(String mimeType) {
		return false;
	}
}
