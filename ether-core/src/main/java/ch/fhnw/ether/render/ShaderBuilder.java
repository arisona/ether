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

package ch.fhnw.ether.render;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.builtin.FragmentShadedTriangleShader;
import ch.fhnw.ether.render.shader.builtin.LineShader;
import ch.fhnw.ether.render.shader.builtin.PointShader;
import ch.fhnw.ether.render.shader.builtin.UnshadedTriangleShader;
import ch.fhnw.ether.render.variable.IShaderUniform;
import ch.fhnw.ether.scene.attribute.IAttribute;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.ICustomMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.util.Pair;

public final class ShaderBuilder {
	private static final class Attributes {
		final Map<IAttribute, Pair<Integer, Supplier<?>>> attributes = new HashMap<>();

		void provide(IAttribute attribute, Pair<Integer, Supplier<?>> link) {
			if (attributes.put(attribute, link) != null)
				throw new IllegalArgumentException("duplicate attribute: " + attribute);
		}
		
		void require(IAttribute attribute) {
			if (attributes.put(attribute, null) != null)
				throw new IllegalArgumentException("duplicate attribute: " + attribute);			
		}

		Pair<Integer, Supplier<?>> getSupplier(IShader shader, IShaderUniform<?> uniform) {
			for (Entry<IAttribute, Pair<Integer, Supplier<?>>> entry : attributes.entrySet()) {
				if (entry.getKey().id().equals(uniform.id()))
					return entry.getValue();
			}
			throw new IllegalArgumentException("shader " + shader + " requires uniform attribute " + uniform.id());
		}

		@Override
		public String toString() {
			final StringBuffer s = new StringBuffer();
			attributes.forEach((key, value) -> s.append("[").append(key).append(", ").append(value).append("] "));
			return s.toString();
		}
	}

	@SuppressWarnings("unchecked")
	public static <S extends IShader> S create(S shader, IMesh mesh, Map<IAttribute, Supplier<?>> globals) {
		Attributes attributes = new Attributes();

		// add material & geometry attributes
		if (mesh != null) {
			IAttribute[] provided = mesh.getMaterial().getProvidedAttributes();
			for (int i = 0; i < provided.length; ++i)
				attributes.provide(provided[i], new Pair<>(i, null));

			for (IAttribute required : mesh.getMaterial().getRequiredAttributes())
				attributes.require(required);
		}

		// add global attributes
		if (globals != null)
			globals.forEach((attribute, supplier) -> attributes.provide(attribute, new Pair<>(-1, supplier)));

		// create shader and attach all attributes this shader requires
		if (shader == null)
			shader = (S) createShader(mesh, Collections.unmodifiableSet(attributes.attributes.keySet()));

		// attach material attributes to uniforms
		for (IShaderUniform<?> uniform : shader.getUniforms()) {
			if (!uniform.isLinked()) {
				Pair<Integer, Supplier<?>> link = attributes.getSupplier(shader, uniform);
				if (link.first == -1)
					uniform.setSupplier(link.second);
				else
					uniform.setIndex(link.first);
			}
		}

		return shader;
	}

	// as soon as we have more builtin shaders we should move to a more flexible
	// scheme, e.g. derive shader from provided attributes
	private static IShader createShader(IMesh mesh, Collection<IAttribute> attributes) {
		IMaterial material = mesh.getMaterial();
		if (material instanceof ICustomMaterial) {
			return ((ICustomMaterial) material).getShader();
		}

		switch (mesh.getType()) {
		case POINTS:
			return new PointShader(attributes);
		case LINES:
		case LINE_STRIP:
		case LINE_LOOP:
			return new LineShader(attributes, mesh.getType());
		case TRIANGLES:
		case TRIANGLE_STRIP:
		case TRIANGLE_FAN:
			if (material instanceof ColorMaterial || material instanceof ColorMapMaterial) {
				return new UnshadedTriangleShader(attributes, mesh.getType());
			} else if (material instanceof ShadedMaterial) {
				//return new FlatShadedTriangleShader(attributes);
				return new FragmentShadedTriangleShader(attributes, mesh.getType());
			}
		}
		throw new UnsupportedOperationException("cant create shader for mesh: " + mesh);
	}
}
