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
import java.util.HashSet;
import java.util.Set;

import ch.fhnw.ether.formats.ModelFace;
import ch.fhnw.ether.formats.ModelGroup;
import ch.fhnw.ether.formats.ModelObject;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.util.TextUtilities;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;

public final class ObjWriter {
	private final ModelObject object;
	private final PrintWriter out;
	private final PrintWriter mtl;
	private final Set<String> materials = new HashSet<>();
	
	public ObjWriter(File file) throws FileNotFoundException {
		URL resource = null;
		try {
			resource = file.toURI().toURL();
		} catch (Exception e) {
		}
		object = new ModelObject(resource);
		out    = new PrintWriter(file);
		mtl    = new PrintWriter(TextUtilities.getFileNameWithoutExtension(file) + ".mtl");
	}

	public void addMesh(IMesh mesh) {
		if (mesh.getType() != Primitive.TRIANGLES)
			return;

		ModelGroup g = new ModelGroup(mesh.getName());

		if(materials.add(mesh.getMaterial().getName()))
			writeMaterial(mesh.getMaterial());

		float[] data = mesh.getTransformedPositionData();

		int vidx = 0;
		boolean hasTextures = mesh.getMaterial() instanceof ColorMapMaterial;
		int[] vi = new int[3];
		for (int i = 0; i < data.length; i += 9) {			
			vi[0] = vidx++;
			vi[1] = vidx++;
			vi[2] = vidx++;
			g.addFace(new ModelFace(vi, null, hasTextures ? vi : null));
		}
		addGroup(g);
	}

	private void writeMaterial(IMaterial material) {
		mtl.println("newmtl Material_" + material.getName());
		if(material instanceof ColorMapMaterial) {
			mtl.println("illum 4");
			mtl.println("Ka 0.00 0.00 0.00");
			mtl.println("Kd 1.00 1.00 1.00");
			mtl.println("Ks 0.00 0.00 0.00");
			mtl.println("Tf 1.00 1.00 1.00");
			mtl.println("Ni 1.00");
			mtl.println("map_Kd " + material.getName() + ".png");
		} else if(material instanceof ColorMaterial) {
			//.... add color material support
		}
	}

	public void addV(Vec3 vec) {
		object.getVertices().add(vec);
	}

	public void addN(Vec3 vec) {
		object.getNormals().add(vec);
	}

	public void addGroup(ModelGroup g) {
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
		mtl.close();
	}

	private void writeGroup(ModelGroup group, PrintWriter out) {
		out.println("g " + group.getName());
		out.println("usmtl Material_" + group.getName());
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
