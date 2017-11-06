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

package ch.fhnw.ether.formats;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.util.Log;

// XXX where should we cache images / textures??? also, images are mutable, so we need to be careful (e.g. cache a copy)
public final class TextureLoader {
	private static final Log log = Log.create();

	private TextureLoader() {
	}

	private static final Map<String, IGPUImage> IMAGE_CACHE = new HashMap<>();

	public static IGPUImage loadTexture(String path) {
		IGPUImage image = IMAGE_CACHE.get(path);
		if (image == null) {
			try {
				image = IGPUImage.read(new File(path));
				IMAGE_CACHE.put(path, image);
			} catch (Exception e) {
				log.warning("can't load texture image: " + path);
			}
		}
		return image;
	}

	public static IGPUImage loadTexture(InputStream in) {
		try {
			return IGPUImage.read(in);
		} catch (Exception e) {
			log.warning("can't load texture image");
			return null;
		}
	}
}
