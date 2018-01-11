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

package ch.fhnw.ether.render.shader.base;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import ch.fhnw.ether.render.IVertexBuffer;
import ch.fhnw.ether.render.gl.Program;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.variable.IShaderArray;
import ch.fhnw.ether.render.variable.IShaderUniform;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.util.Log;

public abstract class AbstractShader implements IShader {
	private static final Log LOG = Log.create();

	public static final String INLINE = "/*__inline__*/\n";

	// important: keep this in sync with Primitive enum in IMesh
	public static final int[] MODE = { 
			GL11.GL_POINTS, GL11.GL_LINES, GL11.GL_LINE_STRIP, GL11.GL_LINE_LOOP,
			GL11.GL_TRIANGLES, GL11.GL_TRIANGLE_STRIP, GL11.GL_TRIANGLE_FAN 
	};

	private final Class<?> root;
	private final String name;
	private final String[] source;
	private final Primitive type;
	private Program program;

	private List<IShaderUniform<?>> uniforms = new ArrayList<>();
	private List<IShaderArray<?>> arrays = new ArrayList<>();

	protected AbstractShader(Class<?> root, String name, String source, Primitive type) {
		this.root = root;
		this.name = name;
		this.source = new String[] { source };
		this.type = type;
	}

	protected AbstractShader(Class<?> root, String name, String vert, String frag, String geom, Primitive type) {
		this.root = root;
		this.name = name;
		this.source = new String[] { vert, frag, geom };
		this.type = type;
	}

	@Override
	public final String id() {
		return name;
	}

	@Override
	public final void update(Object[] uniformData) {
		if (program == null) {
			String vertShader;
			String fragShader;
			String geomShader;
			if (source.length == 1) {
				vertShader = source[0] + "_vert.glsl";
				fragShader = source[0] + "_frag.glsl";
				geomShader = source[0] + "_geom.glsl";
			} else {
				vertShader = INLINE + source[0];
				fragShader = INLINE + source[1];
				geomShader = INLINE + source[2];
			}
			try {
				program = Program.create(root, vertShader, fragShader, geomShader, System.err);
			} catch (Throwable t) {
				LOG.severe("cannot create glsl program. exiting.", t);
				System.exit(1);
			}
		}
		uniforms.forEach(attr -> attr.update(uniformData));
	}

	@Override
	public final void enable() {
		// enable program & uniforms (set uniforms, enable textures, change gl
		// state)
		program.enable();
		uniforms.forEach(attr -> attr.enable(program));
	}

	@Override
	public final void render(IVertexBuffer buffer) {
		buffer.bind();
		arrays.forEach(attr -> attr.enable(program, buffer));
				
		int mode = MODE[type.ordinal()];
		GL11.glDrawArrays(mode, 0, buffer.getNumVertices());

		arrays.forEach(attr -> attr.disable(program, buffer));
		buffer.unbind();
	}

	@Override
	public final void disable() {
		// disable program and uniforms (disable textures, restore gl state)
		uniforms.forEach(attr -> attr.disable(program));
		program.disable();
	}

	@Override
	public final List<IShaderUniform<?>> getUniforms() {
		return uniforms;
	}

	@Override
	public List<IShaderArray<?>> getArrays() {
		return arrays;
	}

	protected final void addUniform(IShaderUniform<?> uniform) {
		uniforms.add(uniform);
	}

	protected final void addArray(IShaderArray<?> array) {
		arrays.add(array);
	}

	@Override
	public String toString() {
		return id();
	}
}
