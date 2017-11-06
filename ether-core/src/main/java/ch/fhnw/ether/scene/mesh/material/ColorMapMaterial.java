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

package ch.fhnw.ether.scene.mesh.material;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.util.color.RGBA;

public final class ColorMapMaterial extends AbstractMaterial {

	private RGBA color;
	private IGPUImage colorMap;

	public ColorMapMaterial() {
		this(IGPUImage.TRANSPARENT_1x1);
	}

	public ColorMapMaterial(IGPUImage colorMap) {
		this(RGBA.WHITE, colorMap);
	}

	public ColorMapMaterial(RGBA color, IGPUImage colorMap) {
		this(color, colorMap, false);
	}

	public ColorMapMaterial(RGBA color, IGPUImage colorMap, boolean perVertexColor) {
		super(provide(IMaterial.COLOR, IMaterial.COLOR_MAP), require(IGeometry.POSITION_ARRAY,
				perVertexColor ? IGeometry.COLOR_ARRAY : null, IGeometry.COLOR_MAP_ARRAY));

		this.color = color;
		this.colorMap = colorMap;
	}

	public RGBA getColor() {
		return color;
	}

	public void setColor(RGBA color) {
		this.color = color;
		updateRequest();
	}

	public IGPUImage getColorMap() {
		return colorMap;
	}

	public void setColorMap(IGPUImage colorMap) {
		this.colorMap = colorMap;
		updateRequest();
	}

	@Override
	public Object[] getData() {
		return data(color, colorMap);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + color + ", " + colorMap + "]";
	}
}
