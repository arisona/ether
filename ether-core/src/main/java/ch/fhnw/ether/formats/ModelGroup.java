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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.util.color.RGB;

public final class ModelGroup {
	private final String name;
	private final List<ModelFace> faces = new ArrayList<>();

	private ModelMaterial material;
	private int           vertexCount;
	private int           normalCount;
	private int           uvCount;
	
	public ModelGroup(String name) {
		this.name        = name;
	}

	public String getName() {
		return name;
	}

	public ModelMaterial getMaterial() {
		return material;
	}

	public IMaterial getMaterial(Map<ModelMaterial, IMaterial> cache) {
		IMaterial result = cache.get(material);
		if (result == null) {
			if (material != null) {
				result = new ShadedMaterial(material.getEmissionColor(), material.getAmbientColor(), material.getDiffuseColor(), material.getSpecularColor(), material.getShininess(), 1, 1, material.getTexture());
				result.setName(material.getName());
			} else {
				result = new ShadedMaterial(RGB.WHITE);
			}
			cache.put(material,  result);
		}
		return result;
	}

	public void setMaterial(ModelMaterial material) {
		this.material = material;
	}

	public List<ModelFace> getFaces() {
		return faces;
	}

	public void addFace(ModelFace face) {
		faces.add(face);
	}
	
	public int getNormalCount() {
		return normalCount;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public int getUVCount() {
		return uvCount;
	}

	public void setCounts(int vertexCount, int normalCount, int uvCount) {
		this.vertexCount = vertexCount;
		this.normalCount = normalCount;
		this.uvCount     = uvCount;		
	}
}
