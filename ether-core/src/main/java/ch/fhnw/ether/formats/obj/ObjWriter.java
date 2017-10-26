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

package ch.fhnw.ether.formats.obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.formats.ModelFace;
import ch.fhnw.ether.formats.ModelGroup;
import ch.fhnw.ether.formats.ModelObject;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;

public final class ObjWriter {
	private final List<IMesh> meshes = new ArrayList<>();
	private final ModelObject object;
	private final PrintWriter out;

	public ObjWriter(File file) throws FileNotFoundException {
		URL resource = null;
		try {
			resource = file.toURI().toURL();
		} catch (Exception e) {
		}
		object = new ModelObject(resource);
		out = new PrintWriter(file);
	}

	// TODO: normal, texcoord, material handling
	public void addMesh(IMesh mesh) {
		if (mesh.getType() != Primitive.TRIANGLES)
			return;

		meshes.add(mesh);
		ModelGroup g = new ModelGroup(mesh.getName());
		final List<Vec3> vs = object.getVertices();
		float[] data = mesh.getTransformedPositionData();
		for (int i = 0; i < data.length; i += 9) {
			int[] vi = new int[3];
			vs.add(new Vec3(data[i + 0], data[i + 1], data[i + 2]));
			vi[0] = vs.size();
			vs.add(new Vec3(data[i + 3], data[i + 4], data[i + 5]));
			vi[1] = vs.size();
			vs.add(new Vec3(data[i + 6], data[i + 7], data[i + 8]));
			vi[2] = vs.size();
			g.addFace(new ModelFace(vi, null, null));
		}
		object.addGroup(g);
	}

	public void write() {
		for (Vec3 v : object.getVertices())
			out.println("v " + v.x + " " + v.y + " " + v.z);
		for (Vec2 t : object.getTexCoords())
			out.println("vt " + t.x + " " + t.y);
		for (Vec3 n : object.getNormals())
			out.println("vn " + n.x + " " + n.y + " " + n.z);
		for (ModelGroup g : object.getGroups())
			writeGroup(g, out);
		out.close();
	}

	private void writeGroup(ModelGroup group, PrintWriter out) {
		out.println("g " + group.getName());
		for (ModelFace f : group.getFaces())
			writeFace(f, out);
	}

	private void writeFace(ModelFace face, PrintWriter out) {
		StringBuilder result = new StringBuilder();
		result.append("f ");
		int[] vIndices = face.getVertexIndices();
		int[] nIndices = face.getNormalIndices();
		int[] tIndices = face.getTexCoordIndices();
		for (int i = 0; i < vIndices.length; i++) {
			result.append(vIndices[i]);
			if (tIndices != null) {
				result.append('/');
				result.append(tIndices[i]);
			}
			if (nIndices != null) {
				result.append(tIndices == null ? "//" : "/");
				result.append(nIndices[i]);
			}
			result.append(' ');
		}
		out.println(result.toString());
	}
}
