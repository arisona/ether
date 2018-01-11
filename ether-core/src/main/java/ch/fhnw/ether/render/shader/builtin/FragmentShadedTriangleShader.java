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

package ch.fhnw.ether.render.shader.builtin;

import java.util.Collection;

import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.base.AbstractShader;
import ch.fhnw.ether.render.variable.base.BooleanUniform;
import ch.fhnw.ether.render.variable.base.FloatUniform;
import ch.fhnw.ether.render.variable.base.Vec3FloatUniform;
import ch.fhnw.ether.render.variable.builtin.ColorMapArray;
import ch.fhnw.ether.render.variable.builtin.ColorMapUniform;
import ch.fhnw.ether.render.variable.builtin.LightUniformBlock;
import ch.fhnw.ether.render.variable.builtin.NormalArray;
import ch.fhnw.ether.render.variable.builtin.PositionArray;
import ch.fhnw.ether.render.variable.builtin.ViewUniformBlock;
import ch.fhnw.ether.scene.attribute.IAttribute;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.IMaterial;

public class FragmentShadedTriangleShader extends AbstractShader {
	public FragmentShadedTriangleShader(Collection<IAttribute> attributes, Primitive primitive) {
		super(IShader.class, "builtin.shader.fragment_shaded_triangles", "/shaders/fragment_shaded_vct", primitive);

		boolean useTexture = attributes.contains(IGeometry.COLOR_MAP_ARRAY);

		addArray(new PositionArray());
		addArray(new NormalArray());

		if (useTexture)
			addArray(new ColorMapArray());

		addUniform(new BooleanUniform("shader.color_map_flag", "useColorMap", () -> useTexture));

		addUniform(new Vec3FloatUniform(IMaterial.EMISSION, "material.emissionColor"));
		addUniform(new Vec3FloatUniform(IMaterial.AMBIENT, "material.ambientColor"));
		addUniform(new Vec3FloatUniform(IMaterial.DIFFUSE, "material.diffuseColor"));
		addUniform(new Vec3FloatUniform(IMaterial.SPECULAR, "material.specularColor"));
		addUniform(new FloatUniform(IMaterial.SHININESS, "material.shininess"));
		addUniform(new FloatUniform(IMaterial.STRENGTH, "material.strength"));
		addUniform(new FloatUniform(IMaterial.ALPHA, "material.alpha"));
		
		if (useTexture)
			addUniform(new ColorMapUniform());

		addUniform(new ViewUniformBlock());
		addUniform(new LightUniformBlock());
	}
}
